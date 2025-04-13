package io.github.lijinhong11.titlechanger.api;

import java.util.List;

public interface TitlePlaceholderExtension {
    String getPlaceholderHeader();

    String getPlaceholderValue(String placeholder, String[] args);

    String getExtensionName();

    List<String> getPlaceholders();
}
