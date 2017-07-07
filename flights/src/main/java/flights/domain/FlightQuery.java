package flights.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Location Query
 * Created by claudio on 07/07/17.
 */
@Data
public class FlightQuery {

    Airport origin;

    Airport destination;

    LocalDateTime departureAt;

    Integer days;

    public String departure(){
        return this.departureAt.toString();
    }

}
