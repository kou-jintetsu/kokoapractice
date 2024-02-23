package permision.builder

import com.example.kokoapractice.util.ObjectUtils

class KokoaPermissionResult(val deniedPermissions: List<String>) {
    val isGranKokoa: Boolean

    init {
        isGranKokoa = ObjectUtils.isEmpty(deniedPermissions)
    }
}