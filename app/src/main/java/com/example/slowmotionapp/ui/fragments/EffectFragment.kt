package com.example.slowmotionapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slowmotionapp.databinding.FragmentEffectBinding
import com.example.slowmotionapp.effects.FilterAdapter
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.filterPosition
import com.example.slowmotionapp.ui.fragments.CropSpeedFragment.Companion.ePlayerView

class EffectFragment : Fragment(), FilterAdapter.OnItemClickListener {

    private var _binding: FragmentEffectBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FilterAdapter
    private lateinit var filterTypes: List<FilterType>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEffectBinding.inflate(inflater, container, false)

        filterTypes = FilterType.createFilterList()

        // Initialize RecyclerView
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = FilterAdapter(filterTypes, this, lifecycleScope)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onItemClick(position: Int) {
        ePlayerView!!.setGlFilter(
            FilterType.createGlFilter(
                filterTypes[position],
                requireContext()
            )
        )
        filterPosition = position

    }
}