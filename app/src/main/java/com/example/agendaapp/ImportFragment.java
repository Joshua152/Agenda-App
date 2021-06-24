package com.example.agendaapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.Platform.GoogleClassroom;
import com.example.agendaapp.RecyclerAdapters.ImportRecyclerAdapter;
import com.example.agendaapp.Utils.Utility;

public class ImportFragment extends Fragment {

    private Context context;

    private RecyclerView recyclerView;

    private Platform[] platforms;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstance) {
        View view = inflater.inflate(R.layout.fragment_import, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.import_toolbar);
        toolbar.setTitle(getString(R.string.import_title));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        setHasOptionsMenu(true);

        init(view);

        return view;
    }

    /**
     * Inits the views (constructs)
     * @param view The inflated fragment
     */
    public void init(View view) {
        context = getContext();

        recyclerView = (RecyclerView) view.findViewById(R.id.import_recycler_view);

        platforms = new Platform[] {
                new GoogleClassroom(getActivity(), requireActivity().getActivityResultRegistry()),
                new GoogleClassroom(getActivity(), requireActivity().getActivityResultRegistry())
        };

        for(Platform p : platforms)
            getLifecycle().addObserver(p);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new ImportRecyclerAdapter(platforms));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);
                transaction.replace(R.id.fragment_container, MainActivity.homeFragment);
                transaction.addToBackStack(Utility.IMPORT_FRAGMENT);
                transaction.commit();

                return true;
        }

        return false;
    }
}
