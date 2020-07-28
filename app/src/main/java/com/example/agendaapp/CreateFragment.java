package com.example.agendaapp;

import android.graphics.drawable.Animatable;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

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

public class CreateFragment extends Fragment {

    AppBarLayout appBarLayout;
    Toolbar toolbar;
    LinearLayout linearLayout;
    ScrollView scrollView;
    ConstraintLayout constraintLayout;
    LinearLayout llDescription;
    TextInputLayout tiTitle;
    TextInputLayout tiDueDate;
    TextInputLayout tiDescription;
    TextInputEditText etTitle;
    TextInputEditText etDueDate;
    TextInputEditText etDescription;
    Spinner sSubjects;

    HomeFragment homeFragment;

    DateInfo currentDateInfo;
    Resize resize;

    int descriptionMinHeight;
    int originalContentHeight;

    boolean priority;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.create_toolbar);
        toolbar.setTitle(getString(R.string.create_title));
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
        appBarLayout = (AppBarLayout) view.findViewById(R.id.create_app_bar_layout);
        linearLayout = (LinearLayout) view.findViewById(R.id.create_linear_layout);
        scrollView = (ScrollView) view.findViewById(R.id.create_scroll_view);
        constraintLayout = (ConstraintLayout) view.findViewById(R.id.create_constraint_layout);
        llDescription = (LinearLayout) view.findViewById(R.id.create_ll_description);
        tiTitle = (TextInputLayout) view.findViewById(R.id.create_ti_title);
        tiDueDate = (TextInputLayout) view.findViewById(R.id.create_ti_due_date);
        tiDescription = (TextInputLayout) view.findViewById(R.id.create_ti_description);
        etTitle = (TextInputEditText) view.findViewById(R.id.create_et_title);
        etDueDate = (TextInputEditText) view.findViewById(R.id.create_et_due_date);
        etDescription = (TextInputEditText) view.findViewById(R.id.create_et_description);
        sSubjects = (Spinner) view.findViewById(R.id.create_s_subject);

        homeFragment = new HomeFragment();

        currentDateInfo = Utility.getDay(getActivity(), 1);
        resize = new Resize(getActivity());

        descriptionMinHeight = 0;
        originalContentHeight = resize.getContentHeight();

        priority = true;
    }

    private void initLayout() {
        etTitle.setFocusable(true);
        etDueDate.setFocusable(true);
        etDescription.setFocusable(true);
        sSubjects.setFocusable(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subject_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSubjects.setAdapter(adapter);

        etDueDate.setText(Utility.getDay(getActivity(), 1).getDate());

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
                set.connect(R.id.create_ll_description, ConstraintSet.TOP, R.id.create_ll_middle, ConstraintSet.BOTTOM);
                set.connect(R.id.create_ll_description, ConstraintSet.BOTTOM, R.id.create_scroll_view, ConstraintSet.BOTTOM);
                set.connect(R.id.create_ll_description, ConstraintSet.START, R.id.create_scroll_view, ConstraintSet.START);
                set.connect(R.id.create_ll_description, ConstraintSet.END, R.id.create_scroll_view, ConstraintSet.END);
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
                tiDueDate.clearFocus();
                tiDescription.clearFocus();
            } else {
                etDescription.setHeight(toHeight - (int)(toolbar.getHeight() + llDescription.getTop() + tiDescription.getPaddingTop() +
                        tiDescription.getPaddingBottom() + tiDescription.getPaddingBottom()));
            }
        });

        tiDueDate.setEndIconOnClickListener(view -> {
            DatePickerFragment fragment = new DatePickerFragment((datePicker, year, month, day) -> {
                currentDateInfo = Utility.getLocalDateFormat(getActivity(), day, month + 1, year);
                etDueDate.setText(currentDateInfo.getDate());

                priority = Utility.compareDates(Utility.getDay(getActivity(), 2), currentDateInfo);
            });

            fragment.show(getParentFragmentManager(), "Date Picker");
        });

        sSubjects.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Utility.hideSoftKeyboard(getActivity());

                etTitle.clearFocus();
                etDueDate.clearFocus();
                etDescription.clearFocus();

                return false;
            }
        });

    }

    private void save() {
        Bundle bundle = new Bundle();
        bundle.putString(Utility.SAVE_BUNDLE_TITLE_KEY,
                !etTitle.getText().toString().equals("") ? etTitle.getText().toString() : getString(R.string.untitled));
        bundle.putString(Utility.SAVE_BUNDLE_DUE_DATE_KEY, etDueDate.getText().toString());
        bundle.putString(Utility.SAVE_BUNDLE_SUBJECT_KEY, sSubjects.getSelectedItem().toString());
        bundle.putString(Utility.SAVE_BUNDLE_DESCRIPTION_KEY, etDescription.getText().toString());
        bundle.putInt(Utility.SAVE_BUNDLE_DAY_KEY, currentDateInfo.getDay());
        bundle.putInt(Utility.SAVE_BUNDLE_MONTH_KEY, currentDateInfo.getMonth());
        bundle.putInt(Utility.SAVE_BUNDLE_YEAR_KEY, currentDateInfo.getYear());
        bundle.putBoolean(Utility.SAVE_BUNDLE_PRIORITY_KEY, priority);
        bundle.putBoolean(Utility.SAVE_BUNDLE_CREATE_NEW_KEY, true);

        getParentFragmentManager().setFragmentResult(Utility.SAVE_RESULT_KEY, bundle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.addToBackStack(Utility.HOME_FRAGMENT);
                transaction.commit();
                return true;
            case R.id.create_save :
                save();
                FragmentTransaction saveTransaction = getParentFragmentManager().beginTransaction();
                saveTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                saveTransaction.replace(R.id.fragment_container, homeFragment);
                saveTransaction.addToBackStack(Utility.HOME_FRAGMENT);
                saveTransaction.commit();
                return true;
            case R.id.create_star :
                priority = true;
                break;
        }

        return false;
    }
}
