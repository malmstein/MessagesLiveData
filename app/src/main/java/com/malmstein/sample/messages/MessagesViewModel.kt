package com.malmstein.sample.messages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.malmstein.sample.messages.data.AttachmentEntity
import com.malmstein.sample.messages.data.MessageEntity
import com.malmstein.sample.messages.data.MessageModel
import com.malmstein.sample.messages.data.MessagesRepository
import com.malmstein.sample.messages.data.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesViewModel(private val repository: MessagesRepository) : ViewModel() {

    fun loadInitialPage() = loadPagedMessages(1)
    fun loadPagedMessages(page: Int): LiveData<List<MessageModel>> {

        val messagesLiveData = repository.getPagedMessages(20 * page)
        val attachmentsLiveData = repository.getAttachments()

        val combinedMessages = MediatorLiveData<List<MessageModel>>()

        combinedMessages.addSource(messagesLiveData) { value ->
            combinedMessages.value = combineMessagesAndAttachments(messagesLiveData, attachmentsLiveData)
        }
        combinedMessages.addSource(attachmentsLiveData) { value ->
            combinedMessages.value = combineMessagesAndAttachments(messagesLiveData, attachmentsLiveData)
        }

        return combinedMessages
    }

    fun loadMessages(): LiveData<List<MessageModel>> {

        val messagesLiveData = repository.getMessages()
        val attachmentsLiveData = repository.getAttachments()

        val combinedMessages = MediatorLiveData<List<MessageModel>>()

        combinedMessages.addSource(messagesLiveData) { value ->
            combinedMessages.value = combineMessagesAndAttachments(messagesLiveData, attachmentsLiveData)
        }
        combinedMessages.addSource(attachmentsLiveData) { value ->
            combinedMessages.value = combineMessagesAndAttachments(messagesLiveData, attachmentsLiveData)
        }

        return combinedMessages
    }

    private fun combineMessagesAndAttachments(
        messagesLiveData: LiveData<List<MessageEntity>>,
        attachmentsLiveData: LiveData<List<AttachmentEntity>>
    ): List<MessageModel> {
        val messages = messagesLiveData.value
        val attachments = attachmentsLiveData.value

        return if (messages != null && attachments != null) {
            val messagesUI = mutableListOf<MessageModel>()
            var uniqueId: Long = 0
            messages.forEach { message ->
                messagesUI.add(MessageModel.from(uniqueId, message))
                uniqueId++
                attachments.filter { it.messageId == message.id }.forEach { attachment ->
                    messagesUI.add(MessageModel.from(uniqueId, attachment, message))
                    uniqueId++
                }
            }
            messagesUI
        } else {
            emptyList()
        }
    }

    fun onDeleteMessageRequested(message: MessageModel.Message) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMessage(message.messageId)
        }
    }

    fun onDeleteAttachmentRequested(attachment: MessageModel.Attachment) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAttachment(attachment.attachmentId)
        }
    }

    fun loadMore() {
    }
}

/**
 * Factory for [MessagesViewModel].
 */
class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val database = getDatabase(context)
    private val repository = MessagesRepository(database.messageDao, database.attachmentsDao)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MessagesViewModel(repository) as T
    }
}