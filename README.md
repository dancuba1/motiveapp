# :calendar:MOTIVE:thinking:
## What is Motive?
### Motive is an Event Sharing and Organisation Planning Native Android Application which allows users to create events, share said events with their friends, and add them to their in-app calendar, where they can additionally add their own private events.</br>
---
### Home Screen  

<img src="media/image21.png" alt="Home Page(Following)" width="200" height="400"> <img src="media/image22.png" alt="Home Page(Following)" width="200" height="400">  

The Home Screen offers two tabs:  
- **Discover**: All public events from all users are shown in order of upload date.  
- **Following**: Events from users you follow are shown in order of upload date.

---


### In-App Calendar
<img src="media/image44.png" alt="Calendar Page" width="200" height="400">
Each day is clickable, directing the user to the corresponding private events page for the day clicked.</br> Events are also shown as a preview on the month view. </br>

---

### Private Events

<img src="media/image47.png" alt="Day View Page" width="200" height="400"> <img src="media/image48.png" alt="Create Private Event Page" width="200" height="400"> <img src="media/image50.png" alt="Create Private Event Page (Filled)" width="200" height="400"> <img src="media/image46.png" alt="Delete Private Event" width="200" height="400"> 

Users can create private events directly, which are saved locally on their device and do not require any internet connection to access.  
Users can create custom events, and they are color-coordinated to the event type (e.g., Work - Blue).  
Public events are also shown here and can be deleted, which will remove your attendance from said event.  

---

### Public Events

Public Events can be accessed through clicking on them in the home page, or on a profile page.  

<img src="media/image24.png" alt="Public Event Page" width="200" height="400"> <img src="media/image23.png" alt="Public Event Page (Add to calendar/save)" width="200" height="400"> </br>

The Public Event page offers an image, title, location, price and external link for the event. The uploader's profile picture is visible which directs to their profile page. </br>
Users can save an event, publically and/or privately, this will determine whether other users that follow them can view their attendance.

---
#### :point_right:	Calendar Preview

At the bottom of the page is the calendar preview, which displays the calendar of the user, from 2 days prior to the event to 2 days after (Horizontally scrollable).  

---

#### :point_right:	Attendance View

<img src="media/image29.png" alt="Public Event Page (with multiple attendees)" width="200" height="400">  
Additionally, users can view whether the other users that they follow are attending the event.  

***

Both calendar preview and attendance view offer the user the ability to make an informed decision of their attendance, and whether they wish to publically display that.  

---

### Search  

<img src="media/image36.png" alt="Search Page" width="200" height="400"><img src="media/image43.png" alt="Search Page (Advanced Search)" width="200" height="400">  <img src="media/image37.png" alt="Search Page (text tag search example)" width="200" height="400">  <img src="media/image35.png" alt="Search Page (standard search example)" width="200" height="400">  

The Search page grants 2 functions:
- **Text Search**: Users search across all public events, using Algolia Search API, which allows for searching across all attributes of event (e.g. tag, location, etc).  
- **Advanced Search**: Search parameters can be set such as price, date, tags and event genre.  

---

### Profiles  

Profiles can either be accessed via the profile tab (the user's own profile) or clicking on other user's profile pictures.  

---


#### Own Profile  

<img src="media/image57.png" alt="Home Page" width="200" height="400"> <img src="media/image58.png" alt="Profile Page (Own)" width="200" height="400">

Users can view their event's that they have created or the events they have set to publically attend. Additionally clicking on their followers or following will display the correspinding users.

---

#### Other User's Profile  

<img src="media/image56.png" alt="Profile Page (Other)" width="200" height="400">

Users can follow other users on this page and view all their public events.  

---

Lastly, users can click add event at the bottom of the screen to create their own public event.  

---


### Public Event Creation  

<img src="media/image53.png" alt="Create Public Event Page (empty 1)" width="200" height="400"><img src="media/image51.png" alt="Create Public Event Page (empty 2)" width="200" height="400">  

<img src="media/image52.png" alt="Create Public Event Page (filled 1)" width="200" height="400"><img src="media/image54.png" alt="Create Public Event Page (filled 2)" width="200" height="400">

Users create a public event, including a banner image, title, location, description, link, price, tags, date, time, genre and type (for calendar colour correspondance).

---
