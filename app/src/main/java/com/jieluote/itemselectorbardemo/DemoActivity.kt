package com.jieluote.itemselectorbardemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jieluote.itemselectorbar.ItemSelectorBar
import kotlinx.android.synthetic.main.activity_main.*

/**
 * author: jieluote
 */
class DemoActivity : AppCompatActivity() {
    private val TAG  = DemoActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /***
            两种使用方式,分别是
            1.使用代码(优先级高,如果同时定义了xml和代码,以代码为准)
            2.直接在XML中定义
        ***/
        testByCode()
        //testByXML()
    }

    fun testByCode(){
        /***代码方式,通过链式调用setXXX方法实现,最后需要调用create方法***/
        //支持的数据格式共4种,分别是 1.字符串 2.数组 3.list(不可变集合) 4.mutableList(可变集合)
        var str = "--甲乙丙丁戊己庚辛壬癸--"
        val arrays = arrayOf("","","","1","2","3","4","5","6","7","8","9","10","11","12","","","")
        val list: List<String> = listOf("请选择城市","深圳", "北京","广州","呼和浩特","乌鲁木齐","上海","请选择城市")
        val mutableList: MutableList<Any> = mutableListOf("","","","",1990,1991,1992,1993,1994,1995,1996,"","","")
        item_selector_bar
            .setOrientation(ItemSelectorBar.Orientation.horizontal)             //方向
            .setShowScrollbars(false)                                           //是否显示滚动条
            .setNoSelectItemTextSize(12F)                                       //未选中文本大小
            .setNoSelectItemTextColor(R.color.text_color_no_select_item)        //未选中文本颜色
            .setSelectItemTextSize(18F)                                         //选中文本大小
            .setSelectItemTextColor(R.color.text_color_select_item)             //选中文本颜色
            //.setItemBgDrawable(R.drawable.item_bg)                            //item背景
            .setItemWidth(resources.getDimensionPixelSize(R.dimen.item_width))  //宽度(如果不设置,默认为wrap)
            //.setItemWidth(ViewGroup.LayoutParams.WRAP_CONTENT)                //宽度wrap,文本长度都一样时推荐使用固定尺寸
            //.setItemHeight(resources.getDimensionPixelSize(R.dimen.item_height)) //同上
            .setItemHorPadding(5)                                               //左右padding(wrap时需要有padding,否则都挤到一起了)
            //.setItemVerPadding(5)                                             //上下padding,同上
            .setContentText(str)                                                //内容
            .create()                                                           //创建(create必须调用,其它可选)
        item_selector_bar.setOnSelectedListener(object : ItemSelectorBar.OnSelectedListener {
            override fun onSelectedItem(position: Int, view: View) {
                if(view is TextView){
                    Log.d(TAG,"text:"+view.text+",position:"+position);
                    indicator_tv.setText(view.text)
                }
            }
        })
        //mutableList.remove(1990)
        //mutableList.remove(1996)
        //item_selector_bar.notifyData() 当可变集合内容发生改变后,调用notify()进行刷新
    }

    fun testByXML(){
        /***XML方式: 所有属性在XML中定义(包括内容),代码只需要调用create方法即可。监听可视需求调用***/
        item_selector_bar.create()
        item_selector_bar.setOnSelectedListener(object : ItemSelectorBar.OnSelectedListener {
            override fun onSelectedItem(position: Int, view: View) {
                if (view is TextView) {
                    Log.d(TAG, "text:" + view.text + ",position:" + position);
                    indicator_tv.setText(view.text)
                }
            }
        })
    }
}