package com.campusfp.remarket.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.Modelo.ModeloCategoria
import com.campusfp.remarket.Modelo.ModeloChat
import com.campusfp.remarket.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class AdaptadorChat : RecyclerView.Adapter<AdaptadorChat.HolderChat> {

    private val context : Context
    private val chatArrayList : ArrayList<ModeloChat>
    private val firebaseAuth : FirebaseAuth

    // Constantes para identificar cada mensaje
    companion object {
        private const val MENSAJE_IZQ = 0
        private const val MENSAJE_DER = 1
    }

    constructor(context: Context, chatArrayList: ArrayList<ModeloChat>) {
        this.context = context
        this.chatArrayList = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    // Mostrar las burbujas de chat en un lado u otro
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {

        if (viewType == MENSAJE_DER) {

            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_der, parent, false)
            return HolderChat(view)
        } else {

            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_izq, parent, false)
            return HolderChat(view)
        }
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    // Si el uid es del emisor se muestra a la derecha y si es del receptor a la izquierda
    override fun getItemViewType(position: Int): Int {
        if (chatArrayList[position].emisorUid == firebaseAuth.uid) {
            return MENSAJE_DER
        } else {
            return MENSAJE_IZQ
        }
    }

    // Enlazar los datos del item con la vista
    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modeloChat = chatArrayList[position]

        val mensaje = modeloChat.mensaje
        val tipoMensaje = modeloChat.tipoMensaje
        val tiempo = modeloChat.tiempo

        val formato_fecha_hora = Constantes.obtenerFechaHora(tiempo)
        holder.Tv_tiempo_mensaje.text = formato_fecha_hora

        if (tipoMensaje == Constantes.MENSAJE_TIPO_TEXTO) {

            holder.Tv_mensaje.visibility = View.VISIBLE
            holder.Iv_mensaje.visibility = View.GONE

            holder.Tv_mensaje.text = mensaje

        } else {

            holder.Tv_mensaje.visibility = View.GONE
            holder.Iv_mensaje.visibility = View.VISIBLE

            try {

                Glide.with(context).load(mensaje).placeholder(R.drawable.imagen_chat).error(R.drawable.imagen_chat_falla).into(holder.Iv_mensaje)
            } catch (e:Exception) {

            }
        }
    }

    inner class HolderChat(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var Tv_mensaje : TextView = itemView.findViewById(R.id.Tv_mensaje)
        var Iv_mensaje : ShapeableImageView = itemView.findViewById(R.id.Iv_mensaje)
        var Tv_tiempo_mensaje : TextView = itemView.findViewById(R.id.Tv_tiempo_mensaje)
    }

}