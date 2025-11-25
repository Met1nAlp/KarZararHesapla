package com.example.karzararhesapla.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Data.IslemRepository
import com.example.karzararhesapla.Entity.Islemler
import kotlinx.coroutines.launch

class IslemViewModel : ViewModel()
{
    private val repository = IslemRepository()

    private val _islemListesi = MutableLiveData<Resource<List<Islemler>>>()
    val islemListesi: LiveData<Resource<List<Islemler>>> = _islemListesi

    private val _islemIslemDurumu = MutableLiveData<Resource<Boolean>>()
    val islemIslemDurumu: LiveData<Resource<Boolean>> = _islemIslemDurumu

    private val _ozetGelir = MutableLiveData<Double>(0.0)
    val ozetGelir: LiveData<Double> = _ozetGelir

    private val _ozetGider = MutableLiveData<Double>(0.0)
    val ozetGider: LiveData<Double> = _ozetGider

    private val _ozetNetKar = MutableLiveData<Double>(0.0)
    val ozetNetKar: LiveData<Double> = _ozetNetKar

    fun islemleriGetir(uid: String)
    {
        _islemListesi.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.islemleriGetir(uid)
            _islemListesi.value = result

            if (result is Resource.Success)
            {
                hesapla(result.data ?: emptyList())
            }
        }
    }

    private fun hesapla(liste: List<Islemler>)
    {
        var gelir = 0.0
        var gider = 0.0

        liste.forEach { islem ->
            if (islem.islemTuru == "SATIS" || islem.islemTuru == "GELIR")
            {
                gelir += islem.islemTutari
            }
            else
            {
                gider += islem.islemTutari
            }
        }

        _ozetGelir.value = gelir
        _ozetGider.value = gider
        _ozetNetKar.value = gelir - gider
    }

    fun islemEkle(islem: Islemler)
    {
        _islemIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _islemIslemDurumu.value = repository.islemEkle(islem)
        }
    }

    fun islemSil(islemID: String, uid: String)
    {
        _islemIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _islemIslemDurumu.value = repository.islemSil(islemID, uid)
        }
    }

    fun islemGuncelle(islem: Islemler)
    {
        _islemIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _islemIslemDurumu.value = repository.islemGuncelle(islem)
        }
    }
}