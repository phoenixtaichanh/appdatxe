package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupportRepository(private val apiService: ApiService) {

    // ========== FAQ ==========
    suspend fun getFAQs(category: String? = null): Result<List<FaqDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFAQs(category)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to load FAQs: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getFAQCategories(): Result<List<FaqCategoryDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFAQCategories()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to load categories: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getFAQDetail(faqId: Int): Result<FaqDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFAQDetail(faqId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("FAQ not found"))
                } else {
                    Result.failure(Exception("Failed to load FAQ: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun rateFAQ(faqId: Int, helpful: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.rateFAQ(faqId, mapOf("helpful" to helpful))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to rate FAQ: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ========== CONSULTANT CHAT ==========
    suspend fun getConversations(status: String? = null, category: String? = null): Result<List<ConsultantConversationDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConsultantConversations(status, category)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to load conversations: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createConversation(subject: String, category: String, message: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createConsultantConversation(
                    mapOf("subject" to subject, "category" to category, "message" to message)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    // Backend returns { success: true, conversation_id: N }
                    val convId = (body?.get("conversation_id") as? Number)?.toInt()
                    if (convId != null) {
                        Result.success(convId)
                    } else {
                        Result.failure(Exception("Invalid response: conversation_id not found"))
                    }
                } else {
                    Result.failure(Exception("Failed to create conversation: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMessages(conversationId: Int): Result<List<ConsultantMessageDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConsultantMessages(conversationId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to load messages: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun sendMessage(conversationId: Int, message: String): Result<ConsultantMessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendConsultantMessage(conversationId, mapOf("message" to message))
                if (response.isSuccessful) {
                    val data = response.body()
                    val msgData = data?.get("data") as? Map<*, *>
                    if (msgData != null) {
                        val dto = ConsultantMessageDto(
                            id = (msgData["id"] as? Number)?.toInt() ?: 0,
                            conversationId = (msgData["conversation_id"] as? Number)?.toInt() ?: conversationId,
                            senderId = (msgData["sender_id"] as? Number)?.toInt() ?: 0,
                            senderType = msgData["sender_type"] as? String ?: "",
                            senderName = msgData["sender_name"] as? String,
                            message = msgData["message"] as? String ?: message,
                            messageType = msgData["message_type"] as? String ?: "text",
                            attachmentUrl = msgData["attachment_url"] as? String,
                            isRead = msgData["is_read"] as? Boolean ?: false,
                            createdAt = msgData["created_at"] as? String ?: ""
                        )
                        Result.success(dto)
                    } else {
                        Result.failure(Exception("Invalid response"))
                    }
                } else {
                    Result.failure(Exception("Failed to send message: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun resolveConversation(conversationId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.resolveConsultantConversation(conversationId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to resolve: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun closeConversation(conversationId: Int, rating: Int?, feedback: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val body = mutableMapOf<String, Any>()
                rating?.let { body["rating"] = it }
                feedback?.let { body["feedback"] = it }
                val response = apiService.closeConsultantConversation(conversationId, body)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to close: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getUnreadCount(): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUnreadConsultantCount()
                if (response.isSuccessful) {
                    val count = response.body()?.get("count") as? Int ?: 0
                    Result.success(count)
                } else {
                    Result.success(0)
                }
            } catch (e: Exception) {
                Result.success(0)
            }
        }
}
