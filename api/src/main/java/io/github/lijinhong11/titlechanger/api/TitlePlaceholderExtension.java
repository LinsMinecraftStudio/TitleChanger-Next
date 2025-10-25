package io.github.lijinhong11.titlechanger.api;

import java.util.List;

public interface TitlePlaceholderExtension {
    String getPlaceholderHeader();

    String getStaticPlaceholderValue(String placeholder, String[] args);

    String getDynamicPlaceholderValue(String placeholder, String[] args);

    List<String> getPlaceholders();
}
