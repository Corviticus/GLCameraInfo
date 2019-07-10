package com.example.cameracheckup

import android.widget.TextView
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class GLExtensionsAdapter(
    private val mContext: Context,
    private val mDataModel: MutableList<String>) :
    ArrayAdapter<String>(mContext, R.layout.row_item, mDataModel) {

    private class ViewHolder {
        internal var extensionName: TextView? = null
    }

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, glExtensionsRowView: View?, parent: ViewGroup): View {

        val glExtensionsViewHolder = ViewHolder()
        val glExtension = mDataModel[position]

        val rowView: View = when (glExtensionsRowView) {
            null -> inflater.inflate(R.layout.row_item, parent, false)
            else -> glExtensionsRowView
        }

        glExtensionsViewHolder.extensionName = rowView.findViewById(R.id.gl_extensions_text_view)
        glExtensionsViewHolder.extensionName?.text = glExtension

        return rowView
    }
}
