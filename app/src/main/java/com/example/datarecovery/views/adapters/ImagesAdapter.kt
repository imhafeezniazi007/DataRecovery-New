package com.example.datarecovery.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.models.RecylerViewModel
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant


class ImagesAdapter(
    val context: Context,
    private var list: ArrayList<FilesModel>,
    val dataListener: DataListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var rowIndex = -1
    var check = false
    var isDataLoaded = false
    var isWatchAd = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_layout, parent, false)
            PostViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_more_layout, parent, false)
            ViewMoreViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        Log.d("TAG", "multipleViewtypes: position $position , ${holder.itemViewType}")
        if (holder.itemViewType == 1) {
            val holder = holder as ViewMoreViewHolder
            if (isDataLoaded) {
                holder.viewMoreTV.setOnClickListener {
                    dataListener.onClickWatchAd(true)
                }
            }
        } else {
            val holder = holder as PostViewHolder
            holder.itemView.setOnClickListener {
                if (isDataLoaded) {
                    if (check) {
                        holder.checkbox.isChecked = !holder.checkbox.isChecked()
                        list.get(position).isCheck = holder.checkbox.isChecked
                        dataListener.onRecieve(list.get(position))
                    } else {
                        dataListener.onClick(list.get(position))
                    }
                }
//            rowIndex=position;
//            notifyDataSetChanged();
            }
            if (check) {
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = false
            } else {
                holder.checkbox.visibility = View.GONE
            }

            val singleItem = list[position].file
            try {
                Glide.with(context)
                    .load("file://" + singleItem.path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .centerCrop()
                    .error(R.drawable.home)
                    .into(holder.imageView)
            } catch (e: Exception) {
                //do nothing
                Toast.makeText(context, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
            }

            holder.itemView.setOnLongClickListener {
                if (isDataLoaded) {
                    check = true
                    notifyDataSetChanged()
                }
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount(): Int {
//        return list.size
        return if (!AppPreferences.getInstance(context).isAppPurchased) {
            if (list.size > 500 && !isWatchAd && isDataLoaded) {
                500
            } else {
                list.size
            }
        } else {
            list.size
        }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageIV)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

    }

    class ViewMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewMoreTV: TextView = itemView.findViewById(R.id.viewMoreTV)

    }

    fun setLongClickFalse() {
        check = false
        notifyDataSetChanged()
    }

    fun setIsDataLoaded() {
        isDataLoaded = true
        notifyDataSetChanged()
    }

    fun setIsWatchAd() {
        isWatchAd = true
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
       return 0
//        return if (position == 9 && !isWatchAd && isDataLoaded && !AppPreferences.getInstance(context).isAppPurchased) {
//            1
//        } else {
//            0
//        }
    }
}