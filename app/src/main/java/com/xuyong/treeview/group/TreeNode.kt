package com.xuyong.treeview.group

import android.view.View

/**
 * 节点model
 */
class TreeNode(var name1: String) {

    var type = 1
    var name2 = ""
    var deep =  0
    var x = 0
    var y = 0
    var view : View? = null
    var children : List<TreeNode>? = null

}