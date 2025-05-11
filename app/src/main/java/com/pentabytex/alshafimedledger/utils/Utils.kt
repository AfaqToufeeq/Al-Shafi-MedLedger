package com.pentabytex.alshafimedledger.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.enums.LogLevel
import com.pentabytex.alshafimedledger.utils.Constants.EMAIL_ADDRESS
import java.util.UUID


object Utils {

    fun hideStatusBar(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }


    fun setStatusBarColor(activity: AppCompatActivity, colorResId: Int) {
        activity.window.statusBarColor = activity.resources.getColor(colorResId, activity.theme)
    }

    fun mode(sharePref: Boolean) {
        when (sharePref) {
            true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun log(level: LogLevel, message: String, tag: String = "checkLogs", throwable: Throwable? = null) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARNING -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }


    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }



    fun progressDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog.setContentView(R.layout.layout_progress_dialog)
        dialog.setCancelable(false)
        val lottieAnimation = dialog.findViewById<ImageView>(R.id.gif)
        Glide.with(context).load(R.drawable.load_gif).into(lottieAnimation)
        return dialog
    }


    fun sendEmail(context: Context) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject of the email")

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } catch (ex: ActivityNotFoundException) {
            showToast(context,"Error: ${ex.message}")
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun setHandler(loader: Dialog) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (loader.isShowing) loader.dismiss()
        },5000)
    }


    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun String.isValidPhone(): Boolean {
        return Patterns.PHONE.matcher(this).matches()
    }


    /**
     * Generic function to navigate from one activity to another with optional transitions.
     *
     * @param context The context from which the navigation is triggered.
     * @param targetActivityClass The class of the target activity to navigate to.
     * @param finishCurrentActivity Boolean flag to indicate if the current activity should be finished.
     * @param activityOptions Options for custom activity transition animations.
     * @param extras Optional bundle of extra data to pass to the target activity.
     */

    fun navigateToActivity(
        context: Context,
        targetActivityClass: Class<*>,
        finishCurrentActivity: Boolean = false,
        isAnimation: Boolean = false,
        extras: Bundle? = null
    ) {
        val intent = Intent(context, targetActivityClass)
        extras?.let { intent.putExtras(it) }

        val activityOptions = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )

        if (isAnimation)
            context.startActivity(intent, activityOptions.toBundle())
        else
            context.startActivity(intent)

        if (finishCurrentActivity) {
            (context as Activity).finish()
        }
    }



    /**
     * Opens the dialer with the given phone number.
     */
    fun dialNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
        context.startActivity(intent)
    }

}