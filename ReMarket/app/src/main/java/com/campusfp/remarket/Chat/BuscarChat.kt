package com.campusfp.remarket.Chat

import android.widget.Filter
import com.campusfp.remarket.Adaptadores.AdaptadorChats
import com.campusfp.remarket.Modelo.ModeloChats
import java.util.Locale

class BuscarChat : Filter {

    private val adaptadorChats : AdaptadorChats
    private val filtroLista : ArrayList<ModeloChats>

    constructor(adaptadorChats: AdaptadorChats, filtroLista: ArrayList<ModeloChats>) : super() {
        this.adaptadorChats = adaptadorChats
        this.filtroLista = filtroLista
    }

    override fun performFiltering(filtro: CharSequence?): FilterResults {

        var filtro : CharSequence ?= filtro
        val resultados = FilterResults()

        if (!filtro.isNullOrEmpty()) {

            // Convertir la entrada a mayuscula y añadir los resultados que contienen esa entrada
            // al array list
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroModelos = ArrayList<ModeloChats>()

            for (i in filtroLista.indices) {
                if (filtroLista[i].nombres.uppercase().contains(filtro)) {

                    filtroModelos.add(filtroLista[i])
                }
            }

            resultados.count = filtroModelos.size
            resultados.values = filtroModelos
        } else {

            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    // Almacenar los resultados de que coiniden con la busqueda
    override fun publishResults(filtro: CharSequence, resultados: FilterResults) {

        adaptadorChats.chatsArrayList = resultados.values as ArrayList<ModeloChats>
        adaptadorChats.notifyDataSetChanged()
    }
}