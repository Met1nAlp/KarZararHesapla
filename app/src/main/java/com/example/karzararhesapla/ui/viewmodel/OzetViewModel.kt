package com.example.karzararhesapla.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.data.OzetRepository
import com.example.karzararhesapla.entity.Ozet
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