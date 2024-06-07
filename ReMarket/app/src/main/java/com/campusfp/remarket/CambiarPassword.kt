package com.campusfp.remarket

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.campusfp.remarket.Opciones_login.Login_email
import com.campusfp.remarket.databinding.ActivityCambiarPasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class CambiarPassword : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnActualizarPass.setOnClickListener {
            validarInfo()
        }
    }

    private var pass_actual = ""
    private var pass_nueva = ""
    private var pass_r = ""

    private fun validarInfo() {

        // Validar los campos
        pass_actual = binding.EtPassActual.text.toString().trim()
        pass_nueva = binding.EtPassNueva.text.toString().trim()
        pass_r = binding.EtPassR.text.toString().trim()

        if (pass_actual.isEmpty()) {

            binding.EtPassActual.error = "Ingresa tu contraseña actual"
            binding.EtPassActual.requestFocus()

        } else if (pass_nueva.isEmpty()) {

            binding.EtPassNueva.error = "Ingresa una nueva contraseña nueva"
            binding.EtPassNueva.requestFocus()

        } else if (pass_r.isEmpty()) {

            binding.EtPassR.error = "Repite tu contraseña nueva"
            binding.EtPassR.requestFocus()

        } else if (pass_nueva != pass_r) {

            binding.EtPassR.error = "La contraseñas no coinciden"
            binding.EtPassR.requestFocus()

        } else {

            autenticarUsuarioCamPass()
        }
    }

    private fun autenticarUsuarioCamPass() {

        // Verficar el usuario en firebase
        progressDialog.setMessage("Autenticando Usuario")
        progressDialog.show()

        val autoCredencial = EmailAuthProvider.getCredential(firebaseUser.email.toString(), pass_actual)
        firebaseUser.reauthenticate(autoCredencial)

            .addOnSuccessListener {
                progressDialog.dismiss()
                actualizarPass()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Cambiar la contraseña si se ha verificado correctamente el usuario
    private fun actualizarPass(){

        progressDialog.setMessage("Cambiando la contraseña")
        progressDialog.show()

        firebaseUser.updatePassword(pass_nueva)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Se ha actualizado la contraseña", Toast.LENGTH_SHORT).show()

                firebaseAuth.signOut()
                startActivity(Intent(this, Login_email::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}