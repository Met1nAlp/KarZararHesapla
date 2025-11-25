package com.example.karzararhesapla.Entity

data class Ozet(
    var ozetID: String = "",
    var kullaniciID: String = "",
    var toplamSatisAdet: Int = 0,
    var toplamGider: Double = 0.0,
    var toplamSatisTutar: Double = 0.0,
    var netKar: Double = 0.0,
    var karZararYuzdesi: Double = 0.0
)