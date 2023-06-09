package com.example.slowmotionapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentCropBinding
import com.example.slowmotionapp.viewmodel.SharedViewModel

class CropFragment : Fragment() {

    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!

    private var cropSelected = false

    private lateinit var sharedViewModel: SharedViewModel

    // Get a reference to the shared ViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCropBinding.inflate(inflater, container, false)

        binding.btnFree.setOnClickListener {
            sharedViewModel.pauseVideo(true)
            sharedViewModel.cropSelected(1)
            binding.imageViewFree.setImageResource(R.drawable.crop_select)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btn11.setOnClickListener {
            sharedViewModel.pauseVideo(true)
            sharedViewModel.cropSelected(2)
            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_select)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btnPortrait.setOnClickListener {
            sharedViewModel.pauseVideo(true)
            sharedViewModel.cropSelected(3)
            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_select)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btnLandScape.setOnClickListener {
            sharedViewModel.pauseVideo(true)
            sharedViewModel.cropSelected(4)
            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_select)

            cropSelected = true
        }

        binding.btnCancel.setOnClickListener {
            cropSelected = false
            sharedViewModel.switchFragmentB(true)
        }
        binding.btnOk.setOnClickListener {
            if (cropSelected) {
                sharedViewModel.startCrop(true)
                sharedViewModel.cropSelected(-1)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select Crop aspect ratio.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        return binding.root
    }

}