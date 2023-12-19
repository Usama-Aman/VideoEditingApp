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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference

class GoProActivity : Fragment() {

    private lateinit var btnCloseGoPro: ImageView
    private lateinit var btnGoPro: LinearLayout
    private lateinit var btnGoProText: TextView
    private lateinit var subtitle: TextView
    private lateinit var tvTermsAndPrivacy: TextView

    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.activity_go_pro, container, false)

        initViews()

        return v
    }


    private fun initViews() {
        btnCloseGoPro = v.findViewById(R.id.closeGoPro)
        btnGoPro = v.findViewById(R.id.btnGo)
        btnGoProText = v.findViewById(R.id.btnGoProText)
        tvTermsAndPrivacy = v.findViewById(R.id.tvTermsAndPrivacy)
        subtitle = v.findViewById(R.id.subtitle)


        setTermsAndPrivacyTextView()
//        val data = "<div>\n" +
//                "<h2><u>Why do we use it?</u></h2>\n" +
//                "<p>It is a long <strong>established</strong> fact that a reader will be <u>distracted</u> by the readable content of a page when looking at its <span style=\"background-color: #f1c40f;\"><strong><em><u>layout</u></em></strong></span>. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, <span style=\"color: #e03e2d;\">sometimes</span> on purpose (injected humour and the like).</p>\n" +
//                "</div>"
//        val html =
//            "<html><body style=\\\" font-size:14pt; font-family: 'Arial', sans-serif; \\\">$data</body></html>"
//
//        subtitle.text = Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)


        if (SharedPreference.getBoolean(context!!, Constants.IS_PRO_USER)) {
            btnGoPro.visibility = View.GONE
        } else {
            btnGoPro.visibility = View.VISIBLE
            btnGoProText.text = "GO PRO"
            btnGoPro.background =
                ContextCompat.getDrawable(context!!, R.drawable.green_button_drawable)
        }


        btnGoPro.setOnClickListener {
            SharedPreference.saveBoolean(
                context!!, Constants.IS_PRO_USER,
                !SharedPreference.getBoolean(context!!, Constants.IS_PRO_USER)
            )

            val i = Intent(context!!, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
    }

    private fun setTermsAndPrivacyTextView() {

        val spannableString =
            SpannableString("Please read our Terms & Condition and Privacy Policy")

        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
//                ds.linkColor = ContextCompat.getColor(context!!, R.color.appColor)
                ds.bgColor = ContextCompat.getColor(context!!,R.color.transparent)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                val intent = Intent(context,WebViewActivity::class.java)
                intent.putExtra("content", "https://youmergeit.com/terms.html")
                context!!.startActivity(intent)
            }
        }
        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.bgColor = ContextCompat.getColor(context!!,R.color.transparent)
//                ds.linkColor = ContextCompat.getColor(context!!, R.color.appColor)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                val intent = Intent(context,WebViewActivity::class.java)
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