package com.example.dictionary.features.detail

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.dictionary.R
import com.example.dictionary.databinding.FragmentDetailBinding
import com.example.dictionary.databinding.FragmentSearchBinding
import com.example.dictionary.model.Word
import com.example.dictionary.utils.SEARCH_WORD

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private lateinit var word: Word
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        word = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(SEARCH_WORD, Word::class.java)!!
        } else {
            @Suppress("DEPRECATION") requireArguments().getParcelable(SEARCH_WORD)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWord()

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupWord() {
        binding.tvTitle.text = word.title
        binding.tvIpa.text = word.ipa
        binding.tvMeaning.text = word.meaning

        binding.ibBookmark.setOnClickListener {

        }

        initializePlayer(word.soundUrl)
        binding.ibSpeaker.setOnClickListener {
            player.play()
        }

        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, word.examples
        )
        binding.lv.adapter = adapter
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    companion object {
        @JvmStatic
        fun newInstance(word: Word) = DetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(SEARCH_WORD, word)
            }
        }
    }
}
