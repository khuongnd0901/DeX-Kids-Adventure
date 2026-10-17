package com.khuongnd.dexkids.story.adventure;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

/** Public HTTPS GET only. No app identifiers, cookies, credentials or child data. */
public final class StorySourceClient {
    @FunctionalInterface public interface Connections { HttpURLConnection open(URL url) throws IOException; }
    public record Result(boolean unchanged, byte[] body, String etag, String modified) {}
    private final Connections connections;
    private final int maxBytes;
    private final String accept;
    public StorySourceClient() { this(url -> (HttpURLConnection)url.openConnection()); }
    public StorySourceClient(Connections connections) { this(connections, StoryPackCodec.MAX_BYTES, "application/json"); }
    public StorySourceClient(Connections connections, int maxBytes, String accept) {
        this.connections=connections; this.maxBytes=maxBytes; this.accept=accept;
    }

    public static String validateUrl(String text) {
        try {
            URI uri=new URI(text.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null
                || uri.getFragment()!=null || text.length()>2048) throw new IllegalArgumentException("SOURCE_HTTPS_REQUIRED");
            return uri.toASCIIString();
        } catch (URISyntaxException e) { throw new IllegalArgumentException("SOURCE_URL_INVALID", e); }
    }

    public Result fetch(String source, String etag, String modified) throws IOException {
        URL url=new URL(validateUrl(source));
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
        for (int redirects=0; redirects<=3; redirects++) {
            if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("SOURCE_CANCELLED");
            HttpURLConnection connection=connections.open(url);
            try {
                connection.setInstanceFollowRedirects(false); connection.setUseCaches(false);
                connection.setConnectTimeout(5000); connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept",accept);
                connection.setRequestProperty("Accept-Encoding","identity");
                // Do not forward cache validators to another origin after a redirect.
                if(redirects==0) {
                    if(etag!=null && !etag.isEmpty())connection.setRequestProperty("If-None-Match",etag);
                    if(modified!=null && !modified.isEmpty())connection.setRequestProperty("If-Modified-Since",modified);
                }
                int status=connection.getResponseCode();
                if(status==304)return new Result(true,null,etag,modified);
                if(status==301 || status==302 || status==303 || status==307 || status==308) {
                    String location=connection.getHeaderField("Location");
                    if(location==null)throw new IOException("SOURCE_REDIRECT_INVALID");
                    url=new URL(validateUrl(new URL(url,location).toString())); continue;
                }
                if(status!=200)throw new IOException("SOURCE_HTTP_"+status);
                String encoding=connection.getHeaderField("Content-Encoding");
                if(encoding!=null && !encoding.equalsIgnoreCase("identity"))throw new IOException("SOURCE_ENCODING_UNSUPPORTED");
                if(connection.getContentLengthLong()>maxBytes)throw new IOException("SOURCE_TOO_LARGE");
                ByteArrayOutputStream body=new ByteArrayOutputStream();
                try(InputStream input=connection.getInputStream()) {
                    byte[] buffer=new byte[8192]; int read;
                    while((read=input.read(buffer))!=-1) {
                        if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("SOURCE_CANCELLED");
                        if(System.nanoTime()>deadline)throw new java.net.SocketTimeoutException("SOURCE_TIMEOUT");
                        if(body.size()+read>maxBytes)throw new IOException("SOURCE_TOO_LARGE");
                        body.write(buffer,0,read);
                    }
                }
                if(System.nanoTime()>deadline)throw new java.net.SocketTimeoutException("SOURCE_TIMEOUT");
                return new Result(false,body.toByteArray(),header(connection,"ETag"),header(connection,"Last-Modified"));
            } finally { connection.disconnect(); }
        }
        throw new IOException("SOURCE_TOO_MANY_REDIRECTS");
    }
    private static String header(HttpURLConnection connection,String name) throws IOException {
        String value=connection.getHeaderField(name);
        if(value!=null && (value.length()>1024 || value.contains("\r") || value.contains("\n")))throw new IOException("SOURCE_HEADER_INVALID");
        return value;
    }
}
