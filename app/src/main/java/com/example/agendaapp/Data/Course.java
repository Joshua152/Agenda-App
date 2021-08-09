/**
 * Class to hold simple course data
 */

package com.example.agendaapp.Data;

import android.graphics.drawable.Drawable;

public class Course {

    private String courseId;

    private String coursePlatform;
    private String courseName;
    private String courseSubject;
    private Drawable courseIcon;

    public Course(String courseId, String coursePlatform, String courseName, String courseSubject, Drawable courseIcon) {
        this.courseId = courseId;

        this.coursePlatform = coursePlatform;
        this.courseName = courseName;
        this.courseSubject = courseSubject;
        this.courseIcon = courseIcon;
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
