package com.example.physiocare.utils;

import com.example.physiocare.models.auth.AuthResponse;
import com.example.physiocare.models.auth.LoginRequest;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class ServiceUtils {
    private static String token = null;
    private static String rol = null;
    private static String id = null;
    private static String login = null;
    public static final String SERVER = "http://bellon.shop:8080";

    public static String getRol() {
        return rol;
    }

    public static String getId() {
        return id;
    }

    public static String getLogin() {
        return login;
    }

    public static void setToken(String token) {
        ServiceUtils.token = token;
    }

    public static void removeToken() {
        ServiceUtils.token = null;
    }

    public static boolean login(String username, String password) {
        try {
            String credentials = new Gson().toJson(new LoginRequest(username, password));
            String jsonResponse = getResponse(SERVER.replace("/api", "") +
                    "/auth/login", credentials, "POST");

            AuthResponse authResponse = new Gson().fromJson(jsonResponse, AuthResponse.class);
            if (authResponse != null && authResponse.isOk()) {
                setToken(authResponse.getToken());
                ServiceUtils.rol = authResponse.getRol();
                ServiceUtils.login = authResponse.getLogin();
                if(authResponse.getId() != null){
                    ServiceUtils.id = authResponse.getId();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getCharset(String contentType) {
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                return param.split("=", 2)[1];
            }
        }

        return null;
    }

    public static String getResponse(String url, String data, String method) {
        BufferedReader bufInput = null;
        StringJoiner result = new StringJoiner("\n");
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(method);

            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");

            if(token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            if (data != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }

            String charset = getCharset(conn.getHeaderField("Content-Type"));

            if (charset != null) {
                InputStream input = conn.getInputStream();
                if ("gzip".equals(conn.getContentEncoding())) {
                    input = new GZIPInputStream(input);
                }

                bufInput = new BufferedReader(
                        new InputStreamReader(input));

                String line;
                while((line = bufInput.readLine()) != null) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufInput != null) {
                try {
                    bufInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result.toString();
    }

    public static String getResponse2(String url, String data, String method) {
        BufferedReader bufInput = null;
        StringJoiner result = new StringJoiner("\n");
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(method);

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "JavaFX-Client");

            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            if (data != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(data.getBytes());
                    wr.flush();
                }
            }

            int status = conn.getResponseCode();
            if (status == 401) {
                throw new IOException("Access denied. Token might be expired.");
            }

            InputStream input = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            if ("gzip".equals(conn.getContentEncoding())) {
                input = new GZIPInputStream(input);
            }

            bufInput = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = bufInput.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufInput != null) bufInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static CompletableFuture<String> getResponseAsync(String url, String data, String method) {
        return CompletableFuture.supplyAsync(() -> getResponse(url, data, method));
    }

    public static CompletableFuture<Boolean> loginAsync(String username, String password) {
        return CompletableFuture.supplyAsync(() -> login(username, password));
    }
}