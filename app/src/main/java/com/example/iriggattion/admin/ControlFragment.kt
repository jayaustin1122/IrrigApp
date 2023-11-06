package com.example.iriggattion.admin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.iriggattion.R
import com.example.iriggattion.adapter.ViewPagerAdapter
import com.example.iriggattion.controls.ControlOneFragment
import com.example.iriggattion.controls.ControlTwoFragment
import com.example.iriggattion.controls.ControlThreeFragment
import com.example.iriggattion.databinding.FragmentControlBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class ControlFragment : Fragment() {
    private lateinit var binding: FragmentControlBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentControlBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(ControlOneFragment(), "Top Pump")
        adapter.addFragment(ControlTwoFragment(), "Bottom Pump")
        adapter.addFragment(ControlThreeFragment(), "Servo")

        binding.viewPager.adapter = adapter
        binding.tbLayout.setupWithViewPager(binding.viewPager)
        sharedPreferences = requireActivity().getSharedPreferences("ButtonState", Context.MODE_PRIVATE)
        val savedState = sharedPreferences.getBoolean("isButtonOn", false)

        val savedControlState = sharedPreferences.getString("controlState", "")
        binding.controlState.text = savedControlState ?: "Default State"

        binding.switchState.isChecked = !savedState
        binding.switchState.isChecked = savedState


        binding.switchState.setOnCheckedChangeListener { _, checkedId ->
            val pumpRef = database.getReference("State")
            val newStatus = if (checkedId) "Manual" else "Automatic"
            pumpRef.setValue(newStatus)
            binding.controlState.text = newStatus
            with(sharedPreferences.edit()) {
                putBoolean("isButtonOn", checkedId)
                putString("controlState", newStatus)
                apply()
            }
        }
    }

}
