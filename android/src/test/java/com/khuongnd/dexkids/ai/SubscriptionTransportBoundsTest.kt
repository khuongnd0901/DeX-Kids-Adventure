package com.khuongnd.dexkids.ai

import com.khuongnd.dexkids.auth.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SubscriptionTransportBoundsTest {
    private class Auth : SubscriptionSessionSource {
        override fun freshSession(rejectedAccessToken: String?) = ChatGptSession("oaiapp_synthetic", "s", "", "id", "synthetic", "", "resource.invoke chatgpt.tokens.use.direct", Long.MAX_VALUE)
        override fun failure(e: Exception) {}
    }
    @Test fun payloadOmitsUnsupportedFields() {
        val p = ChatGptResponsesClient.payload("account-model", JSONArray().put(JSONObject().put("role", "user").put("content", "chào")), "Only JSON")
        assertFalse(p.getBoolean("store")); assertTrue(p.getBoolean("stream"))
        listOf("temperature", "max_output_tokens", "previous_response_id", "tools").forEach { assertFalse(p.has(it)) }
    }
    @Test fun cancellationDuringAuthPreventsAnyPhysicalDispatch() {
        var calls=0
        val operation=SubscriptionOperation()
        val auth=object: SubscriptionSessionSource {
            override fun freshSession(rejectedAccessToken: String?): ChatGptSession { operation.cancel(); return Auth().freshSession(null) }
            override fun failure(e: Exception) {}
        }
        try { ChatGptResponsesClient(auth).response(JSONObject(),operation,beforeDispatch={calls++}); fail() }
        catch(e:ChatGptFailure){assertEquals("CANCELLED",e.code)}
        assertEquals(0,calls)
    }
    @Test fun dispatchGateCountsBoth401Starts() {
        var calls=0
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            okhttp3.Response.Builder().request(chain.request()).protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401).message("synthetic").body(okhttp3.ResponseBody.create(null,"" )).build()
        }.build()
        try { ChatGptResponsesClient(Auth(),client).response(JSONObject(),beforeDispatch={calls++});fail() }
        catch(e:ChatGptFailure){assertEquals("SESSION_EXPIRED",e.code)}
        assertEquals(2,calls)
    }
    @Test fun stalledBodyHasBoundedTimeoutAndNoReplay() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("data: {}\n\n").setBodyDelay(2,TimeUnit.SECONDS))
            val client=OkHttpClient.Builder().retryOnConnectionFailure(false).readTimeout(100,TimeUnit.MILLISECONDS)
                .callTimeout(300,TimeUnit.MILLISECONDS).addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().url(server.url("/responses")).build())
                }.build()
            try { ChatGptResponsesClient(Auth(),client).response(JSONObject());fail() }
            catch(e:ChatGptFailure){assertEquals("NETWORK_OR_INVALID_RESPONSE",e.code)}
            assertEquals(1,server.requestCount)
        }
    }
    @Test fun activeCancellationDropsPartialResponse() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("data: {}\n\n").setBodyDelay(2,TimeUnit.SECONDS))
            val client=OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().url(server.url("/responses")).build())
            }.build()
            val operation=SubscriptionOperation();val executor=Executors.newSingleThreadExecutor()
            try {
                val future=executor.submit<String> {
                    try {ChatGptResponsesClient(Auth(),client).response(JSONObject(),operation); "UNEXPECTED"}
                    catch(e:ChatGptFailure){e.code}
                }
                assertNotNull(server.takeRequest(2,TimeUnit.SECONDS));operation.cancel()
                assertEquals("CANCELLED",future.get(2,TimeUnit.SECONDS));assertEquals(1,server.requestCount)
            }finally{executor.shutdownNow()}
        }
    }
}
