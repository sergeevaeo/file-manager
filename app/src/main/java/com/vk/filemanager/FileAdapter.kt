package com.vk.filemanager

import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.vk.filemanager.R
import java.io.File
import java.util.*


class FileAdapter(private val filesList: Array<File>) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // Создание нового ViewHolder для элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    // Привязка данных из списка к элементу списка в RecyclerView
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = filesList[position]
        holder.bind(file)
        holder.itemView.setOnClickListener {
            if (file.isDirectory) {
                val path: String = file.absolutePath.toString()
                val intent = Intent(holder.itemView.context, FileListActivity::class.java)
                intent.putExtra("path", path)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                holder.itemView.context.startActivity(intent)
            } else {
                // при нажатии на файл открывается меню открыть/отправить
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.inflate(R.menu.file_menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.open -> {
                            // открытие картинки
                            try {
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                val type = "image/*"
                                intent.setDataAndType(Uri.parse(file.absolutePath), type)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                holder.itemView.context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(holder.itemView.context.applicationContext, "Cannot open the file", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                        R.id.send -> {
                            val fileUri = FileProvider.getUriForFile(
                                holder.itemView.context,
                                holder.itemView.context.applicationContext.packageName + ".provider",
                                file
                            )
                            // создаем Intent с действием ACTION_SEND и типом данных файла
                            val sendIntent = Intent(Intent.ACTION_SEND)
                            sendIntent.type = holder.itemView.context.contentResolver.getType(fileUri)
                            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            // вызываем стандартное меню отправки
                            holder.itemView.context.startActivity(Intent.createChooser(sendIntent, "Отправить"))
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

    }


    // Количество элементов в списке
    override fun getItemCount(): Int {
        return filesList.size
    }

    private fun getFileSize(file: File): String {
        val length = file.length()
        val units = arrayOf("B", "KB", "MB", "GB", "TB")

        var fileSize = length.toDouble()
        var unitIndex = 0

        while (fileSize > 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }

        return "%.2f %s".format(fileSize, units[unitIndex])
    }

    // Класс ViewHolder для элемента списка
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        private val fileSizeTextView: TextView = itemView.findViewById(R.id.fileSize)
        private val fileDateTextView: TextView = itemView.findViewById(R.id.fileCreationDate)
        private val fileIconImageView: ImageView = itemView.findViewById(R.id.fileIcon)

        // Привязка данных к элементу списка
        fun bind(file: File) {
            fileNameTextView.text = file.name
            fileSizeTextView.text = getFileSize(file)
            fileDateTextView.text = Date(file.lastModified()).toString()
            var iconResource = when (file.extension) {
                "png", "jpeg" -> R.drawable.picture
                "pdf", "txt" -> R.drawable.document
                else -> R.drawable.file
            }
            if (file.isDirectory) iconResource = R.drawable.dir
            fileIconImageView.setImageResource(iconResource)

        }
    }
}