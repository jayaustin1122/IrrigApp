package com.example.iriggattion.ui

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashFragment : Fragment() {
    private lateinit var binding : FragmentSplashBinding
    lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        Handler().postDelayed({
                checkUser()
        },2000)
    }
    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
        }
        else{

            val dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = snapshot.child("userType").value

                        if (userType == "admin") {
                            Toast.makeText(this@SplashFragment.requireContext(), "Login Successfully", Toast.LENGTH_SHORT).show()
                            findNavController().apply {
                                popBackStack(R.id.splashFragment, false) // Pop all fragments up to HomeFragment
                                navigate(R.id.navigationFragment) // Navigate to LoginFragment
                            }

                        } else if (userType == "member") {
                            Toast.makeText(this@SplashFragment.requireContext(), "Login Successfully", Toast.LENGTH_SHORT).show()
                            findNavController().apply {
                                popBackStack(R.id.splashFragment, false) // Pop all fragments up to HomeFragment
                                navigate(R.id.userHomeFragment) // Navigate to LoginFragment
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }
    }

}