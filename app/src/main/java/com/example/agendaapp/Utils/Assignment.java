package com.example.agendaapp.Utils;

public class Assignment {

    DateInfo dateInfo;

    String title;
    String subject;
    String description;

    public Assignment() {
        title = "";
        subject = "";
        description = "";
    }

    public Assignment(String title, String subject, String description, DateInfo dateInfo) {
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.dateInfo = dateInfo;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateInfo(DateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }
}
