package com.example.karzararhesapla.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karzararhesapla.Core.Resource
import com.example.karzararhesapla.UI.Adapter.IslemAdapter
import com.example.karzararhesapla.UI.ViewModel.IslemViewModel
import com.example.karzararhesapla.databinding.ActivityAnaSayfaBinding
import com.google.firebase.auth.FirebaseAuth

class AnaSayfa : AppCompatActivity()
{
    private lateinit var binding: ActivityAnaSayfaBinding
    private val islemViewModel: IslemViewModel by viewModels()

    private val adapter = IslemAdapter()

    private val kullaniciID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityAnaSayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

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
                    Toast.makeText(this, "Yükleniyor...", Toast.LENGTH_SHORT).show()
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
    }

    private fun loadData()
    {
        kullaniciID?.let { uid ->
            islemViewModel.islemleriGetir(uid)
        }
    }

    private fun setupClicks()
    {
        binding.cardAddSale.setOnClickListener {
            Toast.makeText(this, "Yakında: İşlem Ekleme", Toast.LENGTH_SHORT).show()
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

        binding.cardAddTarget.setOnClickListener {
            startActivity(Intent(this, HedeflerSayfasi::class.java))
        }
    }

    override fun onResume()
    {
        super.onResume()
        kullaniciID?.let { islemViewModel.islemleriGetir(it) }
    }
}