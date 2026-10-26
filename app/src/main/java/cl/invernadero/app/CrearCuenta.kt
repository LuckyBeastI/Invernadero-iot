package cl.invernadero.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ServerValue

/** Pantalla "Crear Cuenta": registra al usuario en la colección "usuarios" de Firebase. */
class CrearCuenta : AppCompatActivity() {

    private lateinit var txtRut: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtApellido: EditText
    private lateinit var txtCorreo: EditText
    private lateinit var spCargo: Spinner
    private lateinit var txtClave: EditText
    private lateinit var txtRepetir: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button
    private lateinit var progreso: ProgressBar

    private val cargos = listOf(
        "Seleccione su cargo",
        "Administrador",
        "Encargado de invernadero",
        "Técnico",
        "Supervisor"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_cuenta)
        ajustarBordes()

        txtRut = findViewById(R.id.txtRut)
        txtNombre = findViewById(R.id.txtNombre)
        txtApellido = findViewById(R.id.txtApellido)
        txtCorreo = findViewById(R.id.txtCorreo)
        spCargo = findViewById(R.id.spCargo)
        txtClave = findViewById(R.id.txtClave)
        txtRepetir = findViewById(R.id.txtRepetir)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolver = findViewById(R.id.btnVolver)
        progreso = findViewById(R.id.progreso)

        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCargo.adapter = adaptador

        botonRegistrar()
        btnVolver.setOnClickListener { finish() }

    } // Cierra el "onCreate".


    private fun botonRegistrar() {
        btnRegistrar.setOnClickListener {
            val rut = Seguridad.normalizarRut(txtRut.text.toString())
            val nombre = txtNombre.text.toString().trim()
            val apellido = txtApellido.text.toString().trim()
            val correo = txtCorreo.text.toString().trim().lowercase()
            val cargo = spCargo.selectedItem.toString()
            val clave = txtClave.text.toString()
            val repetir = txtRepetir.text.toString()

            val error = when {
                rut == null -> "RUT inválido. Ejemplo: 12345678-5"
                nombre.isEmpty() || apellido.isEmpty() -> "Ingrese nombre y apellido."
                !Seguridad.correoValido(correo) -> "Correo electrónico inválido."
                spCargo.selectedItemPosition == 0 -> "Seleccione un cargo."
                !Seguridad.claveEsSegura(clave) -> "La contraseña debe tener 8 o más caracteres, con letras y números."
                clave != repetir -> "Las contraseñas no coinciden."
                else -> null
            }
            if (error != null || rut == null) {
                mensaje(error ?: "Revise los datos.")
                return@setOnClickListener
            }

            ocultarTeclado()
            cargando(true)
            BaseDatos.conectar(
                alConectar = { guardarUsuario(rut, nombre, apellido, correo, cargo, clave) },
                alFallar = {
                    cargando(false)
                    mensaje("No se pudo conectar con Firebase. Revise su conexión.")
                }
            )
        }
    } // Cierra la función "botonRegistrar".


    private fun guardarUsuario(
        rut: String, nombre: String, apellido: String,
        correo: String, cargo: String, clave: String
    ) {
        val ref = BaseDatos.usuarios.child(rut)
        ref.get()
            .addOnSuccessListener { x ->
                if (x.exists()) {
                    cargando(false)
                    mensaje("Ya existe una cuenta con ese RUT.")
                    return@addOnSuccessListener
                }

                val sal = Seguridad.nuevaSal()
                val datos = mapOf(
                    "rut" to rut,
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "correo" to correo,
                    "cargo" to cargo,
                    "permiso" to Usuario.PERMISO_MONITOR, // el permiso de operador se asigna desde la consola
                    "hash" to Seguridad.hashClave(clave, sal),
                    "sal" to sal,
                    "creado" to ServerValue.TIMESTAMP
                )
                ref.setValue(datos)
                    .addOnSuccessListener {
                        mensaje("¡ Cuenta creada ! Ya puede iniciar sesión.")
                        finish()
                    }
                    .addOnFailureListener {
                        cargando(false)
                        mensaje("No se pudo registrar la cuenta.")
                    }
            }
            .addOnFailureListener {
                cargando(false)
                mensaje("Error al consultar la base de datos.")
            }
    } // Cierra la función "guardarUsuario".


    private fun cargando(activo: Boolean) {
        progreso.visibility = if (activo) View.VISIBLE else View.GONE
        btnRegistrar.isEnabled = !activo
    } // Cierra la función "cargando".

} // Cierra la clase "CrearCuenta".
