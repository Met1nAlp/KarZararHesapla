package com.example.karzararhesapla.Entity

data class Hedefler(
    var hedefID: String = "",
    var kullaniciID: String = "",
    var hedefAdi: String = "",
    var hedefKategori: String = "",
    var hedefMiktar: Int = 0,
    var hedefTutar: Double = 0.0,
    var hedefBaslangicTarihi: Long = 0L,
    var hedefBitisTarihi: Long = 0L,
    var tamamlandiMi: Boolean = false
)