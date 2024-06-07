package com.campusfp.remarket

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.databinding.ActivityRecuperarPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperarPassword : AppCompatActivity() {

    private lateinit var binding : ActivityRecuperarPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        // Botones
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnEnviarInst.setOnClickListener {
            validarEmail()
        }
    }

    private var email = ""

    private fun validarEmail() {

        // Validar los campos
        email = binding.EtEmail.text.toString().trim()

        if (email.isEmpty()) {

            Toast.makeText(this, "Ingresa un correo electrónico", Toast.LENGTH_SHORT).show()

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            // El email no corresponde
            binding.EtEmail.error = "Email inválido"
            binding.EtEmail.requestFocus()

        } else {
            enviarInstrucciones()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun enviarInstrucciones() {

        progressDialog.setMessage("Envíando las instrucciones a $email")
        progressDialog.dismiss()

        // Se usa el metodo de recuperacion de firebase con el email introducido
        firebaseAuth.sendPasswordResetEmail(email)

            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Se han envíado las instrucciones de recuperación", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->

                progressDialog.dismiss()
                Toast.makeText(this, "No se han podido envíar las instrucciones ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}