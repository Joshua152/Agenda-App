/**
 * This class represents a ListModerator.
 * The ListModerator allows you to have
 * multiple ArrayLists while having the ability
 * to act on them either as if they were separate
 * or combined.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 11/26/2020
 */

package com.example.agendaapp.Utils;

import android.os.Bundle;

import java.util.ArrayList;

public class ListModerator<T> {

    // Array of ArrayLists
    ArrayList<T>[] lists;

    /**
     * Constructor
     * @param lists All the lists separated by commas in the correct order
     */
    public ListModerator(ArrayList<T>... lists) {
        this.lists = lists;
    }


}
