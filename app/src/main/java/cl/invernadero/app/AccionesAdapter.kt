package cl.invernadero.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Adaptador de la lista de "Ver Acciones". */
class AccionesAdapter : RecyclerView.Adapter<AccionesAdapter.Fila>() {

    private var datos: List<Accion> = emptyList()

    fun actualizar(nuevos: List<Accion>) {
        datos = nuevos
        notifyDataSetChanged()
    }

    class Fila(v: View) : RecyclerView.ViewHolder(v) {
        val tvFecha: TextView = v.findViewById(R.id.tvFecha)
        val tvHora: TextView = v.findViewById(R.id.tvHora)
        val tvUsuario: TextView = v.findViewById(R.id.tvUsuario)
        val tvAccion: TextView = v.findViewById(R.id.tvAccion)
        val tvTemperatura: TextView = v.findViewById(R.id.tvTemperatura)
    }

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): Fila =
        Fila(LayoutInflater.from(padre.context).inflate(R.layout.item_accion, padre, false))

    override fun onBindViewHolder(f: Fila, posicion: Int) {
        val a = datos[posicion]
        f.tvFecha.text = ": ${formatoFecha(a.fecha)}"
        f.tvHora.text = ": ${formatoHora(a.fecha)}"
        f.tvUsuario.text = ": ${a.rut}"
        f.tvAccion.text = ": ${a.accion}"
        f.tvTemperatura.text = ": ${formatoTemp(a.temperatura)}"
    }

    override fun getItemCount() = datos.size

} // Cierra la clase "AccionesAdapter".
