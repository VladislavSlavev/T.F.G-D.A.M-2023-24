package com.campusfp.remarket

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.databinding.ActivityLoginEmailBinding
import com.campusfp.remarket.databinding.ActivityRegistroEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registro_email : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroEmailBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnRegistrar.setOnClickListener{
            validarInfo()
        }

    }

    private var email = ""
    private var password = ""
    private var r_password = ""

    // Comprobar la informacion del usuario
    private fun validarInfo() {
        email = binding.EtEmail.text.toString().trim()
        password = binding.EtPassword.text.toString().trim()
        r_password = binding.EtRPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.EtEmail.error = "Email no válido"
            binding.EtEmail.requestFocus()
        }
        else if (email.isEmpty()){
            binding.EtEmail.error = "Ingresa un email"
            binding.EtEmail.requestFocus()
        }
        else if (password.isEmpty()){
            binding.EtPassword.error = "Ingresa una contraseña"
            binding.EtPassword.requestFocus()
        }
        else if (password.count() < 6){
            binding.EtPassword.error = "Ingresa al menos 6 carácteres"
            binding.EtPassword.requestFocus()
        }
        else if (r_password.isEmpty()){
            binding.EtRPassword.error = "Repite la contraseña"
            binding.EtRPassword.requestFocus()
        }
        else if (password != r_password){
            binding.EtRPassword.error = "Las contraseñas no coinciden"
            binding.EtRPassword.requestFocus()
        }
        else{
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        // Funcion para crear usuario de firebase llamando al metodo que recoge los datos en un hashmap
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                llenarInfoBD()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se ha registrado al usuario debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Crear los datos que va a tener el usuario en la BD
    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val tiempo = Constantes.obtenerTiempoDis()
        val emailUsuario = firebaseAuth.currentUser!!.email
        val uidUsuario = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["nombres"] = ""
        hashMap["codigoTelefono"] = ""
        hashMap["telefono"] = ""
        hashMap["urlImagenPerfil"] = ""
        hashMap["proveedor"] = "Email"
        hashMap["tiempo"] = tiempo
        hashMap["email"] = "$emailUsuario"
        hashMap["uid"] = "$uidUsuario"
        hashMap["fecha_nac"] = ""

        // Una vez se realice el registro se inicia Main Activity
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidUsuario!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se ha registrado debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}