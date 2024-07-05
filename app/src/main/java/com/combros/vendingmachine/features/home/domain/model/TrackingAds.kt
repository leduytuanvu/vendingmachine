package com.combros.vendingmachine.features.home.domain.model

data class TrackingAds (
   var vendCode:String ="",
   var androidId:String ="",
   var timeStart:String ="",
   var timeEnd:String ="",
   var trackingAds: ArrayList<TrackingAdsDetail> = ArrayList()

)
data class TrackingAdsDetail (
   var adsName:String ="",
   var adsType:String ="",
   var impression:Int =0,

)