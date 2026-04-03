package me.mmmjjkx.titlechanger.enums.formatting;

public enum Alignment {
    LEFT("[left]"),
    CENTER("[center]"),
    RIGHT("[right]");

    private final String mark;

    Alignment(String mark) {
        this.mark = mark;
    }

    public String getMark() {
        return mark;
    }
}
