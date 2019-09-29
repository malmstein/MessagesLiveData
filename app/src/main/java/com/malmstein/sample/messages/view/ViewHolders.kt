package com.malmstein.sample.messages.view

import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.malmstein.sample.messages.MessagesViewModel
import com.malmstein.sample.messages.R
import com.malmstein.sample.messages.data.MessageModel

class MessagesViewHolder(itemView: View, private val viewModel: MessagesViewModel) : RecyclerView.ViewHolder(itemView) {

    private val username: TextView by lazy { itemView.findViewById<TextView>(R.id.view_message_item_name) }
    private val text: TextView by lazy { itemView.findViewById<TextView>(R.id.view_message_item_message) }
    private val profile: ImageView by lazy { itemView.findViewById<ImageView>(R.id.view_message_item_profile) }

    fun bind(message: MessageModel.Message, messageFromSameUser: Boolean) {
        if (messageFromSameUser) {
            username.visibility = View.GONE
            profile.visibility = View.INVISIBLE
        } else {
            username.visibility = View.VISIBLE
            username.text = message.name

            if (!message.isSelf) {
                profile.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.profileUrl)
                    .circleCrop()
                    .dontAnimate()
                    .into(profile)
            }
        }
        text.text = message.message
        itemView.setOnClickListener {
            showDeleteMessageMenu(itemView, message)
        }
    }

    private fun showDeleteMessageMenu(itemView: View, message: MessageModel.Message) {
        val popup = PopupMenu(itemView.context, itemView)
        popup.inflate(R.menu.message_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete -> {
                    viewModel.onDeleteMessageRequested(message); true
                }
                else -> false

            }
        }
        popup.show()
    }
}

class AttachmentViewHolder(itemView: View, private val viewModel: MessagesViewModel) : RecyclerView.ViewHolder(itemView) {

    private val fileName: TextView by lazy { itemView.findViewById<TextView>(R.id.view_attachment_item_file) }
    private val image: ImageView by lazy { itemView.findViewById<ImageView>(R.id.view_attachment_item_image) }

    fun bind(attachment: MessageModel.Attachment) {
        fileName.text = attachment.name

        Glide.with(itemView.context)
            .load(attachment.linkUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
            .dontAnimate()
            .into(image)

        itemView.setOnClickListener {
            showDeleteAttachmentMenu(itemView, attachment)
        }
    }

    private fun showDeleteAttachmentMenu(itemView: View, attachment: MessageModel.Attachment) {
        val popup = PopupMenu(itemView.context, itemView)
        popup.inflate(R.menu.message_attachment_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete_attachment -> {
                    viewModel.onDeleteAttachmentRequested(attachment); true
                }
                else -> false
            }
        }
        popup.show()
    }
}