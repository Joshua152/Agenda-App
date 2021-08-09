/**
 * Fragment for editing an assignment
 *
 * @uthor Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
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
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Dialogs.DatePickerFragment;
import com.example.agendaapp.Utils.DateUtils;
import com.example.agendaapp.Utils.Resize;
import com.example.agendaapp.Data.SaveInfo;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditFragment extends Fragment {

    public static final String TITLE = "Title";

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

    private Resize contentViewResize;

    private Assignment assignment;

    private int originalPosition;

    private int descriptionMinHeight;
    private int originalContentHeight;

    private boolean createNew;

    private boolean priority;
    private boolean pressedPriority;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.edit_toolbar);
        toolbar.setTitle(getArguments().getString(TITLE));
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
     * Init method for all the views
     * @param view The inflated Fragment view
     */
    private void init(View view) {
        context = getContext();

        constraintLayout = (ConstraintLayout) view.findViewById(R.id.edit_constraint_layout);
        llDescription = (LinearLayout) view.findViewById(R.id.edit_ll_description);
        tiTitle = (TextInputLayout) view.findViewById(R.id.edit_ti_title);
        tiDescription = (TextInputLayout) view.findViewById(R.id.edit_ti_description);
        etTitle = (TextInputEditText) view.findViewById(R.id.edit_et_title);
        etDescription = (TextInputEditText) view.findViewById(R.id.edit_et_description);
        tvDueDate = (TextView) view.findViewById(R.id.edit_tv_due_date);
        ibDate = (ImageButton) view.findViewById(R.id.edit_ib_date);
        sSubjects = (Spinner) view.findViewById(R.id.edit_s_subject);

        contentViewResize = new Resize((View) getActivity().getWindow().getDecorView(),
                (View) getActivity().getWindow().getDecorView().findViewById(Window.ID_ANDROID_CONTENT));

        SaveInfo info = getArguments().getParcelable(Utility.SAVE_INFO);
        assignment = info.getAssignment();
        originalPosition = info.getPosition();

        descriptionMinHeight = 0;
        originalContentHeight = contentViewResize.getContentHeight();

        createNew = info.getCreateNew();

        priority = info.getIsPriority();
        pressedPriority = priority && !DateUtils.inPriorityRange(context, assignment.getDateInfo());
    }

    /**
     * Inits the layout (sets texts, spinners...)
     */
    private void initLayout() {
        etTitle.setFocusable(true);
        etDescription.setFocusable(true);
        sSubjects.setFocusable(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subject_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSubjects.setAdapter(adapter);

        etTitle.setText(assignment.getTitle());
        tvDueDate.setText(assignment.getDateInfo().getDate());
        etDescription.setText(assignment.getDescription());
        sSubjects.setSelection(Utility.getSubjectPositionFromTitle(context, assignment.getSubject()), true);
    }

    /**
     * Inits the listeners
     */
    private void initListeners() {
        contentViewResize.addListener((Resize.ResizeListener) (fromHeight, toHeight, contentView) -> {
            if(toHeight == originalContentHeight) {
                // line height = height * multiplier + extra
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

        etTitle.addTextChangedListener(new TextWatcher() {
            int linesBefore;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                linesBefore = etTitle.getLineCount();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                int linesAfter = etTitle.getLineCount();

                if(linesBefore != linesAfter) {
                  if(linesAfter < etTitle.getMaxLines() || (linesBefore < linesAfter && linesAfter == etTitle.getMaxLines())) {
                      etDescription.setHeight(etDescription.getHeight() - (int) ((linesAfter - linesBefore) * (etDescription.getLineHeight() * etDescription.getLineSpacingMultiplier()
                              + etDescription.getLineSpacingExtra())));
                  } else if(linesAfter == etTitle.getMaxLines() + 1 && linesBefore < linesAfter) {
                      etDescription.setHeight(etDescription.getHeight() + 1);
                  } else if(linesAfter == etTitle.getMaxLines() && linesAfter < linesBefore) {
                      etDescription.setHeight(etDescription.getHeight() - 1);
                  }
                }
            }
        });

        ibDate.setOnClickListener(view -> {
            DatePickerFragment fragment = new DatePickerFragment((datePicker, year, month, day) -> {
                assignment.setDateInfo(DateUtils.getLocalDateFormat(getActivity(), day, month + 1, year));
                tvDueDate.setText(assignment.getDateInfo().getDate());

                if(!pressedPriority) {
                    priority = DateUtils.compareDates(DateUtils.getDay(getActivity(), 2), assignment.getDateInfo()) == DateInfo.FURTHER;
                    toggleStar();
                }

                star.setVisible(!DateUtils.inPriorityRange(context, assignment.getDateInfo()));
            });

            fragment.setOnClickListener((dialogInterface, button) -> {
                assignment.setDateInfo(DateUtils.getNoneInstance(context));
                tvDueDate.setText(assignment.getDateInfo().getDate());
            });

            Utility.hideSoftKeyboard(getActivity());

            etTitle.clearFocus();
            etDescription.clearFocus();
            sSubjects.clearFocus();

            fragment.setDateInfo(assignment.getDateInfo());
            fragment.show(getParentFragmentManager(), "Date Picker");
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
                set.connect(R.id.edit_ll_description, ConstraintSet.TOP, R.id.edit_constraint_middle, ConstraintSet.BOTTOM);
                set.connect(R.id.edit_ll_description, ConstraintSet.BOTTOM, R.id.edit_scroll_view, ConstraintSet.BOTTOM);
                set.connect(R.id.edit_ll_description, ConstraintSet.START, R.id.edit_scroll_view, ConstraintSet.START);
                set.connect(R.id.edit_ll_description, ConstraintSet.END, R.id.edit_scroll_view, ConstraintSet.END);
                set.applyTo(constraintLayout);

                llDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
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
     * Puts the data into a Bundle to a fragment result
     */
    private void save() {
        // Due date is set in the listener
        assignment.setTitle(etTitle.getText().toString());
        assignment.setSubject(sSubjects.getSelectedItem().toString());
        assignment.setDescription(etDescription.getText().toString());

        if(assignment.getTitle().equals(""))
            assignment.setTitle(getString(R.string.untitled));

        Bundle bundle = new Bundle();
        bundle.putParcelable(Utility.SAVE_INFO, new SaveInfo(assignment, priority, createNew, originalPosition));

        // To ViewFragment
        getParentFragmentManager().setFragmentResult(createNew ? Utility.HOME_RESULT_KEY : Utility.VIEW_RESULT_KEY, bundle);
    }

    /**
     * Toggles the star from priority to upcoming
     */
    public void toggleStar() {
        if(priority)
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        else
            star.setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit, menu);

        if(priority)
            menu.getItem(0).setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.unstar_anim));
        else
            menu.getItem(0).setIcon(AnimatedVectorDrawableCompat.create(context, R.drawable.star_anim));

        star = menu.getItem(0);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Utility.hideSoftKeyboard(getActivity());

            getParentFragmentManager().popBackStack();

            return true;
        } else if(item.getItemId() == R.id.edit_save) {
            save();

            Utility.hideSoftKeyboard(getActivity());

            getParentFragmentManager().popBackStack();

            return true;
        } else if(item.getItemId() == R.id.edit_star) {
            pressedPriority = !pressedPriority;

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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(DateUtils.inPriorityRange(context, assignment.getDateInfo()))
            menu.getItem(0).setVisible(false);
    }

    /**
     * Creates a new instance of the EditFragment (creating a new assignment)
     * @param context Context used to instantiate the DateInfo object for the new Assignment
     * @return Returns a new instance of EditFragment
     */
    public static EditFragment newInstance(Context context) {

        Bundle bundle = new Bundle();

        Assignment assignment = new Assignment();
        assignment.setDateInfo(DateUtils.getDay(context, 1));

        bundle.putString(TITLE, context.getString(R.string.create_title));
        bundle.putParcelable(Utility.SAVE_INFO, new SaveInfo(assignment, true, true, -1));

        EditFragment editFragment = new EditFragment();
        editFragment.setArguments(bundle);

        return editFragment;
    }

    /**
     * Creates a new instance of the EditFragment (not creating a new assignment; editing)
     * @param assignment The assignment to be shown
     * @param position The original position of the fragment (only needed if createNew is false)
     * @param priority If the assignment is a priority assignment
     * @return Returns a new instance of EditFragment
     */
    public static EditFragment newInstance(Context context, Assignment assignment, int position, boolean priority) {
        Bundle bundle = new Bundle();

        bundle.putString(TITLE, context.getString(R.string.edit_title));
        bundle.putParcelable(Utility.SAVE_INFO, new SaveInfo(assignment, priority, false, position));

        EditFragment editFragment = new EditFragment();
        editFragment.setArguments(bundle);

        return editFragment;
    }
}
