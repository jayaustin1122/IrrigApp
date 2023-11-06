package com.example.iriggattion.admin

import android.app.ProgressDialog
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iriggattion.adapter.WaterLevelAdapter
import com.example.iriggattion.databinding.FragmentLogsBinding
import com.example.iriggattion.model.WaterLevelModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class LogsFragment : Fragment() {
    private lateinit var binding : FragmentLogsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    // array list to hold events
    private lateinit var waterLevelModel : ArrayList<WaterLevelModel>
    //adapter
    private lateinit var adapter : WaterLevelAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLogsBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        auth = FirebaseAuth.getInstance()
        getLogs()
        binding.btnGenerate.setOnClickListener {
            generatePdfReport(waterLevelModel)
        }
    }
    private fun generatePdfReport(logs: List<WaterLevelModel>) {
        progressDialog.setMessage("Saving PDF File...")
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val pdfDocument = PdfDocument()
            val timestamp = System.currentTimeMillis()
            val fileName = "report_$timestamp.pdf"
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            // Draw the logs on the page
            val paint = Paint()
            paint.textSize = 12f
            var yPosition = 50f
            for (log in logs) {
                canvas.drawText("Date: ${log.currentDate}", 50f, yPosition, paint)
                canvas.drawText("Level: ${log.status}", 50f, yPosition + 20f, paint)
                yPosition += 40f
            }
            pdfDocument.finishPage(page)
            // Save the document
            val filePath = requireContext().getExternalFilesDir(null)?.path + "/$fileName"
            val file = File(filePath)
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                Snackbar.make(
                    requireView(),
                    "Report downloaded successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun getLogs() {
        //initialize
        waterLevelModel = ArrayList()
        val dbRef = FirebaseDatabase.getInstance().getReference("LogsWaterLevel")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the fragment is still attached to an activity
                context?.let {
                    // clear list
                    waterLevelModel.clear()
                    for (data in snapshot.children){
                        //data as model
                        val model = data.getValue(WaterLevelModel::class.java)

                        // add to array
                        model?.let { waterLevelModel.add(it) }
                    }
                    //set up adapter
                    adapter = WaterLevelAdapter(requireContext(), waterLevelModel)
                    binding.recyclerView.setHasFixedSize(true)
                    binding.recyclerView.layoutManager = LinearLayoutManager(context,).apply {
                        reverseLayout = true
                        stackFromEnd = true
                    }
                    binding.recyclerView.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}