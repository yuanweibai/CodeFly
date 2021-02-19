package com.code.lib.utils

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder

object Files {

    /**
     * 清除文件
     *
     * @param filePathList
     */
    fun clearFiles(filePathList: List<String>) {
        for (i in filePathList.indices) {
            val file = File(filePathList[i])
            if (file.exists()) {
                file.delete()
            }
        }
    }
}