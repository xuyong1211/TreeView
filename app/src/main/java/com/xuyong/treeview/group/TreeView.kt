package com.xuyong.treeview.group

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.children
import com.xuyong.treeview.R
import com.xuyong.treeview.px
import kotlin.math.max

class TreeView(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {

    var verticalGap = 15
    var horizontalGap = 5
    var lineWidth = 5
    var drawLine = false

    private var widthCount = 0
    var adapter: TreeAdapter? = null

    var maxDeep: Int = 0

    init {
        val obtainStyledAttributes =
            context.obtainStyledAttributes(attributeSet, R.styleable.TreeView)
        verticalGap = obtainStyledAttributes.getInteger(R.styleable.TreeView_verticalGap, 5)
        horizontalGap = obtainStyledAttributes.getInteger(R.styleable.TreeView_horizontalGap, 15)
        lineWidth = obtainStyledAttributes.getInteger(R.styleable.TreeView_lineWidth, 1)
        drawLine = obtainStyledAttributes.getBoolean(R.styleable.TreeView_drawLine, false)
        if(drawLine){ setWillNotDraw(false) }
        obtainStyledAttributes.recycle()
    }

    /**
     * 遍历tree数据，创建所有的节点view，同时求出tree的最大深度，为每个node的deep(深度)赋值
     */
    private fun obtainAllView(node: TreeNode?, deep: Int) {
        if (node == null) {
            maxDeep = max(deep, maxDeep)
            return
        }
        node.children?.forEach { treeNode ->
            obtainAllView(treeNode, deep + 1)
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
        childWidth = measuredWidth / widthCount
        childHeight = measuredHeight / maxDeep
        currentX = 0
        measureAllView(adapter?.treeNode!!)
        addAllViews(adapter?.treeNode!!)
        for (child in children) {
            child.measure(
                MeasureSpec.makeMeasureSpec(
                    childWidth - horizontalGap.px(),
                    MeasureSpec.EXACTLY
                ), MeasureSpec.makeMeasureSpec(childHeight - verticalGap.px(), MeasureSpec.EXACTLY)
            )
        }
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }


    var childWidth = 0
    var childHeight = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutAllView(adapter?.treeNode!!)
    }

    override fun onDraw(canvas: Canvas?) {
        if(drawLine){drawLines(adapter?.treeNode, canvas)}
        super.onDraw(canvas)
    }

    /**
     * 绘制父子节点的连线
     */
    private fun drawLines(node: TreeNode?, canvas: Canvas?) {
        if (node?.children != null && node.children!!.isNotEmpty()) {
            val path = Path()
            node.children!!.forEach {
                path.moveTo(
                    (node.x + ((childWidth - horizontalGap.px()) / 2)).toFloat(),
                    (node.y + childHeight - verticalGap.px()).toFloat()
                )
                path.lineTo(
                    (it.x + ((childWidth - horizontalGap.px()) / 2)).toFloat(),
                    it.y.toFloat()
                )
                canvas!!.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.BLACK
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = lineWidth.px().toFloat()
                })
                drawLines(it, canvas)
            }
        }

    }

    private fun layoutAllView(treeNode: TreeNode) {

        if (treeNode.children == null) {
            treeNode.view?.layout(
                treeNode.x + 2.px(),
                treeNode.y,
                (treeNode.x + childWidth),
                (treeNode.y + childHeight - verticalGap.px())
            )
            return
        }

        for ((_, childNode) in (treeNode.children!!.withIndex())) {
            layoutAllView(childNode)
        }

        treeNode.view?.layout(
            treeNode.x + 2.px(),
            treeNode.y,
            (treeNode.x + childWidth),
            (treeNode.y + childHeight - verticalGap.px())
        )
    }


    private fun addAllViews(treeNode: TreeNode) {
        if (treeNode.children == null) {
            addView(treeNode.view)
            return
        }

        for ((_, childNode) in (treeNode.children!!.withIndex())) {
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
            Log.d("measureAllView", "${treeNode.name1}-- ${treeNode.x} -- ${treeNode.y}")
            return currentX - childWidth
        }

        for ((index, childNode) in (treeNode.children!!.withIndex())) {
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
        Log.d("measureAllView", "${treeNode.name1}-- ${treeNode.x} -- ${treeNode.y}")
        return (nodeX - (childWidth / 2))
    }

}