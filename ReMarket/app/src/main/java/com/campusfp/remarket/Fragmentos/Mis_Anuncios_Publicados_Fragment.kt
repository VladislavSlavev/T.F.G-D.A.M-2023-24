package com.campusfp.remarket.Fragmentos

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.campusfp.remarket.Adaptadores.AdaptadorAnuncio
import com.campusfp.remarket.Modelo.ModeloAnuncio
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.FragmentFavAnunciosBinding
import com.campusfp.remarket.databinding.FragmentMisAnunciosPublicadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Mis_Anuncios_Publicados_Fragment : Fragment() {

    private lateinit var binding: FragmentMisAnunciosPublicadosBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var anunciosArrayList: ArrayList<ModeloAnuncio>
    private lateinit var anunciosAdaptador : AdaptadorAnuncio

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentMisAnunciosPublicadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarMisAnuncios()

        // Filtro para buscar anuncios
        binding.EtBuscar.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {

                try {

                    val consulta = filtro.toString()
                    anunciosAdaptador.filter.filter(consulta)
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

    // Cargar en array list y mostrar los anuncios publicados por el usuaro
    private fun cargarMisAnuncios() {
        anunciosArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    anunciosArrayList.clear()
                    for (ds in snapshot.children) {

                        try {
                            val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                            anunciosArrayList.add(modeloAnuncio!!)
                        } catch (e:Exception) {

                        }
                    }

                    anunciosAdaptador = AdaptadorAnuncio(mContext, anunciosArrayList)
                    binding.misAnunciosRv.adapter = anunciosAdaptador
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

}