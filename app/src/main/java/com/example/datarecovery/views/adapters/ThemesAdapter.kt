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

class ThemesAdapter(val context: Context) : RecyclerView.Adapter<ThemesAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.theme_layout, parent, false)
        return PostViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (position == 0) {
//         holder.mainLayout.backgroundTintList = context.getColorStateList(R.color.dark_theme_tint)

            holder.mainLayout.setBackgroundColor(Color.parseColor("#F2F6F9"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#E1E8F8"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#FDF1D9"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#FEE4E5"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#DCF0E5"), PorterDuff.Mode.SRC_ATOP);

            holder.titleTV.setTextColor(Color.parseColor("#000000"))
            holder.tv.setTextColor(Color.parseColor("#000000"))
            holder.imagesTV.setTextColor(Color.parseColor("#000000"))
            holder.audiosTV.setTextColor(Color.parseColor("#000000"))
            holder.videosTV.setTextColor(Color.parseColor("#000000"))
            holder.documentsTV.setTextColor(Color.parseColor("#000000"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.black));
        }else if (position == 1) {
//         holder.mainLayout.backgroundTintList = context.getColorStateList(R.color.dark_theme_tint)

            holder.mainLayout.setBackgroundColor(Color.parseColor("#262626"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#707070"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#67635C"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#675F5F"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#5D635F"), PorterDuff.Mode.SRC_ATOP);


            holder.titleTV.setTextColor(Color.parseColor("#FFFFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }else if (position == 2) {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#493D9D"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#5E53BF"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#574DB2"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#554BAF"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#4E45A4"), PorterDuff.Mode.SRC_ATOP);


            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }else if (position == 3) {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#E38881"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#CE8C8D"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#CE8C8D"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#B07983"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#A97581"), PorterDuff.Mode.SRC_ATOP);

            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }else if (position == 4) {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#005C53"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#2B7870"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#2B7870"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#2B7870"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#2B7870"), PorterDuff.Mode.SRC_ATOP);


            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }else if (position == 5) {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#D3710B"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#DB8934"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#DB8934"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#DB8934"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#DB8934"), PorterDuff.Mode.SRC_ATOP);


            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }
        else if (position == 6) {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#048ABF"))
            holder.imagesLayout.getBackground().setColorFilter(Color.parseColor("#50ADD3"), PorterDuff.Mode.SRC_ATOP);
            holder.documentsLayout.getBackground().setColorFilter(Color.parseColor("#50ADD3"), PorterDuff.Mode.SRC_ATOP);
            holder.audiosLayout.getBackground().setColorFilter(Color.parseColor("#50ADD3"), PorterDuff.Mode.SRC_ATOP);
            holder.videosLayout.getBackground().setColorFilter(Color.parseColor("#50ADD3"), PorterDuff.Mode.SRC_ATOP);


            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.tv.setTextColor(Color.parseColor("#FFFFFF"))
            holder.imagesTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.audiosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.videosTV.setTextColor(Color.parseColor("#FFFFFF"))
            holder.documentsTV.setTextColor(Color.parseColor("#FFFFFF"))

            holder.drawerIV.setColorFilter(
                ContextCompat.getColor(context,
                R.color.white));
        }
    }

    override fun getItemCount(): Int = 7

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
        val imagesLayout: ConstraintLayout = itemView.findViewById(R.id.imagesLayout)
        val documentsLayout: ConstraintLayout = itemView.findViewById(R.id.documentsLayout)
        val audiosLayout: ConstraintLayout = itemView.findViewById(R.id.audiosLayout)
        val videosLayout: ConstraintLayout = itemView.findViewById(R.id.videosLayout)
        val drawerIV: ImageView = itemView.findViewById(R.id.drawerIV)
        val titleTV: TextView = itemView.findViewById(R.id.titleTV)
        val tv: TextView = itemView.findViewById(R.id.tv)
        val imagesTV: TextView = itemView.findViewById(R.id.imagesTV)
        val documentsTV: TextView = itemView.findViewById(R.id.documentsTV)
        val audiosTV: TextView = itemView.findViewById(R.id.audiosTV)
        val videosTV: TextView = itemView.findViewById(R.id.videosTV)


    }

}