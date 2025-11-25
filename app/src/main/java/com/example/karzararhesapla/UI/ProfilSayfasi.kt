package com.example.karzararhesapla.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Kullanicilar
import com.example.karzararhesapla.R
import com.example.karzararhesapla.UI.ViewModel.KullaniciViewModel
import com.example.karzararhesapla.databinding.ActivityProfilSayfasiBinding
import com.example.karzararhesapla.databinding.LayoutProfilGuncelleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class ProfilSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityProfilSayfasiBinding
    private lateinit var guncelleDialogBinding: LayoutProfilGuncelleDialogBinding
    private val viewModel: KullaniciViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private var mevcutKullanici: Kullanicilar? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilSayfasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupListeners()
        setupObservers()

        val uid = auth.currentUser?.uid
        if (uid != null)
        {
            viewModel.bilgileriGetir(uid)
        }
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
                        binding.tvProfileName.text = it.isimSoyisim
                        binding.tvCompanyName.text = it.sirketBilgileri

                        binding.etEmail.setText(it.ePosta)
                        binding.etPhone.setText(it.telefonNo)
                        binding.etCompany.setText(it.sirketBilgileri)
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
    }

    private fun showUpdateDialog(kullanicilar: Kullanicilar)
    {
        val dialog = BottomSheetDialog(this)
        guncelleDialogBinding = LayoutProfilGuncelleDialogBinding.inflate(layoutInflater)
        val view = layoutInflater.inflate(R.layout.layout_profil_guncelle_dialog, null)
        dialog.setContentView(view)

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
            }
            if (yeniTel.isEmpty())
            {
                guncelleDialogBinding.etUpdatePhone.error = "Lütfen telefon numarası giriniz"
            }
            if (yeniSirket.isEmpty())
            {
                guncelleDialogBinding.etUpdateCompany.error = "Lütfen şirket bilgilerini giriniz"
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