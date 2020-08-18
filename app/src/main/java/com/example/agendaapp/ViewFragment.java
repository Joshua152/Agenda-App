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

import com.example.agendaapp.Utils.DateInfo;
import com.example.agendaapp.Utils.Utility;
import com.google.android.material.transition.MaterialContainerTransform;

public class ViewFragment extends Fragment {

    Context context;

    Toolbar toolbar;
    LinearLayout llRoot;
    TextView tvTitle;
    TextView tvDueDate;
    TextView tvSubject;
    TextView tvDescription;

    Bundle editBundle;

    Transition sharedElementEnter;
    Transition sharedElementReturn;

    int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.view_toolbar);
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

        editBundle = getArguments();

        llRoot = (LinearLayout) view.findViewById(R.id.view_ll_root);
        tvTitle = (TextView) view.findViewById(R.id.view_tv_title);
        tvDueDate = (TextView) view.findViewById(R.id.view_tv_due_date);
        tvSubject = (TextView) view.findViewById(R.id.view_tv_subject);
        tvDescription = (TextView) view.findViewById(R.id.view_tv_description);

        sharedElementEnter = TransitionInflater.from(context).inflateTransition(R.transition.transition_shared_element_enter);
        sharedElementReturn = TransitionInflater.from(context).inflateTransition(R.transition.transition_shared_element_return);

        position = getArguments().getInt(Utility.EDIT_BUNDLE_POSITION_KEY);
    }

    private void initLayout(View view) {
        tvTitle.setText(getArguments().getString(Utility.EDIT_BUNDLE_TITLE_KEY));
        tvDueDate.setText(getString(R.string.due_date, getArguments().getString(Utility.EDIT_BUNDLE_DUE_DATE_KEY)));
        tvSubject.setText(getString(R.string.subject, getArguments().getString(Utility.EDIT_BUNDLE_SUBJECT_KEY)));
        tvDescription.setText(getArguments().getString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY));

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
        tvTitle.setText(editBundle.getString(Utility.EDIT_BUNDLE_TITLE_KEY));
        tvDueDate.setText(getString(R.string.due_date, editBundle.getString(Utility.EDIT_BUNDLE_DUE_DATE_KEY)));
        tvSubject.setText(getString(R.string.subject, editBundle.getString(Utility.EDIT_BUNDLE_SUBJECT_KEY)));
        tvDescription.setText(editBundle.getString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY));
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
                System.out.println("go home");
                return true;
            case R.id.view_edit :
                String title = editBundle.getString(Utility.EDIT_BUNDLE_TITLE_KEY);
                String dueDate = editBundle.getString(Utility.EDIT_BUNDLE_DUE_DATE_KEY);
                String description = editBundle.getString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY);
                String subject = editBundle.getString(Utility.EDIT_BUNDLE_SUBJECT_KEY);
                DateInfo dateInfo = new DateInfo(editBundle.getString(Utility.EDIT_BUNDLE_DUE_DATE_KEY),
                        editBundle.getInt(Utility.EDIT_BUNDLE_DAY_KEY), editBundle.getInt(Utility.EDIT_BUNDLE_MONTH_KEY),
                        editBundle.getInt(Utility.EDIT_BUNDLE_YEAR_KEY));

                int originalPosition = editBundle.getInt(Utility.EDIT_BUNDLE_POSITION_KEY);
                boolean priority = editBundle.getBoolean(Utility.EDIT_BUNDLE_PRIORITY_KEY);

                transaction.replace(R.id.fragment_container, EditFragment.newInstance(title, dueDate,
                        description, subject, dateInfo, originalPosition, priority));
                transaction.addToBackStack(Utility.EDIT_FRAGMENT);
                transaction.commit();

                return true;
        }

        return false;
    }

    public static ViewFragment newInstance(String title, String dueDate, String description, String subject,
                                           DateInfo dateInfo, int originalPosition, boolean priority) {

        Bundle bundle = new Bundle();
        bundle.putString(Utility.EDIT_BUNDLE_TITLE_KEY, title);
        bundle.putString(Utility.EDIT_BUNDLE_DUE_DATE_KEY, dueDate);
        bundle.putString(Utility.EDIT_BUNDLE_SUBJECT_KEY, subject);
        bundle.putString(Utility.EDIT_BUNDLE_DESCRIPTION_KEY, description);
        bundle.putInt(Utility.EDIT_BUNDLE_DAY_KEY, dateInfo.getDay());
        bundle.putInt(Utility.EDIT_BUNDLE_MONTH_KEY, dateInfo.getMonth());
        bundle.putInt(Utility.EDIT_BUNDLE_YEAR_KEY, dateInfo.getYear());
        bundle.putInt(Utility.EDIT_BUNDLE_POSITION_KEY, originalPosition);
        bundle.putBoolean(Utility.EDIT_BUNDLE_PRIORITY_KEY, priority);
        bundle.putBoolean(Utility.EDIT_BUNDLE_CREATE_NEW_KEY, false);

        ViewFragment viewFragment = new ViewFragment();
        viewFragment.setArguments(bundle);

        return viewFragment;
    }

    class ResultListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(String key, Bundle bundle) {
            editBundle = bundle;
            update();

            getParentFragmentManager().setFragmentResult(Utility.HOME_RESULT_KEY, bundle);
        }
    }
}
