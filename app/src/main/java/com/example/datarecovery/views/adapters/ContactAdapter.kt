package com.example.datarecovery.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel

class ContactAdapter(private val context: Context, private val childContactList: ArrayList<ContactModel>,
                     private val selectedList: ArrayList<ContactModel>,private var onItemClicked: ((contactModel: ContactModel) -> Unit)
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.child_duplicate_layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val singleItem = childContactList[position]
        holder.name.text = singleItem.name
        holder.path.text = singleItem.mobileNumber
        holder.size.visibility = View.GONE

        holder.checkbox.isChecked = selectedList.contains(singleItem)
        Glide.with(context).load(R.drawable.ic_contacts_logo)
            .placeholder(R.drawable.ic_contacts_logo)
            .error(R.drawable.ic_contacts_logo)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
            .into(holder.thumbnail)

        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("TAG", "onBindViewHolder: setOnCheckedChangeListener ${isChecked}")
            Log.d("TAG", "onBindViewHolder: ${childContactList.get(position).name}")
            onItemClicked(childContactList.get(position))
        }



    }

    override fun getItemCount(): Int = childContactList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val name: TextView = itemView.findViewById(R.id.fileName)
        val path: TextView = itemView.findViewById(R.id.filePath)
        val size: TextView = itemView.findViewById(R.id.fileSize)
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)


    }
}