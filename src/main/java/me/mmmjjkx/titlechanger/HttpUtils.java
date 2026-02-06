package me.mmmjjkx.titlechanger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class HttpUtils {
    private static final String HITOKOTO_API_URL = "https://v1.hitokoto.cn/";
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/%s/version";
    private static final HttpClient http = HttpClient.newHttpClient();

    public static String getHikotoko(String defaultValue) {
        AtomicReference<String> hikotoko = new AtomicReference<>(defaultValue);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HITOKOTO_API_URL))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .build();
        try {
            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAcceptAsync(body -> {
                        Map<?, ?> map = new Gson().fromJson(body, Map.class);
                        String hitokoto = (String) map.get("hitokoto");
                        String from = (String) map.get("from");
                        hikotoko.set(hitokoto + " —— " + from);
                    }).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return hikotoko.get();
    }

    @Nullable
    public static String getLatestModrinthVersion(String loader, String packId, String mcv) {
        try {
            String loaders = "[\"" + loader + "\"]";
            String gameVersions = "[\"" + mcv + "\"]";

            URI uri = new URI(
                    String.format(MODRINTH_API_URL, packId)
                            + "?loaders=" + URLEncoder.encode(loaders, StandardCharsets.UTF_8)
                            + "&game_versions=" + URLEncoder.encode(gameVersions, StandardCharsets.UTF_8)
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            JsonArray list = JsonParser.parseString(body).getAsJsonArray();
            if (list.isEmpty()) {
                return null;
            }

            JsonObject obj = list.get(0).getAsJsonObject();
            return obj.get("version_number").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
