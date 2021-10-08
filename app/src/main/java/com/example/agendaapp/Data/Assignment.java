/**
 * This class represents a single Assignment.
 * An assignment contains the data of its title,
 * due date, subject, and description.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 11/26/2020
 */

package com.example.agendaapp.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Assignment implements Parcelable, Serializable {

    private static final long serialVersionUID = -1605429230305680146L;

    // The assignment's course id (if imported)
    private String courseId;
    // Assignment specific id (<Platform name>:<id>)
    private String id;

    // Assignment due date
    private DateInfo dateInfo;
    // Title of the assignment
    private String title;
    // Assignment subject
    private String subject;
    // Assignment description
    private String description;

    /**
     * No arg constructor
     */
    public Assignment() {
        courseId = "";
        id = "";

        title = "";
        subject = "";
        description = "";
        dateInfo = null;
    }

    /**
     * 4 arg constructor. Should be used to initialize all fields.
     * @param title Assignment title
     * @param subject Assignment subject
     * @param description Assignment description
     * @param dateInfo Assignment due date
     */
    public Assignment(String title, String subject, String description, DateInfo dateInfo) {
        courseId = "";
        id = "";

        this.title = title;
        this.subject = subject;
        this.description = description;
        this.dateInfo = dateInfo;
    }

    /**
     * Private constructor to be used by the Parcelable creator
     * @param in Parcel to be read in
     */
    private Assignment(Parcel in) {
        title = in.readString();
        subject = in.readString();
        description = in.readString();
        dateInfo = in.readParcelable(DateInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<Assignment> CREATOR =
            new Parcelable.Creator<Assignment>() {

        /**
         * Creates a new Assignment from a given Parcel
         * @param in The Parcel from which to create a new Assignment
         * @return Returns the Assignment gotten from the Parcel
         */
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        /**
         * Creates a new Assignment array
         * @param size Size of the Assignment array
         * @return Returns the new Assignment array
         */
        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };

    /**
     * Writes to a Parcel
     * @param out Parcel to be written to
     * @param flags Flags (info about the data)
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(subject);
        out.writeString(description);
        out.writeParcelable(dateInfo, 0);
    }

    /**
     * Describes the class (does not do anything because
     * Assignment is not extending anything)
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Sets the course id (platform name|courseId)
     * @param courseId The course id
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Sets the assignment id (platformName|courseId|assignmentId)
     * @param id The assignment id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the assignment title
     * @param title Assignment title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the assignment subject
     * @param subject Assignment subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Sets the assignment description
     * @param description Assignment description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the due date of the assignment (represented with DateInfo)
     * @param dateInfo DateInfo which represents the due date of the assignment
     */
    public void setDateInfo(DateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }

    /**
     * Gets the course id in the form of (platformName|courseId)
     * @return Returns the course id String
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Gets the assignment id in the form of (platformName|courseId|assignmentId)
     * @return Returns a string containing the assignment id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the title of the assignment
     * @return Returns a String containing the assignment title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the subject name of the assignment
     * @return Returns a String containing the assignment subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the description of the assignment
     * @return Returns a String containing the assignment description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the due date of the assignment (represented with DateInfo)
     * @return Returns a DateInfo containing the assignment due date
     */
    public DateInfo getDateInfo() {
        return dateInfo;
    }

    @Override
    public String toString() {
        return "\n{\n\tTitle: " + title + " " +
                "\n\tDescription: " + description +
                "\n\tSubject: " + subject +
                "\n\tDue Date: " + dateInfo.getDate() +
                "\n}\n";
    }
}
