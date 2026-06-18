package com.abdulin.nutritionapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.abdulin.nutritionapp.data.local.AppDatabase
import com.abdulin.nutritionapp.data.local.dao.DiaryDao
import com.abdulin.nutritionapp.data.local.dao.MealPlanDao
import com.abdulin.nutritionapp.data.local.dao.SavedMealTemplateDao
import com.abdulin.nutritionapp.data.local.dao.WaterDao
import com.abdulin.nutritionapp.data.local.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "nutrition_app_db"
    private const val PREFS_NAME = "db_security_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val KEY_DB_ENCRYPTED = "db_encrypted_v1"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        SQLiteDatabase.loadLibs(context)

        val securePrefs = securePrefs(context)
        resetLegacyUnencryptedDatabaseIfNeeded(context, securePrefs.getBoolean(KEY_DB_ENCRYPTED, false))
        val passphrase = getOrCreatePassphrase(securePrefs)
        val supportFactory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .openHelperFactory(supportFactory)
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    securePrefs.edit().putBoolean(KEY_DB_ENCRYPTED, true).apply()
                }
            })
            .build()
    }

    @Provides
    fun provideWaterDao(db: AppDatabase): WaterDao = db.waterDao()

    @Provides
    fun provideWeightDao(db: AppDatabase): WeightDao = db.weightDao()

    @Provides
    fun provideDiaryDao(db: AppDatabase): DiaryDao = db.diaryDao()

    @Provides
    fun provideMealPlanDao(db: AppDatabase): MealPlanDao = db.mealPlanDao()

    @Provides
    fun provideSavedMealTemplateDao(db: AppDatabase): SavedMealTemplateDao = db.savedMealTemplateDao()

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun getOrCreatePassphrase(securePrefs: android.content.SharedPreferences): ByteArray {
        val existing = securePrefs.getString(KEY_DB_PASSPHRASE, null)
        val phrase = existing ?: UUID.randomUUID().toString().also {
            securePrefs.edit().putString(KEY_DB_PASSPHRASE, it).apply()
        }
        return SQLiteDatabase.getBytes(phrase.toCharArray())
    }

    private fun resetLegacyUnencryptedDatabaseIfNeeded(context: Context, isEncryptedReady: Boolean) {
        if (isEncryptedReady) {
            return
        }

        context.deleteDatabase(DATABASE_NAME)
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        dbPath.delete()
        dbPath.parentFile?.resolve("$DATABASE_NAME-shm")?.delete()
        dbPath.parentFile?.resolve("$DATABASE_NAME-wal")?.delete()
        dbPath.parentFile?.resolve("$DATABASE_NAME-journal")?.delete()
    }
}
