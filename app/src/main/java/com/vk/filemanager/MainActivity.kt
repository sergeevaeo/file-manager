package com.vk.filemanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private val storage: String = "/storage/emulated/0/Changed"
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.fileList)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Environment.isExternalStorageManager())){
            // Запрос разрешения
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            } else {
                TODO("VERSION.SDK_INT < R")
            }
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        val intent = Intent(this@MainActivity, FileListActivity::class.java)
        val path = Environment.getExternalStorageDirectory().absolutePath
        intent.putExtra("path", path)
        startActivity(intent)



        //создаем директорию Changed
        val dir = File(storage)
        if (!dir.exists()) {
            dir.mkdir()
        }

        // сохранение хэш-кода файлов в бд и проверка изменений
        fun saveFileHashesToDatabase() = CoroutineScope(Dispatchers.IO).launch {
            try {
                val files = File(Environment.getExternalStorageDirectory().absolutePath).listFiles()
                val db = AppDatabaseHolder.getInstance(applicationContext)
                val fileDao = db.fileDao()
                files!!.forEach { file ->
                        val fileHash = file.absolutePath.hashCode()
                        val fileEntity = fileDao.getFileById(fileHash)
                        if (fileEntity == null) {
                            val sourceFile = File(file.absolutePath)
                            val destinationDir = File(storage)
                            val copiedFile = File(destinationDir, sourceFile.name)
                            sourceFile.copyTo(copiedFile, overwrite = true)
                            fileDao.insertFile(
                                FileEntity(
                                    id = file.absolutePath.hashCode(),
                                    name = file.absolutePath,
                                )
                            )
                        }
                }
            } catch (e: Exception) {
                Log.e("File Hash Saving", "Error", e)
            }
        }
        for (file in dir.listFiles()!!) {
                file.delete()
        }
        saveFileHashesToDatabase()
    }

}









