package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.core.util.Screens

@Composable
fun InformationHomeComposable(
    navController: NavHostController,
    vendCode: String,
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(Color(0xFFCB1A17)),
        Arrangement.Center,
        Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier
            .weight(1f),
        ) {
            Column(
                modifier = Modifier,
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    Arrangement.Center,
                    Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .width(21.dp)
                            .height(21.dp)
                            .clickable { },
                        alignment = Alignment.TopEnd,
                        painter = painterResource(id = R.drawable.image_circle_phone),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "1900.99.99.80", color = Color.White, fontSize = 15.sp)
                }
                Text(text = vendCode, color = Color.White, fontSize = 13.sp)
            }
        }
        Box(modifier = Modifier
            .weight(1f),
            Alignment.Center,
        ) {
            Image(
                modifier = Modifier
                    .height(26.dp)
                    .clickable {
                        navController.navigate(Screens.SettingScreenRoute.route)
                    },
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.image_logo_avf),
                contentDescription = ""
            )
        }
        Box(modifier = Modifier
            .weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                Arrangement.Center,
                Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier.height(20.dp),
                    alignment = Alignment.Center,
                    painter = painterResource(id = R.drawable.image_flags),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Tiếng việt", color = Color.White)
            }
        }
    }
}