package com.campusfp.remarket.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.campusfp.remarket.Adaptadores.AdaptadorChat
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.Modelo.ModeloChat
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityChatBinding
import com.campusfp.remarket.databinding.ActivityDetalleAnuncioBinding
import com.google.android.gms.common.api.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // UID del receptor
    private var uidVendedor = ""

    // UID del emisor
    private var mUid = ""

    private var mNombre = ""
    private var recibirToken = ""

    private var rutaChat = ""
    private var imagenUri : Uri ?= null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        uidVendedor = intent.getStringExtra("uidVendedor")!!
        mUid = firebaseAuth.uid!!

        rutaChat = Constantes.rutaChat(uidVendedor, mUid)

        cargarMiInfo()
        cargarInfoVendedor()
        cargarMensajes()

        // Botones
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.adjuntarFAB.setOnClickListener {
            seleccionarImgDialog()
        }

        binding.enviarFAB.setOnClickListener {
            validarInfo()
        }
    }

    private fun cargarMiInfo() {

        // Obtener el nombre
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")

            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    mNombre = "${snapshot.child("nombres").value}"
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun cargarMensajes() {

        // Almacenar los mensajes del chat en un array list
        val mensajeArrayList = ArrayList<ModeloChat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(rutaChat)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajeArrayList.clear()
                    for (ds : DataSnapshot in snapshot.children) {
                        val modeloChat = ds.getValue(ModeloChat::class.java)

                        try {

                            val modeloChat = ds.getValue(ModeloChat::class.java)
                            mensajeArrayList.add(modeloChat!!)
                        } catch (e:Exception) {

                        }
                    }

                    // Mostrar el array list de los mensajes
                    val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajeArrayList)
                    binding.chatsRv.adapter = adaptadorChat

                    binding.chatsRv.setHasFixedSize(true)
                    var linearLayoutManager = LinearLayoutManager(this@ChatActivity)
                    linearLayoutManager.stackFromEnd = true
                    binding.chatsRv.layoutManager = linearLayoutManager
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun validarInfo() {

        val mensaje = binding.EtMensajeChat.text.toString().trim()
        val tiempo = Constantes.obtenerTiempoDis()

        if (mensaje.isNotEmpty()) {

            enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje, tiempo)
        }
    }

    private fun cargarInfoVendedor(){

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    // Asignar nombre e imagen
                    try {
                        val nombres = "${snapshot.child("nombres").value}"
                        val imagen = "${snapshot.child("urlImagenPerfil").value}"
                        recibirToken = "${snapshot.child("fcmToken").value}"

                        binding.TxtNombreVendedorChat.text = nombres

                        try {
                            Glide.with(this@ChatActivity).load(imagen).placeholder(R.drawable.img_perfil).into(binding.toolbarIV)
                        } catch (e:Exception) {

                        }

                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun seleccionarImgDialog() {

        // Desplegable para eligir imagen de la camara o galeria
        val popupMenu = PopupMenu(this, binding.adjuntarFAB)
        popupMenu.menu.add(Menu.NONE,1,1,"Cámara")
        popupMenu.menu.add(Menu.NONE,2,2,"Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem->

            val itemId = menuItem.itemId
            if (itemId == 1) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {

                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            } else if (itemId == 2) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    imagenGaleria()
                } else {

                    concederPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            true
        }
    }

    private fun imagenGaleria() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->

            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data
                subirImgStorage()
            } else {

            }
    }

    private val concederPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()){esConcedido->

        if (esConcedido) {
            imagenGaleria()
        } else {
            Toast.makeText(this, "El permiso de almacenamiento ha sido denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirCamara() {

        // Guardar la foto tomada desde la camara
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_img")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_img")

        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    // Guardar la foto de la camara en la BD
    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->

        if (resultado.resultCode == Activity.RESULT_OK) {
            subirImgStorage()
        }
    }

    // Abrir la camara si la app tiene el permiso
    private val concederPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultado->

        var concedidoTodos = true

        for (seCondede in resultado.values) {

            concedidoTodos = concedidoTodos && seCondede
        }

        if (concedidoTodos) {

            abrirCamara()
        } else {
            Toast.makeText(this, "El permiso de la camara/almacenamiento han sido denegados", Toast.LENGTH_SHORT).show()
        }

    }

    private fun subirImgStorage() {
        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()

        // Guardar la imagen con el identificador del tiempo en la BD
        val tiempo = Constantes.obtenerTiempoDis()
        val nombreRutaImg = "ImagenesChat/$tiempo"

        val storageRef = FirebaseStorage.getInstance().getReference(nombreRutaImg)
        storageRef.putFile(imagenUri!!)

            .addOnSuccessListener {taskSnapshot->

                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                val urlImagen = uriTask.result.toString()

                if (uriTask.isSuccessful) {
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN, urlImagen, tiempo)
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "No se ha podido subir la imagen ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {

        //progressDialog.setMessage("Envíando mensaje")
        //progressDialog.show()

        // Almacenar todos los datos del mensaje en un hashmap
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()

        hashMap["idMensaje"] = "$keyId"
        hashMap["tipoMensaje"] = "$tipoMensaje"
        hashMap["mensaje"] = "$mensaje"
        hashMap["emisorUid"] = "$mUid"
        hashMap["receptorUid"] = "$uidVendedor"
        hashMap["tiempo"] = tiempo

        // Guardar el hashmap en la BD
        refChat.child(rutaChat).child(keyId).setValue(hashMap)

            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMensajeChat.setText("")

                if(tipoMensaje == Constantes.MENSAJE_TIPO_TEXTO) {
                    prepararNotif(mensaje)
                } else {
                    prepararNotif("Se ha envíado una imagen")
                }
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se ha podido envíar el mensaje ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun prepararNotif(mensaje : String) {

        // Preparar los atributos de la notificacion
        val notificationJo = JSONObject()
        val notificationDataJo = JSONObject()
        val notificationNotificionJo = JSONObject()

        try {
            notificationDataJo.put("notificationType", "${Constantes.NOTIFICACION_NUEVO_MENSAJE}")
            notificationDataJo.put("senderUid", "${firebaseAuth.uid}")
            notificationNotificionJo.put("title", "$mNombre")
            notificationNotificionJo.put("body", "$mensaje")
            notificationNotificionJo.put("sound", "default")
            notificationJo.put("to", "$recibirToken")
            notificationJo.put("notification", notificationNotificionJo)
            notificationJo.put("data", notificationDataJo)

        }catch (e: Exception){

        }

        enviarNotif(notificationJo)
    }

    private fun enviarNotif(notificationJo: JSONObject) {

        // Enviar los datos del objeto JSON con los datos a la api fcm
        val jsonObjectRequest : JsonObjectRequest = object  : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            notificationJo,
            com.android.volley.Response.Listener {
                // Notificacion enviada
            },
            com.android.volley.Response.ErrorListener { e->
                // Notificacion no enviada
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {

                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=${Constantes.FCM_SERVER_KEY}"
                return headers
            }
        }

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

}