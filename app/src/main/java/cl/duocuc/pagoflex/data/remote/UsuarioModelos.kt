package cl.duocuc.pagoflex.data.remote

import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class RecuperarRequest(
    val correo: String
)

data class RecuperarResponse(
    val correo: String,
    val codigo: String,
    val minutosVigencia: Int
)

data class RestablecerRequest(
    val correo: String,
    val codigo: String,
    val nueva: String
)

data class ActualizarPerfilRequest(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val comuna: String?
)

data class CambiarContrasenaRequest(
    val actual: String,
    val nueva: String
)

data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String?,
    val comuna: String?,
    val rol: String,
    val rolNombre: String,
    val empresaId: Long?,
    val cargo: String?,
    val avatarUrl: String?
)

fun UsuarioResponse.aUsuario() = Usuario(
    id = id,
    nombre = nombre,
    rut = rut,
    correo = correo,
    rol = Rol.valueOf(rol),
    telefono = telefono,
    comuna = comuna,
    empresaId = empresaId,
    cargo = cargo,
    avatarUrl = avatarUrl
)
