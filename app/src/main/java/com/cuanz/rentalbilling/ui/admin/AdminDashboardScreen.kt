package com.cuanz.rentalbilling.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuanz.rentalbilling.repo.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminStats(
    val devicesCount: Int = 0,
    val membersCount: Int = 0,
    val activeSessions: Int = 0,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    repo: RentalRepository
) : ViewModel() {
    private val _stats = MutableStateFlow(AdminStats())
    val stats = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.observeDevices(),
                repo.observeMembers(),
                repo.observeActiveSessions(),
            ) { d, m, a -> AdminStats(d.size, m.size, a.size) }
                .collect { _stats.value = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onDevices: () -> Unit,
    onMembers: () -> Unit,
    onSessions: () -> Unit,
    onReports: () -> Unit,
    vm: AdminDashboardViewModel = hiltViewModel()
) {
    val stats by vm.stats.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Admin Dashboard") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Devices", stats.devicesCount, Modifier.weight(1f))
                StatCard("Members", stats.membersCount, Modifier.weight(1f))
                StatCard("Active", stats.activeSessions, Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            ListItem(
                leadingContent = { Icon(Icons.Filled.Devices, null) },
                headlineContent = { Text("Devices") },
                supportingContent = { Text("Manage phone & tablet inventory") },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onDevices)
            )
            Divider()
            ListItem(
                leadingContent = { Icon(Icons.Filled.People, null) },
                headlineContent = { Text("Members") },
                supportingContent = { Text("Register, top-up, set tier") },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onMembers)
            )
            Divider()
            ListItem(
                leadingContent = { Icon(Icons.Filled.Receipt, null) },
                headlineContent = { Text("Sessions") },
                supportingContent = { Text("Start, monitor, close rentals") },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSessions)
            )
            Divider()
            ListItem(
                leadingContent = { Icon(Icons.Filled.Assessment, null) },
                headlineContent = { Text("Reports") },
                supportingContent = { Text("Daily revenue & session metrics") },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onReports)
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}
