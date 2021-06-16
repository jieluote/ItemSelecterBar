package com.jieluote.itemselectorbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.OnScrollChangeListener
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.get

/**
 * item选择器(支持横向纵向布局,支持字符串,数组,集合格式数据)
 * 适用于选择年龄、城市、日期等文本场景
 * author: jieluote
 */
class ItemSelectorBar : LinearLayout {
    val TAG = ItemSelectorBar::class.java.simpleName
    lateinit var selectedListener: OnSelectedListener

    interface OnSelectedListener {
        fun onSelectedItem(position: Int, view: View)
    }

    fun setOnSelectedListener(listener: OnSelectedListener) {
        this.selectedListener = listener
    }

    private var mContext: Context? = null
    private var mOrientation: Orientation? = null
    private var mIsShowScrollbars: Boolean = false
    private var mItemWidth: Int = 0
    private var mItemHeight: Int = 0
    private var mItemVerPadding: Int = 0   //垂直padding
    private var mItemHorPadding: Int = 0   //水平padding
    private var mTotalSize: Int = 0        //总大小(坐标)
    private var mNoSelectItemTextSize: Float = 16F
    private var mNoSelectItemTextColor: Int = R.color.text_color_select_item
    private var mSelectItemTextSize: Float = 20F
    private var mSelectItemTextColor: Int = R.color.text_color_no_select_item
    private var mItemBgDrawable: Int? = null

    private var scrollbarThumbHorizontal: Drawable? = null
    private var scrollbarTrackHorizontal: Drawable? = null
    private var scrollbarThumbVertical: Drawable? = null
    private var scrollbarTrackVertical: Drawable? = null

    private val NO_SELECT_ITEM_DEFAULT_TEXT_SIZE: Int = 16
    private val SELECT_ITEM_DEFAULT_TEXT_SIZE: Int = 20
    private val ITEM_DEFAULT_WIDTH: Int = LinearLayout.LayoutParams.WRAP_CONTENT
    private val ITEM_DEFAULT_HEIGHT: Int = LinearLayout.LayoutParams.WRAP_CONTENT

    private var mContentStr: String? = null
    private var mContentArrays: Array<String>? = null
    private var mContentList: List<Any>? = null
    private var mDataList: ArrayList<Any>? = null

    private lateinit var mVerticalScrollView: ScrollView
    private lateinit var mHorizontalScrollView: HorizontalScrollView
    private lateinit var mContentLy: LinearLayout
    private var mCustomTextView: TextView? = null

    private var mTempSelectedIndex: Int = 0
    private val SELECT_ITEM_SCALE = 0.5f  //选中的item占 滚动条的位置比例(0到1),比如0.5就是滚动条1/2处(中间)显示选中的item

    fun setOrientation(orientation: Orientation?): ItemSelectorBar {
        mOrientation = orientation
        return this
    }

    fun setNoSelectItemTextSize(textSize: Float): ItemSelectorBar {
        mNoSelectItemTextSize = textSize
        return this
    }

    fun setNoSelectItemTextColor(textColor: Int): ItemSelectorBar {
        mNoSelectItemTextColor = textColor
        return this
    }

    fun setSelectItemTextSize(textSize: Float): ItemSelectorBar {
        mSelectItemTextSize = textSize
        return this
    }

    fun setSelectItemTextColor(textColor: Int): ItemSelectorBar {
        mSelectItemTextColor = textColor
        return this
    }

    fun setItemBgDrawable(itemBgDrawable : Int):ItemSelectorBar{
        mItemBgDrawable = itemBgDrawable
        return this
    }

    fun setContentText(text: String?): ItemSelectorBar {
        mContentStr = text
        return this
    }

    fun setContentText(text: Array<String>?): ItemSelectorBar {
        mContentArrays = text
        return this
    }

    fun setContentText(text: List<Any>?): ItemSelectorBar {
        mContentList = text
        return this
    }

    fun setShowScrollbars(isShow: Boolean): ItemSelectorBar {
        mIsShowScrollbars = isShow
        return this
    }

    fun setItemWidth(itemWidth: Int): ItemSelectorBar {
        mItemWidth = itemWidth
        return this
    }

    fun setItemHeight(itemHeight: Int): ItemSelectorBar {
        mItemHeight = itemHeight
        return this
    }

    fun setItemVerPadding(itemVerPadding: Int): ItemSelectorBar {
        mItemVerPadding = itemVerPadding
        return this
    }

    fun setItemHorPadding(itemHorPadding: Int): ItemSelectorBar {
        mItemHorPadding = itemHorPadding
        return this
    }

    fun setThumbHorizontal(Thumb: Drawable): ItemSelectorBar {
        scrollbarThumbHorizontal = Thumb
        return this
    }

    fun setTrackHorizontal(Thumb: Drawable): ItemSelectorBar {
        scrollbarTrackHorizontal = Thumb
        return this
    }

    fun setThumbVertical(Thumb: Drawable): ItemSelectorBar {
        scrollbarThumbVertical = Thumb
        return this
    }

    fun setTrackVertical(Thumb: Drawable): ItemSelectorBar {
        scrollbarTrackVertical = Thumb
        return this
    }

    fun setCustomTextView(textView: TextView): ItemSelectorBar {
        mCustomTextView = textView
        return this
    }

    init {
        Log.d(TAG, "init {0}")
    }

    constructor(context: Context) : super(context) {
        Log.d(TAG, "init {1}")
        mContext = context
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        Log.d(TAG, "init {2}")
        mContext = context
        init(ctx, attrs)
    }

    constructor(ctx: Context?, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr) {
        Log.d(TAG, "init {3}")
        mContext = context
        init(ctx, attrs)
    }

    private fun init(ctx: Context?, attrs: AttributeSet) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.selector_bar_attrs)
        var value = typedArray.getString(R.styleable.selector_bar_attrs_orientation).toString()
        var orientation: String = if (value == "0") {
            Orientation.vertical.name
        } else {
            Orientation.horizontal.name
        }
        mOrientation = Orientation.valueOf(orientation)
        mIsShowScrollbars = typedArray.getBoolean(R.styleable.selector_bar_attrs_isShowScrollbars, false)

        mNoSelectItemTextSize = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_noSelectItemTextSize, NO_SELECT_ITEM_DEFAULT_TEXT_SIZE).toFloat()
        mNoSelectItemTextColor = typedArray.getResourceId(R.styleable.selector_bar_attrs_noSelectItemTextColor, R.color.text_color_no_select_item)
        mSelectItemTextSize = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_selectItemTextSize, SELECT_ITEM_DEFAULT_TEXT_SIZE).toFloat()
        mSelectItemTextColor = typedArray.getResourceId(R.styleable.selector_bar_attrs_selectItemTextColor, R.color.text_color_select_item)
        mItemBgDrawable = typedArray.getResourceId(R.styleable.selector_bar_attrs_itemBgDrawable, -1)
        mItemWidth = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_itemWidth, ITEM_DEFAULT_WIDTH)
        mItemHeight = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_itemHeight, ITEM_DEFAULT_HEIGHT)
        mItemHorPadding = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_itemHorPadding, 0)
        mItemVerPadding = typedArray.getDimensionPixelSize(R.styleable.selector_bar_attrs_itemVerPadding, 0)

        scrollbarThumbHorizontal = typedArray.getDrawable(R.styleable.selector_bar_attrs_scrollbarThumbHorizontal)
        scrollbarTrackHorizontal = typedArray.getDrawable(R.styleable.selector_bar_attrs_scrollbarTrackHorizontal)
        scrollbarThumbVertical = typedArray.getDrawable(R.styleable.selector_bar_attrs_scrollbarThumbVertical)
        scrollbarTrackVertical = typedArray.getDrawable(R.styleable.selector_bar_attrs_scrollbarTrackVertical)
        var text = typedArray.getString(R.styleable.selector_bar_attrs_contentText)
        if (text != null) {
            setContentText(text)
        }
        typedArray.recycle()
        initView()
    }

    private fun initView() {
        val inflate = inflate(this.context, R.layout.item_selector_bar_layout, this)
        initScrollView(inflate)
    }

    private fun initScrollView(rootView: View) {
        mVerticalScrollView = rootView.findViewById<ScrollView>(R.id.vertical_scroll_view)
        mHorizontalScrollView = rootView.findViewById<HorizontalScrollView>(R.id.horizontal_scroll_view)
    }

    fun create() {
        create(-1)
    }

    fun create(defaultIndex: Int) {
        applyUIStyle()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            applyData()
        }
        if (defaultIndex != -1) {
            scrollToIndex(defaultIndex)
        } else {
            //默认移动到中间位置item
            var index = Math.ceil(((mDataList?.size ?: 2) / 2.0))
            scrollToIndex(index.toInt())
        }
    }

    private fun applyUIStyle() {
        mVerticalScrollView.setVerticalScrollBarEnabled(mIsShowScrollbars);
        mHorizontalScrollView.setHorizontalScrollBarEnabled(mIsShowScrollbars);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            mHorizontalScrollView.horizontalScrollbarTrackDrawable = scrollbarTrackHorizontal
            mHorizontalScrollView.horizontalScrollbarThumbDrawable = scrollbarThumbHorizontal
            mVerticalScrollView.verticalScrollbarTrackDrawable = scrollbarTrackVertical
            mVerticalScrollView.verticalScrollbarThumbDrawable = scrollbarThumbVertical
        }
    }

    private fun applyData() {
        mTotalSize = 0
        if (mContentList != null) {
            if (mContentList is ArrayList<Any>?) {
                mDataList = mContentList as ArrayList<Any>?
            } else {
                mDataList = arrayListOf()
                for (item in mContentList!!) {
                    mDataList?.add(item.toString())
                }
            }
            Log.d(TAG, "applyData original list:" + mDataList)
        } else if (mContentStr != null) {
            mDataList = arrayListOf()
            for (item in mContentStr!!.toList()) {
                mDataList!!.add(item.toString())
            }
            Log.d(TAG, "applyData str to list:" + mDataList)
        } else if (mContentArrays != null) {
            mDataList = mContentArrays!!.toMutableList() as ArrayList<Any>
            Log.d(TAG, "applyData arrays to list:" + mDataList)
        } else if (mDataList == null) {
            Log.e(TAG, "data is not valid")
            return
        }
        Log.d(TAG, "applyData size:" + (mDataList?.size ?: 0))
        mContentLy = LinearLayout(mContext)
        val lp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        lp.gravity = Gravity.CENTER
        mContentLy.layoutParams = lp
        Log.d(TAG, "mOrientation:" + mOrientation)
        if (isHorizontal()) {
            mContentLy.orientation = LinearLayout.HORIZONTAL
            bindHorizontalScrollView(mDataList)
        } else {
            mContentLy.orientation = LinearLayout.VERTICAL
            bindVerticalScrollView(mDataList)
        }
    }

    fun notifyData() {
        Log.d(TAG, "notifyData size:" + (mDataList?.size ?: 0))
        if (isHorizontal()) {
            mContentLy.orientation = LinearLayout.HORIZONTAL
            bindHorizontalScrollView(mDataList)
        } else {
            mContentLy.orientation = LinearLayout.VERTICAL
            bindVerticalScrollView(mDataList)
        }
    }

    /**
     * 滚动到指定下标 index从0开始(需要延时,否则不生效)
     */
    private fun scrollToIndex(index: Int) {
        Log.d(TAG, "scrollToIndex index:" + index)
        Handler(Looper.getMainLooper()).postDelayed({
            if (mDataList?.size ?: 0 <= index) {
                return@postDelayed
            }
            if (mOrientation == Orientation.horizontal) {
                val distance = getScrollDistanceByIndex(index, mHorizontalScrollView.width)
                Log.d(TAG, "scrollToIndex x:" + distance)
                mHorizontalScrollView.scrollTo(distance, 0)
            } else if (mOrientation == Orientation.vertical) {
                val distance = getScrollDistanceByIndex(index, mVerticalScrollView.height)
                Log.d(TAG, "scrollToIndex y:" + y)
                mVerticalScrollView.scrollTo(0, distance)
            }
        }, 600)
    }

    /**
     * 根据下标获取滚动距离
     */
    fun getScrollDistanceByIndex(index: Int, parentSize: Int): Int {
        val childCount = mDataList?.size ?: 0
        if (childCount == 0) {
            return 0
        }
        //当前item自身的size(如果是横向那么就是宽度,如果是纵向那么就是高度)
        var itemSize = 0
        if (isHorizontal()) {
            itemSize = mContentLy.get(index).width
        } else {
            itemSize = mContentLy.get(index).height
        }
        //当前item的总size,也可以理解为它的坐标加上自身size
        val itemTotalSize = index.toFloat() / childCount * mTotalSize
        //将item移到中间所需要的距离
        var distance =
            itemTotalSize - ((parentSize - itemSize).toFloat() * SELECT_ITEM_SCALE + itemSize)
        Log.d(TAG,"item width:" + itemSize + ",itemTotalSize:" + itemTotalSize + ",parentSize:" + parentSize + ",distance:" + distance
        )
        return distance.toInt()
    }

    /**
     * 根据滚动距离获取被选中Item的下标
     */
    fun getSelectedIndexByScrollDistance(distance: Int, parentSize: Int): Int {
        //当前scale位置size(宽度或者高度)
        val currentScaleSize = parentSize * SELECT_ITEM_SCALE + distance
        val childCount: Int = mDataList?.size ?: 0
        return (currentScaleSize / mTotalSize * childCount).toInt()
    }

    private fun bindHorizontalScrollView(list: ArrayList<Any>?) {
        Log.d(TAG, "bindHor list:" + list)
        if (list == null) {
            return
        }
        mContentLy.removeAllViews()
        mHorizontalScrollView.removeAllViews()

        for (itemStr in list) {
            Log.d(TAG, "initScrollView itemStr:" + itemStr)
            val defaultItemTextView = getDefaultItemTextView(itemStr.toString())
            mContentLy.addView(defaultItemTextView)
        }
        mHorizontalScrollView.setOnScrollChangeListener(OnScrollChangeListener { view, scrollX, scrollY, i2, i3 ->
            // 得到选中的item的下标
            val currentSelectedIndex = getSelectedIndexByScrollDistance(scrollX, view.width)
            if (currentSelectedIndex == mTempSelectedIndex) {
                return@OnScrollChangeListener
            }
            Log.d(TAG, "currentSelectedIndex:" + currentSelectedIndex)
            mTempSelectedIndex = currentSelectedIndex
            for (index in 0 until mContentLy.childCount) {
                val tv = mContentLy.getChildAt(index) as TextView
                if (index == currentSelectedIndex) {
                    if (this@ItemSelectorBar::selectedListener.isInitialized) {
                        selectedListener.onSelectedItem(index, tv)
                    }
                    tv.textSize = mSelectItemTextSize
                    tv.setTextColor(resources.getColor(mSelectItemTextColor))
                } else {
                    tv.textSize = mNoSelectItemTextSize
                    tv.setTextColor(resources.getColor(mNoSelectItemTextColor))
                }
            }
        })
        mHorizontalScrollView.addView(mContentLy)
    }

    private fun bindVerticalScrollView(list: List<Any>?) {
        Log.d(TAG, "bindVer list:" + list)
        if (list == null) {
            return
        }
        mContentLy.removeAllViews()
        mVerticalScrollView.removeAllViews()

        for (itemStr in list) {
            Log.d(TAG, "initScrollView itemStr:" + itemStr)
            mContentLy.addView(getDefaultItemTextView(itemStr.toString()))
        }
        mVerticalScrollView.setOnScrollChangeListener(OnScrollChangeListener { view, scrollX, scrollY, i2, i3 ->
            // 得到选中的item的下标
            val currentSelectedIndex = getSelectedIndexByScrollDistance(scrollY, view.height)
            if (currentSelectedIndex == mTempSelectedIndex) {
                return@OnScrollChangeListener
            }
            Log.d(TAG, "currentSelectedIndex:" + currentSelectedIndex)
            mTempSelectedIndex = currentSelectedIndex
            for (index in 0 until mContentLy.childCount) {
                val tv = mContentLy.getChildAt(index) as TextView
                if (index == currentSelectedIndex) {
                    if (this@ItemSelectorBar::selectedListener.isInitialized) {
                        selectedListener.onSelectedItem(index, tv)
                    }
                    tv.textSize = mSelectItemTextSize
                    tv.setTextColor(resources.getColor(mSelectItemTextColor))
                } else {
                    tv.textSize = mNoSelectItemTextSize
                    tv.setTextColor(resources.getColor(mNoSelectItemTextColor))
                }
            }
        })
        mVerticalScrollView.addView(mContentLy)
    }

    private fun getDefaultItemTextView(str: String): TextView {
        if (mCustomTextView != null) {
            mNoSelectItemTextSize = mCustomTextView!!.textSize
            mNoSelectItemTextColor = mCustomTextView!!.currentTextColor
            return mCustomTextView as TextView
        }
        val tv = TextView(context)
        val lp = LayoutParams(
            mItemWidth,
            mItemHeight
        )
        Log.d(TAG,"mItemWidth:" + mItemWidth + ",mItemHeight:" + mItemHeight + ",mItemHorPadding:" + mItemHorPadding + ",mItemVerPadding:" + mItemVerPadding)
        tv.setPadding(mItemHorPadding, mItemVerPadding, mItemHorPadding, mItemVerPadding)
        tv.layoutParams = lp
        tv.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(view: View?,p1: Int,p2: Int,p3: Int,p4: Int,p5: Int,p6: Int,p7: Int,p8: Int) {
                if (isHorizontal()) {
                    mTotalSize += view?.width ?: 0
                } else {
                    mTotalSize += view?.height ?: 0
                }
                tv.removeOnLayoutChangeListener(this)
            }
        })
        context?.resources?.let {
            if (mItemBgDrawable != null && mItemBgDrawable != -1) tv.setBackground(
                it.getDrawable(mItemBgDrawable!!)
            )
        }
        tv.setSingleLine()
        tv.gravity = Gravity.CENTER
        tv.maxLines = 1
        tv.text = str
        tv.textSize = mNoSelectItemTextSize
        tv.setTextColor(resources.getColor(mNoSelectItemTextColor))
        return tv
    }

    enum class Orientation(i: String) {
        horizontal("0"), vertical("1")
    }

    private fun isHorizontal(): Boolean {
        return mOrientation == Orientation.horizontal
    }
}