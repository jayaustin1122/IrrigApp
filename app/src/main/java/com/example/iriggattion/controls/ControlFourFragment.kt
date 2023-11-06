package com.example.iriggattion.controls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentControlFourBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ControlFourFragment : Fragment() {
    private lateinit var binding: FragmentControlFourBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentControlFourBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.switchState.setOnCheckedChangeListener { _, isChecked ->
            val pumpRef = database.getReference("State")
            val newStatus = if (isChecked) "Automatic" else "Manual"
            pumpRef.setValue(newStatus)
            binding.switchState.text = "State: $newStatus"
        }
    }
}
