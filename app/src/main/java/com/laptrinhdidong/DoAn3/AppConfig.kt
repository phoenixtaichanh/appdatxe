package com.laptrinhdidong.DoAn3

import com.laptrinhdidong.DoAn3.data.remote.SocketManager

object AppConfig {
    // IMPORTANT: Change this URL based on your testing environment
    //
    // FOR EMULATOR:     Use "http://10.0.2.2:3000/api/"
    // FOR REAL PHONE:   Use "http://<YOUR_COMPUTER_IP>:3000/api/"
    //                   Example: "http://192.168.1.100:3000/api/"
    // FOR NGROK:        Use the ngrok URL if your backend is exposed via ngrok
    //                   Example: "https://xxxx-xxxx.ngrok-free.app/api/"
    //
    // HOW TO FIND YOUR COMPUTER IP:
    //   Windows: Open CMD -> type "ipconfig" -> look for "IPv4 Address"
    //   Your phone must be on the same WiFi network as your computer.
    //
    // ========== ANDROID EMULATOR ==========
    // Giữ nguyên nếu chạy trên Android Emulator
    const val BASE_URL = "http://10.0.2.2:3000/api/"

    // ========== ĐIỆN THOẠI THẬT (cùng mạng LAN) ==========
    // Thay "192.168.x.x" bằng IP thật của máy tính
    // Xem IP: Windows -> CMD -> ipconfig -> IPv4 Address
    // const val BASE_URL = "http://192.168.110.144:3000/api/"

    // ========== NGROK (khuyến nghị cho test) ==========
    // Nếu dùng ngrok để expose backend:
    // const val BASE_URL = "https://xxxx-xxxx.ngrok-free.app/api/"

    init {
        SocketManager.init(BASE_URL)
    }
}
