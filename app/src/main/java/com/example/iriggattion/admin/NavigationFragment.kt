package com.example.iriggattion.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class NavigationFragment : Fragment() {
    private lateinit var binding : FragmentNavigationBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNavigationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val homeFragment: Fragment = HomeFragment()
        val controlsFragment : Fragment = ControlFragment()
        val logsFragment: Fragment = LogsFragment()

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_controls -> ControlFragment()
                R.id.navigation_logs -> LogsFragment()
                else -> return@setOnNavigationItemSelectedListener false
            }

            try {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment, selectedFragment.tag)
                    .addToBackStack(selectedFragment.tag)
                    .commit()
            } catch (e: Exception) {
                // Handle any exceptions
                e.printStackTrace()
            }

            true
        }
        // Initially load the HomeFragment
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }
}