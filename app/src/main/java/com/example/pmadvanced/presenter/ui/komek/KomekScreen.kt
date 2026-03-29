package com.example.pmadvanced.presenter.ui.komek


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pmadvanced.data.model.HelpApplication
import com.example.pmadvanced.data.model.HelpRequest
import com.example.pmadvanced.presenter.ui.komek.KomekViewModel
import com.example.pmadvanced.ui.theme.White

@Composable
fun KomekScreen(currentUserId: Int?) {
    val viewModel: KomekViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf<Int?>(null) } 

    val tabs = listOf("All Requests", "My Requests")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.loadMyRequests()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Komek", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) White else Color.Gray) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = White)
                }
            } else {
                when (selectedTab) {
                    0 -> AllRequestsTab(
                        requests = uiState.openRequests,
                        currentUserId = currentUserId,
                        onApply = { showApplyDialog = it },
                        onRefresh = { viewModel.loadOpenRequests() }
                    )
                    1 -> MyRequestsTab(
                        requests = uiState.myRequests,
                        currentUserId = currentUserId,
                        onCancel = { viewModel.cancelRequest(it) },
                        onAccept = { reqId, appId -> viewModel.acceptApplication(reqId, appId) },
                        onReject = { reqId, appId -> viewModel.rejectApplication(reqId, appId) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = White,
            contentColor = Color.Black
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create Request")
        }

        uiState.successMessage?.let {
            LaunchedEffect(it) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearMessages()
            }
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                containerColor = Color.DarkGray
            ) { Text(it, color = White) }
        }
    }

    if (showCreateDialog) {
        CreateRequestDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, cat, days ->
                viewModel.createRequest(title, desc, cat, days)
                showCreateDialog = false
            }
        )
    }

    showApplyDialog?.let { requestId ->
        ApplyDialog(
            onDismiss = { showApplyDialog = null },
            onApply = { message ->
                viewModel.applyToRequest(requestId, message)
                showApplyDialog = null
            }
        )
    }
}

@Composable
fun AllRequestsTab(
    requests: List<HelpRequest>,
    currentUserId: Int?,
    onApply: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No open requests", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(requests) { req ->
                RequestCard(
                    request = req,
                    currentUserId = currentUserId,
                    showApplyButton = req.requesterId != currentUserId,
                    onApply = { onApply(req.id) }
                )
            }
        }
    }
}

@Composable
fun MyRequestsTab(
    requests: List<HelpRequest>,
    currentUserId: Int?,
    onCancel: (Int) -> Unit,
    onAccept: (Int, Int) -> Unit,
    onReject: (Int, Int) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You have no requests", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(requests) { req ->
                RequestCard(
                    request = req,
                    currentUserId = currentUserId,
                    showApplyButton = false,
                    showCancelButton = req.status in listOf("open", "in_progress"),
                    onCancel = { onCancel(req.id) },
                    showApplications = true,
                    onAccept = { appId -> onAccept(req.id, appId) },
                    onReject = { appId -> onReject(req.id, appId) }
                )
            }
        }
    }
}

@Composable
fun RequestCard(
    request: HelpRequest,
    currentUserId: Int?,
    showApplyButton: Boolean = false,
    showCancelButton: Boolean = false,
    showApplications: Boolean = false,
    onApply: () -> Unit = {},
    onCancel: () -> Unit = {},
    onAccept: (Int) -> Unit = {},
    onReject: (Int) -> Unit = {}
) {
    val statusColor = when (request.status) {
        "open" -> Color(0xFF4CAF50)
        "in_progress" -> Color(0xFFFFC107)
        "completed" -> Color(0xFF2196F3)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.title, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.2f)) {
                    Text(request.status, color = statusColor, fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF2A2A2A)) {
                Text(request.category, color = Color.Gray, fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(request.description, color = Color.LightGray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showApplyButton) {
                    Button(
                        onClick = onApply,
                        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Apply to Help") }
                }
                if (showCancelButton) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Cancel") }
                }
            }

            if (showApplications && request.applications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Applications (${request.applications.size})", color = Color.Gray, fontSize = 12.sp)
                request.applications.forEach { app ->
                    ApplicationItem(app = app, onAccept = { onAccept(app.id) }, onReject = { onReject(app.id) })
                }
            }
        }
    }
}

@Composable
fun ApplicationItem(app: HelpApplication, onAccept: () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Applicant #${app.applicantId}", color = White, fontSize = 13.sp)
            app.message?.let { Text(it, color = Color.Gray, fontSize = 12.sp) }
        }
        if (app.status == "pending") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onAccept) { Text("Accept", color = Color(0xFF4CAF50)) }
                TextButton(onClick = onReject) { Text("Reject", color = Color.Red) }
            }
        } else {
            Text(app.status, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun CreateRequestDialog(onDismiss: () -> Unit, onCreate: (String, String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("other") }
    var days by remember { mutableStateOf("3") }
    val categories = listOf("tutor", "physical", "rental", "other")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("New Help Request", color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Title", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White,
                        focusedBorderColor = White, unfocusedBorderColor = Color.Gray))

                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description", color = Color.Gray) }, minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White,
                        focusedBorderColor = White, unfocusedBorderColor = Color.Gray))

                Text("Category", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = White,
                                selectedLabelColor = Color.Black,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }

                OutlinedTextField(value = days, onValueChange = { days = it },
                    label = { Text("Expires in days (1-30)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White,
                        focusedBorderColor = White, unfocusedBorderColor = Color.Gray))
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, description, category, days.toIntOrNull() ?: 3) },
                enabled = title.length >= 3 && description.length >= 10,
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun ApplyDialog(onDismiss: () -> Unit, onApply: (String?) -> Unit) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Apply to Help", color = White) },
        text = {
            OutlinedTextField(
                value = message, onValueChange = { message = it },
                label = { Text("Message (optional)", color = Color.Gray) }, minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White,
                    focusedBorderColor = White, unfocusedBorderColor = Color.Gray)
            )
        },
        confirmButton = {
            Button(onClick = { onApply(message.takeIf { it.isNotBlank() }) },
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black)
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}