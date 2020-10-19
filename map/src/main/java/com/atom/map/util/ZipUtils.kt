package com.atom.map.util

import android.util.Log
import java.io.*
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.math.log10
import kotlin.math.pow

class ZipUtils {

    companion object {


        private val BUFF_SIZE = 1024 * 1024 // 1M Byte


        /**
         * 批量压缩文件（夹）
         */
        @Throws(IOException::class)
        fun zipFiles(
            resFileList: Collection<File>,
            zipFile: File
        ) {
            val zipout = ZipOutputStream(
                BufferedOutputStream(
                    FileOutputStream(zipFile), BUFF_SIZE
                )
            )
            for (resFile in resFileList) {
                zipFile(resFile, zipout, "")
            }
            zipout.close()
        }

        /**
         * 压缩文件
         */
        @Throws(IOException::class)
        fun zipFiles(
            resFile: File,
            zipFile: File
        ) {
            val zipout = ZipOutputStream(
                BufferedOutputStream(
                    FileOutputStream(zipFile), BUFF_SIZE
                )
            )
            zipFile(resFile, zipout, "")
            zipout.close()
        }

        /**
         * 批量压缩文件（夹）
         *
         * @param resFileList 要压缩的文件（夹）列表
         * @param zipFile 生成的压缩文件
         * @param comment 压缩文件的注释
         * @throws IOException 当压缩过程出错时抛出
         */
        @Throws(IOException::class)
        fun zipFiles(
            resFileList: Collection<File>,
            zipFile: File,
            comment: String
        ) {
            val zipout = ZipOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        zipFile
                    ), BUFF_SIZE
                )
            )
            for (resFile in resFileList) {
                zipFile(resFile, zipout, "")
            }
            zipout.setComment(comment)
            zipout.close()
        }

        /**
         * 解压缩一个文件
         */
        @Throws(ZipException::class, IOException::class)
        fun upZipFile(zipFile: File, folderPath: String) {
            val desDir = File(folderPath)
            if (!desDir.exists()) {
                desDir.mkdirs()
            }
            val zf = ZipFile(zipFile)
            val entries: Enumeration<*> = zf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.isDirectory) {
                    continue
                }
                val `in` = zf.getInputStream(entry)
                var str = folderPath + File.separator + entry.name
                str = String(str.toByteArray(), Charsets.UTF_8)
                val desFile = File(str)
                if (!desFile.exists()) {
                    val fileParentDir = desFile.parentFile
                    if (!fileParentDir.exists()) {
                        fileParentDir.mkdirs()
                    }
                    desFile.createNewFile()
                }
                val out: OutputStream = FileOutputStream(desFile)
                val buffer = ByteArray(BUFF_SIZE)
                var realLength: Int
                while (`in`.read(buffer).also { realLength = it } > 0) {
                    out.write(buffer, 0, realLength)
                }
                `in`.close()
                out.close()
            }
        }

        /**
         * 解压文件名包含传入文字的文件
         *
         * @param zipFile 压缩文件
         * @param folderPath 目标文件夹
         * @param nameContains 传入的文件匹配名
         * @throws ZipException 压缩格式有误时抛出
         * @throws IOException IO错误时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun upZipSelectedFile(
            zipFile: File,
            folderPath: String,
            nameContains: String
        ): ArrayList<File>? {
            val fileList = ArrayList<File>()
            val desDir = File(folderPath)
            if (!desDir.exists()) {
                desDir.mkdir()
            }
            val zf = ZipFile(zipFile)
            val entries: Enumeration<*> = zf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.name.contains(nameContains)) {
                    val `in` = zf.getInputStream(entry)
                    var str = folderPath + File.separator + entry.name
                    str = String(str.toByteArray(charset("utf-8")), charset("gbk"))
                    // str.getBytes("GB2312"),"8859_1" 输出
                    // str.getBytes("8859_1"),"GB2312" 输入
                    val desFile = File(str)
                    if (!desFile.exists()) {
                        val fileParentDir = desFile.parentFile
                        if (!fileParentDir.exists()) {
                            fileParentDir.mkdirs()
                        }
                        desFile.createNewFile()
                    }
                    val out: OutputStream = FileOutputStream(desFile)
                    val buffer = ByteArray(BUFF_SIZE)
                    var realLength: Int
                    while (`in`.read(buffer).also { realLength = it } > 0) {
                        out.write(buffer, 0, realLength)
                    }
                    `in`.close()
                    out.close()
                    fileList.add(desFile)
                }
            }
            return fileList
        }

        /**
         * 获得压缩文件内文件列表
         *
         * @param zipFile 压缩文件
         * @return 压缩文件内文件名称
         * @throws ZipException 压缩文件格式有误时抛出
         * @throws IOException 当解压缩过程出错时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun getEntriesNames(zipFile: File): ArrayList<String> {
            val entryNames = ArrayList<String>()
            val entries = getEntriesEnumeration(zipFile)
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                entryNames.add(
                    String(
                        getEntryName(entry).toByteArray(charset("GB2312")),
                        Charsets.ISO_8859_1
                    )
                )
            }
            return entryNames
        }

        /**
         * 获得压缩文件内压缩文件对象以取得其属性
         *
         * @param zipFile 压缩文件
         * @return 返回一个压缩文件列表
         * @throws ZipException 压缩文件格式有误时抛出
         * @throws IOException IO操作有误时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun getEntriesEnumeration(zipFile: File): Enumeration<*> {
            val zf = ZipFile(zipFile)
            return zf.entries()
        }

        /**
         * 取得压缩文件对象的注释
         *
         * @param entry 压缩文件对象
         * @return 压缩文件对象的注释
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun getEntryComment(entry: ZipEntry): String? {
            return String(entry.comment.toByteArray(charset("GB2312")), Charsets.ISO_8859_1)
        }

        /**
         * 取得压缩文件对象的名称
         *
         * @param entry 压缩文件对象
         * @return 压缩文件对象的名称
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun getEntryName(entry: ZipEntry): String {
            return String(entry.name.toByteArray(charset("GB2312")), Charsets.ISO_8859_1)
        }

        /**
         * 压缩文件
         *
         * @param resFile 需要压缩的文件（夹）
         * @param zipout 压缩的目的文件
         * @param rootpath 压缩的文件路径
         * @throws FileNotFoundException 找不到文件时抛出
         * @throws IOException 当压缩过程出错时抛出
         */
        @Throws(FileNotFoundException::class, IOException::class)
        public fun zipFile(
            resFile: File,
            zipout: ZipOutputStream,
            rootpath: String
        ) {
            var rootpath = rootpath
            rootpath = (rootpath + (if (rootpath.trim { it <= ' ' }
                    .isEmpty()) "" else File.separator) + resFile.name)
            rootpath = String(rootpath.toByteArray(), Charsets.UTF_8)
            if (resFile.isDirectory) {
                val fileList = resFile.listFiles()
                for (file in fileList) {
                    zipFile(file, zipout, rootpath)
                }
            } else {
                val buffer = ByteArray(BUFF_SIZE)
                val `in` = BufferedInputStream(
                    FileInputStream(resFile),
                    BUFF_SIZE
                )
                zipout.putNextEntry(ZipEntry(rootpath))
                var realLength: Int
                while (`in`.read(buffer).also { realLength = it } != -1) {
                    zipout.write(buffer, 0, realLength)
                }
                `in`.close()
                zipout.flush()
                zipout.closeEntry()
            }
        }

        //第二种实现
        @Throws(IOException::class)
        fun zip(src: String, dest: String) {
            // 提供了一个数据项压缩成一个ZIP归档输出流
            var out: ZipOutputStream? = null
            try {
                val outFile = File(dest) // 源文件或者目录
                val fileOrDirectory = File(src) // 压缩文件路径
                out = ZipOutputStream(FileOutputStream(outFile))
                // 如果此文件是一个文件，否则为false。
                if (fileOrDirectory.isFile) {
                    zipFileOrDirectory(out, fileOrDirectory, "")
                } else {
                    // 返回一个文件或空阵列。
                    val entries = fileOrDirectory.listFiles()
                    for (i in entries.indices) {
                        // 递归压缩，更新curPaths
                        zipFileOrDirectory(out, entries[i], "")
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            } finally {
                // 关闭输出流
                if (out != null) {
                    try {
                        out.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
        }

        @Throws(IOException::class)
        private fun zipFileOrDirectory(
            out: ZipOutputStream,
            fileOrDirectory: File,
            curPath: String
        ) {
            // 从文件中读取字节的输入流
            var `in`: FileInputStream? = null
            try {
                // 如果此文件是一个目录，否则返回false。
                if (!fileOrDirectory.isDirectory) {
                    // 压缩文件
                    val buffer = ByteArray(4096)
                    var bytes_read: Int
                    `in` = FileInputStream(fileOrDirectory)
                    // 实例代表一个条目内的ZIP归档
                    val entry = ZipEntry(
                        curPath
                                + fileOrDirectory.name
                    )
                    // 条目的信息写入底层流
                    out.putNextEntry(entry)
                    while (`in`.read(buffer).also { bytes_read = it } != -1) {
                        out.write(buffer, 0, bytes_read)
                    }
                    out.closeEntry()
                } else {
                    // 压缩目录
                    val entries = fileOrDirectory.listFiles()
                    for (i in entries.indices) {
                        // 递归压缩，更新curPaths
                        zipFileOrDirectory(
                            out, entries[i], curPath
                                    + fileOrDirectory.name + "/"
                        )
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                // throw ex;
            } finally {
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
        }

        @Throws(IOException::class)
        fun unzip(zipFileName: String, outputDirectory: String) {
            var zipFile: ZipFile? = null
            try {
                zipFile = ZipFile(zipFileName)
                val e: Enumeration<*> = zipFile.entries()
                var zipEntry: ZipEntry? = null
                val dest = File(outputDirectory)
                dest.mkdirs()
                while (e.hasMoreElements()) {
                    zipEntry = e.nextElement() as ZipEntry
                    val entryName = zipEntry.name
                    var `in`: InputStream? = null
                    var out: FileOutputStream? = null
                    try {
                        if (zipEntry.isDirectory) {
                            var name = zipEntry.name
                            name = name.substring(0, name.length - 1)
                            val f = File(
                                outputDirectory + File.separator
                                        + name
                            )
                            f.mkdirs()
                        } else {
                            var index = entryName.lastIndexOf("\\")
                            if (index != -1) {
                                val df = File(
                                    outputDirectory + File.separator
                                            + entryName.substring(0, index)
                                )
                                df.mkdirs()
                            }
                            index = entryName.lastIndexOf("/")
                            if (index != -1) {
                                val df = File(
                                    outputDirectory + File.separator
                                            + entryName.substring(0, index)
                                )
                                df.mkdirs()
                            }
                            val f = File(
                                outputDirectory + File.separator
                                        + zipEntry.name
                            )
                            // f.createNewFile();
                            `in` = zipFile.getInputStream(zipEntry)
                            out = FileOutputStream(f)
                            var c: Int
                            val by = ByteArray(1024)
                            while (`in`.read(by).also { c = it } != -1) {
                                out.write(by, 0, c)
                            }
                            out.flush()
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        throw IOException("解压失败：$ex")
                    } finally {
                        if (`in` != null) {
                            try {
                                `in`.close()
                            } catch (ex: IOException) {
                            }
                        }
                        if (out != null) {
                            try {
                                out.close()
                            } catch (ex: IOException) {
                            }
                        }
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                throw IOException("解压失败：$ex")
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close()
                    } catch (ex: IOException) {
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun splitZip(fileSrc: String, destSrc: String , size : Long): Int {
            val file = File(fileSrc)
            if (!file.exists()) {
                 Log.e("fengpian" ,"文件不存在！")
                return 0
            }
            //需要拆分文件大小
            val countFileSize = file.length()
            //统计zip文件被分割的个数
            var partNum = 0
            partNum = if (countFileSize % size == 0L) {
                (countFileSize / size).toInt()
            } else {
                (countFileSize / size).toInt() + 1
            }
            Log.e("fengpian"  ,"分割文件个数：$partNum")
            val `in`: InputStream
            `in` = FileInputStream(file)
            val bis = BufferedInputStream(`in`)
            var bos: BufferedOutputStream? = null
            val bytes = ByteArray(1024 * 1024 * 20)
            for (i in 0 until partNum) {
                val newFileSrc = destSrc + "part-" + i + ".zip"
                val newFile = File(newFileSrc)
                if (!newFile.parentFile.exists()) {
                    Log.e("fengpian"  ,"创建文件分割目录！")
                    newFile.parentFile.mkdirs()
                }
                bos = BufferedOutputStream(FileOutputStream(newFile))
                var readSize = -1
                var count = 0
                while (bis.read(bytes).also { readSize = it } != -1) {
                    bos.write(bytes, 0, readSize)
                    bos.flush()
                    count += readSize
                    if (count >= size) {
                        break
                    }
                }
            }
            bis.close()
            `in`.close()
            bos!!.close()
            return partNum
        }
        @Throws(Exception::class)
        fun mergeFile(savePath : String , vararg files: File) {
            val file = File(savePath)
            if (!file.exists()) {
                Log.e("juhe" ,"创建目录：123123")
            }
            val bos = BufferedOutputStream(FileOutputStream(file))
            var bis: BufferedInputStream? = null
            val bytes = ByteArray(1024 * 1024 * 10)
            var readSize = 0
            Log.e("juhe" ,"创建目录：2")
            for (element in files) {
                Log.e("juhe" ,"创建目录：3  ${element.path}")
                bis = BufferedInputStream(FileInputStream(element))
                while (bis.read(bytes).also { readSize = it } != -1) {
                    bos.write(bytes, 0, readSize)
                }
            }
            bos.close()
            bis!!.close()
        }

        fun readableFileSize(size: Long): String? {
            if (size <= 0) {
                return "0"
            }
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(
                size / 1024.0.pow(digitGroups.toDouble())
            ).toString() + "" + units[digitGroups]
        }
    }
}