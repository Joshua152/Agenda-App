package com.example.agendaapp.Utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment{

    DatePickerDialog.OnDateSetListener listener;

    DateInfo dateInfo;

    public DatePickerFragment(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;

        dateInfo = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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

    public void setDateInfo(DateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }
}
