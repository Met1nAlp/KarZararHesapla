package com.example.karzararhesapla.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.ui.viewmodel.KullaniciViewModel
import com.example.karzararhesapla.databinding.ActivityGirisSayfasiBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue

class GirisSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityGirisSayfasiBinding
    private val viewModel : KullaniciViewModel by viewModels()
    private val PREFS_NAME = "KarZararPrefs"
    private val KEY_REMEMBER_ME = "remember_me"
    private val KEY_EMAIL = "saved_email"
    private val KEY_PASSWORD = "saved_password"


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityGirisSayfasiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Check Remember Me
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        if (rememberMe) {
            val savedEmail = prefs.getString(KEY_EMAIL, "")
            val savedPassword = prefs.getString(KEY_PASSWORD, "")
            binding.etEmail.setText(savedEmail)
            binding.etPassword.setText(savedPassword)
            binding.cbRememberMe.isChecked = true
        }

        // Auto-login removed as per request
        /*
        if (FirebaseAuth.getInstance().currentUser != null)
        {
            startActivity(Intent(this , AnaSayfa::class.java))
            finish()
        }
        */

        viewModel.girisState.observe(this) { sonuc ->

            when(sonuc)
            {
                is Resource.Loading ->
                {
                    setLoadingState(true)
                }
                is Resource.Success ->
                {
                    // Handle Remember Me
                    val currentEmail = binding.etEmail.text.toString()
                    val currentPassword = binding.etPassword.text.toString()
                    val isRememberChecked = binding.cbRememberMe.isChecked

                    val editor = prefs.edit()
                    if (isRememberChecked) {
                        editor.putBoolean(KEY_REMEMBER_ME, true)
                        editor.putString(KEY_EMAIL, currentEmail)
                        editor.putString(KEY_PASSWORD, currentPassword)
                    } else {
                        editor.clear()
                    }
                    editor.apply()

                    Toast.makeText(this , "Giriş başarılı , Ana sayfaya yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this , AnaSayfa::class.java))
                    finish()
                }
                is Resource.Error ->
                {
                    setLoadingState(false)
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
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.etEmail.error = "Şifre sıfırlama bağlantısı için e-posta giriniz."
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            setLoadingState(true)
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    setLoadingState(false)
                    Toast.makeText(this, "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    setLoadingState(false)
                    Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "İşlem Yapılıyor..."
            binding.btnLogin.alpha = 0.7f
        } else {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Giriş Yap"
            binding.btnLogin.alpha = 1.0f
        }
    }
}