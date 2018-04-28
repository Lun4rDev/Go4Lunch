package com.hernandez.mickael.go4lunch.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.activities.MainActivity
import com.hernandez.mickael.go4lunch.adapters.WorkmatesListAdapter
import com.hernandez.mickael.go4lunch.model.Workmate


/**
 * Created by Mickael Hernandez on 24/01/2018.
 */
class WorkmatesFragment : Fragment() {

    /** List of workmates */
    private var workmatesList = ArrayList<Workmate>()

    /** Adapter between workmates list and ListView */
    private lateinit var mAdapter: WorkmatesListAdapter

    /** Users Firestore collection reference */
    var mColRef = FirebaseFirestore.getInstance().collection("users")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true

        // Inflate the layout for this fragment
        val convertView = inflater.inflate(R.layout.fragment_workmates, container, false)

        // RecyclerView in layout
        val recyclerView = convertView.findViewById<RecyclerView>(R.id.list_workmates)

        // Setting up list and its adapter
        mAdapter = WorkmatesListAdapter(context!!, R.layout.row_workmate, workmatesList)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = llm
        recyclerView.adapter = mAdapter

        // Row separator
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, llm.orientation))

        // Item click listener
        recyclerView.setOnClickListener {
            // restoId = it.tag
            (activity as MainActivity).displayRestaurant(it.tag.toString())
        }

        // Populating workmates list with Firestore data
        mColRef.addSnapshotListener { colSnapshot, _ ->
            if(colSnapshot != null && colSnapshot.documents.isNotEmpty()){
                workmatesList.clear()
                for(doc in colSnapshot.documents){
                    if(doc.exists()){
                        workmatesList.add(doc.toObject(Workmate::class.java)!!)
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
        }

        return convertView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mAdapter = WorkmatesListAdapter(context!!, R.layout.row_workmate, workmatesList)
        mAdapter.notifyDataSetChanged()
    }

    /** Returns list of workmates */
    fun getList(): ArrayList<Workmate> {
        return workmatesList
    }
}