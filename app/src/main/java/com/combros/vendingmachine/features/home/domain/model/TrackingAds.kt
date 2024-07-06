package com.combros.vendingmachine.features.home.domain.model

import com.google.gson.annotations.SerializedName
import java.util.Arrays
enum class TypeAds{BigAds,HomeAds,Unknown}
data class DataTrackingAds(
    @SerializedName("event_id") var eventId: String,
    @SerializedName("event_type") var eventType: String,
    @SerializedName("severity") var severity: String,
    @SerializedName("event_time") var eventTime: String,
    @SerializedName("event_data") var data: TrackingAds,
    @Transient var isSent: Boolean,
)
data class TrackingAds (
  @SerializedName("vend_code") var vendCode:String = "",
  @SerializedName("android_id") var androidId: String = "",
  @SerializedName("time_start") var timeStart: String = "",
  @SerializedName("time_end") var timeEnd: String ="",
  @SerializedName("trackingAds") var listAds: ArrayList<Ads> = arrayListOf(),

)

data class Ads(
  @SerializedName("ads_name") var adsName: String = "",
  @SerializedName("ads_type") var adsType: String = "",
  @SerializedName("impression") var impression: Int = 0,
)