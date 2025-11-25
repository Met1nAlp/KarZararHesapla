package com.example.karzararhesapla.Data

import com.example.karzararhesapla.Core.Constants
import com.example.karzararhesapla.Core.IKullaniciRepository
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Kullanicilar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class KullaniciRepository : IKullaniciRepository
{
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference
    private val yol = dbRef.child(Constants.NODE_KULLANICILAR)

    override suspend fun bilgileriGetir(kullaniciID: String): Resource<Kullanicilar>
    {
        return try
        {
            if (kullaniciID.isEmpty()) return Resource.Error("Geçersiz Kullanıcı ID")

            val snapshot = yol.child(kullaniciID).get().await()
            val kullanici = snapshot.getValue(Kullanicilar::class.java)

            if (kullanici != null)
            {
                Resource.Success(kullanici)
            }
            else
            {
                Resource.Error("Kullanıcı bilgileri bulunamadı.")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Veri çekme hatası: ${e.localizedMessage}")
        }
    }

    override suspend fun girisYap(ePosta: String, parola: String): Resource<String>
    {
        return try
        {
            if (ePosta.isEmpty() || parola.isEmpty()) return Resource.Error("E-posta ve parola boş olamaz")

            val sonuc = auth.signInWithEmailAndPassword(ePosta, parola).await()
            val uid = sonuc.user?.uid

            if (uid != null)
            {
                Resource.Success(uid)
            }
            else
            {
                Resource.Error("Giriş yapıldı ancak kullanıcı ID alınamadı.")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Giriş başarısız: Lütfen bilgilerinizi kontrol edin.")
        }
    }

    override suspend fun kayitOl(kullanici: Kullanicilar): Resource<String>
    {
        return try
        {
            if (kullanici.ePosta.isEmpty() || kullanici.parola.isEmpty()) return Resource.Error("Eksik bilgi girdiniz.")

            val authSonuc = auth.createUserWithEmailAndPassword(kullanici.ePosta, kullanici.parola).await()
            val uid = authSonuc.user?.uid ?: return Resource.Error("Kullanıcı oluşturulamadı.")

            kullanici.kullaniciID = uid
            yol.child(uid).setValue(kullanici).await()

            Resource.Success(uid)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Kayıt işlemi başarısız: ${e.localizedMessage}")
        }
    }

    override suspend fun bilgileriGuncelle(kullanici: Kullanicilar): Resource<String>
    {
        return try
        {
            if (kullanici.kullaniciID.isNotEmpty())
            {
                yol.child(kullanici.kullaniciID).setValue(kullanici).await()
                Resource.Success("Bilgiler başarıyla güncellendi.")
            }
            else
            {
                Resource.Error("Güncellenecek kullanıcı bulunamadı.")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Resource.Error("Güncelleme sırasında hata oluştu: ${e.localizedMessage}")
        }
    }
}