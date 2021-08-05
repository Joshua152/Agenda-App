/**
 * Class for the RecyclerView adapter for the import fragment. This is for the platform
 * list.
 */

package com.example.agendaapp.RecyclerAdapters;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.ImportFragment;
import com.example.agendaapp.R;
import com.example.agendaapp.Utils.ImageTransformations.CircleCropTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImportRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // TODO: MAKE SELECTION PERSIST ON SCREEN ROTATION

    private Context context;
    private ImportFragment fragment;

    private SelectionTracker<Long> tracker;

    private List<Platform> platforms;

    /**
     * ViewHolder class for a platform
     */
    private class PlatformViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llRoot;

        private ImageView ivPlatformIcon;
        private TextView tvName;

        private LinearLayout llSignedIn;
        private ImageView ivAccount;
        private View signin;
        private ImageView signOut;

        private PlatformViewHolder(View itemView) {
            super(itemView);

            llRoot = (LinearLayout) itemView.findViewById(R.id.import_ll_root);

            ivPlatformIcon = (ImageView) itemView.findViewById(R.id.import_iv_platform_icon);
            tvName = (TextView) itemView.findViewById(R.id.import_tv_platform_name);

            llSignedIn = (LinearLayout) itemView.findViewById(R.id.import_ll_signed_in);
            ivAccount = (ImageView) itemView.findViewById(R.id.import_iv_account_icon);
            signin = (View) itemView.findViewById(R.id.import_btn_sign_in);
            signOut = (ImageView) itemView.findViewById(R.id.import_iv_sign_out);

            initListeners();
        }

        /**
         * Inits the listeners for the views (onClick)
         */
        public void initListeners() {
            llRoot.setOnLongClickListener(view -> {
                llRoot.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                return false;
            });

            signin.setOnClickListener(view -> {
                Platform platform = platforms.get(getBindingAdapterPosition());

                platform.onClickSignIn();
            });

            signOut.setOnClickListener(view -> {
                platforms.get(getBindingAdapterPosition()).onClickSignOut();

                llSignedIn.setVisibility(View.INVISIBLE);
                signin.setVisibility(View.VISIBLE);
            });
        }

        /**
         * Replaces the default sign in button with a custom one
         *
         * @param newView The view to replace the original with
         */
        private void replaceSignInButton(View newView) {
            View btn = (View) itemView.findViewById(R.id.import_btn_sign_in);

            if(btn == null)
                return;

            ViewGroup parent = (ViewGroup) btn.getParent();

            int index = parent.indexOfChild(btn);

            parent.removeView(btn);

            if (newView.getParent() != null)
                ((ViewGroup) newView.getParent()).removeView(newView);

            parent.addView(newView, index);

            signin = newView;

            initListeners();
        }

        /**
         * Makes the profile picture and sign out icon visible, hides the sign in button
         */
        private void setSignedInUI() {
            signin.setVisibility(View.INVISIBLE);
            llSignedIn.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(platforms.get(getBindingAdapterPosition()).getAccountIconURL())
                    .transform(new CircleCropTransform())
                    .into(ivAccount);
        }

        /**
         * Makes the profile picture and sign out icon invisible while making the sign in button visible
         */
        private void setSignedOutUI() {
            signin.setVisibility(View.VISIBLE);
            llSignedIn.setVisibility(View.INVISIBLE);
        }

        /**
         * Item details lookup for the recycler view selection builder
         * @return Returns an instance of ItemDetails containing the item position and selection key
         */
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getBindingAdapterPosition();
                }

                @Override
                public Long getSelectionKey() {
                    return (long) getBindingAdapterPosition();
                }
            };
        }
    }

    /**
     * Gets platforms from ImportFragment
     */
    public ImportRecyclerAdapter(ImportFragment fragment, ArrayList<Platform> platforms) {
        this.context = fragment.getContext();
        this.fragment = fragment;

        this.platforms = platforms;

        tracker = null;

        setHasStableIds(true);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        tracker = new SelectionTracker.Builder<Long>(
                "selectionId",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new ItemDetailsLookup<Long>() {
                    @Override
                    public ItemDetails<Long> getItemDetails(MotionEvent e) {
                        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());

                        if (view != null) {
                            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);

                            if (holder instanceof ImportRecyclerAdapter.PlatformViewHolder)
                                return ((ImportRecyclerAdapter.PlatformViewHolder) holder).getItemDetails();
                        }

                        return null;
                    }
                },
                StorageStrategy.createLongStorage()
        )
        .withSelectionPredicate(SelectionPredicates.createSelectAnything())
        .build();

        tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            boolean visible = false;

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();

                if(visible && tracker.getSelection().size() == 0) {
                    fragment.hideContextualAppBar();

                    visible = false;
                } else if(!visible && tracker.getSelection().size() > 0){
                    fragment.showContextualAppBar();

                    visible = true;
                }
            }

            @Override
            public void onItemStateChanged(Long key, boolean selected) {
                super.onItemStateChanged(key, selected);

                View view = recyclerView.getLayoutManager().findViewByPosition(Math.toIntExact(key));

                view.setActivated(selected);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View platformHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_import, parent, false);
        return new PlatformViewHolder(platformHolder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlatformViewHolder platformHolder = (PlatformViewHolder) holder;

        Platform platform = platforms.get(position);

        if(platform.getSignInButton() != null)
            platformHolder.replaceSignInButton(platform.getSignInButton());

        if(platform.isSignedIn())
            platformHolder.setSignedInUI();

        platformHolder.ivPlatformIcon.setImageDrawable(platform.getPlatformIcon());
        platformHolder.tvName.setText(platform.getPlatformName());

        platform.addSignedInListener(platformHolder::setSignedInUI);
        platform.addSignOutRequestListener(platformHolder::setSignedOutUI);
    }

    @Override
    public long getItemId(int position) {
        return Math.toIntExact(position);
    }

    @Override
    public int getItemCount() {
        return platforms.size();
    }

    public SelectionTracker<Long> getSelectionTracker() {
        return tracker;
    }
}
