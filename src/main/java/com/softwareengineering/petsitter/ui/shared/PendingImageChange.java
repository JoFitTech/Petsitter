package com.softwareengineering.petsitter.ui.shared;

public record PendingImageChange(Type type, byte[] content) {

    public enum Type {
        UNCHANGED,
        REPLACE,
        REMOVE
    }

    public static PendingImageChange unchanged() {
        return new PendingImageChange(Type.UNCHANGED, null);
    }

    public static PendingImageChange replace(byte[] content) {
        return new PendingImageChange(Type.REPLACE, content);
    }

    public static PendingImageChange remove() {
        return new PendingImageChange(Type.REMOVE, null);
    }
}
