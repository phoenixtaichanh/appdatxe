package com.laptrinhdidong.DoAn3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import com.laptrinhdidong.DoAn3.data.repository.SupportRepository
import com.laptrinhdidong.DoAn3.ui.components.AppTopBar
import com.laptrinhdidong.DoAn3.ui.components.GradientButton
import com.laptrinhdidong.DoAn3.ui.components.LoadingOverlay
import com.laptrinhdidong.DoAn3.ui.theme.*
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ========== STATE ==========
data class SupportState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0 = FAQ, 1 = Chat với Tư vấn viên
    val faqs: List<FaqDto> = emptyList(),
    val faqCategories: List<FaqCategoryDto> = emptyList(),
    val selectedCategory: String = "all",
    val expandedFaqId: Int? = null,
    val conversations: List<ConsultantConversationDto> = emptyList(),
    val currentConversation: ConsultantConversationDto? = null,
    val messages: List<ConsultantMessageDto> = emptyList(),
    val newMessage: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val showNewChatDialog: Boolean = false,
    val newChatSubject: String = "",
    val newChatCategory: String = "general",
    val newChatMessage: String = "",
    val unreadCount: Int = 0
)

// ========== VIEWMODEL ==========
@dagger.hilt.android.lifecycle.HiltViewModel
class SupportViewModel @javax.inject.Inject constructor(
    private val repository: SupportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SupportState())
    val state: StateFlow<SupportState> = _state.asStateFlow()

    init {
        loadFAQCategories()
        loadFAQs()
        loadConversations()
    }

    private fun loadFAQCategories() {
        viewModelScope.launch {
            repository.getFAQCategories().onSuccess { categories ->
                _state.value = _state.value.copy(faqCategories = categories)
            }.onFailure { e ->
                android.util.Log.e("SupportVM", "loadFAQCategories error: ${e.message}")
            }
        }
    }

    fun loadFAQs(category: String = "all") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, selectedCategory = category)
            val cat = if (category == "all") null else category
            repository.getFAQs(cat).onSuccess { faqs ->
                _state.value = _state.value.copy(isLoading = false, faqs = faqs)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false)
                android.util.Log.e("SupportVM", "loadFAQs error: ${e.message}")
            }
        }
    }

    fun toggleFaq(faqId: Int) {
        _state.value = _state.value.copy(
            expandedFaqId = if (_state.value.expandedFaqId == faqId) null else faqId
        )
    }

    fun rateFaq(faqId: Int, helpful: Boolean) {
        viewModelScope.launch {
            repository.rateFAQ(faqId, helpful)
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            repository.getConversations().onSuccess { convs ->
                _state.value = _state.value.copy(conversations = convs)
            }
        }
        viewModelScope.launch {
            repository.getUnreadCount().onSuccess { count ->
                _state.value = _state.value.copy(unreadCount = count)
            }
        }
    }

    fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    fun openConversation(conv: ConsultantConversationDto) {
        _state.value = _state.value.copy(currentConversation = conv)
        loadMessages(conv.id)
    }

    fun backFromConversation() {
        _state.value = _state.value.copy(currentConversation = null, messages = emptyList())
        loadConversations()
    }

    private fun loadMessages(convId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getMessages(convId).onSuccess { msgs ->
                _state.value = _state.value.copy(isLoading = false, messages = msgs)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun updateNewMessage(text: String) {
        _state.value = _state.value.copy(newMessage = text)
    }

    fun sendMessage() {
        val conv = _state.value.currentConversation ?: return
        val text = _state.value.newMessage.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, newMessage = "")
            repository.sendMessage(conv.id, text).onSuccess { msg ->
                _state.value = _state.value.copy(
                    isSending = false,
                    messages = _state.value.messages + msg
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isSending = false,
                    errorMessage = "Gửi tin nhắn thất bại",
                    newMessage = text
                )
            }
        }
    }

    fun showNewChatDialog() {
        _state.value = _state.value.copy(
            showNewChatDialog = true,
            newChatSubject = "",
            newChatCategory = "general",
            newChatMessage = ""
        )
    }

    fun dismissNewChatDialog() {
        _state.value = _state.value.copy(showNewChatDialog = false)
    }

    fun updateNewChatSubject(s: String) {
        _state.value = _state.value.copy(newChatSubject = s)
    }

    fun updateNewChatCategory(c: String) {
        _state.value = _state.value.copy(newChatCategory = c)
    }

    fun updateNewChatMessage(m: String) {
        _state.value = _state.value.copy(newChatMessage = m)
    }

    fun submitNewChat(onSuccess: (Int) -> Unit) {
        val st = _state.value
        if (st.newChatMessage.trim().isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Vui lòng nhập tin nhắn")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.createConversation(
                st.newChatSubject.ifEmpty { "Hỗ trợ chung" },
                st.newChatCategory,
                st.newChatMessage.trim()
            ).onSuccess { convId ->
                _state.value = _state.value.copy(isLoading = false, showNewChatDialog = false)
                loadConversations()
                // Find the new conversation and open it
                repository.getConversations().onSuccess { convs ->
                    convs.firstOrNull { it.id == convId }?.let { conv ->
                        openConversation(conv)
                    }
                }
                onSuccess(convId)
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể tạo cuộc trò chuyện"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun refresh() {
        when (_state.value.selectedTab) {
            0 -> loadFAQs(_state.value.selectedCategory)
            1 -> {
                if (_state.value.currentConversation != null) {
                    loadMessages(_state.value.currentConversation!!.id)
                } else {
                    loadConversations()
                }
            }
        }
    }
}

// ========== SCREEN ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.currentConversation != null) {
                // Chat detail view
                ChatDetailView(
                    conversation = state.currentConversation!!,
                    messages = state.messages,
                    newMessage = state.newMessage,
                    isSending = state.isSending,
                    onBack = { viewModel.backFromConversation() },
                    onMessageChange = { viewModel.updateNewMessage(it) },
                    onSend = { viewModel.sendMessage() },
                    isLoading = state.isLoading
                )
            } else {
                // Support main view
                AppTopBar(
                    title = "Hỗ trợ & Trợ giúp",
                    onBackClick = onBack
                )

                // Tab selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        title = "Câu hỏi thường gặp",
                        isSelected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabButton(
                        title = "Nhắn tư vấn viên",
                        isSelected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        modifier = Modifier.weight(1f),
                        badge = state.unreadCount.takeIf { it > 0 }
                    )
                }

                when (state.selectedTab) {
                    0 -> FAQTab(
                        faqs = state.faqs,
                        categories = state.faqCategories,
                        selectedCategory = state.selectedCategory,
                        expandedFaqId = state.expandedFaqId,
                        isLoading = state.isLoading,
                        onCategorySelect = { viewModel.loadFAQs(it) },
                        onToggleFaq = { viewModel.toggleFaq(it) },
                        onRateFaq = { id, helpful -> viewModel.rateFaq(id, helpful) }
                    )
                    1 -> ChatListTab(
                        conversations = state.conversations,
                        unreadCount = state.unreadCount,
                        isLoading = state.isLoading,
                        onConversationClick = { viewModel.openConversation(it) },
                        onNewChat = { viewModel.showNewChatDialog() },
                        onRefresh = { viewModel.loadConversations() }
                    )
                }
            }
        }

        // New chat dialog
        if (state.showNewChatDialog) {
            NewChatDialog(
                subject = state.newChatSubject,
                category = state.newChatCategory,
                message = state.newChatMessage,
                isLoading = state.isLoading,
                onSubjectChange = { viewModel.updateNewChatSubject(it) },
                onCategoryChange = { viewModel.updateNewChatCategory(it) },
                onMessageChange = { viewModel.updateNewChatMessage(it) },
                onDismiss = { viewModel.dismissNewChatDialog() },
                onSubmit = { viewModel.submitNewChat {} }
            )
        }

        if (state.isLoading && state.faqs.isEmpty() && state.conversations.isEmpty()) {
            LoadingOverlay()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

// ========== FAQ TAB ==========
@Composable
private fun FAQTab(
    faqs: List<FaqDto>,
    categories: List<FaqCategoryDto>,
    selectedCategory: String,
    expandedFaqId: Int?,
    isLoading: Boolean,
    onCategorySelect: (String) -> Unit,
    onToggleFaq: (Int) -> Unit,
    onRateFaq: (Int, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Category chips
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category filter row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label = "Tất cả",
                        isSelected = selectedCategory == "all",
                        color = PrimaryPurple,
                        onClick = { onCategorySelect("all") }
                    )
                    categories.take(4).forEach { cat ->
                        val chipColor = try {
                            Color(android.graphics.Color.parseColor(cat.color))
                        } catch (e: Exception) {
                            PrimaryPurple
                        }
                        CategoryChip(
                            label = cat.label,
                            isSelected = selectedCategory == cat.key,
                            color = chipColor,
                            onClick = { onCategorySelect(cat.key) }
                        )
                    }
                }
            }

            // FAQ items
            if (faqs.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QuestionAnswer,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Không có câu hỏi nào",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            itemsIndexed(faqs) { _, faq ->
                FAQItem(
                    faq = faq,
                    isExpanded = expandedFaqId == faq.id,
                    onToggle = { onToggleFaq(faq.id) },
                    onRate = { helpful -> onRateFaq(faq.id, helpful) }
                )
            }
        }
    }
}

@Composable
private fun FAQItem(
    faq: FaqDto,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRate: (Boolean) -> Unit
) {
    var showRating by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QuestionMark,
                        null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = faq.question,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = faq.answer,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Câu hỏi này có hữu ích không?",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { onRate(true) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ThumbUp,
                                    null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hữu ích", color = AccentGreen, fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { onRate(false) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ThumbDown,
                                    null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Không", color = AccentRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else DarkCard,
        border = if (isSelected) BorderStroke(1.5.dp, color) else null
    ) {
        Text(
            text = label,
            color = if (isSelected) color else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ========== CHAT LIST TAB ==========
@Composable
private fun ChatListTab(
    conversations: List<ConsultantConversationDto>,
    unreadCount: Int,
    isLoading: Boolean,
    onConversationClick: (ConsultantConversationDto) -> Unit,
    onNewChat: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // New chat button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onNewChat),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentGreen, AccentGreenLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bắt đầu cuộc trò chuyện",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Nhắn tin cho nhân viên tư vấn",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = TextSecondary
                )
            }
        }

        // Conversation list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (conversations.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Chưa có cuộc trò chuyện nào",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nhấn nút trên để bắt đầu",
                                color = TextHint,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            items(conversations) { conv ->
                ConversationItem(
                    conversation = conv,
                    onClick = { onConversationClick(conv) }
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ConsultantConversationDto,
    onClick: () -> Unit
) {
    val statusColor = when (conversation.status) {
        "active" -> AccentGreen
        "waiting" -> AccentOrange
        "resolved" -> AccentBlue
        "closed" -> TextSecondary
        else -> TextSecondary
    }

    val statusLabel = when (conversation.status) {
        "active" -> "Đang trò chuyện"
        "waiting" -> "Chờ phản hồi"
        "resolved" -> "Đã giải quyết"
        "closed" -> "Đã đóng"
        else -> conversation.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentBlue, PrimaryPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.subject ?: "Hỗ trợ chung",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AccentRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (conversation.lastMessage != null) {
                    Text(
                        text = conversation.lastMessage,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 11.sp
                        )
                    }
                    if (conversation.lastMessageAt != null) {
                        Text(
                            text = formatRelativeTime(conversation.lastMessageAt),
                            color = TextHint,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ========== CHAT DETAIL VIEW ==========
@Composable
private fun ChatDetailView(
    conversation: ConsultantConversationDto,
    messages: List<ConsultantMessageDto>,
    newMessage: String,
    isSending: Boolean,
    onBack: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    val listState = rememberScrollState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollTo(listState.maxValue)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard)
                .padding(top = 32.dp) // status bar
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentBlue, PrimaryPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.subject ?: "Hỗ trợ chung",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (conversation.status) {
                        "active" -> "Đang trò chuyện"
                        "waiting" -> "Chờ phản hồi..."
                        "resolved" -> "Đã giải quyết"
                        else -> conversation.status
                    },
                    color = when (conversation.status) {
                        "active" -> AccentGreen
                        "waiting" -> AccentOrange
                        else -> TextSecondary
                    },
                    fontSize = 12.sp
                )
            }
        }

        // Messages
        if (isLoading && messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(listState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Chat,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Bắt đầu cuộc trò chuyện",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                messages.forEach { msg ->
                    ConsultantMessageBubble(
                        message = msg.message,
                        senderName = msg.senderName,
                        senderType = msg.senderType,
                        timestamp = msg.createdAt,
                        isOwn = false
                    )
                }
            }
        }

        // Quick replies
        if (conversation.status == "active" || conversation.status == "waiting") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cảm ơn", "Đã hiểu", "Cần hỗ trợ thêm").forEach { reply ->
                    SuggestionChip(
                        onClick = { onMessageChange(reply) },
                        label = { Text(reply, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Input bar
        if (conversation.status != "closed") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkCard)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newMessage,
                    onValueChange = onMessageChange,
                    placeholder = {
                        Text(
                            "Nhắn tin tư vấn viên...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkCard,
                        focusedContainerColor = DarkCard,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (newMessage.isNotBlank() && !isSending) PrimaryPurple else DarkCard.copy(alpha = 0.5f))
                        .clickable(enabled = newMessage.isNotBlank() && !isSending) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            null,
                            tint = if (newMessage.isNotBlank()) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // Conversation closed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cuộc trò chuyện đã đóng",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsultantMessageBubble(
    message: String,
    senderName: String?,
    senderType: String,
    timestamp: String,
    isOwn: Boolean
) {
    val isConsultant = senderType == "consultant"
    val bubbleColor = if (isConsultant) PrimaryPurple else DarkCard.copy(alpha = 0.8f)
    val textColor = if (isConsultant) Color.White else TextPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
    ) {
        if (!isConsultant && !senderName.isNullOrEmpty()) {
            Text(
                text = senderName,
                color = AccentGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        if (isConsultant) {
            Text(
                text = "Tư vấn viên",
                color = PrimaryPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwn) 16.dp else 4.dp,
                        bottomEnd = if (isOwn) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        Text(
            text = formatTime(timestamp),
            color = TextHint,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
        )
    }
}

// ========== NEW CHAT DIALOG ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatDialog(
    subject: String,
    category: String,
    message: String,
    isLoading: Boolean,
    onSubjectChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val categories = listOf(
        "general" to "Câu hỏi chung",
        "booking" to "Đặt xe & Chuyến đi",
        "payment" to "Thanh toán",
        "complaint" to "Khiếu nại",
        "technical" to "Kỹ thuật",
        "billing" to "Hóa đơn"
    )
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SupportAgent,
                    null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Liên hệ tư vấn viên",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Chủ đề",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    placeholder = { Text("VD: Cần hỗ trợ về đặt xe", color = TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        cursorColor = PrimaryPurple,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    "Loại vấn đề",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: "Câu hỏi chung",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = TextPrimary) },
                                onClick = {
                                    onCategoryChange(key)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Tin nhắn đầu tiên",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Mô tả vấn đề của bạn...", color = TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        cursorColor = PrimaryPurple,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            GradientButton(
                text = "Gửi",
                onClick = onSubmit,
                enabled = message.trim().isNotEmpty() && !isLoading,
                isLoading = isLoading
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextSecondary)
            }
        }
    )
}

// ========== HELPERS ==========
@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PrimaryPurple else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (badge != null && badge > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badge > 9) "9+" else badge.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return timestamp.takeLast(5)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        timestamp.takeLast(8).take(5)
    }
}

private fun formatRelativeTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return ""
        val now = Date()
        val diff = now.time - date.time
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "${minutes.toInt()} phút"
            hours < 24 -> "${hours.toInt()} giờ"
            days < 7 -> "${days.toInt()} ngày"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        ""
    }
}
