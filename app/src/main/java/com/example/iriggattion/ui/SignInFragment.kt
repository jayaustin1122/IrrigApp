package com.example.iriggattion.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInFragment : Fragment() {
    private lateinit var binding : FragmentSignInBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var backPressTime = 0L
    private var doubleBackToExitPressedOnce = false
    private val handler = Handler()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        binding.tvCreate.setOnClickListener {
            database.getReference("Register").setValue("True")
            findNavController().apply {
                navigate(R.id.signUpFragment) // Navigate to LoginFragment
            }
        }
        binding.btnLogin.setOnClickListener {
            validateData()
        }
    }
    var email = ""
    var pass = ""

    private fun validateData() {
        email = binding.etUsernameLogin.text.toString().trim()
        pass = binding.etPasswordLogin.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email
            Toast.makeText(this.requireContext(),"Email Invalid", Toast.LENGTH_SHORT).show()
        }
        else if (pass.isEmpty()){
            Toast.makeText(this.requireContext(),"Empty Fields are not allowed", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onPause() {
        callback.remove()
        super.onPause()
    }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                requireActivity().finish()
            } else {
                doubleBackToExitPressedOnce = true
                Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }
    private fun loginUser() {
        val email = binding.etUsernameLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()
        progressDialog.setMessage("Logging In...")
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithEmailAndPassword(email,password).await()
                withContext(Dispatchers.Main){
                    checkUser()
                }

            }
            catch (e : Exception){
                withContext(Dispatchers.Main){
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SignInFragment.requireContext(),
                        "Failed Creating Account or ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
    private fun checkUser() {
        progressDialog.setTitle("Checking user...")

        val firebaseUser = auth.currentUser!!

        val dbref = FirebaseDatabase.getInstance().getReference("Users")
        dbref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val userType = snapshot.child("userType").value

                    if (userType == "admin") {
                        Toast.makeText(this@SignInFragment.requireContext(), "Login Successfully", Toast.LENGTH_SHORT).show()
                        findNavController().apply {
                            popBackStack(R.id.splashFragment, false) // Pop all fragments up to HomeFragment
                            navigate(R.id.navigationFragment) // Navigate to LoginFragment
                        }

                    } else if (userType == "member") {
                        Toast.makeText(this@SignInFragment.requireContext(), "Login Successfully", Toast.LENGTH_SHORT).show()
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