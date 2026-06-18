package com.laptrinhdidong.DoAn3

import com.laptrinhdidong.DoAn3.data.remote.SocketManager

object AppConfig {
    const val BASE_URL = "http://10.0.2.2:3000/api/"

    init {
        SocketManager.init(BASE_URL)
    }
}
