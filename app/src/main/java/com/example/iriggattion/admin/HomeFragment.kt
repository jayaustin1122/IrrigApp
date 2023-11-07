package com.example.iriggattion.admin

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.iriggattion.R
import com.example.iriggattion.adapter.ViewPagerAdapter
import com.example.iriggattion.databinding.FragmentHomeBinding
import com.example.iriggattion.notification.NotificationData
import com.example.iriggattion.notification.PushNotification
import com.example.iriggattion.notification.RetrofitInstance
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

const val TOPIC = "/topics/allUsers"
class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private var waterLevel: Int = 0 // This is the variable you want to preserve


    // array list to hold events
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        waterLevel()
        val adapter= ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(EmergencyContectsFragment(),"Emergency Contacts")
        adapter.addFragment(ContactsRegisteredFragment(),"Contacts")

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

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
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Notification sent successfully")
            } else {
                Log.e(TAG, "Failed to send notification. Error: ${response.errorBody().toString()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.toString()}")
        }
    }
 //    Check if the permission is granted
    private fun hasSmsPermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), android.Manifest.permission.SEND_SMS)
    }
    private val SMS_PERMISSION_REQUEST_CODE = 123
    // Request the SMS permission
    private fun requestSmsPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This app needs permission to send SMS messages.",
            SMS_PERMISSION_REQUEST_CODE,
            android.Manifest.permission.SEND_SMS
        )
    }
    // Override onRequestPermissionsResult to handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }
    // Implement EasyPermissions.PermissionCallbacks interface
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            // Permission granted, you can now send SMS
            sendSmsToContacts()
        }
    }
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            // Permission denied, inform the user or handle it appropriately
            Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }



    private fun waterLevel() {
        val database = FirebaseDatabase.getInstance()
        val waterLevelRef = database.getReference("WaterLevel")

        waterLevelRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                waterLevel = dataSnapshot.getValue(Int::class.java) ?: 0

                binding.waterlevelLabel.text = waterLevel.toString()

                val color = when {
                    waterLevel >= 511 -> {
                        binding.waterLevelStatus.setText("High")
                        Glide.with(requireContext())
                            .load(R.drawable.alert_img)
                            .into(binding.imgOnOff1)
                        writeToFirebase("Irrigation Water level at High!",waterLevel)
                        requestSmsPermission()
                        // Send the notification
                        val notification = PushNotification(
                            NotificationData("Warning", "Water level is Dangerous"),
                            TOPIC
                        ).also {
                            sendNotification(it)
                        }
                        Color.RED


                    }
                    waterLevel in 390..510 -> {
                        binding.waterLevelStatus.setText("Medium")
                        writeToFirebase("Irrigation Gate Half Opened", waterLevel)
                        Color.parseColor("#FFA500") // Orange

                    }
                    else -> {
                        binding.waterLevelStatus.setText("Normal")
                        writeToFirebase("Irrigation Gate Normal", waterLevel)
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
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun sendSmsToContacts() {
        val database = FirebaseDatabase.getInstance()
        val contactsRef = database.getReference("Contacts")

        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (contactSnapshot in dataSnapshot.children) {
                    val phoneNumber = contactSnapshot.child("phone").getValue(String::class.java)
                    if (!phoneNumber.isNullOrEmpty()) {
                        sendSms(phoneNumber)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
    private fun sendSms(phoneNumber: String) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(phoneNumber,null,"Warning the Water Level Is Currently Dangerous",null,null)
        Toast.makeText(requireContext(), "Sms Send", Toast.LENGTH_SHORT).show()
    }

    fun writeToFirebase(status: String, waterLevel: Int) {
        val database = FirebaseDatabase.getInstance()
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()
        val timestamp = System.currentTimeMillis()
        val hashMap : HashMap<String, Any?> = HashMap()

        hashMap["status"] = status
        hashMap["currentDate"] = currentDate
        hashMap["currentTime"] = currentTime
        hashMap["id"] = "$timestamp"
        hashMap["waterLevel"] = waterLevel.toString()

        try {
            database.getReference("LogsWaterLevel")
                .child(timestamp.toString())
                .setValue(hashMap)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        Toast.makeText(this@HomeFragment.requireContext(), "Logs Updated", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(this.requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
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
