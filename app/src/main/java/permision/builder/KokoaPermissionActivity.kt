package permision.builder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kokoapractice.R
import com.example.kokoapractice.util.ObjectUtils
import permision.builder.KokoaPermissionUtil.startSettingActivityForResult
import java.util.ArrayDeque
import java.util.Deque

class KokoaPermissionActivity : AppCompatActivity() {
    var denyTitle: CharSequence? = null
    var denyMessage: CharSequence? = null
    var permissions: Array<String>? = emptyArray()

    var packageNameTmp: String? = null
    var hasSettingButton = false
    var settingButtonText: String? = null
    var deniedCloseButtonText: String? = null
    var isShownRationaleDialog = false
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        setupFromSavedInstanceState(savedInstanceState)
        checkPermissions(false)
    }

    private fun setupFromSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS)
            denyTitle = savedInstanceState.getCharSequence(EXTRA_DENY_TITLE)
            denyMessage = savedInstanceState.getCharSequence(EXTRA_DENY_MESSAGE)
            packageNameTmp = savedInstanceState.getString(EXTRA_PACKAGE_NAME)
            hasSettingButton = savedInstanceState.getBoolean(EXTRA_SETTING_BUTTON, true)
            deniedCloseButtonText = savedInstanceState.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT)
            settingButtonText = savedInstanceState.getString(EXTRA_SETTING_BUTTON_TEXT)
        } else {
            val intent = intent
            permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS)
            denyTitle = intent.getCharSequenceExtra(EXTRA_DENY_TITLE)
            denyMessage = intent.getCharSequenceExtra(EXTRA_DENY_MESSAGE)
            packageNameTmp = intent.getStringExtra(EXTRA_PACKAGE_NAME)
            hasSettingButton = intent.getBooleanExtra(EXTRA_SETTING_BUTTON, true)
            deniedCloseButtonText = intent.getStringExtra(EXTRA_DENIED_DIALOG_CLOSE_TEXT)
            settingButtonText = intent.getStringExtra(EXTRA_SETTING_BUTTON_TEXT)
        }
    }


    private fun checkPermissions(fromOnActivityResult: Boolean) {
        val needPermissions: MutableList<String> = ArrayList()
        for (permission in permissions!!) {
            if (KokoaPermissionUtil.isDenied(permission)) {
                needPermissions.add(permission)
            }
        }
        if (needPermissions.isEmpty()) {
            permissionResult(null)
        } else if (fromOnActivityResult) { //From Setting Activity
            permissionResult(needPermissions)
        } else { // //Need Request Permissions
            requestPermissions(needPermissions)
        }
    }

    private fun permissionResult(deniedPermissions: MutableList<String>?) {
        finish()
        overridePendingTransition(0, 0)
        if (permissionListenerStack != null) {
            val listener = permissionListenerStack!!.pop()
            if (ObjectUtils.isEmpty(deniedPermissions)) {
                listener.onPermissionGranKokoa()
            } else {
                listener.onPermissionDenied(deniedPermissions)
            }
            if (permissionListenerStack!!.size == 0) {
                permissionListenerStack = null
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    fun requestPermissions(needPermissions: List<String?>) {
        ActivityCompat.requestPermissions(
            this, needPermissions.toTypedArray<String?>(),
            REQ_CODE_PERMISSION_REQUEST
        )
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(EXTRA_PERMISSIONS, permissions)
        outState.putCharSequence(EXTRA_DENY_TITLE, denyTitle)
        outState.putCharSequence(EXTRA_DENY_MESSAGE, denyMessage)
        outState.putString(EXTRA_PACKAGE_NAME, packageNameTmp)
        outState.putBoolean(EXTRA_SETTING_BUTTON, hasSettingButton)
        outState.putString(EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText)
        outState.putString(EXTRA_SETTING_BUTTON_TEXT, settingButtonText)
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val deniedPermissions = KokoaPermissionUtil.getDeniedPermissions(*permissions)
        if (deniedPermissions!!.isEmpty()) {
            permissionResult(null)
        } else {
            showPermissionDenyDialog(deniedPermissions)
        }
    }

    fun showPermissionDenyDialog(deniedPermissions: MutableList<String>?) {
        if (TextUtils.isEmpty(denyMessage)) {
            // denyMessage 설정 안함
            permissionResult(deniedPermissions)
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(denyTitle)
            .setMessage(denyMessage)
            .setCancelable(false)
            .setNegativeButton(deniedCloseButtonText) { dialogInterface, i ->
                permissionResult(
                    deniedPermissions
                )
            }
        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.kokoapermission_setting)
            }
            builder.setPositiveButton(settingButtonText) { dialog, which ->
                startSettingActivityForResult(
                    this@KokoaPermissionActivity
                )
            }
        }
        builder.show()
    }

    fun showWindowPermissionDenyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(denyMessage)
            .setCancelable(false)
            .setNegativeButton(deniedCloseButtonText) { dialogInterface, i -> checkPermissions(false) }
        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.kokoapermission_setting)
            }
            builder.setPositiveButton(settingButtonText) { dialog, which ->
                val uri = Uri.fromParts("package", packageNameTmp, null)
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                startActivityForResult(
                    intent,
                    REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING
                )
            }
        }
        builder.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            KokoaPermissionUtil.REQ_CODE_REQUEST_SETTING -> checkPermissions(true)
            REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST -> if (!TextUtils.isEmpty(
                    denyMessage
                )
            ) {  // 권한이 거부되고 denyMessage 가 있는 경우
                showWindowPermissionDenyDialog()
            } else {     // 권한있거나 또는 denyMessage가 없는 경우는 일반 permission 을 확인한다.
                checkPermissions(false)
            }

            REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING -> checkPermissions(false)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val REQ_CODE_PERMISSION_REQUEST = 10
        const val REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 30
        const val REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING = 31
        const val EXTRA_PERMISSIONS = "permissions"
        const val EXTRA_DENY_TITLE = "deny_title"
        const val EXTRA_DENY_MESSAGE = "deny_message"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_SETTING_BUTTON = "setting_button"
        const val EXTRA_SETTING_BUTTON_TEXT = "setting_button_text"
        const val EXTRA_DENIED_DIALOG_CLOSE_TEXT = "denied_dialog_close_text"
        private var permissionListenerStack: Deque<PermissionListener>? = null
        fun startActivity(context: Context?, intent: Intent?, listener: PermissionListener) {
            if (permissionListenerStack == null) {
                permissionListenerStack = ArrayDeque()
            }
            permissionListenerStack!!.push(listener)
            context!!.startActivity(intent)
        }
    }
}