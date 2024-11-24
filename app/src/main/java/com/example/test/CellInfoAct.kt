package com.example.test.cellinfo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow

class CellInfoAct(private val context: Context) {
    val cellId = MutableStateFlow<Int?>(null)
    val lac = MutableStateFlow<Int?>(null)
    val rsrp = MutableStateFlow<Int?>(null)
    val signalStrength = MutableStateFlow<Int?>(null)
    val cellType = MutableStateFlow<String?>(null)


    fun getCellInfo() {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        val cellInfoList = telephonyManager.allCellInfo

        cellInfoList?.forEach { cellInfo ->
            when (cellInfo) {
                is CellInfoGsm -> {
                    cellId.value = cellInfo.cellIdentity.cid
                    lac.value = cellInfo.cellIdentity.lac
                    signalStrength.value = cellInfo.cellSignalStrength.dbm
                    rsrp.value = null // Not available for GSM
                    cellType.value = "GSM"
                }
                is CellInfoLte -> {
                    cellId.value = cellInfo.cellIdentity.ci
                    lac.value = cellInfo.cellIdentity.tac
                    rsrp.value = cellInfo.cellSignalStrength.rsrp
                    signalStrength.value = cellInfo.cellSignalStrength.level
                    cellType.value = "LTE"
                }
                else -> {
                    cellId.value = null
                    lac.value = null
                    rsrp.value = null
                    signalStrength.value = null
                    cellType.value = "Unknown"
                }
            }
        }
    }
}