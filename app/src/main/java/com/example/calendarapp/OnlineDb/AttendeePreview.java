package com.example.calendarapp.OnlineDb;

public class AttendeePreview {
    private String attendeeName, attendeeUid, profilePicUrl;
    private String eventId;

    public AttendeePreview(){

    }
    public AttendeePreview(String eventId, String attendeeName, String attendeeUid, String profilePicUrl) {
        this.eventId = eventId;
        this.attendeeUid = attendeeUid;
        this.attendeeName = attendeeName;
        this.profilePicUrl = profilePicUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendeeUid() {
        return attendeeUid;
    }

    public void setAttendeeUid(String attendeeUid) {
        this.attendeeUid = attendeeUid;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    @Override
    public String toString() {
        return "AttendeePreview{" +
                "attendeeName='" + attendeeName + '\'' +
                ", attendeeUid='" + attendeeUid + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", eventId='" + eventId + '\'' +
                '}';
    }
}
