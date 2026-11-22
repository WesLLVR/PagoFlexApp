package cl.duocuc.pagoflex.model

data class Usuario(
    val id: Long,
    val nombre: String,
    val rut: String,
    val correo: String,
    val rol: Rol,
    val telefono: String? = null,
    val comuna: String? = null,
    val empresaId: Long? = null,
    val cargo: String? = null,
    val avatarUrl: String? = null
) {
    val primerNombre: String get() = nombre.substringBefore(" ")
}
