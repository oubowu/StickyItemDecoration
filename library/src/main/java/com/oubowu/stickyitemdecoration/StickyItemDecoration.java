package com.oubowu.stickyitemdecoration;

import android.graphics.Canvas;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by lenovo on 2017/1/6.
 */

public class StickyItemDecoration extends RecyclerView.ItemDecoration {

    private int mStickyHeadType;
    private int mFirstVisiblePosition;
    //    private int mFirstCompletelyVisiblePosition;
    private int mStickyHeadPosition;
    private int[] mInto;

    private RecyclerView.Adapter mAdapter;

    private StickyHeadContainer mStickyHeadContainer;
    private boolean mEnableStickyHead = true;


    private OnStickyChangeListener mOnStickyChangeListener;
    public void setOnStickyChangeListener(OnStickyChangeListener onStickyChangeListener){
        this.mOnStickyChangeListener = onStickyChangeListener;
    }

    public StickyItemDecoration(StickyHeadContainer stickyHeadContainer, int stickyHeadType) {
        mStickyHeadContainer = stickyHeadContainer;
        mStickyHeadType = stickyHeadType;
    }


    // 当我们调用mRecyclerView.addItemDecoration()方法添加decoration的时候，RecyclerView在绘制的时候，去会绘制decorator，即调用该类的onDraw和onDrawOver方法，
    // 1.onDraw方法先于drawChildren
    // 2.onDrawOver在drawChildren之后，一般我们选择复写其中一个即可。
    // 3.getItemOffsets 可以通过outRect.set()为每个Item设置一定的偏移量，主要用于绘制Decorator。

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        checkCache(parent);

        if (mAdapter == null) {
            // checkCache的话RecyclerView未设置之前mAdapter为空
            return;
        }

        calculateStickyHeadPosition(parent);

        if (mEnableStickyHead /*&& mFirstCompletelyVisiblePosition > mStickyHeadPosition*/ && mFirstVisiblePosition >= mStickyHeadPosition && mStickyHeadPosition != -1) {
            View belowView = parent.findChildViewUnder(c.getWidth() / 2, mStickyHeadContainer.getChildHeight() + 0.01f);
            mStickyHeadContainer.onDataChange(mStickyHeadPosition);
            int offset;
            if (isStickyHead(parent, belowView) && belowView.getTop() > 0) {
                offset = belowView.getTop() - mStickyHeadContainer.getChildHeight();
            } else {
                offset = 0;
            }
            if (mOnStickyChangeListener!=null){
                mOnStickyChangeListener.onScrollable(offset);
            }
        } else {
            if (mOnStickyChangeListener!=null){
                mOnStickyChangeListener.onInVisible();
            }
        }

    }

    public void enableStickyHead(boolean enableStickyHead) {
        mEnableStickyHead = enableStickyHead;
        if (!mEnableStickyHead) {
            mStickyHeadContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void calculateStickyHeadPosition(RecyclerView parent) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        //        mFirstCompletelyVisiblePosition = findFirstCompletelyVisiblePosition(layoutManager);

        // 获取第一个可见的item位置
        mFirstVisiblePosition = findFirstVisiblePosition(layoutManager);

        // 获取标签的位置，
        int stickyHeadPosition = findStickyHeadPosition(mFirstVisiblePosition);
        if (stickyHeadPosition >= 0 && mStickyHeadPosition != stickyHeadPosition) {
            // 标签位置有效并且和缓存的位置不同
            mStickyHeadPosition = stickyHeadPosition;
        }
    }

    /**
     * 从传入位置递减找出标签的位置
     *
     * @param formPosition
     * @return
     */
    private int findStickyHeadPosition(int formPosition) {

        for (int position = formPosition; position >= 0; position--) {
            // 位置递减，只要查到位置是标签，立即返回此位置
            final int type = mAdapter.getItemViewType(position);
            if (isStickyHeadType(type)) {
                return position;
            }
        }

        return -1;
    }

    /**
     * 通过适配器告知类型是否为标签
     *
     * @param type
     * @return
     */
    private boolean isStickyHeadType(int type) {
        return mStickyHeadType == type;
    }

    /**
     * 找出第一个可见的Item的位置
     *
     * @param layoutManager
     * @return
     */
    private int findFirstVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int firstVisiblePosition = 0;
        if (layoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mInto = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(mInto);
            firstVisiblePosition = Integer.MAX_VALUE;
            for (int pos : mInto) {
                firstVisiblePosition = Math.min(pos, firstVisiblePosition);
            }
        }
        return firstVisiblePosition;
    }

    /**
     * 找出第一个完全可见的Item的位置
     *
     * @param layoutManager
     * @return
     */
    private int findFirstCompletelyVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int firstVisiblePosition = 0;
        if (layoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mInto = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(mInto);
            firstVisiblePosition = Integer.MAX_VALUE;
            for (int pos : mInto) {
                firstVisiblePosition = Math.min(pos, firstVisiblePosition);
            }
        }
        return firstVisiblePosition;
    }

    /**
     * 检查缓存
     *
     * @param parent
     */
    private void checkCache(final RecyclerView parent) {

        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter) {
            mAdapter = adapter;
            // 适配器为null或者不同，清空缓存
            mStickyHeadPosition = -1;

            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    reset();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    reset();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    reset();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    reset();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    reset();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    reset();
                }
            });

        }
    }

    private void reset() {
        mStickyHeadContainer.reset();
    }

    /**
     * 查找到view对应的位置从而判断出是否标签类型
     *
     * @param parent
     * @param view
     * @return
     */
    private boolean isStickyHead(RecyclerView parent, View view) {
        final int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        final int type = mAdapter.getItemViewType(position);
        return isStickyHeadType(type);
    }

}
