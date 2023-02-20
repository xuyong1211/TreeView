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


private val IMAGE_SIZE = 300.dp.toInt()
private const val EXTRA_SCALE_FACTOR = 1.5f
class TouchableTreeView(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {

    var verticalGap = 15
    var horizontalGap = 5
    var lineWidth = 5
    var drawLine = false

    private var widthCount = 0
    var adapter: TreeAdapter? = null

    var maxDeep: Int = 0


    private var originalOffsetX = 0f
    private var originalOffsetY = 0f
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


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        originalOffsetX = (width - IMAGE_SIZE) / 2f
        originalOffsetY = (height - IMAGE_SIZE) / 2f

        smallScale = 1f
        bigScale = 2f
        currentScale = smallScale
        scaleAnimator.setFloatValues(smallScale, bigScale)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            gestureDetector.onTouchEvent(event)
        }
        return true
    }

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

    override fun onDraw(canvas: Canvas) {
        val scaleFraction = (currentScale - smallScale) / (bigScale - smallScale)
//        canvas.save()
        canvas.translate(offsetX * scaleFraction, offsetY * scaleFraction)
        canvas.scale(currentScale, currentScale, width / 2f, height / 2f)
        if(drawLine){drawLines(adapter?.treeNode, canvas)}
//        canvas.restore()
        super.onDraw(canvas)

//        canvas.translate(offsetX * scaleFraction, offsetY * scaleFraction)
//        canvas.scale(currentScale, currentScale, width / 2f, height / 2f)
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
                )
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
            if (big) {
                offsetX -= distanceX
                offsetY -= distanceY
                fixOffsets()
                invalidate()
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            big = !big
            if (big) {
                offsetX = (e.x - width / 2f) * (1 - bigScale / smallScale)
                offsetY = (e.y - height / 2f) * (1 - bigScale / smallScale)
                fixOffsets()
                scaleAnimator.start()
            } else {
                scaleAnimator.reverse()
            }
            return true
        }
    }

    inner class TreeScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            offsetX = (detector.focusX - width / 2f) * (1 - bigScale / smallScale)
            offsetY = (detector.focusY - height / 2f) * (1 - bigScale / smallScale)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {

        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val tempCurrentScale = currentScale * detector.scaleFactor
            if (tempCurrentScale < smallScale || tempCurrentScale > bigScale) {
                return false
            } else {
                currentScale *= detector.scaleFactor // 0 1; 0 无穷
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