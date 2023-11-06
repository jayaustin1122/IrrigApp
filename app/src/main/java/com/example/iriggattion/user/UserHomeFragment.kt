package com.example.iriggattion.user

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import com.example.iriggattion.R
import com.example.iriggattion.adapter.ViewPagerAdapter
import com.example.iriggattion.admin.ContactsRegisteredFragment
import com.example.iriggattion.admin.EmergencyContectsFragment
import com.example.iriggattion.admin.TOPIC
import com.example.iriggattion.databinding.FragmentUserHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging


class UserHomeFragment : Fragment() {
    private lateinit var binding : FragmentUserHomeBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserHomeBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        waterLevel()
        loadUsersInfo()

        val adapter= ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(EmergencyContectsFragment(),"Emergency Contacts")
        binding.viewPager.adapter=adapter
        binding.tbLayout.setupWithViewPager(binding.viewPager)
        val optionMap = binding.btnLogout
        val popUpMenu = PopupMenu(this.requireContext(),optionMap)
        popUpMenu.menuInflater.inflate(R.menu.settings_menu,popUpMenu.menu)
        popUpMenu.setOnMenuItemClickListener { menuItem ->
            change(menuItem.itemId)
            true
        }
        binding.btnLogout.setOnClickListener {
            popUpMenu.show()
        }

    }
    private fun change(itemId: Int) {
        when(itemId){
            R.id.navLogout -> {
                auth.signOut()
                findNavController().apply {
                    popBackStack(R.id.homeFragment, false) // Pop all fragments
                    navigate(R.id.signInFragment) // Navigate to LoginFragment
                }
            }
            R.id.navAboutApp -> {
                findNavController().apply {
                    navigate(R.id.aboutFragment) // Navigate to LoginFragment
                }
            }

        }
    }
    private fun loadUsersInfo() {
        //reference
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val fullname = "${snapshot.child("fullName").value}"

                    //set data
                    binding.userName.text = "Welcome $fullname"

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
    private fun waterLevel() {
        val database = FirebaseDatabase.getInstance()
        val waterLevelRef = database.getReference("WaterLevel")

        waterLevelRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val waterLevel = dataSnapshot.getValue(Int::class.java) ?: 0

                binding.waterlevelLabel.text = waterLevel.toString()

                val color = when {
                    waterLevel >= 80 -> {
                        Color.RED
                    }
                    waterLevel in 50..79 -> {
                        Color.parseColor("#FFA500") // Orange
                    }
                    else -> {
                        Color.GREEN
                    }
                }


                val initialColor = (binding.mainBG.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
                val colorAnimator2 = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    initialColor,
                    color
                )


                colorAnimator2.duration = 1000
                colorAnimator2.addUpdateListener { animator ->
                    binding.mainBG.setBackgroundColor(animator.animatedValue as Int)
                }
                colorAnimator2.start()

                val progressAnimator = ValueAnimator.ofInt(
                    binding.progressBarWaterLevel.progress,
                    waterLevel
                )
                progressAnimator.duration = 1000
                progressAnimator.addUpdateListener { animator ->
                    binding.progressBarWaterLevel.progress = animator.animatedValue as Int
                }
                progressAnimator.start()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
}