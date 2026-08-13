// Sesuaikan package name dengan lokasi file ini di project kamu
package com.amikom.bumdesma.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor untuk mengganti User-Agent request agar terlihat seperti
 * browser desktop biasa, bukan HTTP client bawaan Android.
 *
 * Ini HANYA membantu jika proteksi hosting cuma mengecek header User-Agent.
 * Jika proteksinya berbasis JavaScript challenge (misalnya Cloudflare-style
 * "Checking your browser..." atau captcha), interceptor ini TIDAK akan
 * membantu, karena butuh eksekusi JS yang tidak bisa dilakukan HTTP client.
 */
public class UserAgentInterceptor implements Interceptor {

    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request newRequest = originalRequest.newBuilder()
                .header("User-Agent", DESKTOP_USER_AGENT)
                .build();
        return chain.proceed(newRequest);
    }
}