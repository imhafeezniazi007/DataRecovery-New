package com.example.datarecovery.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.datarecovery.R

class CustomAdapter(
    context: Context,
    languages: ArrayList<String>
) :
    ArrayAdapter<String?>(context, 0, languages as List<String>) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    private fun initView(
        position: Int, convertView: View,
        parent: ViewGroup
    ): View {
        // It is used to set our custom view.
        var convertView = convertView
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        }
        val textViewName = convertView.findViewById<TextView>(R.id.spTV)
        val currentItem: String? = getItem(position)

        // It is used the name to the TextView when the
        // current item is not null.
        if (currentItem != null) {
            textViewName.setText(currentItem)
        }
        return convertView
    }
}
