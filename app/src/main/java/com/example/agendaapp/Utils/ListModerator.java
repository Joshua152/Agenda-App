/*
  This class represents a ListModerator.
  The ListModerator allows you to have
  multiple ArrayLists while having the ability
  to act on them either as if they were separate
  or combined.

  Note: Assumes each list has a header in the RecyclerView

  @author Joshua Au
  @version 1.0
  @since 11/26/2020
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

        while(index > lists[list].size()) {
            index -= lists[list].size() + 1; // + 1 to account for the header
            list++;
        }

        lists[list].set(index - 1, item); // - 1 to account for the header for the list at which the index is

        return list;
    }

    /**
     * Removes the item at the given overall index
     * @param index The index of the item to be removed
     */
    public void removeOverall(int index) {
        int list = 0;

        while(index > lists[list].size()) {
            index -= lists[list].size() + 1; // + 1 to account for header
            list++;
        }

        lists[list].remove(index - 1); // - 1 to account for the header for the list at which the index is
    }

    /**
     * Gets an item given the overall index
     * @param index Overall item index
     * @return Returns item at the given index
     */
    public T getOverall(int index) {
        int list = 0;

        while(index > lists[list].size()) {
            index -= lists[list].size() + 1; // + 1 to account for header
            list++;
        }

        return lists[list].get(index - 1); // - 1 to account for the header for the list at which the index is
    }

    /**
     * Gets the item's array position given the overall index. (Accounts for headers)
     * @param index Overall item index
     * @return Returns the item's array position
     */
    public int getArrayPosFromOverall(int index) {
        int list = 0;

        while(index > lists[list].size()) {
            index -= lists[list].size() + 1; // + 1 to account for the header
            list++;
        }

        return index - 1; // - 1 to account for the header for the list at which the index is
    }

    /**
     * Gets the total number of items in the arrays
     * @return Returns an int containing the item count
     */
    public int getItemCount() {
        int count = 0;

        for(ArrayList<T> list : lists)
            count += list.size();

        return count;
    }

    /**
     * Returns the number of arrays in the moderator
     * @return Returns an int containing the number of arrays
     */
    public int lists() {
        return lists.length;
    }

    /**
     * Gets the ArrayList at the given index
     * @param index The ArrayList index
     * @return Returns the requested ArrayList
     */
    public ArrayList<T> getList(int index) {
        return lists[index];
    }

    /**
     * Overriding equals(). Compares underlying arrays.
     * @param obj The object to compare to this
     * @return Returns true if this and obj's underlying arrays have the same values
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ListModerator))
            return false;

        for(int i = 0; i < lists.length; i++) {
            if(!lists[i].equals(((ListModerator<T>) obj).getList(i)))
                return false;
        }

        return true;
    }

    /**
     * Overriding toString()
     * @return Returns the ListModerator in the form of a String (all underlying
     * lists are printed out)
     */
    @Override
    public String toString() {
        String converted = "";

        for(int i = 0; i < lists.length; i++)
            converted += (lists[i].toString() + "\n");

        return converted;
    }
}
