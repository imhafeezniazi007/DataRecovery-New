package com.example.datarecovery.views.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.R
import org.w3c.dom.Text

class ThemesAdapterNew(val context: Context) : RecyclerView.Adapter<ThemesAdapterNew.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.theme_layout_new, parent, false)
        return PostViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (position == 0) {

          holder.themeIV.setImageResource(R.drawable.default_theme)
        }else if (position == 1) {
            holder.themeIV.setImageResource(R.drawable.dark_theme)

        }else if (position == 2) {
            holder.themeIV.setImageResource(R.drawable.theme_1)
        }else if (position == 3) {
            holder.themeIV.setImageResource(R.drawable.theme_2)
        }else if (position == 4) {
            holder.themeIV.setImageResource(R.drawable.theme_3)
        }else if (position == 5) {
            holder.themeIV.setImageResource(R.drawable.theme_4)
        }
        else if (position == 6) {
            holder.themeIV.setImageResource(R.drawable.theme_5)
        }
    }

    override fun getItemCount(): Int = 7

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val themeIV: ImageView = itemView.findViewById(R.id.themeIV)



    }

}