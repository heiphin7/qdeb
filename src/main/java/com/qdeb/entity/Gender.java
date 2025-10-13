package com.qdeb.entity;

public enum Gender {
    M("Male"),
    F("Female"),
    O("Other");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static Gender fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value) || 
                gender.getDisplayName().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        
        throw new IllegalArgumentException("Некорректное значение гендера: " + value + 
                ". Доступные значения: M, F, O");
    }
}
