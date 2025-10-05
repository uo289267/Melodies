package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.HistoryEntry

class HistoryEntryAdapter(private val entries: List<HistoryEntry>) :
    RecyclerView.Adapter<HistoryEntryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvHistoryName)
        val tvTime: TextView = itemView.findViewById(R.id.tvHistoryTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_history_entry_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvName.text = entry.nameOfSheet
        holder.tvTime.text = entry.formattedTime
    }

    override fun getItemCount(): Int = entries.size
}
