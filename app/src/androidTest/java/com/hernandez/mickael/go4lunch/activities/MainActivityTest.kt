package com.hernandez.mickael.go4lunch.activities

import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import org.junit.Assert.*
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.R.id.navigation_list
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

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
        mActivity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = navigation_list
        assert(mActivity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId == navigation_list)
    }
}