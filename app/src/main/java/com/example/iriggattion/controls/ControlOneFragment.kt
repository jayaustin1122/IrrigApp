package com.example.iriggattion.controls

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentControlOneBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ControlOneFragment : Fragment() {
    private lateinit var binding: FragmentControlOneBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var isDefaultBackground = false
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentControlOneBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        startStatusChecking()
        sharedPreferences = requireActivity().getSharedPreferences("ButtonState", Context.MODE_PRIVATE)
        val savedState = sharedPreferences.getBoolean("isButtonOn", false)
        binding.btnPump.setBackgroundColor(if (savedState) R.color.mainColor else R.color.textColor)

        binding.btnPump.setOnClickListener {
            val pumpRef = database.getReference("TopPump")

            pumpRef.get().addOnSuccessListener { snapshot ->
                val currentValue = snapshot.value.toString()
                val newStatus = if (currentValue == "ON") {
                    "OFF"
                } else {
                    "ON"
                }

                pumpRef.setValue(newStatus)

                // Change the background color between red and the default color
                isDefaultBackground = !isDefaultBackground

                // Update the icon and stroke color
                val defaultColor = resources.getColor(R.color.textColor)
                val pressedColor = resources.getColor(R.color.mainColor)

                if (isDefaultBackground) {
                    // Change the background color to red
                    binding.btnPump.setBackgroundColor(defaultColor)
                } else {
                    // Change the background color to the default color
                    binding.btnPump.setBackgroundColor(pressedColor)
                }

                // Update the icon and stroke color
                binding.btnPump.compoundDrawableTintMode = PorterDuff.Mode.SRC_ATOP
                binding.btnPump.compoundDrawableTintList = ColorStateList.valueOf(if (isDefaultBackground) pressedColor else defaultColor)
                with(sharedPreferences.edit()) {
                    putBoolean("isButtonOn", isDefaultBackground)
                    apply()
                }
            }
        }
    }
    private val handler = Handler(Looper.getMainLooper())

    private val statusListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val status = dataSnapshot.value.toString()
            if (status == "Manual") {
                // Enable the button
                binding.btnPump.isEnabled = true
            } else if (status == "Automatic") {
                // Disable the button
                binding.btnPump.isEnabled = false
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val statusChecker = object : Runnable {
        override fun run() {
            val statusRef = database.getReference("State")
            statusRef.addListenerForSingleValueEvent(statusListener)

            // Schedule the next run after 1 second
            handler.postDelayed(this, 1000)
        }
    }

    private fun startStatusChecking() {
        handler.post(statusChecker)
    }

    private fun stopStatusChecking() {
        handler.removeCallbacks(statusChecker)
    }



    override fun onDestroyView() {
        super.onDestroyView()

        // Stop status checking when the view is destroyed
        stopStatusChecking()
    }
}

