package com.campusfp.remarket.Adaptadores

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusfp.remarket.Constantes
import com.campusfp.remarket.DetalleAnuncio.DetalleAnuncio
import com.campusfp.remarket.Filtro.FiltrarAnuncio
import com.campusfp.remarket.Modelo.ModeloAnuncio
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ItemAnuncioBinding
import com.campusfp.remarket.databinding.ItemAnuncioNuevaVersionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdaptadorAnuncio : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>, Filterable{

    private lateinit var binding : ItemAnuncioNuevaVersionBinding

    private var context : Context
    var anuncioArrayList : ArrayList<ModeloAnuncio>
    private var firebaseAuth : FirebaseAuth
    private var filtroLista : ArrayList<ModeloAnuncio>
    private var filtro : FiltrarAnuncio ?= null

    constructor(context: Context, anuncioArrayList: ArrayList<ModeloAnuncio>) {

        this.context = context
        this.anuncioArrayList = anuncioArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        this.filtroLista = anuncioArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {

        binding = ItemAnuncioNuevaVersionBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding.root)
    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    // Enlazar los datos del item con la vista
    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {

        val modeloAnuncio = anuncioArrayList[position]

        val titulo = modeloAnuncio.titulo
        val descripcion = modeloAnuncio.descripcion
        val direccion = modeloAnuncio.direccion
        val condicion = modeloAnuncio.condicion
        val precio = modeloAnuncio.precio
        val tiempo = modeloAnuncio.tiempo

        val formatoFecha = Constantes.obtenerFecha(tiempo)

        cargarPrimeraImg(modeloAnuncio, holder)

        comprobarFavorito(modeloAnuncio, holder)

        holder.Tv_titulo.text = titulo
        holder.Tv_descripcion.text = descripcion
        holder.Tv_direccion.text = direccion
        holder.Tv_condicion.text = condicion
        holder.Tv_precio.text = precio
        holder.Tv_fecha.text = formatoFecha

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleAnuncio::class.java)
            intent.putExtra("idAnuncio", modeloAnuncio.id)
            context.startActivity(intent)
        }

        // Cambiar color de texto dependiendo del estado
        if (condicion.equals("Nuevo")) {

            holder.Tv_condicion.setTextColor(Color.parseColor("#48C9B0"))
        } else if ((condicion.equals("Usado"))) {

            holder.Tv_condicion.setTextColor(Color.parseColor("#5DADE2"))
        } else if ((condicion.equals("Renovado"))) {

            holder.Tv_condicion.setTextColor(Color.parseColor("#A569BD"))
        }

        holder.Ib_fav.setOnClickListener {

            val favorito = modeloAnuncio.favorito

            //Si el anuncio esta en favorito se elimina, si no se añade
            if(favorito) {

                Constantes.eliminarAnuncioFav(context, modeloAnuncio.id)
            } else {

                Constantes.agregarAnuncioFav(context, modeloAnuncio.id)
            }
        }
    }

    // Si el anuncio esta en favoritos se representa el corazon rojo, si no, el vacio
    private fun comprobarFavorito(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(modeloAnuncio.id)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    val favorito = snapshot.exists()
                    modeloAnuncio.favorito = favorito

                    if (favorito) {

                        holder.Ib_fav.setImageResource(R.drawable.ic_favorito)
                    } else {

                        holder.Ib_fav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun cargarPrimeraImg(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {

        // Buscar y guardar la primera imagen de cada anuncio
        val idAnuncio = modeloAnuncio.id

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(context).load(imagenUrl).placeholder(R.drawable.ic_imagen).into(holder.imagenIv)
                        } catch (e:Exception) {

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // ViewHolder para el recycler view
    inner class HolderAnuncio(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var imagenIv = binding.imagenIv
        var Tv_titulo = binding.TvTitulo
        var Tv_descripcion = binding.TvDescripcion
        var Tv_direccion = binding.TvDireccion
        var Tv_condicion = binding.TvCondicion
        var Tv_precio = binding.TvPrecio
        var Tv_fecha = binding.TvFecha
        var Ib_fav = binding.IbFav

    }

    // Obtener el filtro de la lista
    override fun getFilter(): Filter {

        if (filtro == null) {

            filtro = FiltrarAnuncio(this, filtroLista)
        }
        return filtro as FiltrarAnuncio
    }
}