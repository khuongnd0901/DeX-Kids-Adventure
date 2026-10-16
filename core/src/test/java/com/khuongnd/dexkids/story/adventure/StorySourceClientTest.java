package com.khuongnd.dexkids.story.adventure;

import org.junit.jupiter.api.Test;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StorySourceClientTest {
    static class Response extends HttpURLConnection {
        int status; byte[] body; Map<String,String> headers=new HashMap<>(); boolean disconnected;
        Response(int status,byte[] body) throws Exception {super(new URL("https://example.org/pack.json"));this.status=status;this.body=body;}
        public void disconnect(){disconnected=true;} public boolean usingProxy(){return false;} public void connect(){}
        public int getResponseCode() throws IOException {return status;}
        public InputStream getInputStream() throws IOException {return new ByteArrayInputStream(body);}
        public String getHeaderField(String key){return headers.get(key);}
        public long getContentLengthLong(){return -1;}
    }
    @Test void publicHttpsOnly() {
        for(String url:List.of("http://example.org/p","file:///p","https://user:pass@example.org/p","https://example.org/p#part","not a url"))
            assertThrows(IllegalArgumentException.class,()->StorySourceClient.validateUrl(url));
        assertEquals("https://example.org/p",StorySourceClient.validateUrl(" https://example.org/p "));
    }
    @Test void conditionalDownloadAndNoChildHeaders() throws Exception {
        var c=new Response(200,"{}".getBytes(StandardCharsets.UTF_8));c.headers.put("ETag","\"v2\"");
        var result=new StorySourceClient(url->c).fetch("https://example.org/pack.json","\"v1\"","yesterday");
        assertArrayEquals(c.body,result.body());assertEquals("\"v2\"",result.etag());assertFalse(result.unchanged());
        assertEquals("\"v1\"",c.getRequestProperty("If-None-Match"));
        assertNull(c.getRequestProperty("Authorization"));assertNull(c.getRequestProperty("Cookie"));assertTrue(c.disconnected);
        assertEquals(5000,c.getConnectTimeout());assertEquals(10000,c.getReadTimeout());assertFalse(c.getInstanceFollowRedirects());
        var unchanged=new Response(304,new byte[0]);
        assertTrue(new StorySourceClient(url->unchanged).fetch("https://example.org/p","etag",null).unchanged());
    }
    @Test void redirectsCannotDowngradeOrForwardValidators() throws Exception {
        var first=new Response(302,new byte[0]);first.headers.put("Location","https://other.org/p");
        var second=new Response(200,new byte[0]);var queue=new ArrayDeque<>(List.of(first,second));
        new StorySourceClient(url->queue.remove()).fetch("https://example.org/p","etag","yesterday");
        assertNull(second.getRequestProperty("If-None-Match"));assertTrue(first.disconnected);assertTrue(second.disconnected);
        first.headers.put("Location","http://example.org/p");
        assertThrows(IllegalArgumentException.class,()->new StorySourceClient(url->first).fetch("https://example.org/p",null,null));
        first.headers.put("Location","/again");
        assertThrows(IOException.class,()->new StorySourceClient(url->first).fetch("https://example.org/p",null,null));
    }
    @Test void failuresBoundedStreamsTimeoutAndCleanup() throws Exception {
        for(int status:List.of(404,500)) {
            var c=new Response(status,new byte[0]);
            assertThrows(IOException.class,()->new StorySourceClient(url->c).fetch("https://example.org/p",null,null));assertTrue(c.disconnected);
        }
        var large=new Response(200,new byte[StoryPackCodec.MAX_BYTES+1]);
        assertThrows(IOException.class,()->new StorySourceClient(url->large).fetch("https://example.org/p",null,null));assertTrue(large.disconnected);
        var timeout=new Response(200,new byte[0]) {public InputStream getInputStream() throws IOException {throw new SocketTimeoutException();}};
        assertThrows(SocketTimeoutException.class,()->new StorySourceClient(url->timeout).fetch("https://example.org/p",null,null));assertTrue(timeout.disconnected);
    }
}
