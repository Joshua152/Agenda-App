package com.joshuaau.plantlet;

import android.widget.DatePicker;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.joshuaau.plantlet.Data.Assignment;
import com.joshuaau.plantlet.Data.DateInfo;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LargeTest
public class AssignmentInteractionTest {
    @Rule
    public ActivityScenarioRule rule = new ActivityScenarioRule<>(MainActivity.class);

    private final Assignment[] a = {
            new Assignment("Assignment 1", "Math", "Description 1: This is pretty cool right?\nYeah it is!", new DateInfo("6/2/21", 2, 6, 2021)),
            new Assignment("Assignment 2", "Science", "Description 2: Hey there!", new DateInfo("6/3/21", 3, 6, 2021)),
            new Assignment("Assignment 3", "Literature", "How is it going?", new DateInfo("6/20/21", 20, 6, 2021)),
            new Assignment("Assignment 4", "Other", "Not good!!!!! Cause writing tests is so boring!!!!", new DateInfo("7/1/21", 1, 7, 2021))
    };

    @Test
    public void createAssignments() {
        for(Assignment assignment : a)
            createAssignment(assignment);
    }

    @Test
    public void openAssignments() {
        for (Assignment assignment : a) {
            onView(withId(R.id.home_recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(allOf(hasDescendant(withText(assignment.getTitle())),
                            hasDescendant(withText(assignment.getDescription()))), click()));

            onView(withId(R.id.view_tv_title))
                    .check(matches(withText(assignment.getTitle())));

            onView(withId(R.id.view_tv_due_date))
                    .check(matches(withText("Due Date: " + assignment.getDateInfo().getDate())));

            onView(withId(R.id.view_tv_subject))
                    .check(matches(withText("Subject: " + assignment.getSubject())));

            onView(withId(R.id.view_tv_description))
                    .check(matches(withText(assignment.getDescription())));

            onView(withContentDescription("Navigate up"))
                    .perform(click());
        }
    }

    public void createAssignment(Assignment a) {
        onView(withId(R.id.fab))
                .perform(click());

        onView(withId(R.id.edit_et_title))
                .perform(typeText(a.getTitle()));

        onView(withId(R.id.edit_et_description))
                .perform(typeText(a.getDescription()));

        onView(withId(R.id.edit_ib_date))
                .perform(click());

        onView(is(instanceOf(DatePicker.class)))
                .perform(PickerActions.setDate(a.getDateInfo().getYear(), a.getDateInfo().getMonth(),
                        a.getDateInfo().getDay()));

        onView(withText("OK"))
                .perform(click());

        onView(withId(R.id.edit_s_subject))
                .perform(click());

        onData(allOf(is(instanceOf(String.class)), is(a.getSubject())))
                .perform(click());

        onView(withId(R.id.create_save))
                .perform(click());
    }
}