package com.example.karzararhesapla.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.entity.Kullanicilar
import com.example.karzararhesapla.ui.viewmodel.KullaniciViewModel
import com.example.karzararhesapla.databinding.ActivityUyeOlSayfasiBinding
import java.io.ByteArrayOutputStream

class UyeOlSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityUyeOlSayfasiBinding
    private val viewModel : KullaniciViewModel by viewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageBase64: String = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityUyeOlSayfasiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, imageUri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        }
                        
                        binding.imgProfilePlaceholder.setImageBitmap(bitmap)
                        selectedImageBase64 = encodeImageToBase64(bitmap)
                        
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Resim seçilirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.imgProfilePlaceholder.setOnClickListener {
            selectImage()
        }
        
        binding.tvAddPhoto.setOnClickListener {
            selectImage()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }

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
                sirketBilgileri = sirket,
                profilResmi = selectedImageBase64
            )
            viewModel.kayitOl(yeniKullanici)
        }

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Compress to reduce size (e.g., 50% quality)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}