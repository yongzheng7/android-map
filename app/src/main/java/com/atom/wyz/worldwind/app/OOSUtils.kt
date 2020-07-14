package com.atom.wyz.worldwind.app

class OOSUtils {
    companion object{
        // To run the sample correctly, the following variables must have valid values.
        // The endpoint value below is just the example. Please use proper value according to your region

        // To run the sample correctly, the following variables must have valid values.
        // The endpoint value below is just the example. Please use proper value according to your region
        // 访问的endpoint地址
        const val OSS_ENDPOINT = "oss-cn-beijing.aliyuncs.com"

        //callback 测试地址
        const val OSS_CALLBACK_URL = "http://oss-demo.aliyuncs.com:23450"

        // STS 鉴权服务器地址。
        // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS鉴权服务器。
        const val STS_SERVER_URL = "http://****/sts/getsts" //STS 地址


        const val BUCKET_NAME = "imatom"
        const val OSS_ACCESS_KEY_ID = "LTAI4G6P7a83eBmAvCTfZ2Ze"
        const val OSS_ACCESS_KEY_SECRET = "GRZdnwVIfyvaqkEAdka4nJm9wn4inh"

        const val DOWNLOAD_SUC = 1
        const val DOWNLOAD_Fail = 2
        const val UPLOAD_SUC = 3
        const val UPLOAD_Fail = 4
        const val UPLOAD_PROGRESS = 5
        const val LIST_SUC = 6
        const val HEAD_SUC = 7
        const val RESUMABLE_SUC = 8
        const val SIGN_SUC = 9
        const val BUCKET_SUC = 10
        const val GET_STS_SUC = 11
        const val MULTIPART_SUC = 12
        const val STS_TOKEN_SUC = 13
        const val FAIL = 9999
        const val REQUESTCODE_AUTH = 10111
        const val REQUESTCODE_LOCALPHOTOS = 10112
    }
}