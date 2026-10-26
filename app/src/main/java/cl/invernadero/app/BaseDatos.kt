package cl.invernadero.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

/**
 * Acceso centralizado a Firebase Realtime Database.
 *
 *  usuarios/{rut}           -> rut, nombre, apellido, correo, cargo, permiso, hash, sal, creado
 *  invernadero/estado       -> temperatura, ventanaAbierta, actualizado
 *  invernadero/acciones/{id}-> fecha, rut, tipo, accion, temperatura
 */
object BaseDatos {

    /** Sobre esta temperatura la ventana se abre automáticamente. */
    const val UMBRAL_TEMPERATURA = 28.0

    private val db get() = FirebaseDatabase.getInstance()
    val usuarios get() = db.getReference("usuarios")
    val estado get() = db.getReference("invernadero/estado")
    val acciones get() = db.getReference("invernadero/acciones")

    /**
     * Abre una sesión anónima de Firebase antes de usar la base de datos.
     * Así las reglas pueden exigir "auth != null" y la base no queda abierta a cualquiera
     * desde Internet. El usuario no ve nada de esto: el login sigue siendo con RUT y contraseña.
     */
    fun conectar(alConectar: () -> Unit, alFallar: (Exception) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            alConectar()
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { alConectar() }
            .addOnFailureListener { alFallar(it) }
    } // Cierra la función "conectar".

    /** Guarda una acción en el historial (solo se puede agregar, nunca editar ni borrar). */
    fun registrarAccion(usu: Usuario, tipo: String, descripcion: String, temperatura: Double) {
        acciones.push().setValue(
            mapOf(
                "fecha" to ServerValue.TIMESTAMP,
                "rut" to usu.rut,
                "tipo" to tipo,
                "accion" to descripcion,
                "temperatura" to temperatura
            )
        )
    } // Cierra la función "registrarAccion".

    fun cerrarSesion() = FirebaseAuth.getInstance().signOut()

} // Cierra el objeto "BaseDatos".
