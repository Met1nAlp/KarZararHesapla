package com.example.karzararhesapla.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Kullanicilar
import com.example.karzararhesapla.UI.ViewModel.KullaniciViewModel
import com.example.karzararhesapla.databinding.ActivityUyeOlSayfasiBinding

class UyeOlSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityUyeOlSayfasiBinding
    private val viewModel : KullaniciViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityUyeOlSayfasiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()

        viewModel.kayitState.observe(this){ sonuc ->

            when(sonuc)
            {
                is Resource.Loading ->
                {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Yükleniyor"
                }
                is Resource.Success ->
                {
                    Toast.makeText(this , "Kayıt başarılı, Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this , GirisSayfasi::class.java))
                    finish()
                }
                is Resource.Error ->
                {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Kayıt Ol ve Başla"
                    Toast.makeText(this , sonuc.message , Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.btnRegister.setOnClickListener {

            val isim = binding.etNameSurname.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val tel = binding.etPhone.text.toString().trim()
            val sirket = binding.etCompany.text.toString().trim()

            if ( isim.isEmpty())
            {
                binding.etNameSurname.error = "Lütfen isminizi giriniz."
                binding.etNameSurname.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty())
            {
                binding.etRegEmail.error = "Lütfen E-Posta adresinizi giriniz"
                binding.etRegEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty())
            {
                binding.etRegPassword.error = "Lütfen parolanızı giriniz"
                binding.etRegPassword.requestFocus()
                return@setOnClickListener
            }
            if (tel.isEmpty())
            {
                binding.etPhone.error = "Lütfen telefon numaranızı giriniz"
                binding.etPhone.requestFocus()
                return@setOnClickListener
            }
            if (sirket.isEmpty())
            {
                binding.etCompany.error = "Lütfen şirketinizi giriniz"
                binding.etCompany.requestFocus()
                return@setOnClickListener
            }
            if (!binding.cbTerms.isChecked)
            {
                binding.cbTerms.error = "Lütfen koşulları kabul ediniz"
                binding.cbTerms.requestFocus()
                return@setOnClickListener
            }

            val yeniKullanici = Kullanicilar(
                isimSoyisim = isim,
                ePosta = email,
                parola = password,
                telefonNo = tel,
                sirketBilgileri = sirket
            )
            viewModel.kayitOl(yeniKullanici)
        }

    }
}