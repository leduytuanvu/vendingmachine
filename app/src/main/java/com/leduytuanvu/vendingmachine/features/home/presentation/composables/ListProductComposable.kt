package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.leduytuanvu.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState


@Composable
fun ListProductComposable(
    state: HomeViewState,
    viewModel: HomeViewModel,
    onTurnOnClick: () -> Unit
) {


    val chunkedList = state.listSlot.chunked(3)

    Box (modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
            items(chunkedList.size) { index ->
                val chunk = chunkedList[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    chunk.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            onClick = {}
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Gray
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                ) {
                                    Text(
                                        text = item.productCode,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

//        if (!homeVendingMachineViewModel.isAdsVisible.value) {
//            VendingMachineButtonComposable(
//                onClick = { onTurnOnClick() },
//                text = "Turn on"
//            )
//        }
//
//        VendingMachineButtonComposable(
//            onClick = {  },
//            text = "50.000 vnd"
//        )
    }
}