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
    private ArrayList<T>[] lists;

    /**
     * Constructor
     * @param lists All the lists separated by commas in the correct order
     */
    public ListModerator(ArrayList<T>... lists) {
        this.lists = lists;
    }

    /**
     * Switches 2 items given their overall indices
     * @param index1 Overall index 1
     * @param index2 Overall index 2
     */
    public void swap(int index1, int index2) {
        T temp = getOverall(index1);

        setOverall(index1, getOverall(index2));
        setOverall(index2, temp);
    }

    /**
     * Calls set on the given index (treating all
     * arrays as one single)
     * @param index The index to change
     * @return Returns the index of the list being added to
     */
    public int setOverall(int index, T item) {
        int list = 0;

        while(index >= lists[list].size()) {
            index -= lists[list].size();
            list++;
        }

        lists[list].set(index, item);

        return list;
    }

    /**
     * Gets an item given the overall index
     * @param index Overall item index
     * @return Returns item at the given index
     */
    public T getOverall(int index) {
        int list = 0;

        while(index >= lists[list].size()) {
            index -= lists[list].size();
            list++;
        }

        return lists[list].get(index);
    }

    /**
     * Gets the ArrayList at the given index
     * @param index The ArrayList index
     * @return Returns the requested ArrayList
     */
    public ArrayList<T> getList(int index) {
        return lists[index];
    }

    @Override
    public String toString() {
        String converted = "";

        for(int i = 0; i < lists.length; i++)
            converted += (lists[i].toString() + "\n");

        return converted;
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof ListModerator))
            return false;

        for(int i = 0; i < lists.length; i++) {
            if(!lists[i].equals(((ListModerator<T>) obj).getList(i)))
                return false;
        }

        return true;
    }
}
