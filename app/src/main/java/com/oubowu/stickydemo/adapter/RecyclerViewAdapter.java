package com.oubowu.stickydemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oubowu.stickydemo.callback.OnItemClickListener;
import com.oubowu.stickydemo.entitiy.StickyHeadEntity;
import com.oubowu.stickydemo.holder.RecyclerViewHolder;
import com.oubowu.stickyitemdecoration.FullSpanUtil;

import java.util.List;

/**
 * Created by Oubowu on 2016/7/21 17:40.
 * <p/>
 * 适配器
 */
public abstract class RecyclerViewAdapter<T, V extends StickyHeadEntity<T>> extends RecyclerView.Adapter<RecyclerViewHolder> {

    public final static int TYPE_DATA = 1;
    public final static int TYPE_STICKY_HEAD = 2;
    public final static int TYPE_SMALL_STICKY_HEAD_WITH_DATA = 3;

    protected List<V> mData;

    protected OnItemClickListener<T> mItemClickListener;

    public RecyclerViewAdapter(List<V> data) {
        mData = data;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, TYPE_STICKY_HEAD);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder holder) {
        FullSpanUtil.onViewAttachedToWindow(holder, this, TYPE_STICKY_HEAD);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final RecyclerViewHolder holder = new RecyclerViewHolder(parent.getContext(),
                LayoutInflater.from(parent.getContext()).inflate(getItemLayoutId(viewType), parent, false));
        if (mItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = holder.getLayoutPosition();
                    mItemClickListener.onItemClick(view, getData().get(position).getData(), position);
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        bindData(holder, getItemViewType(position), position, mData.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getItemType();
    }

    public abstract int getItemLayoutId(int viewType);

    public abstract void bindData(RecyclerViewHolder holder, int viewType, int position, T item);

    public void add(int pos, V item) {
        mData.add(pos, item);
        notifyItemInserted(pos);
    }

    public void delete(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void addMoreData(List<V> data) {
        int startPos = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(startPos, data.size());
    }

    public List<V> getData() {
        return mData;
    }

    public void setData(List<V> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener<T> itemClickListener) {
        mItemClickListener = itemClickListener;
    }
}
