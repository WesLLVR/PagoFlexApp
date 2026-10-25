package cl.duocuc.pagoflex.model

data class Usuario(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val rut: String,
    val correo: String,
    val rol: Rol,
    val idEmpresa: Long? = null,
    val nombreEmpresa: String? = null,
    val cargo: String? = null
) {
    val primerNombre: String get() = nombre.substringBefore(" ")
}
