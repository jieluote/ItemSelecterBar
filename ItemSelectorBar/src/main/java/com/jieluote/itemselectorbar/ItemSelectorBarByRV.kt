package com.jieluote.itemselectorbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * item选择器(基于RecycleView实现)
 * 支持横向纵向布局,支持字符串,数组,集合格式数据
 * 适用于选择年龄、城市、星座等文本场景
 * 注意：如果不指定尺寸,那么只支持等长文本的显示
 * author: jieluote
 */
class ItemSelectorBarByRV : LinearLayout {
    val TAG = ItemSelectorBarByRV::class.java.simpleName
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

    private var mItemWidth: Int = ITEM_DEFAULT_WIDTH
    private var mItemHeight: Int = ITEM_DEFAULT_HEIGHT

    private var mIsSetWidth: Boolean = false
    private var mIsSetHeight: Boolean = false

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

    private var mContentStr: String? = null
    private var mContentArrays: Array<String>? = null
    private var mContentList: List<Any>? = null
    private var mDataList: ArrayList<Any>? = null

    private lateinit var mItemSelectorRv: RecyclerView
    private var mCustomTextView: TextView? = null
    private lateinit var mItemSelectorAdapter:ItemSelectorAdapter

    private var mTempSelectedIndex: Int = 0
    private val SELECT_ITEM_SCALE = 0.5f  //选中的item占 滚动条的位置比例(0到1),比如0.5就是滚动条1/2处(中间)显示选中的item
    private var mScrollDistance: Int = 0

    companion object{
        val NO_SELECT_ITEM_DEFAULT_TEXT_SIZE: Int = 16
        val SELECT_ITEM_DEFAULT_TEXT_SIZE: Int = 20
        val ITEM_DEFAULT_WIDTH: Int = RecyclerView.LayoutParams.WRAP_CONTENT
        val ITEM_DEFAULT_HEIGHT: Int = RecyclerView.LayoutParams.WRAP_CONTENT
    }

    fun setOrientation(orientation: Orientation?): ItemSelectorBarByRV {
        mOrientation = orientation
        return this
    }

    fun setNoSelectItemTextSize(textSize: Float): ItemSelectorBarByRV {
        mNoSelectItemTextSize = textSize
        return this
    }

    fun setNoSelectItemTextColor(textColor: Int): ItemSelectorBarByRV {
        mNoSelectItemTextColor = textColor
        return this
    }

    fun setSelectItemTextSize(textSize: Float): ItemSelectorBarByRV {
        mSelectItemTextSize = textSize
        return this
    }

    fun setSelectItemTextColor(textColor: Int): ItemSelectorBarByRV {
        mSelectItemTextColor = textColor
        return this
    }

    fun setItemBgDrawable(itemBgDrawable : Int):ItemSelectorBarByRV{
        mItemBgDrawable = itemBgDrawable
        return this
    }

    fun setContentText(text: String?): ItemSelectorBarByRV {
        mContentStr = text
        return this
    }

    fun setContentText(text: Array<String>?): ItemSelectorBarByRV {
        mContentArrays = text
        return this
    }

    fun setContentText(text: List<Any>?): ItemSelectorBarByRV {
        mContentList = text
        return this
    }

    fun setShowScrollbars(isShow: Boolean): ItemSelectorBarByRV {
        mIsShowScrollbars = isShow
        return this
    }

    fun setItemWidth(itemWidth: Int): ItemSelectorBarByRV {
        mItemWidth = itemWidth
        mIsSetWidth = true
        return this
    }

    fun setItemHeight(itemHeight: Int): ItemSelectorBarByRV {
        mItemHeight = itemHeight
        mIsSetHeight = true
        return this
    }

    fun setItemVerPadding(itemVerPadding: Int): ItemSelectorBarByRV {
        mItemVerPadding = itemVerPadding
        return this
    }

    fun setItemHorPadding(itemHorPadding: Int): ItemSelectorBarByRV {
        mItemHorPadding = itemHorPadding
        return this
    }

    fun setThumbHorizontal(Thumb: Drawable): ItemSelectorBarByRV {
        scrollbarThumbHorizontal = Thumb
        return this
    }

    fun setTrackHorizontal(Thumb: Drawable): ItemSelectorBarByRV {
        scrollbarTrackHorizontal = Thumb
        return this
    }

    fun setThumbVertical(Thumb: Drawable): ItemSelectorBarByRV {
        scrollbarThumbVertical = Thumb
        return this
    }

    fun setTrackVertical(Thumb: Drawable): ItemSelectorBarByRV {
        scrollbarTrackVertical = Thumb
        return this
    }

    fun setCustomTextView(textView: TextView): ItemSelectorBarByRV {
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
        val inflate = inflate(this.context, R.layout.item_selector_bar_by_rv_layout, this)
    }

    private fun initRecycleView(view: View?) {
        mItemSelectorRv = rootView.findViewById<RecyclerView>(R.id.item_selector_rv)
        val layoutManager = LinearLayoutManager(context)
        if(isHorizontal()){
            Log.d(TAG,"initRecycleView HORIZONTAL")
            layoutManager.orientation = RecyclerView.HORIZONTAL
        }else{
            Log.d(TAG,"initRecycleView VERTICAL")
            layoutManager.orientation = RecyclerView.VERTICAL
        }

        mItemSelectorRv.setLayoutManager(layoutManager)
        mItemSelectorAdapter = ItemSelectorAdapter()
        mItemSelectorRv.setAdapter(mItemSelectorAdapter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mItemSelectorRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    var itemSize = 0
                    var parentSize = 0
                    if (isHorizontal()) {
                        mScrollDistance += dx
                        parentSize = recyclerView.width
                        itemSize = mItemWidth
                    } else {
                        mScrollDistance += dy
                        parentSize = recyclerView.height
                        itemSize = mItemHeight
                    }

                    // 得到选中的item的下标
                    val currentSelectedIndex = getSelectedIndexByScrollDistance(mScrollDistance, itemSize, parentSize)
                    Log.d(TAG, "currentSelectedIndex:" + currentSelectedIndex + ",mScrollDistance:" + mScrollDistance)
                    if (currentSelectedIndex == mTempSelectedIndex) {
                        return
                    }
                    mTempSelectedIndex = currentSelectedIndex
                    for (index in 0 until mItemSelectorAdapter.getItemCount()) {
                        var tv = recyclerView.layoutManager?.findViewByPosition(index)
                        if (tv == null) {
                            Log.d(TAG, "item is null,index:" + index)
                            continue
                        }
                        tv = tv as TextView
                        Log.d(TAG, "tv:" + tv)
                        if (index == currentSelectedIndex) {
                            if (this@ItemSelectorBarByRV::selectedListener.isInitialized) {
                                selectedListener.onSelectedItem(index, tv)
                            }
                            setSeletedUI(tv)
                        } else {
                            setUnSeletedUI(tv)
                        }
                    }
                }
            })
        }
    }

    fun create() {
        create(-1)
    }

    fun create(defaultIndex: Int) {
        initRecycleView(this)
        applyUIStyle()
        applyData()
        notifyData()
        if (defaultIndex != -1) {
            scrollToIndex(defaultIndex)
        } else {
            //默认移动到中间位置item
            val index = Math.ceil(((mDataList?.size ?: 0) / 2.0))
            scrollToIndex(index.toInt())
        }
    }

    private fun applyUIStyle() {
        mItemSelectorRv.setVerticalScrollBarEnabled(mIsShowScrollbars);
        mItemSelectorRv.setHorizontalScrollBarEnabled(mIsShowScrollbars);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            mItemSelectorRv.horizontalScrollbarTrackDrawable = scrollbarTrackHorizontal
            mItemSelectorRv.horizontalScrollbarThumbDrawable = scrollbarThumbHorizontal
            mItemSelectorRv.verticalScrollbarTrackDrawable = scrollbarTrackVertical
            mItemSelectorRv.verticalScrollbarThumbDrawable = scrollbarThumbVertical
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
    }

    fun notifyData() {
        Log.d(TAG, "notifyData size:" + (mDataList?.size ?: 0))
        mItemSelectorAdapter.notifyDataSetChanged()
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
            if (isHorizontal()) {
                val distance = getScrollDistanceByIndex(index, mItemSelectorRv.width)
                Log.d(TAG, "scrollToIndex x:" + distance)
                mItemSelectorRv.scrollBy(distance, 0)
            } else {
                val distance = getScrollDistanceByIndex(index, mItemSelectorRv.height)
                Log.d(TAG, "scrollToIndex y:" + y)
                mItemSelectorRv.scrollBy(0, distance)
            }
        }, 600)
    }

    /**
     * 根据下标获取滚动距离
     */
    private fun getScrollDistanceByIndex(index: Int, parentSize: Int): Int {
        val childCount = mDataList?.size ?: 0
        if (childCount == 0) {
            return 0
        }

        //当前item自身的size(如果是横向那么就是宽度,如果是纵向那么就是高度)
        var itemSize = 0
        val item = mItemSelectorRv.layoutManager?.findViewByPosition(index)
        if (isHorizontal()) {
            itemSize = if (item == null) mItemWidth else item.width
        } else {
            itemSize = if (item == null) mItemHeight else item.height
        }
        //当前item的总size
        val itemTotalSize = index.toFloat() / childCount * mTotalSize

        //将item移到中间所需要的距离
        var distance =
                itemTotalSize - ((parentSize - itemSize).toFloat() * SELECT_ITEM_SCALE + itemSize)
        Log.d(TAG, "item size:" + itemSize + ",itemTotalSize:" + itemTotalSize + ",parentSize:" + parentSize + ",distance:" + distance)
        return distance.toInt()
    }

    /**
     * 根据滚动距离获取被选中Item的下标
     */
    fun getSelectedIndexByScrollDistance(distance: Int, itemSize: Int, parentSize: Int): Int {
        //当前scale位置尺寸(宽度或者高度)
        val currentScaleSize = parentSize * SELECT_ITEM_SCALE + distance
        val childCount: Int = mDataList?.size ?: 0
        val totalSize = itemSize * childCount
        mTotalSize = totalSize
        return (currentScaleSize / totalSize * childCount).toInt()
    }

    inner class ItemSelectorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemSelectorHolder(getDefaultItemTextView())
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder.itemView is TextView) {
                val content = mDataList?.get(position)
                (holder.itemView as TextView).setText(content.toString())
            }
        }

        override fun getItemCount(): Int {
            return if(mDataList == null) 0 else mDataList?.size!!
        }
    }

    internal class ItemSelectorHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        init {
        }
    }

    private fun getDefaultItemTextView(): TextView {
        if (mCustomTextView != null) {
            mNoSelectItemTextSize = mCustomTextView!!.textSize
            mNoSelectItemTextColor = mCustomTextView!!.currentTextColor
            return mCustomTextView as TextView
        }
        val tv = TextView(context)
        val lp = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,RecyclerView.LayoutParams.WRAP_CONTENT)
        if (mIsSetWidth) {
            lp.width = mItemWidth
        }
        if (mIsSetHeight) {
            lp.height = mItemHeight
        }

        tv.setPadding(mItemHorPadding, mItemVerPadding, mItemHorPadding, mItemVerPadding)
        tv.layoutParams = lp
        tv.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(view: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                if (isHorizontal()) {
                        mItemWidth = view?.width ?: 0

                } else {
                        mItemHeight = view?.height ?: 0
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
        tv.textSize = mNoSelectItemTextSize
        tv.setTextColor(resources.getColor(mNoSelectItemTextColor))
        return tv
    }

    private fun setSeletedUI(tv:TextView){
        tv.textSize = mSelectItemTextSize
        tv.setTextColor(resources.getColor(mSelectItemTextColor))
    }

    private fun setUnSeletedUI(tv:TextView){
        tv.textSize = mNoSelectItemTextSize
        tv.setTextColor(resources.getColor(mNoSelectItemTextColor))
    }

    class SpaceItemDecoration(private val vspace: Int, private val hspace: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
                outRect: Rect,
                view: View,
                recyclerView: RecyclerView,
                state: RecyclerView.State
        ) {
            outRect.top = vspace
            outRect.right = hspace
        }
    }

    enum class Orientation(i: String) {
        horizontal("0"), vertical("1")
    }

    private fun isHorizontal(): Boolean {
        return mOrientation == Orientation.horizontal
    }
}