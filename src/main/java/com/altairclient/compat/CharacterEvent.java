package com.altairclient.compat;

public record CharacterEvent(char chr) {
    public String codepointAsString() {
        return String.valueOf(chr);
    }
}
