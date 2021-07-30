/**
 * Tests for the DateUtils class
 */

package com.example.agendaapp;

import android.content.Context;

import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Utils.DateUtils;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class DateUtilsTest {

    @Test
    public void test_compareDates() {
        assertEquals(DateInfo.SAME, DateUtils.compareDates(new DateInfo("1/1/2021", 1, 1, 2021),
                new DateInfo("1/1/2021", 1, 1, 2021)));

        assertEquals(DateInfo.CLOSER, DateUtils.compareDates(new DateInfo("3/2/2021", 2, 3, 2021),
                new DateInfo("6/23/2021", 23, 6, 2021)));

        assertEquals(DateInfo.FURTHER, DateUtils.compareDates(new DateInfo("10/11/2021", 11, 10, 2021),
                new DateInfo("5/1/2021", 1, 5, 2021)));
    }
}
