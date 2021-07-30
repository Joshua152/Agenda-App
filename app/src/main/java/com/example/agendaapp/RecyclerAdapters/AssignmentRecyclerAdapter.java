/**
 * This class is for the RecyclerAdapter. The adapter has a ViewHolder to
 * represent an assignment. There are also ViewHolders for a header (priority,
 * upcoming) and for a spacer at the bottom to make sure that the last assignment
 * is not covered up by the bottom app bar.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp.RecyclerAdapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.MainActivity;
import com.example.agendaapp.R;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Data.ListModerator;
import com.example.agendaapp.Utils.Utility;
import com.example.agendaapp.ViewFragment;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class AssignmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements ItemMoveCallback.ItemTouchHelperContract {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ASSIGNMENT = 1;
    private final static int TYPE_SPACER = 2;

    private RecyclerView recyclerView;

    // Context of the RecyclerView
    private Context context;

    // ListModerator for the ArrayLists
    private ListModerator<Assignment> moderator;
    // ArrayList of priority assignments
    private ArrayList<Assignment> priority;
    // ArrayList of upcoming assignments
    private ArrayList<Assignment> upcoming;

    /**
     * ViewHolder for the header (Priority, Upcoming)
     */
    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeader;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            tvHeader = (TextView) itemView.findViewById(R.id.tv_row_header);
        }
    }

    /**
     * ViewHolder for an assignment (public for ItemMoveCallback)
     */
    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout frameIconBackground;
        private TextView tvTitle;
        private TextView tvDueDate;
        private TextView tvDescription;
        private ImageView ivType;
        private ImageView ivDone;

        private MaterialCardView cardView;

        public AssignmentViewHolder(View itemView) {
            super(itemView);

            frameIconBackground = (FrameLayout) itemView.findViewById(R.id.assignment_frame_icon);
            tvTitle = (TextView) itemView.findViewById(R.id.assignment_tv_title);
            tvDueDate = (TextView) itemView.findViewById(R.id.assignment_tv_due_date);
            tvDescription = (TextView) itemView.findViewById(R.id.assignment_tv_description);
            ivType = (ImageView) itemView.findViewById(R.id.assignment_iv_title);
            ivDone = (ImageView) itemView.findViewById(R.id.assignment_iv_done);

            cardView = (MaterialCardView) itemView;

            initListeners();
        }

        /**
         * Initializes the listeners for the CardView and for the check mark button
         */
        private void initListeners() {
            // Don't use lambda, errors on getParentFragmentManager
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ViewFragment viewFragment;

                    FragmentTransaction transaction = MainActivity.homeFragment.getParentFragmentManager().beginTransaction();

                    int position = getBindingAdapterPosition();

                    viewFragment = ViewFragment.newInstance(moderator.getOverall(position), position, position <= priority.size());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ViewCompat.setTransitionName(cardView, Utility.TRANSITION_BACKGROUND + position);

                        transaction.setReorderingAllowed(true);
                        transaction.addSharedElement(cardView, cardView.getTransitionName());
                    }

                    transaction.replace(R.id.fragment_container, viewFragment);
                    transaction.addToBackStack(Utility.VIEW_FRAGMENT);
                    transaction.commit();
                }
            });

            ivDone.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();

                removeItem(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, moderator.getItemCount() + moderator.lists());

                if(priority.size() == 0)
                    notifyItemChanged(0);

                if(upcoming.size() == 0)
                    notifyItemChanged(priority.size() + 1);
            });
        }

        /**
         * Removes an item from the ArrayList
         * @param position The position of this assignment
         */
        private void removeItem(int position) {
            moderator.removeOverall(position);

            Utility.serializeArrays(context, priority, upcoming);
        }
    }

    /**
     * ViewHolder for a spacer (separation view)
     */
    private class SpacerViewHolder extends RecyclerView.ViewHolder {

        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Constructor for the adapter
     * @param context RecyclerView context
     * @param priority ArrayList of priority assignments
     * @param upcoming ArrayList of upcoming assignments
     */
    public AssignmentRecyclerAdapter(Context context, ArrayList<Assignment> priority, ArrayList<Assignment> upcoming) {
        this.context = context;
        this.priority = priority;
        this.upcoming = upcoming;

        moderator = new ListModerator<Assignment>(priority, upcoming);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int itemType) {
        switch(itemType) {
            case TYPE_HEADER :
                View headerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_header, parent, false);
                return new HeaderViewHolder(headerItem);
            case TYPE_ASSIGNMENT :
                View assignmentItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_assignment, parent, false);
                return new AssignmentViewHolder(assignmentItem);
            case TYPE_SPACER :
                View spacerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_spacer, parent, false);
                return new SpacerViewHolder(spacerItem);
            default :
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder ) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            if(position == 0) {
                headerHolder.tvHeader.setText(context.getString(R.string.priority));

                if(priority.size() == 0)
                    headerHolder.tvHeader.append(" " + context.getString(R.string.none));
            } else {
                headerHolder.tvHeader.setText(context.getString(R.string.upcoming_assignments));

                if(upcoming.size() == 0)
                    headerHolder.tvHeader.append(" " + context.getString(R.string.none));
            }
        } else if(holder instanceof AssignmentViewHolder) {
            AssignmentViewHolder assignmentHolder = (AssignmentViewHolder) holder;

            Assignment assignment = position <= priority.size() ? priority.get(moderator.getArrayPosFromOverall(position)) :
                    upcoming.get(moderator.getArrayPosFromOverall(position));

            assignmentHolder.tvTitle.setText(assignment.getTitle());
            assignmentHolder.tvDueDate.setText(assignment.getDateInfo().getDate());
            assignmentHolder.tvDescription.setText(assignment.getDescription());
            assignmentHolder.ivType.setImageResource(Utility.getSubjectDrawable(context, assignment.getSubject()));

            if(position <= priority.size() && DateUtils.isLate(context, priority.get(moderator.getArrayPosFromOverall(position)).getDateInfo())) {
                assignmentHolder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.late));
                assignmentHolder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.late));
                assignmentHolder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.late));
            } else {
                assignmentHolder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
                assignmentHolder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
                assignmentHolder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
            }

            setColor(assignment.getSubject(), assignmentHolder.frameIconBackground.getBackground());
        }
    }

    @Override
    public void onRowMoved(AssignmentViewHolder holder, int fromPosition, int toPosition) {
        // going down
        if(fromPosition < toPosition) {
            if(toPosition == priority.size() + 1 && DateUtils.inPriorityRange(context, priority.get(fromPosition - 1).getDateInfo())) {
                Toast.makeText(context, context.getString(R.string.already_in_range_toast), Toast.LENGTH_SHORT).show();
            } else if(toPosition < moderator.getItemCount() + 2) {
                if(fromPosition != priority.size()) { // size is always at least 1
                    moderator.swap(fromPosition, toPosition);
                } else {
                    upcoming.add(0, priority.get(fromPosition - 1));
                    priority.remove(priority.size() - 1);
                }
            } else {
                return;
            }
        } else {
            if(toPosition != 0 && fromPosition != priority.size() + 2) {
                moderator.swap(fromPosition, toPosition);
            } else if(toPosition > 0) {
                priority.add(upcoming.get(0));
                upcoming.remove(0);
            } else {
                return;
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(AssignmentViewHolder holder) {
        holder.cardView.setSelected(true);
    }

    @Override
    public void onRowClear(AssignmentViewHolder holder) {
        holder.cardView.setSelected(false);

        recyclerView.post(() -> {
            notifyItemChanged(0);
            notifyItemChanged(priority.size() + 1);
        });
    }

    @Override
    public int getItemCount() {
        return moderator.getItemCount() + 3; // added three extra for spacer and titles
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == priority.size() + 1)
            return TYPE_HEADER;
        else if(position == getItemCount() - 1)
            return TYPE_SPACER;
        else
            return TYPE_ASSIGNMENT;
    }

    /**
     * Setter method for both arrays at once
     * @param priority The new priority ArrayList
     * @param upcoming The new upcoming ArrayList
     */
    public void setArrays(ArrayList<Assignment> priority, ArrayList<Assignment> upcoming) {
        this.priority = priority;
        this.upcoming = upcoming;
    }

    /**
     * Sets the color of a drawable based on the subject
     * @param subject The subject to base the color off of
     * @param drawable The drawable to set the tint of
     */
    private void setColor(String subject, Drawable drawable) {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), Utility.getSubjectColor(context, subject));
    }
}
