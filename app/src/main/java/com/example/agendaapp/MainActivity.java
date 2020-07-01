package com.example.agendaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static HomeFragment homeFragment;
    public static CreateFragment createFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        addHomeFragment();
    }

    private void init() {
        homeFragment = new HomeFragment();
        createFragment = new CreateFragment();
    }

    private void addHomeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, homeFragment);
        transaction.addToBackStack("Home Fragment");
        transaction.commit();
    }
}
