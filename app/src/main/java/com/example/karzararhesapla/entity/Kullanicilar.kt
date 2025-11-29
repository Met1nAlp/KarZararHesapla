package com.example.karzararhesapla.entity

data class Kullanicilar(
    var kullaniciID: String = "",
    var isimSoyisim: String = "",
    var telefonNo: String = "",
    var sirketBilgileri: String = "",
    var ePosta: String = "",
    var parola: String = "",
    var profilResmi: String = ""
)