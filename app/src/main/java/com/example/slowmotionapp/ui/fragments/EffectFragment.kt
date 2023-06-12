package com.example.slowmotionapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.daasuu.epf.EPlayerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentEffectBinding
import com.example.slowmotionapp.effects.FilterAdapter
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.fragments.CropSpeedFragment.Companion.ePlayerView
import com.example.slowmotionapp.utils.Utils

class EffectFragment : Fragment() {

    private var _binding: FragmentEffectBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEffectBinding.inflate(inflater, container, false)

        // list

        // list
        val filterTypes: List<FilterType> = FilterType.createFilterList()
        binding.listView.adapter = FilterAdapter(requireContext(), R.layout.row_text, filterTypes)
        binding.listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                ePlayerView!!.setGlFilter(
                    FilterType.createGlFilter(
                        filterTypes[position],
                        requireContext()
                    )
                )
            }
        return binding.root
    }
}