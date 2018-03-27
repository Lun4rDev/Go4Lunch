package com.hernandez.mickael.go4lunch.activities

import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import org.junit.Assert.*
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
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
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import android.support.test.espresso.UiController
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.ViewAction
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.view.View
import com.hernandez.mickael.go4lunch.fragments.WorkmatesFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.Matcher


/**
 * Created by Mickael Hernandez on 14/02/2018.
 */
@RunWith(AndroidJUnit4::class)

class MainActivityTest {
    private val maxWaitingTime = 2000L

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

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.hernandez.mickael.go4lunch", appContext.packageName)
    }

    @Test
    fun changeNavigationTab(){
        mActivity.viewPager.currentItem = 2
        assert(mActivity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId == 2)
        //onView(instanceOf(WorkmatesFragment::class.java)).check(matches(isDisplayed()))
    }

    /** Tests the launch of RestaurantActivity via the restaurants ListView */
    @Test
    fun restaurantViaListView() {
        // register next activity that need to be monitored.
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(RestaurantActivity::class.java.name, null, false)
        onView(withId(R.id.search_item)).perform(click())
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("e \n"))
        Thread.sleep(4000)
        val nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000)
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
        val nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000)
        assertNotNull(nextActivity) // assert that the selected tab is the second (top stories tab)
        nextActivity.finish()
    }

    /** Tests that the user disconnects via the nav drawer */
    @Test
    fun logoutViaNavigationDrawer() {
        // register next activity that need to be monitored.
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(ConnectionActivity::class.java.name, null, false)
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open()) // opens the navigation drawer
        Espresso.onView(ViewMatchers.withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.item_logout)) // clicks on the top stories item in drawer
        val nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000)
        assertNotNull(nextActivity) // assert that the selected tab is the second (top stories tab)
        nextActivity.finish()
    }

    // TODO: Obsolete if above works
    /** Tests the search field and that it generates restaurants in the list */
    @Test
    fun search() {
        onView(withId(R.id.search_item)).perform(click())
        //Espresso.onData(CoreMatchers.anything()).inAdapterView(CoreMatchers.allOf(instanceOf())).atPosition(0)
        //onView(allOf(instanceOf(SearchView::class.java)))
        //typeSearchViewText("e \n")
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("e \n"))
        Thread.sleep(5000)
        assertTrue(mActivity.getRestaurantCount() > 0)
    }

    /** Tests that workmates are generated in the list */
    @Test
    fun workmates() {
        assertTrue(mActivity.getWorkmatesCount() > 0)
    }

    private fun typeSearchViewText(text: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
            }


            override fun getDescription(): String {
                return "Change view text"
            }

            override fun perform(uiController: UiController, view: View) {
                (view as SearchView).setQuery(text, false)
            }
        }
    }


}