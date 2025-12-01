package jnb.fitness;

import android.content.Context;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

public class ApiClient {
    private final Context ctx;
    private final SessionManager session;

    public ApiClient(Context ctx) {
        this.ctx = ctx;
        this.session = new SessionManager(ctx);
    }

    private String read(HttpURLConnection conn) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }
    private String readError(HttpURLConnection conn) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    public JSONObject post(String path, JSONObject body) throws Exception {
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        byte[] bytes = body.toString().getBytes("UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "POST " + url + " => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public String getAuthText(String path) throws Exception {
        String primary = UrlConfig.getApiBaseUrl(ctx);
        String alt = primary.equals(UrlConfig.getDebugApiBaseUrl(ctx))
                ? UrlConfig.getDeviceApiBaseUrl(ctx)
                : UrlConfig.getDebugApiBaseUrl(ctx);
        try {
            URL url = new URL(primary + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String token = session.getToken();
            if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
            int code = conn.getResponseCode();
            String s = code >= 200 && code < 300 ? read(conn) : readError(conn);
            Log.d("API", "GET " + url + " => " + code + " | " + s);
            conn.disconnect();
            if (code >= 200 && code < 300) return s;
            // Fallback if non-2xx
        } catch(Exception ignored) {}
        URL url2 = new URL(alt + path);
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        conn2.setConnectTimeout(8000);
        conn2.setReadTimeout(15000);
        conn2.setRequestMethod("GET");
        conn2.setRequestProperty("Accept", "application/json");
        String token2 = session.getToken();
        if (token2 != null) conn2.setRequestProperty("Authorization", "Bearer " + token2);
        int code2 = conn2.getResponseCode();
        String s2 = code2 >= 200 && code2 < 300 ? read(conn2) : readError(conn2);
        Log.d("API", "GET " + url2 + " => " + code2 + " | " + s2);
        conn2.disconnect();
        return s2;
    }

    public JSONArray getAuthArray(String path) throws Exception {
        return new JSONArray(getAuthText(path));
    }

    public JSONObject getAuthJson(String path) throws Exception {
        return new JSONObject(getAuthText(path));
    }

    public JSONObject putAuthJson(String path, JSONObject body) throws Exception {
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        byte[] bytes = body.toString().getBytes("UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "PUT " + url + " => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public JSONObject postAuthJson(String path, JSONObject body) throws Exception {
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        byte[] bytes = body.toString().getBytes("UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "POST " + url + " => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public JSONObject postAuthRaw(String path, String raw) throws Exception {
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        byte[] bytes = raw.getBytes("UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "POST " + url + " => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s != null && !s.isEmpty() ? s : "{}");
    }

    public JSONObject deleteAuth(String path) throws Exception {
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "DELETE " + url + " => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public JSONObject putAuthMultipart(String path, String fieldName, String fileName, byte[] fileBytes, String mimeType) throws Exception {
        String boundary = "----JNBBoundary" + System.currentTimeMillis();
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + (mimeType != null ? mimeType : "application/octet-stream") + "\r\n\r\n";
        os.write(header.getBytes("UTF-8"));
        os.write(fileBytes);
        String footer = "\r\n--" + boundary + "--\r\n";
        os.write(footer.getBytes("UTF-8"));
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "PUT " + url + " [multipart] => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public JSONObject postAuthMultipart(String path, java.util.Map<String,String> fields, String fileFieldName, String fileName, byte[] fileBytes, String mimeType) throws Exception {
        String boundary = "----JNBBoundary" + System.currentTimeMillis();
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        if (fields != null) {
            for (java.util.Map.Entry<String,String> e : fields.entrySet()) {
                String value = e.getValue() != null ? e.getValue() : "";
                String part = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n" +
                        "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                        value + "\r\n";
                os.write(part.getBytes("UTF-8"));
            }
        }
        if (fileBytes != null && fileBytes.length > 0 && fileFieldName != null) {
            String header = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + (fileName!=null?fileName:"file") + "\"\r\n" +
                    "Content-Type: " + (mimeType != null ? mimeType : "application/octet-stream") + "\r\n\r\n";
            os.write(header.getBytes("UTF-8"));
            os.write(fileBytes);
            os.write("\r\n".getBytes("UTF-8"));
        }
        String footer = "--" + boundary + "--\r\n";
        os.write(footer.getBytes("UTF-8"));
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "POST " + url + " [multipart] => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }

    public JSONObject putAuthMultipartFields(String path, java.util.Map<String,String> fields, String fileFieldName, String fileName, byte[] fileBytes, String mimeType) throws Exception {
        String boundary = "----JNBBoundary" + System.currentTimeMillis();
        String base = UrlConfig.getApiBaseUrl(ctx);
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        String token = session.getToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        if (fields != null) {
            for (java.util.Map.Entry<String,String> e : fields.entrySet()) {
                String part = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n\r\n" +
                        (e.getValue() != null ? e.getValue() : "") + "\r\n";
                os.write(part.getBytes("UTF-8"));
            }
        }
        if (fileBytes != null && fileBytes.length > 0 && fileFieldName != null) {
            String header = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + (fileName!=null?fileName:"file") + "\"\r\n" +
                    "Content-Type: " + (mimeType != null ? mimeType : "application/octet-stream") + "\r\n\r\n";
            os.write(header.getBytes("UTF-8"));
            os.write(fileBytes);
            os.write("\r\n".getBytes("UTF-8"));
        }
        String footer = "--" + boundary + "--\r\n";
        os.write(footer.getBytes("UTF-8"));
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        String s = code >= 200 && code < 300 ? read(conn) : new BufferedReader(new InputStreamReader(conn.getErrorStream())).readLine();
        Log.d("API", "PUT " + url + " [multipart] => " + code + " | " + s);
        conn.disconnect();
        return new JSONObject(s);
    }
}
