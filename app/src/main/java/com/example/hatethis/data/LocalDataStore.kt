package com.example.hatethis.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hatethis.model.Mission
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader

// DateRecordEntity 정의
@Entity(tableName = "date_records")
data class DateRecordEntity(
    @PrimaryKey val recordId: String,          // 기록 고유 ID
    val partnerA: String,                      // 파트너 A의 사용자 ID
    val partnerB: String,                      // 파트너 B의 사용자 ID
    val missionStatus: String,                 // 미션 상태 (Enum 이름)
    val emotion: String?,                      // 감정 상태 (Optional Enum 이름)
    val photoUrls: String,                     // 사진 URL 리스트 (CSV 형식)
    val comments: String,                      // 댓글 리스트 (CSV 형식)
    val createdAt: Long,                       // 생성 시간 (타임스탬프)
    val updatedAt: Long                        // 마지막 업데이트 시간 (타임스탬프)
)

// MissionEntity 정의
@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val title: String,
    val environment: Int,
    val locationTag: String,
    val detail: String
) {
    fun toDomain(): Mission {
        return Mission(
            title = this.title,
            environment = this.environment,
            locationTag = this.locationTag.split(",").filter { it.isNotBlank() },
            detail = this.detail
        )
    }
}

// Mission -> MissionEntity 변환
fun Mission.toEntity(): MissionEntity {
    return MissionEntity(
        title = this.title,
        environment = this.environment,
        locationTag = this.locationTag.joinToString(","),
        detail = this.detail
    )
}


// DateRecordDao 정의
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

// MissionDao 정의
@Dao
interface MissionDao {
    @Query("SELECT * FROM missions")
    suspend fun getAllMissions(): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE title = :title")
    suspend fun getMissionByTitle(title: String): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    @Update
    suspend fun updateMission(mission: MissionEntity)
}

// Database 정의
@Database(entities = [DateRecordEntity::class, MissionEntity::class], version = 3, exportSchema = false)
abstract class DateRecordDatabase : RoomDatabase() {
    abstract fun dateRecordDao(): DateRecordDao
    abstract fun missionDao(): MissionDao

    companion object {
        @Volatile
        private var INSTANCE: DateRecordDatabase? = null

        // Room Database Migration
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS missions (" +
                            "title TEXT PRIMARY KEY NOT NULL, " +
                            "environment INTEGER NOT NULL, " +
                            "locationTag TEXT NOT NULL, " +
                            "detail TEXT NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): DateRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DateRecordDatabase::class.java,
                    "date_record_database"
                )
                    .addMigrations(MIGRATION_2_3) // 새로운 마이그레이션 추가
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// LocalDataStore 정의
class LocalDataStore(context: Context) {
    private val database = DateRecordDatabase.getDatabase(context)
    private val context = context.applicationContext
    // DateRecord 관련 함수들
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

    // Mission 관련 함수들
    suspend fun saveMission(mission: MissionEntity) {
        withContext(Dispatchers.IO) {
            database.missionDao().insertMission(mission)
        }
    }

    suspend fun saveMissions(missions: List<MissionEntity>) {
        withContext(Dispatchers.IO) {
            database.missionDao().insertMissions(missions)
        }
    }

    suspend fun getAllMissions(): List<MissionEntity> {
        return withContext(Dispatchers.IO) {
            database.missionDao().getAllMissions()
        }
    }

    suspend fun getMissionByTitle(title: String): MissionEntity? {
        return withContext(Dispatchers.IO) {
            database.missionDao().getMissionByTitle(title)
        }
    }

    suspend fun deleteMission(mission: MissionEntity) {
        withContext(Dispatchers.IO) {
            database.missionDao().deleteMission(mission)
        }
    }

    suspend fun updateMission(mission: MissionEntity) {
        withContext(Dispatchers.IO) {
            database.missionDao().updateMission(mission)
        }
    }

    // **추가된 함수: 기본 미션 데이터 저장**
    suspend fun initializeDefaultMissions(defaultMissions: List<MissionEntity>) {
        withContext(Dispatchers.IO) {
            val existingMissions = database.missionDao().getAllMissions()
            if (existingMissions.isEmpty()) {
                // 로컬 데이터베이스가 비어 있는 경우 기본 미션 저장
                database.missionDao().insertMissions(defaultMissions)
                println("Default missions initialized.") // 로그 출력
            }
        }
    }
    // JSON 파일에서 기본 미션 로드
    suspend fun loadMissionsFromJson() {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource("mission.json") // raw 디렉토리 내의 mission.json
                val reader = InputStreamReader(inputStream)
                val missionListType = object : TypeToken<List<Mission>>() {}.type
                val missions: List<Mission> = Gson().fromJson(reader, missionListType)
                saveMissions(missions.map { it.toEntity() }) // 로컬 데이터베이스에 저장
                reader.close()
                inputStream.close()
                println("Missions loaded from JSON: $missions")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper methods to convert between List<String> and String (CSV format)
    fun List<String>.toCsv(): String = joinToString(",")
    fun String.toList(): List<String> = split(",").filter { it.isNotBlank() }
}
