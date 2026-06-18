package com.abdulin.nutritionapp.presentation.health

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(
    viewModel: HealthConnectViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.health_connect_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.home_refresh))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                !state.isAvailable -> InfoCard(
                    title = stringResource(R.string.health_connect_unavailable_title),
                    subtitle = stringResource(R.string.health_connect_unavailable_subtitle)
                )
                !state.hasPermissions -> {
                    InfoCard(
                        title = stringResource(R.string.health_connect_permissions_title),
                        subtitle = stringResource(R.string.health_connect_permissions_subtitle)
                    )
                    Button(onClick = { permissionsLauncher.launch(viewModel.permissions) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.health_connect_permissions_action))
                    }
                }
                else -> {
                    InfoCard(
                        title = stringResource(R.string.health_connect_connected_title),
                        subtitle = stringResource(R.string.health_connect_connected_subtitle)
                    )
                    MetricCard(Icons.AutoMirrored.Filled.DirectionsRun, stringResource(R.string.home_steps), state.steps.toString())
                    MetricCard(Icons.Default.LocalFireDepartment, stringResource(R.string.home_activity), "${state.activeCalories.toInt()} ${stringResource(R.string.unit_kcal)}")
                    MetricCard(Icons.Default.MonitorWeight, stringResource(R.string.home_weight), state.latestWeightKg?.let { "$it ${stringResource(R.string.unit_kg)}" } ?: stringResource(R.string.no_data))
                    MetricCard(Icons.Default.Bedtime, stringResource(R.string.health_connect_sleep), formatSleep(state.sleepMinutes))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun MetricCard(icon: ImageVector, title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatSleep(minutes: Long): String {
    if (minutes <= 0) return "0 h"
    val hours = minutes / 60
    val rest = minutes % 60
    return "${hours}h ${rest}m"
}
