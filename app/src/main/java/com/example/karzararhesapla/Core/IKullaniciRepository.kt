package com.example.karzararhesapla.Core

import com.example.karzararhesapla.Entity.Kullanicilar

interface IKullaniciRepository
{
    suspend fun bilgileriGetir(kullaniciID: String): Resource<Kullanicilar>

    suspend fun girisYap(ePosta : String , parola : String): Resource<String>

    suspend fun kayitOl(kullanici: Kullanicilar): Resource<String>

    suspend fun bilgileriGuncelle(kullanici : Kullanicilar): Resource<String>

}