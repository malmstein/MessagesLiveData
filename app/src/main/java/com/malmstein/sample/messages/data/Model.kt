package com.malmstein.sample.messages.data

import com.google.gson.annotations.SerializedName

data class DataJSON(
    @SerializedName("messages") val messages: List<MessageJSON>,
    @SerializedName("users") val users: List<UserJSON>
)

data class AttachmentJSON(
    val id: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)

data class MessageJSON(
    val id: Long,
    val userId: Long,
    val content: String,
    val attachments: List<AttachmentJSON>?
)

data class UserJSON(
    val id: Long,
    val name: String,
    val avatarId: String
)

sealed class MessageModel {

    companion object {
        fun from(id: Long, messageEntity: MessageEntity): MessageModel {
            return Message.from(id, messageEntity)
        }

        fun from(id: Long, attachmentEntity: AttachmentEntity, messageEntity: MessageEntity): MessageModel {
            return Attachment.from(id, attachmentEntity, messageEntity)
        }
    }

    data class Attachment(val id: Long, val attachmentId: String, val userId: Long, val name: String, val linkUrl: String, val isSelf: Boolean) :
        MessageModel() {
        companion object {
            fun from(id: Long, attachmentEntity: AttachmentEntity, messageEntity: MessageEntity) =
                Attachment(id, attachmentEntity.id, messageEntity.userId, attachmentEntity.name, attachmentEntity.thumbnailUrl, messageEntity.isSelf)
        }
    }

    data class Message(
        val id: Long,
        val userId: Long,
        val messageId: Long,
        val name: String,
        val profileUrl: String,
        val message: String,
        val isSelf: Boolean
    ) : MessageModel() {
        companion object {
            fun from(id: Long, message: MessageEntity) =
                Message(id, message.userId, message.id, message.userName, message.userProfile, message.content, message.isSelf)
        }
    }
}