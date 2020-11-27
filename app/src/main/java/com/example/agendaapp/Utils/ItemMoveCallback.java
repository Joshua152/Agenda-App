package com.example.agendaapp.Utils;

import android.graphics.Canvas;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.AssignmentRecyclerAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {
    private ItemTouchHelperContract adapter;

    public ItemMoveCallback(ItemTouchHelperContract adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        int flags = holder instanceof AssignmentRecyclerAdapter.AssignmentViewHolder ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;

        return makeMovementFlags(flags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder holder, RecyclerView.ViewHolder target) {
        adapter.onRowMoved((AssignmentRecyclerAdapter.AssignmentViewHolder) holder,
                holder.getAdapterPosition(), target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder holder, int actionState) {
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if(holder instanceof AssignmentRecyclerAdapter.AssignmentViewHolder) {
                AssignmentRecyclerAdapter.AssignmentViewHolder assignmentHolder =
                        (AssignmentRecyclerAdapter.AssignmentViewHolder) holder;

                adapter.onRowSelected(assignmentHolder);
            }
        }

        super.onSelectedChanged(holder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        super.clearView(recyclerView, holder);

        if(holder instanceof AssignmentRecyclerAdapter.AssignmentViewHolder) {
            AssignmentRecyclerAdapter.AssignmentViewHolder assignmentHolder =
                    (AssignmentRecyclerAdapter.AssignmentViewHolder) holder;

            adapter.onRowClear(assignmentHolder);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        float top = viewHolder.itemView.getTop() + dY;
        float bottom = top + viewHolder.itemView.getHeight();

        if(top < 0) {
            dY  = -viewHolder.itemView.getTop();
        } else if(bottom > recyclerView.getHeight()) {
            dY = recyclerView.getHeight() - viewHolder.itemView.getHeight() - viewHolder.itemView.getTop();
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder holder, int direction) {}

    public interface ItemTouchHelperContract {
        void onRowMoved(AssignmentRecyclerAdapter.AssignmentViewHolder holder, int fromPosition, int toPosition);
        void onRowSelected(AssignmentRecyclerAdapter.AssignmentViewHolder holder);
        void onRowClear(AssignmentRecyclerAdapter.AssignmentViewHolder holder);
    }
}