
package com.example.loginapp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


class WifiCredentialActivity : AppCompatActivity() {

    private lateinit var deviceRasp: BluetoothDevice
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    lateinit var bAdapter: BluetoothAdapter
    private val MY_PERMISSIONS_REQUEST_BLUETOOTH = 123
    private val uuid: UUID = UUID.fromString("815425a5-bfac-47bf-9321-c5ff980b5e11") // SPP için standart UUID
    private var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_credential)

        bAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bAdapter == null) {
            Log.i("TAG", "bAdapter value is null!")
        } else {
            Log.i("TAG", "bAdapter value is NOT NULLL!")
        }

        val pairedBtn: Button = findViewById(R.id.pairedBtn)
        val getssid : TextView = findViewById(R.id.ssid)
        val getpsk : TextView = findViewById(R.id.psk)


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH),
                MY_PERMISSIONS_REQUEST_BLUETOOTH)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                MY_PERMISSIONS_REQUEST_BLUETOOTH)
        }

        val deviceAddress = "B8:27:EB:C4:46:C5"
        deviceRasp = bAdapter.getRemoteDevice(deviceAddress)

        pairedBtn.setOnClickListener {
            if(bAdapter.isEnabled){
            }
            try {
                val ssid = getssid.text.toString()
                val psk = getpsk.text.toString()
                Log.i("TAG", ssid)
                Log.i("TAG", psk)
                bluetoothSocket = deviceRasp.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                inputStream = bluetoothSocket?.inputStream
                Toast.makeText(this, "Connected to ${deviceRasp.name}", Toast.LENGTH_SHORT).show()
                outputStream?.write(ssid.toByteArray())
                outputStream?.flush()
                //outputStream?.write(parser.toByteArray(ß))
                // PSK'yi gönder
                waitForResponse()
                outputStream?.write(psk.toByteArray())
                outputStream?.flush()


            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun waitForResponse() {
        try {
            val buffer = ByteArray(1024)
            val bytes: Int = inputStream?.read(buffer) ?: -1
            val response = String(buffer, 0, bytes)



        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to receive response", Toast.LENGTH_SHORT).show()
        }
    }
}