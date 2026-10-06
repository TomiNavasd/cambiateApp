sealed class Ruta(val route: String) {

    data object Login : Ruta("login")

    data object Register : Ruta("register")

    data object Lista : Ruta("lista")

    data object Detalle : Ruta("detalle/{id}") {
        const val ARG_ID = "id"
        fun crear(id: String) = "detalle/$id"
    }

    /** El id es opcional: sin id es una prenda nueva; con id, se edita esa prenda. */
    data object AltaEdicion : Ruta("alta_edicion?id={id}") {
        const val ARG_ID = "id"
        fun crear(id: String? = null) = if (id == null) "alta_edicion" else "alta_edicion?id=$id"
    }
}