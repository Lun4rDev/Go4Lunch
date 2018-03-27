package com.hernandez.mickael.go4lunch.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.hernandez.mickael.go4lunch.R

class EmailDialogFragment:DialogFragment() {

    // Use this instance of the interface to deliver action events
    lateinit var mListener:NoticeDialogListener

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog:DialogFragment, email:String, password:String)
        fun onDialogNegativeClick(dialog:DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context:Context) {
        super.onAttach(context)

        // Verify that the host activity implements the callback interface
        try
        {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = context as NoticeDialogListener
        }
        catch (e:ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() + " must implement NoticeDialogListener"))
        }
    }
    override fun onCreateDialog(savedInstanceState:Bundle?):Dialog {
        val builder = AlertDialog.Builder(this.activity!!)

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val view = View.inflate(context, R.layout.dialog_signin, null)

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.signin) { _, _ ->
                    if(view.findViewById<EditText>(R.id.edit_password).text.count() >= 6){
                        mListener.onDialogPositiveClick(this,
                                view.findViewById<EditText>(R.id.edit_username).text.toString(),
                                view.findViewById<EditText>(R.id.edit_password).text.toString())
                    } else {
                        Toast.makeText(context, "Password must contain at least 6 characters.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    mListener.onDialogNegativeClick(this@EmailDialogFragment)
                    this@EmailDialogFragment.dialog.cancel()
                }
        return builder.create()
        }
}