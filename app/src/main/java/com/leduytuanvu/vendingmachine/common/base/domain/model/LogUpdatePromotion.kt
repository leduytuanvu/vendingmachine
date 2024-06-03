package com.leduytuanvu.vendingmachine.common.base.domain.model

data class LogUpdatePromotion (
    val machineCode: String,
    val androidId: String,
    val campaignId: String,
    val voucherCode: String,
    val orderCode: String,
    val promotionId: String,
    val status: Boolean,
    val extra: String,
    val isSent: Boolean,
)