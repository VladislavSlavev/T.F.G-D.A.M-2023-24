package com.campusfp.remarket.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.campusfp.remarket.Adaptadores.AdaptadorImagenSeleccionada
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.MainActivity
import com.campusfp.remarket.Modelo.ModeloImagenSeleccionada
import com.campusfp.remarket.R
import com.campusfp.remarket.SeleccionarUbicacion
import com.campusfp.remarket.databinding.ActivityCrearAnuncioBinding
import com.campusfp.remarket.databinding.ItemImagenesSeleccionadasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imagenUri : Uri?= null

    private lateinit var imagenSelecArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSel : AdaptadorImagenSeleccionada

    private var Edicion = false
    private var idAnuncioEditar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)


        // Verificar si se esta editando o creando el anuncio
        Edicion = intent.getBooleanExtra("Edicion", false)

        // Se identifica desde que actividad se viene para saber si mostrar editar o crear
        if (Edicion) {

            // Se llega desde detalle anuncio
            idAnuncioEditar = intent.getStringExtra("idAnuncio") ?: ""
            cargarDetalles()
            binding.BtnCrearAnuncio.text = "Actualizar Anuncio"

        } else {
            // Se llega desde Main activity
            binding.BtnCrearAnuncio.text = "Crear Anuncio"
        }

        imagenSelecArrayList = ArrayList()

        cargarImagenes()

        binding.agregarImg.setOnClickListener {
            mostrarOpciones()
        }

        binding.Localizacion.setOnClickListener {

            val intent = Intent(this, SeleccionarUbicacion::class.java)
            seleccionarUbicacion_ARL.launch(intent)
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
            //onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun cargarDetalles() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    //Obtener info del anuncio
                    val marca = "${snapshot.child("marca").value}"
                    val categoria = "${snapshot.child("categoria").value}"
                    val condicion = "${snapshot.child("condicion").value}"
                    val localizacion = "${snapshot.child("direccion").value}"
                    val precio = "${snapshot.child("precio").value}"
                    val titulo = "${snapshot.child("titulo").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    latitud = (snapshot.child("latitud").value) as Double
                    longitud = (snapshot.child("longitud").value) as Double

                    // Asignar info en las vistas
                    binding.EtMarca.setText(marca)
                    binding.Categoria.setText(categoria)
                    binding.Categoria.isEnabled = false
                    binding.Condicion.setText(condicion)
                    binding.Condicion.isEnabled = false
                    binding.Localizacion.setText(localizacion)
                    binding.EtPrecio.setText(precio)
                    binding.EtTitulo.setText(titulo)
                    binding.EtDescripcion.setText(descripcion)

                    val refImagenes = snapshot.child("Imagenes").ref
                    refImagenes.addListenerForSingleValueEvent(object : ValueEventListener{

                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val id = "${ds.child("id").value}"
                                val imagenUrl = "${ds.child("imagenUrl").value}"

                                val modeloImgSeleccionada = ModeloImagenSeleccionada(id, null, imagenUrl, true)
                                imagenSelecArrayList.add(modeloImgSeleccionada)
                            }

                            cargarImagenes()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var marca = ""
    private var categoria = ""
    private var condicion = ""
    private var direccion = ""
    private var precio = ""
    private var titulo = ""
    private var descripcion = ""
    private var latitud = 0.0
    private var longitud = 0.0

    private fun validarDatos() {

        // OBtener los valores introducidos
        marca = binding.EtMarca.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        condicion = binding.Condicion.text.toString().trim()
        direccion = binding.Localizacion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        // Validacion
        if (marca.isEmpty()) {

            binding.EtMarca.error = "Ingresa una marca"
            binding.EtMarca.requestFocus()

        } else if (categoria.isEmpty()) {

            binding.Categoria.error = "Ingresa una categoría"
            binding.Categoria.requestFocus()

        } else if (condicion.isEmpty()) {

            binding.Condicion.error = "Ingresa una condición"
            binding.Condicion.requestFocus()
        }
        else if (direccion.isEmpty()) {

            binding.Localizacion.error = "Ingresa una dirección"
            binding.Localizacion.requestFocus()
        }
        else if (precio.isEmpty()) {

            binding.EtPrecio.error = "Ingresa un precio"
            binding.EtPrecio.requestFocus()

        } else if (titulo.isEmpty()) {

            binding.EtTitulo.error = "Ingresa un título"
            binding.EtTitulo.requestFocus()

        } else if (descripcion.isEmpty()) {

            binding.EtDescripcion.error = "Ingresa una descripción"
            binding.EtDescripcion.requestFocus()

        } else {

            if (Edicion) {

                actualizarAnuncio()
            } else {

                if (imagenUri == null) {

                    Toast.makeText(this, "Añade al menos una imagen", Toast.LENGTH_SHORT).show()
                } else {

                    agregarAnuncio()
                }
            }
        }
    }

    private fun actualizarAnuncio() {

        progressDialog.setMessage("Actualizando Anuncio")
        progressDialog.show()

        // Hashmap con los datos
        val hashMap = HashMap<String, Any>()
        hashMap["marca"] = marca
        hashMap["categoria"] = categoria
        hashMap["condicion"] = condicion
        hashMap["direccion"] = direccion
        hashMap["precio"] = precio
        hashMap["titulo"] = titulo
        hashMap["descripcion"] = descripcion
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud


        // Actualizar el anuncio con los datos del hashmap
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                cargarImagenesStorage(idAnuncioEditar)
                val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Se ha actualizado el anuncio", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se ha podido actualizar el anuncio debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Guardado de la ubicacion seleccionada
    private val seleccionarUbicacion_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->
        if (resultado.resultCode == Activity.RESULT_OK) {

            val data = resultado.data
            if (data != null) {
                latitud = data.getDoubleExtra("latitud", 0.0)
                longitud = data.getDoubleExtra("longitud", 0.0)
                direccion = data.getStringExtra("direccion") ?: ""

                binding.Localizacion.setText(direccion)
            }
        } else {

            //Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarAnuncio() {
        progressDialog.setMessage("Subiendo Anuncio")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoDis()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        // Datos del anuncio
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["marca"] = marca
        hashMap["categoria"] = categoria
        hashMap["condicion"] = condicion
        hashMap["direccion"] = direccion
        hashMap["precio"] = precio
        hashMap["titulo"] = titulo
        hashMap["descripcion"] = descripcion
        hashMap["estado"] = Constantes.anuncio_disponible
        hashMap["tiempo"] = tiempo
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud

        ref.child(keyId!!).setValue(hashMap)
            .addOnSuccessListener {

                cargarImagenesStorage(keyId)
                progressDialog.dismiss()
                val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Se ha publicado el anuncio", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
            .addOnFailureListener {e->

                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun cargarImagenesStorage(keyId: String) {

        for (i in imagenSelecArrayList.indices) {

            val modeloImagenSel = imagenSelecArrayList[i]

            // Solo se suben las imagenes de la galeria o camara, y no las que ya hay en la BD
            if (!modeloImagenSel.deInternet) {

                val nombreImagen = modeloImagenSel.id
                val rutaNombreImagen = "Anuncios/$nombreImagen"

                val storageReference = FirebaseStorage.getInstance().getReference(rutaNombreImagen)
                storageReference.putFile(modeloImagenSel.imagenUri!!)
                    .addOnSuccessListener {taskSnapshot->

                        val uriTask = taskSnapshot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val urlImgCargada = uriTask.result

                        if (uriTask.isSuccessful) {

                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modeloImagenSel.id}"
                            hashMap["imagenUrl"] = "$urlImgCargada"

                            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                            ref.child(keyId).child("Imagenes")
                                .child(nombreImagen).updateChildren(hashMap)
                        }

                        if (Edicion) {

                            progressDialog.dismiss()

                        } else {

                            //progressDialog.dismiss()
                            //limpiarCampos()
                        }

                    }
                    .addOnFailureListener {e->

                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }


    private fun limpiarCampos() {
        imagenSelecArrayList.clear()
        adaptadorImagenSel.notifyDataSetChanged()
        binding.EtMarca.setText("")
        binding.Categoria.setText("")
        binding.Condicion.setText("")
        binding.Localizacion.setText("")
        binding.EtPrecio.setText("")
        binding.EtTitulo.setText("")
        binding.EtDescripcion.setText("")

    }

    // Desplegable para eligir imagen de la camara o galeria
    private fun mostrarOpciones() {

        val popupMenu = PopupMenu(this, binding.agregarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->

            val itemId = item.itemId

            // Camara - Si la version del SO es mayor o igual solo se necesita permiso de la camara
            if (itemId == 1) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {

                    solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            // Galeria - Si la version del SO es mayor o igual solo se necesita permiso de la galeria
            } else if (itemId == 2) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    imagenGaleria()
                } else {

                    solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }
            true
        }
    }

    // Mostrar la imagen si la app tiene los permisos
    private val solicitarPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()) {esConcedido->

        if (esConcedido) {

            imagenGaleria()

        } else {

            Toast.makeText(this, "El permiso de almacenamiento ha sido denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun imagenGaleria() {

        val intent = Intent(Intent.ACTION_PICK)

        // Solo mostrar archivos de imagen
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }


    private val resultadoGaleria_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->

        // Guardar la imagen seleccionada y sus datos en el array
        if (resultado.resultCode == Activity.RESULT_OK) {

            val data = resultado.data

            imagenUri = data!!.data

            val tiempo = "${Constantes.obtenerTiempoDis()}"
            val modeloImgSel = ModeloImagenSeleccionada(
                tiempo, imagenUri, null, false
            )
            imagenSelecArrayList.add(modeloImgSel)
            cargarImagenes()

        } else {

            //Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        }

    }

    // Dar permiso para usar la camara
    private val solicitarPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {resultado->

        var todosConcedidos = true
        for (esConcedido in resultado.values) {
            todosConcedidos = todosConcedidos && esConcedido
        }

        if (todosConcedidos) {

            imagenCamara()
        } else {

            Toast.makeText(this, "El permiso de la cámara, almacenamiento, u ambas, ha sido denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun imagenCamara() {

        // Guardar la foto tomada desde la camara
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)

    }

    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {resultado->

        // Guardar en el array la foto de la camara
        if (resultado.resultCode == Activity.RESULT_OK) {

            val tiempo = "${Constantes.obtenerTiempoDis()}"
            val modeloImgSel = ModeloImagenSeleccionada(
                tiempo, imagenUri, null, false
            )

            imagenSelecArrayList.add(modeloImgSel)
            cargarImagenes()

        } else {

            //Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList, idAnuncioEditar)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }
}