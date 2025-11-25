package com.example.karzararhesapla.UI

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.Entity.Hedefler
import com.example.karzararhesapla.UI.ViewModel.HedefViewModel
import com.example.karzararhesapla.databinding.ActivityHedeflerSayfasiBinding
import com.example.karzararhesapla.databinding.LayoutHedefEkleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HedeflerSayfasi : AppCompatActivity()
{
    private lateinit var binding: ActivityHedeflerSayfasiBinding
    private val viewModel: HedefViewModel by viewModels()
    private val kullaniciID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHedeflerSayfasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        if (kullaniciID == null) {
            Toast.makeText(this, "Oturum açılmadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        setupObservers()

        viewModel.hedefleriGetir(kullaniciID)
    }

    private fun setupObservers()
    {
        viewModel.hedefIslemDurumu.observe(this) { sonuc ->
            when(sonuc) {
                is Resource.Loading ->
                {
                    Toast.makeText(this , "Hedefler yükleniyor" , Toast.LENGTH_SHORT).show()
                }
                is Resource.Success ->
                {
                    Toast.makeText(this, "Hedef başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                    kullaniciID?.let { viewModel.hedefleriGetir(it) }
                }
                is Resource.Error ->
                {
                    Toast.makeText(this, sonuc.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.hedefListesi.observe(this) { sonuc ->
            when (sonuc)
            {
                is Resource.Loading ->
                {
                    Toast.makeText(this, "Hedefler yükleniyor", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success ->
                {
                    val hedefler = sonuc.data ?: emptyList()

                    if (hedefler.isEmpty()) {
                        Toast.makeText(this, "Henüz hedef eklenmemiş", Toast.LENGTH_SHORT).show()
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

        binding.btnYeniHedef.setOnClickListener {
            showAddTargetDialog()
        }
    }

    private fun showAddTargetDialog() {
        val dialog = BottomSheetDialog(this)

        val dialogBinding = LayoutHedefEkleDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        val takvim = Calendar.getInstance()
        var secilenTarihLong: Long = 0L

        dialogBinding.llDateSelect.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, yil, ay, gun ->
                    takvim.set(yil, ay, gun)
                    secilenTarihLong = takvim.timeInMillis

                    val format = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
                    dialogBinding.tvSelectedDate.text = format.format(takvim.time)
                    dialogBinding.tvSelectedDate.setTextColor(resources.getColor(android.R.color.black, theme))
                },
                takvim.get(Calendar.YEAR),
                takvim.get(Calendar.MONTH),
                takvim.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        dialogBinding.btnSaveTarget.setOnClickListener {
            val baslik = dialogBinding.etTargetTitle.text.toString().trim()
            val kategori = dialogBinding.etTargetCategory.text.toString().trim()
            val tutarStr = dialogBinding.etTargetAmount.text.toString().trim()
            val adetStr = dialogBinding.etTargetQuantity.text.toString().trim()

            if (baslik.isEmpty())
            {
                dialogBinding.etTargetTitle.error = "Hedef adı giriniz"
                return@setOnClickListener
            }

            if (tutarStr.isEmpty() && adetStr.isEmpty())
            {
                Toast.makeText(this, "Lütfen hedef tutar veya adet giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secilenTarihLong == 0L)
            {
                Toast.makeText(this, "Lütfen bitiş tarihi seçiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val yeniHedef = Hedefler(
                kullaniciID = kullaniciID!!,
                hedefAdi = baslik,
                hedefKategori = kategori,
                hedefTutar = tutarStr.toDoubleOrNull() ?: 0.0,
                hedefMiktar = adetStr.toIntOrNull() ?: 0,
                hedefBaslangicTarihi = System.currentTimeMillis(),
                hedefBitisTarihi = secilenTarihLong,
                tamamlandiMi = false
            )

            viewModel.hedefEkle(yeniHedef)
            dialog.dismiss()
        }

        dialog.show()
    }
}