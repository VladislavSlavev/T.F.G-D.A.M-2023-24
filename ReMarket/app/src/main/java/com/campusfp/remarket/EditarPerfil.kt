package com.campusfp.remarket

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.DatePicker
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.campusfp.remarket.databinding.ActivityEditarPerfilBinding
import com.campusfp.remarket.databinding.FragmentCuentaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditarPerfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imageUri : Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarInfo()

        binding.BtnActualizar.setOnClickListener {
            validarInfo()
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IvAbrirCal.setOnClickListener {
            establecerFecha()
        }

        binding.FABCambiarImg.setOnClickListener {
            select_imagen_de()
        }
    }

    // Escoger una fecha y almacenarla
    private fun establecerFecha() {

        val mCalendario = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { datePicker, anio, mes, dia ->

            mCalendario.set(Calendar.YEAR, anio)
            mCalendario.set(Calendar.MONDAY, mes)
            mCalendario.set(Calendar.DAY_OF_MONTH, dia)

            val formato = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(formato, Locale.ENGLISH)
            binding.EtFNac.setText(sdf.format(mCalendario.time))
        }

        DatePickerDialog(this, datePicker, mCalendario.get(Calendar.YEAR), mCalendario.get(Calendar.MONTH), mCalendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    private var nombres = ""
    private var f_nac = ""
    private var cod_tlf = ""
    private var telefono = ""

    private fun validarInfo() {

        // Asignar los valores introducidos
        nombres = binding.EtNombres.text.toString().trim()
        f_nac = binding.EtFNac.text.toString().trim()
        cod_tlf = binding.selectorCod.selectedCountryCodeWithPlus
        telefono = binding.EtTelefono.text.toString().trim()

        // Validar los datos
        if(nombres.isEmpty()) {

            Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
        } else if (f_nac.isEmpty()) {

            Toast.makeText(this, "Ingresa una fecha", Toast.LENGTH_SHORT).show()
        } else if (cod_tlf.isEmpty()) {

            Toast.makeText(this, "Selecciona un código", Toast.LENGTH_SHORT).show()
        } else if (telefono.isEmpty()) {

            Toast.makeText(this, "Ingresa un teléfono", Toast.LENGTH_SHORT).show()
        } else {
            actualizarInfo()
        }

    }

    private fun actualizarInfo() {

        progressDialog.setMessage("Actualizando información")

        val hashMap : HashMap<String, Any> = HashMap()

        // Almacenar los datos nuevos en un hashmap
        hashMap["nombres"] = nombres
        hashMap["fecha_nac"] = f_nac
        hashMap["codigoTelefono"] = cod_tlf
        hashMap["telefono"] = telefono

        // Actualizar los datos del hashmap en la BD
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun cargarInfo() {

        // Sacar los datos del usuario de la BD
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"

                    // Asignar
                    binding.EtNombres.setText(nombres)
                    binding.EtFNac.setText(f_nac)
                    binding.EtTelefono.setText(telefono)

                    // Cargar foto de perfil
                    try {
                        Glide.with(applicationContext).load(imagen).placeholder(R.drawable.img_perfil).into(binding.imgPerfil)

                    } catch (e:Exception) {

                        Toast.makeText(this@EditarPerfil, "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    // Asignar codigo del telefono
                    try {
                        val codigo = codTelefono.replace("+", "").toInt() // +34 -> 34
                        binding.selectorCod.setCountryForPhoneCode(codigo)

                    } catch (e:Exception) {

                        //Toast.makeText(this@EditarPerfil, "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun subirImagenStorage() {

        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()

        // Guardar la imagen del perfil en la BD
        val rutaImagen = "imagenesPerfil/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile(imageUri!!)

            .addOnSuccessListener { taskSnapShot->
                val uriTask = taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagenCargada = uriTask.result.toString()
                
                if (uriTask.isSuccessful) {
                    
                    actualizarImagenBD(urlImagenCargada)
                }

            }

            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun actualizarImagenBD(urlImagenCargada: String) {

        // Actualizar la url de la imagen de perfil al usuario
        progressDialog.setMessage("Actualizando Imagen")
        progressDialog.show()

        val hashMap : HashMap<String, Any> = HashMap()

        if (imageUri != null) {
            hashMap["urlImagenPerfil"] = urlImagenCargada
        }

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(applicationContext, "La imagen de perfil se ha actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->

                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun select_imagen_de() {

        // Menu desplegable para hacer una foto o seleccionar una de la galeria
        val popupMenu = PopupMenu(this, binding.FABCambiarImg)

        popupMenu.menu.add(Menu.NONE,1,1,"Cámara")

        popupMenu.menu.add(Menu.NONE,2,2,"Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->

            val itemId = item.itemId

            if (itemId == 1) {
                // Camara - Si la version del SO es mayor o igual solo se necesita permiso de la camara
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    concederPermisoCam.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {

                    concederPermisoCam.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            } else if (itemId == 2) {
                // Galeria - Si la version del SO es mayor o igual solo se necesita permiso de la galeria
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    imagenGaleria()
                } else {

                    concederPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }

            return@setOnMenuItemClickListener true

        }

    }

    // Dar permiso para usar la camara
    private val concederPermisoCam = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {resultado->

        var concedidoTodos = true

        for (seConcede in resultado.values) {
            concedidoTodos = concedidoTodos && seConcede
        }

        if (concedidoTodos) {
            imagenCamara()

        } else {

            Toast.makeText(this, "El permiso de la cámara, almacenamiento, u ambas, ha sido denegado", Toast.LENGTH_SHORT).show()
        }

    }

    private fun imagenCamara() {

        // Guardar la foto de la camara
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultadoCamara_ARL.launch(intent)

    }

    // Guardar la foto en la BD
    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->

        if (resultado.resultCode == Activity.RESULT_OK) {

            subirImagenStorage()

            /*try {
                Glide.with(this).load(imageUri).placeholder(R.drawable.img_perfil).into(binding.imgPerfil)

            } catch (e:Exception) {

                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }*/
        }

    }

    private val concederPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()) {esConcedido->

        if (esConcedido) {
            imagenGaleria()

        } else {

            Toast.makeText(this, "El permiso de almacenamiento ha sido denegado", Toast.LENGTH_SHORT).show()

        }

    }

    private fun imagenGaleria() {

        val intent = Intent(Intent.ACTION_PICK)

        // Solo mostrar imagenes al abrir la galeria
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }


    // Guardar la imagen en la BD
    private val resultadoGaleria_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->

        if (resultado.resultCode == Activity.RESULT_OK) {

            val data = resultado.data

            imageUri = data!!.data

            subirImagenStorage()

            /*try {
                Glide.with(this).load(imageUri).placeholder(R.drawable.img_perfil).into(binding.imgPerfil)

            } catch (e:Exception) {

                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }*/

        }

    }

}