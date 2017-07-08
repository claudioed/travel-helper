package api.domain.hotel;

import api.domain.airport.Airport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hotel Query
 * Created by claudio on 07/07/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelQuery {

  Airport airport;

  String checkIn;

  String checkOut;

}
