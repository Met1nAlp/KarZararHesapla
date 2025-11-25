package com.example.karzararhesapla.Core

import com.example.karzararhesapla.Entity.Islemler

interface IIslemRepository
{
    suspend fun islemleriGetir(kullaniciID: String): Resource<List<Islemler>>

    suspend fun islemEkle( islem : Islemler): Resource<Boolean>

    suspend fun islemSil (islemID: String , kullaniciID: String) : Resource<Boolean>

    suspend fun islemGuncelle( islem : Islemler): Resource<Boolean>
}