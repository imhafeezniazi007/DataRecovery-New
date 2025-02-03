package com.example.datarecovery.views.adapters

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AppPreferences

class AudiosAdapter(val context: Context, private var list:ArrayList<FilesModel>, val dataListener: DataListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var rowIndex = -1
    var check = false
    var isDataLoaded = false
    var isWatchAd = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audios_layout, parent, false)
             PostViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_more_layout, parent, false)
           ViewMoreViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder.itemViewType == 1){
            val holder = holder as ViewMoreViewHolder
            holder.viewMoreTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.font_size))
            if (isDataLoaded) {
                holder.viewMoreTV.setOnClickListener {
                    dataListener.onClickWatchAd(true)
                }
            }
        }else {
            val holder = holder as PostViewHolder
            val singleItem = list.get(position).file
            holder.nameTV.text = singleItem.name

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
            }

            holder.itemView.setOnLongClickListener {
                if (isDataLoaded) {
                    check = true
                    notifyDataSetChanged()
                }
                return@setOnLongClickListener true
            }
            if (check) {
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = false
            } else {
                holder.checkbox.visibility = View.GONE
            }
        }
    }

    override fun getItemCount():  Int {
//        return list.size
       return if (!AppPreferences.getInstance(context).isAppPurchased){
            if (list.size > 500 && !isWatchAd && isDataLoaded) {
               500
           } else {
               list.size
           }
       }else{
           list.size
       }

    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

    }
    class ViewMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val viewMoreTV: TextView = itemView.findViewById(R.id.viewMoreTV)


    }

    fun setLongClickFalse(){
        check = false
        notifyDataSetChanged()
    }
    fun setIsDataLoaded(){
        isDataLoaded = true
        notifyDataSetChanged()
    }
    fun setIsWatchAd(){
        isWatchAd = true
        notifyDataSetChanged()
    }
    override fun getItemViewType(position: Int): Int {
       return 0

//        return if (position == 499 && !isWatchAd && isDataLoaded && !AppPreferences.getInstance(context).isAppPurchased) {
//            1
//        } else {
//            0
//        }
    }
}