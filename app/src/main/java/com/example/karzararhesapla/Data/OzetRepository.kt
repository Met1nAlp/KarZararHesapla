package com.example.karzararhesapla.Data

import com.example.karzararhesapla.Core.Constants
import com.example.karzararhesapla.Core.IOzetRepository
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Ozet
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class OzetRepository : IOzetRepository
{
    private val dbRef = FirebaseDatabase.getInstance().reference

    override suspend fun ozetiGetir(ozetID: String, kullaniciID: String): Resource<Ozet>
    {
        return try
        {
            val snapshot = dbRef.child(Constants.NODE_OZETLER)
                .child(kullaniciID)
                .child(ozetID)
                .get()
                .await()

            val ozet = snapshot.getValue(Ozet::class.java)

            if (ozet != null)
            {
                Resource.Success(ozet)
            }
            else
            {
                Resource.Error("Özet veri bulunamadı.")
            }
        }
        catch (e: Exception)
        {
            Resource.Error(e.message ?: "Veri çekilirken hata oluştu")
        }
    }
}