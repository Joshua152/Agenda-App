package com.example.agendaapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Utils.DateInfo;
import com.example.agendaapp.Utils.Serialize;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    CreateFragment createFragment;

    static Context context;

    Toolbar toolbar;
    FloatingActionButton fab;

    NestedScrollView nestedScrollView;
    RecyclerView recyclerView;

    RecyclerView.LayoutManager recyclerViewLayoutManager;
    RecyclerView.Adapter recyclerViewAdapter;

    static ArrayList<DateInfo> pDateInfo;
    static ArrayList<DateInfo> uDateInfo;
    static ArrayList<String> pTitles;
    static ArrayList<String> pDueDates;
    static ArrayList<String> pDescriptions;
    static ArrayList<String> uTitles;
    static ArrayList<String> uDueDates;
    static ArrayList<String> uDescriptions;
    static ArrayList<Integer> pTypes;
    static ArrayList<Integer> uTypes;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.home_toolbar);
        toolbar.setTitle(getString(R.string.home_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        getParentFragmentManager().setFragmentResultListener(Utility.SAVE_RESULT_KEY, this,
                new ResultListener());

        init(view);

        initListeners();

        return view;
    }

    private void init(View view) {
        context = getContext();

        initArrays();

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.home_nested_scroll);
        recyclerView = (RecyclerView) view.findViewById(R.id.home_recycler_view);

        recyclerViewLayoutManager = new LinearLayoutManager(context);
        setArrayAdapter();

        createFragment = new CreateFragment();

        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initArrays() {
        ArrayList[] serialized = (ArrayList[]) Serialize.deserialize(context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);

        if(serialized != null) {
            pTitles = serialized[Utility.SERIALIZATION_P_TITLES];
            pDueDates = serialized[Utility.SERIALIZATION_P_DUE_DATE];
            pTypes = serialized[Utility.SERIALIZATION_P_SUBJECT];
            pDescriptions = serialized[Utility.SERIALIZATION_P_DESCRIPTION];
            pDateInfo = serialized[Utility.SERIALIZATION_P_DATE_INFO];
            uTitles = serialized[Utility.SERIALIZATION_U_TITLES];
            uDueDates = serialized[Utility.SERIALIZATION_U_DUE_DATE];
            uTypes = serialized[Utility.SERIALIZATION_U_SUBJECT];
            uDescriptions = serialized[Utility.SERIALIZATION_U_DESCRIPTION];
            uDateInfo = serialized[Utility.SERIALIZATION_U_DATE_INFO];
        } else {
            pDateInfo = new ArrayList<DateInfo>();
            uDateInfo = new ArrayList<DateInfo>();
            pTitles = new ArrayList<String>();
            pDueDates = new ArrayList<String>();
            pDescriptions = new ArrayList<String>();
            uTitles = new ArrayList<String>();
            uDueDates = new ArrayList<String>();
            uDescriptions = new ArrayList<String>();
            pTypes = new ArrayList<Integer>();
            uTypes = new ArrayList<Integer>();
        }
    }

    private void initListeners() {
        fab.setOnClickListener(view -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            transaction.replace(R.id.fragment_container, createFragment);
            transaction.addToBackStack(Utility.CREATE_FRAGMENT);
            transaction.commit();
        });
    }

    private int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];

        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    private void setArrayAdapter() {
        recyclerViewAdapter = new AssignmentRecyclerAdapter(context, pTitles.toArray(new String[pTitles.size()]),
                pDueDates.toArray(new String[pDueDates.size()]), pDescriptions.toArray(new String[pDescriptions.size()]),
                toIntArray(pTypes), uTitles.toArray(new String[uTitles.size()]), uDueDates.toArray(new String[uDueDates.size()]),
                uDescriptions.toArray(new String[uDescriptions.size()]), toIntArray(uTypes));
    }

    private void save(Bundle bundle) {
        String title = bundle.getString(Utility.SAVE_BUNDLE_TITLE_KEY, "Untitled");
        String subject = bundle.getString(Utility.SAVE_BUNDLE_SUBJECT_KEY, "Other");
        String description = bundle.getString(Utility.SAVE_BUNDLE_DESCRIPTION_KEY, "");
        String dueDate = bundle.getString(Utility.SAVE_BUNDLE_DUE_DATE_KEY, "");
        DateInfo dateInfo = new DateInfo(dueDate, bundle.getInt(Utility.SAVE_BUNDLE_DAY_KEY, 0),
            bundle.getInt(Utility.SAVE_BUNDLE_MONTH_KEY, 0), bundle.getInt(Utility.SAVE_BUNDLE_YEAR_KEY, 0));

        String[] sArray = getResources().getStringArray(R.array.subject_array);

        int subjectDrawable = 0;

        if(subject.equals(sArray[0])) { // Art
            subjectDrawable = R.drawable.ic_brush_black_24dp;
        } else if(subject.equals(sArray[1])) { // History
            subjectDrawable = R.drawable.ic_history_edu_black_24dp;
        } else if(subject.equals(sArray[2])) { // Language
            subjectDrawable = R.drawable.ic_language_black_24dp;
        } else if(subject.equals(sArray[3])) { // Literature
            subjectDrawable = R.drawable.ic_book_black_24dp;
        } else if(subject.equals(sArray[4])) { // Math
            subjectDrawable = R.drawable.ic_calculate_black_24dp;
        } else if(subject.equals(sArray[5])) { // Music
            subjectDrawable = R.drawable.ic_music_note_black_24dp;
        } else if(subject.equals(sArray[6])) { // Science
            subjectDrawable = R.drawable.ic_science_black_24dp;
        } else if(subject.equals(sArray[7])) { // Other
            subjectDrawable = R.drawable.ic_miscellaneous_services_black_24dp;
        }

        //format and sort

        if(compareDates(Utility.getDay(getActivity(), 2), dateInfo)) {
            addToPriority(title, dueDate, description, subjectDrawable, dateInfo);
        } else {
            addToUpcoming(title, dueDate, description, subjectDrawable, dateInfo);
        }

        serializeArrays();

        setArrayAdapter();

        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void addToPriority(String title, String dueDate, String description, int subjectDrawable, DateInfo dateInfo) {
        for(int i = 0; i < pDateInfo.size(); i++) {
            DateInfo fromArray = pDateInfo.get(i);

            if(compareDates(fromArray, dateInfo)) {
                pDateInfo.add(i, dateInfo);

                pTitles.add(i, title);
                pDueDates.add(i, dueDate);
                pDescriptions.add(i, description);
                pTypes.add(i, subjectDrawable);

                return;
            }
        }

        pDateInfo.add(dateInfo);
        pTitles.add(title);
        pDueDates.add(dueDate);
        pDescriptions.add(description);
        pTypes.add(subjectDrawable);
    }

    private void addToUpcoming(String title, String dueDate, String description, int subjectDrawable, DateInfo dateInfo) {
        for(int i = 0; i < uDateInfo.size(); i++) {
            DateInfo fromArray = uDateInfo.get(i);

            if(compareDates(fromArray, dateInfo)) {
                uDateInfo.add(i, dateInfo);

                uTitles.add(i, title);
                uDueDates.add(i, dueDate);
                uDescriptions.add(i, description);
                uTypes.add(i, subjectDrawable);

                return;
            }
        }

        uDateInfo.add(dateInfo);
        uTitles.add(title);
        uDueDates.add(dueDate);
        uDescriptions.add(description);
        uTypes.add(subjectDrawable);
    }

    // returns true if di1 is further away than di2
    private boolean compareDates(DateInfo di1, DateInfo di2) {
        if(di1.getYear() > di2.getYear()) {
            return true;
        } else if(di1.getYear() == di2.getYear()) {
            if(di1.getMonth() > di2.getMonth()) {
                return true;
            } else if(di1.getMonth() == di2.getMonth()) {
                if(di1.getDay() > di2.getDay()) {
                    return true;
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void serializeArrays() {
        ArrayList[] serialize = new ArrayList[10];
        serialize[Utility.SERIALIZATION_P_TITLES] = pTitles;
        serialize[Utility.SERIALIZATION_P_DUE_DATE] = pDueDates;
        serialize[Utility.SERIALIZATION_P_SUBJECT] = pTypes;
        serialize[Utility.SERIALIZATION_P_DESCRIPTION] = pDescriptions;
        serialize[Utility.SERIALIZATION_P_DATE_INFO] = pDateInfo;
        serialize[Utility.SERIALIZATION_U_TITLES] = uTitles;
        serialize[Utility.SERIALIZATION_U_DUE_DATE] = uDueDates;
        serialize[Utility.SERIALIZATION_U_SUBJECT] = uTypes;
        serialize[Utility.SERIALIZATION_U_DESCRIPTION] = uDescriptions;
        serialize[Utility.SERIALIZATION_U_DATE_INFO] = uDateInfo;

        Serialize.serialize(serialize, context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);
    }

    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            save(bundle);
        }
    }
}

class AssignmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static int TYPE_HEADER = 0;
    final static int TYPE_ASSIGNMENT = 1;
    final static int TYPE_SPACER = 2;

    Context context;

    //p : priority | u : upcoming

    String[] pTitles;
    String[] pDueDates;
    String[] pDescriptions;
    String[] uTitles;
    String[] uDueDates;
    String[] uDescriptions;

    int[] pTypes;
    int[] uTypes;

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvHeader;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            tvHeader = (TextView) itemView.findViewById(R.id.tv_row_header);
        }
    }

    class AssignmentViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDueDate;
        TextView tvDescription;
        ImageView ivType;
        ImageView ivDone;

        MaterialCardView cardView;

        int position;

        public AssignmentViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.assignment_tv_title);
            tvDueDate = (TextView) itemView.findViewById(R.id.assignment_tv_due_date);
            tvDescription = (TextView) itemView.findViewById(R.id.assignment_tv_description);
            ivType = (ImageView) itemView.findViewById(R.id.assignment_iv_title);
            ivDone = (ImageView) itemView.findViewById(R.id.assignment_iv_done);

            cardView = (MaterialCardView) itemView;

            position = 0;

            setListener();
        }

        private void setListener() {
            cardView.setOnClickListener(view -> System.out.println("Clicked"));

            cardView.setOnLongClickListener(view -> {
                ((MaterialCardView) view).setChecked(!((MaterialCardView) view).isChecked());

                if(ivDone.getVisibility() == View.INVISIBLE) {
                    ivDone.setVisibility(View.VISIBLE);
                } else {
                    ivDone.setVisibility(View.INVISIBLE);
                }

                return true;
            });

            ivDone.setOnClickListener(view -> {
                removeItem(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pTitles.length + uTitles.length + 2);
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
                    break;
                default :
                    headerHolder.tvHeader.setText(context.getString(R.string.upcoming_assignments));
                    break;
            }
        } else if(holder instanceof AssignmentViewHolder) {
            AssignmentViewHolder assignmentHolder = (AssignmentViewHolder) holder;
            assignmentHolder.position = position;

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
        }
    }

    @Override
    public int getItemCount() {
        return pTitles.length + uTitles.length + 3; // added one extra for spacing
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
}
