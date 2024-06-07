package com.campusfp.remarket

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.Adaptadores.AdaptadorComentario
import com.campusfp.remarket.Modelo.ModeloComentario
import com.campusfp.remarket.databinding.ActivityComentariosBinding
import com.campusfp.remarket.databinding.ActivityDetalleVendedorBinding
import com.campusfp.remarket.databinding.CuadroAgregarComentarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Comentarios : AppCompatActivity() {

    private lateinit var binding : ActivityComentariosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var uidVendedor = ""

    private lateinit var comentarioArrayList : ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario : AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidVendedor = intent.getStringExtra("uidVendedor").toString()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IbAgregarCom.setOnClickListener {
            dialogComentar()
        }

        listarComentarios()
    }

    private fun listarComentarios() {

        // Almacenar en array list y mostrar los comentarios desde la BD
        comentarioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
        ref.child(uidVendedor).child("Comentarios")

            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    comentarioArrayList.clear()

                    for (ds in snapshot.children){

                        val modelo = ds.getValue(ModeloComentario::class.java)
                        comentarioArrayList.add(modelo!!)
                    }

                    adaptadorComentario = AdaptadorComentario(this@Comentarios, comentarioArrayList)
                    binding.RvComentarios.adapter = adaptadorComentario
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comentario = ""

    private fun dialogComentar() {

        // Ventana de dialogo con campo para escribir un comentario
        val agregar_com_binding = CuadroAgregarComentarioBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this)
        builder.setView(agregar_com_binding.root)

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)


        agregar_com_binding.IbCerrar.setOnClickListener {
            alertDialog.dismiss()
        }

        agregar_com_binding.BtnComentar.setOnClickListener {
            comentario = agregar_com_binding.EtAgregarComentario.text.toString()

            if (comentario.isEmpty()){

                Toast.makeText(this, "Ingresa un comentario", Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                agregarComentario()
            }
        }


    }

    private fun agregarComentario() {

        // Guardar el comentario escrito en la BD con sus respectivos datos
        progressDialog.setMessage("Agregando comentario")
        progressDialog.show()

        val tiempo = "${Constantes.obtenerTiempoDis()}"

        val hashMap = HashMap<String, Any> ()
        hashMap["id"] = "$tiempo"
        hashMap["tiempo"] = "$tiempo"
        hashMap["uid"] = "${firebaseAuth.uid}" //Usuario el cual está visualizando los comentarios del vendedor
        hashMap["uid_vendedor"] = uidVendedor
        hashMap["comentario"] = "${comentario}"

        val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
        ref.child(uidVendedor).child("Comentarios").child(tiempo)
            .setValue(hashMap)

            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Se ha publicado el comentario", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}