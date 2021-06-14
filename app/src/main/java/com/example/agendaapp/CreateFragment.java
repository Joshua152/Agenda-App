/**
 * Fragment for creating a new assignment. This is the same as the view fragment
 * except there are now EditTexts.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Utils.DatePickerFragment;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.Resize;
import com.example.agendaapp.Data.SaveInfo;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateFragment extends Fragment {

    private Context context;

    private Toolbar toolbar;
    private ConstraintLayout constraintLayout;
    private LinearLayout llDescription;
    private TextInputLayout tiTitle;
    private TextInputLayout tiDescription;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextView tvDueDate;
    private ImageButton ibDate;
    private Spinner sSubjects;

    private MenuItem star;

    private DateInfo currentDateInfo;
    private Resize resize;

    private int descriptionMinHeight;
    private int originalContentHeight;

    private boolean priority;
    private boolean pressedPriority;

    @Override
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

    /**
     * Inits the fields
     * @param view The inflated fragment
     */
    private void init(View view) {
        context = getContext();

        constraintLayout = (ConstraintLayout) view.findViewById(R.id.create_constraint_layout);
        llDescription = (LinearLayout) view.findViewById(R.id.create_ll_description);
        tiTitle = (TextInputLayout) view.findViewById(R.id.create_ti_title);
        tiDescription = (TextInputLayout) view.findViewById(R.id.create_ti_description);
        etTitle = (TextInputEditText) view.findViewById(R.id.create_et_title);
        etDescription = (TextInputEditText) view.findViewById(R.id.create_et_description);
        tvDueDate = (TextView) view.findViewById(R.id.create_tv_due_date);
        ibDate = (ImageButton) view.findViewById(R.id.create_ib_date);
        sSubjects = (Spinner) view.findViewById(R.id.create_s_subject);

        currentDateInfo = DateUtils.getDay(getActivity(), 1);
        resize = new Resize(getActivity());

        descriptionMinHeight = 0;
        originalContentHeight = resize.getContentHeight();

        priority = true;
        pressedPriority = false;
    }

    /**
     * Inits the layout (ex. texts, spinners)
     */
    private void initLayout() {
        etTitle.setFocusable(true);
        etDescription.setFocusable(true);
        sSubjects.setFocusable(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subject_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSubjects.setAdapter(adapter);

        tvDueDate.setText(DateUtils.getDay(getActivity(), 1).getDate());
    }

    /**
     * Inits the listeners
     */
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
                set.connect(R.id.create_ll_description, ConstraintSet.TOP, R.id.create_constraint_middle, ConstraintSet.BOTTOM);
                set.connect(R.id.create_ll_description, ConstraintSet.BOTTOM, R.id.create_scroll_view, ConstraintSet.BOTTOM);
                set.connect(R.id.create_ll_description, ConstraintSet.START, R.id.create_scroll_view, ConstraintSet.START);
                set.connect(R.id.create_ll_description, ConstraintSet.END, R.id.create_scroll_view, ConstraintSet.END);
                set.applyTo(constraintLayout);

                llDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ibDate.setOnClickListener(view -> {
            DatePickerFragment fragment = new DatePickerFragment((datePicker, year, month, day) -> {
                currentDateInfo = DateUtils.getLocalDateFormat(getActivity(), day, month + 1, year);
                tvDueDate.setText(currentDateInfo.getDate());

                if(!pressedPriority) {
                    priority = DateUtils.compareDates(DateUtils.getDay(getActivity(), 2), currentDateInfo) == DateInfo.FURTHER;
                    toggleStar();
                }

                star.setVisible(!DateUtils.inPriorityRange(context, currentDateInfo));
            });

            Utility.hideSoftKeyboard(getActivity());

            etTitle.clearFocus();
            etDescription.clearFocus();
            sSubjects.clearFocus();

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

    /**
     * Saves the data to a Bundle and sets it as a FragmentResult
     */
    private void save() {
        Bundle bundle = new Bundle();

        Assignment assignment = new Assignment();
        assignment.setTitle(!etTitle.getText().toString().equals("") ? etTitle.getText().toString() : getString(R.string.untitled));
        assignment.setSubject(sSubjects.getSelectedItem().toString());
        assignment.setDescription(etDescription.getText().toString());
        assignment.setDateInfo(currentDateInfo);

        bundle.putParcelable(Utility.SAVE_INFO, new SaveInfo(assignment, priority, true, -1));

        getParentFragmentManager().setFragmentResult(Utility.HOME_RESULT_KEY, bundle);
    }

    /**
     * Toggles the star to either priority or upcoming
     */
    public void toggleStar() {
        if(priority)
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        else
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create, menu);

        menu.getItem(0).setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        star = menu.getItem(0);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(DateUtils.inPriorityRange(context, currentDateInfo)) {
            menu.getItem(0).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                transaction.replace(R.id.fragment_container, MainActivity.homeFragment);
                transaction.addToBackStack(Utility.HOME_FRAGMENT);
                transaction.commit();

                return true;
            case R.id.create_save :
                save();

                Utility.hideSoftKeyboard(getActivity());

                FragmentTransaction saveTransaction = getParentFragmentManager().beginTransaction();
                saveTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                saveTransaction.replace(R.id.fragment_container, MainActivity.homeFragment);
                saveTransaction.addToBackStack(Utility.HOME_FRAGMENT);
                saveTransaction.commit();

                return true;
            case R.id.create_star :
                pressedPriority = true;

                if(priority)
                    item.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
                else
                    item.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));

                ((AnimatedVectorDrawableCompat) item.getIcon()).start();

                priority = !priority;

                return true;
        }

        return false;
    }
}
