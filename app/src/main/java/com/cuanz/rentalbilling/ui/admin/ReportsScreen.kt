package com.cuanz.rentalbilling.ui.admin

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
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.SessionStatus
import com.cuanz.rentalbilling.repo.RentalRepository
import com.cuanz.rentalbilling.util.Format
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

data class DailyReport(
    val dateLabel: String = "",
    val rangeStart: Long = 0L,
    val rangeEnd: Long = 0L,
    val sessions: List<RentalSession> = emptyList(),
    val revenue: Long = 0L,
    val avgDurationMin: Int = 0,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repo: RentalRepository
) : ViewModel() {

    private val _report = MutableStateFlow(DailyReport())
    val report = _report.asStateFlow()

    init { loadToday() }

    fun loadToday() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60 * 60 * 1000

        viewModelScope.launch {
            repo.observeRecentSessions(500).collect { all ->
                val today = all.filter {
                    it.status == SessionStatus.ENDED &&
                        (it.endedAt ?: 0L) in start until end
                }
                val revenue = today.sumOf { it.finalBill ?: 0L }
                val durations = today.mapNotNull { s ->
                    val end2 = s.endedAt ?: return@mapNotNull null
                    ((end2 - s.startedAt) / 60_000L).toInt()
                }
                val avg = if (durations.isEmpty()) 0 else durations.sum() / durations.size
                _report.value = DailyReport(
                    dateLabel = Format.date(start),
                    rangeStart = start,
                    rangeEnd = end,
                    sessions = today,
                    revenue = revenue,
                    avgDurationMin = avg,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit, vm: ReportsViewModel = hiltViewModel()) {
    val r by vm.report.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Report") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text(r.dateLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Revenue", Format.rupiah(r.revenue), Modifier.weight(1f))
                MetricCard("Sessions", r.sessions.size.toString(), Modifier.weight(1f))
                MetricCard("Avg dur.", Format.minutesToHm(r.avgDurationMin), Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Text("Sessions today", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(r.sessions, key = { it.id }) { s ->
                    ListItem(
                        headlineContent = { Text("Session #${s.id} · device ${s.deviceId}") },
                        supportingContent = {
                            Text(
                                "${Format.datetime(s.startedAt)} → ${s.endedAt?.let { Format.datetime(it) } ?: "—"}"
                            )
                        },
                        trailingContent = { Text(Format.rupiah(s.finalBill ?: 0L)) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
