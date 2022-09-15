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
import android.view.ViewStructure;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.Platform;
import com.joshuaau.plantlet.HomeFragment;
import com.joshuaau.plantlet.ImportFragment;
import com.joshuaau.plantlet.OptionsFragment;
import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.Connectivity;
import com.joshuaau.plantlet.Utils.ImageTransformations.CircleCropTransform;
import com.joshuaau.plantlet.Utils.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ImportRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // TODO: MAKE SELECTION PERSIST ON SCREEN ROTATION

    private Context context;
    private ImportFragment fragment;

    private RecyclerView recyclerView;

    private SelectionTracker<Long> tracker;

    private List<Platform> platforms;

    /**
     * ViewHolder class for a platform
     */
    private class PlatformViewHolder extends RecyclerView.ViewHolder {

        private View itemView;

        private ImageView ivPlatformIcon;
        private TextView tvName;

        private FrameLayout frameLayout;
        private LinearLayout llSignedIn;
        private ImageView ivAccount;

        private LinearLayout llSignedOut;
        private View signIn;
        private View originalSignIn;
        private TextView signInWith;
        private ImageView signOut;

        private LinearLayout llOptions;

        private Connectivity.ConnectivityListener connectivityListener;

        private PlatformViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            ivPlatformIcon = (ImageView) itemView.findViewById(R.id.import_iv_platform_icon);
            tvName = (TextView) itemView.findViewById(R.id.import_tv_platform_name);

            frameLayout = (FrameLayout) itemView.findViewById(R.id.import_frame);
            llSignedIn = (LinearLayout) itemView.findViewById(R.id.import_ll_signed_in);
            ivAccount = (ImageView) itemView.findViewById(R.id.import_iv_account_icon);

            llSignedOut = (LinearLayout) itemView.findViewById(R.id.import_ll_signed_out);
            signIn = (View) itemView.findViewById(R.id.import_btn_sign_in);
            originalSignIn = signIn;
            signInWith = (TextView) itemView.findViewById(R.id.import_tv_with);
            signOut = (ImageView) itemView.findViewById(R.id.import_iv_sign_out);

            llOptions = (LinearLayout) itemView.findViewById(R.id.import_ll_options);

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

                setSignedOutUI();
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

            if(btn != null) {
                ViewGroup parent = (ViewGroup) btn.getParent();
                parent.removeView(btn);
            }

            if(llSignedOut.getChildCount() > 1)
                return;

            if(newView.getParent() != null)
                ((ViewGroup) newView.getParent()).removeView(newView);

            llSignedOut.addView(newView, llSignedOut.getChildCount());
//            llSignedOut.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT));

//            llSignedOut.setMinimumWidth(Math.max(signInWith.getWidth(), newView.getWidth()));
//            signIn.requestLayout();
//            llSignedOut.requestLayout();

            signIn = newView;

//            if(newView.equals(originalSignIn))
//                signInWith.setVisibility(View.GONE);
//            else
//                signInWith.setVisibility(View.VISIBLE);

            initListeners();
        }

        /**
         * Replaces custom button with original if necessary
         */
        private void useOriginalSignInButton() {
            if(signIn.equals(originalSignIn))
                replaceSignInButton(originalSignIn);
        }

        /**
         * Makes the profile picture and sign out icon visible, hides the sign in button
         */
        private void setSignedInUI() {
            if(getBindingAdapterPosition() == -1)
                return;

            llSignedOut.setVisibility(View.INVISIBLE);
            llSignedIn.setVisibility(View.VISIBLE);

            if(platforms.get(getBindingAdapterPosition()).hasOptions())
                llOptions.setVisibility(View.VISIBLE);

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
            signIn.setEnabled(true);

            llSignedOut.setVisibility(View.VISIBLE);
            llSignedIn.setVisibility(View.INVISIBLE);
            llOptions.setVisibility(View.GONE);
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

        recyclerView = null;

        this.platforms = platforms;

        tracker = null;

        setHasStableIds(true);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        tracker = new SelectionTracker.Builder<Long>(
                "selectionId",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new ItemDetailsLookup<Long>() {
                    @Override
                    public ItemDetails<Long> getItemDetails(MotionEvent e) {
                        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());

                        if(view != null) {
                            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);

                            if(holder instanceof ImportRecyclerAdapter.PlatformViewHolder)
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

//                View view = recyclerView.getLayoutManager().findViewByPosition(Math.toIntExact(key));

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(Math.toIntExact(key));

                if(holder == null)
                    return;

                handleSelected(holder.itemView, selected);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_import, parent, false);

        holder.setOnClickListener(view -> {
            int pos = recyclerView.getChildLayoutPosition(view);

            if(platforms.get(pos).getSignedIn()) {
                FragmentTransaction optionsTransaction = fragment.getParentFragmentManager().beginTransaction();
                optionsTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
                optionsTransaction.replace(R.id.fragment_container, OptionsFragment.newInstance(platforms.get(pos)));
                optionsTransaction.addToBackStack(Utility.HOME_FRAGMENT);
                optionsTransaction.commit();
            }
        });

        return new PlatformViewHolder(holder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlatformViewHolder platformHolder = (PlatformViewHolder) holder;

        Platform platform = platforms.get(position);

//        if(platform.getSignInButton() != null)
//            platformHolder.replaceSignInButton(platform.getSignInButton());
//        else
//            platformHolder.useOriginalSignInButton();

        if(platform.getSignedIn())
            platformHolder.setSignedInUI();
        else
            platformHolder.setSignedOutUI();

        if(!platform.getOAuthHelper().getConfigured())
            platformHolder.signIn.setEnabled(false);

        if(platform.hasOptions() && platform.getSignedIn())
            platformHolder.llOptions.setVisibility(View.VISIBLE);
        else
            platformHolder.llOptions.setVisibility(View.GONE);

        platformHolder.ivPlatformIcon.setImageResource(platform.getPlatformIconId());
        platformHolder.tvName.setText(platform.getPlatformName());

        handleSelected(holder.itemView, tracker.isSelected((long) position));

        platform.addSignedInListener(platformHolder::setSignedInUI);
        platform.addSignOutRequestListener(platformHolder::setSignedOutUI);

        platforms.get(position).getOAuthHelper().addConfigListener(() -> {
            fragment.getActivity().runOnUiThread(() -> {
                platformHolder.signIn.setEnabled(true);
            });
        });
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        handleSelected(viewHolder.itemView, tracker.isSelected((long) viewHolder.getBindingAdapterPosition()));
    }

    @Override
    public long getItemId(int position) {
//        return platforms.get(position).getID();

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

    /**
     * Handles the view UI depending on if the view is selected
     * @param view The view holder view
     * @param selected If the view is selected
     */
    private void handleSelected(View view, boolean selected) {
        if(view == null)
            return;

        view.setActivated(selected);

        if(selected)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary_26a));
        else
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSurface));
    }
}
