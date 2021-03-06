package com.example.tareservefinal

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tareservefinal.util.HashCode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.toptoche.searchablespinnerlibrary.SearchableSpinner


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ClassSelection.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClassSelection : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var database: DatabaseReference

    private lateinit var loadingTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        return inflater.inflate(R.layout.fragment_class_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val model = (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java]})

        (activity as MainActivity).supportActionBar?.setTitle(GoogleSignIn.getLastSignedInAccount(activity)?.displayName)

        loadingTextView = view.findViewById(R.id.courseLoadingTextView)

        super.onViewCreated(view, savedInstanceState)
        val IdArrayList = ArrayList<String>()
        val classArrayList = ArrayList<String>()
        database = FirebaseDatabase.getInstance().reference

        val recyclerView = view.findViewById<RecyclerView>(R.id.classListView)
        val spinnerFind = view.findViewById<SearchableSpinner>(R.id.spinner_find)
        val spinnerData:ArrayList<String> = ArrayList()
        var isFirstTimeClick = true

        val adapter = ClassListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        spinnerFind.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) { // your code here
                if(!isFirstTimeClick) {
                    database.child("Users").child(model!!.userId).child("ClassList").child(HashCode().hashCode(spinnerData[position])).setValue(spinnerData[position])
                    database.child("Users").child(model!!.userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                model!!.isTA = dataSnapshot.child("isTA").value.toString()
                                IdArrayList.clear()
                                dataSnapshot.child("ClassList").children.forEach {
                                    IdArrayList.add(it.value.toString())
                                }
                                adapter.setIdArray(IdArrayList)

                                classArrayList.clear()
                                for(id in IdArrayList)
                                {
                                    if(id.contains(" "))
                                        classArrayList.add(dataSnapshot.child(id.substring(0, id.indexOf(" "))).child(id).key.toString())
                                }
                                if (classArrayList.size == 0) loadingTextView.text = "Click the button in the bottom-right corner to add a course"
                                else loadingTextView.visibility = View.INVISIBLE

                                adapter.setClasses(classArrayList)
                            }
                        })
                    database.run {  }
                }
                else
                {
                    isFirstTimeClick = false
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) { // your code here
            }
        })

        val classRef = database.child("Users").child(model!!.userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    model!!.isTA = dataSnapshot.child("isTA").value.toString()
                    IdArrayList.clear()
                    dataSnapshot.child("ClassList").children.forEach {
                        IdArrayList.add(it.value.toString())
                    }
                    adapter.setIdArray(IdArrayList)

                }
            })
        classRef.run {  }

        val innerClassRef = database.child("Classes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                classArrayList.clear()
                for(id in IdArrayList)
                {
                    if(id.contains(" "))
                    classArrayList.add(dataSnapshot.child(id.substring(0, id.indexOf(" "))).child(id).key.toString())
                }
                if (classArrayList.size == 0) loadingTextView.text = "Click the button in the bottom-right corner to add a course"
                else loadingTextView.visibility = View.INVISIBLE

                adapter.setClasses(classArrayList)
            }
        })
        innerClassRef.run {  }

        database.child("Classes")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach {
                            it.children.forEach { spinnerData.add(it.key.toString()) }
                            val spinnerAdapt:ArrayAdapter<String> = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, spinnerData)
                            spinnerFind.adapter = spinnerAdapt
                        }
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.logout).isVisible = true
        menu.findItem(R.id.edit_classes).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_classes -> {
                requireView().findNavController().navigate(R.id.action_classSelection_to_classRemoval)
            }

            R.id.logout -> {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestServerAuthCode("886038774832-lja4e3reta073i1iup90qmo49aeeepuc.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                GoogleSignIn.getClient(requireActivity(), gso).signOut()
                        .addOnCompleteListener(requireActivity(), OnCompleteListener<Void?> {
                            GoogleSignIn.getClient(requireActivity(), gso).revokeAccess()
                                    .addOnCompleteListener(requireActivity(), OnCompleteListener<Void?> {
                                        requireView().findNavController().navigate(
                                                R.id.action_classSelection_to_loginScreen)
                                    })
                        })
            }
        }

        return false
    }

    inner class ClassListAdapter:
        RecyclerView.Adapter<ClassListAdapter.ClassViewHolder>(){
        private var classes = emptyList<String>()
        private var idArray = emptyList<String>()

        override fun getItemCount(): Int {

            return classes.size
        }

        fun setClasses(classes: List<String>)
        {
            this.classes = classes
            notifyDataSetChanged()
        }

        fun setIdArray(idArray: List<String>)
        {
            this.idArray = idArray
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {


            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_card_view, parent, false)
            return ClassViewHolder(v)
        }

        override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {

            holder.view.findViewById<TextView>(R.id.itemName).text=classes[position]

            val model = (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java]})

            if(model!!.isTA.equals("0")) {
                holder.itemView.setOnClickListener() {
                    it.findNavController().navigate(
                        R.id.action_classSelection_to_taSelection,
                        bundleOf("param1" to idArray[position])
                    )
                }
            }
            else
            {
                holder.itemView.setOnClickListener {
                    it.findNavController().navigate(
                        R.id.action_classSelection_to_taScreen,
                        bundleOf("param1" to idArray[position])
                    )
                }
            }

        }

        inner class ClassViewHolder(val view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
            override fun onClick(view: View?){

                if (view != null) {
                }
            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ClassSelection.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClassSelection().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
