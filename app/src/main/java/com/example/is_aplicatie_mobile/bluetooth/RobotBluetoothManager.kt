package com.example.is_aplicatie_mobile.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.OutputStream
import java.util.UUID

class RobotBluetoothManager {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val RASPBERRY_MAC = "DC:A6:32:D4:A7:FC"

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    var lastError: String = ""
        private set

    // Adăugat SuppressLint pentru a elimina avertismentul de Deprecate din Java
    @Suppress("DEPRECATION")
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingPermission")
    fun conecteaza(): Boolean {
        if (adapter == null || !adapter.isEnabled) return false

        return try {
            val pairedDevices = adapter.bondedDevices

            // Încearcă mai întâi după MAC-ul cunoscut al Raspberry Pi
            val device = pairedDevices.find { it.address == RASPBERRY_MAC }
                // Dacă nu găsește după MAC, caută după nume
                ?: pairedDevices.find {
                    it.name?.contains("raspberry", ignoreCase = true) == true ||
                    it.name?.contains("robot", ignoreCase = true) == true ||
                    it.name?.contains("bot", ignoreCase = true) == true ||
                    it.name?.contains("pi", ignoreCase = true) == true
                }

            if (device == null) {
                val lista = pairedDevices.joinToString(", ") { "${it.name} (${it.address})" }
                Log.e("BT", "Dispozitive împerecheate: $lista")
                lastError = if (lista.isEmpty()) "Niciun dispozitiv împerecheat găsit."
                            else "Dispozitive găsite: $lista"
                return false
            }

            Log.d("BT", "Conectare la: ${device.name} (${device.address})")
            adapter.cancelDiscovery()

            // Încearcă mai întâi cu UUID SPP standard
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket!!.connect()
            } catch (e1: Exception) {
                Log.w("BT", "SPP UUID eșuat, încerc canal direct RFCOMM 1: ${e1.message}")
                socket?.close()
                // Fallback: conectare directă pe canal RFCOMM 1 (ocolește SDP discovery)
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
                socket = method.invoke(device, 1) as BluetoothSocket
                socket!!.connect()
            }

            outputStream = socket!!.outputStream
            Log.d("BT", "Conectat cu succes la ${device.name}")
            true
        } catch (e: Exception) {
            lastError = "Eroare socket: ${e.message}"
            Log.e("BT", "Eroare conectare: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun trimiteMesaj(mesaj: String) {
        try {
            outputStream?.write((mesaj + "\n").toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deconecteaza() {
        trimiteMesaj("{\"actiune\":\"release\",\"directie\":\"STOP\"}")
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}