package com.example.agendaapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.agendaapp.Utils.DateInfo;
import com.example.agendaapp.Utils.DatePickerFragment;
import com.example.agendaapp.Utils.Resize;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditFragment extends Fragment {

    Context context;

    AppBarLayout appBarLayout;
    Toolbar toolbar;
    LinearLayout linearLayout;
    ScrollView scrollView;
    ConstraintLayout constraintLayout;
    LinearLayout llDescription;
    TextInputLayout tiTitle;
    TextInputLayout tiDescription;
    TextInputEditText etTitle;
    TextInputEditText etDescription;
    TextView tvDueDate;
    ImageButton ibDate;
    Spinner sSubjects;

    MenuItem star;

    DateInfo currentDateInfo;
    Resize resize;

    String title;
    String dueDate;
    String description;

    String subject;

    int originalPosition;

    int descriptionMinHeight;
    int originalContentHeight;

    boolean priority;
    boolean pressedPriority;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.edit_toolbar);
        toolbar.setTitle(getString(R.string.edit_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        setHasOptionsMenu(true);

        init(view);

        initLayout();

        initListeners();

        return view;
    }

    private void init(View view) {
        context = getContext();

        appBarLayout = (AppBarLayout) view.findViewById(R.id.edit_app_bar_layout);
        linearLayout = (LinearLayout) view.findViewById(R.id.edit_linear_layout);
        scrollView = (ScrollView) view.findViewById(R.id.edit_scroll_view);
        constraintLayout = (ConstraintLayout) view.findViewById(R.id.edit_constraint_layout);
        llDescription = (LinearLayout) view.findViewById(R.id.edit_ll_description);
        tiTitle = (TextInputLayout) view.findViewById(R.id.edit_ti_title);
        tiDescription = (TextInputLayout) view.findViewById(R.id.edit_ti_description);
        etTitle = (TextInputEditText) view.findViewById(R.id.edit_et_title);
        etDescription = (TextInputEditText) view.findViewById(R.id.edit_et_description);
        tvDueDate = (TextView) view.findViewById(R.id.edit_tv_due_date);
        ibDate = (ImageButton) view.findViewById(R.id.edit_ib_date);
        sSubjects = (Spinner) view.findViewById(R.id.edit_s_subject);

        currentDateInfo = new DateInfo(getArguments().getString(Utility.EDIT_DUE_DATE_KEY),
                getArguments().getInt(Utility.EDIT_DAY_KEY), getArguments().getInt(Utility.EDIT_MONTH_KEY),
                getArguments().getInt(Utility.EDIT_YEAR_KEY));

        resize = new Resize(getActivity());

        title = getArguments().getString(Utility.EDIT_TITLE_KEY);
        dueDate = getArguments().getString(Utility.EDIT_DUE_DATE_KEY);
        description = getArguments().getString(Utility.EDIT_DESCRIPTION_KEY);
        subject = getArguments().getString(Utility.EDIT_SUBJECT_KEY);
        originalPosition = getArguments().getInt(Utility.EDIT_ORIGINAL_POSITION_KEY);

        descriptionMinHeight = 0;
        originalContentHeight = resize.getContentHeight();

        priority = getArguments().getBoolean(Utility.EDIT_PRIORITY_KEY);
        pressedPriority = false;
    }

    private void initLayout() {
        etTitle.setFocusable(true);
        etDescription.setFocusable(true);
        sSubjects.setFocusable(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subject_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSubjects.setAdapter(adapter);

        etTitle.setText(title);
        tvDueDate.setText(dueDate);
        etDescription.setText(description);
        sSubjects.setSelection(Utility.getSubjectPositionFromTitle(subject, context), true);

        llDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int padding = (int) (8 * getActivity().getResources().getDisplayMetrics().density);

                etDescription.setMinHeight(llDescription.getHeight() - padding * 3);

                descriptionMinHeight = llDescription.getHeight() - padding * 3;

                llDescription.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT));

                ConstraintSet set = new ConstraintSet();
                set.clone(constraintLayout);
                set.connect(R.id.edit_ll_description, ConstraintSet.TOP, R.id.edit_constraint_middle, ConstraintSet.BOTTOM);
                set.connect(R.id.edit_ll_description, ConstraintSet.BOTTOM, R.id.edit_scroll_view, ConstraintSet.BOTTOM);
                set.connect(R.id.edit_ll_description, ConstraintSet.START, R.id.edit_scroll_view, ConstraintSet.START);
                set.connect(R.id.edit_ll_description, ConstraintSet.END, R.id.edit_scroll_view, ConstraintSet.END);
                set.applyTo(constraintLayout);

                llDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initListeners() {
        resize.addListener((Resize.ResizeListener) (fromHeight, toHeight, contentView) -> {
            if(toHeight == originalContentHeight) {
                etDescription.setHeight((int) ((etDescription.getLineCount() * (etDescription.getLineHeight() + etDescription.getLineSpacingExtra())
                        * etDescription.getLineSpacingMultiplier()) + 0.5) + etDescription.getCompoundPaddingTop()
                        + etDescription.getCompoundPaddingBottom());

                etDescription.setMinHeight(descriptionMinHeight);

                tiTitle.clearFocus();
                tiDescription.clearFocus();
            } else {
                etDescription.setHeight(toHeight - (int)(toolbar.getHeight() + llDescription.getTop() + tiDescription.getPaddingTop() +
                        tiDescription.getPaddingBottom() + tiDescription.getPaddingBottom()));
            }
        });

        ibDate.setOnClickListener(view -> {
            DatePickerFragment fragment = new DatePickerFragment(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    currentDateInfo = Utility.getLocalDateFormat(getActivity(), day, month + 1, year);
                    tvDueDate.setText(currentDateInfo.getDate());

                    if(!pressedPriority) {
                        priority = Utility.compareDates(Utility.getDay(getActivity(), 2), currentDateInfo) == Utility.FURTHER;
                        toggleStar();
                    }

                    star.setVisible(!Utility.inPriorityRange(currentDateInfo, context));
                }
            });

            fragment.setDateInfo(currentDateInfo);
            fragment.show(getParentFragmentManager(), "Date Picker");
        });

        sSubjects.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Utility.hideSoftKeyboard(getActivity());

                etTitle.clearFocus();
                etDescription.clearFocus();

                return false;
            }
        });

    }

    private void save() {
        Bundle bundle = new Bundle();
        bundle.putString(Utility.EDIT_BUNDLE_TITLE_KEY,
                !etTitle.getText().toString().equals("") ? etTitle.getText().toString() : getString(R.string.untitled));
        bundle.putString(Utility.EDIT_BUNDLE_DUE_DATE_KEY, tvDueDate.getText().toString());
        bundle.putString(Utility.EDIT_BUNDLE_SUBJECT_KEY, Utility.getSubjectFromPosition(sSubjects.getSelectedItemPosition(), context));
        bundle.putString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY, etDescription.getText().toString());
        bundle.putInt(Utility.EDIT_BUNDLE_DAY_KEY, currentDateInfo.getDay());
        bundle.putInt(Utility.EDIT_BUNDLE_MONTH_KEY, currentDateInfo.getMonth());
        bundle.putInt(Utility.EDIT_BUNDLE_YEAR_KEY, currentDateInfo.getYear());
        bundle.putBoolean(Utility.EDIT_BUNDLE_PRIORITY_KEY, priority);
        bundle.putInt(Utility.EDIT_BUNDLE_POSITION_KEY, originalPosition);
        bundle.putBoolean(Utility.EDIT_BUNDLE_CREATE_NEW_KEY, false);

        getParentFragmentManager().setFragmentResult(Utility.EDIT_RESULT_KEY, bundle);
    }

    public void toggleStar() {
        if(priority) {
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        } else {
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit, menu);

        if(priority) {
            menu.getItem(0).setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        } else {
            menu.getItem(0).setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));
        }

        star = menu.getItem(0);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                getParentFragmentManager().popBackStack();
                return true;
            case R.id.edit_save :
                save();
                getParentFragmentManager().popBackStack();
                return true;
            case R.id.edit_star :
                pressedPriority = true;

                if(priority) {
                    item.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
                } else {
                    item.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));
                }

                ((AnimatedVectorDrawableCompat) item.getIcon()).start();

                priority = !priority;

                return true;
        }

        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(Utility.inPriorityRange(currentDateInfo, context)) {
            menu.getItem(0).setVisible(false);
        }
    }

    public static EditFragment newInstance(String title, String dueDate, String description, String subject,
                                           DateInfo dateInfo, int originalPosition, boolean priority) {

        Bundle bundle = new Bundle();
        bundle.putString(Utility.EDIT_TITLE_KEY, title);
        bundle.putString(Utility.EDIT_DUE_DATE_KEY, dueDate);
        bundle.putString(Utility.EDIT_SUBJECT_KEY, subject);
        bundle.putString(Utility.EDIT_DESCRIPTION_KEY, description);
        bundle.putInt(Utility.EDIT_DAY_KEY, dateInfo.getDay());
        bundle.putInt(Utility.EDIT_MONTH_KEY, dateInfo.getMonth());
        bundle.putInt(Utility.EDIT_YEAR_KEY, dateInfo.getYear());
        bundle.putInt(Utility.EDIT_ORIGINAL_POSITION_KEY, originalPosition);
        bundle.putBoolean(Utility.EDIT_PRIORITY_KEY, priority);

        EditFragment editFragment = new EditFragment();
        editFragment.setArguments(bundle);

        return editFragment;
    }
}
