package com.amikom.bumdesma.api;

import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.UserAgentInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit instance = null;

    public static Retrofit getInstance() {
        if (instance == null) {

            // Logging — bisa lihat request/response di Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    // Menyamarkan User-Agent sebagai browser desktop.
                    // Ditaruh SEBELUM logging supaya Logcat menampilkan
                    // header final yang benar-benar dikirim ke server.
                    .addInterceptor(new UserAgentInterceptor())
                    // Skip halaman interstitial warning ngrok free plan
                    // (kalau tidak ditambahkan, response yang balik HTML,
                    // bukan JSON -> menyebabkan MalformedJsonException)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request requestWithHeader = original.newBuilder()
                                .header("ngrok-skip-browser-warning", "true")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(requestWithHeader);
                    })
                    .addInterceptor(logging)
                    // Timeout diperpanjang untuk toleransi jaringan seluler lambat/tidak stabil
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    // Otomatis coba ulang sekali kalau koneksi gagal (mis. timeout, connection reset)
                    .retryOnConnectionFailure(true)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    // Shortcut langsung dapat ApiService
    public static ApiService getService() {
        return getInstance().create(ApiService.class);
    }
}