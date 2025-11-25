package com.example.karzararhesapla.Core

import com.example.karzararhesapla.Entity.Ozet

interface IOzetRepository
{
    suspend fun ozetiGetir(ozetID: String , kullaniciID: String ): Resource<Ozet>
}