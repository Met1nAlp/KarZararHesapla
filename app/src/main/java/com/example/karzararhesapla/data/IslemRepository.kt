package com.example.karzararhesapla.data

import com.example.karzararhesapla.core.Constants
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.core.IIslemRepository
import com.example.karzararhesapla.entity.Islemler
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class IslemRepository : IIslemRepository
{
    private val dbRef = FirebaseDatabase.getInstance().reference

    override suspend fun islemleriGetir(kullaniciID: String): Resource<List<Islemler>>
    {
        return try
        {
            if (kullaniciID.isEmpty()) return Resource.Error("Geçersiz Kullanıcı ID")

            val snapshot = dbRef.child(Constants.NODE_ISLEMLER)
                .child(kullaniciID)
                .get().await()

            val liste = mutableListOf<Islemler>()

            for (veri in snapshot.children)
            {
                val islem = veri.getValue(Islemler::class.java)
                islem?.let { liste.add(it) }
            }

            Resource.Success(liste)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("İşlemler yüklenirken hata oluştu: ${e.localizedMessage}")
        }
    }

    override suspend fun islemEkle(islem: Islemler): Resource<Boolean>
    {
        return try
        {
            if (islem.kullaniciID.isEmpty()) return Resource.Error("Kullanıcı bilgisi eksik")

            val yol = dbRef.child(Constants.NODE_ISLEMLER).child(islem.kullaniciID)
            val yeniKey = yol.push().key ?: return Resource.Error("İşlem ID oluşturulamadı")

            islem.islemID = yeniKey

            yol.child(yeniKey).setValue(islem).await()
            Resource.Success(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("İşlem eklenemedi: ${e.localizedMessage}")
        }
    }

    override suspend fun islemSil(islemID: String, kullaniciID: String): Resource<Boolean>
    {
        return try
        {
            if (islemID.isEmpty() || kullaniciID.isEmpty()) return Resource.Error("Silinecek işlem bulunamadı")

            dbRef.child(Constants.NODE_ISLEMLER)
                .child(kullaniciID)
                .child(islemID)
                .removeValue().await()

            Resource.Success(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Silme işlemi başarısız: ${e.localizedMessage}")
        }
    }

    override suspend fun islemGuncelle(islem: Islemler): Resource<Boolean>
    {
        return try
        {
            if (islem.islemID.isEmpty() || islem.kullaniciID.isEmpty()) return Resource.Error("Güncellenecek işlem bulunamadı")

            dbRef.child(Constants.NODE_ISLEMLER)
                .child(islem.kullaniciID)
                .child(islem.islemID)
                .setValue(islem).await()
            Resource.Success(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Güncelleme başarısız: ${e.localizedMessage}")
        }
    }
}