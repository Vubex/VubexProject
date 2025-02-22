package de.flavius.vubex.vubexsecurityspigot.utils;

import java.util.Objects;
import java.util.UUID;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class UserMethodPair {
    private UUID uuid;

    private String methodName;

    public UserMethodPair(UUID uuid, String methodName) {
        this.uuid = uuid;
        this.methodName = methodName;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserMethodPair that = (UserMethodPair)o;
        return (this.uuid.equals(that.uuid) && this.methodName.equals(that.methodName));
    }

    public int hashCode() {
        return Objects.hash(new Object[] { this.uuid, this.methodName });
    }
}
