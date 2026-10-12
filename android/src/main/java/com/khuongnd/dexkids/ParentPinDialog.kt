package com.khuongnd.dexkids

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import com.khuongnd.dexkids.parent.ParentPin

/** Optional local gate. Derivation runs off the UI thread; cancellation never authorizes. */
object ParentPinDialog {
    fun unlock(activity: Activity, accepted: () -> Unit, cancelled: () -> Unit = {}) {
        val settings = ParentSettings(activity)
        val credential = settings.pinCredential
        if (credential == null) { accepted(); return }
        show(activity, false, cancelled) { pin, _, complete ->
            val now = System.currentTimeMillis()
            if (settings.pinRetryAfter > now) {
                complete("Too many attempts. Wait 30 seconds.", null)
            } else {
                Thread({
                    val ok = runCatching { ParentPin.verify(pin, credential) }.getOrDefault(false)
                    activity.runOnUiThread {
                        complete(if (ok) null else "Incorrect PIN", {
                            if (ok) {
                                settings.pinFailures = 0
                                settings.pinRetryAfter = 0
                                accepted()
                            } else {
                                settings.pinFailures += 1
                                if (settings.pinFailures >= 3) {
                                    settings.pinRetryAfter = System.currentTimeMillis() + 30_000
                                    settings.pinFailures = 0
                                }
                            }
                        })
                    }
                }, "dexkids-pin-check").start()
            }
        }
    }

    fun configure(activity: Activity, changed: () -> Unit) {
        show(activity, true, {}) { pin, confirmation, complete ->
            if (!ParentPin.valid(pin) || pin != confirmation) {
                complete("Enter matching PINs with 4 to 8 digits.", null)
            } else {
                Thread({
                    val encoded = runCatching { ParentPin.encode(pin) }
                    activity.runOnUiThread {
                        complete(if (encoded.isSuccess) null else "Unable to save PIN.", {
                            encoded.getOrNull()?.let {
                                ParentSettings(activity).apply {
                                    pinCredential = it
                                    pinFailures = 0
                                    pinRetryAfter = 0
                                }
                                changed()
                            }
                        })
                    }
                }, "dexkids-pin-create").start()
            }
        }
    }

    private fun show(activity: Activity, setup: Boolean, cancelled: () -> Unit,
                     submit: (String, String, (String?, (() -> Unit)?) -> Unit) -> Unit) {
        fun input(hintText: String) = EditText(activity).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(android.text.InputFilter.LengthFilter(8))
            importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
        }
        val pin = input("Parent PIN")
        val confirmation = input("Confirm PIN")
        val fields = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
            addView(pin)
            if (setup) addView(confirmation)
        }
        var succeeded = false
        val dialog = AlertDialog.Builder(activity)
            .setTitle(if (setup) "Set parent PIN" else "Enter parent PIN")
            .setView(fields).setNegativeButton("Cancel", null)
            .setPositiveButton(if (setup) "Save PIN" else "Unlock", null)
            .create()
        dialog.setOnDismissListener { if (!succeeded) cancelled() }
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                button.isEnabled = false
                submit(pin.text.toString(), confirmation.text.toString()) { error, action ->
                    if (dialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
                        button.isEnabled = true
                        if (error == null) {
                            succeeded = true
                            dialog.dismiss()
                            action?.invoke()
                        } else {
                            action?.invoke()
                            pin.text.clear()
                            pin.error = error
                        }
                    }
                }
            }
        }
        dialog.show()
    }
}
