package com.example.slowmotionapp.ui.fragments

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.slowmotionapp.databinding.FragmentVideoCheckBinding
import com.example.slowmotionapp.ui.activities.EditorActivity
import java.io.File

class VideoCheckFragment : Fragment() {

    private var _binding: FragmentVideoCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVideoCheckBinding.inflate(inflater, container, false)


        val videoUri: String = (activity as EditorActivity?)!!.getVideoUri()!!

        Log.d("MaximusFragment", "onCreateView:1 $videoUri")

        binding.checkVideoView.setVideoURI(Uri.parse(videoUri))

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Preparing Video to Play...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        binding.checkVideoView.setOnPreparedListener {
            progressDialog.dismiss()
            binding.checkVideoView.start()
            it.isLooping = true
        }

        binding.checkYes.setOnClickListener {
            val fragment1 = VideoCheckFragment()
            val fragment2 = TrimVideoFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(com.example.slowmotionapp.R.id.fragment_container, fragment2)
            transaction.addToBackStack(null)
            transaction.remove(fragment1).commit()
        }

        binding.checkNo.setOnClickListener {
            deleteVideo(videoUri)
            requireActivity().finish()
        }

        return binding.root
    }

    private fun deleteVideo(videoFilePath: String): Boolean {
        val videoFile = File(videoFilePath)

        if (videoFile.exists()) {
            return videoFile.delete()
        }

        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}