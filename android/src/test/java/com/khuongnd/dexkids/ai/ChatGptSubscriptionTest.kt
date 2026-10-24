package com.khuongnd.dexkids.ai

import com.khuongnd.dexkids.auth.*
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ChatGptSubscriptionTest {
    private fun completed(text: String = "Xin chào") = JSONObject().put("id", "r1").put("status", "completed")
        .put("output", JSONArray().put(JSONObject().put("type", "message").put("role", "assistant")
            .put("status", "completed").put("content", JSONArray().put(JSONObject().put("type", "output_text").put("text", text)))))
    private fun event(type: String, response: JSONObject? = null): String = "event: $type\ndata: " +
        JSONObject().put("type", type).apply { if (response != null) put("response", response) }.toString() + "\n\n"
    private fun stream() = event("response.created", JSONObject().put("id", "r1")) + event("response.completed", completed())
    private fun failure(code: String? = null, block: () -> Unit) {
        try { block(); fail("must reject") }
        catch (e: ChatGptFailure) { if (code != null) assertEquals(code, e.code) }
    }
    @Test fun vietnameseCompletionIsAuthoritative() {
        val deltas = StringBuilder()
        val delta = "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Xin\"}\n\n"
        val response = ResponsesStream.read(Buffer().writeUtf8(delta + stream()), onDelta = { deltas.append(it) })
        assertEquals("Xin", deltas.toString()); assertEquals("Xin chào", ResponsesStream.chatText(response))
    }
    @Test fun missingTerminalMalformedIncompleteAndQuotaFailClosed() {
        failure("INCOMPLETE_RESPONSE") { ResponsesStream.read(Buffer().writeUtf8("data: {\"type\":\"response.output_text.delta\",\"delta\":\"partial\"}\n\n")) }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8("data: not-json\n\n")) }
        failure("INCOMPLETE_RESPONSE") { ResponsesStream.read(Buffer().writeUtf8(event("response.incomplete"))) }
        failure("QUOTA_LIMITED") { ResponsesStream.read(Buffer().writeUtf8(event("response.failed", JSONObject().put("error", JSONObject().put("code", "subscription_sharing_usage_limit_exceeded"))))) }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(event("response.completed", completed().put("status", "in_progress")))) }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(event("response.created", JSONObject().put("id", "other")) + event("response.completed", completed()))) }
    }
    @Test fun cancelledAndOversizedStreamsFail() {
        val op = SubscriptionOperation(); op.cancel()
        failure("CANCELLED") { ResponsesStream.read(Buffer().writeUtf8(stream()), op::check) }
        failure("STREAM_TOO_LARGE") { ResponsesStream.read(Buffer().writeUtf8("data: " + JSONObject().put("type", "response.output_text.delta").put("delta", "x".repeat(65537)) + "\n\n")) }
    }
    @Test fun chatNeverAcceptsToolCallsOrOverlongAnswers() {
        failure("CHAT_INVALID_OUTPUT") { ResponsesStream.chatText(completed("x".repeat(16001))) }
        failure("CHAT_INVALID_OUTPUT") { ResponsesStream.chatText(JSONObject().put("output", JSONArray().put(JSONObject().put("type", "function_call")))) }
    }

    @Test fun modelsComeOnlyFromVisibleAccountCatalog() {
        val json = JSONObject("""{"models":[{"slug":"account-a","display_name":"A","visibility":"list"},{"slug":"hidden","visibility":"hide"},{"slug":"account-b","visibility":"list"}]}""")
        assertEquals(listOf("account-a", "account-b"), ChatGptModelRepository.parse(json).map { it.slug })
    }


    private class Auth : SubscriptionSessionSource {
        var refreshes = 0
        override fun freshSession(rejectedAccessToken: String?): ChatGptSession {
            if (rejectedAccessToken != null) refreshes++
            return ChatGptSession("oaiapp_test", "sub", "", "id", "token$refreshes", "refresh", "resource.invoke chatgpt.tokens.use.direct", Long.MAX_VALUE)
        }
        override fun failure(e: Exception) {}
    }
    @Test fun transportRefreshesPreStream401OnceButNeverRetriesOtherFailures() {
        for (status in listOf(401, 403, 429, 500, 503)) {
            val auth = Auth(); var requests = 0
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val code = if (requests++ == 0) status else 200
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message("synthetic")
                    .header("Content-Type", "text/event-stream")
                    .body((if (code == 200) stream() else "private provider body").toResponseBody("text/event-stream".toMediaType())).build()
            }.build()
            val responses = ChatGptResponsesClient(auth, client)
            val request = ChatGptResponsesClient.payload("account-model", JSONArray().put(JSONObject().put("role", "user").put("content", "chào")), "Vietnamese")
            if (status == 401) {
                assertEquals("Xin chào", ResponsesStream.chatText(responses.response(request)))
                assertEquals(2, requests); assertEquals(1, auth.refreshes)
            } else {
                failure { responses.response(request) }; assertEquals(1, requests); assertEquals(0, auth.refreshes)
            }
        }
    }
    @Test fun completedSseDoesNotDependOnExactMimeButJsonBodyCannotSucceed() {
        for (media in listOf(null, "Text/Event-Stream", "application/octet-stream")) {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                    .apply { if (media != null) header("Content-Type", media) }
                    .body(stream().toResponseBody()).build()
            }.build()
            assertEquals("Xin chào", ResponsesStream.chatText(ChatGptResponsesClient(Auth(), client).response(JSONObject())))
        }
        val jsonClient = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("Content-Type", "application/json").body("{}\n".toResponseBody()).build()
        }.build()
        failure("INCOMPLETE_RESPONSE") { ChatGptResponsesClient(Auth(), jsonClient).response(JSONObject()) }
    }
    private fun itemEvent(type: String, index: Int = 0, item: JSONObject): String = "data: " +
        JSONObject().put("type", type).put("output_index", index).put("item", item) + "\n\n"
    private fun emptyTerminal() = event("response.completed", JSONObject().put("id", "r1")
        .put("status", "completed").put("output", JSONArray()))
    private fun item() = completed().getJSONArray("output").getJSONObject(0).put("id", "m1")
    private fun created() = event("response.created", JSONObject().put("id", "r1"))

    @Test fun subscriptionEmptyTerminalUsesOnlyCompletedOutputItems() {
        val message = item()
        val added = itemEvent("response.output_item.added", item = JSONObject().put("id", "m1").put("status", "in_progress"))
        val done = itemEvent("response.output_item.done", item = message)
        assertEquals("Xin chào", ResponsesStream.chatText(ResponsesStream.read(Buffer().writeUtf8(created() + added + done + emptyTerminal()))))
        failure("INCOMPLETE_RESPONSE") { ResponsesStream.read(Buffer().writeUtf8(created() + added + done)) }
        failure("INCOMPLETE_RESPONSE") { ResponsesStream.read(Buffer().writeUtf8(created() + added + emptyTerminal())) }
    }
    @Test fun duplicateGappedOrIncompleteDoneItemsCannotBecomeAReply() {
        val done = itemEvent("response.output_item.done", item = item())
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(created() + done + done + emptyTerminal())) }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(created() + itemEvent("response.output_item.done", 1, item()) + emptyTerminal())) }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(created() + itemEvent("response.output_item.done", item = item().put("status", "incomplete")) + emptyTerminal())) }
        val other = JSONObject().put("id", "other").put("status", "in_progress")
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(created() + itemEvent("response.output_item.added", item = other) + done + emptyTerminal())) }
    }
    @Test fun finalSnapshotCannotOverrideConflictingCompletedItem() {
        val final = completed("Different").apply { getJSONArray("output").getJSONObject(0).put("id", "m1") }
        failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(created() + itemEvent("response.output_item.done", item = item()) + event("response.completed", final))) }
    }

    @Test fun contradictoryTerminalErrorFieldsNeverAuthorizeOutput() {
        for (error in listOf<Any>("malformed error", JSONObject().put("code", "failed"))) {
            failure("INVALID_STREAM") { ResponsesStream.read(Buffer().writeUtf8(event("response.completed", completed().put("error", error)))) }
        }
    }
    @Test fun repeated401StopsAfterOneRefreshAndNetworkFailureIsNotReplayed() {
        for (networkError in listOf(false, true)) {
            val auth = Auth(); var requests = 0
            val client = OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor { chain ->
                requests++
                if (networkError) throw java.io.IOException("synthetic offline")
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(401).message("Denied")
                    .body("hidden".toResponseBody()).build()
            }.build()
            failure(if (networkError) "NETWORK_OR_INVALID_RESPONSE" else "SESSION_EXPIRED") {
                ChatGptResponsesClient(auth, client).response(JSONObject())
            }
            assertEquals(if (networkError) 1 else 2, requests)
            assertEquals(if (networkError) 0 else 1, auth.refreshes)
        }
    }
    @Test fun interruptedStreamIsNotReissued() {
        val auth = Auth(); var requests = 0
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            requests++
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("Content-Type", "text/event-stream").body(event("response.created", JSONObject().put("id", "r1")).toResponseBody()).build()
        }.build()
        failure("INCOMPLETE_RESPONSE") { ChatGptResponsesClient(auth, client).response(JSONObject()) }
        assertEquals(1, requests)
    }


    @Test fun failedOrTruncatedSseCannotReplayOrReturnPartialText() {
        for (body in listOf("data: {\"type\":\"response.output_text.delta\",\"delta\":\"partial\"}\n\n", "data: broken\n\n")) {
            var requests = 0
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                requests++
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("fixture")
                    .body(body.toResponseBody()).build()
            }.build()
            failure { ChatGptResponsesClient(Auth(), client).response(JSONObject()) }
            assertEquals(1, requests)
        }
    }
}
