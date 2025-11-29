package com.example.karzararhesapla.core

import com.example.karzararhesapla.entity.Hedefler

interface IHedefRepository
{
    suspend fun hedefleriGetir(kullaniciID: String): Resource<List<Hedefler>>

    suspend fun hedefEkle(hedefler: Hedefler) : Resource<String>

    suspend fun hedefGuncelle(hedefler: Hedefler) : Resource<String>

    suspend fun hedefSil(hedefID: String , kullaniciID: String): Resource<String>
}
