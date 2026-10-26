package cl.invernadero.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Pantalla "Menú": muestra el usuario logueado y redirige a las demás pantallas. */
class MenuPrincipal : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var btnManipular: Button
    private lateinit var btnAcciones: Button
    private lateinit var btnEstadisticas: Button
    private lateinit var btnPerfil: Button
    private lateinit var btnCerrar: Button

    private var usu: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ajustarBordes()

        usu = intent.obtenerUsuario()
        val usuario = usu ?: return irAlLogin()

        tvNombre = findViewById(R.id.tvNombre)
        btnManipular = findViewById(R.id.btnManipular)
        btnAcciones = findViewById(R.id.btnAcciones)
        btnEstadisticas = findViewById(R.id.btnEstadisticas)
        btnPerfil = findViewById(R.id.btnPerfil)
        btnCerrar = findViewById(R.id.btnCerrar)

        tvNombre.text = usuario.nombreCompleto

        abrir(btnManipular, ManipularCircuito::class.java)
        abrir(btnAcciones, Acciones::class.java)
        abrir(btnEstadisticas, Estadisticas::class.java)
        abrir(btnPerfil, Perfil::class.java)
        botonCerrarSesion()
        desactivarFlecha()

    } // Cierra el "onCreate".


    /** Abre la pantalla indicada enviándole el objeto "usu". */
    private fun abrir(boton: Button, pantalla: Class<*>) {
        boton.setOnClickListener {
            val intent = Intent(this@MenuPrincipal, pantalla)
            intent.putExtra("usu", usu)
            startActivity(intent)
        }
    } // Cierra la función "abrir".


    private fun botonCerrarSesion() {
        btnCerrar.setOnClickListener {
            val a = AlertDialog.Builder(this@MenuPrincipal)
            a.setTitle("Pregunta")
            a.setMessage("¿Desea cerrar la sesión?")
            a.setIcon(android.R.drawable.ic_dialog_alert)
            a.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            a.setPositiveButton("Aceptar") { _, _ ->
                BaseDatos.cerrarSesion()
                irAlLogin()
            }
            a.setCancelable(false)
            a.show()
        }
    } // Cierra la función "botonCerrarSesion".


    /** Evita volver al login con la flecha atrás (se debe usar "Cerrar Sesión"). */
    private fun desactivarFlecha() {
        onBackPressedDispatcher.addCallback(this) { }
    } // Cierra la función "desactivarFlecha".

} // Cierra la clase "MenuPrincipal".
