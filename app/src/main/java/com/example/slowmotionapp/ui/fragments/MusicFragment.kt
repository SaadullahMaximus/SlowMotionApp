package com.example.slowmotionapp.ui.fragments

import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.slowmotionapp.adapters.Mp3StoreAdapter
import com.example.slowmotionapp.databinding.FragmentMusicBinding
import com.example.slowmotionapp.helper.DownloadWorker
import com.example.slowmotionapp.repository.Mp3StoreRepository
import com.example.slowmotionapp.viewmodel.Mp3StoreViewModel
import com.example.slowmotionapp.viewmodel.SharedViewModel
import com.example.slowmotionapp.viewmodelfactory.Mp3StoreViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MusicFragment : Fragment() {

    private lateinit var viewModel: Mp3StoreViewModel
    private lateinit var mp3StoreAdapter: Mp3StoreAdapter

    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!

    private lateinit var workManager: WorkManager
    private var progressDialog: ProgressDialog? = null

    private var mediaPlayer = MediaPlayer()

    private lateinit var mp: MediaPlayer

    private lateinit var sharedViewModel: SharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.stopAllMusic.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mp.stop()
                mp.reset()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentMusicBinding.inflate(inflater, container, false)

        workManager = WorkManager.getInstance(requireContext())

        mp3StoreAdapter = Mp3StoreAdapter(emptyList(), { link, position ->

            mp = MediaPlayer()

            mp.setDataSource(link)
            mp.prepareAsync()

            mp.setOnPreparedListener {
                it.start()
                Log.d("PREPARESSAAD", "onCreateView: PREPARES")
                mp3StoreAdapter.notifyItemChanged(position)
                mp3StoreAdapter.dialogDismiss()
            }

            mp.setOnCompletionListener {
                mp.seekTo(0)
                mp.start()
            }

            mediaPlayer = mp

            // Stop any other media player that might be playing
            val previousMediaPlayer = mp3StoreAdapter.getCurrentMediaPlayer()
            if (previousMediaPlayer != null && previousMediaPlayer.isPlaying) {
                previousMediaPlayer.stop()
            }

            mp3StoreAdapter.setCurrentMediaPlayer(mediaPlayer)

        }, {
            mediaPlayer.stop()
            mediaPlayer.reset()
            startDownloadWork(it)
        })

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

    private fun startDownloadWork(downloadUrl: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(DownloadWorker.KEY_DOWNLOAD_URL to downloadUrl)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Downloading MP3...")
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setCancelable(false)
        progressDialog?.show()

        val downloadLiveData = workManager.getWorkInfoByIdLiveData(downloadRequest.id)
        downloadLiveData.observe(requireActivity()) { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val outputData = workInfo.outputData
                        val downloadPath = outputData.getString(DownloadWorker.KEY_DOWNLOAD_PATH)
                        // Do something with the downloaded file path
                        sharedViewModel.downloadMusicPath(downloadPath!!)

                        progressDialog?.dismiss() // Dismiss the progress dialog
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        progressDialog?.dismiss() // Dismiss the progress dialog if the download fails or is cancelled
                    }
                    else -> {
                        val progress = workInfo.progress.getInt(DownloadWorker.PROGRESS_KEY, 0)
                        progressDialog?.progress = progress // Update the progress dialog
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            workManager.enqueueUniqueWork(
                "download",
                ExistingWorkPolicy.REPLACE,
                downloadRequest
            )
        }
    }

}