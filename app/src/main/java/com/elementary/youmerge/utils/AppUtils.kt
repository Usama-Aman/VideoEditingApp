package com.elementary.youmerge.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.elementary.youmerge.R
import com.irozon.sneaker.Sneaker


@Suppress("DEPRECATION")
class AppUtils {

    companion object {

        private var dialog: ProgressDialog? = null

        fun showDialog(context: Context) {
            try {
                hideKeyboard(context as AppCompatActivity)
                if (dialog != null) {
                    if (!dialog?.isShowing!!) {
                        dialog?.show()
                    }
                } else
                    initializedDialog(context)

            } catch (e: Exception) {
                dialog = null
                e.printStackTrace()
            }
        }

        private fun initializedDialog(context: Context) {
            dialog = ProgressDialog(context)
            dialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            dialog?.setTitle("Loading")
            dialog?.setMessage("Please wait")
            dialog?.setCancelable(false)
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.isIndeterminate = true
            dialog?.show()
        }

        fun dismissDialog() {
            try {
                if (dialog != null) {
                    if (dialog?.isShowing!!)
                        dialog?.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getVideoDuration(path: String, context: Context): Long {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(path))
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()

                duration?.toLongOrNull()?.div(1000) ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }

        //        fun isNetworkAvailable(context: Context): Boolean {
//            val cm = context
//                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val netInfo = cm.activeNetworkInfo
//            return (netInfo != null && netInfo.isConnectedOrConnecting
//                    && cm.activeNetworkInfo!!.isAvailable
//                    && cm.activeNetworkInfo!!.isConnected)
//        }
//
//
        fun hideKeyboard(context: Activity) {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = context.currentFocus
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
            }
        }

        //
//        fun validateEmail(email: String): Boolean {
//            return !(email.isEmpty() || !isValidEmail(email))
//        }
//
//        private fun isValidEmail(email: String): Boolean {
//            return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(
//                email
//            )
//                .matches()
//        }
//
//        fun validatePhone(phone: String): Boolean {
//            return !(phone.isEmpty() || !isValidPhone(phone));
//        }
//
//        fun isValidPhone(phone: String): Boolean {
//            return !TextUtils.isEmpty(phone) && android.util.Patterns.PHONE.matcher(phone)
//                .matches()
//        }
//
//        fun setBackground(view: View, bg: Int) {
//            view.setBackgroundResource(bg)
//        }
//
        fun showToast(activity: Activity?, message: String, isSuccess: Boolean) {
            try {
                if (isSuccess)
                    Sneaker.with(activity!!)
                        .setTitle("Success")
                        .autoHide(true)
                        .setDuration(1500)
                        .setMessage(message, activity.resources.getColor(R.color.white))
                        .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .sneakSuccess()
                else
                    Sneaker.with(activity!!)
                        .setTitle("Error")
                        .autoHide(true)
                        .setDuration(1500)
                        .setMessage(message, activity.resources.getColor(R.color.white))
                        .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .sneakError()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
//
//        fun createPlainRequestBody(string: String): RequestBody =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), string)
//
//        fun createFileMultiPart(
//            imagePath: String,
//            key: String,
//            ext: String
//        ): MultipartBody.Part {
//
//            val file = File(imagePath)
//
//            val requestBody = if (ext == ".pdf")
//                file.asRequestBody("application/pdf".toMediaTypeOrNull())
//            else
//                file.asRequestBody("image/*".toMediaTypeOrNull())
//
//            return MultipartBody.Part.createFormData(key, file.name, requestBody)
//        }
//
//        fun getLoginModel(context: Context): LoginModel {
//            val gson = Gson()
//            return gson.fromJson(
//                SharedPreference.getSimpleString(context, Constants.userData),
//                LoginModel::class.java
//            )
//        }
//
//        fun getDateFormat(
//            context: Context,
//            day: Int,
//            monthOfYear: Int,
//            year: Int
//        ): String {
//            val gson = Gson()
//            val settingModel = gson.fromJson(
//                SharedPreference.getSimpleString(context, Constants.settingsData),
//                SettingsModel::class.java
//            )
//
//            for (i in settingModel.data.date_formats.indices)
//                if (settingModel.data.date_formats[i].value == settingModel.data.settings.date_format.toInt())
//                    when (settingModel.data.date_formats[i].format) {
//                        "YYYY-MM-DD" -> {
//                            return "$year-$monthOfYear-$day"
//                        }
//                        "DD-MM-YYYY" -> {
//                            return "$day-$monthOfYear-$year"
//                        }
//                        "YYYY/MM/DD" -> {
//                            return "$year/$monthOfYear/$day"
//                        }
//                        "DD/MM/YYYY" -> {
//                            return "$day/$monthOfYear/$year"
//                        }
//                        else -> {
//                            return "$day-$monthOfYear-$year"
//                        }
//                    }
//            return ""
//        }

        fun preventTwoClick(view: View) {
            view.isEnabled = false
            view.postDelayed({ view.isEnabled = true }, 500)
        }


        fun hideWithAnimation(view: View) {
            view.animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        view.visibility = View.GONE
                    }
                })
        }
    }
}