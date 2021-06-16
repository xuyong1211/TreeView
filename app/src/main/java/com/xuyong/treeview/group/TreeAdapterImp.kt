package com.xuyong.treeview.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xuyong.treeview.R

class TreeAdapterImp : TreeAdapter {
    override var treeNode: TreeNode? = null
    override fun getView(node: TreeNode, viewGroup: ViewGroup): View {
        val view =  LayoutInflater.from(viewGroup.context).inflate(R.layout.tree_node, viewGroup,false)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        tvName.text = node.name1
        return view
    }
}