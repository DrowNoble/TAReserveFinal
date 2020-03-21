package com.example.tareservefinal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TaInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaInfo : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ta_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val taName = view.findViewById<TextView>(R.id.TaName)
        val taDesc = view.findViewById<TextView>(R.id.TaDescript)
        val taTime = view.findViewById<TextView>(R.id.TaTimes)
        val queueUp = view.findViewById<ToggleButton>(R.id.queueUp)
        val model = (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java]})
        database = FirebaseDatabase.getInstance().reference
        val studentRef = database.child("Users").child(param1!!).child("TAData")


        queueUp.setOnClickListener{
            if(!queueUp.isChecked)
            {
                studentRef.child("StudentList").child(model!!.userId).removeValue()
            }
            else
            {
                var numStudents = "v"
                val classRef = studentRef.child("NumStudents").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        numStudents = dataSnapshot.value.toString()
                        print(dataSnapshot.value.toString()+"MISSINGO")
                        studentRef.child("StudentList").child(model!!.userId).setValue(numStudents.toInt()+1)
                    }
                })
                classRef.run {  }
            }
        }

        val classRef = database.child("Users").child(param1!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    taName.text = dataSnapshot.child("Name").value.toString()
                    taTime.text = dataSnapshot.child("TAData").child("Schedule").value.toString()
                    taDesc.text = dataSnapshot.child("TAData").child("Description").value.toString()
                }
            })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TaInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}