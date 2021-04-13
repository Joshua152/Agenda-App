/**
 * This class holds all the info for when saving to a Bundle.
 * (assignment, priority, position, original position, create new
 *
 * @author Joshua Au
 * @version 1.0
 * @since 3/15/2021
 */

package com.example.agendaapp.Data;

import android.os.Parcel;
import android.os.Parcelable;

public class SaveInfo implements Parcelable {
    // The saved assignment
    private Assignment assignment;
    // Whether or not the assignment should be in the priority list
    private boolean isPriority;
    // Whether or not a new assignment should be created
    private boolean createNew;
    // The new position of the assignment
    private int position;
    // The original position of the assignment
    private int originalPosition;

    /**
     * Constructor
     * @param assignment The assignment
     * @param isPriority If the assignment is a priority
     * @param createNew If a new assignment should be created
     * @param position The new position of the assignment
     * @param originalPosition The original position of the assignment
     */
    public SaveInfo(Assignment assignment, boolean isPriority, boolean createNew, int position, int originalPosition) {
        this.assignment = assignment;
        this.isPriority = isPriority;
        this.createNew = createNew;
        this.position = position;
        this.originalPosition = originalPosition;
    }

    /**
     * Constructor for reading in a Parcel
     * @param in The parcel to read from
     */
    public SaveInfo(Parcel in) {
        assignment = in.readParcelable(Assignment.class.getClassLoader());
        isPriority = in.readInt() == 1;
        createNew = in.readInt() == 1;
        position = in.readInt();
        originalPosition = in.readInt();
    }

    public static final Parcelable.Creator<SaveInfo> CREATOR = new Parcelable.Creator<SaveInfo>() {
        public SaveInfo createFromParcel(Parcel in) {
            return new SaveInfo(in);
        }

        public SaveInfo[] newArray(int size) {
            return new SaveInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(assignment, 0);
        out.writeInt(isPriority ? 1 : 0);
        out.writeInt(createNew ? 1 : 0);
        out.writeInt(position);
        out.writeInt(originalPosition);
    }

    /**
     * Gets the assignment to be saved
     * @return The assignment to be saved
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Gets if the assignment is a priority
     * @return Returns a boolean for if it is a priority
     */
    public boolean getIsPriority() {
        return isPriority;
    }

    /**
     * Gets if a new assignment should be created
     * @return Returns a boolean for if an assignment should be created
     */
    public boolean getCreateNew() {
        return createNew;
    }

    /**
     * Gets the new position of the assignment
     * @return Returns an int for the new position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the original position of the assignment
     * @return Returns an int for the originalPosition
     */
    public int getOriginalPosition() {
        return originalPosition;
    }
}
