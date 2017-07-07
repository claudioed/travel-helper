package flights.domain;

import lombok.Data;

/**
 * Flight
 * Created by claudio on 07/07/17.
 */
@Data
public class Flight {

    String destination;

    String departureDate;

    String returnDate;

    String price;

    String airline;

}
