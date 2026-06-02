package com.laptrinhdidong.DoAn3.data.remote

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

data class DriverLocationUpdate(
    val lat: Double,
    val lng: Double,
    val rideId: Int,
    val timestamp: Long
)

data class RideStatusUpdate(
    val rideId: Int,
    val status: String,
    val timestamp: Long
)

data class NewRideNotification(
    val rideId: Int,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val destAddress: String,
    val vehicleType: String,
    val distanceKm: Double,
    val durationMin: Int,
    val price: Double,
    val passengerName: String,
    val timestamp: Long
)

object SocketManager {
    private const val TAG = "SocketManager"

    private var socket: Socket? = null
    private var currentToken: String? = null
    private var baseUrl: String = ""

    private val _driverLocationFlow = MutableSharedFlow<DriverLocationUpdate>(replay = 1)
    val driverLocationFlow: SharedFlow<DriverLocationUpdate> = _driverLocationFlow

    private val _rideStatusFlow = MutableSharedFlow<RideStatusUpdate>(replay = 1)
    val rideStatusFlow: SharedFlow<RideStatusUpdate> = _rideStatusFlow

    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState

    private val _newRideFlow = MutableSharedFlow<NewRideNotification>(replay = 0)
    val newRideFlow: SharedFlow<NewRideNotification> = _newRideFlow

    fun init(baseUrl: String) {
        this.baseUrl = baseUrl.removeSuffix("/api/").removeSuffix("/")
    }

    fun connect(token: String) {
        if (socket?.connected() == true && currentToken == token) {
            Log.d(TAG, "Already connected")
            return
        }

        disconnect()

        currentToken = token

        val opts = IO.Options().apply {
            transports = arrayOf(WebSocket.NAME)
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
            reconnectionDelayMax = 5000
            auth = mapOf("token" to token)
            query = "token=$token"
        }

        try {
            socket = IO.socket(baseUrl, opts)
            setupListeners()
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}")
        }
    }

    private fun setupListeners() {
        socket?.let { s ->
            s.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to WebSocket")
                _connectionState.tryEmit(true)
            }

            s.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Disconnected from WebSocket")
                _connectionState.tryEmit(false)
            }

            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection error: ${args.contentToString()}")
                _connectionState.tryEmit(false)
            }

            s.on("driver:location") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val lat = data.getDouble("lat")
                        val lng = data.getDouble("lng")
                        val rideId = data.optInt("rideId", 0)
                        val timestamp = data.optLong("timestamp", System.currentTimeMillis())
                        _driverLocationFlow.tryEmit(DriverLocationUpdate(lat, lng, rideId, timestamp))
                        Log.d(TAG, "Driver location: $lat, $lng")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "driver:location parse error: ${e.message}")
                }
            }

            s.on("ride:status:changed") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val rideId = data.getInt("rideId")
                        val status = data.getString("status")
                        val timestamp = data.optLong("timestamp", System.currentTimeMillis())
                        _rideStatusFlow.tryEmit(RideStatusUpdate(rideId, status, timestamp))
                        Log.d(TAG, "Ride status changed: $rideId -> $status")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "ride:status:changed parse error: ${e.message}")
                }
            }

            s.on("new_ride") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val notification = NewRideNotification(
                            rideId = data.getInt("rideId"),
                            pickupLat = data.getDouble("pickupLat"),
                            pickupLng = data.getDouble("pickupLng"),
                            pickupAddress = data.optString("pickupAddress", ""),
                            destAddress = data.optString("destAddress", ""),
                            vehicleType = data.optString("vehicleType", "motorbike"),
                            distanceKm = data.optDouble("distanceKm", 0.0),
                            durationMin = data.optInt("durationMin", 0),
                            price = data.optDouble("price", 0.0),
                            passengerName = data.optString("passengerName", "Khach"),
                            timestamp = data.optLong("timestamp", System.currentTimeMillis())
                        )
                        _newRideFlow.tryEmit(notification)
                        Log.d(TAG, "New ride available: #${notification.rideId} - ${notification.pickupAddress}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "new_ride parse error: ${e.message}")
                }
            }
        }
    }

    fun emitLocationUpdate(lat: Double, lng: Double, rideId: Int) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Socket not connected, skipping location emit")
            return
        }

        val data = JSONObject().apply {
            put("lat", lat)
            put("lng", lng)
            put("rideId", rideId)
        }

        socket?.emit("location:update", data)
        Log.d(TAG, "Emitted location: $lat, $lng for ride $rideId")
    }

    fun joinRide(rideId: Int) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Socket not connected, cannot join ride")
            return
        }
        socket?.emit("join:ride", rideId)
        Log.d(TAG, "Joined ride room: $rideId")
    }

    fun leaveRide(rideId: Int) {
        if (socket?.connected() != true) return
        socket?.emit("leave:ride", rideId)
        Log.d(TAG, "Left ride room: $rideId")
    }

    fun requestDriverLocation(rideId: Int) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Socket not connected, cannot request driver location")
            return
        }
        val data = JSONObject().apply { put("rideId", rideId) }
        socket?.emit("request:driver:location", data)
        Log.d(TAG, "Requested driver location for ride: $rideId")
    }

    fun emitRideStatus(rideId: Int, status: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Socket not connected, cannot emit ride status")
            return
        }
        val data = JSONObject().apply {
            put("rideId", rideId)
            put("status", status)
        }
        socket?.emit("ride:status", data)
        Log.d(TAG, "Emitted ride status: $rideId -> $status")
    }

    fun isConnected(): Boolean = socket?.connected() == true

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        currentToken = null
        _connectionState.tryEmit(false)
        Log.d(TAG, "Disconnected")
    }
}
