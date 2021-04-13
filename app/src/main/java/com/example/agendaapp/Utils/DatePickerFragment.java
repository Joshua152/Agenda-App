/**
 * Fragment for the Date Picker Dialog.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp.Utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.example.agendaapp.Data.DateInfo;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment{

    // The listener for what to do when the date has been set
    private DatePickerDialog.OnDateSetListener listener;

    // DateInfo to be displayed
    private DateInfo dateInfo;

    /**
     * Constructor
     * @param listener The DatePickerDialog.OnDateSetListener (Defines what happens when the date
     *                 is set)
     */
    public DatePickerFragment(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;

        dateInfo = null;
    }

    /**
     * Sets the day, month, and year of the dialog when it is created
     * @param savedInstanceState The saved state
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        int year = 0;
        int month = 0;
        int day = 0;

        if(dateInfo == null) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
        } else {
            year = dateInfo.getYear();
            month = dateInfo.getMonth() - 1;
            day = dateInfo.getDay();
        }

        return new DatePickerDialog(getActivity(), listener, year, month, day);
    }

    /**
     * Sets the DateInfo for which the Dialog will be displayed
     * @param dateInfo The date info to be displayed
     */
    public void setDateInfo(DateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }
}
