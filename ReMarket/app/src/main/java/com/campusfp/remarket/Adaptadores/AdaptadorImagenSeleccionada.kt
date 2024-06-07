package com.campusfp.remarket.Adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusfp.remarket.Modelo.ModeloImagenSeleccionada
import com.campusfp.remarket.R
import com.campusfp.remarket.databinding.ActivityEditarPerfilBinding
import com.campusfp.remarket.databinding.ItemImagenesSeleccionadasBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AdaptadorImagenSeleccionada(
    private val context: Context,
    private val imagenesSelecarrayList: ArrayList<ModeloImagenSeleccionada>,
    private val idAnuncio: String
): RecyclerView.Adapter<AdaptadorImagenSeleccionada.HolderImagenSeleccionada>() {

    private lateinit var binding: ItemImagenesSeleccionadasBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {

        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSeleccionada(binding.root)

    }

    override fun getItemCount(): Int {
        return imagenesSelecarrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {

        val modelo = imagenesSelecarrayList[position]

        if (modelo.deInternet) {

            // Lectura de las imgs de Firebase
            try {

                val imagenUrl = modelo.imagenUrl
                Glide.with(context).load(imagenUrl).placeholder(R.drawable.item_imagen).into(binding.itemImagen)
            } catch (e:Exception) {

            }
        } else {

            // Lectura de las imgs seleccionadas desde la camara o galeria
            try {

                val imagenUri = modelo.imagenUri
                Glide.with(context).load(imagenUri).placeholder(R.drawable.item_imagen).into(holder.item_imagen)

            } catch (e:Exception){

            }
        }

        holder.btn_cerrar.setOnClickListener {

            if (modelo.deInternet) {

                // Vistas del diseño
                val Btn_si : MaterialButton
                val Btn_no : MaterialButton
                val dialog = Dialog(context)

                dialog.setContentView(R.layout.cuadro_eliminar_img)

                Btn_si = dialog.findViewById(R.id.Btn_si)
                Btn_no = dialog.findViewById(R.id.Btn_no)

                Btn_si.setOnClickListener {

                    eliminarImgFirebase(modelo, holder, position)
                    dialog.dismiss()
                }

                Btn_no.setOnClickListener {

                    dialog.dismiss()
                }

                dialog.show()
                dialog.setCanceledOnTouchOutside(false)

            } else {

                imagenesSelecarrayList.remove(modelo)
                notifyDataSetChanged()
            }
        }

    }

    private fun eliminarImgFirebase(modelo: ModeloImagenSeleccionada, holder: AdaptadorImagenSeleccionada.HolderImagenSeleccionada, position: Int) {

        val idImagen = modelo.id

        // La imagen se eliminara en la BD
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").child(idImagen)
            .removeValue()
            .addOnSuccessListener {

                try {

                    imagenesSelecarrayList.remove(modelo)
                    eliminarImgStorage(modelo)
                    notifyItemRemoved(position)
                } catch (e:Exception) {

                }
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Eliminar la imagen del almacenamiento
    private fun eliminarImgStorage(modelo: ModeloImagenSeleccionada) {

        val  rutaImg = "Anuncios/"+modelo.id
        val ref = FirebaseStorage.getInstance().getReference(rutaImg)
        ref.delete()
            .addOnSuccessListener {

                Toast.makeText(context, "Se ha eliminado la imagen", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    inner class HolderImagenSeleccionada(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var item_imagen = binding.itemImagen
        var btn_cerrar = binding.cerrarItem

    }

}