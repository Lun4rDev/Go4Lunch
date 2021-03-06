package com.hernandez.mickael.go4lunch.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Mickael Hernandez on 26/03/2018.
 */
class ParametersActivityTest {

    val testImgUrl = "https://picsum.photos/400"

    val testUsername = "test username"

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(ParametersActivity::class.java)

    private lateinit var mActivity : ParametersActivity

    /** Prepares the activity */
    @Before
    fun setUp()
    {
        mActivity = mActivityTestRule.activity
        mActivity.mUser?.uid
    }

    /** Finishes the activity  */
    @After
    @Throws(Exception::class)
    fun tearDown() {
        mActivity.finish()
    }

    /** Tests that the user name and picture url are updated */
    @Test
    fun updateUserData() {
        // Type user name
        onView(withId(R.id.edit_name)).perform(clearText(), typeText(testUsername))

        // Type image url
        onView(withId(R.id.edit_imgurl)).perform(clearText(), typeText(testImgUrl))

        // Wait a second
        Thread.sleep(1000)

        // Click on apply
        onView(withId(R.id.action_apply)).perform(click())

        // Wait a second
        Thread.sleep(1000)

        // Get firestore data to compare
        FirebaseFirestore.getInstance().collection("users").document(mActivity.mUser!!.uid).get().addOnCompleteListener {

            // Assert the data exists and is equal to the one entered
            assertTrue(it.result.exists())
            assertEquals(testUsername, it.result.get("displayName"))
            assertEquals(testImgUrl, it.result.get("photoUrl"))
        }
    }
}