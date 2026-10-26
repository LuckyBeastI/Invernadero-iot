package cl.invernadero.app

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

/**
 * Datos del usuario que se envían entre pantallas (igual que en el ejemplo de clase).
 * La contraseña NO viaja en este objeto: solo se guarda su hash en Firebase.
 */
data class Usuario(
    val rut: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val cargo: String,
    val permiso: String
) : Serializable {

    val nombreCompleto: String get() = "$nombre $apellido"
    val esOperador: Boolean get() = permiso == PERMISO_OPERADOR

    companion object {
        const val PERMISO_OPERADOR = "operador"
        const val PERMISO_MONITOR = "monitor"

        fun desde(s: DataSnapshot) = Usuario(
            rut = s.child("rut").getValue(String::class.java) ?: "",
            nombre = s.child("nombre").getValue(String::class.java) ?: "",
            apellido = s.child("apellido").getValue(String::class.java) ?: "",
            correo = s.child("correo").getValue(String::class.java) ?: "",
            cargo = s.child("cargo").getValue(String::class.java) ?: "",
            permiso = s.child("permiso").getValue(String::class.java) ?: PERMISO_MONITOR
        )
    }
} // Cierra la clase "Usuario".
