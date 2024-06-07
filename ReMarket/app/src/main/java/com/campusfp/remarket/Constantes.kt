package com.campusfp.remarket

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.campusfp.remarket.Filtro.FiltrarAnuncio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

object Constantes {

    // Disponibilidad de anuncios
    const val anuncio_disponible = "Disponible"
    const val anuncio_vendido = "Vendido"

    // Tipos de mensajes
    const val MENSAJE_TIPO_TEXTO = "TEXTO"
    const val MENSAJE_TIPO_IMAGEN = "IMAGEN"

    // Notificaciones
    const val NOTIFICACION_NUEVO_MENSAJE = "NOTIFICACION DE NUEVO MENSAJE"
    const val FCM_SERVER_KEY = "AAAA8SLgAXM:APA91bH-gXjvrPyWxicM-uSHXCghJad4EHDXLO986IPrXT4Zia8iZXRs85wGnjntBQf7G64txp_D2TekOa8NksAYsFkZde75CM6ixY0hjkLw83Fcsq9MCS4n_SNSA8EKutfmWngg4QhZ"

    // Array categorias
    val categorias = arrayOf(
        "Todos",
        "Móviles",
        "Ordenadores",
        "Electrónica y electrodomésticos",
        "Vehículos",
        "Consolas y videojuegos",
        "Hogar y muebles",
        "Belleza y cuidado personal",
        "Libros",
        "Deportes",
        "Juguetes",
        "Mascotas"
    )

    // Array iconos de categorias
    val categoriasIcono = arrayOf(

        R.drawable.ic_cat_todos,
        R.drawable.ic_cat_moviles,
        R.drawable.ic_cat_ordenadores,
        R.drawable.ic_cat_electrodomesticos,
        R.drawable.ic_cat_vehiculos,
        R.drawable.ic_cat_juegos,
        R.drawable.ic_cat_muebles,
        R.drawable.ic_cat_belleza,
        R.drawable.ic_cat_libros,
        R.drawable.ic_cat_deportes,
        R.drawable.ic_cat_juguetes,
        R.drawable.ic_cat_mascotas

    )

    // Array condiciones
    val condiciones = arrayOf(
        "Nuevo",
        "Usado",
        "Renovado"
    )

    // Tiempo del dispositivo
    fun obtenerTiempoDis() : Long{
        return System.currentTimeMillis()
    }

    // Formato de string a long que devuelve la fecha
    fun obtenerFecha(tiempo : Long) : String {
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return android.text.format.DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

    // Formato de string a long que devuelve la fecha y hora
    fun obtenerFechaHora(tiempo : Long) : String {
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return android.text.format.DateFormat.format("dd/MM/yyyy hh:mm:a", calendario).toString()
    }

    fun agregarAnuncioFav(context: Context, idAnuncio: String) {

        // Añadir el id del anuncio a favoritos en la BD usuarios
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = Constantes.obtenerTiempoDis()

        val hashMap = HashMap<String, Any>()
        hashMap["idAnuncio"] = idAnuncio
        hashMap["tiempo"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .setValue(hashMap)
            .addOnSuccessListener {

                //Toast.makeText(context, "Anuncio agregado a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun eliminarAnuncioFav(context: Context, idAnuncio: String) {

        // Eliminar el id del anuncio de favoritos en la BD usuarios
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {

                //Toast.makeText(context, "Anuncio eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Intent para usar la api de maps
    fun mapaIntent (context: Context, latitud : Double, longitud : Double) {

        val googleMapIntentUri = Uri.parse("http://maps.google.com/maps?daddr=$latitud,$longitud")

        val mapIntent = Intent(Intent.ACTION_VIEW, googleMapIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            // Maps está instalado
            context.startActivity(mapIntent)
        } else {
            // Maps no esta instalado
            Toast.makeText(context, "No tienes Google Maps instalado", Toast.LENGTH_SHORT).show()
        }

    }

    // Intent para realizar llamadas
    fun llamarIntent (context: Context, tlf : String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.setData(Uri.parse("tel:$tlf"))
        context.startActivity(intent)
    }

    // Intent para enviar sms
    fun smsIntent (context: Context, tlf : String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("smsto:$tlf"))
        intent.putExtra("sms_body", "")
        context.startActivity(intent)

    }

    // Crear la ruta de cada chat usando los ids
    fun rutaChat(receptorUid : String, emisorUid : String) : String {

        val arrayUid = arrayOf(receptorUid, emisorUid)
        Arrays.sort(arrayUid)

        // Concatenar los uid del receptor y emisor
        return "${arrayUid[0]}_${arrayUid[1]}"
    }
}