package com.campusfp.remarket.Adaptadores

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.campusfp.remarket.Modelo.ModeloCategoria
import com.campusfp.remarket.RvListenerCategoria
import com.campusfp.remarket.databinding.ActivitySeleccionarUbicacionBinding
import com.campusfp.remarket.databinding.ItemCategoriaInicioBinding
import java.util.Random

class AdaptadorCategoria (
    private val context : Context,
    private val categoriaArrayList : ArrayList<ModeloCategoria>,
    private val rvListenerCategoria: RvListenerCategoria
): RecyclerView.Adapter<AdaptadorCategoria.HolderCategoria>(){

    private lateinit var binding: ItemCategoriaInicioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoria {
        binding = ItemCategoriaInicioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategoria(binding.root)
    }

    override fun getItemCount(): Int {
        return categoriaArrayList.size
    }


    // Enlazar los datos del item con la vista
    override fun onBindViewHolder(holder: HolderCategoria, position: Int) {
        val modeloCategoria = categoriaArrayList[position]

        // Obtener datos de la categoría
        val icono = modeloCategoria.icon
        val categoria = modeloCategoria.categoria

        // Crear color aleatorio para cada icono
        val random = Random()
        val color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

        // Establecer datos en las vistas
        holder.categoriaIconoIv.setImageResource(icono)
        holder.categoriaTv.text = categoria
        holder.categoriaIconoIv.setBackgroundColor(color)

        holder.itemView.setOnClickListener {

            rvListenerCategoria.onCategoriaClick(modeloCategoria)
        }
    }


    inner class  HolderCategoria(itemView : View) : ViewHolder(itemView){

        var categoriaIconoIv = binding.catergoriaIconoIv
        var categoriaTv = binding.TvCategoria
    }

}