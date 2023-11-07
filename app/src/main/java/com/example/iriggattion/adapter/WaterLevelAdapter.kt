package com.example.iriggattion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iriggattion.R
import com.example.iriggattion.model.WaterLevelModel
import com.example.iriggattion.databinding.LogsItemBinding

class WaterLevelAdapter: RecyclerView.Adapter<WaterLevelAdapter.ViewHolderWaterLevel> {
    private lateinit var binding : LogsItemBinding
    private val context : Context
    var waterLevelArrayList : ArrayList<WaterLevelModel>

    constructor(
        context: Context,
        waterLevelArrayList: ArrayList<WaterLevelModel>
    ) : super() {
        this.context = context
        this.waterLevelArrayList = waterLevelArrayList
    }
    inner class ViewHolderWaterLevel(itemView: View): RecyclerView.ViewHolder(itemView){
        var date : TextView = binding.date
        var time : TextView = binding.time
        var status : TextView = binding.tvState
        var waterLevel2 : TextView = binding.tvWaterlevel
        var image : ImageView = binding.alertImg
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderWaterLevel {
        binding = LogsItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolderWaterLevel(binding.root)
    }

    override fun getItemCount(): Int {
        return waterLevelArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderWaterLevel, position: Int) {
        //get data
        val model = waterLevelArrayList[position]
        val time = model.currentTime
        val date = model.currentDate
        val status = model.status
        val water = model.waterLevel

        holder.status.text = status
        holder.time.text = time
        holder.date.text = date
        holder.waterLevel2.text = water
        when{
            water.toInt() >= 511 ->{
                Glide.with(this@WaterLevelAdapter.context)
                    .load(R.drawable.alert_img)
                    .into(binding.alertImg)
            }
            water.toInt() in 390..510 ->{
                Glide.with(this@WaterLevelAdapter.context)
                    .load(R.drawable.error_yellow)
                    .into(binding.alertImg)
            }
            water.toInt() <= 389 ->{
                Glide.with(this@WaterLevelAdapter.context)
                    .load(R.drawable.level_img)
                    .into(binding.alertImg)
            }
        }

    }
}