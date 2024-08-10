package com.example.calendarapp.EventObjects;


//      Enum for type of events
public enum EventType {
    SOCIAL(0, "Social"),
    WORK(1, "Work"),
    PERSONAL(2, "Personal");
    private final int value;

    private final String displayName;



    EventType(int value, String displayName) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }
    public int getValue() {
        return value;
    }
}
