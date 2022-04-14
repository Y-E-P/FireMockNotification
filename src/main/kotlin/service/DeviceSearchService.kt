package service

import repo.Device
import utils.AdbCommunicate
import utils.parseDevicesList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object DeviceSearchService {

    private const val INTERVAL_DURATION: Long = 1000
    private var executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var working = false
    private var justStart = false
    var onDevicesListChanged: (@Synchronized (List<Device>) -> Unit)? = null

    @Synchronized
    fun startMonitoringDevices() {
        working = true
        executor.execute(monitorRunnable)
    }

    @Synchronized
    fun stopMonitoringDevices() {
        working = false
        onDevicesListChanged?.invoke(emptyList())
        justStart = true
    }

    @Synchronized
    fun shutDown() {
        onDevicesListChanged = null
        executor.shutdownNow()
    }

    var monitorRunnable = Runnable {
        while (working) {
            fun onSuccess(data: String) {
                val split = data.split("\n")
                    .toTypedArray()
                    .map { it.replace("\r", "") }
                    .filter { it.isNotEmpty() }
                val devices: MutableList<Device> = ArrayList()
                for (i in 1 until split.size) {
                    val line = split[i]
                    devices.add(parseDevicesList(line))
                }
                onDevicesListChanged?.invoke(devices)
                try {
                    Thread.sleep(INTERVAL_DURATION)
                } catch (e: InterruptedException) {
                }
            }
            AdbCommunicate().execute("adb devices -l") { out ->
                when (out) {
                    is AdbCommunicate.ConsoleOutput.Success -> {
                        onSuccess(out.data)
                    }
                    is AdbCommunicate.ConsoleOutput.Error -> {}
                }
            }
        }
    }
}