package cl.invernadero.app

import com.google.firebase.database.DataSnapshot

/** Un registro del historial: quién hizo qué, cuándo y con qué temperatura. */
data class Accion(
    val fecha: Long,
    val rut: String,
    val tipo: String,
    val accion: String,
    val temperatura: Double
) {
    companion object {
        const val APERTURA = "apertura"   // Apertura manual de la ventana
        const val CIERRE = "cierre"       // Cierre manual de la ventana
        const val ALERTA = "alerta"       // Temperatura alta: apertura automática
        const val LECTURA = "lectura"     // Lectura enviada por el sensor

        fun desde(s: DataSnapshot): Accion? {
            val fecha = (s.child("fecha").value as? Number)?.toLong() ?: return null
            return Accion(
                fecha = fecha,
                rut = s.child("rut").getValue(String::class.java) ?: "",
                tipo = s.child("tipo").getValue(String::class.java) ?: "",
                accion = s.child("accion").getValue(String::class.java) ?: "",
                temperatura = (s.child("temperatura").value as? Number)?.toDouble() ?: 0.0
            )
        }
    }
} // Cierra la clase "Accion".
