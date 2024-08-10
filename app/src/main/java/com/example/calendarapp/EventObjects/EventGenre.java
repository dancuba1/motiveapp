package com.example.calendarapp.EventObjects;


//Enum for the event genre
public enum EventGenre {
    LIVEMUSIC(0, "Live Music"),
    BARPUB(1, "Bar & Pub"),
    BOOKSPOETRY(2, "Books & Poetry"),
    EDUCATION(3, "Education"),
    CLUBNIGHT(4, "Club Night"),
    COMEDY(5, "Comedy"),
    CONFERENCE(6, "Conference"),
    CLASS(7, "Class"),
    EXHIBITION(8, "Exhibition"),
    FESTIVAL(9, "Festival"),
    FOODDRINK(10, "Food & Drink"),
    FUNDRAISINGCHARITY(11, "Fundraising & Charity"),
    HEALTHFITNESS(12, "Health & Fitness"),
    SPORT(13, "Sport"),
    THEATREDRAMA(14, "Theatre & Drama"),
    OTHER(15, "Other");

    private final int value;
    private final String displayName;

    EventGenre(int value, String displayName){
        this.value = value;
        this.displayName = displayName;
    }

    public static EventGenre fromValue(int value) {
        for (EventGenre genre : values()) {
            if (genre.getValue() == value) {
                return genre;
            }
        }
        throw new IllegalArgumentException("No genre with value " + value + " found");
    }
    public int getValue(){
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }


}

