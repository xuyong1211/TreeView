package com.xuyong.treeview.group

import android.view.View
import android.view.ViewGroup

/**
 * treeView 适配器。 用于提供每个node的子view
 */
interface TreeAdapter{

    var treeNode: TreeNode?

    fun getView(node: TreeNode, viewGroup: ViewGroup):View

    fun getViewType(node:TreeNode):Int

}