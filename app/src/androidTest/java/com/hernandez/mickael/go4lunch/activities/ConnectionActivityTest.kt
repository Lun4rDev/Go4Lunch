package com.hernandez.mickael.go4lunch.activities

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.hernandez.mickael.go4lunch.R
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Mickael Hernandez on 26/03/2018.
 */
@RunWith(AndroidJUnit4::class)

class ConnectionActivityTest {

    /** Test account credentials */
    var mAccountMail = "test@test.com"
    var mAccountPassword = "test_password"

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(ConnectionActivity::class.java)

    private lateinit var mActivity : ConnectionActivity

    /** Prepares the activity */
    @Before
    fun setUp()
    {
        mActivity = mActivityTestRule.activity
    }

    /** Finishes the activity  */
    @After
    @Throws(Exception::class)
    fun tearDown() {
        mActivity.finish()
    }

    /** Tests that the connection (with e-mail) is viable */
    @Test
    fun connect() {
        // If user is automatically connected, try and disconnect
        /*try {
            onView(withId(R.id.search_item)).check(matches(isDisplayed()))
            Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open()) // opens the navigation drawer
            Espresso.onView(ViewMatchers.withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.item_logout)) // clicks on the top stories item in drawer
        } catch(e: Exception){
            e.printStackTrace()
        }*/

        try {
            onView(withText(mActivity.getString(R.string.restaurant_map))).check(matches(isDisplayed()))
            return
        } catch(e: Exception){
            e.printStackTrace()
        }
        // register next activity that need to be monitored.
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(MainActivity::class.java.name, null, false)

        // clicks on the e-mail button
        onView(withId(R.id.btn_email)).perform(click())

        // Delay to let the dialog open
        Thread.sleep(1000)

        // fills EditViews with mail and password
        onView(withId(R.id.edit_username)).perform(clearText(), typeText(mAccountMail))
        onView(withId(R.id.edit_password)).perform(clearText(), typeText(mAccountPassword))

        // clicks on sign-in
        onView(withText(R.string.signin)).perform(click())

        // tests that MainActivity has been launched
        val nextActivity = InstrumentationRegistry.getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000)
        assertNotNull(nextActivity)
        nextActivity.finish()
    }


}