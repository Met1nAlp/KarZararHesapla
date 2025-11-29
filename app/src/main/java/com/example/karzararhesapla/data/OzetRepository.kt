package com.example.karzararhesapla.data

import com.example.karzararhesapla.core.Constants
import com.example.karzararhesapla.core.IOzetRepository
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.entity.Ozet
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