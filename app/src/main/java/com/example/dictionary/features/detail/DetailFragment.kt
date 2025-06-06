package com.example.dictionary.features.detail

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.dictionary.R
import com.example.dictionary.data.local.AppDatabase
import com.example.dictionary.data.local.WordRepository
import com.example.dictionary.databinding.FragmentDetailBinding
import com.example.dictionary.features.word.Word
import com.example.dictionary.features.word.WordViewModel
import com.example.dictionary.features.word.WordViewModelFactory
import com.example.dictionary.network.DictionarySite
import com.example.dictionary.utils.SEARCH_WORD
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private lateinit var word: Word
    private lateinit var player: ExoPlayer
    private lateinit var viewModel: WordViewModel

    private val db by lazy {
        AppDatabase.getInstance(requireContext())
    }
    private val dictionarySite = DictionarySite()
    private val wordRepository by lazy {
        WordRepository(db.wordDao())
    }

    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        word = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(SEARCH_WORD, Word::class.java)!!
        } else {
            @Suppress("DEPRECATION") requireArguments().getParcelable(SEARCH_WORD)!!
        }
        viewModel = ViewModelProvider(
            this,
            WordViewModelFactory(dictionarySite, wordRepository)
        )[WordViewModel::class.java]
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
        setupBookmark()
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupWord() {
        binding.tvTitle.text = word.title
        binding.tvIpa.text = word.ipa
        binding.tvMeaning.text = word.meaning

        binding.ibBookmark.setOnClickListener {
            handleBookmark()
        }

        initializePlayer(word.soundUrl)
        binding.ibSpeaker.setOnClickListener {
            playSound()
        }

        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, word.examples
        )
        binding.lv.adapter = adapter
    }

    private fun setupBookmark() {
        viewModel.getAllWords()
        lifecycleScope.launch {
            viewModel.allWords.collect {
                Log.i("test", it.toString())
            }
        }
        viewModel.getWordsByTitle(word.title)
        lifecycleScope.launch {
            viewModel.wordsByTitle.collect { words ->
                isBookmarked = words.isNotEmpty()
                if (isBookmarked) {
                    Log.i("test", "bookmarked")
                    binding.ibBookmark.setImageResource(R.drawable.baseline_bookmark_added_24)
                } else {
                    Log.i("test", "not bookmark")
                    binding.ibBookmark.setImageResource(R.drawable.baseline_bookmark_add_24)
                }
            }
        }
    }

    private fun handleBookmark() {
        isBookmarked = !isBookmarked
        if (!isBookmarked) {
            viewModel.deleteWord(word)
        } else {
            viewModel.insertWord(word)
        }
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun playSound() {
        player.seekTo(0)
        player.play()
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
