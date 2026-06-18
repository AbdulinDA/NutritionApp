package com.abdulin.nutritionapp.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { 
        try { HealthConnectClient.getOrCreate(context) } catch (e: Exception) { null }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient?.permissionController?.getGrantedPermissions()
            ?.containsAll(permissions) ?: false
    }

    // --- READ ---
    suspend fun readStepsForToday(): Long {
        val client = healthConnectClient ?: return 0
        if (!hasAllPermissions()) return 0
        return try {
            val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
            val response = client.readRecords(
                ReadRecordsRequest(StepsRecord::class, timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now()))
            )
            response.records.sumOf { it.count }
        } catch (e: Exception) { 0 }
    }

    suspend fun readActiveCaloriesForToday(): Double {
        val client = healthConnectClient ?: return 0.0
        if (!hasAllPermissions()) return 0.0
        return try {
            val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
            val response = client.readRecords(
                ReadRecordsRequest(
                    ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now())
                )
            )
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) { 0.0 }
    }

    suspend fun readLatestWeightKg(): Double? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        ZonedDateTime.now().minusDays(30).toInstant(),
                        Instant.now()
                    )
                )
            )
            response.records.maxByOrNull { it.time }?.weight?.inKilograms
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readSleepMinutesLastNight(): Long {
        val client = healthConnectClient ?: return 0
        if (!hasAllPermissions()) return 0
        return try {
            val now = ZonedDateTime.now()
            val start = now.minusHours(24).toInstant()
            val response = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, Instant.now())
                )
            )
            response.records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        } catch (e: Exception) {
            0
        }
    }

    // --- WRITE ---
    suspend fun writeWater(amountMl: Int) {
        val client = healthConnectClient ?: return
        if (!hasAllPermissions()) return
        try {
            val record = HydrationRecord(
                startTime = Instant.now(),
                startZoneOffset = ZonedDateTime.now().offset,
                endTime = Instant.now(),
                endZoneOffset = ZonedDateTime.now().offset,
                volume = Volume.milliliters(amountMl.toDouble()),
                metadata = Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun writeWeight(weightKg: Double) {
        val client = healthConnectClient ?: return
        if (!hasAllPermissions()) return
        try {
            val record = WeightRecord(
                time = Instant.now(),
                zoneOffset = ZonedDateTime.now().offset,
                weight = Mass.kilograms(weightKg),
                metadata = Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) { e.printStackTrace() }
    }
}
