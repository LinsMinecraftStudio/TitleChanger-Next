package me.mmmjjkx.titlechanger.fabric;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class HttpUtils {
    private static final String HITOKOTO_API_URL = "https://v1.hitokoto.cn/";
    private static final String HITOKOTO_INTERNATIONAL_API_URL = "https://international.v1.hitokoto.cn/";
    private static final HttpClient http = HttpClient.newHttpClient();

    public static String getHikotoko(boolean international) {
        AtomicReference<String> hikotoko = new AtomicReference<>("无法获取一言，请稍后再试");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(international ? HITOKOTO_INTERNATIONAL_API_URL : HITOKOTO_API_URL))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        try {
            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAcceptAsync(body -> {
                        Map<?,?> map = new Gson().fromJson(body, Map.class);
                        String hitokoto = (String) map.get("hitokoto");
                        String from = (String) map.get("from");
                        hikotoko.set(hitokoto + " —— " + from);
                    }).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return hikotoko.get();
    }
}
