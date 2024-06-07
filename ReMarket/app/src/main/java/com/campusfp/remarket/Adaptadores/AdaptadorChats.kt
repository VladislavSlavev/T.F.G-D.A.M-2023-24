package com.campusfp.remarket.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusfp.remarket.Chat.BuscarChat
import com.campusfp.remarket.Chat.ChatActivity
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.Modelo.ModeloChats
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityChatBinding
import com.campusfp.remarket.databinding.ItemChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdaptadorChats : RecyclerView.Adapter<AdaptadorChats.HolderChats>, Filterable{

    private var context : Context
    var chatsArrayList : ArrayList<ModeloChats>
    private lateinit var binding: ItemChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var mUid = ""
    private var filtroLista : ArrayList<ModeloChats>
    private var filtro : BuscarChat ?= null

    constructor(context: Context, chatsArrayList: ArrayList<ModeloChats>) {
        this.context = context
        this.chatsArrayList = chatsArrayList
        this.filtroLista = chatsArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        mUid = firebaseAuth.uid!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {

        binding = ItemChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {

        return chatsArrayList.size
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {

        val modeloChats = chatsArrayList[position]

        cargarUlitmoMensaje(modeloChats, holder)

        holder.itemView.setOnClickListener {

            val uidRecibido = modeloChats.uidRecibido
            if (uidRecibido != null) {

                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uidVendedor", uidRecibido)
                context.startActivity(intent)
            }
        }
    }

    // Seleccionar el ultimop mensaje de la BD chats y guardar sus datos
    private fun cargarUlitmoMensaje(modeloChats: ModeloChats, holder: AdaptadorChats.HolderChats) {

        val chatKey = modeloChats.keyChat

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){

                        val uidEmisor = "${ds.child("emisorUid").value}"
                        val idMensaje = "${ds.child("idMensaje").value}"
                        val mensaje = "${ds.child("mensaje").value}"
                        val uidReceptor = "${ds.child("receptorUid").value}"
                        val tiempo = ds.child("tiempo").value as Long
                        val tipoMensaje = "${ds.child("tipoMensaje").value}"

                        val formato_fecha_hora = Constantes.obtenerFechaHora(tiempo)

                        modeloChats.uidEmisor = uidEmisor
                        modeloChats.idMensaje = idMensaje
                        modeloChats.mensaje = mensaje
                        modeloChats.receptorUid = uidReceptor
                        modeloChats.tipoMensaje = tipoMensaje

                        holder.Tv_fecha.text = "$formato_fecha_hora"

                        if (tipoMensaje == Constantes.MENSAJE_TIPO_TEXTO) {

                            holder.Tv_ult_mensaje.text = mensaje
                        } else {

                            holder.Tv_ult_mensaje.text = "Se ha enviado una imagen"
                        }

                        cargarInfoUsuarioRecibir(modeloChats, holder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // Cargar la informacion del usuario receptor
    private fun cargarInfoUsuarioRecibir(modeloChats: ModeloChats, holder: AdaptadorChats.HolderChats) {

        val emisorUid = modeloChats.uidEmisor
        val receptorUid = modeloChats.receptorUid
        var uidRecibir = ""

        if(emisorUid == mUid) {

            uidRecibir = receptorUid
        } else {

            uidRecibir = emisorUid
        }

        modeloChats.uidRecibido = uidRecibir

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidRecibir)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"

                    // Asignar info
                    modeloChats.nombres = nombres
                    modeloChats.urlImgPerfil = imagen

                    holder.Tv_nombres.text = nombres

                    // Cargar imagen del perfil
                    try {
                        Glide.with(context).load(imagen).placeholder(R.drawable.img_perfil).into(holder.Iv_perfil)
                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    inner class HolderChats(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var Iv_perfil = binding.IvPerfil
        var Tv_nombres = binding.TvNombres
        var Tv_ult_mensaje = binding.TvUltMensaje
        var Tv_fecha = binding.TvFecha
    }

    override fun getFilter(): Filter {
        if(filtro == null) {
            filtro = BuscarChat(this, filtroLista)
        }
        return filtro!!
    }
}