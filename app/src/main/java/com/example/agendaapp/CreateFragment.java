package com.example.agendaapp;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    Resize resize;
    HideListener hideListener;

    int descriptionMinHeight;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.create_toolbar);
        toolbar.setTitle("Create Assignment");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        sSubjects = (Spinner) view.findViewById(R.id.s_subject);

        homeFragment = new HomeFragment();

        resize = new Resize(getActivity());
        hideListener = new HideListener();

        descriptionMinHeight = 0;
    }

    private void initLayout() {
        llDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int padding = (int) (8 * getActivity().getResources().getDisplayMetrics().density);

                etDescription.setMinHeight(llDescription.getHeight() - padding * 2);

                descriptionMinHeight = llDescription.getHeight() - padding * 2;

                llDescription.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT));

                ConstraintSet set = new ConstraintSet();
                set.clone(constraintLayout);
                set.connect(R.id.create_ll_description, ConstraintSet.TOP, R.id.create_ti_due_date, ConstraintSet.BOTTOM);
                set.connect(R.id.create_ll_description, ConstraintSet.START, R.id.create_scroll_view, ConstraintSet.START);
                set.connect(R.id.create_ll_description, ConstraintSet.END, R.id.create_scroll_view, ConstraintSet.END);
                set.applyTo(constraintLayout);

                llDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initListeners() {
        toolbar.setOnTouchListener(hideListener);
        sSubjects.setOnTouchListener(hideListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subject_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSubjects.setAdapter(adapter);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point point = new Point();
        display.getSize(point);

        resize.addListener(new Resize.ResizeListener() {
            @Override
            public void onResize(int fromHeight, int toHeight, ViewGroup contentView) {
                if(fromHeight > toHeight) {
                    etDescription.setHeight(toHeight - (int)(toolbar.getHeight() + llDescription.getTop() + tiDescription.getPaddingTop() +
                            tiDescription.getPaddingBottom() + tiDescription.getPaddingBottom()));
                } else {
                    etDescription.setHeight((int) ((etDescription.getLineCount() * (etDescription.getLineHeight() + etDescription.getLineSpacingExtra())
                            * etDescription.getLineSpacingMultiplier()) + 0.5) + etDescription.getCompoundPaddingTop()
                            + etDescription.getCompoundPaddingBottom());

                    etDescription.setMinHeight(descriptionMinHeight);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.expand_in, R.animator.expand_out);
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.addToBackStack(Utility.HOME_FRAGMENT);
                transaction.commit();
                return true;
            default :
                return false;
        }
    }

    class HideListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Utility.hideSoftKeyboard(getActivity());

            return false;
        }
    }
}
