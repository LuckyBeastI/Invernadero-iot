package cl.invernadero.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

/** Pantalla "Estadísticas": cantidades calculadas a partir del historial de acciones. */
class Estadisticas : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvAperturas: TextView
    private lateinit var tvCierres: TextView
    private lateinit var tvAlertas: TextView
    private lateinit var tvLecturas: TextView
    private lateinit var tvMaxima: TextView
    private lateinit var tvPromedio: TextView
    private lateinit var btnVolver: Button

    private val consulta: Query get() = BaseDatos.acciones.limitToLast(500)

    private val escucha = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val acciones = snapshot.children.mapNotNull { Accion.desde(it) }
            val lecturas = acciones.filter { it.tipo == Accion.LECTURA }.map { it.temperatura }

            tvAperturas.text = acciones.count { it.tipo == Accion.APERTURA }.toString()
            tvCierres.text = acciones.count { it.tipo == Accion.CIERRE }.toString()
            tvAlertas.text = acciones.count { it.tipo == Accion.ALERTA }.toString()
            tvLecturas.text = lecturas.size.toString()
            tvMaxima.text = if (lecturas.isEmpty()) "--" else formatoTemp(lecturas.max())
            tvPromedio.text = if (lecturas.isEmpty()) "--" else formatoTemp(lecturas.average())
        }

        override fun onCancelled(error: DatabaseError) {
            mensaje("No se pudieron calcular las estadísticas.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estadisticas)
        ajustarBordes()

        val usu = intent.obtenerUsuario() ?: return irAlLogin()

        tvNombre = findViewById(R.id.tvNombre)
        tvAperturas = findViewById(R.id.tvAperturas)
        tvCierres = findViewById(R.id.tvCierres)
        tvAlertas = findViewById(R.id.tvAlertas)
        tvLecturas = findViewById(R.id.tvLecturas)
        tvMaxima = findViewById(R.id.tvMaxima)
        tvPromedio = findViewById(R.id.tvPromedio)
        btnVolver = findViewById(R.id.btnVolver)

        tvNombre.text = usu.nombreCompleto
        btnVolver.setOnClickListener { finish() }

    } // Cierra el "onCreate".

    override fun onStart() {
        super.onStart()
        consulta.addValueEventListener(escucha)
    }

    override fun onStop() {
        super.onStop()
        consulta.removeEventListener(escucha)
    }

} // Cierra la clase "Estadisticas".
