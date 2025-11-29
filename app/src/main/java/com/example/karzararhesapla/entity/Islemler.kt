package com.example.karzararhesapla.entity

data class Islemler(
    var islemID: String = "",
    var kullaniciID: String = "",
    var islemTuru: String = "",
    var islemKategorisi: String = "",
    var islemTutari: Double = 0.0,
    var islemAciklamasi: String = "",
    var islemTarihi: Long = 0L
)
