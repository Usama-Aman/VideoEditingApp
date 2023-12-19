package com.elementary.youmerge

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference


class SettingsFragment : Fragment() {

    private lateinit var v: View
    private lateinit var resolutionSpinner: Spinner
    private lateinit var bitRateSpinner: Spinner
    private lateinit var frameRateSpinner: Spinner
    private lateinit var btnGoPro: LinearLayout
    private lateinit var tvTermsAndPrivacy: TextView

    var videoResolutionsList: MutableList<VideoSettingModel> = ArrayList()
    var bitRateList: MutableList<VideoSettingModel> = ArrayList()
    var frameRateList: MutableList<VideoSettingModel> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_settings, container, false)

        initViews()


        return v
    }

    private fun initViews() {
        btnGoPro = v.findViewById(R.id.btnGoPro)
        resolutionSpinner = v.findViewById(R.id.resolutionSpinner)
        bitRateSpinner = v.findViewById(R.id.bitRateSpinner)
        frameRateSpinner = v.findViewById(R.id.frameRateSpinner)
        tvTermsAndPrivacy = v.findViewById(R.id.tvTermsAndPrivacy)

        videoResolutionsList = (activity as MainActivity).videoResolutionsList
        bitRateList = (activity as MainActivity).bitRateList
        frameRateList = (activity as MainActivity).frameRateList
        setTermsAndPrivacyTextView()

        if (SharedPreference.getBoolean(context!!, Constants.IS_PRO_USER)) {
            btnGoPro.visibility = View.GONE
        } else {
            btnGoPro.visibility = View.VISIBLE
        }


        val resolutionStrings: MutableList<String> = ArrayList()
        for (i in videoResolutionsList.indices)
            resolutionStrings.add(videoResolutionsList[i].name)

        val bitRateStrings: MutableList<String> = ArrayList()
        for (i in bitRateList.indices)
            bitRateStrings.add(bitRateList[i].name)

        val frameRateStrings: MutableList<String> = ArrayList()
        for (i in frameRateList.indices)
            frameRateStrings.add(frameRateList[i].name)


        val resolutionAdapter = ArrayAdapter<String>(
            context!!,
            R.layout.item_spinner,
            resolutionStrings
        )
        resolutionAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down)
        resolutionSpinner.adapter = resolutionAdapter

        val bitRateAdapter = ArrayAdapter<String>(
            context!!,
            R.layout.item_spinner,
            bitRateStrings
        )
        bitRateAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down)
        bitRateSpinner.adapter = bitRateAdapter

        val frameRateAdapter = ArrayAdapter<String>(
            context!!,
            R.layout.item_spinner,
            frameRateStrings
        )
        frameRateAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down)
        frameRateSpinner.adapter = frameRateAdapter

        resolutionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d(
                    "Resolution", "${(activity as MainActivity).frameRateList[position].name} " +
                            "${(activity as MainActivity).frameRateList[position].id}" +
                            "${(activity as MainActivity).frameRateList[position].value}"
                )

                SharedPreference.saveInt(
                    context!!,
                    Constants.VIDEO_RESOLUTION,
                    position
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        bitRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d(
                    "Bit Rate", "${(activity as MainActivity).bitRateList[position].name} " +
                            "${(activity as MainActivity).bitRateList[position].id}" +
                            "${(activity as MainActivity).bitRateList[position].value}"
                )

                SharedPreference.saveInt(
                    context!!,
                    Constants.BIT_RATE, position
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        frameRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d(
                    "Frame Rate", "${(activity as MainActivity).frameRateList[position].name} " +
                            "${(activity as MainActivity).frameRateList[position].id}" +
                            "${(activity as MainActivity).frameRateList[position].value}"
                )

                SharedPreference.saveInt(
                    context!!,
                    Constants.FRAME_RATE, position
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }


        resolutionSpinner.setSelection(
            SharedPreference.getInt(
                context!!,
                Constants.VIDEO_RESOLUTION,
                1
            )
        )
        bitRateSpinner.setSelection(SharedPreference.getInt(context!!, Constants.BIT_RATE, 1))
        frameRateSpinner.setSelection(SharedPreference.getInt(context!!, Constants.FRAME_RATE, 2))


        btnGoPro.setOnClickListener {
            (activity as MainActivity).goToSelectedFragment(R.id.goPro)
        }

        frameRateSpinner.isEnabled = SharedPreference.getBoolean(context, Constants.IS_PRO_USER)
        bitRateSpinner.isEnabled = SharedPreference.getBoolean(context, Constants.IS_PRO_USER)
        resolutionSpinner.isEnabled = SharedPreference.getBoolean(context, Constants.IS_PRO_USER)

    }

    private fun setTermsAndPrivacyTextView() {

        val spannableString =
            SpannableString("Please read our Terms & Condition and Privacy Policy")

        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
//                ds.linkColor = ContextCompat.getColor(context!!, R.color.appColor)
                ds.bgColor = ContextCompat.getColor(context!!, R.color.transparent)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("content", "https://youmergeit.com/terms.html")
                context!!.startActivity(intent)
            }
        }
        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.bgColor = ContextCompat.getColor(context!!, R.color.transparent)
//                ds.linkColor = ContextCompat.getColor(context!!, R.color.appColor)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("content", "https://youmergeit.com/privacy.html")
                context!!.startActivity(intent)
            }
        }

        spannableString.setSpan(
            clickableSpan1,
            16,
            33,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.appColor)),
            16,
            33,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            clickableSpan2,
            38,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.appColor)),
            38,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTermsAndPrivacy.text = spannableString
        tvTermsAndPrivacy.movementMethod = LinkMovementMethod.getInstance()

    }

}