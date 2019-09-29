package com.malmstein.sample.messages.data

import android.content.Context
import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.malmstein.sample.messages.R
import com.malmstein.sample.messages.extensions.ioThread
import java.io.IOException

@Entity
data class AttachmentEntity(@PrimaryKey val id: String, val messageId: Long, val name: String, val url: String, val thumbnailUrl: String) {
    companion object {
        fun from(attachmentJSON: AttachmentJSON, messageId: Long): AttachmentEntity {
            return AttachmentEntity(attachmentJSON.id, messageId, attachmentJSON.title, attachmentJSON.url, attachmentJSON.thumbnailUrl)
        }
    }
}

@Entity
data class MessageEntity(
    @PrimaryKey val id: Long,
    val content: String,
    val userId: Long,
    val userName: String,
    val userProfile: String,
    val isSelf: Boolean
)

@Dao
interface MessagesDao {

    @Query("delete from MessageEntity where id = :messageId")
    fun deleteMessage(messageId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<MessageEntity>)

    @Query("select * from MessageEntity")
    fun loadAllMessages(): LiveData<List<MessageEntity>>
}

@Dao
interface AttachmentsDao {

    @Query("delete from AttachmentEntity where id = :attachmentId")
    fun deleteAttachment(attachmentId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<AttachmentEntity>)

    @Query("select * from AttachmentEntity")
    fun loadAllAttachments(): LiveData<List<AttachmentEntity>>
}

@Database(entities = [MessageEntity::class, AttachmentEntity::class], version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract val messageDao: MessagesDao
    abstract val attachmentsDao: AttachmentsDao
}

private lateinit var INSTANCE: MessageDatabase

fun getDatabase(context: Context): MessageDatabase {
    synchronized(MessageDatabase::class) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room
                .databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    "messages_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            prepopulateDb(context, getDatabase(context))
                        }
                    }
                })
                .build()
        }
    }
    return INSTANCE
}

private fun prepopulateDb(context: Context, database: MessageDatabase) {
    try {
        val jsonString = loadJSONFromAssets(context.assets)
        if (jsonString.isNotEmpty()) {
            val dataJSONEntity = Gson().fromJson(jsonString, DataJSON::class.java)
            val users = dataJSONEntity.users
            val attachments: MutableList<AttachmentEntity> = mutableListOf()
            val messages = dataJSONEntity.messages.map {
                if (!it.attachments.isNullOrEmpty()) {
                    attachments.addAll(mapAttachments(it.attachments, it.id))
                }
                val user = findUserName(it.userId, users)
                if (user != null) {
                    MessageEntity(it.id, it.content, user.id, user.name, user.avatarId, false)
                } else {
                    MessageEntity(it.id, it.content, 1, context.getString(R.string.message_self_me), "", true)
                }
            }

            database.messageDao.insertAll(messages)
            database.attachmentsDao.insertAll(attachments)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun mapAttachments(attachmentJSON: List<AttachmentJSON>?, messageId: Long): List<AttachmentEntity> {
    return if (attachmentJSON.isNullOrEmpty()) {
        emptyList()
    } else {
        attachmentJSON.map { AttachmentEntity.from(it, messageId) }
    }
}

private fun findUserName(userId: Long, users: List<UserJSON>): UserJSON? {
    return if (userId == 1L) {
        null
    } else {
        users.find { it.id == userId }
    }
}

fun loadJSONFromAssets(assetManager: AssetManager): String {
    return try {
        val inputStream = assetManager.open("data.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer)
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}