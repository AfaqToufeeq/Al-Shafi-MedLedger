package com.pentabytex.alshafimedledger.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun requestPermissionIfNeeded(activity: Activity, permission: String, requestCode: Int) {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request permission
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }

    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        grantResults: IntArray,
        permission: String,
        onPermissionGranted: () -> Unit
    ) {
        if (requestCode == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                // Check if user denied permission
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                if (shouldShowRationale) {
                    // Show rationale and ask for permission again
                    showPermissionRationaleDialog(activity, permission, requestCode)
                } else {
                    // User has denied permission permanently
                    showSettingsDialog(activity)
                }
            }
        }
    }

    private fun showPermissionRationaleDialog(activity: Activity, permission: String, requestCode: Int) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("This permission is required to proceed.")
            .setCancelable(false)
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionIfNeeded(activity, permission, requestCode)
            }
            .setNegativeButton("Deny", null)
            .show()
    }

    private fun showSettingsDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("You have permanently denied permission. Please enable it in app settings.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
