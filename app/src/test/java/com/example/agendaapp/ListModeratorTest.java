package com.example.agendaapp;

import com.example.agendaapp.Utils.ListModerator;

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
     * Test for getOverall()
     */
    @Test
    public void testSetOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        assertEquals(2, listModerator.setOverall(10, "99"));
        assertTrue( listModerator.getList(2).equals(new ArrayList<String>(
                Arrays.asList("99", "22", "23", "24", "25"))));
    }

    /**
     * Test for geOverall()
     */
    @Test
    public void testGetOverall() {
        ListModerator<String> listModerator = getSampleModerator();

        assertEquals("25", listModerator.getOverall(14));
    }

    /**
     * Test for swap()
     */
    @Test
    public void testSwap() {
        ListModerator<String> listModerator = getSampleModerator();

        listModerator.swap(0, 1);
        listModerator.swap(3, 8);

        System.out.println(listModerator.toString());

        assertTrue(listModerator.equals(new ListModerator<String>(
                new ArrayList<String>(Arrays.asList("02", "01", "03", "14", "05")),
                new ArrayList<String>(Arrays.asList("11", "12", "13", "04", "15")),
                new ArrayList<String>(Arrays.asList("21", "22", "23", "24", "25"))
        )));
    }

    /**
     * Gets a simple sample String ListModerator
     * @return Returns a ListModerator of type String
     */
    private ListModerator<String> getSampleModerator() {
        return new ListModerator<String>(
                new ArrayList<String>(Arrays.asList("01", "02", "03", "04", "05")),
                new ArrayList<String>(Arrays.asList("11", "12", "13", "14", "15")),
                new ArrayList<String>(Arrays.asList("21", "22", "23", "24", "25"))
        );
    }
}