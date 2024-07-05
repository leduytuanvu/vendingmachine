package com.combros.vendingmachine.features.home.domain.model

import java.util.Arrays
enum class TypeAds{BigAds,HomeAds,Unknown}
data class DataTrackingAds(
   var data: ArrayList<TrackingAds> = arrayListOf()
)
data class TrackingAds (
   var vendCode:String = "",
   var androidId: String = "",
   var timeStart: String = "",
   var timeEnd: String ="",
   var listAds: ArrayList<Ads> = arrayListOf()
)

data class Ads(
   var adsName: String = "",
   var adsType: String = "",
   var impression: Int = 0,
)