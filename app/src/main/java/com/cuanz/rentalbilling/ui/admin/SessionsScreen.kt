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
import com.cuanz.rentalbilling.billing.BillingEngine
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.DeviceStatus
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.SessionStatus
import com.cuanz.rentalbilling.repo.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionsState(
    val active: List<RentalSession> = emptyList(),
    val devices: List<Device> = emptyList(),
    val members: List<Member> = emptyList(),
)

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {
    val state = combine(
        repo.observeActiveSessions(),
        repo.observeDevices(),
        repo.observeMembers(),
    ) { a, d, m -> SessionsState(a, d, m) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionsState())

    fun start(deviceId: Long, memberId: Long?, plannedMinutes: Int?, packageRate: Long?) {
        viewModelScope.launch {
            repo.startSession(deviceId, memberId, plannedMinutes, packageRate)
        }
    }

    fun stop(sessionId: Long) {
        viewModelScope.launch { repo.endSession(sessionId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(onBack: () -> Unit, vm: SessionsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    var showStart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Sessions") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showStart = true }) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Start")
            }
        }
    ) { pad ->
        LazyColumn(modifier = Modifier.padding(pad)) {
            items(s.active, key = { it.id }) { session ->
                val device = s.devices.find { it.id == session.deviceId }
                val member = s.members.find { it.id == session.memberId }
                val bill = BillingEngine.computeNow(session)
                ListItem(
                    headlineContent = { Text(device?.let { "${it.code} — ${it.name}" } ?: "Device #${session.deviceId}") },
                    supportingContent = {
                        Text("${member?.name ?: "Walk-in"} · ${bill.grossMinutes} min · Rp ${bill.total}")
                    },
                    trailingContent = {
                        TextButton(onClick = { vm.stop(session.id) }) { Text("Stop") }
                    }
                )
                Divider()
            }
        }
    }

    if (showStart) {
        StartSessionDialog(
            devices = s.devices.filter { it.status == DeviceStatus.AVAILABLE },
            members = s.members,
            onDismiss = { showStart = false },
            onConfirm = { d, m, planned, pkgRate ->
                vm.start(d, m, planned, pkgRate)
                showStart = false
            }
        )
    }
}

@Composable
private fun StartSessionDialog(
    devices: List<Device>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long?, Int?, Long?) -> Unit
) {
    var device by remember { mutableStateOf<Device?>(devices.firstOrNull()) }
    var member by remember { mutableStateOf<Member?>(null) }
    var planned by remember { mutableStateOf("") }
    var packageRate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Session") },
        text = {
            Column {
                Text("Device", style = MaterialTheme.typography.labelMedium)
                devices.forEach { d ->
                    FilterChip(
                        selected = device?.id == d.id,
                        onClick = { device = d },
                        label = { Text("${d.code}") }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Member (optional)", style = MaterialTheme.typography.labelMedium)
                FilterChip(selected = member == null, onClick = { member = null }, label = { Text("Walk-in") })
                members.take(20).forEach { m ->
                    FilterChip(selected = member?.id == m.id, onClick = { member = m }, label = { Text(m.name) })
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = planned,
                    onValueChange = { planned = it.filter { c -> c.isDigit() } },
                    label = { Text("Planned minutes (optional, for package)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = packageRate,
                    onValueChange = { packageRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Package rate IDR (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val d = device ?: return@TextButton
                    onConfirm(
                        d.id,
                        member?.id,
                        planned.toIntOrNull(),
                        packageRate.toLongOrNull()
                    )
                },
                enabled = device != null
            ) { Text("Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
