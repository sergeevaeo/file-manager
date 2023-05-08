package com.vk.filemanager

import android.content.Context
import androidx.room.*


@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: Int,
    val name: String,
)

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)
    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getFileById(id: Int): FileEntity?
}

@Database(entities = [FileEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}

object AppDatabaseHolder {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "databaseHashes"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = instance
            instance
        }
    }
}




