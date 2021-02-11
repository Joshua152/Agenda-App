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
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.example.agendaapp.Utils.Assignment;
import com.example.agendaapp.Utils.DateInfo;
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

        getParentFragmentManager().setFragmentResultListener(Utility.EDIT_RESULT_KEY, this, new ResultListener());

        init(view);

        initLayout(view);

        initListeners(view);

        return view;
    }

    private void init(View view) {
        context = getContext();

        llRoot = (LinearLayout) view.findViewById(R.id.view_ll_root);
        tvTitle = (TextView) view.findViewById(R.id.view_tv_title);
        tvDueDate = (TextView) view.findViewById(R.id.view_tv_due_date);
        tvSubject = (TextView) view.findViewById(R.id.view_tv_subject);
        tvDescription = (TextView) view.findViewById(R.id.view_tv_description);

        assignment = getArguments().getParcelable(Utility.ASSIGNMENT_KEY);

        position = getArguments().getInt(Utility.POSITION_KEY);

        isPriority = getArguments().getBoolean(Utility.PRIORITY_KEY);
    }

    private void initLayout(View view) {
        tvTitle.setText(assignment.getTitle());
        tvDueDate.setText(getString(R.string.due_date, assignment.getDateInfo().getDate()));
        tvSubject.setText(getString(R.string.subject, assignment.getSubject()));
        tvDescription.setText(assignment.getDescription());

        ViewCompat.setTransitionName(llRoot, context.getString(R.string.transition_background) + position);

        setSharedElementEnterTransition(new MaterialContainerTransform());

        postponeEnterTransition();
    }

    private void initListeners(View view) {
        llRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                llRoot.getViewTreeObserver().removeOnPreDrawListener(this);

                startPostponedEnterTransition();

                return true;
            }
        });
    }

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
                transaction.replace(R.id.fragment_container, EditFragment.newInstance(assignment, position, isPriority));
                transaction.addToBackStack(Utility.EDIT_FRAGMENT);
                transaction.commit();

                return true;
        }

        return false;
    }

    public static ViewFragment newInstance(Assignment assignment, int originalPosition, boolean priority) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Utility.ASSIGNMENT_KEY, assignment);
        bundle.putBoolean(Utility.PRIORITY_KEY, priority);
        bundle.putInt(Utility.POSITION_KEY, originalPosition);
        bundle.putBoolean(Utility.CREATE_NEW_KEY, false);

        ViewFragment viewFragment = new ViewFragment();
        viewFragment.setArguments(bundle);

        return viewFragment;
    }

    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            assignment = bundle.getParcelable(Utility.ASSIGNMENT_KEY);

            update();

            getParentFragmentManager().setFragmentResult(Utility.HOME_RESULT_KEY, bundle);
        }
    }
}
