package com.example.agendaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class CreateFragment extends Fragment {

    CollapsingToolbarLayout toolbarLayout;
    Toolbar toolbar;

    HomeFragment homeFragment;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        toolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        toolbarLayout.setTitle("Add Assignment");

        toolbar = (Toolbar) view.findViewById(R.id.create_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);

        init(view);

        return view;
    }

    public void init(View view) {
        homeFragment = new HomeFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.expand_in, R.animator.expand_out);
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.addToBackStack(MainActivity.HOME_FRAGMENT);
                transaction.commit();
                return true;
            default :
                return false;
        }
    }
}
