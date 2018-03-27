package com.hernandez.mickael.go4lunch.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.R
import kotlinx.android.synthetic.main.activity_parameters.view.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Mickael Hernandez on 26/03/2018.
 */
class ParametersActivityTest {

    val testImgUrl = "https://2.bp.blogspot.com/-bD9iB6eEFNc/WXdfMahDf-I/AAAAAAAAEX0/Jr2V9cPvrJITSZkTmwt2k1PBZ_m830A5wCLcBGAs/s1600/image1.png"

    val testUsername = "test username"

    lateinit var mUid : String

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
        onView(withId(R.id.edit_name)).perform(clearText(), typeText(testUsername))
        onView(withId(R.id.edit_imgurl)).perform(clearText(), typeText(testImgUrl))
        onView(withId(R.id.action_apply)).perform(click())
        val res = FirebaseFirestore.getInstance().collection("users").document(mUid).get().result
        assertEquals(res.get("displayName"), testImgUrl)
        assertEquals(res.get("photoUrl"), testImgUrl)
    }
}