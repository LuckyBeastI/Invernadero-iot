package cl.invernadero.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/** Pantalla "Ver Perfil": todos los datos del usuario que inició sesión. */
class Perfil : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvRut: TextView
    private lateinit var tvNom: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvCargo: TextView
    private lateinit var tvPermiso: TextView
    private lateinit var tvClave: TextView
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ajustarBordes()

        val usu = intent.obtenerUsuario() ?: return irAlLogin()

        tvNombre = findViewById(R.id.tvNombre)
        tvRut = findViewById(R.id.tvRut)
        tvNom = findViewById(R.id.tvNom)
        tvApellido = findViewById(R.id.tvApellido)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvCargo = findViewById(R.id.tvCargo)
        tvPermiso = findViewById(R.id.tvPermiso)
        tvClave = findViewById(R.id.tvClave)
        btnVolver = findViewById(R.id.btnVolver)

        tvNombre.text = usu.nombreCompleto
        tvRut.text = usu.rut
        tvNom.text = usu.nombre
        tvApellido.text = usu.apellido
        tvCorreo.text = usu.correo
        tvCargo.text = usu.cargo
        tvPermiso.text = if (usu.esOperador) "Operador (control)" else "Monitor (solo lectura)"
        // La contraseña no se muestra: en Firebase solo existe su hash
        tvClave.text = "•••••••• (protegida)"

        btnVolver.setOnClickListener { finish() }

    } // Cierra el "onCreate".

} // Cierra la clase "Perfil".
