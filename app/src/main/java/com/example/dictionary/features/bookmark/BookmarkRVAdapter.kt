package com.example.dictionary.features.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dictionary.databinding.ItemBookmarkBinding
import com.example.dictionary.features.word.Word

class BookmarkRVAdapter(
    private val onClick: (Word) -> Unit,
    private val items: List<Word>,
    private var getCheckedItems: (List<Word>) -> Unit
) : RecyclerView.Adapter<BookmarkRVAdapter.BookmarkViewHolder>() {

    private val _checkedItems = mutableListOf<Word>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding =
            ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClick.invoke(item)
        }
    }

    inner class BookmarkViewHolder(private val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(word: Word) {
            binding.apply {
                val title = "${word.title}, "
                val type = "${word.type}, "
                tvWord.text = title
                tvMeaning.text = word.meaning
                tvType.text = type
                checkbox.setOnClickListener {
                    if (checkbox.checkedState == 1) {
                        _checkedItems.add(word)
                    } else {
                        _checkedItems.remove(word)
                    }
                    getCheckedItems.invoke(_checkedItems)
                }
            }
        }
    }
}
