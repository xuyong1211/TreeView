package com.xuyong.treeview.group

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.hencoder.scalableimageview.dp
import com.hencoder.scalableimageview.getAvatar
import com.xuyong.treeview.R
import com.xuyong.treeview.px
import kotlin.math.max
import kotlin.math.min


class TouchableTreeView(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {

    var verticalGap = 15
    var horizontalGap = 5
    var lineWidth = 5
    var drawLine = false

    private var widthCount = 0
    var adapter: TreeAdapter? = null

    var maxDeep: Int = 0

    private var offsetX = 0f
    private var offsetY = 0f
    private var smallScale = 0f
    private var bigScale = 0f
    private val henGestureListener = TreeGestureListener()
    private val henScaleGestureListener = TreeScaleGestureListener()
    private val henFlingRunner = HenFlingRunner()
    private val gestureDetector = GestureDetectorCompat(context, henGestureListener)
    private val scaleGestureDetector = ScaleGestureDetector(context, henScaleGestureListener)
    private var big = false
    private var currentScale = 0f
        set(value) {
            field = value
            invalidate()
        }
    private val scaleAnimator = ObjectAnimator.ofFloat(this, "currentScale", smallScale, bigScale)
    private val scroller = OverScroller(context)




    init {
        val obtainStyledAttributes =
            context.obtainStyledAttributes(attributeSet, R.styleable.TreeView)
        verticalGap = obtainStyledAttributes.getInteger(R.styleable.TreeView_verticalGap, 5)
        horizontalGap = obtainStyledAttributes.getInteger(R.styleable.TreeView_horizontalGap, 15)
        lineWidth = obtainStyledAttributes.getInteger(R.styleable.TreeView_lineWidth, 1)
        drawLine = obtainStyledAttributes.getBoolean(R.styleable.TreeView_drawLine, false)
        if (drawLine) {
            setWillNotDraw(false)
        }
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
            widthCount++   // 末节点 占一个单位宽度  由此得到一个item宽度
            maxDeep = max(deep, maxDeep)  // 由此可得到一个item高度
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
            )//此处只考虑在给定布局大小内摆放所有树节点，其他测量模式同理只是确定该布局大小的依据不同
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

    override fun onDraw(canvas: Canvas) {

        val scaleFraction = (currentScale - smallScale) / (bigScale - smallScale)//缩放进度
        canvas.translate(offsetX * scaleFraction, offsetY * scaleFraction)  //用缩放进度0-1的变化改变offset值的影响，避免位置突变
        canvas.scale(currentScale, currentScale, width / 2f, height / 2f)


        if(drawLine){drawLines(adapter?.treeNode, canvas)} //绘制节点连线
        super.onDraw(canvas)// 绘制子view

    }

    /**
     * 绘制父子节点的连线
     */
    private fun drawLines(node: TreeNode?, canvas: Canvas?) {
        //遍历所有有父的节点，与其父进行连线
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
        //遍历摆放childView  其实layout时无所谓顺序
        if (treeNode.children == null) {
            layoutSpecificView(treeNode)
            return
        }

        for ((_, childNode) in (treeNode.children!!.withIndex())) {
            layoutAllView(childNode)
        }

        layoutSpecificView(treeNode)
    }

    private fun layoutSpecificView(treeNode: TreeNode) {
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
                leftX = x + childWidth  //最左子节点的右边x
            }
            if (index == (treeNode.children?.size?.minus(1))) {
                rightX = x   //最右边子节点左边x
            }
        }
        val nodeX = leftX + (rightX - leftX) / 2  //父节点的中点x坐标 取其最左和最右两个子node的中点
        treeNode.x = (nodeX - (childWidth / 2))//左上角x
        treeNode.y = ((treeNode.deep.minus(1)).times(childHeight)) //左上角y
        Log.d("measureAllView", "${treeNode.name1}-- ${treeNode.x} -- ${treeNode.y}")
        return (nodeX - (childWidth / 2))
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        smallScale = 1f
        bigScale = 2f
        currentScale = smallScale
        scaleAnimator.setFloatValues(smallScale, bigScale)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {//不同时处理两种触摸事件
            gestureDetector.onTouchEvent(event)
        }
        return true
    }

    //越界修复  超过大值取小  小于小值取大
    private fun fixOffsets() {
        offsetX = min(offsetX, (width * bigScale - width) / 2)
        offsetX = max(offsetX, -(width * bigScale - width) / 2)
        offsetY = min(offsetY, (height * bigScale - height) / 2)
        offsetY = max(offsetY, -(height * bigScale - height) / 2)
    }

    inner class TreeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            downEvent: MotionEvent?,
            currentEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (big) {
                scroller.fling(offsetX.toInt(), offsetY.toInt(), velocityX.toInt(), velocityY.toInt(),
                    (- (width * bigScale - width) / 2).toInt(),
                    ((width * bigScale - width) / 2).toInt(),
                    (- (height * bigScale - height) / 2).toInt(),
                    ((height * bigScale - height) / 2).toInt(),200,200
                )  //根据当前偏移 计算后续惯性滑动的偏移量  都是相对值
                ViewCompat.postOnAnimation(this@TouchableTreeView, henFlingRunner)
            }
            return false
        }

        override fun onScroll(
            downEvent: MotionEvent?,
            currentEvent: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (big) { //放大状态 可以滚动， 根据以上次回调的便宜距离计算当前偏移量
                offsetX -= distanceX
                offsetY -= distanceY
                fixOffsets() // 同时需要进行偏移修正
                invalidate()
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            big = !big
            if (big) {   //解决双击放大缩小，缩放点跟手问题，（双击问题和双指缩放类似，双指缩放可以看做是固定缩放比的基于某点的双击）
                offsetX = (e.x - width / 2f) * (1 - bigScale / smallScale)  //撑出去的点 矫正回来  所以取反
                offsetY = (e.y - height / 2f) * (1 - bigScale / smallScale)
                fixOffsets()
                scaleAnimator.start()// 此刻从小放大，
            } else {
                scaleAnimator.reverse()
            }
            return true
        }
    }

    inner class TreeScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            //类比双击缩放
            offsetX = (detector.focusX - width / 2f) * (1 - bigScale / smallScale)
            offsetY = (detector.focusY - height / 2f) * (1 - bigScale / smallScale)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {

        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val tempCurrentScale = currentScale * detector.scaleFactor
            if (tempCurrentScale < smallScale || tempCurrentScale > bigScale) {
                //缩放量越界的忽略，并不消费此次回调，下次回调时的初始值为此回调之前的值
                return false
            } else {
                //下次回调时的初始值为上次消费的值
                currentScale *= detector.scaleFactor // 0 1; 0 无穷
                Log.d("onScale","${currentScale}")
                if(currentScale >= (bigScale - 0.1f)){
                    big = true
                }else if(currentScale <= (smallScale +0.1f)){
                    big = false
                }
                return true
            }
        }
    }

    inner class HenFlingRunner : Runnable {
        override fun run() {
            if (scroller.computeScrollOffset()) {
                offsetX = scroller.currX.toFloat()
                offsetY = scroller.currY.toFloat()
                invalidate()
                ViewCompat.postOnAnimation(this@TouchableTreeView, this)
            }
        }
    }

}