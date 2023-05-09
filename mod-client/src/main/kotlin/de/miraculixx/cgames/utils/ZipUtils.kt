package de.miraculixx.cgames.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object ZipUtils {
    fun unZipFolder(zipFilePath: String, outputPath: String) {
        val zipInputStream = ZipInputStream(FileInputStream(zipFilePath))
        var zipEntry: ZipEntry
        var zipEntryName: String
        while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
            zipEntryName = zipEntry.name
            if (zipEntry.isDirectory) {
                zipEntryName = zipEntryName.substring(0, zipEntryName.length - 1)
                val folder = File(outputPath + File.separator + zipEntryName)
                folder.mkdirs()
            } else {
                val file = File(outputPath + File.separator + zipEntryName)
                file.createNewFile()
                val fileOutputStream = FileOutputStream(file)
                var len: Int
                val buffer = ByteArray(1024)
                while (zipInputStream.read(buffer).also { len = it } != -1) {
                    fileOutputStream.write(buffer, 0, len)
                    fileOutputStream.flush()
                }
                fileOutputStream.close()
            }
        }
        zipInputStream.close()
    }
}