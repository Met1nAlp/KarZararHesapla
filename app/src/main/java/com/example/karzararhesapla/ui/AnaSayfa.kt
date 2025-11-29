package com.example.karzararhesapla.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karzararhesapla.R
import com.example.karzararhesapla.core.Resource
import com.example.karzararhesapla.databinding.ActivityAnaSayfaBinding
import com.example.karzararhesapla.databinding.LayoutIslemEkleDialogBinding
import com.example.karzararhesapla.entity.*
import com.example.karzararhesapla.ui.adapter.*
import com.example.karzararhesapla.ui.viewmodel.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class AnaSayfa : AppCompatActivity()
{
    private lateinit var binding: ActivityAnaSayfaBinding
    private val islemViewModel: IslemViewModel by viewModels()
    private val kullaniciViewModel: KullaniciViewModel by viewModels()

    private val adapter = IslemAdapter()

    private val kullaniciID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityAnaSayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        if (kullaniciID == null)
        {
            startActivity(Intent(this, GirisSayfasi::class.java))
            finish()
            return
        }

        setupRecyclerView()
        setupObservers()
        loadData()
        setupClicks()
    }

    private fun setupRecyclerView()
    {
        binding.rvIslemler.layoutManager = LinearLayoutManager(this)
        binding.rvIslemler.adapter = adapter
    }

    private fun setupObservers()
    {
        islemViewModel.islemListesi.observe(this) { sonuc ->
            when (sonuc)
            {
                is Resource.Loading ->
                {

                }
                is Resource.Success ->
                {
                    val liste = sonuc.data ?: emptyList()
                    adapter.submitList(liste.reversed())

                    val satisAdedi = liste.count { it.islemTuru == "SATIS" || it.islemTuru == "GELIR" }
                    binding.tvSalesCount.text = "$satisAdedi"
                }
                is Resource.Error ->
                {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        islemViewModel.ozetNetKar.observe(this) { netKar ->

            val formattedKar = String.format("%.2f", netKar)

            if (netKar < 0)
            {
                binding.tvProfitPercentage.text = "${formattedKar} ₺"
                binding.tvProfitLabel.text = "Genel Zarar"
            }
            else
            {
                binding.tvProfitPercentage.text = "+${formattedKar} ₺"
                binding.tvProfitLabel.text = "Genel Kar"
            }
        }
        
        islemViewModel.islemIslemDurumu.observe(this) { sonuc ->
            when(sonuc)
            {
                is Resource.Success ->
                {
                    Toast.makeText(this, "İşlem başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    kullaniciID?.let { islemViewModel.islemleriGetir(it) }
                }
                is Resource.Error ->
                {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        kullaniciViewModel.kullaniciBilgiState.observe(this) { sonuc ->
            if (sonuc is Resource.Success)
            {
                val kullanici = sonuc.data
                kullanici?.let {
                    if (it.profilResmi.isNotEmpty())
                    {
                        try
                        {
                            val decodedString = android.util.Base64.decode(it.profilResmi, android.util.Base64.DEFAULT)
                            val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            binding.imgProfileBtn.setImageBitmap(decodedByte)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun loadData()
    {
        kullaniciID?.let { uid ->
            islemViewModel.islemleriGetir(uid)
            kullaniciViewModel.bilgileriGetir(uid)
        }
    }

    private fun setupClicks()
    {
        binding.cardAddSale.setOnClickListener {
            showAddTransactionDialog()
        }

        binding.cardKarZarar.setOnClickListener {
            startActivity(Intent(this, KarZararSayfasi::class.java))
        }

        binding.imgProfileBtn.setOnClickListener {
            startActivity(Intent(this, ProfilSayfasi::class.java))
        }

        binding.cardHedefler.setOnClickListener {
            startActivity(Intent(this, HedeflerSayfasi::class.java))
        }
        
        // Tümünü Gör butonu
        binding.root.findViewById<android.widget.TextView>(R.id.tvViewAll)?.setOnClickListener {
            startActivity(Intent(this, KarZararSayfasi::class.java))
        }
    }

    private fun showAddTransactionDialog()
    {
        val dialog = BottomSheetDialog(this)

        val dialogBinding = LayoutIslemEkleDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val mevcutListe = islemViewModel.islemListesi.value?.data ?: emptyList()
        val stokListesi = mevcutListe.filter { it.islemTuru == "ALIS" || it.islemTuru == "GIDER" }

        val stokIsimleri = stokListesi.map { it.islemAciklamasi }.toMutableList()

        if (stokIsimleri.isEmpty())
        {
            stokIsimleri.add("Stokta ürün yok")
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            stokIsimleri
        )
        dialogBinding.spinnerStock.adapter = adapter

        dialogBinding.rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbSale)
            {
                dialogBinding.tilDescription.visibility = View.GONE
                dialogBinding.llStockSpinner.visibility = View.VISIBLE
                dialogBinding.tilCategory.visibility = View.GONE

                if (stokListesi.isEmpty())
                {
                    Toast.makeText(this, "Satılacak stok bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                dialogBinding.tilDescription.visibility = View.VISIBLE
                dialogBinding.llStockSpinner.visibility = View.GONE
                dialogBinding.tilCategory.visibility = View.VISIBLE
            }
        }

        dialogBinding.btnAddTransaction.setOnClickListener {
            val tutarString = dialogBinding.etAmount.text.toString().trim()
            val kategori = dialogBinding.etCategory.text.toString().trim()

            val secilenId = dialogBinding.rgTransactionType.checkedRadioButtonId
            val islemTuru = if (secilenId == R.id.rbSale) "SATIS" else "ALIS"

            var aciklama = ""

            if (islemTuru == "SATIS")
            {
                if (stokListesi.isNotEmpty())
                {
                    aciklama = dialogBinding.spinnerStock.selectedItem.toString()
                }
                else
                {
                    Toast.makeText(this, "Stokta ürün yok, satış yapılamaz.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            else
            {
                aciklama = dialogBinding.etDescription.text.toString().trim()
            }

            if (tutarString.isEmpty())
            {
                dialogBinding.etAmount.error = "Tutar giriniz"
                return@setOnClickListener
            }
            if (aciklama.isEmpty())
            {
                Toast.makeText(this, "Ürün adı veya açıklama giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val yeniIslem = Islemler(
                kullaniciID = kullaniciID!!,
                islemTuru = islemTuru,
                islemKategorisi = kategori,
                islemTutari = tutarString.toDoubleOrNull() ?: 0.0,
                islemAciklamasi = aciklama,
                islemTarihi = System.currentTimeMillis()
            )

            islemViewModel.islemEkle(yeniIslem)

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume()
    {
        super.onResume()
        kullaniciID?.let { 
            islemViewModel.islemleriGetir(it)
            kullaniciViewModel.bilgileriGetir(it)
        }
    }
}