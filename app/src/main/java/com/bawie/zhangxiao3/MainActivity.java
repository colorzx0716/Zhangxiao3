package com.bawie.zhangxiao3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // loadData();//信任所有https的请求：第一种实现
         cardData();//带证书验证
    }

    /**
     * 带证书验证
     */
    private void cardData() {

        FormBody formbody = new FormBody.Builder().add("mobile", "13717830672").add("password", "123456").build();
        Request request = new Request.Builder().url("https://www.zhaoapi.cn/user/login").post(formbody).build();

        //带证书验证里的方法
        setCard().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response != null && response.body() != null){
                    final String result;
                    try {
                        result = response.body().string();
                        Gson gson = new Gson();
                        Bean bean = gson.fromJson(result, Bean.class);
                        Bean.DataBean data = bean.getData();
                        String mobile = data.getMobile();
                        System.out.println("登录的手机号mobile = " + mobile);


                    }catch (Exception e){

                    }
                }
            }
        });

    }

    //带证书验证里的方法
    public OkHttpClient setCard() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(getAssets().open("zhaoapi_server.cer")));//拷贝好的证书
            SSLContext sslContext = SSLContext.getInstance("TLS");
            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.addInterceptor(new LogInterceptor());//拦截器
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }


    /**
     * 信任所有https的请求：第一种实现
     */
    private void loadData() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor())//拦截器
                .sslSocketFactory(createSSLSocketFactory()).hostnameVerifier(new TrustAllHostnameVerifier())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        FormBody formbody = new FormBody.Builder().add("mobile", "13717830672").add("password", "123456").build();
        Request request = new Request.Builder().url("https://120.27.23.105/user/login").post(formbody).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response != null && response.body() != null){
                    final String result;
                    try {
                        result = response.body().string();
                        Gson gson = new Gson();
                        Bean bean = gson.fromJson(result, Bean.class);
                        Bean.DataBean data = bean.getData();
                        String mobile = data.getMobile();
                        System.out.println("登录的手机号mobile = " + mobile);


                    }catch (Exception e){

                    }
                }
            }
        });
    }

    //信任所有https的请求的方法
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    //信任所有https的请求的方法
    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    //信任所有https的请求的方法
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }







}
