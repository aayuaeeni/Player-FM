package com.raju.pratilipi_fm.utils

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionUtility {

    fun hasAllPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
}