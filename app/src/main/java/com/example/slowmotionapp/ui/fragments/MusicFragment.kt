package com.example.slowmotionapp.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slowmotionapp.adapters.Mp3StoreAdapter
import com.example.slowmotionapp.databinding.FragmentMusicBinding
import com.example.slowmotionapp.repository.Mp3StoreRepository
import com.example.slowmotionapp.viewmodel.Mp3StoreViewModel
import com.example.slowmotionapp.viewmodelfactory.Mp3StoreViewModelFactory

class MusicFragment : Fragment() {

    private lateinit var viewModel: Mp3StoreViewModel
    private lateinit var mp3StoreAdapter: Mp3StoreAdapter

    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentMusicBinding.inflate(inflater, container, false)

        mp3StoreAdapter = Mp3StoreAdapter(emptyList()) { link ->
            // Start playing the music using the provided link
            // You can use the MediaPlayer or any other audio playback library of your choice
            // Example with MediaPlayer:
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(link)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = mp3StoreAdapter


        Mp3StoreRepository()

        // Inside onCreateView method
        val mp3StoreViewModelFactory = Mp3StoreViewModelFactory(Mp3StoreRepository())
        viewModel = ViewModelProvider(
            requireActivity(),
            mp3StoreViewModelFactory
        )[Mp3StoreViewModel::class.java]

        viewModel.mp3Stores.observe(requireActivity()) {
            // Display the mp3 store names in your UI
            // You can use a RecyclerView with a custom adapter for this
            mp3StoreAdapter.setData(it)
        }

        viewModel.error.observe(requireActivity()) {
            // Handle the error and display a message in your UI
        }

        viewModel.fetchMp3Stores()

        return binding.root
    }
}