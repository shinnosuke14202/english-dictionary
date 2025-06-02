package com.example.dictionary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dictionary.databinding.ActivityMainBinding
import com.example.dictionary.features.bookmark.BookmarkFragment
import com.example.dictionary.features.search.SearchFragment
import com.example.dictionary.features.settings.SettingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragments = listOf(
        SearchFragment.newInstance(), BookmarkFragment.newInstance(), SettingFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // disable swiping

        binding.bnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemHome -> binding.viewPager.setCurrentItem(0, false)
                R.id.itemBookmark -> binding.viewPager.setCurrentItem(1, false)
                R.id.itemSetting -> binding.viewPager.setCurrentItem(2, false)
            }
            true
        }
    }
}
