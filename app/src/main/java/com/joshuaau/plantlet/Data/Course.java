/**
 * Class to hold simple course data
 */

package com.joshuaau.plantlet.Data;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Course implements Serializable {

    private static final long serialVersionUID = -7339613603991095953L;

    private String courseId;

    private String coursePlatform;
    private String courseName;
    private String courseSubject;
    private transient int courseIconId;

    /**
     * 5 arg constructor to set all fields
     * @param courseId The course id **GENERATE USING generateCourseId() METHOD
     * @param coursePlatform The course platform String
     * @param courseName The course name
     * @param courseSubject The course subject
     * @param courseIconId The course icon drawable id
     */
    public Course(String courseId, String coursePlatform, String courseName, String courseSubject, int courseIconId) {
        this.courseId = courseId;

        this.coursePlatform = coursePlatform;
        this.courseName = courseName;
        this.courseSubject = courseSubject;
        this.courseIconId = courseIconId;
    }

    /**
     * Generates the course id in the correct form of platformName|courseId
     * @param coursePlatform The course platform
     * @param courseId The course id String
     */
    public static String generateCourseId(String coursePlatform, String courseId) {
        return coursePlatform + "|" + courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public void setCourseIcon(int courseIconId) {
        this.courseIconId = courseIconId;
    }

    public String getCourseId() {
        return courseId;
    }

    /**
     * Gets the pure course id without the platform or the pipe ('|')
     * @return Returns the base id String
     */
    public String getBaseCourseId() {
        return courseId.substring(courseId.indexOf("|") + 1);
    }

    public String getCoursePlatform() {
        return coursePlatform;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public int getCourseIconId() {
        return courseIconId;
    }
}
