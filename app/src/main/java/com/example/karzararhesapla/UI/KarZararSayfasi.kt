package com.example.karzararhesapla.UI

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Islemler
import com.example.karzararhesapla.R
import com.example.karzararhesapla.UI.ViewModel.IslemViewModel
import com.example.karzararhesapla.databinding.ActivityKarZararSayfasiBinding
import com.example.karzararhesapla.databinding.LayoutIslemEkleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class KarZararSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityKarZararSayfasiBinding
    private val viewModel: IslemViewModel by viewModels()
    private val kullaniciID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityKarZararSayfasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        if (kullaniciID == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        setupObservers()

        viewModel.islemleriGetir(kullaniciID)
    }

    private fun setupObservers()
    {
        viewModel.islemIslemDurumu.observe(this) { sonuc ->
            when(sonuc) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    Toast.makeText(this, "İşlem başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    kullaniciID?.let { viewModel.islemleriGetir(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.islemListesi.observe(this) { sonuc ->
            when (sonuc)
            {
                is Resource.Loading -> { }
                is Resource.Success ->
                {
                    val liste: List<Islemler> = sonuc.data ?: emptyList()

                    var toplamGelir = 0.0
                    var toplamGider = 0.0

                    liste.forEach { islem ->
                        if (islem.islemTuru == "SATIS" || islem.islemTuru == "GELIR")
                        {
                            toplamGelir += islem.islemTutari
                        }
                        else
                        {
                            toplamGider += islem.islemTutari
                        }
                    }

                    val netKar = toplamGelir - toplamGider

                    binding.tvTotalIncome.text = "+$toplamGelir ₺"
                    binding.tvTotalExpense.text = "-$toplamGider ₺"

                    if (netKar >= 0) {
                        binding.tvNetProfit.text = "+$netKar ₺"
                    } else {
                        binding.tvNetProfit.text = "$netKar ₺"
                    }
                }
                is Resource.Error ->
                {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners()
    {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog()
    {
        val dialog = BottomSheetDialog(this)

        val dialogBinding = LayoutIslemEkleDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val mevcutListe = viewModel.islemListesi.value?.data ?: emptyList()
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

                if (stokListesi.isEmpty())
                {
                    Toast.makeText(this, "Satılacak stok bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                dialogBinding.tilDescription.visibility = View.VISIBLE
                dialogBinding.llStockSpinner.visibility = View.GONE
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

            viewModel.islemEkle(yeniIslem)

            dialog.dismiss()
        }

        dialog.show()
    }
}