package cl.invernadero.app

import android.util.Base64
import android.util.Patterns
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Medidas de seguridad (ISO/IEC 27400):
 *  - La contraseña nunca se guarda: se guarda su hash PBKDF2 con una "sal" aleatoria por usuario.
 *  - Validación de RUT (dígito verificador), correo y contraseña robusta.
 */
object Seguridad {

    private const val ITERACIONES = 10_000
    private const val LARGO_BITS = 256

    /** Genera una sal aleatoria distinta para cada usuario. */
    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    } // Cierra la función "nuevaSal".

    /** Calcula el hash PBKDF2 de la contraseña (no se puede revertir para obtener la contraseña). */
    fun hashClave(clave: String, sal: String): String {
        val spec = PBEKeySpec(clave.toCharArray(), Base64.decode(sal, Base64.NO_WRAP), ITERACIONES, LARGO_BITS)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    } // Cierra la función "hashClave".

    /** Compara la contraseña escrita con el hash guardado en Firebase. */
    fun verificarClave(clave: String, sal: String?, hashGuardado: String?): Boolean {
        if (sal == null || hashGuardado == null) return false
        return try {
            val calculado = hashClave(clave, sal)
            MessageDigest.isEqual(calculado.toByteArray(), hashGuardado.toByteArray())
        } catch (e: IllegalArgumentException) {
            false
        }
    } // Cierra la función "verificarClave".

    /**
     * Valida un RUT chileno y lo deja en formato 12345678-9 (sin puntos).
     * Devuelve null si el RUT o su dígito verificador no son válidos.
     */
    fun normalizarRut(texto: String): String? {
        val limpio = texto.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        if (limpio.length !in 8..9) return null
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return null
        return if (digitoVerificador(cuerpo) == dv) "$cuerpo-$dv" else null
    } // Cierra la función "normalizarRut".

    private fun digitoVerificador(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + resto
        }
    } // Cierra la función "digitoVerificador".

    fun claveEsSegura(clave: String) =
        clave.length >= 8 && clave.any { it.isDigit() } && clave.any { it.isLetter() }

    fun correoValido(correo: String) = Patterns.EMAIL_ADDRESS.matcher(correo).matches()

} // Cierra el objeto "Seguridad".
