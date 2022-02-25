package de.ur.servus.core.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;

/**
 * This is a POJO version of the Event class and is only used to convert database data to Event objects.
 */
class EventPOJO implements POJO<Event>{
    private String name;
    private String description;
    private String id;
    private String genre;
    private List<Double> location;
    private List<AttendantPOJO> attendants;

    public EventPOJO() {
    }

    public EventPOJO(Event event) {
        this.name = event.getName();
        this.description = event.getDescription();
        this.id = event.getId();
        this.attendants = event.getAttendants().stream().map(AttendantPOJO::new).collect(Collectors.toList());
        this.genre = event.getGenre();

        this.location = new ArrayList<>(2);
        this.location.add(event.getLocation().latitude);
        this.location.add(event.getLocation().longitude);
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> latlng) {
        this.location = latlng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AttendantPOJO> getAttendants() {
        return attendants;
    }

    public void setAttendants(List<AttendantPOJO> attendants) {
        this.attendants = attendants;
    }

    public void setGenre(String genre){this.genre = genre;}

    public String getGenre(){return genre;}


    @Override
    public String[] excludedFields() {
        return new String[]{
                "id"
        };
    }

    @Override
    public Event toObject() {
        Event event = new Event(
                this.name,
                this.description,
                new LatLng(this.location.get(0), this.location.get(1)),
                this.attendants.stream().map(pojo -> new Attendant(pojo.getUserId(), pojo.isCreator())).collect(Collectors.toList()),
                this.genre
        );
        event.setId(this.id);
        return event;
    }
}
