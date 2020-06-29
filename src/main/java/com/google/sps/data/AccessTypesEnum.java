package com.google.sps.data;

public enum AccessTypesEnum {
    NARROW_READ("Narrow Read"),
    NARROW_WRITE("Narrow Write"),
    WIDE_READ("Wide Read"),
    WIDE_WRITE("Wide Write");

    private final String text;

    AccessTypesEnum(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}