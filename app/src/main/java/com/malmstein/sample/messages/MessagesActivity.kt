package com.malmstein.sample.messages

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malmstein.sample.messages.data.MessageModel
import com.malmstein.sample.messages.view.MessagesAdapter

class MessagesActivity : AppCompatActivity() {

    private val viewModel: MessagesViewModel by viewModels { ViewModelFactory(this) }
    private val messagesList: RecyclerView by lazy { findViewById<RecyclerView>(R.id.messages_list) }

    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        setupRecyclerView()

        viewModel.loadMessages().observe(this, Observer { value ->
            value?.let { messages ->
                renderMessages(messages)
            }
        })
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        adapter = MessagesAdapter(viewModel)
        adapter.setHasStableIds(true)
        messagesList.itemAnimator = DefaultItemAnimator()
        messagesList.adapter = adapter
        messagesList.layoutManager = layoutManager
    }

    private fun renderMessages(messages: List<MessageModel>) {
        adapter.notifyChanges(messages)
    }
}
