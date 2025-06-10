package com.example.dictionary.features.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.dictionary.databinding.FragmentHistoryBinding
import com.example.dictionary.utils.HISTORY_LIST
import com.example.dictionary.utils.HISTORY_PREFS

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireContext().getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val historyText = sharedPreferences.getString(HISTORY_LIST, "") ?: ""
        var items = historyText.split(",")
        if (items.size > 100) {
            items = items.subList(0, 99)
            val newHistory = items.joinToString(",")
            sharedPreferences.edit { putString(HISTORY_LIST, newHistory) }
        } else {
            items = items.subList(0, items.size - 2)
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )
        binding.lvHistory.adapter = adapter

        binding.lvHistory.setOnItemLongClickListener { _, _, position, _ ->
            val selectedItem = items[position]

            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("History Item", selectedItem)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Copied: $selectedItem", Toast.LENGTH_SHORT).show()

            true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}
