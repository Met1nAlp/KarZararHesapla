package com.example.karzararhesapla.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Data.OzetRepository
import com.example.karzararhesapla.Entity.Ozet
import kotlinx.coroutines.launch

class OzetViewModel : ViewModel()
{
    private val repository = OzetRepository()

    private val _ozetVerisi = MutableLiveData<Resource<Ozet>>()
    val ozetVerisi: LiveData<Resource<Ozet>> = _ozetVerisi

    fun ozetiGetir(ozetID: String, uid: String)
    {
        _ozetVerisi.value = Resource.Loading()
        viewModelScope.launch {
            _ozetVerisi.value = repository.ozetiGetir(ozetID, uid)
        }
    }
}