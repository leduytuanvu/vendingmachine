package com.combros.vendingmachine.core.util

class ByteArrays {
    // Vending machine
    val vmPollStatus = byteArrayOf(0x02, 0x00, 0x04, 0x00, 0x00, 0x9E.toByte(), 0x00, 0x03, 0xB9.toByte(), 0xE1.toByte())
    val vmTurnOnLight = byteArrayOf(0x00, 0xFF.toByte(), 0xDD.toByte(), 0x22, 0xAA.toByte(),0x55)
    val vmTurnOffLight = byteArrayOf(0x00, 0xFF.toByte(), 0xDD.toByte(), 0x22, 0x55, 0xAA.toByte())
    val vmCheckDropSensor = byteArrayOf(0x00, 0xFF.toByte(), 0x64.toByte(), 0x9B.toByte(), 0xAA.toByte(), 0x55.toByte())
    val vmCheckDropSensor2 = byteArrayOf(0x00, 0xFE.toByte(), 0x64.toByte(), 0x9B.toByte(), 0x55.toByte(), 0xAA.toByte())
    val vmReadTemp = byteArrayOf(0x00, 0xFF.toByte(), 0xDC.toByte(), 0x23.toByte(), 0x55.toByte(), 0xAA.toByte())
    val vmReadDoor = byteArrayOf(0x00, 0xFF.toByte(), 0xDF.toByte(), 0x20.toByte(), 0x55.toByte(), 0xAA.toByte())
    val vmInchingMode0 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x00, 0xFF.toByte())
    val vmInchingMode1 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x01.toByte(), 0xFE.toByte())
    val vmInchingMode2 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x02.toByte(), 0xFD.toByte())
    val vmInchingMode3 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x03.toByte(), 0xFC.toByte())
    val vmInchingMode4 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x04.toByte(), 0xFB.toByte())
    val vmInchingMode5 = byteArrayOf(0x00, 0xFF.toByte(), 0xE6.toByte(), 0x19.toByte(), 0x05.toByte(), 0xFA.toByte())
    val vmTurnOnGlassHeatingMode = byteArrayOf(0x00, 0xFF.toByte(), 0xD4.toByte(), 0x2B.toByte(), 0x01.toByte(), 0xFE.toByte())
    val vmTurnOffGlassHeatingMode = byteArrayOf(0x00, 0xFF.toByte(), 0xD4.toByte(), 0x2B.toByte(), 0x00, 0xFF.toByte())

    // Control temp
    val vmSetTemp1 = byteArrayOf(0x00, 0xFF.toByte(), 0xCC.toByte(), 0x33.toByte(), 0x01, 0xFE.toByte())
    // Cool
    val vmSetTemp2 = byteArrayOf(0x00, 0xFF.toByte(), 0xCD.toByte(), 0x32.toByte(), 0x01, 0xFE.toByte())
    // Set temp
    val vmSetTemp3 = byteArrayOf(0x00, 0xFF.toByte(), 0xCE.toByte(), 0x31.toByte(), 0x05, 0xFA.toByte())

    val checkPort = byteArrayOf(0xFA.toByte(), 0xFB.toByte(), 0x41.toByte(), 0x00.toByte(), 0x40.toByte())

    fun vmTurnOffGlassHeatingMode(value: Int): ByteArray {
        val byteArraySlot: Byte = (value + 120).toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                0x00,
                0xFF.toByte(),
                byteArraySlot,
                (0x86 - (value - 1)).toByte(),
                0x55,
                0xAA.toByte(),
            )
        return byteArray
    }
    val vmDrop1 = byteArrayOf(0x00, 0xFF.toByte(), 0x01, 0xFE.toByte(), 0xAA.toByte(), 0x55)
    val vmDrop11 = byteArrayOf(0x01, 0xFE.toByte(), 0x02, 0xFD.toByte(), 0x55, 0xAA.toByte())

    // Cash box
    val cbPollStatus = byteArrayOf(0x03, 0x00, 0x01, 0x00, 0x19, 0x1B.toByte())
    val cbTransferToCashBox = byteArrayOf(0x03, 0x00, 0x01, 0x00, 0x1F.toByte(), 0x1D.toByte())
    val cbReset = byteArrayOf(0x03, 0x00, 0x01, 0x00, 0x0A.toByte(), 0x08)
    val cbEnableType3456789 = byteArrayOf(0x03, 0x02, 0x01, 0x00, 0x15, 0x01, 0xFC.toByte(), 0xE8.toByte())
    val cbDisable = byteArrayOf(0x03, 0x02, 0x01, 0x00, 0x15, 0x00, 0x00, 0x15.toByte())
    val cbGetBillType = byteArrayOf(0x03, 0x00, 0x01, 0x00, 0x16.toByte(), 0x14.toByte())
    val cbEscrowOn = byteArrayOf(0x03.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x11.toByte(), 0xFF.toByte(), 0xED.toByte())
    val cbStack = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1A.toByte(), 0x11, 0x08)
    val cbReject = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1A.toByte(), 0x22.toByte(), 0x3B.toByte())
    val cbSetRecyclingBillType4 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x17, 0x04, 0x10)
    val cbGetNumberRottenBoxBalance = byteArrayOf(0x03, 0x00, 0x01, 0x00, 0x1B.toByte(), 0x19)
    val setDrop2 = byteArrayOf(0x00.toByte(), 0xFF.toByte(), 0xE9.toByte(), 0x16.toByte(), 0x02.toByte(), 0xFD.toByte())
    val setDrop3 = byteArrayOf(0x00.toByte(), 0xFF.toByte(), 0xE9.toByte(), 0x16.toByte(), 0x03.toByte(), 0xFC.toByte())
    val setDrop5 = byteArrayOf(0x00.toByte(), 0xFF.toByte(), 0xE9.toByte(), 0x16.toByte(), 0x05.toByte(), 0xFA.toByte())
    val checkCheck2 = byteArrayOf(0x01.toByte(), 0xFE.toByte(), 0x02.toByte(), 0xFD.toByte(), 0xAA.toByte(), 0x55.toByte())


    val cbDispenseBill1 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill2 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x02, 0x1D.toByte())
    val cbDispenseBill3 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x03, 0x1C.toByte())
    val cbDispenseBill4 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x04, 0x1B.toByte())
    val cbDispenseBill5 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x05, 0x1A.toByte())
    val cbDispenseBill6 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x06, 0x19)
    val cbDispenseBill7 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x07, 0x18)
    val cbDispenseBill8 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x08, 0x17)
    val cbDispenseBill9 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x09, 0x16)
    val cbDispenseBill10 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0A.toByte(), 0x15)
    val cbDispenseBill11 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0B.toByte(), 0x14)
    val cbDispenseBill12 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0C.toByte(), 0x13)
    val cbDispenseBill13 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0D.toByte(), 0x12)
    val cbDispenseBill14 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0E.toByte(), 0x11)
    val cbDispenseBill15 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x0F.toByte(), 0x10)
    val cbDispenseBill16 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill17 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill18 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill19 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill20 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill21 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill22 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill23 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill24 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill25 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill26 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill27 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill28 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill29 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill30 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill31 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill32 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill33 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill34 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
    val cbDispenseBill35 = byteArrayOf(0x03, 0x01, 0x01, 0x00, 0x1C.toByte(), 0x01, 0x1E.toByte())
}