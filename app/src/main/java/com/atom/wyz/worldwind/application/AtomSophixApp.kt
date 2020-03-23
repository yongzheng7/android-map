package com.atom.wyz.worldwind.application

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.Keep
import com.taobao.sophix.PatchStatus
import com.taobao.sophix.SophixApplication
import com.taobao.sophix.SophixEntry
import com.taobao.sophix.SophixManager
import com.taobao.sophix.listener.PatchLoadStatusListener


class AtomSophixApp : SophixApplication() {
    private val TAG = "AtomSophixApp"

    @Keep
    @SophixEntry(AtomApp::class)
    internal class RealApplicationStub {}


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initSophix()
    }

    fun initSophix() {
        var appVersion: String

        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (e: PackageManager.NameNotFoundException) {
            appVersion = "1.3";
            e.printStackTrace();
        }

        SophixManager.getInstance().setContext(this)
                .setAppVersion(appVersion)

                .setAesKey(null)
                .setEnableDebug(false)
                .setPatchLoadStatusStub(PatchLoadStatusListener { mode, code, info, handlePatchVersion ->
                    var msg: String = StringBuilder("").append("Mode:").append(mode)
                            .append(" Code:").append(code)
                            .append(" Info:").append(info)
                            .append(" HandlePatchVersion:").append(handlePatchVersion).toString();

                    Log.d(TAG, "onCreate://...... " + msg.toString());
//                                    if (msgDisplayListener != null) {
//                                      msgDisplayListener.handle(msg);
//                                    } else {
//                                      cacheMsg.append("\n").append(msg);
//                                    }
                    // 补丁加载回调通知
                    if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                        // 表明补丁加载成功
                    } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                        // 表明新补丁生效需要重启. 开发者可提示用户或者强制重启;
                        // 建议: 用户可以监听进入后台事件, 然后调用killProcessSafely自杀，以此加快应用补丁，详见1.3.2.3
                        // SophixManager.getInstance().killProcessSafely();

                    } else if (code == PatchStatus.CODE_LOAD_FAIL) {
                        // 内部引擎异常, 推荐此时清空本地补丁, 防止失败补丁重复加载
                        SophixManager.getInstance().cleanPatches();
                    } else {
                        // 其它错误信息, 查看PatchStatus类说明
                    }
                }).initialize();

    }
}