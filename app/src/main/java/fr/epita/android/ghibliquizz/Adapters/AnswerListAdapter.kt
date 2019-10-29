package fr.epita.android.ghibliquizz.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.epita.android.ghibliquizz.Models.PeopleObject
import fr.epita.android.ghibliquizz.R

const val genderMaleUrl = "https://img.icons8.com/ultraviolet/40/000000/male.png"
const val genderFemaleUrl = "https://img.icons8.com/ultraviolet/40/000000/female.png"

class AnswerListAdapter(
    private val context : Context,
    private val data: ArrayList<PeopleObject>,
    private val itemOnClickListener: View.OnClickListener
) : RecyclerView.Adapter<AnswerListAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView = itemView.findViewById(R.id.name)
        val genderView: ImageView = itemView.findViewById(R.id.gender)
        val ageView: TextView = itemView.findViewById(R.id.age)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = LayoutInflater
            .from(context)
            .inflate(R.layout.activity_main_answer_row, parent, false)

        rowView.setOnClickListener(itemOnClickListener)

        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = data[position]

        holder.nameView.text = currentItem.name
        holder.ageView.text = currentItem.age

        if (currentItem.gender == "Male") {
            Glide
                .with(context)
                .load(genderMaleUrl)
                .error(AppCompatResources.getDrawable(context, R.drawable.error_loading))
                .into(holder.genderView)
        } else {
            Glide
                .with(context)
                .load(genderFemaleUrl)
                .error(AppCompatResources.getDrawable(context, R.drawable.error_loading))
                .into(holder.genderView)
        }



        holder.itemView.tag = currentItem.id
    }
}