package com.cuanz.rentalbilling.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuanz.rentalbilling.repo.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {
    var error by mutableStateOf<String?>(null)
        private set

    fun lookupByPhone(phone: String, onFound: (Long) -> Unit) {
        viewModelScope.launch {
            val members = repo.observeMembers().first()
            val match = members.firstOrNull { it.phone == phone }
            if (match != null) {
                error = null
                onFound(match.id)
            } else {
                error = "Member not found. Ask admin to register your number."
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onAdmin: () -> Unit,
    onMember: (Long) -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    var phone by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rental Billing",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("Phone & Tablet Rental — Indonesia", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(48.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Member Login", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                vm.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { vm.lookupByPhone(phone, onMember) },
                    enabled = phone.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue as Member") }
            }
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onAdmin) { Text("I'm an Admin →") }
    }
}
