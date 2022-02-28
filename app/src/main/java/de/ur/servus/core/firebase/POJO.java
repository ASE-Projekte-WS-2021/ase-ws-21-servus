package de.ur.servus.core.firebase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface POJO<T> {

    /**
     * Returns all class fields as map. Can be used to update data in database.
     * @return Map containing field names and data.
     */
    default Map<String, Object> toFieldMap() {
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

    /**
     * Returns a list with all fields names, that should not be saved in the database.
     * @return
     */
    default String[] excludedFields(){
        return new String[]{};
    };

    T toObject();
}
