package pl.podkal.citycaller.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionScene.Transition.TransitionOnClick
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.databinding.IncidentRowBinding

class IncidentdAdapter(private val incidents: List<IncidentModel>,
                       private val onClick: (IncidentModel) -> Unit):
    RecyclerView.Adapter<IncidentdAdapter.IncidentViewHolder>() {

    inner class IncidentViewHolder(binding: IncidentRowBinding):
            RecyclerView.ViewHolder(binding.root) {
                init {
                    binding.incidentRow.setOnClickListener {
                        onClick(incidents[adapterPosition])
                    }
                }
        val img = binding.incidentImg
        val desc = binding.incidentDescTv
        val reactions = binding.reactionNumberTv
            }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        return IncidentViewHolder(
            IncidentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun getItemCount(): Int {
        return incidents.size
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        bindData(holder, position)
    }
    private fun bindData(holder: IncidentViewHolder, position: Int) {
        Glide.
        with(holder.img)
            .load(incidents[position].imageUrl)
            .into(holder.img)

        holder.desc.text = incidents[position].desc

        val reactInt = incidents[position].reactions


        if (reactInt != null) holder.reactions.text = "Reakcje: 0"
        else holder.reactions.text = "Reakcje: ${reactInt}"

    }
}