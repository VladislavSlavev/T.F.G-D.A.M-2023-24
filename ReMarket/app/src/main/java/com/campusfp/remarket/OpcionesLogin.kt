package com.campusfp.remarket

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.Opciones_login.Login_email
import com.campusfp.remarket.databinding.ActivityMainBinding
import com.campusfp.remarket.databinding.ActivityOpcionesLoginBinding
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.nio.file.attribute.AclEntry.Builder

class OpcionesLogin : AppCompatActivity() {

    private lateinit var binding: ActivityOpcionesLoginBinding

    private  lateinit var  firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        // Metodo de inicio de sesion con google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        comprobarSesion()

        binding.IngresarEmail.setOnClickListener {
            startActivity(Intent(this@OpcionesLogin, Login_email::class.java))
        }

        binding.IngresarGoogle.setOnClickListener {
            mGoogleSignInClient.revokeAccess() // Denegar el acceso a la aplicación para poder tener el popup de seleccion de cuentas
            googleLogin()
        }

    }

    // Iniciar la vista de inicio de sesion
    private fun googleLogin() {
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    // Datos del inicio de sesion con la vista de google
    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {resultado->
            if (resultado.resultCode == RESULT_OK) {
                val data = resultado.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val cuenta = task.getResult(ApiException::class.java)
                    autenticacionGoogle(cuenta.idToken)

                } catch (e:Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun autenticacionGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)

            // Se registra al usuario con todos sus datos en la BD si no existe, si existe se inicia Main Activity
            .addOnSuccessListener {resultadoAuth->
                if (resultadoAuth.additionalUserInfo!!.isNewUser) {
                    llenarInfoBD()
                } else {
                    startActivity(Intent(this ,MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun llenarInfoBD() {

        progressDialog.setMessage("Guardando información")

        val tiempo = Constantes.obtenerTiempoDis()
        val emailUsuario = firebaseAuth.currentUser!!.email
        val uidUsuario = firebaseAuth.uid
        val nombreUsuario = firebaseAuth.currentUser?.displayName

        // Hashmap de los datos del usuario
        val hashMap = HashMap<String, Any>()
        hashMap["nombres"] = "$nombreUsuario"
        hashMap["codigoTelefono"] = ""
        hashMap["telefono"] = ""
        hashMap["urlImagenPerfil"] = ""
        hashMap["proveedor"] = "Google"
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

    // Si el usuario esta registrado se inicia Main Activity
    private  fun comprobarSesion() {
        if(firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}