package com.leduytuanvu.vendingmachine.features.home.presentation.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.pathFolderAds
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.home.domain.repository.HomeRepository
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class HomeViewModel @Inject constructor (
    private val homeRepository: HomeRepository,
    private val baseRepository: BaseRepository,
    private val context: Context,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        getListPathAdsFromLocal()
        getListSlotFromLocal()
    }

    fun getListPathAdsFromLocal() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                var listAds = homeRepository.getListVideoAdsFromLocal()
                if (listAds.isEmpty()) {
                    homeRepository.writeVideoAdsFromAssetToLocal(
                        context,
                        R.raw.ads1,
                        "ads1.mp4",
                        pathFolderAds,
                    )
                    homeRepository.writeVideoAdsFromAssetToLocal(
                        context,
                        R.raw.ads2,
                        "ads2.mp4",
                        pathFolderAds,
                    )
                    homeRepository.writeVideoAdsFromAssetToLocal(
                        context,
                        R.raw.ads3,
                        "ads3.mp4",
                        pathFolderAds,
                    )
                    listAds = homeRepository.getListVideoAdsFromLocal()
                }
                _state.update { it.copy(
                    listAds = listAds,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                logger.info("Error video ads: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = true) }
        }
    }

    fun hideAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = false) }
        }
    }

    fun addProduct(slot: Slot) {
        viewModelScope.launch {
            val listSlotBuy = _state.value.listSlotBuy
            if(listSlotBuy.isEmpty()) {
                slot.inventory = 1
                listSlotBuy.add(slot)
            } else {
                val index = listSlotBuy.indexOfFirst { it.productCode == slot.productCode }
                if (index == -1) {
                    listSlotBuy.add(slot)
                } else {
                    listSlotBuy[index].inventory++
                }
            }
            _state.update { it.copy (
                slot = slot,
                listSlotBuy = listSlotBuy,
                numberProduct = 1,
            ) }
        }
    }

    fun minusProduct(slot: Slot) {
        viewModelScope.launch {
            val listSlotBuy = _state.value.listSlotBuy
            val index = listSlotBuy.indexOfFirst { it.productCode == slot.productCode }
            listSlotBuy[index].inventory--
            if(listSlotBuy[index].inventory==0) {
                listSlotBuy.remove(listSlotBuy[index])
            }
            _state.update { it.copy (
                slot = slot,
                listSlotBuy = listSlotBuy,
                numberProduct = listSlotBuy[index].inventory,
            ) }
        }
    }

    fun plusProduct(slot: Slot) {
        viewModelScope.launch {
            val listSlotBuy = _state.value.listSlotBuy
            val index = listSlotBuy.indexOfFirst { it.productCode == slot.productCode }
            listSlotBuy[index].inventory++
            _state.update { it.copy (
                slot = slot,
                listSlotBuy = listSlotBuy,
                numberProduct = listSlotBuy[index].inventory,
            ) }
        }
    }

    fun paymentNow() {
        viewModelScope.launch {
            sendEvent(Event.NavigateToHomeScreen)
        }
    }


//    fun plusProduct() : Boolean {
//        viewModelScope.launch {
//
//        }
//    }


    fun getListSlotFromLocal() {
        logger.debug("getListSlotFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listSlot: ArrayList<Slot>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
                    path = pathFileSlot
                )
                if(listSlot.isNullOrEmpty()) {
                    _state.update {
                        it.copy(
                            listSlot = arrayListOf(),
                            listSlotShowInHome = arrayListOf(),
                            isLoading = false,
                        )
                    }
                } else {
                    val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
                    for(item in listSlot) {
                        if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock) {
                            val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
                            if (index == -1) {
                                listSlotShowInHome.add(item)
                            } else {
                                listSlotShowInHome[index].inventory += item.inventory
                            }
                        }
                    }
                    _state.update {
                        it.copy(
                            listSlot = listSlot,
                            listSlotShowInHome = listSlotShowInHome,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get slot from local fail in SetupSlotViewModel/getListSlotFromLocal: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}