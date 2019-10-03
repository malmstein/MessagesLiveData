package com.malmstein.sample.messages.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.coroutineScope

class MessagesRepository(
    private val messagesDao: MessagesDao,
    private val attachmentsDao: AttachmentsDao
) {

    fun getPagedMessages(total: Int) = messagesDao.loadPagedMessages(total)
    fun getAttachments(): LiveData<List<AttachmentEntity>> = attachmentsDao.loadAllAttachments()

    suspend fun deleteMessage(message: Long) = coroutineScope {
        messagesDao.deleteMessage(message)
    }

    suspend fun deleteAttachment(attachmentId: String) = coroutineScope {
        attachmentsDao.deleteAttachment(attachmentId)
    }
}