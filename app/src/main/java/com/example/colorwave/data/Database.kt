package com.example.colorwave.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val login: String,
    val passwordHash: String
)

@Entity(tableName = "saved_palettes")
data class PaletteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userLogin: String, 
    val trackName: String,
    val colorsHex: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AppDao {
    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun getUser(login: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerUser(user: UserEntity)

    @Query("SELECT * FROM saved_palettes WHERE userLogin = :login ORDER BY timestamp DESC")
    fun getPalettesForUser(login: String): Flow<List<PaletteEntity>>

    @Insert
    suspend fun insertPalette(palette: PaletteEntity)

    @Delete
    suspend fun deletePalette(palette: PaletteEntity)
}

@Database(entities = [UserEntity::class, PaletteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "colorwave_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
