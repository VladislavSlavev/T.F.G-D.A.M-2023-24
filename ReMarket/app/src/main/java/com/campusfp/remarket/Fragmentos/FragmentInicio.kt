package com.campusfp.remarket.Fragmentos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.campusfp.remarket.Adaptadores.AdaptadorAnuncio
import com.campusfp.remarket.Adaptadores.AdaptadorCategoria
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.Modelo.ModeloAnuncio
import com.campusfp.remarket.Modelo.ModeloCategoria
import com.campusfp.remarket.R
import com.campusfp.remarket.RvListenerCategoria
import com.campusfp.remarket.SeleccionarUbicacion
import com.campusfp.remarket.databinding.FragmentInicioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentInicio : Fragment() {

    private lateinit var binding: FragmentInicioBinding

    // Radio de distancia para mostrar los anuncios
    private var MAX_DISTANCIA_MOSTRAR_ANUNCIO = 10


    private lateinit var mContext : Context

    private lateinit var anuncioArrayList: ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio : AdaptadorAnuncio
    private lateinit var localizacionSP : SharedPreferences

    private var actualLatitud = 0.0
    private var actualLongitud = 0.0
    private var actualDireccion = ""


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentInicioBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // Variables en las que se guardan los datos de la ubicacion
        localizacionSP = mContext.getSharedPreferences("LOCACION_SP", Context.MODE_PRIVATE)

        actualLatitud = localizacionSP.getFloat("ACTUAL_LATITUD", 0.0f).toDouble()
        actualLongitud = localizacionSP.getFloat("ACTUAL_LONGITUD", 0.0f).toDouble()
        actualDireccion = localizacionSP.getString("ACTUAL_DIRECCION", "")!!

        val distanciaSlider = binding.distanciaSlider
        val distanciaTextView = binding.distanciaTextView

        distanciaTextView.text = distanciaSlider.value.toInt().toString()

        distanciaSlider.addOnChangeListener { _, value, _ ->

            // Actualizar MAX_DISTANCIA_MOSTRAR_ANUNCIO con el valor del Slider
            MAX_DISTANCIA_MOSTRAR_ANUNCIO = value.toInt()

            distanciaTextView.text = value.toInt().toString()

            // Volver a cargar los anuncios con la nueva distancia
            cargarAnuncios("Todos") // o la categoría que esté seleccionada
        }

        if (actualLatitud != 0.0 && actualLongitud !=0.0){
            binding.TvLocalizacion.text = actualDireccion
        }

        cargarCategorias()
        cargarAnuncios("Todos")

        binding.TvLocalizacion.setOnClickListener {
            val intent = Intent(mContext, SeleccionarUbicacion::class.java)
            seleccionarUbicacionARL.launch(intent)
        }

        binding.EtBuscar.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {

                // Filtro de anuncios
                try {

                    val consulta = filtro.toString()
                    adaptadorAnuncio.filter.filter(consulta)
                } catch (e:Exception) {

                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        // Limpiar el campo de busqueda
        binding.IbLimpiar.setOnClickListener {

            val consulta = binding.EtBuscar.text.toString().trim()

            if (consulta.isNotEmpty()) {

                binding.EtBuscar.setText("")
            }
        }

    }


    private val seleccionarUbicacionARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado->

        // Almacenar los datos de la ubicacion y cargar los anuncios de todas las categorias
        if (resultado.resultCode == Activity.RESULT_OK){

            val data = resultado.data
            if (data!=null){

                actualLatitud = data.getDoubleExtra("latitud", 0.0)
                actualLongitud = data.getDoubleExtra("longitud",0.0)
                actualDireccion = data.getStringExtra("direccion").toString()

                localizacionSP.edit()
                    .putFloat("ACTUAL_LATITUD", actualLatitud.toFloat())
                    .putFloat("ACTUAL_LONGITUD", actualLongitud.toFloat())
                    .putString("ACTUAL_DIRECCION", actualDireccion)
                    .apply()

                binding.TvLocalizacion.text = actualDireccion

                cargarAnuncios("Todos")

            }else{

                //Toast.makeText(context, "Cancelado",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cargar los anuncios de la categoria seleccionada
    private fun cargarCategorias(){
        val categoriaArrayList = ArrayList<ModeloCategoria>()

        // Mostrar las categorias
        for (i in 0 until Constantes.categorias.size){
            val modeloCategoria = ModeloCategoria(Constantes.categorias[i], Constantes.categoriasIcono[i])
            categoriaArrayList.add(modeloCategoria)
        }

        val adaptadorCategoria = AdaptadorCategoria(
            mContext,
            categoriaArrayList,
            object : RvListenerCategoria{
                override fun onCategoriaClick(modeloCategoria: ModeloCategoria) {
                    val categoriaSeleccionada = modeloCategoria.categoria
                    cargarAnuncios(categoriaSeleccionada)
                }
            }
        )

        binding.categoriaRV.adapter = adaptadorCategoria
    }

    private fun cargarAnuncios(categoria : String){

        // Mostrar los anuncios de la categoria elegida dentro del area de busqueda
        anuncioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                anuncioArrayList.clear()
                for (ds in snapshot.children){

                    try {

                        val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                        val distancia = calcularDistanciaKM(
                            modeloAnuncio?.latitud ?: 0.0,
                            modeloAnuncio?.longitud ?: 0.0
                        )
                        if (categoria == "Todos"){

                            if (distancia <= MAX_DISTANCIA_MOSTRAR_ANUNCIO){
                                anuncioArrayList.add(modeloAnuncio!!)
                            }
                        } else {

                            if (modeloAnuncio!!.categoria.equals(categoria)){
                                if (distancia <= MAX_DISTANCIA_MOSTRAR_ANUNCIO){
                                    anuncioArrayList.add(modeloAnuncio)
                                }
                            }
                        }
                    } catch (e:Exception){

                    }
                }
                adaptadorAnuncio = AdaptadorAnuncio(mContext, anuncioArrayList)
                binding.anunciosRv.adapter = adaptadorAnuncio

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun calcularDistanciaKM (latitud : Double , longitud : Double) : Double{

        // Sacar la ubicacion actual
        val puntoPartida = Location(LocationManager.NETWORK_PROVIDER)
        puntoPartida.latitude = actualLatitud
        puntoPartida.longitude = actualLongitud

        // Sacar la ubicacion del anuncio
        val puntoFinal = Location(LocationManager.NETWORK_PROVIDER)
        puntoFinal.latitude = latitud
        puntoFinal.longitude = longitud

        // Sacar la distancia entre los dos anteriores
        val distanciaMetros = puntoPartida.distanceTo(puntoFinal).toDouble()
        return distanciaMetros/1000
    }

}