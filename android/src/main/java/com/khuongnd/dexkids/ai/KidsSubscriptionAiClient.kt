package com.khuongnd.dexkids.ai

import android.content.Context
import com.khuongnd.dexkids.auth.ChatGptAuthManager
import com.khuongnd.dexkids.auth.ChatGptFailure
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface KidsAiTransport {
    fun ready(): Boolean
    fun model(): String
    fun request(system: String, input: String, schema: JSONObject?, maxRequestBytes: Int,
                consent: () -> Boolean): String
}

/** Shared subscription-only adapter. No browser, implicit consent, provider fallback or replay. */
class KidsSubscriptionAiClient(context: Context) : KidsAiTransport {
    private val app = context.applicationContext
    private val settings = ChatGptSettings(app)
    private val auth = ChatGptAuthManager.get(app)
    override fun model() = settings.model()
    override fun ready() = settings.enabled && auth.connected() &&
        settings.models(auth.connectionKey()).any { it.slug == model() }

    override fun request(system: String, input: String, schema: JSONObject?,
                maxRequestBytes: Int, consent: () -> Boolean): String {
        if (!ready() || !consent()) throw IOException("AI_DISABLED")
        val selected = model()
        val epoch = auth.sessionGeneration()
        val owner = Thread.currentThread()
        val operation = SubscriptionOperation()
        fun allowed() = !owner.isInterrupted && consent() && settings.enabled && model() == selected &&
            auth.sessionGeneration() == epoch
        fun checkConsent() { if (!allowed()) { operation.cancel(); throw ChatGptFailure("CANCELLED") } }
        val instructions = system + "\nReturn only the requested JSON, without Markdown fences." +
            (schema?.let { "\nRequired JSON schema: $it" } ?: "")
        val payload = ChatGptResponsesClient.payload(selected,
            JSONArray().put(JSONObject().put("role", "user").put("content", input)), instructions)
        require(payload.toString().toByteArray(Charsets.UTF_8).size <= maxRequestBytes)
        // Interrupting existing gateway workers now cancels their actual HTTP read as well.
        val watcher = cancellations.scheduleWithFixedDelay({
            if (!runCatching { allowed() }.getOrDefault(false)) operation.cancel()
        }, 0, 50, TimeUnit.MILLISECONDS)
        try {
            val response = ChatGptResponsesClient(auth).response(payload, operation, beforeDispatch = {
                checkConsent()
                KidsAiDispatchLimiter.acquire(app, waitMillis = { delay ->
                    var remaining = delay
                    while (remaining > 0) {
                        operation.check(); checkConsent()
                        val step = minOf(remaining, 100L)
                        Thread.sleep(step); remaining -= step
                    }
                })
                checkConsent()
            })
            checkConsent()
            return ResponsesStream.chatText(response)
        } catch (e: ChatGptFailure) {
            // Quota and interrupted/ambiguous requests never enter the pack retry loop.
            throw IOException("SUBSCRIPTION_${e.code}")
        } finally { watcher.cancel(false); operation.cancel() }
    }
    companion object {
        private val cancellations = Executors.newSingleThreadScheduledExecutor { task ->
            Thread(task, "KidsAiCancellation").apply { isDaemon = true }
        }
    }
}
