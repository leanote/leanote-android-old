package com.leanote.android.networking;

/**
 * Created by xiongxingxing on 16/8/30.
 */

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp backed {@link com.android.volley.toolbox.HttpStack HttpStack} that does not
 * use okhttp-urlconnection
 */
public class OkHttp3Stack implements HttpStack {

    private OkHttpClient.Builder clientBuilder;

    public OkHttp3Stack() {
        this(false);
    }

    public OkHttp3Stack(boolean allowAllCert) {
        if (allowAllCert) {
            clientBuilder = new OkHttpClient.Builder();
            allowAllSSLOkHttp(clientBuilder);
        } else {
            clientBuilder = new OkHttpClient.Builder();
        }
        clientBuilder.addNetworkInterceptor(new StethoInterceptor());
    }

    private static HttpEntity entityFromOkHttpResponse(Response response) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        ResponseBody body = response.body();

        entity.setContent(body.byteStream());
        entity.setContentLength(body.contentLength());
        entity.setContentEncoding(response.header("Content-Encoding"));

        if (body.contentType() != null) {
            entity.setContentType(body.contentType().type());
        }
        return entity;
    }

    @SuppressWarnings("deprecation")
    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder,
                                                          com.android.volley.Request<?> request)
            throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                // Ensure backwards compatibility.  Volley assumes a request with a null body is a GET.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create(MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static ProtocolVersion parseProtocol(final Protocol p) {
        switch (p) {
            case HTTP_1_0:
                return new ProtocolVersion("HTTP", 1, 0);
            case HTTP_1_1:
                return new ProtocolVersion("HTTP", 1, 1);
            case SPDY_3:
                return new ProtocolVersion("SPDY", 3, 1);
            case HTTP_2:
                return new ProtocolVersion("HTTP", 2, 0);
        }

        throw new IllegalAccessError("Unkwown protocol");
    }

    private static RequestBody createRequestBody(Request request) throws AuthFailureError {
        byte[] body = request.getBody();
        if (body == null) {
            if (request.getMethod() == Request.Method.POST) {
                body = "".getBytes();
            } else {
                return null;
            }
        }

        return RequestBody.create(MediaType.parse(request.getBodyContentType()), body);
    }

    public static void allowAllSSLOkHttp(OkHttpClient.Builder builder) {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }}, new SecureRandom());
            builder.sslSocketFactory(sc.getSocketFactory());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {

        int timeoutMs = request.getTimeoutMs();

        clientBuilder.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.writeTimeout(timeoutMs, TimeUnit.MILLISECONDS);

        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        okHttpRequestBuilder.url(request.getUrl());

        Map<String, String> headers = request.getHeaders();
        for (final String name : headers.keySet()) {
            okHttpRequestBuilder.addHeader(name, headers.get(name));
        }
        for (final String name : additionalHeaders.keySet()) {
            okHttpRequestBuilder.addHeader(name, additionalHeaders.get(name));
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request);

        OkHttpClient client = clientBuilder.build();
        okhttp3.Request okHttpRequest = okHttpRequestBuilder.build();
        Response okHttpResponse = client.newCall(okHttpRequest).execute();

        StatusLine responseStatus = new BasicStatusLine(parseProtocol(okHttpResponse.protocol()),
                okHttpResponse.code(), okHttpResponse.message());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromOkHttpResponse(okHttpResponse));

        Headers responseHeaders = okHttpResponse.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                response.addHeader(new BasicHeader(name, value));
            }
        }

        return response;
    }

}