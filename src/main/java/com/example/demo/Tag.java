package com.example.demo;

/**
 * Contains static values, which represents different xml tags
 */
public enum Tag {
    PRODUCT("Product"),
    NAME("Name"),
    ACTIVE("Active");
    private final String value;

    Tag(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
