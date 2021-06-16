package com.xuyong.treeview.group

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.children
import com.xuyong.treeview.px
import kotlin.math.max

class TreeView(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {

    private var widthCount = 0
    var adapter: TreeAdapter? = null

    var maxDeep: Int = 0

    /**
     * 遍历tree数据，创建所有的节点view，同时求出tree的最大深度，为每个node的deep(深度)赋值
     */
    private fun obtainAllView(node: TreeNode?, deep: Int) {
        if (node == null) {
            maxDeep = max(deep, maxDeep)
            return
        }
        node.children?.forEach { treeNode ->
            obtainAllView(treeNode, deep +1)

        }
        if (node.children == null) {
            widthCount++   // 末节点 占一个单位宽度
            maxDeep = max(deep, maxDeep)
        }
        val childView = adapter?.getView(node, this)
        node.view = childView
        node.deep = deep
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthCount = 0
        obtainAllView(adapter?.treeNode, 1)
        childWidth = measuredWidth / widthCount - 2.px()
        childHeight = measuredHeight / maxDeep
        currentX = 0
        measureAllView(adapter?.treeNode!!)
        addAllViews(adapter?.treeNode!!)
        for (child in children) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(childHeight-5.px(),MeasureSpec.EXACTLY))
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec))
    }




    var childWidth = 0
    var childHeight = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutAllView(adapter?.treeNode!!)
    }

    private fun layoutAllView(treeNode: TreeNode) {

        if (treeNode.children == null) {
            treeNode.view?.layout(treeNode.x+2.px(),treeNode.y,(treeNode.x + childWidth),(treeNode.y+childHeight-5.px()))

            return
        }

        for ((_,childNode) in(treeNode.children!!.withIndex())){
            layoutAllView(childNode)
        }

        treeNode.view?.layout(treeNode.x,treeNode.y,(treeNode.x + childWidth),(treeNode.y+childHeight-5.px()))
    }



    private fun addAllViews(treeNode: TreeNode) {
        if (treeNode.children == null) {
            addView(treeNode.view)
            return
        }

        for ((_,childNode) in(treeNode.children!!.withIndex())){
            addAllViews(childNode)
        }

        addView(treeNode.view)

    }

    var currentX = 0
    private fun measureAllView(treeNode: TreeNode): Int {

        var leftX = 0
        var rightX = 0
        if (treeNode.children == null) {
            currentX += childWidth
            treeNode.x = (currentX - childWidth)
            treeNode.y = ((treeNode.deep.minus(1)).times(childHeight))
            Log.d("measureAllView","${treeNode.name1}-- ${treeNode.x} -- ${treeNode.y}")
            return currentX - childWidth
        }

        for ((index,childNode) in(treeNode.children!!.withIndex())){
            val x = measureAllView(childNode)
            if (index == 0) {
                leftX = x + childWidth
            }
            if (index == (treeNode.children?.size?.minus(1))) {
                rightX = x
            }
        }
        val nodeX = leftX + (rightX - leftX) / 2  //父节点的x坐标 取其最左和最右两个子node的中点
        treeNode.x = (nodeX - (childWidth / 2))
        treeNode.y = ((treeNode.deep.minus(1)).times(childHeight))
        Log.d("measureAllView","${treeNode.name1}-- ${treeNode.x} -- ${treeNode.y}")
        return (nodeX - (childWidth / 2))
    }

}