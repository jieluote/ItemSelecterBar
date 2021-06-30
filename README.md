Item选择器(支持横向纵向布局,支持字符串,数组,集合格式数据)<br>
适用于滚动选择年龄、城市、日期、星座等文本场景<br>
author: jieluote

### 效果展示 ###
![image](https://github.com/jieluote/ItemSelecterBar/blob/master/ItemSelectorBarDemo.gif)


### 引入方式 ###
implementation 'com.github.jieluote:ItemSelecterBar:1.1'


### 支持的数据格式 ###
支持的数据格式共4种,分别是 1.字符串 2.数组 3.list(不可变集合) 4.mutableList(可变集合)<br>
val str = "---鼠牛虎兔龙蛇马羊猴鸡狗猪---"<br>
val arrays = arrayOf("1","2","3","4","5","6","7","8","9","10","11")<br>
val list: List<String> = listOf("请选择城市","深圳", "北京","广州","呼和浩特","乌鲁木齐","上海","请选择城市")<br>
val mutableList: MutableList<Any> = mutableListOf("","","","",1990,1991,1992,1993,1994,1995,1996,"","","")<br>

### 使用方式 ###
支持代码和XML两种方式配置,如果同时设置了,以代码为准

#### 1.代码 ####
代码方式,通过链式调用setXXX方法实现,最后需要调用create方法<br>
  ```kotlin
  /***代码方式,通过链式调用setXXX方法实现,最后需要调用create方法***/
        item_selector_bar
            .setOrientation(ItemSelectorBar.Orientation.horizontal)             //方向
            .setShowScrollbars(false)                                           //是否显示滚动条
            .setThumbHorizontal(getDrawable(R.drawable.scrollview_thumb_bg)!!)  //设置滚动条Thumb背景 (Android 29及以上支持)
            .setTrackHorizontal(getDrawable(R.drawable.scrollview_track_bg)!!)  //设置滚动条Track背景 (Android 29及以上支持)
            .setNoSelectItemTextSize(12F)                                       //未选中文本大小
            .setNoSelectItemTextColor(R.color.text_color_no_select_item)        //未选中文本颜色
            .setSelectItemTextSize(18F)                                         //选中文本大小
            .setSelectItemTextColor(R.color.text_color_select_item)             //选中文本颜色
            .setItemBgDrawable(R.drawable.item_bg)                              //item背景
            .setItemWidth(resources.getDimensionPixelSize(R.dimen.item_width))  //宽度(如果不设置,默认为wrap)
            //.setItemWidth(ViewGroup.LayoutParams.WRAP_CONTENT)                //宽度wrap,文本长度都一样时推荐使用固定尺寸
            .setItemHeight(resources.getDimensionPixelSize(R.dimen.item_height))//同上
            .setItemHorPadding(5)                                               //左右padding(wrap时需要有padding,否则都挤到一起了)
            .setItemVerPadding(5)                                               //上下padding,同上
            .setContentText(str)                                                //内容,可以是上方的四种格式数据
            .create()                                                           //创建(create必须调用,其它可选)
        item_selector_bar.setOnSelectedListener(object : ItemSelectorBar.OnSelectedListener {
            override fun onSelectedItem(position: Int, view: View) {
                if(view is TextView){
                    Log.d(TAG,"text:"+view.text+",position:"+position);
                    indicator_tv.setText(view.text)
                }
            }
        })
        /*
        mutableList.remove(1990)
        mutableList.remove(1996)
        //当可变集合内容发生改变后,调用notify()进行刷新
        item_selector_bar.notifyData() */
  
  对应XMl文件：
  <!--代码方式xml-->
    <com.jieluote.itemselectorbar.ItemSelectorBar
        android:id="@+id/item_selector_bar"
        android:layout_width="250dp"
        android:layout_height="100dp"
        android:background="@drawable/item_selector_bar_bg"/>
      
  ```
  
  #### 2.XML ####
  ```kotlin
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
  对应XMl文件：
  <!--横版 XML-->
    <com.jieluote.itemselectorbar.ItemSelectorBar
        android:id="@+id/item_selector_bar"
        android:layout_width="250dp"
        android:layout_height="100dp"
        android:background="@drawable/item_selector_bar_bg"
        app:orientation="horizontal"
        app:isShowScrollbars="true"
        app:noSelectItemTextSize="@dimen/text_size_10"
        app:noSelectItemTextColor="@color/text_color_no_select_item"
        app:selectItemTextSize="@dimen/text_size_12"
        app:selectItemTextColor="@color/text_color_select_item"
        app:itemWidth="@dimen/item_width"
        app:itemHeight="@dimen/item_height"
        app:itemHorPadding="0dp"
        app:scrollbarThumbHorizontal="@drawable/scrollview_thumb_bg"
        app:scrollbarTrackHorizontal="@drawable/scrollview_track_bg"
        app:contentText="··XML横版测试··"/>

    <!--竖版 XML-->
    <com.jieluote.itemselectorbar.ItemSelectorBar
        android:id="@+id/item_selector_bar"
        android:layout_width="150dp"
        android:layout_height="300dp"
        android:background="@drawable/item_selector_bar_bg"
        app:orientation="vertical"
        app:isShowScrollbars="false"
        app:noSelectItemTextSize="@dimen/text_size_10"
        app:noSelectItemTextColor="@color/text_color_no_select_item"
        app:selectItemTextSize="@dimen/text_size_12"
        app:selectItemTextColor="@color/text_color_select_item"
        app:itemBgDrawable="@drawable/item_bg"
        app:itemWidth="@dimen/item_width"
        app:itemHeight="@dimen/item_height"
        app:itemVerPadding="2dp"
        app:scrollbarThumbVertical="@drawable/scrollview_thumb_bg"
        app:scrollbarTrackVertical="@drawable/scrollview_track_bg"
        app:contentText="··XML竖版测试··"/>
   
  ```
  ### 祝你使用愉快 ###
  
