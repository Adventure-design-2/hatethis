package com.example.hatethis.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "date_records")
data class DateRecordEntity(
    @PrimaryKey val recordId: String,
    val partnerA: String,
    val partnerB: String,
    val missionStatus: String,
    val emotion: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface DateRecordDao {
    @Query("SELECT * FROM date_records")
    suspend fun getAllRecords(): List<DateRecordEntity>

    @Query("SELECT * FROM date_records WHERE recordId = :id")
    suspend fun getRecordById(id: String): DateRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DateRecordEntity)

    @Delete
    suspend fun deleteRecord(record: DateRecordEntity)
}

@Database(entities = [DateRecordEntity::class], version = 1, exportSchema = false)
abstract class DateRecordDatabase : RoomDatabase() {
    abstract fun dateRecordDao(): DateRecordDao

    companion object {
        @Volatile
        private var INSTANCE: DateRecordDatabase? = null

        fun getDatabase(context: Context): DateRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DateRecordDatabase::class.java,
                    "date_record_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LocalDataStore(context: Context) {
    private val database = DateRecordDatabase.getDatabase(context)

    suspend fun saveRecord(record: DateRecordEntity) {
        withContext(Dispatchers.IO) {
            database.dateRecordDao().insertRecord(record)
        }
    }

    suspend fun getAllRecords(): List<DateRecordEntity> {
        return withContext(Dispatchers.IO) {
            database.dateRecordDao().getAllRecords()
        }
    }

    suspend fun getRecordById(recordId: String): DateRecordEntity? {
        return withContext(Dispatchers.IO) {
            database.dateRecordDao().getRecordById(recordId)
        }
    }
}
