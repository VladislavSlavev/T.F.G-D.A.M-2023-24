package com.campusfp.remarket.DetalleAnuncio

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.campusfp.remarket.Adaptadores.AdaptadorImgSlider
import com.campusfp.remarket.Anuncios.CrearAnuncio
import com.campusfp.remarket.Chat.ChatActivity
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.DetalleVendedor.DetalleVendedor
import com.campusfp.remarket.MainActivity
import com.campusfp.remarket.Modelo.ModeloAnuncio
import com.campusfp.remarket.Modelo.ModeloImgSlider
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityDetalleAnuncioBinding
import com.campusfp.remarket.databinding.ItemImagenSliderBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""

    private var anuncioLatitud = 0.0
    private var anuncioLongitud = 0.0

    private var uidVendedor = ""
    private var telVendedor = ""

    private var favorito = false

    private lateinit var imagenSliderArrayList: ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Esconder los botones para activarlos solo cuando el vendedor se meta dentro
        binding.IbEditar.visibility = View.GONE
        binding.IbEliminar.visibility = View.GONE
        binding.BtnMapa.visibility = View.GONE
        binding.BtnLlamar.visibility = View.GONE
        binding.BtnSms.visibility = View.GONE
        binding.BtnChat.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        idAnuncio = intent.getStringExtra("idAnuncio").toString()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        comprobarFav()
        cargarInfoAnuncio()
        cargarImgAnuncio()

        // Botones
        binding.IbEditar.setOnClickListener {
            opcionesDialog()
        }

        binding.IbFav.setOnClickListener {
            if (favorito) {
                Constantes.eliminarAnuncioFav(this, idAnuncio)
            } else {
                Constantes.agregarAnuncioFav(this, idAnuncio)
            }
        }

        binding.IbEliminar.setOnClickListener {
            val mAlertDialog = MaterialAlertDialogBuilder(this)
            mAlertDialog.setTitle("Eliminar Anuncio").setMessage("¿Quieres eliminar este anuncio?")
                .setPositiveButton("Eliminar") {dialog, which->
                    eliminarAnuncio()
                }
                .setNegativeButton("Cancelar") {dialog, which->
                    dialog.dismiss()
                }.show()
        }

        binding.BtnMapa.setOnClickListener {
            Constantes.mapaIntent(this, anuncioLatitud, anuncioLongitud)
        }

        binding.BtnLlamar.setOnClickListener {

            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                val numTel = telVendedor

                if (numTel.isEmpty()) {

                    Toast.makeText(this, "El vendedor no tiene asignado un número de teléfono", Toast.LENGTH_SHORT).show()
                } else {
                    Constantes.llamarIntent(this, numTel)
                }
            } else {
                permisoLlamada.launch(android.Manifest.permission.CALL_PHONE)
            }
        }

        binding.BtnSms.setOnClickListener {

            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                val numTel = telVendedor

                if (numTel.isEmpty()) {

                    Toast.makeText(this, "El vendedor no tiene asignado un número de teléfono", Toast.LENGTH_SHORT).show()
                } else {
                    Constantes.smsIntent(this, numTel)
                }
            } else {
                permisoSMS.launch(android.Manifest.permission.SEND_SMS)
            }

        }

        binding.BtnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }

        binding.IvInfoVendedor.setOnClickListener {
            val intent = Intent(this, DetalleVendedor::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }

    }

    private fun opcionesDialog() {
        val popupMenu = PopupMenu(this, binding.IbEditar)

        // Desplegable de opciones
        popupMenu.menu.add(Menu.NONE,0,0, "Editar")
        popupMenu.menu.add(Menu.NONE,1,1, "Marcar como vendido")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            val itemId = item.itemId

            // Editar
            if (itemId == 0) {
                val intent = Intent(this, CrearAnuncio::class.java)
                intent.putExtra("Edicion", true)
                intent.putExtra("idAnuncio", idAnuncio)
                startActivity(intent)

            // Marcar como vendido
            } else if (itemId == 1) {

             dialogMarcarVendido()
            }

            return@setOnMenuItemClickListener true
        }
    }

    // Almacenar los datos del anuncio seleccionado desde la BD
    private fun cargarInfoAnuncio() {

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)

                        uidVendedor = modeloAnuncio!!.uid
                        val titulo = modeloAnuncio.titulo
                        val descripcion = modeloAnuncio.descripcion
                        val direccion = modeloAnuncio.direccion
                        val condicion = modeloAnuncio.condicion
                        val categoria = modeloAnuncio.categoria
                        val precio = modeloAnuncio.precio
                        val estado = modeloAnuncio.estado
                        anuncioLatitud = modeloAnuncio.latitud
                        anuncioLongitud = modeloAnuncio.longitud
                        val tiempo = modeloAnuncio.tiempo

                        val formatoFecha = Constantes.obtenerFecha(tiempo)

                        // Comprobar si el anuncio es del usuario para mostrar las opciones
                        if (uidVendedor == firebaseAuth.uid) {
                            binding.IbEditar.visibility = View.VISIBLE
                            binding.IbEliminar.visibility = View.VISIBLE

                            // Esconder los botones de contacto
                            binding.BtnMapa.visibility = View.GONE
                            binding.BtnChat.visibility = View.GONE
                            binding.BtnSms.visibility = View.GONE
                            binding.BtnLlamar.visibility = View.GONE

                            binding.TxtDescrVendedor.visibility = View.GONE
                            binding.perfilVendedor.visibility = View.GONE

                        } else {

                            // Esconder los botones de edicion para los compradores
                            binding.IbEditar.visibility = View.GONE
                            binding.IbEliminar.visibility = View.GONE

                            // Mostrar los botones de contancto
                            binding.BtnMapa.visibility = View.VISIBLE
                            binding.BtnChat.visibility = View.VISIBLE
                            binding.BtnSms.visibility = View.VISIBLE
                            binding.BtnLlamar.visibility = View.VISIBLE

                            binding.TxtDescrVendedor.visibility = View.VISIBLE
                            binding.perfilVendedor.visibility = View.VISIBLE
                        }

                        // Asignar info en las vistas
                        binding.TvTitulo.text = titulo
                        binding.TvDescr.text = descripcion
                        binding.TvDirec.text = direccion
                        binding.TvCondicion.text = condicion
                        binding.TvCat.text = categoria
                        binding.TvPrecio.text = precio
                        binding.TvEstado.text = estado
                        binding.TvFecha.text = formatoFecha

                        if (estado.equals("Disponible")) {

                            binding.TvEstado.setTextColor(Color.BLUE)
                        } else {
                            binding.TvEstado.setTextColor(Color.RED)
                        }

                        cargarInfoVendedor()

                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun marcarComoVendido() {

        // Marcar el anuncio como vendido en la BD
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "${Constantes.anuncio_vendido}"

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).updateChildren(hashMap)
            .addOnSuccessListener {

                Toast.makeText(this, "Anuncio marcado como vendido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "No se ha marcado como vendido por ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dialogMarcarVendido() {

        // Cuadro de confirmacion para marcar como vendido
        val Btn_si : MaterialButton
        val Btn_no : MaterialButton
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.cuadro_marcar_vendido)

        Btn_si = dialog.findViewById(R.id.Btn_si)
        Btn_no = dialog.findViewById(R.id.Btn_no)

        Btn_si.setOnClickListener {
            marcarComoVendido()
            dialog.dismiss()
        }

        Btn_no.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

    }

    // Almacenar informacion de la BD del vendedor para mostrarla
    private fun cargarInfoVendedor() {

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTel = "${snapshot.child("codigoTelefono").value}"
                    val nombre = "${snapshot.child("nombres").value}"
                    val imagenPerfil = "${snapshot.child("urlImagenPerfil").value}"
                    val tiempo_reg = snapshot.child("tiempo").value as Long

                    val for_fecha = Constantes.obtenerFecha(tiempo_reg)
                    telVendedor = "$codTel$telefono"

                    binding.TvNombres.text = nombre
                    binding.TvMiembro.text = for_fecha

                    try {

                        Glide.with(this@DetalleAnuncio)
                            .load(imagenPerfil).placeholder(R.drawable.img_perfil).into(binding.ImgPerfil)
                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    // Almacenar las imagenes del anuncio desde la BD
    private fun cargarImgAnuncio() {

        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    imagenSliderArrayList.clear()

                    for (ds in snapshot.children) {

                        try {
                            val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                            imagenSliderArrayList.add(modeloImgSlider!!)
                        } catch (e:Exception) {

                        }
                    }

                    val adaptadorImgSlider = AdaptadorImgSlider(this@DetalleAnuncio, imagenSliderArrayList)
                    binding.imagenSliderVP.adapter = adaptadorImgSlider
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // Si el id del anuncio se encuentra en la BD favoritos dentro de la del usuario
    // se cambia de un icono a otro y viceversa
    private fun comprobarFav() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}").child("Favoritos").child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    favorito = snapshot.exists()

                    if (favorito) {

                        binding.IbFav.setImageResource(R.drawable.ic_favorito)
                    } else {

                        binding.IbFav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun eliminarAnuncio(){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).removeValue()
            .addOnSuccessListener {

                startActivity(Intent(this@DetalleAnuncio, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(this, "Anuncio eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Marcar el telefono asignado si el vendedor lo tiene asignado o si la app tiene los permisos
    private val permisoLlamada =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {conceder->

            if (conceder) {

                val numTel = telVendedor

                if (numTel.isEmpty()) {

                    Toast.makeText(this, "El vendedor no tiene asignado un número de teléfono", Toast.LENGTH_SHORT).show()
                } else {
                    Constantes.llamarIntent(this, numTel)
                }
            }
            else {
                Toast.makeText(this, "La aplicación no tiene el permiso para hacer llamadas", Toast.LENGTH_SHORT).show()
            }
        }

    // Enviar sms al telefono asignado si el vendedor lo tiene asignado o si la app tiene los permisos
    private val permisoSMS =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { conceder ->

            if (conceder) {

                val numTel = telVendedor

                if (numTel.isEmpty()) {

                    Toast.makeText(
                        this@DetalleAnuncio,
                        "El vendedor no tiene asignado un número de teléfono",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Constantes.smsIntent(this, numTel)
                }
            } else {
                Toast.makeText(
                    this@DetalleAnuncio,
                    "La aplicación no tiene el permiso para hacer SMS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}