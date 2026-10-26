package cl.invernadero.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/** Pantalla "Login": valida RUT y contraseña contra los usuarios guardados en Realtime Database. */
class Login : AppCompatActivity() {

    private lateinit var txtRut: EditText
    private lateinit var txtClave: EditText
    private lateinit var btnIniciar: Button
    private lateinit var btnCrear: Button
    private lateinit var progreso: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ajustarBordes()

        txtRut = findViewById(R.id.txtRut)
        txtClave = findViewById(R.id.txtClave)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnCrear = findViewById(R.id.btnCrear)
        progreso = findViewById(R.id.progreso)

        botonIniciar()
        botonCrearCuenta()

    } // Cierra el "onCreate".


    private fun botonIniciar() {
        btnIniciar.setOnClickListener {
            val rut = Seguridad.normalizarRut(txtRut.text.toString())
            val clave = txtClave.text.toString()

            if (rut == null) {
                mensaje("¡ RUT inválido ! Ejemplo: 12345678-5")
                return@setOnClickListener
            }
            if (clave.isEmpty()) {
                mensaje("¡ Ingrese su contraseña !")
                return@setOnClickListener
            }

            // Control de acceso: bloqueo temporal tras 5 intentos fallidos
            val espera = (bloqueadoHasta - System.currentTimeMillis()) / 1000
            if (espera > 0) {
                mensaje("Demasiados intentos. Espere $espera segundos.")
                return@setOnClickListener
            }

            ocultarTeclado()
            cargando(true)
            BaseDatos.conectar(
                alConectar = { buscarUsuario(rut, clave) },
                alFallar = {
                    cargando(false)
                    mensaje("No se pudo conectar con Firebase. Revise su conexión.")
                }
            )
        }
    } // Cierra la función "botonIniciar".


    private fun buscarUsuario(rut: String, clave: String) {
        BaseDatos.usuarios.child(rut).get()
            .addOnSuccessListener { x ->
                val correcto = x.exists() && Seguridad.verificarClave(
                    clave,
                    x.child("sal").getValue(String::class.java),
                    x.child("hash").getValue(String::class.java)
                )
                cargando(false)

                if (!correcto) {
                    registrarFallo()
                    return@addOnSuccessListener
                }

                intentosFallidos = 0
                val usu = Usuario.desde(x)
                val intent = Intent(this@Login, MenuPrincipal::class.java)
                intent.putExtra("usu", usu)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                cargando(false)
                mensaje("Error al consultar la base de datos.")
            }
    } // Cierra la función "buscarUsuario".


    private fun registrarFallo() {
        intentosFallidos++
        if (intentosFallidos >= 5) {
            bloqueadoHasta = System.currentTimeMillis() + 30_000
            intentosFallidos = 0
        }
        txtClave.setText("")
        // Mensaje genérico: no revela si el RUT existe o no
        mensaje("¡ Error de RUT y/o Contraseña !")
    } // Cierra la función "registrarFallo".


    private fun botonCrearCuenta() {
        btnCrear.setOnClickListener {
            startActivity(Intent(this@Login, CrearCuenta::class.java))
        }
    } // Cierra la función "botonCrearCuenta".


    private fun cargando(activo: Boolean) {
        progreso.visibility = if (activo) View.VISIBLE else View.GONE
        btnIniciar.isEnabled = !activo
        btnCrear.isEnabled = !activo
    } // Cierra la función "cargando".


    companion object {
        private var intentosFallidos = 0
        private var bloqueadoHasta = 0L
    }

} // Cierra la clase "Login".
