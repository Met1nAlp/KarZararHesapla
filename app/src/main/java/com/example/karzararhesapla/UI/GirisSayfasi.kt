package com.example.karzararhesapla.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.UI.ViewModel.KullaniciViewModel
import com.example.karzararhesapla.databinding.ActivityGirisSayfasiBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue

class GirisSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityGirisSayfasiBinding
    private val viewModel : KullaniciViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityGirisSayfasiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()

        if (FirebaseAuth.getInstance().currentUser != null)
        {
            startActivity(Intent(this , AnaSayfa::class.java))
            finish()
        }

        viewModel.girisState.observe(this) { sonuc ->

            when(sonuc)
            {
                is Resource.Loading ->
                {
                    setLoadingState(true)
                }
                is Resource.Success ->
                {
                    Toast.makeText(this , "Giriş başarılı , Ana sayfaya yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this , AnaSayfa::class.java))
                    finish()
                }
                is Resource.Error ->
                {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Giriş Yap"
                    Toast.makeText(this , sonuc.message , Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLogin.setOnClickListener {

            val ePosta = binding.etEmail.text?.toString().orEmpty().trim()
            val parola = binding.etPassword.text?.toString().orEmpty().trim()


            if (ePosta.isEmpty())
            {
                binding.etEmail.error = "Lütfen E-Posta adresinizi giriniz."
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (parola.isEmpty())
            {
                binding.etPassword.error = "Lütfen parolanızı giriniz."
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            viewModel.girisYap(ePosta, parola)

        }

        binding.tvUyeOlAction.setOnClickListener {
            val intent = Intent(this, UyeOlSayfasi::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this , "Bu özellik yakında eklenecek" , Toast.LENGTH_SHORT).show()
        }

    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Giriş Yapılıyor..."
            binding.btnLogin.alpha = 0.7f
        } else {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Giriş Yap"
            binding.btnLogin.alpha = 1.0f
        }
    }
}