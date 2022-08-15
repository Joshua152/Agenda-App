/**
 * Class for the RecyclerView adapter for the import fragment. This is for the platform
 * list.
 */

package com.joshuaau.plantlet.RecyclerAdapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.HomeFragment;
import com.joshuaau.plantlet.ImportFragment;
import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.Connectivity;
import com.joshuaau.plantlet.Utils.ImageTransformations.CircleCropTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
        private View signIn;
        private ImageView signOut;

        private Connectivity.ConnectivityListener connectivityListener;

        private PlatformViewHolder(View itemView) {
            super(itemView);

            llRoot = (LinearLayout) itemView.findViewById(R.id.import_ll_root);

            ivPlatformIcon = (ImageView) itemView.findViewById(R.id.import_iv_platform_icon);
            tvName = (TextView) itemView.findViewById(R.id.import_tv_platform_name);

            llSignedIn = (LinearLayout) itemView.findViewById(R.id.import_ll_signed_in);
            ivAccount = (ImageView) itemView.findViewById(R.id.import_iv_account_icon);
            signIn = (View) itemView.findViewById(R.id.import_btn_sign_in);
            signOut = (ImageView) itemView.findViewById(R.id.import_iv_sign_out);

            connectivityListener = null;

            initListeners();
        }

        /**
         * Inits the listeners for the views (onClick)
         */
        public void initListeners() {
            signIn.setOnClickListener(view -> {
                Platform platform = platforms.get(getBindingAdapterPosition());
                platform.onClickSignIn();
            });

            signOut.setOnClickListener(view -> {
                platforms.get(getBindingAdapterPosition()).onClickSignOut();

                llSignedIn.setVisibility(View.INVISIBLE);
                signIn.setVisibility(View.VISIBLE);
            });

            connectivityListener = new Connectivity.ConnectivityListener() {
                @Override
                public void onAvailable(Network network) {
                    int pos = getBindingAdapterPosition();

                    // getBindingAdapterPosition() could cause issues if the network state changes before onBind
                    if(pos != -1 && platforms.get(pos).getOAuthHelper().getConfigured()) { // why is getBindingAdapterPosition returning -1
                        fragment.getActivity().runOnUiThread(() -> {
                            signIn.setEnabled(true);
                        });
                    }
                }

                @Override
                public void onLost(Network network) {
                    signIn.setEnabled(false);
                }
            };

            HomeFragment.connectivity.addListener(connectivityListener);
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

            signIn = newView;

            initListeners();
        }

        /**
         * Makes the profile picture and sign out icon visible, hides the sign in button
         */
        private void setSignedInUI() {
            signIn.setVisibility(View.INVISIBLE);
            llSignedIn.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(platforms.get(getBindingAdapterPosition()).getAccountIconURL())
                    .transform(new CircleCropTransform())
                    .into(ivAccount);

            ImportFragment.savePlatforms(context);
        }

        /**
         * Makes the profile picture and sign out icon invisible while making the sign in button visible
         */
        private void setSignedOutUI() {
            signIn.setVisibility(View.VISIBLE);
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
                } else if(!visible && tracker.getSelection().size() > 0) {
                    fragment.showContextualAppBar();

                    visible = true;
                }
            }

            @Override
            public void onItemStateChanged(Long key, boolean selected) {
                super.onItemStateChanged(key, selected);

                View view = recyclerView.getLayoutManager().findViewByPosition(Math.toIntExact(key));

                view.setActivated(selected);

                if(selected)
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary_26a));
                else
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSurface));
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

        if(platform.getSignedIn())
            platformHolder.setSignedInUI();

        if(!platform.getOAuthHelper().getConfigured())
            platformHolder.signIn.setEnabled(false);

        platformHolder.ivPlatformIcon.setImageResource(platform.getPlatformIconId());
        platformHolder.tvName.setText(platform.getPlatformName());

        platform.addSignedInListener(platformHolder::setSignedInUI);
        platform.addSignOutRequestListener(platformHolder::setSignedOutUI);

        platforms.get(position).getOAuthHelper().addConfigListener(() -> {
            fragment.getActivity().runOnUiThread(() -> {
                platformHolder.signIn.setEnabled(true);
            });
        });
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

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        for(int i = 0; i < childCount; i++) {
            PlatformViewHolder holder = (PlatformViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));

            HomeFragment.connectivity.removeListener(holder.connectivityListener);
        }
    }
}
