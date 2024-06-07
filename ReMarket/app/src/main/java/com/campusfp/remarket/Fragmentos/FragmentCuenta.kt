package com.campusfp.remarket.Fragmentos

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.campusfp.remarket.CambiarPassword
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.EditarPerfil
import com.campusfp.remarket.OpcionesLogin
import com.campusfp.remarket.Opciones_login.Login_email
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityRegistroEmailBinding
import com.campusfp.remarket.databinding.FragmentCuentaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentCuenta : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        leerInfo()

        // Botones
        binding.BtnEditarPerfil.setOnClickListener {
            startActivity(Intent(mContext, EditarPerfil::class.java))
        }

        binding.BtnCambiarPass.setOnClickListener {
            startActivity(Intent(mContext, CambiarPassword::class.java))
        }

        binding.BtnVerificar.setOnClickListener {
            verificarCuenta()
            firebaseAuth.signOut()
            startActivity(Intent(mContext, Login_email::class.java))
        }

        binding.BtnEliminarAnuncios.setOnClickListener {
            val alertDialog = MaterialAlertDialogBuilder(mContext)
            alertDialog.setTitle("Eliminar todos mis anuncios")

                .setMessage("¿Quieres eliminar todos tus anuncios?")
                .setPositiveButton("Eliminar"){dialog, which->
                    eliminarTodosAnuncios()
                }
                .setNegativeButton("Cancelar"){dialog, which->
                    dialog.dismiss()
                }
                .show()
        }

        binding.BtnCerrarSesion.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContext, OpcionesLogin::class.java))
            activity?.finishAffinity()
        }
    }

    // Eliminar todos los anuncios del usuario
    private fun eliminarTodosAnuncios() {

        val miUid = firebaseAuth.uid
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").orderByChild("uid").equalTo(miUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    ds.ref.removeValue()
                }

                Toast.makeText(mContext, "Se han eliminado todos sus anuncios",Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // Sacar los datos de la BD y almacenarlos en variables para mostrarlos
    private fun leerInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    var tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    // Codigo tel + telefono
                    val cod_tel = codTelefono + " " + telefono

                    if (tiempo == "null") {
                        tiempo = "0"
                    }

                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    // Asignar informacion

                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    // Asignar imagen
                    try {
                        Glide.with(mContext).load(imagen).placeholder(R.drawable.img_perfil).into(binding.IvPerfil)

                    } catch (e:Exception) {
                        Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    // Si el proveedor es email se crea una variable la cual almacena saber si el usuario esta verificado por email
                    // dependiendo de la respuesta se muestra el mensaje indicado en la vista EstadoCuenta
                    // Si el proveedor es google se muestra el estado verificado
                    if (proveedor == "Email") {

                        val esVerificado = firebaseAuth.currentUser!!.isEmailVerified

                        if (esVerificado) {
                            //Si el usuario esta verificado se esconde el boton
                            binding.BtnVerificar.visibility = View.GONE
                            binding.TvEstadoCuenta.text = "Verificado"

                        } else {
                            binding.BtnVerificar.visibility = View.VISIBLE
                            binding.TvEstadoCuenta.text = "No verificado"
                        }
                    } else {
                        binding.BtnVerificar.visibility = View.GONE
                        binding.TvEstadoCuenta.text = "Verificado"
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // Se envia un email de firebase para verificar la cuenta
    private fun verificarCuenta() {

        progressDialog.setMessage("Envíando email de verificación al su correo")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()

            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(mContext, "Se ha envíado el email de verificación", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->

                progressDialog.dismiss()
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}