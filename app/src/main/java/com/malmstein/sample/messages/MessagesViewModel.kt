package com.malmstein.sample.messages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.malmstein.sample.messages.data.AttachmentEntity
import com.malmstein.sample.messages.data.MessageEntity
import com.malmstein.sample.messages.data.MessageModel
import com.malmstein.sample.messages.data.MessagesRepository
import com.malmstein.sample.messages.data.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesViewModel(private val repository: MessagesRepository) : ViewModel() {

    companion object {
        const val INITIAL_PAGE = 1
        const val ITEMS_PER_PAGE = 20
    }

    private var currentPage = 0
    private val page = MutableLiveData<Int>()

    val messages: LiveData<List<MessageModel>> = Transformations.switchMap(page) {
        currentPage -> loadNextPage(currentPage)
    }

    fun loadMessages(){
        currentPage = INITIAL_PAGE
        page.value = currentPage
    }

    fun loadMoreMessages(){
        currentPage++
        page.value = currentPage
    }

    private fun loadNextPage(page: Int): LiveData<List<MessageModel>>{

        val messagesLiveData = repository.getPagedMessages(ITEMS_PER_PAGE * page)

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