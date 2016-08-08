package io.yeomans.echelon.callbacks;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import io.yeomans.echelon.ui.adapters.SonglistRecyclerAdapter;

/**
 * Created by jason on 8/8/16.
 */
public class SongSwipeTouchHelperCallback extends ItemTouchHelper.Callback {

    private final SonglistRecyclerAdapter mAdapter;

    public SongSwipeTouchHelperCallback(SonglistRecyclerAdapter adapter) {
      mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
      return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
      return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
      int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
      int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
      return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
      //mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
      return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
      //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

  }
