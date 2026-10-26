package cl.invernadero.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Evita que el contenido quede bajo la barra de estado (mismo código del ejemplo de clase). */
fun AppCompatActivity.ajustarBordes() {
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}

/**
 * Recibe el objeto "usu" enviado por la pantalla anterior.
 * Funciona en todas las versiones de Android (no solo desde Android 13).
 */
@Suppress("DEPRECATION")
fun Intent.obtenerUsuario(): Usuario? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra("usu", Usuario::class.java)
    } else {
        getSerializableExtra("usu") as? Usuario
    }

/** Vuelve al login borrando las pantallas anteriores (no se puede volver atrás con la flecha). */
fun AppCompatActivity.irAlLogin() {
    startActivity(
        Intent(this, Login::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}

fun Context.mensaje(texto: String) = Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()

fun AppCompatActivity.ocultarTeclado() {
    val vista = currentFocus ?: return
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(vista.windowToken, 0)
}

fun formatoFecha(ts: Long): String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(ts))
fun formatoHora(ts: Long): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
fun formatoTemp(valor: Double): String = String.format(Locale.getDefault(), "%.1f °C", valor)
