package com.cranezo.utils

import android.app.Activity
import com.cranezo.R
import com.razorpay.Checkout
import org.json.JSONObject

/** The ways a customer can pay, chosen in the app's own picker before Razorpay opens */
enum class PayMethod { UPI, CARD, NETBANKING, MORE }

/**
 * One look for every Razorpay payment in the app (booking advance, final payment, tip, subscription):
 * the Cranezo logo and colour, a retry on failure and a time limit so the payment page never stays open
 * for long. The customer picks UPI / card / net banking in the app first (PaymentMethodSheet),
 * so Razorpay opens straight on that method and not on its own method list.
 * The bank test page ("Success / Failure") is Razorpay's simulator and only appears with test keys.
 */
object CheckoutStyle {

    /** Adds the look to [options] (an order's name, amount and id are already in it) */
    fun apply(options: JSONObject, method: PayMethod? = null): JSONObject {
        options
            .put("theme", JSONObject().put("color", "#FFA000").put("backdrop_color", "#0B1220"))
            // the payment page closes by itself after 5 minutes
            .put("timeout", 300)
            .put("retry", JSONObject().put("enabled", true).put("max_count", 2))
            .put("remember_customer", false)
            .put("send_sms_hash", true)
            .put("allow_rotation", false)
        if (method != null) {
            // only the chosen kind of payment is shown: Razorpay opens straight on it
            val shown = JSONObject()
                .put("upi", method == PayMethod.UPI)
                .put("card", method == PayMethod.CARD)
                .put("netbanking", method == PayMethod.NETBANKING)
                .put("wallet", method == PayMethod.MORE)
                .put("emi", method == PayMethod.MORE)
                .put("paylater", method == PayMethod.MORE)
            options.put("method", shown)
            val prefillKey = when (method) {
                PayMethod.UPI -> "upi"
                PayMethod.CARD -> "card"
                PayMethod.NETBANKING -> "netbanking"
                PayMethod.MORE -> "wallet"
            }
            val prefill = options.optJSONObject("prefill") ?: JSONObject().also { options.put("prefill", it) }
            prefill.put("method", prefillKey)
        }
        return options
    }

    /** Opens Checkout with the Cranezo logo and look, on the chosen method */
    fun open(activity: Activity, keyId: String?, options: JSONObject, method: PayMethod? = null) {
        val checkout = Checkout().apply {
            setKeyID(keyId)
            setImage(R.drawable.cranezo_logo)
        }
        checkout.open(activity, apply(options, method))
    }
}
