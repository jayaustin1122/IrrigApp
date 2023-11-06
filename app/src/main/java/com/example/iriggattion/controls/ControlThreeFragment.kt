package com.example.iriggattion.controls

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentControlThreeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ControlThreeFragment : Fragment() {
    private lateinit var binding : FragmentControlThreeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private var isDefaultBackground = false
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentControlThreeBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("ButtonState", Context.MODE_PRIVATE)
        val savedState = sharedPreferences.getBoolean("isButtonOn", false)
        binding.btnServo.setBackgroundColor(if (savedState) R.color.mainColor else R.color.textColor)

        binding.btnServo.setOnClickListener {
            val pumpRef = database.getReference("Servo")

            pumpRef.get().addOnSuccessListener { snapshot ->
                val currentValue = snapshot.value.toString()
                val newStatus = if (currentValue == "OFF") {
                    "ON"
                } else {
                    "OFF"
                }

                pumpRef.setValue(newStatus)
                // Change the background color between red and the default color
                isDefaultBackground = !isDefaultBackground

                // Update the icon and stroke color
                val defaultColor = resources.getColor(R.color.textColor)
                val pressedColor = resources.getColor(R.color.mainColor)

                if (isDefaultBackground) {
                    // Change the background color to red
                    binding.btnServo.setBackgroundColor(defaultColor)
                } else {
                    // Change the background color to the default color
                    binding.btnServo.setBackgroundColor(pressedColor)
                }

                // Update the icon and stroke color
                binding.btnServo.compoundDrawableTintMode = PorterDuff.Mode.SRC_ATOP
                binding.btnServo.compoundDrawableTintList = ColorStateList.valueOf(if (isDefaultBackground) pressedColor else defaultColor)
                with(sharedPreferences.edit()) {
                    putBoolean("isButtonOn", isDefaultBackground)
                    apply()
                }
            }

        }
    }
}