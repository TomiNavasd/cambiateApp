sealed class Ruta(val route: String) {

    data object Login : Ruta("login")

    data object Register : Ruta("register")

    data object Lista : Ruta("lista")

    data object Detalle : Ruta("detalle/{id}") {
        const val ARG_ID = "id"
        fun crear(id: String) = "detalle/$id"
    }


    data object AltaEdicion : Ruta("alta_edicion?id={id}") {
        const val ARG_ID = "id"
        fun crear(id: String? = null) = if (id == null) "alta_edicion" else "alta_edicion?id=$id"
    }
    data object Outfits : Ruta("outfits")
    data object Notificaciones : Ruta("notificaciones")
    data object Mapa : Ruta("mapa")
    data object Ajustes : Ruta("ajustes")

}