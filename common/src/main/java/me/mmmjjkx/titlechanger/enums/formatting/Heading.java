package me.mmmjjkx.titlechanger.enums.formatting;

public enum Heading {
    L1("[h1]"),
    L2("[h2]"),
    L3("[h3]"),
    NONE("");

    private final String mark;

    Heading(String mark) {
        this.mark = mark;
    }

    public static Heading tryGetFromString(String str) {
        if (str.startsWith(L1.mark)) {
            return L1;
        } else if (str.startsWith(L2.mark)) {
            return L2;
        } else if (str.startsWith(L3.mark)) {
            return L3;
        } else {
            return NONE;
        }
    }

    public String getMark() {
        return mark;
    }
}
