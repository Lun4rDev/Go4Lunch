package com.hernandez.mickael.go4lunch.activities

import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import org.junit.Assert.*
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.R.id.navigation_list
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.NavigationViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.view.menu.ActionMenuItem
import android.support.v7.view.menu.ActionMenuItemView
import android.view.MenuItem
import android.widget.SearchView
import org.hamcrest.CoreMatchers
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import android.support.test.espresso.UiController
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.hernandez.mickael.go4lunch.R.id.drawer_layout
import com.hernandez.mickael.go4lunch.fragments.WorkmatesFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*


/**
 * Created by Mickael Hernandez on 14/02/2018.
 */
@RunWith(AndroidJUnit4::class)

class MainActivityTest {
    private val mDelay = 5000L

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)
    private lateinit var mActivity : MainActivity

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

    /** Tests that the app context is right */
    @Test
    fun useAppContext() {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.hernandez.mickael.go4lunch", appContext.packageName)
    }

    /** Tests that the navigation tab is changing tabs correctly */
    @Test
    fun changeNavigationTab(){
        mActivity.viewPager.currentItem = 2
        assert(mActivity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId == 2)
    }

    /** Tests the launch of RestaurantActivity via the workmates ListView */
    @Test
    fun restaurantViaListView() {
        Thread.sleep(mDelay)
        // register next activity that need to be monitored.
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(RestaurantActivity::class.java.name, null, false)
        //mActivity.viewPager.currentItem = 2
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open()) // opens the navigation drawer
        Espresso.onView(ViewMatchers.withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.item_lunch)) // clicks on the top stories item in drawer
        Thread.sleep(5*mDelay)
        val nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, mDelay)
        assertNotNull(nextActivity) // assert that the selected tab is the second (top stories tab)
        nextActivity.finish()
    }

    /** Tests that the parameters open via the nav drawer */
    @Test
    fun parametersViaNavigationDrawer() {
        // register next activity that need to be monitored.
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(ParametersActivity::class.java.name, null, false)
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open()) // opens the navigation drawer
        Espresso.onView(ViewMatchers.withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.item_settings)) // clicks on the top stories item in drawer
        val nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, mDelay)
        assertNotNull(nextActivity) // assert that the selected tab is the second (top stories tab)
        nextActivity.finish()
    }

    /** Tests that the user disconnects via the nav drawer (activity finishes) */
    @Test
    fun logoutViaNavigationDrawer() {
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open()) // opens the navigation drawer
        Espresso.onView(ViewMatchers.withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.item_logout)) // clicks on the top stories item in drawer
        assertNotNull(mActivity.isFinishing) // assert that the selected tab is the second (top stories tab)
        mActivity.finish()
    }

    /** Tests that nearby restaurant are detected at start */
    @Test
    fun nearbyRestaurants() {
        Thread.sleep(mDelay)
        assertTrue(mActivity.getRestaurantCount() > 0)
    }

    /** Tests the search field and that it generates restaurants in the list */
    @Test
    fun search() {
        // Opens the search field
        onView(withId(R.id.search_item)).perform(click())

        // Type "e" in the field
        onView(isAssignableFrom(SearchView::class.java)).perform(typeText("e"), pressKey(KeyEvent.KEYCODE_ENTER))

        // Sleep 5sec
        Thread.sleep(mDelay)

        // Assert that there's at least 1 restaurant found
        assertTrue(mActivity.getRestaurantCount() > 0)
    }

    /** Tests that workmates are generated in the list */
    @Test
    fun workmates() {
        assertTrue(mActivity.getWorkmatesCount() > 0)
    }
}