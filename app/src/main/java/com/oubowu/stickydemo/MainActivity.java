package com.oubowu.stickydemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oubowu.stickydemo.adapter.RecyclerViewAdapter;
import com.oubowu.stickydemo.adapter.StockAdapter;
import com.oubowu.stickydemo.callback.OnItemClickListener;
import com.oubowu.stickydemo.entitiy.StickyHeadEntity;
import com.oubowu.stickydemo.entitiy.StockEntity;
import com.oubowu.stickyitemdecoration.DividerHelper;
import com.oubowu.stickyitemdecoration.StickyHeadContainer;
import com.oubowu.stickyitemdecoration.StickyItemDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StockAdapter mAdapter;
    private int mStickyPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                initView();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return getStrFromAssets(MainActivity.this, "rasking.json");
            }

            @Override
            protected void onPostExecute(String result) {
                parseAndSetData(result);
            }

        }.execute();

    }

    private void initView() {

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

        mAdapter = new StockAdapter(null);
        mAdapter.setItemClickListener(new OnItemClickListener<StockEntity.StockInfo>() {
            @Override
            public void onItemClick(View view, StockEntity.StockInfo data, int position) {
                Toast.makeText(MainActivity.this, "点击了Item" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseAndSetData(String result) {
        Gson gson = new Gson();

        final StockEntity stockEntity = gson.fromJson(result, StockEntity.class);

        List<StockEntity.StockInfo> data = new ArrayList<>();

        data.add(new StockEntity.StockInfo(RecyclerViewAdapter.TYPE_STICKY_HEAD, "涨幅榜"));
        for (StockEntity.StockInfo info : stockEntity.increase_list) {
            info.setItemType(RecyclerViewAdapter.TYPE_DATA);
            data.add(info);
        }

        data.add(new StockEntity.StockInfo(RecyclerViewAdapter.TYPE_STICKY_HEAD, "跌幅榜"));
        for (StockEntity.StockInfo info : stockEntity.down_list) {
            info.setItemType(RecyclerViewAdapter.TYPE_DATA);
            data.add(info);
        }

        data.add(new StockEntity.StockInfo(RecyclerViewAdapter.TYPE_STICKY_HEAD, "换手率"));
        for (StockEntity.StockInfo info : stockEntity.change_list) {
            info.setItemType(RecyclerViewAdapter.TYPE_DATA);
            data.add(info);
        }

        data.add(new StockEntity.StockInfo(RecyclerViewAdapter.TYPE_STICKY_HEAD, "振幅榜"));
        for (StockEntity.StockInfo info : stockEntity.amplitude_list) {
            info.setItemType(RecyclerViewAdapter.TYPE_DATA);
            data.add(info);
        }

        List<StickyHeadEntity<StockEntity.StockInfo>> list = new ArrayList<>(data.size());
        list.add(new StickyHeadEntity<StockEntity.StockInfo>(null, StockAdapter.TYPE_HEAD, null));
        for (StockEntity.StockInfo info : data) {
            list.add(new StickyHeadEntity<>(info, info.getItemType(), info.stickyHeadName));
        }

        mAdapter.setData(list);
        mRecyclerView.setAdapter(mAdapter);

    }

    /**
     * @return Json数据（String）
     * @description 通过assets文件获取json数据，这里写的十分简单，没做循环判断。
     */
    public static String getStrFromAssets(Context context, String name) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable mDivider;

        public SpaceItemDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                DividerHelper.drawBottomAlignItem(c, mDivider, child, params);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int type = parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view));
            if (type != RecyclerViewAdapter.TYPE_DATA && type != RecyclerViewAdapter.TYPE_SMALL_STICKY_HEAD_WITH_DATA) {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //        if (id == R.id.action_delete && mAdapter.getData().size() > 1) {
        //            for (int i = 1; i < 12; i++) {
        //                mAdapter.getData().remove(1);
        //            }
        //            mAdapter.notifyDataSetChanged();
        //            return true;
        //        }

        if (id == R.id.action_jump) {
            startActivity(new Intent(this, SecondActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
