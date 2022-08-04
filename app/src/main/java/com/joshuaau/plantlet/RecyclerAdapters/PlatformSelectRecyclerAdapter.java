/**
 * The RecyclerView adapter for adding new platforms to the import fragment
 */

package com.joshuaau.plantlet.RecyclerAdapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.joshuaau.plantlet.Data.PlatformInfo;
import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.Resize;

import java.util.HashMap;

import timber.log.Timber;

public class PlatformSelectRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private Resize contentViewResize;

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
            numPlatforms.setSelectAllOnFocus(true);

            numPlatforms.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    try {
                        int n = Integer.parseInt(source.toString());

                        if(n < 0)
                            return "0";

                        if(n > 9)
                            return "9";

                        return null;
                    } catch(NumberFormatException e) {
                        return null;
                    }
                },
                new InputFilter.LengthFilter(1)
            });

            numPlatforms.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString();

                    if(!text.equals(""))
                        selectedPlatforms.put(platformName.getText().toString(), Integer.parseInt(text));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            contentViewResize.addListener(((Resize.ResizeListener) (fromHeight, toHeight, contentView) -> {
                if(toHeight == contentViewResize.getOriginalContentHeight()) {
                    numPlatforms.clearFocus();

                    String s = numPlatforms.getText().toString();

                    if(s.equals("")) {
                        numPlatforms.setText("0");
                    }
                }
            }));

            addPlatform.setOnClickListener((view) -> {
                int num = Integer.parseInt(numPlatforms.getText().toString());

                numPlatforms.setText((num + 1) + "");
            });

            removePlatform.setOnClickListener((view) -> {
                int num = Integer.parseInt(numPlatforms.getText().toString());

                numPlatforms.setText((num - 1) + "");
            });
        }
    }

    /**
     * Constructor, pass in context for layout inflation, and the PlatformInfos in a parameter list
     * @param context The context
     * @param activity The activity
     * @param platforms All the platforms able to be chosen
     */
    public PlatformSelectRecyclerAdapter(Context context, Activity activity, PlatformInfo... platforms) {
        this.context = context;

        contentViewResize = Resize.newInstance(activity);

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

        viewHolder.platformIcon.setImageResource(platforms[position].getPlatformIconId());
        viewHolder.platformName.setText(platforms[position].getPlatformName());

        if(selectedPlatforms.containsKey(platforms[position].getPlatformName()))
            viewHolder.numPlatforms.setText(selectedPlatforms.get(platforms[position].getPlatformName()).toString());
    }

    public void setSelectedPlatforms(HashMap<String, Integer> selectedPlatforms) {
        this.selectedPlatforms = selectedPlatforms;

//        notifyDataSetChanged();
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
