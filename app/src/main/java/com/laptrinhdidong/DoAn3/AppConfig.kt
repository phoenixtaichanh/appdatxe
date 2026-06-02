package com.laptrinhdidong.DoAn3

import com.laptrinhdidong.DoAn3.data.remote.SocketManager

object AppConfig {
    const val BASE_URL = "http://172.26.56.65:3000/api/"

    init {
        SocketManager.init(BASE_URL)
    }
}
