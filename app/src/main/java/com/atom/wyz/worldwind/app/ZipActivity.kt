package com.atom.wyz.worldwind.app

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.util.ZipUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.net.URLConnection


class ZipActivity : Activity() {
    var pathString: String = Environment.getExternalStorageDirectory().absolutePath
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zip)
        findViewById<Button>(R.id.yasuo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                GlobalScope.launch {
                    Log.e("jieya", "主线程id：${mainLooper.thread.id}")
                    try {
                        Log.e("yasuo", " 1   $pathString");
                        val src = File(pathString + "/Download/yasuo.mp4")
                        val zip = File(pathString + "/Download/yasuo1.zip")
                        Log.e("yasuo", " 2  ");
                        if (!zip.exists()) {
                            // zip.createNewFile()
                        }
                        ZipUtils.zipFiles(src, zip)
                        Log.e("yasuo", " 3  ");
//
//                    //第二种实现
//                    ZipUtils.zip(pathString+"/Android/data/com.info.collection/files/cache", pathString+"/Android/data/com.info.collection/files/cache.zip");
//                    ZipUtils.unzip(pathString+"/Android/data/com.info.collection/files/cache.zip", pathString+"/Android/data/com.info.collection/files/cache");
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("yasuo", " 4 $e ");
                    }
                }
            }
        })

        findViewById<Button>(R.id.fengpian).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                GlobalScope.launch {
                    Log.e("fengpian", "主线程id：${mainLooper.thread.id}")

                    val zip = File(pathString + "/Download/yasuo1.zip")
                    Log.e("fengpian", " 1  ${zip.path}");
                    try {
                        val splitZip = ZipUtils.splitZip(
                            zip.path,
                            pathString + "/Download/yasuo1/",
                            1024 * 1024 * 5L
                        )
                        Log.e("fengpian", " 2");
                    } catch (e: Exception) {
                        Log.e("fengpian", " 3 $e");
                    }
                    Log.e("fengpian", " 4");
                }
            }
        })

        findViewById<Button>(R.id.juhe).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                GlobalScope.launch {
                    Log.e("juhe", "主线程id：${mainLooper.thread.id}")

                    val zip = File(pathString + "/Download/yasuo12.zip")
                    if (!zip.exists()) {
                        zip.createNewFile()
                    }
                    val dir = File(pathString + "/Download/yasuo1")
                    val listFiles = dir.listFiles(object : FileFilter {
                        override fun accept(pathname: File?): Boolean {
                            if (pathname == null) return false
                            if (pathname.name.startsWith("part")) return true
                            return false
                        }
                    })
                    Log.e("juhe", " 1  ${zip.path} , $listFiles");
                    val toList = listFiles.toSortedSet()
                    Log.e("juhe", " 2 $toList");

                    try {
                        ZipUtils.mergeFile(
                            zip.path,
                            *toList.toTypedArray()
                        )
                        Log.e("juhe", " 21");
                    } catch (e: Exception) {
                        Log.e("juhe", " 3 $e");
                    }
                    Log.e("juhe", " 4");
                }
            }
        })
        findViewById<Button>(R.id.jieya).setOnClickListener {
            Log.e("jieya", "主线程id：${mainLooper.thread.id}")
            val launch = GlobalScope.launch {

            }
        }

        findViewById<Button>(R.id.upload).setOnClickListener {
            Log.e("upload", "主线程id：${mainLooper.thread.id}")
            val launch = GlobalScope.launch {
                val dir = File(pathString + "/Download/yasuo1")
                val listFiles = dir.listFiles(object : FileFilter {
                    override fun accept(pathname: File?): Boolean {
                        if (pathname == null) return false
                        if (pathname.name.startsWith("part")) return true
                        return false
                    }
                })
                val list = mutableListOf<String>()
                listFiles.forEach { list.add(it.absolutePath) }
                upLoadFile(
                    "http://testfrs.bjhyw.com/testfrs/files/public",
                    list,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("upload", "Callback：${e}")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.e("upload", "Callback：${response}")
                        }
                    }
                )
            }
        }
    }

    /**
     * 通过上传的文件的完整路径生成RequestBody
     * @param fileNames 完整的文件路径
     * @return
     */
    private fun getRequestBody(fileNames: List<String>): RequestBody {
        //创建MultipartBody.Builder，用于添加请求的数据
        val builder = MultipartBody.Builder()
        for (i in fileNames.indices) { //对文件进行遍历
            val file = File(fileNames[i]) //生成文件
            //根据文件的后缀名，获得文件类型
            val fileType: String = getMimeType(file.name)
            builder.addFormDataPart( //给Builder添加上传的文件
                "image",  //请求的名字
                file.name,  //文件的文字，服务器端用来解析的
                RequestBody.create(MediaType.parse(fileType), file) //创建RequestBody，把上传的文件放入
            )
        }
        return builder.build() //根据Builder创建请求
    }

    /**
     * 获得Request实例
     * @param url
     * @param fileNames 完整的文件路径
     * @return
     */
    private fun getRequest(
        url: String,
        fileNames: List<String>
    ): Request {
        val builder = Request.Builder()
        builder
            .url(url)
            .header("Cookie", "DATA_SOURCE_LOOPUP_KEY=alpha; SECURITY_TARGET_URL=http://localhost/home; SECURITY_LOGIN_URL=; JSESSIONID=03D1EF900686DBB99A9F379BA43F8F2C")
            .post(getRequestBody(fileNames))
        return builder.build()
    }

    /**
     * 根据url，发送异步Post请求
     * @param url 提交到服务器的地址
     * @param fileNames 完整的上传的文件的路径名
     * @param callback OkHttp的回调接口
     */
    fun upLoadFile(
        url: String,
        fileNames: List<String>,
        callback: Callback
    ) {
        val cookieStore: HashMap<String, List<Cookie>> = HashMap()
        val okHttpClient = OkHttpClient.Builder().cookieJar(
            object : CookieJar{
                override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                    cookieStore.put(url.host(), cookies);
                }

                override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                    val cookies =  cookieStore.get(url.host())
                    return cookies ?.toMutableList() ?: mutableListOf()
                }

            }
        ).build()
        val call = okHttpClient.newCall(getRequest(url, fileNames))
        call.enqueue(callback)
    }

    /**
     * 获取文件MimeType
     *
     * @param filename
     * @return
     */
    private fun getMimeType(filename: String): String {
        val filenameMap = URLConnection.getFileNameMap()
        var contentType = filenameMap.getContentTypeFor(filename)
        if (contentType == null) {
            contentType = "application/octet-stream" //* exe,所有的可执行程序
        }
        return contentType
    }

}