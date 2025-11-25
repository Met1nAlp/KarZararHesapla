package com.example.karzararhesapla.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Data.HedefRepository
import com.example.karzararhesapla.Entity.Hedefler
import kotlinx.coroutines.launch

class HedefViewModel : ViewModel()
{
    private val repository = HedefRepository()

    private val _hedefListesi = MutableLiveData<Resource<List<Hedefler>>>()
    val hedefListesi: LiveData<Resource<List<Hedefler>>> = _hedefListesi

    private val _hedefIslemDurumu = MutableLiveData<Resource<String>>()
    val hedefIslemDurumu: LiveData<Resource<String>> = _hedefIslemDurumu

    fun hedefleriGetir(uid: String)
    {
        _hedefListesi.value = Resource.Loading()
        viewModelScope.launch {
            _hedefListesi.value = repository.hedefleriGetir(uid)
        }
    }

    fun hedefEkle(hedef: Hedefler)
    {
        _hedefIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _hedefIslemDurumu.value = repository.hedefEkle(hedef)
        }
    }

    fun hedefSil(hedefID: String, uid: String)
    {
        _hedefIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _hedefIslemDurumu.value = repository.hedefSil(hedefID, uid)
        }
    }

    fun hedefGuncelle(hedef: Hedefler)
    {
        _hedefIslemDurumu.value = Resource.Loading()
        viewModelScope.launch {
            _hedefIslemDurumu.value = repository.hedefGuncelle(hedef)
        }
    }
}