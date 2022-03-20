package com.raju.pratilipi_fm.utils

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raju.pratilipi_fm.databinding.OptionSheetLayoutBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class OptionSheet : BottomSheetDialogFragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: OptionSheetLayoutBinding
    private var isRecord: Boolean? = false
    companion object{
        const val REQUEST_PERMISSION_CODE = 100
        var listener: OptionSheetListener? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OptionSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        initActions()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    private fun initActions() {
        binding.tvRecord.setOnClickListener {
           isRecord = true
            if (Build.VERSION.SDK_INT >= 23) {
                if (!PermissionUtility.hasAllPermission(requireActivity())) {
                    requestPermissions()
                }else{
                    listener?.onOptionClicked(isRecord)
                    dismiss()
                }
            }else{
                listener?.onOptionClicked(isRecord)
                dismiss()
            }
        }

        binding.tvPickAudio.setOnClickListener {
            isRecord = false
            if (Build.VERSION.SDK_INT >= 23) {
                if (!PermissionUtility.hasAllPermission(requireActivity())) {
                    requestPermissions()
                }else{
                    listener?.onOptionClicked(isRecord)
                    dismiss()
                }
            }else{
                listener?.onOptionClicked(isRecord)
                dismiss()
            }
        }
    }

    interface OptionSheetListener {
        fun onOptionClicked(isRecord:Boolean?)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            listener?.onOptionClicked(isRecord)
            dismiss()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    //Request permission
    private fun requestPermissions() {
        if (PermissionUtility.hasAllPermission(requireActivity())) {
            return
        }

        EasyPermissions.requestPermissions(
            this,
            "You need to accept all the permission to use this app.",
            REQUEST_PERMISSION_CODE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PERMISSION_CODE) {
            if (PermissionUtility.hasAllPermission(requireActivity())) {
                listener?.onOptionClicked(isRecord)
                dismiss()
            } else {
                requestPermissions()
            }
        }
    }

}