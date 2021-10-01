package tw.bill.java101.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by bill33 on 2016/3/28.
 */
public enum Hobby {
    READING("READING"),
    SPORT("SPORT"),
    MOVIE("MOVIE");

    public static final Hobby[] ALL = {READING, SPORT, MOVIE};

    private final String name;

    public static Hobby forName(final String name) {
        switch (name) {
            case "READING":
                return READING;
            case "SPORT":
                return SPORT;
            case "MOVIE":
                return MOVIE;
            default:
                throw new IllegalArgumentException("Name not define");
        }
    }


    private Hobby(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

