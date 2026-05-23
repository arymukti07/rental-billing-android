package com.cuanz.rentalbilling.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.repo.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {
    val members = repo.observeMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun add(name: String, phone: String, email: String?) {
        viewModelScope.launch {
            repo.upsertMember(Member(name = name, phone = phone, email = email?.ifBlank { null }))
        }
    }

    fun topUp(memberId: Long, amount: Long) {
        viewModelScope.launch { repo.topUp(memberId, amount, "Admin top-up") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(onBack: () -> Unit, vm: MembersViewModel = hiltViewModel()) {
    val list by vm.members.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var topUpFor by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { pad ->
        LazyColumn(modifier = Modifier.padding(pad)) {
            items(list, key = { it.id }) { m ->
                ListItem(
                    headlineContent = { Text(m.name) },
                    supportingContent = { Text("${m.phone} · Rp ${m.balance} · ${m.tier}") },
                    trailingContent = {
                        TextButton(onClick = { topUpFor = m }) { Text("Top-up") }
                    }
                )
                Divider()
            }
        }
    }

    if (showAdd) {
        AddMemberDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, phone, email ->
                vm.add(name, phone, email)
                showAdd = false
            }
        )
    }

    topUpFor?.let { m ->
        TopUpDialog(
            member = m,
            onDismiss = { topUpFor = null },
            onConfirm = { amount ->
                vm.topUp(m.id, amount)
                topUpFor = null
            }
        )
    }
}

@Composable
private fun AddMemberDialog(onDismiss: () -> Unit, onConfirm: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (optional)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, phone, email.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TopUpDialog(member: Member, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var amount by remember { mutableStateOf("50000") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Top-up: ${member.name}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Amount (IDR)") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(amount.toLongOrNull() ?: 0L) },
                enabled = (amount.toLongOrNull() ?: 0L) > 0
            ) { Text("Top-up") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
