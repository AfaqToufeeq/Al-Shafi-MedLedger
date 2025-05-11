package com.pentabytex.alshafimedledger.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pentabytex.alshafimedledger.enums.LogLevel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object ImagePicker {
    private const val REQUEST_PERMISSION_CAMERA = 100
    private const val REQUEST_PERMISSION_STORAGE = 101

    fun checkPermissionsAndPickImage(
        owner: LifecycleOwner,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        dispatcherProvider: CoroutineDispatcherProvider,
        callback: (Uri?) -> Unit
    ) {
        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.P -> {
                handleCameraPermission(owner, takePictureLauncher, dispatcherProvider, callback)
            }
            else -> {
                handleLegacyPermissions(owner, takePictureLauncher, dispatcherProvider, callback)
            }
        }
    }

    private fun handleCameraPermission(
        owner: LifecycleOwner,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        dispatcherProvider: CoroutineDispatcherProvider,
        callback: (Uri?) -> Unit
    ) {
        val (context, activity) = getContextAndActivity(owner) ?: return


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        } else {
            launchCamera(owner, takePictureLauncher, dispatcherProvider, callback)
        }
    }

    private fun handleLegacyPermissions(
        owner: LifecycleOwner,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        dispatcherProvider: CoroutineDispatcherProvider,
        callback: (Uri?) -> Unit
    ) { // Added return type
        val (context, activity) = getContextAndActivity(owner) ?: return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_STORAGE
            )
        } else {
            launchCamera(owner, takePictureLauncher, dispatcherProvider, callback)
        }
    }

    private fun launchCamera(
        owner: LifecycleOwner,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        dispatcherProvider: CoroutineDispatcherProvider,
        callback: (Uri?) -> Unit
    ) {
        val (context, activity) = getContextAndActivity(owner) ?: return

        var imageUri: Uri?
        owner.lifecycleScope.launch(dispatcherProvider.io) {
            try {
                imageUri = createImageUri(context)
                imageUri?.let { takePictureLauncher.launch(it) }
                callback.invoke(imageUri)
            } catch (e: Exception) {
                withContext(dispatcherProvider.main) {
                    Utils.log(LogLevel.ERROR, "Error creating image URI $e")
                    Utils.showToast(activity, "Failed to open camera")
                }
            }
        }
    }

    private fun createImageUri(context: Context): Uri? {
        val imageFile = File(context.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", imageFile)
    }

    fun selectGalleryImage(pickImageLauncher: ActivityResultLauncher<String>) {
        pickImageLauncher.launch("image/*")
    }

    private fun getContextAndActivity(owner: LifecycleOwner): Pair<Context, Activity>? {
        return when (owner) {
            is Activity -> owner to owner
            is Fragment -> owner.requireContext() to owner.requireActivity()
            else -> null
        }
    }
}
