/**
 * This is the fragment to view an assignment. You can also
 * navigate to the EditFragment.
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.SaveInfo;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.transition.MaterialContainerTransform;

public class ViewFragment extends Fragment {

    private Context context;

    private LinearLayout llRoot;
    private TextView tvTitle;
    private TextView tvDueDate;
    private TextView tvSubject;
    private TextView tvDescription;

    private Assignment assignment;
    private int position;
    private boolean isPriority;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.view_toolbar);
        toolbar.setTitle(getString(R.string.view_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        setHasOptionsMenu(true);

        getParentFragmentManager().setFragmentResultListener(Utility.VIEW_RESULT_KEY, this, new ResultListener());

        init(view);

        initLayout(view);

        initListeners();

        return view;
    }

    /**
     * Inits the fields
     * @param view The inflated fragment
     */
    private void init(View view) {
        context = getContext();

        llRoot = (LinearLayout) view.findViewById(R.id.view_ll_root);
        tvTitle = (TextView) view.findViewById(R.id.view_tv_title);
        tvDueDate = (TextView) view.findViewById(R.id.view_tv_due_date);
        tvSubject = (TextView) view.findViewById(R.id.view_tv_subject);
        tvDescription = (TextView) view.findViewById(R.id.view_tv_description);

        SaveInfo info = getArguments().getParcelable(Utility.SAVE_INFO);
        assignment = info.getAssignment();
        position = info.getPosition();
        isPriority = info.getIsPriority();
    }

    /**
     * Inits the layout specifics (texts)
     * @param view The inflated fragment
     */
    private void initLayout(View view) {
        tvTitle.setText(assignment.getTitle());
        tvDueDate.setText(getString(R.string.due_date, assignment.getDateInfo().getDate()));
        tvSubject.setText(getString(R.string.subject, assignment.getSubject()));
        tvDescription.setText(assignment.getDescription());

         ViewCompat.setTransitionName(llRoot, Utility.TRANSITION_BACKGROUND + position);

        setSharedElementEnterTransition(new MaterialContainerTransform());

        postponeEnterTransition();
    }

    /**
     * Inits the listeners
     */
    private void initListeners() {
        llRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                llRoot.getViewTreeObserver().removeOnPreDrawListener(this);

                startPostponedEnterTransition();

                return true;
            }
        });
    }

    /**
     * Updates the TextViews with new Strings
     */
    private void update() {
        tvTitle.setText(assignment.getTitle());
        tvDueDate.setText(getString(R.string.due_date, assignment.getDateInfo().getDate()));
        tvSubject.setText(getString(R.string.subject, assignment.getSubject()));
        tvDescription.setText(assignment.getDescription());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        switch(item.getItemId()) {
            case android.R.id.home :
                getParentFragmentManager().popBackStack();

                return true;
            case R.id.view_edit :
                transaction.replace(R.id.fragment_container, EditFragment.newInstance(context, assignment, position, isPriority));
                transaction.addToBackStack(Utility.EDIT_FRAGMENT);
                transaction.commit();

                return true;
        }

        return false;
    }

    /**
     * Creates a new instance of a ViewFragment
     * @param assignment The assignment to be viewed
     * @param position The position of the assignment's CardView in the RecyclerView
     * @param priority Whether or not the assignment is priority
     * @return Returns a new ViewFragment instance
     */
    public static ViewFragment newInstance(Assignment assignment, int position, boolean priority) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Utility.SAVE_INFO, new SaveInfo(assignment, priority, false, position));

        ViewFragment viewFragment = new ViewFragment();
        viewFragment.setArguments(bundle);

        return viewFragment;
    }

    /**
     * The FragmentResultListener class. The ViewFragment listens for results from the EditFragment
     */
    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            assignment = ((SaveInfo) bundle.getParcelable(Utility.SAVE_INFO)).getAssignment();

            update();

            getParentFragmentManager().setFragmentResult(Utility.HOME_RESULT_KEY, bundle);
        }
    }
}
