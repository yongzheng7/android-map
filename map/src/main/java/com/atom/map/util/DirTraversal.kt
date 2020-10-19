package com.atom.map.util

import java.io.File
import java.io.FileFilter
import java.util.*

class DirTraversal {
    companion object {
        //no recursion
        fun listLinkedFiles(strPath: String): LinkedList<File> {
            val list = LinkedList<File>()
            val dir = File(strPath)
            val file = dir.listFiles()
            for (i in file.indices) {
                list.add(file[i])
            }
            return list
        }
        // 遍历获取所有的zip文件
        fun listFiles(strPath: String): ArrayList<File>? {
            return refreshFileList(strPath , "zip")
        }

        fun refreshFileList(strPath: String , filter : String ): ArrayList<File>? {
            val filelist = ArrayList<File>()
            val dir = File(strPath)
            val files = dir.listFiles(object : FileFilter{
                override fun accept(pathname: File?): Boolean {
                    if(pathname == null) return false ;
                    if(pathname.isDirectory) return true
                    if(pathname.name.toLowerCase(Locale.ROOT).endsWith(filter)) return true
                    return false
                }
            })
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    refreshFileList(files[i].absolutePath , filter) ?.let {
                        filelist.addAll(it)
                    }
                } else {
                    filelist.add(files[i])
                }
            }
            return filelist
        }

        fun arrayListFiles(strPath: String): ArrayList<File> {
            val filelist = ArrayList<File>()
            val dir = File(strPath)
            val files = dir.listFiles()
            for (i in files.indices) {
                filelist.add(files[i].absoluteFile)
            }
            return filelist
        }

        /**
         * 1\可先创建文件的路径
         * @param filePath
         */
        fun makeRootDirectory(filePath: String) {
            var file: File? = null
            try {
                file = File(filePath)
                if (!file.exists()) {
                    file.mkdirs()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 2\然后在创建文件名就不会在报该错误
         * @param filePath
         * @param fileName
         * @return
         */
        fun getFilePath(filePath: String, fileName: String): File? {
            var file: File? = null
            makeRootDirectory(filePath)
            try {
                file = File(filePath + fileName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }
    }
}