/**
 * Class to hold simple course data
 */

package com.example.agendaapp.Data;

import android.graphics.drawable.Drawable;

import com.example.agendaapp.Utils.Serialize;

import java.io.Serializable;

public class Course implements Serializable {

    private static final long serialVersionUID = -7339613603991095953L;

    private String courseId;

    private String coursePlatform;
    private String courseName;
    private String courseSubject;
    private transient Drawable courseIcon;

    /**
     * 5 arg constructor to set all fields
     * @param courseId The course id **GENERATE USING generateCourseId() METHOD
     * @param coursePlatform The course platform String
     * @param courseName The course name
     * @param courseSubject The course subject
     * @param courseIcon The course icon in the form of a drawable
     */
    public Course(String courseId, String coursePlatform, String courseName, String courseSubject, Drawable courseIcon) {
        this.courseId = courseId;

        this.coursePlatform = coursePlatform;
        this.courseName = courseName;
        this.courseSubject = courseSubject;
        this.courseIcon = courseIcon;
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

    public void setCourseIcon(Drawable courseIcon) {
        this.courseIcon = courseIcon;
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

    public Drawable getCourseIcon() {
        return courseIcon;
    }
}
