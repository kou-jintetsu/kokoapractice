package permision.builder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import permision.provider.KokoaPermissionProvider

@SuppressLint("StaticFieldLeak")
object KokoaPermissionUtil {
    const val REQ_CODE_REQUEST_SETTING = 2000
    private const val PREFS_NAME_PERMISSION = "PREFS_NAME_PERMISSION"
    private const val PREFS_IS_FIRST_REQUEST = "IS_FIRST_REQUEST"

    @SuppressLint("StaticFieldLeak")
    private val context: Context? = KokoaPermissionProvider.Companion.context
    fun isGranKokoa(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (isDenied(permission)) {
                return false
            }
        }
        return true
    }

    fun isDenied(permission: String): Boolean {
        return !isGranKokoa(permission)
    }

    private fun isGranKokoa(permission: String): Boolean {
        return if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        } else {
            ContextCompat.checkSelfPermission(
                context!!,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getDeniedPermissions(vararg permissions: String): MutableList<String> {
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (isDenied(permission)) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions
    }

    fun canRequestPermission(activity: Activity?, permissions: Array<String>): Boolean {
        if (isFirstRequest(permissions)) {
            return true
        }
        for (permission in permissions) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!, permission
            )
            if (isDenied(permission) && !showRationale) {
                return false
            }
        }
        return true
    }

    private fun isFirstRequest(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!isFirstRequest(permission)) {
                return false
            }
        }
        return true
    }

    private fun isFirstRequest(permission: String): Boolean {
        return sharedPreferences.getBoolean(getPrefsNamePermission(permission), true)
    }

    private fun getPrefsNamePermission(permission: String): String {
        return PREFS_IS_FIRST_REQUEST + "_" + permission
    }

    private val sharedPreferences: SharedPreferences
        private get() = context!!.getSharedPreferences(PREFS_NAME_PERMISSION, Context.MODE_PRIVATE)

    @JvmOverloads
    fun startSettingActivityForResult(
        activity: Activity,
        requestCode: Int = REQ_CODE_REQUEST_SETTING
    ) {
        activity.startActivityForResult(settingIntent, requestCode)
    }

    val settingIntent: Intent
        get() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + context!!.packageName))

    @JvmOverloads
    fun startSettingActivityForResult(
        fragment: Fragment,
        requestCode: Int = REQ_CODE_REQUEST_SETTING
    ) {
        fragment.startActivityForResult(settingIntent, requestCode)
    }

    fun setFirstRequest(permissions: Array<String>) {
        for (permission in permissions) {
            setFirstRequest(permission)
        }
    }

    private fun setFirstRequest(permission: String) {
        sharedPreferences.edit().putBoolean(getPrefsNamePermission(permission), false).apply()
    }
}