package com.example.calendarapp.EventObjects;

import com.example.calendarapp.EventObjects.Event;
import com.example.calendarapp.EventObjects.EventGenre;
import com.example.calendarapp.EventObjects.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;


//                  Public Event Object
public class PublicEvent extends Event {

    private String eventId;
    private String price;
    private String link;
    private EventGenre eventGenre;
    private String bannerUrl;
    private ArrayList<String> imageUrls;
    private ArrayList<String> tags;
    private ArrayList<String> attendees;


    public PublicEvent() {
        super();
    }



    public PublicEvent(String title, String eventId, String uid, String location, Date date, LocalDateTime startTime, LocalDateTime endTime, EventType eventType, EventGenre eventGenre, String description, String price, String link, String bannerUrl, ArrayList<String> imageUrls, ArrayList<String> tags, ArrayList<String> attendees) {
        super(title, eventId, uid, location, date, startTime, endTime, eventType, description);
        this.eventGenre = eventGenre;
        this.price = price;
        this.link = link;
        this.bannerUrl = bannerUrl;
        this.imageUrls = imageUrls;
        this.tags = tags;
        this.attendees = attendees;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<String> attendees) {
        this.attendees = attendees;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    public EventGenre getEventGenre() {
        return eventGenre;
    }

    public void setEventGenre(EventGenre eventGenre) {
        this.eventGenre = eventGenre;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return super.toString() + "PublicEvent{" +
                "price='" + price + '\'' +
                ", link='" + link + '\'' +
                ", eventGenre=" + eventGenre +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
