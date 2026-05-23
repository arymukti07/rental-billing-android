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
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.DeviceType
import com.cuanz.rentalbilling.repo.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {
    val devices = repo.observeDevices()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun add(code: String, name: String, type: DeviceType, hourlyRate: Long) {
        viewModelScope.launch {
            repo.upsertDevice(Device(code = code, name = name, type = type, hourlyRate = hourlyRate))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(onBack: () -> Unit, vm: DevicesViewModel = hiltViewModel()) {
    val list by vm.devices.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { pad ->
        LazyColumn(modifier = Modifier.padding(pad)) {
            items(list, key = { it.id }) { d ->
                ListItem(
                    headlineContent = { Text("${d.code} — ${d.name}") },
                    supportingContent = { Text("${d.type} · Rp ${d.hourlyRate}/hour · ${d.status}") }
                )
                Divider()
            }
        }
    }

    if (showAdd) {
        AddDeviceDialog(
            onDismiss = { showAdd = false },
            onConfirm = { code, name, type, rate ->
                vm.add(code, name, type, rate)
                showAdd = false
            }
        )
    }
}

@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, DeviceType, Long) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10000") }
    var type by remember { mutableStateOf(DeviceType.TABLET) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device") },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = rate, onValueChange = { rate = it.filter { c -> c.isDigit() } }, label = { Text("Hourly rate (IDR)") })
                Spacer(Modifier.height(8.dp))
                Row {
                    FilterChip(selected = type == DeviceType.TABLET, onClick = { type = DeviceType.TABLET }, label = { Text("Tablet") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = type == DeviceType.PHONE, onClick = { type = DeviceType.PHONE }, label = { Text("Phone") })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(code, name, type, rate.toLongOrNull() ?: 0L) },
                enabled = code.isNotBlank() && name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
