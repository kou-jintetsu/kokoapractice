package permision.builder

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.StringRes
import com.example.kokoapractice.R
import com.example.kokoapractice.util.ObjectUtils
import permision.provider.KokoaPermissionProvider

abstract class PermissionBuilder<T : PermissionBuilder<T>> {
    private val context: Context? = KokoaPermissionProvider.context
    private var listener: PermissionListener? = null
    private var permissions: Array<String> = emptyArray<String>()
    private var rationaleTitle: CharSequence? = null
    private var rationaleMessage: CharSequence? = null
    private var denyTitle: CharSequence? = null
    private var denyMessage: CharSequence? = null
    private var settingButtonText: CharSequence? = null
    private var hasSettingBtn = true
    private var deniedCloseButtonText: CharSequence =
        context!!.getString(R.string.kokoapermission_close)
    private var rationaleConfirmText: CharSequence =
        context!!.getString(R.string.kokoaermission_confirm)

    private var requesKokoaOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    fun checkPermissions() {
        requireNotNull(listener) { "You must setPermissionListener() on KokoaPermission" }
        require(!ObjectUtils.isEmpty(permissions)) { "You must setPermissions() on KokoaPermission" }
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            listener!!.onPermissionGranKokoa()
            return
        }
        val intent = Intent(context, KokoaPermissionActivity::class.java)
        intent.putExtra(KokoaPermissionActivity.EXTRA_PERMISSIONS, permissions)
        intent.putExtra(KokoaPermissionActivity.EXTRA_DENY_TITLE, denyTitle)
        intent.putExtra(KokoaPermissionActivity.EXTRA_DENY_MESSAGE, denyMessage)
        intent.putExtra(KokoaPermissionActivity.EXTRA_PACKAGE_NAME, context!!.packageName)
        intent.putExtra(KokoaPermissionActivity.EXTRA_SETTING_BUTTON, hasSettingBtn)
        intent.putExtra(
            KokoaPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT,
            deniedCloseButtonText
        )

        intent.putExtra(
            KokoaPermissionActivity.EXTRA_SETTING_BUTTON_TEXT,
            settingButtonText
        )

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        KokoaPermissionActivity.startActivity(context, intent, listener!!)
        KokoaPermissionUtil.setFirstRequest(permissions)
    }

    fun setPermissionListener(listener: PermissionListener?): T {
        this.listener = listener
        return this as T
    }

    fun setPermissions(vararg permissions: String): T {
        this.permissions = permissions as Array<String>
        return this as T
    }



    private fun getText(@StringRes stringRes: Int): CharSequence {
        return context!!.getText(stringRes)
    }



    fun setDeniedMessage(@StringRes stringRes: Int): T {
        return setDeniedMessage(getText(stringRes))
    }

    fun setDeniedMessage(denyMessage: CharSequence?): T {
        this.denyMessage = denyMessage
        return this as T
    }



    companion object {
        private const val PREFS_NAME_PERMISSION = "PREFS_NAME_PERMISSION"
        private const val PREFS_IS_FIRST_REQUEST = "PREFS_IS_FIRST_REQUEST"
    }
}