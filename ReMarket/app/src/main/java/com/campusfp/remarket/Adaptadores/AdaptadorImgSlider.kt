package com.campusfp.remarket.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusfp.remarket.Modelo.ModeloImgSlider
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.FragmentMisAnunciosPublicadosBinding
import com.campusfp.remarket.databinding.ItemImagenSliderBinding
import com.google.android.material.imageview.ShapeableImageView

class AdaptadorImgSlider  : RecyclerView.Adapter<AdaptadorImgSlider.HolderImagenSlider> {

    private lateinit var binding: ItemImagenSliderBinding

    private var context : Context
    private var imagenArrayList : ArrayList<ModeloImgSlider>

    constructor(context: Context, imagenArrayList: ArrayList<ModeloImgSlider>) {
        this.context = context
        this.imagenArrayList = imagenArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imagenArrayList.size
    }

    // Cargar el arraylist de las imagenes y el contador
    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val modeloImagenSlider = imagenArrayList[position]

        val imagenUrl = modeloImagenSlider.imagenUrl
        val imagenContador = "${position+1} / ${imagenArrayList.size}"

        holder.imagenContadorTv.text = imagenContador

        // Cargar la imagen
        try {
            Glide.with(context).load(imagenUrl).placeholder(R.drawable.ic_imagen).into(holder.imagenIv)
        } catch (e:Exception) {

        }

        holder.itemView.setOnClickListener {

        }
    }

    inner class HolderImagenSlider(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var imagenIv : ShapeableImageView = binding.ImagenIv
        var imagenContadorTv : TextView = binding.imagenContadorTv
    }

}