package com.example.kokoapractice

import permision.builder.PermissionBuilder


object KokoaPermission {
    val TAG = KokoaPermission::class.java.simpleName
    fun create(): Builder {
        return Builder()
    }

    class Builder : PermissionBuilder<Builder>() {
        fun check() {
            checkPermissions()
        }
    }
}