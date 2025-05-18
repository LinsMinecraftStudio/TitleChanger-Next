package me.mmmjjkx.titlechanger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

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
                        Map<?,?> map = new Gson().fromJson(body, Map.class);
                        String hitokoto = (String) map.get("hitokoto");
                        String from = (String) map.get("from");
                        hikotoko.set(hitokoto + " —— " + from);
                    }).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return hikotoko.get();
    }

    @Nullable
    public static String getLastestModrinthVersion(String loader, String packId, String mcv) {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            URI uri = new URIBuilder(String.format(MODRINTH_API_URL, packId))
                    .addParameter("loaders", "[\"%s\"]".formatted(loader))
                    .addParameter("game_versions", "[\"%s\"".formatted(mcv))
                    .build();

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-Type", "application/json");

            CloseableHttpResponse rep = client.execute(request);
            String entity = EntityUtils.toString(rep.getEntity());
            System.out.println(entity);
            JsonArray list = JsonParser.parseString(entity).getAsJsonArray();

            if (list.get(0) == null) {
                return null;
            }

            JsonObject obj = list.get(0).getAsJsonObject();
            return obj.get("version_number").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
