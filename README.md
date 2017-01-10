# RecyclerView粘性头部
一种新的解决思路，对比我之前写的[PinnedSectionItemDecoration](https://github.com/oubowu/PinnedSectionItemDecoration)有如下好处：<p>
- 粘性头部是放置在RecyclerView外面的View，对比之前绘制出来的粘性头部，能显示出点击的效果，并且处理点击事件更加简单<p>
- 不需要频繁的创建粘性头部的View用于绘制，只需要刷新外置的粘性头部的数据即可

# 效果图
![普通头部](http://ww1.sinaimg.cn/large/904ec4b1jw1fbkslqz6ygg20az0m97wh.gif)
![附着头部](http://ww4.sinaimg.cn/large/904ec4b1jw1fbluz259rig20b00m9nj3.gif)<p>

# 代码实现
StickyHeadContainer用来承载粘性头部布局，并且需要和RecyclerView的顶部对齐
```
<RelativeLayout
    android:id="@+id/content_main"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="com.oubowu.stickydemo.MainActivity"
    tools:showIn="@layout/activity_main">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_alignParentTop="true"
        android:layout_margin="10dp"
        tools:background="@color/colorAccent"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true">

    </android.support.v7.widget.RecyclerView>

    <com.oubowu.stickyitemdecoration.StickyHeadContainer
        android:id="@+id/shc"
        android:layout_width="match_parent"
        android:layout_margin="10dp"
        android:layout_height="wrap_content">

        <include
            layout="@layout/item_stock_sticky_head"/>

    </com.oubowu.stickyitemdecoration.StickyHeadContainer>

</RelativeLayout>
```

RecyclerView只需要添加一个StickyItemDecoration即可实现粘性头部，需要传入StickyHeadContainer用于头部的处理，以及粘性头部的类型；然后头部的点击处理就像平常一样处理
```
        final StickyHeadContainer container = (StickyHeadContainer) findViewById(R.id.shc);
        final TextView tvStockName = (TextView) container.findViewById(R.id.tv_stock_name);
        final CheckBox checkBox = (CheckBox) container.findViewById(R.id.checkbox);
        final ImageView more = (ImageView) container.findViewById(R.id.iv_more);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAdapter.getData().get(mStickyPosition).getData().check = isChecked;
                mAdapter.notifyItemChanged(mStickyPosition);
            }
        });
        container.setDataCallback(new StickyHeadContainer.DataCallback() {
            @Override
            public void onDataChange(int pos) {
                mStickyPosition = pos;
                StockEntity.StockInfo item = mAdapter.getData().get(pos).getData();
                tvStockName.setText(item.stickyHeadName);
                checkBox.setChecked(item.check);
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击了粘性头部的更多", Toast.LENGTH_SHORT).show();
            }
        });
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击了粘性头部：" + tvStockName.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new StickyItemDecoration(container, RecyclerViewAdapter.TYPE_STICKY_HEAD));
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(mRecyclerView.getContext()));
```
Adapter需要重写这两个方法，用于处理GridLayoutManager和StaggeredGridLayoutManager模式下的头部使之占满一行
```
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, TYPE_STICKY_HEAD);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        FullSpanUtil.onViewAttachedToWindow(holder, this, TYPE_STICKY_HEAD);
    }
```

##### PS:若是使用了下拉刷新的控件配合RecyclerView使用的话(显示刷新头部并且RecyclerView跟随头部的显示往下移动那种类型)，因为StickyHeadContainer是独立于RecyclerView存在的，不能跟随RecyclerView移动，需要根据刷新头部的显示情况设置StickyHeadContainer的可见性
