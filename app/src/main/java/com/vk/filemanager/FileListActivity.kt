package com.vk.filemanager

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.widget.ImageButton
import com.vk.filemanager.R
import java.io.File

class FileListActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.fileList)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val path = intent.getStringExtra("path")
        val root = File(path!!)
        val filesAndFolders = root.listFiles()

        // сортировка файлов
        if (filesAndFolders != null) {
            filesAndFolders.sortBy { it.name }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = FileAdapter(filesAndFolders)
            val settingsButton = findViewById<ImageButton>(R.id.sorting)
            settingsButton.setOnClickListener {
                val popupMenu = PopupMenu(this, settingsButton)
                popupMenu.inflate(R.menu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {

                        R.id.sort_by_name -> {
                            filesAndFolders.sortBy { it.name }
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = FileAdapter(filesAndFolders)
                            //  по названию
                            true
                        }


                        R.id.sort_by_size -> {
                            val popupMenuInner = PopupMenu(it.context, it)
                            popupMenuInner.inflate(R.menu.sort_menu)
                            popupMenuInner.setOnMenuItemClickListener { menuItemInner ->
                                when (menuItemInner.itemId) {
                                    R.id.increasing -> {
                                        filesAndFolders.sortBy { it.length() }
                                        recyclerView.layoutManager = LinearLayoutManager(this)
                                        recyclerView.adapter = FileAdapter(filesAndFolders)
                                        true
                                    }
                                    R.id.decreasing -> {
                                        filesAndFolders.sortByDescending { it.length() }
                                        recyclerView.layoutManager = LinearLayoutManager(this)
                                        recyclerView.adapter = FileAdapter(filesAndFolders)
                                        true
                                    }
                                    else -> false
                                }
                            }
                            popupMenuInner.show()
                            true
                        }

                        R.id.sort_by_extension -> {
                            filesAndFolders.sortBy { it.extension }
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = FileAdapter(filesAndFolders)
                            // по расширению
                            true
                        }

                        R.id.sort_by_date -> {
                            filesAndFolders.sortBy { it.lastModified() }
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = FileAdapter(filesAndFolders)
                            // по дате
                            true
                        }

                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }
}