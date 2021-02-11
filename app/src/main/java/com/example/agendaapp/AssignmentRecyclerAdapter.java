package com.example.agendaapp;

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

import com.example.agendaapp.Utils.Assignment;
import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Utils.ListModerator;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class AssignmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements ItemMoveCallback.ItemTouchHelperContract {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ASSIGNMENT = 1;
    private final static int TYPE_SPACER = 2;

    private Context context;

    private RecyclerView recyclerView;

    //p : priority | u : upcoming

    private ListModerator<Assignment> moderator;
    private ArrayList<Assignment> priority;
    private ArrayList<Assignment> upcoming;

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeader;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            tvHeader = (TextView) itemView.findViewById(R.id.tv_row_header);
        }
    }

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

        private void initListeners() {
            cardView.setOnClickListener(view -> {
                ViewFragment viewFragment;

                FragmentTransaction transaction = MainActivity.homeFragment.getParentFragmentManager().beginTransaction();

                int position = getAdapterPosition();

                if(position <= priority.size())
                    viewFragment = ViewFragment.newInstance(priority.get(moderator.getArrayPosFromOverall(position)), position, true);
                else
                    viewFragment = ViewFragment.newInstance(upcoming.get(moderator.getArrayPosFromOverall(position)), position, false);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ViewCompat.setTransitionName(cardView, context.getString(R.string.transition_background) + position);

                    transaction.setReorderingAllowed(true);
                    transaction.addSharedElement(cardView, cardView.getTransitionName());
                }

                transaction.replace(R.id.fragment_container, viewFragment);
                transaction.addToBackStack(Utility.VIEW_FRAGMENT);
                transaction.commit();
            });

            ivDone.setOnClickListener(view -> {
                int position = getAdapterPosition();

                removeItem(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, moderator.getItemCount() + moderator.lists());
            });
        }

        private void removeItem(int position) {
            moderator.removeOverall(position);

            Utility.serializeArrays(context, priority, upcoming);
        }
    }

    class SpacerViewHolder extends RecyclerView.ViewHolder {

        private View spacer;

        public SpacerViewHolder(View itemView) {
            super(itemView);

            spacer = (View) itemView.findViewById(R.id.spacer);
        }
    }

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

            switch(position) {
                case 0 :
                    headerHolder.tvHeader.setText(context.getString(R.string.priority));

                    if(priority.size() == 0)
                        headerHolder.tvHeader.append(" " + context.getString(R.string.none));

                    break;
                default :
                    headerHolder.tvHeader.setText(context.getString(R.string.upcoming_assignments));

                    if(upcoming.size() == 0)
                        headerHolder.tvHeader.append(" " + context.getString(R.string.none));

                    break;
            }
        } else if(holder instanceof AssignmentViewHolder) {
            AssignmentViewHolder assignmentHolder = (AssignmentViewHolder) holder;
            ViewCompat.setTransitionName(assignmentHolder.tvTitle, context.getString(R.string.transition_title) + assignmentHolder.getAdapterPosition());

            Assignment assignment = position <= priority.size() ? priority.get(moderator.getArrayPosFromOverall(position)) :
                    upcoming.get(moderator.getArrayPosFromOverall(position));

            assignmentHolder.tvTitle.setText(assignment.getTitle());
            assignmentHolder.tvDueDate.setText(assignment.getDateInfo().getDate());
            assignmentHolder.tvDescription.setText(assignment.getDescription());
            assignmentHolder.ivType.setImageResource(Utility.getSubjectDrawable(context, assignment.getSubject()));

            if(position <= priority.size() && Utility.isLate(context, priority.get(moderator.getArrayPosFromOverall(position)).getDateInfo())) {
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
    public int getItemCount() {
        return moderator.getItemCount() + 2; // added two extra for spacer
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == priority.size() + 1) {
            return TYPE_HEADER;
        } else if(position == getItemCount() - 1) {
            return TYPE_SPACER;
        } else {
            return TYPE_ASSIGNMENT;
        }
    }

    @Override
    public void onRowMoved(AssignmentViewHolder holder, int fromPosition, int toPosition) {
        boolean priorityContainedItems = priority.size() > 0;
        boolean upcomingContainedItems = upcoming.size() > 0;

        if(fromPosition < toPosition) {
            for(int i = fromPosition; i < toPosition; i++) {
                if(toPosition != priority.size() + 1)
                    moderator.swap(i, i + 1);
                else
                    Toast.makeText(context, context.getString(R.string.already_in_range_toast), Toast.LENGTH_SHORT).show();
            }
        } else {
            for(int i = fromPosition; i > toPosition; i--)
                moderator.swap(i, i - 1);
        }

        notifyItemMoved(fromPosition, toPosition);

        if(priorityContainedItems != priority.size() > 0)
            notifyItemChanged(0);

        if(upcomingContainedItems != upcoming.size() > 0)
            notifyItemChanged(priority.size() + 1);
    }

    @Override
    public void onRowSelected(AssignmentViewHolder holder) {
        holder.cardView.setSelected(true);
    }

    @Override
    public void onRowClear(AssignmentViewHolder holder) {
        holder.cardView.setSelected(false);
    }

    public void setArrays(ArrayList<Assignment> priority, ArrayList<Assignment> upcoming) {
        this.priority = priority;
        this.upcoming = upcoming;
    }

    private void setColor(String subject, Drawable drawable) {
        Drawable unwrappedDrawable = drawable;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Utility.getSubjectColor(context, subject));
    }
}
