/**
 * Class for the RecyclerView adapter for the import fragment. This is for the platform
 * list.
 */

package com.example.agendaapp.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.R;
import com.example.agendaapp.Utils.ImageTransformations.CircleCropTransform;
import com.squareup.picasso.Picasso;

public class ImportRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Platform[] platforms;

    /**
     * ViewHolder class for a platform
     */
    private class PlatformViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivPlatformIcon;
        private TextView tvName;

        private LinearLayout llSignedIn;
        private ImageView ivAccount;
        private View signin;
        private ImageView signOut;

        private PlatformViewHolder(View itemView) {
            super(itemView);

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
            signin.setOnClickListener(view -> {
                Platform platform = platforms[getBindingAdapterPosition()];

                platform.onClickSignIn();
            });

            signOut.setOnClickListener(view -> {
                platforms[getBindingAdapterPosition()].onClickSignOut();

                llSignedIn.setVisibility(View.GONE);
                signin.setVisibility(View.VISIBLE);
            });
        }

        /**
         * Replaces the default sign in button with a custom one
         * @param newView The view to replace the original with
         */
        private void replaceSignInButton(View newView) {
            View btn = (View) itemView.findViewById(R.id.import_btn_sign_in);
            ViewGroup parent = (ViewGroup) btn.getParent();

            int index = parent.indexOfChild(btn);

            parent.removeView(btn);
            parent.addView(newView, index);

            signin = newView;

            initListeners();
        }

        /**
         * Makes the profile picture and signed out icon visible, hides the sign in button
         */
        private void setSignedInUI() {
            signin.setVisibility(View.GONE);
            llSignedIn.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(platforms[getBindingAdapterPosition()].getAccountIconURL())
                    .transform(new CircleCropTransform())
                    .into(ivAccount);
        }
    }

    /**
     * Construct with all the platforms
     * @param platforms The platforms to put in the recycler view
     */
    public ImportRecyclerAdapter(Platform... platforms) {
        this.platforms = platforms;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View platformHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_import, parent, false);
        return new PlatformViewHolder(platformHolder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlatformViewHolder platformHolder = (PlatformViewHolder) holder;

        Platform platform = platforms[position];
        
        if(platform.getSignInButton() != null)
            platformHolder.replaceSignInButton(platform.getSignInButton());

        platformHolder.ivPlatformIcon.setImageDrawable(platform.getPlatformIcon());
        platformHolder.tvName.setText(platform.getPlatformName());

        platform.addListener(platformHolder::setSignedInUI);
    }

    @Override
    public int getItemCount() {
        return platforms.length;
    }
}
