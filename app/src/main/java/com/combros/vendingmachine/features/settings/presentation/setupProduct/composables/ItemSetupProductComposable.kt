package com.combros.vendingmachine.features.settings.presentation.setupProduct.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.combros.vendingmachine.R
import com.combros.vendingmachine.core.util.toVietNamDong
import com.combros.vendingmachine.features.settings.domain.model.Product
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale

@Composable
fun ItemSetupProductComposable(product: Product) {
    Box(
        modifier = Modifier
            .border(
                width = 0.4.dp,
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = product.imageUrl)
                        .apply {
                            crossfade(true)
                            error(R.drawable.image_error)
                            scale(Scale.FILL)
                            size(160, 160)
                        }
                        .build()
                    )
            Image(
                painter = painter,
                contentDescription = product.productName,
                modifier = Modifier.height(160.dp).padding(bottom = 20.dp),
            )
            Text(
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                minLines = 2,
                maxLines = 2,
                text = product.productName,
            )
            Text(
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                text = "Price: ${product.price.toVietNamDong()}",
            )
        }
    }
}