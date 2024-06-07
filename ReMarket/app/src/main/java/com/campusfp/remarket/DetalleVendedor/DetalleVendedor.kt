package com.campusfp.remarket.DetalleVendedor

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.campusfp.remarket.Adaptadores.AdaptadorAnuncio
import com.campusfp.remarket.Comentarios
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.Modelo.ModeloAnuncio
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityDetalleVendedorBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleVendedor : AppCompatActivity() {

    private var uidVendedor = ""

    private lateinit var binding: ActivityDetalleVendedorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidVendedor = intent.getStringExtra("uidVendedor").toString()

        cargarInfoVendedor()
        cargarAnunciosVendedor()

        binding.IvComentarios.setOnClickListener {
            val intent = Intent(this, Comentarios::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun cargarAnunciosVendedor() {

        // Añadir los anuncios del vendedor al array list y mostrarlos
        val anuncioArrayList : ArrayList<ModeloAnuncio> = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.orderByChild("uid").equalTo(uidVendedor)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    anuncioArrayList.clear()

                    for (ds in snapshot.children) {
                        try {

                            val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                            anuncioArrayList.add(modeloAnuncio!!)
                        } catch (e:Exception) {

                        }
                    }

                    val adaptador = AdaptadorAnuncio(this@DetalleVendedor, anuncioArrayList)
                    binding.anunciosRV.adapter = adaptador

                    val contador_anuncios = "${anuncioArrayList.size}"
                    binding.TvNAnuncios.text = contador_anuncios
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }


    private fun cargarInfoVendedor() {

        // Almacenar y mostrar la info del vendedor
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val tiempo_r = snapshot.child("tiempo").value as Long

                    val formato_fecha = Constantes.obtenerFecha(tiempo_r)

                    binding.TvNombres.text = nombres
                    binding.TvMiembro.text = formato_fecha

                    try {

                       Glide.with(this@DetalleVendedor).load(imagen).placeholder(R.drawable.img_perfil).into(binding.IvVendedor)
                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}