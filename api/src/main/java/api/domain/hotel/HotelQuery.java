package api.domain.hotel;

import api.domain.airport.Airport;
import lombok.Data;

/**
 * Hotel Query
 * Created by claudio on 07/07/17.
 */
@Data
public class HotelQuery {

  Airport airport;

  String checkIn;

  String checkOut;

}
