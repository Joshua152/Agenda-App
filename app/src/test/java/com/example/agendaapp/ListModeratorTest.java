package com.example.agendaapp;

import com.example.agendaapp.Data.ListModerator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ListModeratorTest {

    /**
     * Test for setOverall()
     */
    @Test
    public void test_setOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        assertEquals(1, listModerator.setOverall(10, "99"));
        assertTrue(listModerator.getList(1).equals(new ArrayList<String>(
                Arrays.asList("07", "08", "09", "99", "11"))));
    }

    /**
     * Test for removeOverall()
     */
    @Test
    public void test_removeOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        listModerator.removeOverall(8);
        assertTrue(listModerator.getList(1).equals(new ArrayList<String>(
                Arrays.asList("07", "09", "10", "11"))));
    }

    /**
     * Test for geOverall()
     */
    @Test
    public void test_getOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        assertEquals("14", listModerator.getOverall(14));
    }

    /**
     * Test for swap()
     */
    @Test
    public void test_swap() {
        ListModerator<String> listModerator = getSampleModerator();

        listModerator.swap(1, 2);
        listModerator.swap(3, 8);

        assertTrue(listModerator.equals(new ListModerator<String>(
                new ArrayList<String>(Arrays.asList("02", "01", "08", "04", "05")),
                new ArrayList<String>(Arrays.asList("07", "03", "09", "10", "11")),
                new ArrayList<String>(Arrays.asList("13", "14", "15", "16", "17"))
        )));
    }

    /**
     * Test for getArrayPosFromOverall()
     */
    @Test
    public void test_getArrayPosFromOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        assertEquals(4, listModerator.getArrayPosFromOverall(17));
    }

    /**
     * Gets a simple sample String ListModerator
     * @return Returns a ListModerator of type String
     */
    private ListModerator<String> getSampleModerator() {
        return new ListModerator<String>(
                new ArrayList<String>(Arrays.asList("01", "02", "03", "04", "05")),
                new ArrayList<String>(Arrays.asList("07", "08", "09", "10", "11")),
                new ArrayList<String>(Arrays.asList("13", "14", "15", "16", "17"))
        );
    }
}