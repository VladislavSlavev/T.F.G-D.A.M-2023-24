package com.campusfp.remarket.Filtro

import android.annotation.SuppressLint
import android.widget.Filter
import com.campusfp.remarket.Adaptadores.AdaptadorAnuncio
import com.campusfp.remarket.Modelo.ModeloAnuncio
import java.util.Locale

class FiltrarAnuncio(

    private val adaptador : AdaptadorAnuncio,
    private val filtroLista : ArrayList<ModeloAnuncio>

) : Filter() {

    override fun performFiltering(filtro: CharSequence?): FilterResults {
        var filtro = filtro
        var resultados = FilterResults()

        // Convertir el texto a mayuscula
        if (!filtro.isNullOrEmpty()) {

            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroModelo = ArrayList<ModeloAnuncio>()

            // Aplicar filtro para ver si la consulta coincide con la marca ,titulo, etc...
            for (i in filtroLista.indices) {

                if (filtroLista[i].marca.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].categoria.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].condicion.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].titulo.uppercase(Locale.getDefault()).contains(filtro)) {

                    filtroModelo.add(filtroLista[i])
                }
            }
            // Preparar la lista filtrada y recuento de elementos
            resultados.count = filtroModelo.size
            resultados.values = filtroModelo

        } else {
            //Si la consulta esta vacia o es nula se prepara la original
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {

        adaptador.anuncioArrayList = resultados.values as ArrayList<ModeloAnuncio>
        adaptador.notifyDataSetChanged()
    }

}