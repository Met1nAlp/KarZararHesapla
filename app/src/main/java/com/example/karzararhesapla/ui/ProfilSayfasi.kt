package com.example.karzararhesapla.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.karzararhesapla.R
import com.example.karzararhesapla.ui.viewmodel.KullaniciViewModel
import com.example.karzararhesapla.ui.viewmodel.IslemViewModel
import com.example.karzararhesapla.ui.viewmodel.HedefViewModel
import com.example.karzararhesapla.databinding.ActivityProfilSayfasiBinding
import com.example.karzararhesapla.databinding.LayoutProfilGuncelleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

class ProfilSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityProfilSayfasiBinding
    private lateinit var guncelleDialogBinding: LayoutProfilGuncelleDialogBinding
    private val viewModel: KullaniciViewModel by viewModels()
    private val islemViewModel: IslemViewModel by viewModels()
    private val hedefViewModel: HedefViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private var mevcutKullanici: Kullanicilar? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilSayfasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        registerLauncher()
        setupListeners()
        setupObservers()

        val uid = auth.currentUser?.uid
        if (uid != null)
        {
            viewModel.bilgileriGetir(uid)
            islemViewModel.islemleriGetir(uid)
            hedefViewModel.hedefleriGetir(uid)
        }
    }

    private fun registerLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    try {
                        if (imageData != null) {
                            val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this.contentResolver, imageData)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(this.contentResolver, imageData)
                            }
                            binding.imgProfile.setImageBitmap(bitmap)
                            
                            val base64Image = encodeImageToBase64(bitmap)
                            mevcutKullanici?.let {
                                it.profilResmi = base64Image
                                viewModel.bilgileriGuncelle(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Resim seçilirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    private fun setupObservers()
    {
        viewModel.kullaniciBilgiState.observe(this) { sonuc ->
            when (sonuc)
            {
                is Resource.Loading -> { }
                is Resource.Success ->
                {
                    val kullanici = sonuc.data
                    kullanici?.let {
                        mevcutKullanici = it
                        binding.tvProfileName.text = it.isimSoyisim
                        binding.tvCompanyName.text = it.sirketBilgileri

                        binding.etEmail.setText(it.ePosta)
                        binding.etPhone.setText(it.telefonNo)
                        binding.etCompany.setText(it.sirketBilgileri)
                        
                        if (it.profilResmi.isNotEmpty()) {
                            try {
                                val decodedString = android.util.Base64.decode(it.profilResmi, android.util.Base64.DEFAULT)
                                val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                binding.imgProfile.setImageBitmap(decodedByte)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                is Resource.Error ->
                {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.guncellemeState.observe(this) { sonuc ->
            when(sonuc)
            {
                is Resource.Success -> Toast.makeText(this, "Bilgiler Güncellendi", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        islemViewModel.islemListesi.observe(this) { sonuc ->
            if (sonuc is Resource.Success) {
                val islemler = sonuc.data ?: emptyList()
                val satisSayisi = islemler.count { it.islemTuru == "SATIS" }
                binding.tvSalesCount.text = satisSayisi.toString()

                var toplamGelir = 0.0
                var toplamGider = 0.0
                islemler.forEach { 
                    if (it.islemTuru == "SATIS" || it.islemTuru == "GELIR") toplamGelir += it.islemTutari
                    else toplamGider += it.islemTutari
                }
                
                val karOrani = if (toplamGider > 0) {
                    ((toplamGelir - toplamGider) / toplamGider) * 100
                } else if (toplamGelir > 0) {
                    100.0
                } else {
                    0.0
                }
                
                binding.tvProfitRate.text = "%${karOrani.toInt()}"
            }
        }

        hedefViewModel.hedefListesi.observe(this) { sonuc ->
            if (sonuc is Resource.Success) {
                val hedefler = sonuc.data ?: emptyList()
                binding.tvTargetsCount.text = hedefler.size.toString()
            }
        }
    }

    private fun setupListeners()
    {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, GirisSayfasi::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnUpdate.setOnClickListener {
            if ( mevcutKullanici != null)
            {
                showUpdateDialog(mevcutKullanici!!)
            }
            else
            {
                Toast.makeText(this, "Kullanıcı verileri henüz yüklenmedi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditPhoto.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intentToGallery)
        }
    }

    private fun showUpdateDialog(kullanicilar: Kullanicilar)
    {
        val dialog = BottomSheetDialog(this)
        guncelleDialogBinding = LayoutProfilGuncelleDialogBinding.inflate(layoutInflater)
        dialog.setContentView(guncelleDialogBinding.root)

        guncelleDialogBinding.etUpdateName.setText(kullanicilar.isimSoyisim)
        guncelleDialogBinding.etUpdatePhone.setText(kullanicilar.telefonNo)
        guncelleDialogBinding.etUpdateCompany.setText(kullanicilar.sirketBilgileri)

        guncelleDialogBinding.btnSaveUpdate.setOnClickListener {
            val yeniIsim = guncelleDialogBinding.etUpdateName.text.toString()
            val yeniTel = guncelleDialogBinding.etUpdatePhone.text.toString()
            val yeniSirket = guncelleDialogBinding.etUpdateCompany.text.toString()

            if (yeniIsim.isEmpty())
            {
                guncelleDialogBinding.etUpdateName.error = "Lütfen isim giriniz"
                return@setOnClickListener
            }
            if (yeniTel.isEmpty())
            {
                guncelleDialogBinding.etUpdatePhone.error = "Lütfen telefon numarası giriniz"
                return@setOnClickListener
            }
            if (yeniSirket.isEmpty())
            {
                guncelleDialogBinding.etUpdateCompany.error = "Lütfen şirket bilgilerini giriniz"
                return@setOnClickListener
            }

            kullanicilar.isimSoyisim = yeniIsim
            kullanicilar.sirketBilgileri = yeniSirket
            kullanicilar.telefonNo = yeniTel

            viewModel.bilgileriGuncelle(kullanicilar)

            dialog.dismiss()
        }

        dialog.show()
    }
}