package cl.invernadero.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

/**
 * Pantalla "Manipular Circuito":
 *  - Muestra en tiempo real la temperatura y el estado de la ventana (servomotor).
 *  - El Operador puede abrir/cerrar la ventana y simular lecturas del sensor (ESP32).
 *  - El Monitor solo observa (control de acceso).
 */
class ManipularCircuito : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvVentana: TextView
    private lateinit var tvAviso: TextView
    private lateinit var btnAbrir: Button
    private lateinit var btnCerrar: Button
    private lateinit var layoutSimulador: LinearLayout
    private lateinit var sliderTemp: Slider
    private lateinit var tvTempSim: TextView
    private lateinit var btnPublicar: Button
    private lateinit var btnVolver: Button

    private lateinit var usu: Usuario
    private var temperaturaActual = 0.0
    private var ventanaAbierta = false

    /** Escucha los cambios del estado en Firebase (tiempo real entre dispositivos). */
    private val escucha = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val hayDatos = snapshot.hasChild("temperatura")
            temperaturaActual = (snapshot.child("temperatura").value as? Number)?.toDouble() ?: 0.0
            ventanaAbierta = snapshot.child("ventanaAbierta").getValue(Boolean::class.java) ?: false

            tvTemperatura.text = if (hayDatos) formatoTemp(temperaturaActual) else "-- °C"
            val alta = hayDatos && temperaturaActual > BaseDatos.UMBRAL_TEMPERATURA
            tvTemperatura.setTextColor(
                ContextCompat.getColor(
                    this@ManipularCircuito,
                    if (alta) R.color.rojo_alerta else R.color.verde_ok
                )
            )
            tvVentana.text = if (ventanaAbierta) "Ventana: ABIERTA" else "Ventana: CERRADA"
        }

        override fun onCancelled(error: DatabaseError) {
            mensaje("No se pudieron leer los datos del invernadero.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manipular_circuito)
        ajustarBordes()

        usu = intent.obtenerUsuario() ?: return irAlLogin()

        tvNombre = findViewById(R.id.tvNombre)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvVentana = findViewById(R.id.tvVentana)
        tvAviso = findViewById(R.id.tvAviso)
        btnAbrir = findViewById(R.id.btnAbrir)
        btnCerrar = findViewById(R.id.btnCerrar)
        layoutSimulador = findViewById(R.id.layoutSimulador)
        sliderTemp = findViewById(R.id.sliderTemp)
        tvTempSim = findViewById(R.id.tvTempSim)
        btnPublicar = findViewById(R.id.btnPublicar)
        btnVolver = findViewById(R.id.btnVolver)

        tvNombre.text = usu.nombreCompleto
        aplicarPermisos()

        btnAbrir.setOnClickListener { cambiarVentana(true) }
        btnCerrar.setOnClickListener { cambiarVentana(false) }

        tvTempSim.text = "Simular lectura: ${formatoTemp(sliderTemp.value.toDouble())}"
        sliderTemp.addOnChangeListener { _, valor, _ ->
            tvTempSim.text = "Simular lectura: ${formatoTemp(valor.toDouble())}"
        }
        btnPublicar.setOnClickListener { publicarLectura(sliderTemp.value.toDouble()) }
        btnVolver.setOnClickListener { finish() }

    } // Cierra el "onCreate".


    override fun onStart() {
        super.onStart()
        BaseDatos.estado.addValueEventListener(escucha)
    }

    override fun onStop() {
        super.onStop()
        BaseDatos.estado.removeEventListener(escucha)
    }


    /** Control de acceso: solo el Operador puede manipular el invernadero. */
    private fun aplicarPermisos() {
        val operador = usu.esOperador
        btnAbrir.isEnabled = operador
        btnCerrar.isEnabled = operador
        layoutSimulador.visibility = if (operador) View.VISIBLE else View.GONE
        tvAviso.visibility = if (operador) View.GONE else View.VISIBLE
    } // Cierra la función "aplicarPermisos".


    /** Activar / Desactivar: abre o cierra la ventana (servomotor). */
    private fun cambiarVentana(abrir: Boolean) {
        if (!usu.esOperador) return
        BaseDatos.estado.updateChildren(
            mapOf("ventanaAbierta" to abrir, "actualizado" to ServerValue.TIMESTAMP)
        ).addOnSuccessListener {
            BaseDatos.registrarAccion(
                usu,
                if (abrir) Accion.APERTURA else Accion.CIERRE,
                if (abrir) "Apertura manual de ventana" else "Cierre manual de ventana",
                temperaturaActual
            )
            mensaje(if (abrir) "Ventana abierta" else "Ventana cerrada")
        }.addOnFailureListener {
            mensaje("No se pudo enviar la orden al invernadero.")
        }
    } // Cierra la función "cambiarVentana".


    /**
     * Simula el sensor del ESP32: publica la temperatura y, si supera el umbral,
     * abre la ventana automáticamente.
     */
    private fun publicarLectura(temperatura: Double) {
        if (!usu.esOperador) return
        val superaUmbral = temperatura > BaseDatos.UMBRAL_TEMPERATURA
        val aperturaAutomatica = superaUmbral && !ventanaAbierta

        BaseDatos.estado.setValue(
            mapOf(
                "temperatura" to temperatura,
                "ventanaAbierta" to (superaUmbral || ventanaAbierta),
                "actualizado" to ServerValue.TIMESTAMP
            )
        ).addOnSuccessListener {
            BaseDatos.registrarAccion(usu, Accion.LECTURA, "Lectura del sensor", temperatura)
            if (aperturaAutomatica) {
                BaseDatos.registrarAccion(
                    usu, Accion.ALERTA, "Temperatura alta: apertura automática", temperatura
                )
                mensaje("¡ Temperatura alta ! Ventana abierta automáticamente.")
            }
        }.addOnFailureListener {
            mensaje("No se pudo publicar la lectura.")
        }
    } // Cierra la función "publicarLectura".

} // Cierra la clase "ManipularCircuito".
