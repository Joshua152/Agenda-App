/**
 * The RecyclerView adapter for adding new platforms to the import fragment
 */

package com.example.agendaapp.RecyclerAdapters;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.Data.PlatformInfo;
import com.example.agendaapp.ImportFragment;
import com.example.agendaapp.R;

import java.util.HashMap;

public class PlatformSelectRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private PlatformInfo[] platforms;

    private HashMap<String, Integer> selectedPlatforms;

    /**
     * View holder class for individual platforms. The item has buttons and a
     * text box to change the amount of specific platforms to import (ex. 2 Google Classroom
     * imports for 2 accounts)
     */
    public class PlatformSelectViewHolder extends RecyclerView.ViewHolder {

        private ImageView platformIcon;
        private TextView platformName;

        private EditText numPlatforms;
        private ImageView addPlatform;
        private ImageView removePlatform;

        private PlatformSelectViewHolder(View itemView) {
            super(itemView);

            platformIcon = (ImageView) itemView.findViewById(R.id.select_platform_icon);
            platformName = (TextView) itemView.findViewById(R.id.select_platform_name);

            numPlatforms = (EditText) itemView.findViewById(R.id.select_num_platforms);
            addPlatform = (ImageView) itemView.findViewById(R.id.select_add_platform);
            removePlatform = (ImageView) itemView.findViewById(R.id.select_remove_platform);

            initCallbacks();
        }

        /**
         * Inits callbacks (ex. onClick)
         */
        public void initCallbacks() {
            numPlatforms.setFilters(new InputFilter[]{
                    (source, start, end, dest, dstart, dend) -> {
                        int n = Integer.parseInt(source.toString());

                        if(n < 0)
                            return "0";

                        if(n > 9)
                            return "9";

                        return null;
                    }
            });

            addPlatform.setOnClickListener((view) -> {
                int num = Integer.parseInt(numPlatforms.getText().toString());

                numPlatforms.setText((num + 1) + "");

                if(num + 1 <= 9)
                    changeSelectedPlatformsMap(platformName.getText().toString(), 1);
            });

            removePlatform.setOnClickListener((view) -> {
                int num = Integer.parseInt(numPlatforms.getText().toString());

                numPlatforms.setText((num - 1) + "");

                if(num - 1 >= 0)
                    changeSelectedPlatformsMap(platformName.getText().toString(), -1);
            });
        }
    }

    /**
     * Constructor, pass in context for layout inflation, and the PlatformInfos in a parameter list
     * @param context The context
     * @param platforms All the platforms able to be chosen
     */
    public PlatformSelectRecyclerAdapter(Context context, PlatformInfo... platforms) {
        this.context = context;

        this.platforms = platforms;

        selectedPlatforms = new HashMap<String, Integer>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_platform_select, parent, false);
        return new PlatformSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlatformSelectViewHolder viewHolder = (PlatformSelectViewHolder) holder;

        viewHolder.platformIcon.setImageDrawable(platforms[position].getPlatformIcon());
        viewHolder.platformName.setText(platforms[position].getPlatformName());
    }

    /**
     * Changes the number of platforms to add with the given key (platform name)
     * @param key The platform name
     * @param change How many of that platform to add or remove
     */
    public void changeSelectedPlatformsMap(String key, int change) {
        Integer n = selectedPlatforms.get(key);

        if(n == null)
            n = 0;

        selectedPlatforms.put(key, n + change);
    }

    @Override
    public int getItemCount() {
        return platforms.length;
    }

    /**
     * Gets the chosen platforms and the number of each of them in the form of a HashMap
     * @return Returns the map of selected platforms (keys may or may not contain 0s)
     */
    public HashMap<String, Integer> getSelectedPlatforms() {
        return selectedPlatforms;
    }
}
