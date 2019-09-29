package com.malmstein.sample.messages.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.malmstein.sample.messages.MessagesViewModel
import com.malmstein.sample.messages.R
import com.malmstein.sample.messages.data.MessageModel

class MessagesAdapter(private val viewModel: MessagesViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val messages: MutableList<MessageModel> = mutableListOf()

    override fun getItemId(position: Int): Long {
        return when (val item = messages[position]) {
            is MessageModel.Message -> item.id
            is MessageModel.Attachment -> item.id
            else -> throw Exception("Unsupported viewType")
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = messages[position]) {
            is MessageModel.Message -> {
                if (item.isSelf) {
                    R.layout.view_message_self_item
                } else {
                    R.layout.view_messages_item
                }
            }
            is MessageModel.Attachment -> {
                if (item.isSelf) {
                    R.layout.view_attachment_self_item
                } else {
                    R.layout.view_attachment_item
                }
            }
            else -> throw Exception("Unsupported viewType")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.view_messages_item -> {
                val view = inflater.inflate(R.layout.view_messages_item, parent, false)
                MessagesViewHolder(view, viewModel)
            }
            R.layout.view_message_self_item -> {
                val view = inflater.inflate(R.layout.view_message_self_item, parent, false)
                MessagesViewHolder(view, viewModel)
            }
            R.layout.view_attachment_item -> {
                val view = inflater.inflate(R.layout.view_attachment_item, parent, false)
                AttachmentViewHolder(view, viewModel)
            }
            R.layout.view_attachment_self_item -> {
                val view = inflater.inflate(R.layout.view_attachment_self_item, parent, false)
                AttachmentViewHolder(view, viewModel)
            }
            else -> throw Exception("Unsupported viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = messages[position]
        when (holder) {
            is MessagesViewHolder -> {
                val messageFromSameUser = if (position > 0) {
                    when (val previousMessage = messages[position - 1]) {
                        is MessageModel.Message -> {
                            val currentMessage = currentItem as MessageModel.Message
                            previousMessage.userId == currentMessage.userId
                        }
                        is MessageModel.Attachment -> {
                            val currentMessage = currentItem as MessageModel.Message
                            previousMessage.userId == currentMessage.userId
                        }
                    }
                } else {
                    false
                }
                holder.bind(currentItem as MessageModel.Message, messageFromSameUser)
            }
            is AttachmentViewHolder -> holder.bind(currentItem as MessageModel.Attachment)
            else -> throw Exception("Unsupported ViewHolder")
        }
    }

    fun notifyChanges(newList: List<MessageModel>) {
        val diffCallback = MessageDiffCallback(messages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}

class MessageDiffCallback(private val oldList: List<MessageModel>, private val newList: List<MessageModel>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItemId = when (val oldItem = oldList[oldItemPosition]) {
            is MessageModel.Message -> oldItem.id
            is MessageModel.Attachment -> oldItem.id
        }
        val newItemId = when (val newItem = newList[newItemPosition]) {
            is MessageModel.Message -> newItem.id
            is MessageModel.Attachment -> newItem.id
        }

        return oldItemId == newItemId
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val oldItem = when (val oldItem = oldList[oldPosition]) {
            is MessageModel.Message -> oldItem
            is MessageModel.Attachment -> oldItem
        }

        val newItem = when (val newItem = newList[newPosition]) {
            is MessageModel.Message -> newItem
            is MessageModel.Attachment -> newItem
        }

        return oldItem == newItem
    }

    @Nullable
    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}



