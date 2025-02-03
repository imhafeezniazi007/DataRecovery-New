package com.example.datarecovery.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel

class ImagesNewAdapter(val context: Context, val dataListener: DataListener) : RecyclerView.Adapter<ImagesNewAdapter.PostViewHolder>() {
    var rowIndex = -1
    var check = false
    var isDataLoaded = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_layout, parent, false)
        return PostViewHolder(view)
    }

    private val itemDifferCallback = object: DiffUtil.ItemCallback<FilesModel>(){
        override fun areItemsTheSame(oldItem: FilesModel, newItem: FilesModel): Boolean {
            return oldItem.file == newItem.file
            return oldItem.isCheck == newItem.isCheck
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: FilesModel, newItem: FilesModel): Boolean {
            return oldItem == newItem
        }

    }

    private val itemDiffer = AsyncListDiffer(this, itemDifferCallback)

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            if(isDataLoaded) {
                if (check) {
                    holder.checkbox.isChecked = !holder.checkbox.isChecked
                    itemDiffer.currentList[position].isCheck = holder.checkbox.isChecked
                    dataListener.onRecieve(itemDiffer.currentList[position])
                } else {
                    dataListener.onClick(itemDiffer.currentList[position])
                }
            }
            rowIndex=position;
//            notifyDataSetChanged();
        }
        if (check) {
            holder.checkbox.visibility = View.VISIBLE
            holder.checkbox.isChecked = false
        }else{
            holder.checkbox.visibility = View.GONE
        }

        val singleItem = itemDiffer.currentList.get(position).file

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
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        /*if(rowIndex == position){
//            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.green))
//            holder.textView.setBackgroundResource(R.color.white)
            holder.textView.setTextColor(Color.parseColor("#59AA15"));

        }else{
            holder.textView.setTextColor(Color.parseColor("#FFFFFFFF"));
        }*/
        holder.itemView.setOnLongClickListener {
            if(isDataLoaded) {
                check = true
//                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }



    }

    override fun getItemCount(): Int = itemDiffer.currentList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageIV)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

    }

   fun setLongClickFalse(){
       check = false
       notifyDataSetChanged()
   }
    fun setIsDataLoaded(){
       isDataLoaded = true
       notifyDataSetChanged()
   }
    fun submitList(list: ArrayList<FilesModel>) {
        itemDiffer.submitList(list)
    }
}