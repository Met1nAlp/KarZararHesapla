package com.example.karzararhesapla.core

import com.example.karzararhesapla.entity.Ozet

interface IOzetRepository
{
    suspend fun ozetiGetir(ozetID: String , kullaniciID: String ): Resource<Ozet>
}