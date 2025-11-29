package com.example.karzararhesapla.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.data.KullaniciRepository
import com.example.karzararhesapla.entity.Kullanicilar
import kotlinx.coroutines.launch

class KullaniciViewModel : ViewModel()
{
    private val repository = KullaniciRepository()

    private val _girisState = MutableLiveData<Resource<String>>()
    val girisState: LiveData<Resource<String>> = _girisState

    private val _kayitState = MutableLiveData<Resource<String>>()
    val kayitState: LiveData<Resource<String>> = _kayitState

    private val _kullaniciBilgiState = MutableLiveData<Resource<Kullanicilar>>()
    val kullaniciBilgiState: LiveData<Resource<Kullanicilar>> = _kullaniciBilgiState

    private val _guncellemeState = MutableLiveData<Resource<String>>()
    val guncellemeState: LiveData<Resource<String>> = _guncellemeState

    fun girisYap(email: String, sifre: String)
    {
        _girisState.value = Resource.Loading()
        viewModelScope.launch {
            _girisState.value = repository.girisYap(email, sifre)
        }
    }

    fun kayitOl(kullanici: Kullanicilar)
    {
        _kayitState.value = Resource.Loading()
        viewModelScope.launch {
            _kayitState.value = repository.kayitOl(kullanici)
        }
    }

    fun bilgileriGetir(uid: String)
    {
        _kullaniciBilgiState.value = Resource.Loading()
        viewModelScope.launch {
            _kullaniciBilgiState.value = repository.bilgileriGetir(uid)
        }
    }

    fun bilgileriGuncelle(kullanici: Kullanicilar)
    {
        _guncellemeState.value = Resource.Loading()
        viewModelScope.launch {
            _guncellemeState.value = repository.bilgileriGuncelle(kullanici)
        }
    }
}