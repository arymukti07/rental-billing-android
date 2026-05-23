package com.cuanz.rentalbilling.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuanz.rentalbilling.billing.BillingEngine
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.Transaction
import com.cuanz.rentalbilling.repo.RentalRepository
import com.cuanz.rentalbilling.util.Format
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberDashState(
    val member: Member? = null,
    val activeSession: RentalSession? = null,
    val txns: List<Transaction> = emptyList(),
)

@HiltViewModel
class MemberDashboardViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MemberDashState())
    val state = _state.asStateFlow()

    private var bound = false

    fun bind(id: Long) {
        if (bound) return
        bound = true
        viewModelScope.launch {
            combine(
                repo.observeMembers().map { it.firstOrNull { m -> m.id == id } },
                repo.observeActiveSessions().map { it.firstOrNull { s -> s.memberId == id } },
                repo.observeMemberTxns(id),
            ) { m, s, t -> MemberDashState(m, s, t) }
                .collect { _state.value = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDashboardScreen(memberId: Long, onBack: () -> Unit, vm: MemberDashboardViewModel = hiltViewModel()) {
    LaunchedEffect(memberId) { vm.bind(memberId) }
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.member?.name ?: "Member") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Balance", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Format.rupiah(s.member?.balance ?: 0L),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(s.member?.tier ?: "REGULAR", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            s.activeSession?.let { session ->
                val bill = BillingEngine.computeNow(session)
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Active Rental", style = MaterialTheme.typography.titleMedium)
                        Text("${Format.minutesToHm(bill.grossMinutes)} elapsed")
                        Text("Current bill: ${Format.rupiah(bill.total)}")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(s.txns, key = { it.id }) { t ->
                    ListItem(
                        headlineContent = { Text("${t.kind} · ${Format.rupiah(t.amount)}") },
                        supportingContent = {
                            Text(
                                "${Format.datetime(t.createdAt)}${t.note?.let { n -> " · $n" } ?: ""}"
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
