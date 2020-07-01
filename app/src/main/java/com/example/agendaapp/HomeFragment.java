package com.example.agendaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    CreateFragment createFragment;

    Toolbar toolbar;
    FloatingActionButton fab;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.home_toolbar);
        toolbar.setTitle("Agenda");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        init(view);

        initListeners();

        return view;
    }

    private void init(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        createFragment = new CreateFragment();
    }

    private void initListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.expand_in, R.animator.expand_out);
                transaction.replace(R.id.fragment_container, createFragment);
                transaction.addToBackStack(Utility.CREATE_FRAGMENT);
                transaction.commit();
            }
        });
    }
}
