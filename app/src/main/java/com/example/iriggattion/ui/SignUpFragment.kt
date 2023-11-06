package com.example.iriggattion.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.iriggattion.R
import com.example.iriggattion.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class SignUpFragment : Fragment() {
    private lateinit var binding : FragmentSignUpBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }
    private val handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnBack.setOnClickListener {
            findNavController().apply {
                navigate(R.id.signInFragment) // Navigate to LoginFragment
            }
        }
        binding.btnSignUp.setOnClickListener {
            validateData()
        }
    }
    private var email = ""
    private var pass = ""
    private var name = ""
    private var phone = ""
    private var userType = "member"
    private fun validateData() {
        val email = binding.etEmailSignUp.text.toString().trim()
        val pass = binding.etPasswordSignUp.text.toString().trim()
        val name = binding.etFullname.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()


        when {
            email.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Email...", Toast.LENGTH_SHORT).show()
            pass.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Password...", Toast.LENGTH_SHORT).show()
            name.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your FullName...", Toast.LENGTH_SHORT).show()
            phone.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Phone Number...", Toast.LENGTH_SHORT).show()
            phone.length != 11 -> Toast.makeText(this.requireContext(), "Phone Number should be 11 digits...", Toast.LENGTH_SHORT).show()
            else -> createUserAccount()
        }
    }
    private fun createUserAccount() {
        val email = binding.etEmailSignUp.text.toString()
        val password = binding.etPasswordSignUp.text.toString()
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.createUserWithEmailAndPassword(email,password).await()
                withContext(Dispatchers.Main){
                    uploadInfo()
                }

            }
            catch (e : Exception){
                withContext(Dispatchers.Main){
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SignUpFragment.requireContext(),
                        "Failed Creating Account or ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
    private fun uploadInfo() {
        progressDialog.setMessage("Saving Account...")
        progressDialog.show()
        email = binding.etEmailSignUp.text.toString().trim()
        pass = binding.etPasswordSignUp.text.toString().trim()
        name = binding.etFullname.text.toString().trim()
        phone = binding.etPhoneNumber.text.toString().trim()
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()
        val uid = auth.uid
        val timestamp = System.currentTimeMillis()
        val hashMap : HashMap<String, Any?> = HashMap()

        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["password"] = pass
        hashMap["fullName"] = name
        hashMap["currentDate"] = currentDate
        hashMap["currentTime"] = currentTime
        hashMap["id"] = "$timestamp"
        hashMap["userType"] = "member"
        hashMap["phone"] = phone



        try {
            database.getReference("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(hashMap)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        uploadContact()
                        uploadContact2()
                    } else {
                        Toast.makeText(this.requireContext(), task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(this.requireContext(), "Error uploading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadContact() {
        progressDialog.setMessage("Saving Phone Number...")
        progressDialog.show()
        phone = binding.etPhoneNumber.text.toString().trim()
        name = binding.etFullname.text.toString().trim()
        val hashMap : HashMap<String, Any?> = HashMap()

        hashMap["phone"] = phone
        hashMap["fullName"] = name

        try {
            database.getReference("Contacts")
                .child(phone)
                .setValue(hashMap)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        progressDialog.dismiss()
                        findNavController().apply {
                            popBackStack(R.id.signUpFragment, false) // Pop all fragments up to HomeFragment
                            navigate(R.id.signInFragment) // Navigate to LoginFragment
                        }
                        Toast.makeText(this.requireContext(),"Account Created", Toast.LENGTH_SHORT).show()


                    } else {
                        Toast.makeText(this.requireContext(), task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(this.requireContext(), "Error uploading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadContact2() {
        phone = binding.etPhoneNumber.text.toString().trim()
        name = binding.etFullname.text.toString().trim()
        try {
            database.getReference("ContactsForNodemcu")
                .child(phone)
                .setValue(phone)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                    } else {
                    }
                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(this.requireContext(), "Error uploading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone("GMT+08:00")
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }


    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat(   "dd-MM-yyyy")
        return formatter.format(currentDateObject)
    }
}