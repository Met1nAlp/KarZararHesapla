package com.example.karzararhesapla.Data

import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Core.IHedefRepository
import com.example.karzararhesapla.Entity.Hedefler
import com.example.karzararhesapla.Core.Constants
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class HedefRepository : IHedefRepository
{

    private val dbRef = FirebaseDatabase.getInstance().reference

    override suspend fun hedefleriGetir(kullaniciID: String): Resource<List<Hedefler>>
    {
        return try
        {
            val snapshot = dbRef.child(Constants.NODE_HEDEFLER)
                .child(kullaniciID)
                .get()
                .await()

            val liste = mutableListOf<Hedefler>()

            for (veri in snapshot.children)
            {
                val hedef = veri.getValue(Hedefler::class.java)
                hedef?.let { liste.add(it) }
            }

            Resource.Success(liste)
        }
        catch (e: Exception)
        {
            Resource.Error(e.message ?: "Hata oluştu")
        }
    }

    override suspend fun hedefEkle(hedefler: Hedefler): Resource<String>
    {
        return try
        {
            val yol = dbRef.child(Constants.NODE_HEDEFLER).child(hedefler.kullaniciID)

            val yeniKey = yol.push().key ?: return Resource.Error("Key oluşturulamadı")
            hedefler.hedefID = yeniKey

            yol.child(yeniKey).setValue(hedefler).await()

            Resource.Success(yeniKey)
        }
        catch (e: Exception)
        {
            Resource.Error(e.message ?: "Hata oluştu")
        }
    }

    override suspend fun hedefSil(hedefID: String, kullaniciID: String): Resource<String>
    {
        return try
        {
            dbRef.child(Constants.NODE_HEDEFLER)
                .child(kullaniciID)
                .child(hedefID)
                .removeValue()
                .await()

            Resource.Success("Hedef silindi")
        }
        catch (e: Exception)
        {
            Resource.Error(e.message ?: "Silme işlemi yapılamadı")
        }
    }

    override suspend fun hedefGuncelle(hedefler: Hedefler): Resource<String>
    {
        return try
        {
            dbRef.child(Constants.NODE_HEDEFLER)
                .child(hedefler.kullaniciID)
                .child(hedefler.hedefID)
                .setValue(hedefler)
                .await()

            Resource.Success("Hedef güncellendi")
        }
        catch (e: Exception)
        {
            Resource.Error(e.message ?: "Güncelleme işlemi yapılamadı")
        }
    }
}
