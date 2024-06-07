package com.campusfp.remarket.Opciones_login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.MainActivity
import com.campusfp.remarket.R
import com.campusfp.remarket.RecuperarPassword
import com.campusfp.remarket.Registro_email
import com.campusfp.remarket.databinding.ActivityLoginEmailBinding
import com.campusfp.remarket.databinding.ActivityOpcionesLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login_email : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnIngresar.setOnClickListener {
            validarInfo()
        }

        binding.TxtRegistrarme.setOnClickListener {
            startActivity(Intent(this@Login_email, Registro_email::class.java))
        }

        binding.TvRecuperar.setOnClickListener {
            startActivity(Intent(this@Login_email, RecuperarPassword::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validarInfo() {

        // Validar los campos introducidos
        email = binding.EtEmail.text.toString().trim()
        password = binding.EtPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmail.error = "Email inválido"
            binding.EtEmail.requestFocus()
        }
        else if (email.isEmpty()) {
            binding.EtEmail.error = "Ingresa un email"
            binding.EtEmail.requestFocus()
        }
        else if (password.isEmpty()) {
            binding.EtPassword.error = "Ingresa una contraseña"
            binding.EtPassword.requestFocus()
        }
        else {
            loginUsuario()
        }
    }

    private fun loginUsuario() {

        // Comprobar las credenciales en firebase
        progressDialog.setMessage("Entrando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Email o contraseña incorrectos ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}