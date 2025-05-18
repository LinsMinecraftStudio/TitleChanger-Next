package me.mmmjjkx.titlechanger.enums;

public enum Heading {
    L1("[h1]"),
    L2("[h2]"),
    L3("[h3]"),
    NONE("");

    private final String mark;

    Heading(String mark) {
        this.mark = mark;
    }

    public String getMark() {
        return mark;
    }

    public static Heading tryGetFromString(String str) {
        if (str.startsWith("[h1]")) {
            return L1;
        } else if (str.startsWith("[h2]")) {
            return L2;
        } else if (str.startsWith("[h3]")) {
            return L3;
        } else {
            return NONE;
        }
    }
}
