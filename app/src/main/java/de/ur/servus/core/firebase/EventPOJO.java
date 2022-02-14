package de.ur.servus.core.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.ur.servus.core.Event;

/**
 * This is a POJO version of the Event class and is only used to convert database data to Event objects.
 */
class EventPOJO {
    private String name;
    private String description;
    private String id;
    private List<Double> location;
    private int attendants;

    public EventPOJO() {
    }

    public EventPOJO(Event event) {
        this.name = event.getName();
        this.description = event.getDescription();
        this.id = event.getId();
        this.attendants = event.getAttendants();

        this.location = new ArrayList<>(2);
        this.location.add(event.getLocation().latitude);
        this.location.add(event.getLocation().longitude);
    }

    /**
     * Returns all class fields as map. Can be used to update data in database.
     * @return Map containing field names and data.
     */
    public Map<String, Object> toMap() {
        Field[] fields = this.getClass().getDeclaredFields();
        return Arrays.stream(fields).collect(Collectors.toMap(
                Field::getName,
                field -> {
                    try {
                        field.setAccessible(true);
                        var result = field.get(this);
                        return Objects.requireNonNull(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
        ));
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

    public void setAttendants(int attendants) {
        this.attendants = attendants;
    }

    public int getAttendants() {
        return attendants;
    }

    public Event toEvent() {
        Event event = new Event(
                this.name,
                this.description,
                new LatLng(this.location.get(0), this.location.get(1)),
                this.attendants
        );
        event.setId(this.id);
        return event;
    }
}
