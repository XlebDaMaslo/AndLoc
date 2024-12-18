package com.example.test

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager

class CellInfoAct(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @SuppressLint("MissingPermission")
    fun getRsrp(): Int? {
        val cellInfo = telephonyManager.allCellInfo

        val cellInfoLte = cellInfo?.find { it is CellInfoLte } as? CellInfoLte

        return cellInfoLte?.cellSignalStrength?.rsrp
    }
}