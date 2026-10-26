package cl.invernadero.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

/** Pantalla "Ver Acciones": listado en tiempo real de las acciones registradas en Firebase. */
class Acciones : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvVacio: TextView
    private lateinit var rvAcciones: RecyclerView
    private lateinit var btnVolver: Button

    private val adaptador = AccionesAdapter()
    private val consulta: Query get() = BaseDatos.acciones.limitToLast(50)

    private val escucha = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lista = snapshot.children.mapNotNull { Accion.desde(it) }.reversed() // más reciente primero
            adaptador.actualizar(lista)
            tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }

        override fun onCancelled(error: DatabaseError) {
            mensaje("No se pudieron leer las acciones.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acciones)
        ajustarBordes()

        val usu = intent.obtenerUsuario() ?: return irAlLogin()

        tvNombre = findViewById(R.id.tvNombre)
        tvVacio = findViewById(R.id.tvVacio)
        rvAcciones = findViewById(R.id.rvAcciones)
        btnVolver = findViewById(R.id.btnVolver)

        tvNombre.text = usu.nombreCompleto
        rvAcciones.layoutManager = LinearLayoutManager(this)
        rvAcciones.adapter = adaptador
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

} // Cierra la clase "Acciones".
