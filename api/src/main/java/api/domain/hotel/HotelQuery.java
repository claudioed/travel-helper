package api.domain.hotel;

import api.domain.airport.Airport;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Hotel Query
 * Created by claudio on 07/07/17.
 */
@Data
public class HotelQuery {

  Airport airport;

  LocalDateTime checkIn;

  LocalDateTime checkOut;

  public String checkIn() {
    return this.checkIn.toString();
  }

  public String checkOut() {
    return this.checkOut.toString();
  }

}
