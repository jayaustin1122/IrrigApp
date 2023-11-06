package com.example.iriggattion.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iriggattion.adapter.ContactsAdapter
import com.example.iriggattion.databinding.FragmentContactsRegisteredBinding
import com.example.iriggattion.model.ContactModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ContactsRegisteredFragment : Fragment() {
    private lateinit var binding : FragmentContactsRegisteredBinding
    private lateinit var contactArray : ArrayList<ContactModel>

    //adapter
    private lateinit var adapter : ContactsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsRegisteredBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContacts()
    }
    private fun getContacts() {
        //initialize
        contactArray = ArrayList()

        val dbRef = FirebaseDatabase.getInstance().getReference("Contacts")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list
                contactArray.clear()
                for (data in snapshot.children){
                    //data as model
                    val model = data.getValue(ContactModel::class.java)

                    // add to array
                    contactArray.add(model!!)
                }

                //set up adapter
                adapter = ContactsAdapter(this@ContactsRegisteredFragment.requireContext(), contactArray)
                binding.recycler.setHasFixedSize(true)
                binding.recycler.layoutManager = LinearLayoutManager(context,).apply {
                    reverseLayout = true
                    stackFromEnd = true
                }
                binding.recycler.adapter = adapter

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}