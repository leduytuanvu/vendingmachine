package com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogFill
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileProductDetail
import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewState.SetupProductViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SetupProductViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupProductViewState())
    val state = _state.asStateFlow()

    init {
        loadListProduct()
    }

    private fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    titleDialogWarning = mess,
                    isWarning = true,
                )
            }
        }
    }

    fun hideDialogWarning() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isWarning = false,
                    titleDialogWarning = "",
                )
            }
        }
    }

    fun showDialogConfirm(mess: String) {
        viewModelScope.launch {
            if(baseRepository.isHaveNetwork(context)) {
                _state.update {
                    it.copy(
                        titleDialogConfirm = mess,
                        isConfirm = true,
                    )
                }
            } else {
                showDialogWarning("Not have internet, please connect with internet!")
            }
        }
    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConfirm = false,
                    titleDialogConfirm = "",
                )
            }
        }
    }

    fun getListPriceProductFromServer() {
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listPriceOfProduct = settingsRepository.getListPriceOfProductFromServer()
                    _state.update { it.copy(
                        isLoading = false,
                        listPriceOfProduct = listPriceOfProduct,
                    ) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get list price of product from server fail in SetupProductViewModel/getListPriceProductFromServer(): ${e.message}"
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getListImageProductFromServer() {
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listPathImageOfProduct = settingsRepository.getListImageOfProductFromServer()
                    _state.update { it.copy(
                        isLoading = false,
                        listPathImageOfProduct = listPathImageOfProduct,
                    ) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get list image of product from server fail in SetupProductViewModel/getListImageProductFromServer(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadListProduct() {
        logger.debug("loadListProduct")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listProduct = settingsRepository.getListProductFromServer()
                    _state.update {
                        it.copy(
                            listProduct = listProduct,
                            isLoading = false,
                        )
                    }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get list product from server fail in SetupProductViewModel/loadInitListProduct(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun downloadListProductFromServerToLocal() {
        logger.debug("downloadListProductFromServerToLocal")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context = context)) {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            isConfirm = false,
                        )
                    }
                    val listFileNameInFolder =
                        settingsRepository.getListFileNameInFolder(pathFolderImageProduct)
                    if (!baseRepository.isFolderExists(pathFolderImageProduct)) {
                        baseRepository.createFolder(pathFolderImageProduct)
                    }
                    for (product in state.value.listProduct) {
                        if (product.imageUrl!!.isNotEmpty()) {
                            if (!listFileNameInFolder.contains(product.productCode)) {
                                var notHaveError = true
                                for (i in 1..3) {
                                    try {
                                        val request = ImageRequest.Builder(context = context)
                                            .data(product.imageUrl)
                                            .build()
                                        val result = withContext(Dispatchers.IO) {
                                            Coil.imageLoader(context).execute(request).drawable
                                        }
                                        if (result != null) {
                                            val file =
                                                File(pathFolderImageProduct, "${product.productCode}.png")
                                            withContext(Dispatchers.IO) {
                                                file.outputStream().use { outputStream ->
                                                    result.toBitmap().compress(
                                                        Bitmap.CompressFormat.PNG,
                                                        1,
                                                        outputStream
                                                    )
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        notHaveError = false
                                    } finally {
                                        if (notHaveError) break
                                    }
                                }
                            }
                        }
                    }
                    baseRepository.writeDataToLocal(
                        data = state.value.listProduct,
                        path = pathFileProductDetail,
                    )
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    baseRepository.addNewFillLogToLocal(
                        machineCode = initSetup.vendCode,
                        fillType = "download list product from server to local",
                        content = state.value.listProduct.toString(),
                    )
                    sendEvent(Event.Toast("SUCCESS"))
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "download list product from server to local fail in SetupProductViewModel/downloadListProductFromServerToLocal(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}