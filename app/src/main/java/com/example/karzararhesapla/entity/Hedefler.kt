package com.example.karzararhesapla.entity

data class Hedefler(
    var hedefID: String = "",
    var kullaniciID: String = "",
    var hedefAdi: String = "",
    var hedefKategori: String = "",
    var hedefMiktar: Int = 0,
    var hedefTutar: Double = 0.0,
    var hedefBaslangicTarihi: Long = 0L,
    var hedefBitisTarihi: Long = 0L,
    var tamamlandiMi: Boolean = false,
    var tamamlananMiktar: Int = 0,
    var tamamlananTutar: Double = 0.0
)