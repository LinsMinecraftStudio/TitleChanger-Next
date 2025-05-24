package me.mmmjjkx.titlechanger;

import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;
import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.texts.RopeImplString;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TitleProcessor {
    private static final Pattern placeholderPattern = Pattern.compile("%(?:(\\w+)_)?(\\w+)(?::([^%]*))?%");

    private Map<String, TitlePlaceholderExtension> extensions;

    private final Map<String, List<TemplatePart>> templateCache = new ConcurrentHashMap<>();

    private ScheduledExecutorService executor;

    public TitleProcessor() {
        this.executor = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "TitleChanger-Processor");
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                });
    }

    public String firstParse(String template) {
        if (extensions == null) {
            this.extensions = new ConcurrentHashMap<>(TitleExtensionSource.getExtensions()
                    .stream()
                    .collect(Collectors.toMap(TitlePlaceholderExtension::getPlaceholderHeader, e -> e)));
        }

        try {
            return processTemplate(parseTemplate(template));
        } catch (Exception e) {
            System.err.println("Error processing template: " + e.getMessage());
            return template.replaceAll("%.*?%", "ERROR");
        }
    }

    public void startProcessing(String template, long intervalMs, Consumer<String> resultConsumer) {
        List<TemplatePart> parts = parseTemplate(template);

        if (intervalMs < 0) {
            resultConsumer.accept(template);
            return;
        }

        executor.scheduleAtFixedRate(() -> {
            if (Minecraft.getInstance().getWindow().isFullscreen()) {
                return;
            }

            try {
                String result = processTemplate(parts);
                resultConsumer.accept(result);
            } catch (Exception e) {
                System.err.println("Error processing template: " + e.getMessage());
                resultConsumer.accept(template.replaceAll("%.*?%", "ERROR"));
            }
        }, 10, intervalMs, TimeUnit.MILLISECONDS);
    }

    private List<TemplatePart> parseTemplate(String template) {
        return templateCache.computeIfAbsent(template, t -> {
            List<TemplatePart> parts = new ArrayList<>();
            Matcher matcher = placeholderPattern.matcher(template);
            int lastEnd = 0;

            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    parts.add(new TextPart(template.substring(lastEnd, matcher.start())));
                }

                String header = matcher.group(1);
                String placeholder = matcher.group(2);
                String[] args = matcher.group(3) != null ?
                        matcher.group(3).split(",") : new String[0];

                parts.add(new PlaceholderPart(header, placeholder, args));
                lastEnd = matcher.end();
            }

            if (lastEnd < template.length()) {
                parts.add(new TextPart(template.substring(lastEnd)));
            }

            return Collections.unmodifiableList(parts);
        });
    }

    private String processTemplate(List<TemplatePart> parts) {
        RopeImplString result = new RopeImplString("");

        for (TemplatePart part : parts) {
            if (part instanceof TextPart tp) {
                result.concat(new RopeImplString(tp.text));
            } else if (part instanceof PlaceholderPart ph) {
                String value = resolvePlaceholder(ph.header, ph.placeholder, ph.args);
                result.concat(new RopeImplString(value));
            }
        }

        return result.toString();
    }

    private String resolvePlaceholder(String header, String placeholder, String[] args) {
        for (TitlePlaceholderExtension ext : extensions.values()) {
            if (header != null && ext.getPlaceholderHeader().equalsIgnoreCase(header)) {
                if (ext.getPlaceholders().contains(placeholder.toLowerCase())) {
                    return ext.getPlaceholderValue(placeholder, args);
                }
            } else if (header == null && ext.getPlaceholders().contains(placeholder.toLowerCase())) {
                return ext.getPlaceholderValue(placeholder, args);
            }
        }

        return "%" + (header != null ? header + "_" : "") + placeholder +
                (args.length > 0 ? ":" + String.join(",", args) : "") + "%";
    }

    public void shutdown() {
        executor.shutdown();
        executor = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "TitleChanger-Processor");
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                });
    }

    private interface TemplatePart {}

    private record TextPart(String text) implements TemplatePart { }

    /**
     * @param header maybe null
     */
    private record PlaceholderPart(String header, String placeholder, String[] args) implements TemplatePart { }
}