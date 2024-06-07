package com.campusfp.remarket

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.Anuncios.CrearAnuncio
import com.campusfp.remarket.Fragmentos.FragmentChats
import com.campusfp.remarket.Fragmentos.FragmentCuenta
import com.campusfp.remarket.Fragmentos.FragmentInicio
import com.campusfp.remarket.Fragmentos.FragmentMisAnuncios
import com.campusfp.remarket.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private  lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        verFragmentInicio()

        // Navegacion de la barra inferior
        binding.BottomNV.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.Item_Inicio->{
                    verFragmentInicio()
                    true
                }

                R.id.Item_Chats->{
                    verFragmentChats()
                    true
                }

                R.id.Item_Mis_Anuncios->{
                    verFragmentMisAnuncios()
                    true
                }

                R.id.Item_Cuenta->{
                    verFragmentCuenta()
                    true
                }

                else->{
                    false
                }
            }

        }

        binding.FAB.setOnClickListener {
            val intent = Intent(this, CrearAnuncio::class.java)
            intent.putExtra("Edicion", false)
            startActivity(intent)
        }

    }

    // Si no hay ningun usuario activo se inicia Opciones Login
    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        } else {
            agregarFcmToken()
            solicitarPermisoNotif()
        }
    }

    private fun verFragmentInicio(){
        binding.TituloRl.text = "Inicio"

        // Reemplazar el fragment dentro del frame layout
        val fragment = FragmentInicio()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransition.commit()
    }

    private fun verFragmentChats(){
        binding.TituloRl.text = "Chats"

        // Reemplazar el fragment dentro del frame layout
        val fragment = FragmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmentTransition.commit()
    }

    private fun verFragmentMisAnuncios(){
        binding.TituloRl.text = "Anuncios"

        // Reemplazar el fragment dentro del frame layout
        val fragment = FragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmentTransition.commit()
    }

    private fun verFragmentCuenta(){
        binding.TituloRl.text = "Cuenta"

        // Reemplazar el fragment dentro del frame layout
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }

    private fun agregarFcmToken() {

        // Agregar un token a cada usuario
        val mUid = "${firebaseAuth.uid}"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {fcmToken->

                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
                ref.child(mUid)
                    .updateChildren(hashMap)

                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e->
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun solicitarPermisoNotif() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==

                PackageManager.PERMISSION_DENIED) {
                    permisoNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permisoNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()) {esConcedido->

    }
}