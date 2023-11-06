package com.example.iriggattion.model

class WaterLevelModel {
    var currentDate: String = ""
    var currentTime: String = ""
    var status: String = ""
    var id: String = ""
    var waterLevel: String = ""


    constructor(currentDate: String, currentTime: String, status: String,waterLevel:String, id: String) {
        this.currentDate = currentDate
        this.currentTime = currentTime
        this.status = status
        this.waterLevel = waterLevel
        this.id = id
    }
    constructor()
}