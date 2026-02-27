package com.example.engineeringcalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.engineeringcalculator.EngineeringCalculatorApp
import com.example.engineeringcalculator.R
import com.example.engineeringcalculator.databinding.FragmentHistoryBinding
import com.example.engineeringcalculator.viewmodel.HistoryViewModel
import com.example.engineeringcalculator.viewmodel.HistoryViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding: FragmentHistoryBinding
        get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            repository = (requireActivity().application as EngineeringCalculatorApp).repository
        )
    }

    private val historyAdapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerHistory.adapter = historyAdapter
        binding.recyclerHistory.setHasFixedSize(true)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_clear_history) {
                showClearDialog()
                true
            } else {
                false
            }
        }

        viewModel.history.observe(viewLifecycleOwner) { items ->
            historyAdapter.submitList(items)
            binding.textHistoryEmpty.isVisible = items.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showClearDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.history_clear_title)
            .setMessage(R.string.history_clear_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearHistory()
            }
            .show()
    }
}
