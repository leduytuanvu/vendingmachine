package com.leduytuanvu.vendingmachine.features.home.presentation.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFilePaymentMethod
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.pathFolderAds
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.home.domain.repository.HomeRepository
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class HomeViewModel @Inject constructor (
    private val homeRepository: HomeRepository,
    private val baseRepository: BaseRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val byteArrays: ByteArrays,
    private val context: Context,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    private var countdownTimer: CountDownTimer? = null

    private var debounceDelay = 100L
    private var debounceJob: Job? = null

    private val collectJobs = mutableListOf<Job>()

    init {
        logger.debug("init homeviewmodel")
        loadInitData()
        observePortData()
        initLoad()
    }

    fun initLoad() {
        logger.debug("initLoad")
        viewModelScope.launch {
            try {
                portConnectionDatasource.openPortCashBox(_state.value.initSetup!!.portCashBox)
                portConnectionDatasource.startReadingCashBox()
                portConnectionDatasource.openPortVendingMachine(_state.value.initSetup!!.portVendingMachine)
                portConnectionDatasource.startReadingVendingMachine()
                sendCommandCashBox(byteArrays.cbEnableType3456789)
                delay(250)
                sendCommandCashBox(byteArrays.cbSetRecyclingBillType4)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendCommandCashBox(byteArray: ByteArray) {
        logger.debug("pollStatus")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(byteArray)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

//    private fun observePortData() {
//        viewModelScope.launch {
//            portConnectionDatasource.dataFromCashBox.collect { data ->
//                // Handle cash box data
//                processDataFromCashBox(data)
////                _state.update { currentState ->
////                    currentState.copy(cashBoxData = data)
////                }
//            }
//        }
//
//        viewModelScope.launch {
//            portConnectionDatasource.dataFromVendingMachine.collect { data ->
//                // Handle vending machine data
////                Logger.info("HomeViewModel: collected vending machine data is $data")
////                _state.update { currentState ->
////                    currentState.copy(vendingMachineData = data)
////                }
//            }
//        }
//    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        logger.debug("HomeViewModel is being stopped")

        // Cancel all collection jobs
        collectJobs.forEach { it.cancel() }

        // Close any open connections or cleanup resources
        portConnectionDatasource.closeVendingMachinePort()
        portConnectionDatasource.closeCashBoxPort()
    }

    private fun observePortData() {
        val cashBoxJob = viewModelScope.launch {
            portConnectionDatasource.dataFromCashBox.collect { data ->
                // Handle cash box data
                processDataFromCashBox(data)
            }
        }
        collectJobs.add(cashBoxJob)

        val vendingMachineJob = viewModelScope.launch {
            portConnectionDatasource.dataFromVendingMachine.collect { data ->
                // Handle vending machine data
                // Logger.info("HomeViewModel: collected vending machine data is $data")
            }
        }
        collectJobs.add(vendingMachineJob)
    }

    private fun processDataFromCashBox(data: ByteArray) {
        try {
            Logger.info("HomeViewModel: collected cash box data is ${byteArrayToHexString(data)}")
            // Ensure the byte array has at least 8 elements before accessing
            if (data.size == 19) {
                if (data[6] == 0x00.toByte()) {
                    when (data[7]) {
                        0x03.toByte() -> processingCash(5000)
                        0x04.toByte() -> processingCash(10000)
                        0x05.toByte() -> processingCash(20000)
                        0x06.toByte() -> processingCash(50000)
                        0x07.toByte() -> processingCash(100000)
                        0x08.toByte() -> processingCash(200000)
                        0x09.toByte() -> processingCash(500000)
                    }
                }
            }

            // Update the state
//            _state.update { currentState ->
//                currentState.copy(cashBoxData = byteArrayData)
//            }

        } catch (e: Exception) {
            Logger.error("Error processing cash box data: ${e.message}", e)
        }
    }

    private fun processingCash(cash: Int) {
        viewModelScope.launch {
            try {
                logger.debug("cash = $cash")
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.currentCash += cash
                baseRepository.writeDataToLocal(
                    data = initSetup,
                    path = pathFileInitSetup,
                )
                portConnectionDatasource.sendCommandCashBox(byteArrays.cbStack)
                _state.update { it.copy(initSetup = initSetup) }
            } catch (e: Exception) {
                logger.debug("error: ${e.message}")
            }
        }

    }


    private fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(",") { "%02X".format(it) }
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        return hexString.split(",")
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

//    private fun startReadingThread() {
//        readThreadCashBox.start()
//    }
//
//    // Read thread cash box
//    private val readThreadCashBox = object : Thread() {
//        override fun run() {
//            Logger.info("PortConnectionDataSource: start read thread cash box")
//            while (!currentThread().isInterrupted) {
//                try {
//                    portConnectionHelperDatasource.startReadingCashBox(512) { data ->
//                        val dataHexString = byteArrayToHexString(data)
//                        Logger.info("PortConnectionDataSource: data is $dataHexString")
//                        viewModelScope.launch {
//                            _cashBoxData.emit(dataHexString)
//                        }
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    break
//                }
//            }
//        }
//    }
//
//    private fun byteArrayToHexString(byteArray: ByteArray): String {
//        return byteArray.joinToString(",") { "%02X".format(it) }
//    }

    fun loadInitData() {
        logger.info("loadInitData")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                // Get list method payment
                val listPaymentMethod: ArrayList<PaymentMethodResponse> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<PaymentMethodResponse>>() {}.type,
                    path = pathFilePaymentMethod
                )!!
                // Get list path ads
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
                // Get list slot in local
                val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
                    path = pathFileSlot
                )!!
                // Get list slot in home
                val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
                for(item in listSlot) {
                    if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
                        val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
                        if (index == -1) {
                            listSlotShowInHome.add(item)
                        } else {
                            listSlotShowInHome[index].inventory += item.inventory
                        }
                    }
                }
                logger.debug("init:++++ ${initSetup.timeoutPaymentByCash.toLong() * 1000} ")
                _state.update {
                    it.copy(
                        initSetup = initSetup,
                        listAds = listAds,
                        listSlot = listSlot,
                        listSlotInHome = listSlotShowInHome,
                        listPaymentMethod = listPaymentMethod,
                        countDownPaymentByCash = initSetup.timeoutPaymentByCash.toLong(),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load init data fail in HomeViewModel/loadInitData(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

//    fun getListPathAdsFromLocal() {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//                delay(5000)
//                var listAds = homeRepository.getListVideoAdsFromLocal()
//                if (listAds.isEmpty()) {
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads1,
//                        "ads1.mp4",
//                        pathFolderAds,
//                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads2,
//                        "ads2.mp4",
//                        pathFolderAds,
//                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads3,
//                        "ads3.mp4",
//                        pathFolderAds,
//                    )
//                    listAds = homeRepository.getListVideoAdsFromLocal()
//                }
//                _state.update { it.copy(
//                    listAds = listAds,
//                    isLoading = false,
//                ) }
//            } catch (e: Exception) {
//                logger.info("Error video ads: ${e.message}")
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }

    fun showAdsDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            showAds()
        }
    }

    fun showAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = true) }
        }
    }

    fun hideAdsDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            hideAds()
        }
    }

    fun hideAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = false) }
        }
    }

    fun showBigAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowBigAds = true) }
        }
    }

    fun hideBigAds() {
        logger.debug("hideAds")
        viewModelScope.launch {
            _state.update { it.copy(isShowBigAds = false) }
        }
    }

    fun backDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            backInCartPayment()
        }
    }

    fun backInCartPayment() {
        viewModelScope.launch {
            _state.update { it.copy(isPayment = false) }
        }
    }

    fun backInPayment() {
        viewModelScope.launch {
            countdownTimer?.cancel()
            countdownTimer = null
            _state.update { it.copy(isPaymentConfirmation = false) }
        }
    }

    fun chooseAnotherMethodPayment() {
        viewModelScope.launch {
            countdownTimer?.cancel()
            countdownTimer = null
            _state.update { it.copy(
                isPayment = true,
                isPaymentConfirmation = false
            ) }
        }
    }

    fun showPaymentDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            showPayment()
        }
    }

    fun showPayment() {
        viewModelScope.launch {
            _state.update { it.copy (
                isPayment = true,
                isLoading = true
            ) }
            try {
                val totalAmount = homeRepository.getTotalAmount(_state.value.listSlotInCard)
                if(baseRepository.isHaveNetwork(context)) {
                    if(_state.value.initSetup!!.initPromotion == "ON") {
                        val promotion = homeRepository.getPromotion(
                            voucherCode = _state.value.voucherCode,
                            listSlot = _state.value.listSlotInCard,
                        )
                        _state.update { it.copy (
                            promotion = promotion,
                            totalAmount = totalAmount,
                            isLoading = false,
                        ) }
                    } else {
                        _state.update { it.copy (
                            totalAmount = totalAmount,
                            isLoading = false,
                        ) }
                    }
                } else {
                    _state.update { it.copy (
                        totalAmount = totalAmount,
                        isLoading = false,
                    ) }
                }

            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "get promotion fail HomeViewModel/showPayment(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy (
                    isLoading = false,
                ) }
            }
        }
    }

    fun updateNameMethod(nameMethod: String) {
        viewModelScope.launch {
            _state.update { it.copy (nameMethodPayment = nameMethod) }
        }
    }

    fun pollStatus() {
        logger.debug("pollStatus")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getTotalAmount() {
        viewModelScope.launch {
            try {
                val totalAmount = homeRepository.getTotalAmount(_state.value.listSlotInCard)
                _state.update { it.copy (totalAmount = totalAmount) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "get total amount fail in HomeViewModel/getTotalAmount(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun addProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            addProduct(slot)
        }
    }

    fun addProduct(slot: Slot) {
        logger.info("addProduct")
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    val listSlotInCart = ArrayList(currentState.listSlotInCard)
                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                    if (index == -1) {
                        val tmpSlot = slot.copy(inventory = 1)
                        listSlotInCart.add(tmpSlot)
                    } else {
                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                        listSlotInCart[index] = updatedSlot
                    }
                    currentState.copy(
                        listSlotInCard = listSlotInCart,
                        slotAtBottom = listSlotInCart.lastOrNull()
                    )
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "add product fail in HomeViewModel/addProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun minusProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            minusProduct(slot)
        }
    }

    fun minusProduct(slot: Slot) {
        logger.info("minusProduct")
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    var check = false
                    val listSlotInCart = ArrayList(currentState.listSlotInCard)
                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                    if (index != -1) {
                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory - 1)
                        if (updatedSlot.inventory == 0) {
                            listSlotInCart.removeAt(index)
                            check = true
                        } else {
                            listSlotInCart[index] = updatedSlot
                        }
                    }
                    if(listSlotInCart.isEmpty()) {
                        currentState.copy(
                            listSlotInCard = listSlotInCart,
                            slotAtBottom = listSlotInCart.lastOrNull(),
                            isPayment = false,
                            totalAmount = 0,
                        )
                    } else {
                        var total = 0
                        for(item in listSlotInCart) {
                            total+=(item.inventory*item.price)
                        }
                        currentState.copy(
                            listSlotInCard = listSlotInCart,
                            slotAtBottom = if(check) listSlotInCart.lastOrNull() else listSlotInCart[index],
                            totalAmount = total,
                        )
                    }
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "minus product fail in HomeViewModel/minusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun getPromotion(voucherCode: String = "") {
        logger.info("getPromotion")
        viewModelScope.launch {
            try {
                val promotionResponse = homeRepository.getPromotion(
                    voucherCode = voucherCode,
                    listSlot = _state.value.listSlotInCard,
                )
                logger.debug("promotion response: $promotionResponse")
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "minus product fail in HomeViewModel/minusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun applyPromotion(voucherCode: String = "") {
        logger.info("applyPromotion")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                if(baseRepository.isHaveNetwork(context)) {
                    val promotionResponse = homeRepository.getPromotion(
                        voucherCode = voucherCode,
                        listSlot = _state.value.listSlotInCard,
                    )
                    logger.debug("promotion response: $promotionResponse")
                    _state.update { it.copy(
                        promotion = promotionResponse,
                        isLoading = false,
                    ) }
                } else {
                    sendEvent(Event.Toast("Not have internet, please connect with internet!"))
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "minus product fail in HomeViewModel/minusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun paymentConfirmation() {
        logger.info("paymentConfirmation")
        viewModelScope.launch {
            try {
                _state.update { it.copy(countDownPaymentByCash = (_state.value.initSetup!!.timeoutPaymentByCash.toLong())) }
                when(_state.value.nameMethodPayment) {
                    "cash" -> {
                        logger.debug("method payment: cash")
                        val initSetup: InitSetup = baseRepository.getDataFromLocal(
                            type = object : TypeToken<InitSetup>() {}.type,
                            path = pathFileInitSetup
                        )!!
                        if(initSetup.currentCash >= _state.value.totalAmount) {

                        } else {
                            _state.update { it.copy(
                                isPaymentConfirmation = true,
                                isPayment = false,
                            ) }
                            startCountdownPaymentByCash()
                        }
                    }
                    "momo" -> {
                        logger.debug("method payment: momo")
                    }
                    "vnpay" -> {
                        logger.debug("method payment: vnpay")
                    }
                    "zalopay" -> {
                        logger.debug("method payment: zalopay")
                    }
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "payment confirmation fail in HomeViewModel/paymentConfirmation(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startCountdownPaymentByCash() {
        countdownTimer?.cancel() // Cancel any existing timer
        countdownTimer = object : CountDownTimer((_state.value.initSetup!!.timeoutPaymentByCash.toLong()*1000), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _state.update { it.copy(countDownPaymentByCash = (millisUntilFinished / 1000).toLong()) }
                if(_state.value.initSetup!!.currentCash >= _state.value.totalAmount) {

                }
            }

            override fun onFinish() {
                _state.update { it.copy(countDownPaymentByCash = 0) }
                // Handle countdown finish, e.g., cancel payment
                cancelPayment()
            }
        }.start()
    }

    private fun cancelPayment() {
        // Handle payment cancellation
        _state.update { it.copy(isPaymentConfirmation = false) }
        sendEvent(Event.Toast("Payment cancelled due to timeout"))
    }

    fun getInventoryByProductCode(productCode: String): Int {
        val listSlotInCart = _state.value.listSlotInCard
        val index = listSlotInCart.indexOfFirst { it.productCode == productCode }
        return if (index == -1) {
            -1
        } else {
            listSlotInCart[index].inventory
        }
    }

    fun getTotal(): Int {
        val listSlotInCart = _state.value.listSlotInCard
        var total = 0
        for(item in listSlotInCart) {
            total+=(item.inventory*item.price)
        }
        return total
    }

//
//    fun getInventoryOfProduct(productCode: String): Int {
//        val listSlotShowInHome = _state.value.listSlotInHome
//        val index = listSlotShowInHome.indexOfFirst { it.productCode == productCode }
//        return if (index == -1) {
//            -1
//        } else {
//            listSlotShowInHome[index].inventory
//        }
//    }

    fun plusProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            plusProduct(slot)
        }
    }

    fun plusProduct(slot: Slot) {
        logger.info("plusProduct")
        viewModelScope.launch {
            try {
                val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                val indexSlotBuy = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                val listSlotShowInHome = ArrayList(_state.value.listSlotInHome)
                val indexSlotShowInHome = listSlotShowInHome.indexOfFirst { it.productCode == slot.productCode }
                if (indexSlotBuy != -1 && indexSlotShowInHome != -1 && listSlotInCart[indexSlotBuy].inventory < listSlotShowInHome[indexSlotShowInHome].inventory) {
                    _state.update { currentState ->
                        val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                        if (index != -1) {
                            val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                            listSlotInCart[index] = updatedSlot
                        }
                        var total = 0
                        for(item in listSlotInCart) {
                            total+=(item.inventory*item.price)
                        }
                        currentState.copy(
                            listSlotInCard = listSlotInCart,
                            slotAtBottom = listSlotInCart[index],
                            totalAmount = total,
                        )
                    }
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "plus product fail in HomeViewModel/plusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

//    fun paymentNow() {
//        viewModelScope.launch {
//            sendEvent(Event.NavigateToHomeScreen)
//        }
//    }
//
//    fun getListSlotFromLocal() {
//        logger.debug("getListSlotFromLocal")
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//                val listSlot: ArrayList<Slot>? = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
//                    path = pathFileSlot
//                )
//                if(listSlot.isNullOrEmpty()) {
//                    _state.update {
//                        it.copy(
//                            listSlot = arrayListOf(),
//                            listSlotInHome = arrayListOf(),
//                            isLoading = false,
//                        )
//                    }
//                } else {
//                    val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
//                    for(item in listSlot) {
//                        if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
//                            val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
//                            if (index == -1) {
//                                listSlotShowInHome.add(item)
//                            } else {
//                                listSlotShowInHome[index].inventory += item.inventory
//                            }
//                        }
//                    }
//                    _state.update {
//                        it.copy(
//                            listSlot = listSlot,
//                            listSlotInHome = listSlotShowInHome,
//                            isLoading = false,
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                val initSetup: InitSetup = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<InitSetup>() {}.type,
//                    path = pathFileInitSetup
//                )!!
//                val logError = LogError(
//                    machineCode = initSetup.vendCode,
//                    errorType = "application",
//                    errorContent = "get list slot from local fail in HomeViewModel/getListSlotFromLocal: ${e.message}",
//                    eventTime = LocalDateTime.now().toDateTimeString(),
//                )
//                baseRepository.addNewLogToLocal(
//                    eventType = "error",
//                    severity = "normal",
//                    eventData = logError,
//                )
//                sendEvent(Event.Toast("${e.message}"))
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }
}