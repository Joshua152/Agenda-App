package com.example.agendaapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Utils.ItemMoveCallback;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;

public class AssignmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements ItemMoveCallback.ItemTouchHelperContract {

    final static int TYPE_HEADER = 0;
    final static int TYPE_ASSIGNMENT = 1;
    final static int TYPE_SPACER = 2;

    Context context;

    //p : priority | u : upcoming

    public String[] pTitles;
    public String[] pDueDates;
    public String[] pDescriptions;
    public String[] uTitles;
    public String[] uDueDates;
    public String[] uDescriptions;

    public int[] pTypes;
    public int[] uTypes;

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvHeader;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            tvHeader = (TextView) itemView.findViewById(R.id.tv_row_header);
        }
    }

    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameIconBackground;
        TextView tvTitle;
        TextView tvDueDate;
        TextView tvDescription;
        ImageView ivType;
        ImageView ivDone;

        MaterialCardView cardView;

        public AssignmentViewHolder(View itemView) {
            super(itemView);

            frameIconBackground = (FrameLayout) itemView.findViewById(R.id.assignment_frame_icon);
            tvTitle = (TextView) itemView.findViewById(R.id.assignment_tv_title);
            tvDueDate = (TextView) itemView.findViewById(R.id.assignment_tv_due_date);
            tvDescription = (TextView) itemView.findViewById(R.id.assignment_tv_description);
            ivType = (ImageView) itemView.findViewById(R.id.assignment_iv_title);
            ivDone = (ImageView) itemView.findViewById(R.id.assignment_iv_done);

            cardView = (MaterialCardView) itemView;

            setListener();
        }

        private void setListener() {
            cardView.setOnClickListener(view -> {
                FragmentTransaction transaction = MainActivity.homeFragment.getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);

                int position = getAdapterPosition();

                if(position <= pTitles.length) {
                    transaction.replace(R.id.fragment_container, EditFragment.newInstance(pTitles[position - 1],
                            pDueDates[position - 1], pDescriptions[position - 1], pTypes[position - 1], position));
                } else {
                    transaction.replace(R.id.fragment_container, EditFragment.newInstance(uTitles[position - pTitles.length - 2],
                            uDueDates[position - pTitles.length - 2], uDescriptions[position - pTitles.length - 2],
                            uTypes[position - pTitles.length - 2], position));
                }

                transaction.addToBackStack(Utility.EDIT_FRAGMENT);
                transaction.commit();
            });

            ivDone.setOnClickListener(view -> {
                int position = getAdapterPosition();

                removeItem(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pTitles.length + uTitles.length + 2);

                if(pTitles.length == 0) {
                    notifyItemRangeChanged(0, 1);
                }

                if(uTitles.length == 0) {
                    notifyItemRangeChanged(pTitles.length + 1, 1);
                }
            });
        }

        private void removeItem(int position) {
            if(position <= pTitles.length) {
                String[] titles = new String[pTitles.length - 1];
                String[] dueDates = new String[pDueDates.length - 1];
                String[] descriptions = new String[pDescriptions.length - 1];
                int[] types = new int[pTypes.length - 1];

                System.arraycopy(pTitles, 0, titles, 0, position - 1);
                System.arraycopy(pDueDates, 0, dueDates, 0, position - 1);
                System.arraycopy(pDescriptions, 0, descriptions, 0, position - 1);
                System.arraycopy(pTypes, 0, types, 0, position - 1);
                System.arraycopy(pTitles, position, titles, position - 1, pTitles.length - position);
                System.arraycopy(pDueDates, position, dueDates, position - 1, pDueDates.length - position);
                System.arraycopy(pDescriptions, position, descriptions, position - 1, pDescriptions.length - position);
                System.arraycopy(pTypes, position, types, position - 1, pTypes.length - position);

                pTitles = titles;
                pDueDates = dueDates;
                pDescriptions = descriptions;
                pTypes = types;

                HomeFragment.pDateInfo.remove(position - 1);
                HomeFragment.pTitles.remove(position - 1);
                HomeFragment.pDueDates.remove(position - 1);
                HomeFragment.pDescriptions.remove(position - 1);
                HomeFragment.pTypes.remove(position - 1);
            } else {
                String[] titles = new String[uTitles.length - 1];
                String[] dueDates = new String[uDueDates.length - 1];
                String[] descriptions = new String[uDescriptions.length - 1];
                int[] types = new int[uTypes.length - 1];

                System.arraycopy(uTitles, 0, titles, 0, position - pTitles.length - 2);
                System.arraycopy(uDueDates, 0, dueDates, 0, position - pTitles.length - 2);
                System.arraycopy(uDescriptions, 0, descriptions, 0, position - pTitles.length - 2);
                System.arraycopy(uTypes, 0, types, 0, position - pTitles.length - 2);
                System.arraycopy(uTitles, position - pTitles.length - 1, titles, position - pTitles.length - 2,
                        uTitles.length - (position - pTitles.length - 1));
                System.arraycopy(uDueDates, position - pTitles.length - 1, dueDates, position - pTitles.length - 2,
                        uDueDates.length - (position - pDueDates.length - 1));
                System.arraycopy(uDescriptions, position - pTitles.length - 1, descriptions, position - pTitles.length - 2,
                        uDescriptions.length - (position - pDescriptions.length - 1));
                System.arraycopy(uTypes, position - pTitles.length - 1, types, position - pTitles.length - 2,
                        uTypes.length - (position - pTypes.length - 1));

                uTitles = titles;
                uDueDates = dueDates;
                uDescriptions = descriptions;
                uTypes = types;

                HomeFragment.uDateInfo.remove(position - (pTitles.length + 2));
                HomeFragment.uTitles.remove(position - (pTitles.length + 2));
                HomeFragment.uDueDates.remove(position - (pTitles.length + 2));
                HomeFragment.uDescriptions.remove(position - (pTitles.length + 2));
                HomeFragment.uTypes.remove(position - (pTitles.length + 2));
            }

            HomeFragment.serializeArrays();
        }
    }

    class SpacerViewHolder extends RecyclerView.ViewHolder {

        View spacer;

        public SpacerViewHolder(View itemView) {
            super(itemView);

            spacer = (View) itemView.findViewById(R.id.spacer);
        }
    }

    public AssignmentRecyclerAdapter(Context context, String[] pTitles, String[] pDueDates, String[] pDescriptions, int[] pTypes,
                                     String[] uTitles, String[] uDueDates, String[] uDescriptions, int[] uTypes) {

        this.context = context;
        this.pTitles = pTitles;
        this.pDueDates = pDueDates;
        this.pDescriptions = pDescriptions;
        this.pTypes = pTypes;
        this.uTitles = uTitles;
        this.uDueDates = uDueDates;
        this.uDescriptions = uDescriptions;
        this.uTypes = uTypes;
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

                    if(pTitles.length == 0) {
                        headerHolder.tvHeader.append(" " + context.getString(R.string.none));
                    }
                    break;
                default :
                    headerHolder.tvHeader.setText(context.getString(R.string.upcoming_assignments));

                    if(uTitles.length == 0) {
                        headerHolder.tvHeader.append(" " + context.getString(R.string.none));
                    }
                    break;
            }
        } else if(holder instanceof AssignmentViewHolder) {
            AssignmentViewHolder assignmentHolder = (AssignmentViewHolder) holder;

            if(position <= pTitles.length) {
                assignmentHolder.tvTitle.setText(pTitles[position - 1]);
                assignmentHolder.tvDueDate.setText(pDueDates[position - 1]);
                assignmentHolder.tvDescription.setText(pDescriptions[position - 1]);
                assignmentHolder.ivType.setImageResource(pTypes[position - 1]);
            } else {
                assignmentHolder.tvTitle.setText(uTitles[position - pTitles.length - 2]);
                assignmentHolder.tvDueDate.setText(uDueDates[position - pTitles.length - 2]);
                assignmentHolder.tvDescription.setText(uDescriptions[position - pTitles.length - 2]);
                assignmentHolder.ivType.setImageResource(uTypes[position - pTitles.length - 2]);
            }

            setColor(assignmentHolder.frameIconBackground.getBackground(), position);
        }
    }

    @Override
    public int getItemCount() {
        return pTitles.length + uTitles.length + 3; // added one extra for spacer
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == pTitles.length + 1) {
            return TYPE_HEADER;
        } else if(position == getItemCount() - 1) {
            return TYPE_SPACER;
        } else {
            return TYPE_ASSIGNMENT;
        }
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        boolean inPriority = pTitles.length > 0;
        boolean inUpcoming = uTitles.length > 0;

        if(fromPosition < toPosition) {
            for(int i = fromPosition; i < toPosition; i++) {
                swap(i, i + 1);
            }
        } else {
            for(int i = fromPosition; i > toPosition; i--) {
                swap(i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);

        if(inPriority != pTitles.length > 0) {
            notifyItemChanged(0);
        }

        if(inUpcoming != uTitles.length > 0) {
            notifyItemChanged(pTitles.length + 1);
        }
    }

    @Override
    public void onRowSelected(AssignmentViewHolder holder) {
        holder.cardView.setSelected(true);
    }

    @Override
    public void onRowClear(AssignmentViewHolder holder) {
        holder.cardView.setSelected(false);
    }

    private void swap(int fromPosition, int toPosition) {
        if(fromPosition < toPosition && toPosition == HomeFragment.pTitles.size() + 1) {
            HomeFragment.uTitles.add(0, HomeFragment.pTitles.get(fromPosition - 1));
            HomeFragment.uDueDates.add(0, HomeFragment.pDueDates.get(fromPosition - 1));
            HomeFragment.uDescriptions.add(0, HomeFragment.pDescriptions.get(fromPosition - 1));
            HomeFragment.uTypes.add(0, HomeFragment.pTypes.get(fromPosition - 1));
            HomeFragment.uDateInfo.add(0, HomeFragment.pDateInfo.get(fromPosition - 1));

            HomeFragment.pTitles.remove(fromPosition - 1);
            HomeFragment.pDueDates.remove(fromPosition - 1);
            HomeFragment.pDescriptions.remove(fromPosition - 1);
            HomeFragment.pTypes.remove(fromPosition - 1);
            HomeFragment.pDateInfo.remove(fromPosition - 1);
        } else if(fromPosition > toPosition && toPosition == HomeFragment.pTitles.size() + 1) {
            HomeFragment.pTitles.add(HomeFragment.uTitles.get(0));
            HomeFragment.pDueDates.add(HomeFragment.uDueDates.get(0));
            HomeFragment.pDescriptions.add(HomeFragment.uDescriptions.get(0));
            HomeFragment.pTypes.add(HomeFragment.uTypes.get(0));
            HomeFragment.pDateInfo.add(HomeFragment.uDateInfo.get(0));

            HomeFragment.uTitles.remove(0);
            HomeFragment.uDueDates.remove(0);
            HomeFragment.uDescriptions.remove(0);
            HomeFragment.uTypes.remove(0);
            HomeFragment.uDateInfo.remove(0);
        } else if (toPosition <= pTitles.length && toPosition > 0 && HomeFragment.pTitles.size() > 1 ) {
            Collections.swap(HomeFragment.pTitles, fromPosition - 1, toPosition - 1);
            Collections.swap(HomeFragment.pDueDates, fromPosition - 1, toPosition - 1);
            Collections.swap(HomeFragment.pDescriptions, fromPosition - 1, toPosition - 1);
            Collections.swap(HomeFragment.pTypes, fromPosition - 1, toPosition - 1);
            Collections.swap(HomeFragment.pDateInfo, fromPosition - 1, toPosition - 1);
        } else if (toPosition < HomeFragment.pTitles.size() + HomeFragment.uTitles.size() + 2 &&
                toPosition >  HomeFragment.pTitles.size() + 2 && HomeFragment.uTitles.size() > 1) {

            Collections.swap(HomeFragment.uTitles, fromPosition - HomeFragment.pTitles.size() - 2, toPosition - HomeFragment.pTitles.size() - 2);
            Collections.swap(HomeFragment.uDueDates, fromPosition - HomeFragment.pTitles.size() - 2, toPosition - HomeFragment.pTitles.size() - 2);
            Collections.swap(HomeFragment.uDescriptions, fromPosition - HomeFragment.pTitles.size() - 2, toPosition - HomeFragment.pTitles.size() - 2);
            Collections.swap(HomeFragment.uTypes, fromPosition - HomeFragment.pTitles.size() - 2, toPosition - HomeFragment.pTitles.size() - 2);
            Collections.swap(HomeFragment.uDateInfo, fromPosition - HomeFragment.pTitles.size() - 2, toPosition - HomeFragment.pTitles.size() - 2);
        }

        pTitles = HomeFragment.pTitles.toArray(new String[HomeFragment.pTitles.size()]);
        pDueDates = HomeFragment.pDueDates.toArray(new String[HomeFragment.pDueDates.size()]);
        pDescriptions = HomeFragment.pDescriptions.toArray(new String[HomeFragment.pDescriptions.size()]);
        pTypes = Utility.toIntArray(HomeFragment.pTypes);
        uTitles = HomeFragment.uTitles.toArray(new String[HomeFragment.uTitles.size()]);
        uDueDates = HomeFragment.uDueDates.toArray(new String[HomeFragment.uDueDates.size()]);
        uDescriptions = HomeFragment.uDescriptions.toArray(new String[HomeFragment.uDescriptions.size()]);
        uTypes = Utility.toIntArray(HomeFragment.uTypes);

        HomeFragment.serializeArrays();
    }

    private void setColor(Drawable drawable, int position) {
        Drawable unwrappedDrawable = drawable;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Utility.getColor(context, getDrawableId(position)));
    }

    private int getDrawableId(int position) {
        if(position <= pTitles.length) {
            return pTypes[position - 1];
        }

        return uTypes[position - pTitles.length - 2];
    }
}
