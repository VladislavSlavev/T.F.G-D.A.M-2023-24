package com.campusfp.remarket.Modelo

import android.net.Uri

// Prpiedades de la imagen
class ModeloImagenSeleccionada {

    var id = ""
    var imagenUri : Uri?= null
    var imagenUrl : String?= null
    var deInternet = false

    constructor()
    constructor(id: String, imagenUri: Uri?, imagenUrl: String?, deInternet: Boolean) {
        this.id = id
        this.imagenUri = imagenUri
        this.imagenUrl = imagenUrl
        this.deInternet = deInternet
    }


}