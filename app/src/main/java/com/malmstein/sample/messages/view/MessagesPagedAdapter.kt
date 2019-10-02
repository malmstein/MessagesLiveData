package com.malmstein.sample.messages.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malmstein.sample.messages.MessagesViewModel
import com.malmstein.sample.messages.R
import com.malmstein.sample.messages.data.MessageModel

class MessagesPagedAdapter(private val viewModel: MessagesViewModel): PagedListAdapter<MessageModel, RecyclerView.ViewHolder>(DIFF_CALLBACK){

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<MessageModel>() {

            override fun areItemsTheSame(oldMessage: MessageModel, newMessage: MessageModel):Boolean {
                val oldItemId = when (oldMessage) {
                    is MessageModel.Message -> oldMessage.id
                    is MessageModel.Attachment -> oldMessage.id
                }
                val newItemId = when (newMessage) {
                    is MessageModel.Message -> newMessage.id
                    is MessageModel.Attachment -> newMessage.id
                }

                return oldItemId == newItemId
            }

            override fun areContentsTheSame(oldMessage: MessageModel, newMessage: MessageModel) : Boolean {
                val oldItem = when (oldMessage) {
                    is MessageModel.Message -> oldMessage
                    is MessageModel.Attachment -> oldMessage
                }

                val newItem = when (newMessage) {
                    is MessageModel.Message -> newMessage
                    is MessageModel.Attachment -> newMessage
                }

                return oldItem.equals(newItem)
            }
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
        val currentItem = getItem(position)
        when (holder) {
            is MessagesViewHolder -> {
                val messageFromSameUser = if (position > 0) {
                    when (val previousMessage = getItem(position - 1)) {
                        is MessageModel.Message -> {
                            val currentMessage = currentItem as MessageModel.Message
                            previousMessage.userId == currentMessage.userId
                        }
                        is MessageModel.Attachment -> {
                            val currentMessage = currentItem as MessageModel.Message
                            previousMessage.userId == currentMessage.userId
                        }
                        else -> throw Exception("Unsupported ViewHolder")
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

}