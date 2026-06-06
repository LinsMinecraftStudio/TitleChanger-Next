package me.mmmjjkx.titlechanger;

import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;
import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.enums.TriState;
import me.mmmjjkx.titlechanger.texts.RopeImplString;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleProcessor {
    private static final Pattern placeholderPattern = Pattern.compile("%(?:(\\w+)_)?(\\w+)(?::([^%]*))?%");

    private final List<TitlePlaceholderExtension> extensions;

    private ScheduledExecutorService executor;

    private volatile String rawParse = "";

    private Function<String, String> postProcessor = Function.identity();

    public TitleProcessor() {
        this.executor = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "TitleChanger-Processor");
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                });
        this.extensions = TitleExtensionSource.getExtensions();
    }

    public void refresh(String template) {
        rawParse = processTemplate(parseTemplate(template), TriState.TRUE);
    }

    public String firstParse(String template) {
        refresh(template);

        try {
            List<TemplatePart> list = parseTemplate(rawParse);
            return postProcessor.apply(processTemplate(list, TriState.DEFAULT));
        } catch (Exception e) {
            System.err.println("Error processing template: " + e.getMessage());
            return template;
        }
    }

    public String firstParseNoCache(String template) {
        try {
            List<TemplatePart> list = parseTemplate(template);
            return postProcessor.apply(processTemplate(list, TriState.DEFAULT));
        } catch (Exception e) {
            System.err.println("Error processing template: " + e.getMessage());
            return template;
        }
    }

    public void startProcessing(long intervalMs, Consumer<String> resultConsumer) {
        List<TemplatePart> parts = parseTemplate(rawParse);

        Consumer<String> wrappedConsumer = s -> resultConsumer.accept(postProcessor.apply(s));

        if (intervalMs < 0) {
            try {
                wrappedConsumer.accept(processTemplate(parts, TriState.FALSE));
            } catch (Exception e) {
                resultConsumer.accept(rawParse);
            }
            return;
        }

        executor.scheduleAtFixedRate(() -> {
            if (Minecraft.getInstance().getWindow().isFullscreen()) {
                return;
            }

            try {
                String result = processTemplate(parts, TriState.FALSE);
                wrappedConsumer.accept(result);
            } catch (Exception e) {
                System.err.println("Error processing template: " + e.getMessage());
                try {
                    wrappedConsumer.accept(rawParse);
                } catch (Exception e2) {
                    resultConsumer.accept(rawParse);
                }
            }
        }, 100, intervalMs, TimeUnit.MILLISECONDS);
    }

    private List<TemplatePart> parseTemplate(String template) {
        List<TemplatePart> parts = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                parts.add(new TextPart(template.substring(lastEnd, matcher.start())));
            }

            String header = matcher.group(1);
            String placeholder = matcher.group(2);
            String[] args = matcher.group(3) != null ? matcher.group(3).split(",") : new String[0];

            parts.add(new PlaceholderPart(header, placeholder, args));
            lastEnd = matcher.end();
        }

        if (lastEnd < template.length()) {
            parts.add(new TextPart(template.substring(lastEnd)));
        }

        return Collections.unmodifiableList(parts);
    }

    private String processTemplate(List<TemplatePart> parts, TriState staticPlaceholders) {
        RopeImplString result = new RopeImplString("");

        for (TemplatePart part : parts) {
            if (part instanceof TextPart(String text)) {
                result.concat(new RopeImplString(text));
            } else if (part instanceof PlaceholderPart ph) {
                String header = ph.header();
                String placeholder = ph.placeholder();
                String[] args = ph.args();
                String value = switch (staticPlaceholders) {
                    case TRUE -> {
                        String v = resolveStaticPlaceholder(header, placeholder, args);
                        yield Constants.NO_RESULT.equals(v) ? ph.toString() : v;
                    }
                    case FALSE -> {
                        String v = resolveDynamicPlaceholder(header, placeholder, args);
                        yield Constants.NO_RESULT.equals(v) ? ph.toString() : v;
                    }
                    case DEFAULT -> {
                        String v = resolveStaticPlaceholder(header, placeholder, args);
                        if (Constants.NO_RESULT.equals(v)) {
                            v = resolveDynamicPlaceholder(header, placeholder, args);
                        }

                        if (Constants.NO_RESULT.equals(v)) {
                            yield ph.toString();
                        } else {
                            yield v;
                        }
                    }
                };

                result.concat(new RopeImplString(value));
            }
        }

        return result.toString();
    }

    private String resolveDynamicPlaceholder(String header, String placeholder, String[] args) {
        for (TitlePlaceholderExtension ext : extensions) {
            String value = ext.getDynamicPlaceholderValue(placeholder, args);

            if (Constants.NO_RESULT.equals(value)) {
                continue;
            }

            if (!ext.getPlaceholders().contains(placeholder)) {
                continue;
            }

            if ((header != null && ext.getPlaceholderHeader().equalsIgnoreCase(header))
                    || (isStringNullOrBlank(header) && isStringNullOrBlank(ext.getPlaceholderHeader()))) {
                return value;
            }
        }

        return Constants.NO_RESULT;
    }

    private String resolveStaticPlaceholder(String header, String placeholder, String[] args) {
        for (TitlePlaceholderExtension ext : extensions) {
            String value = ext.getStaticPlaceholderValue(placeholder, args);

            if (Constants.NO_RESULT.equals(value)) {
                continue;
            }

            if (!ext.getPlaceholders().contains(placeholder)) {
                continue;
            }

            if ((header != null && ext.getPlaceholderHeader().equalsIgnoreCase(header))
                    || (isStringNullOrBlank(header) && isStringNullOrBlank(ext.getPlaceholderHeader()))) {
                return value;
            }
        }

        return Constants.NO_RESULT;
    }

    public void setPostProcessor(Function<String, String> postProcessor) {
        this.postProcessor = postProcessor != null ? postProcessor : Function.identity();
    }

    public void restart() {
        executor.shutdownNow();
        executor = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "TitleChanger-Processor");
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                });
    }

    private boolean isStringNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    private interface TemplatePart {
    }

    private record TextPart(String text) implements TemplatePart {
    }

    private record PlaceholderPart(String header, String placeholder, String[] args) implements TemplatePart {
        @Override
        public @NotNull String toString() {
            return "%" + (header != null ? header + "_" : "") + placeholder +
                    (args.length > 0 ? ":" + String.join(",", args) : "") + "%";
        }
    }
}