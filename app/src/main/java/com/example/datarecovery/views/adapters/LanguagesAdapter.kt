package com.example.datarecovery.views.adapters

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener

class LanguagesAdapter(val context: Context, private var list:ArrayList<String>, val dataListener: DataListener) : RecyclerView.Adapter<LanguagesAdapter.PostViewHolder>() {
    var selectedPosition = 5
    var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_layout, parent, false)
        return PostViewHolder(view)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        holder.languageTv.text = list.get(position)


        holder.itemView.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
            dataListener.onRecieve(position)
        }
        Log.d("TAG", "onBindViewHolder: $position")
        Log.d("TAG", "onBindViewHolder: $selectedPosition")

        if (selectedPosition === position) {
            val tV =  TypedValue();
            val txtV =  TypedValue();
            val theme = context.theme;
            val success = theme.resolveAttribute(R.attr.text_color, tV, true);
            val txtColor = theme.resolveAttribute(R.attr.custom_blue, txtV, true);
            var colorFromTheme = 0;
            if(success) {
                colorFromTheme = tV.data;
                holder.languageTv.setBackgroundColor(colorFromTheme)
                holder.languageTv.setTextColor(txtV.data)
            }
            else
            {
                holder.itemView.setBackgroundColor(context.resources.getColor(R.color.green))
            }

        } else {
            val txtV =  TypedValue();
            val theme = context.theme;
            val success = theme.resolveAttribute(R.attr.text_color, txtV, true);

            var colorFromTheme = 0;
            if(success) {
                colorFromTheme = txtV.data;
                holder.languageTv.background = null
                holder.languageTv.setTextColor(colorFromTheme)
            }

//            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
        }





    }

    override fun getItemCount(): Int = list.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val languageTv: TextView = itemView.findViewById(R.id.languageTv)

    }

}