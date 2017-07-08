package hotels.domain;

import lombok.Data;

import java.time.LocalDateTime;

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
