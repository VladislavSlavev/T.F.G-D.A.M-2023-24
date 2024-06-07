package com.campusfp.remarket.Modelo

// Propiedades de la lista de chats
class ModeloChats {

    var urlImgPerfil : String = ""
    var nombres : String = ""
    var keyChat : String = ""
    var uidRecibido : String = ""
    var idMensaje : String = ""
    var tipoMensaje : String = ""
    var mensaje : String = ""
    var uidEmisor : String = ""
    var receptorUid : String = ""
    var tiempo : Long = 0

    constructor()
    constructor(
        urlImgPerfil: String,
        nombres: String,
        uidRecibido: String,
        idMensaje: String,
        tipoMensaje: String,
        mensaje: String,
        uidEmisor: String,
        receptorUid: String,
        tiempo: Long
    ) {
        this.urlImgPerfil = urlImgPerfil
        this.nombres = nombres
        this.uidRecibido = uidRecibido
        this.idMensaje = idMensaje
        this.tipoMensaje = tipoMensaje
        this.mensaje = mensaje
        this.uidEmisor = uidEmisor
        this.receptorUid = receptorUid
        this.tiempo = tiempo
    }


}