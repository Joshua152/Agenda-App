/**
 * This is the MainActivity (entry point). This activity
 * holds the HomeFragment.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // The HomeFragment to be displayed (should be used by the other fragments)
    public static HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if(savedInstanceState == null)
            addHomeFragment();
    }

    /**
     * Inits the fields
     */
    private void init() {
        homeFragment = new HomeFragment();
    }

    /**
     * Adds the HomeFragment to the backstack
     */
    private void addHomeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, homeFragment);
        transaction.addToBackStack("Home Fragment");
        transaction.commit();
    }
}
