package com.example.datarecovery.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.models.Duplicate
import com.example.datarecovery.models.DuplicateFile

class DuplicateParentAdapter(private val context: Context,private val duplicateParentList: List<Duplicate>,val dataListener: DataListener) : RecyclerView.Adapter<DuplicateParentAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_duplicate_layout, parent, false)
        return PostViewHolder(view)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        holder.groupTv.text = context.getString(R.string.group) +" ${position + 1}"
        val duplicateChildAdapter = ChildAdapter(context,duplicateParentList[position].getDuplicateFiles()){ it: DuplicateFile ->
            dataListener.onRecieve(it)
        }
        holder.childRv.adapter = duplicateChildAdapter

    }

    override fun getItemCount(): Int = duplicateParentList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val groupTv: TextView = itemView.findViewById(R.id.groupTv)
        val childRv: RecyclerView = itemView.findViewById(R.id.childRv)

    }
}