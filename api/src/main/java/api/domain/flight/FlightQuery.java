package api.domain.flight;

import api.domain.airport.Airport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Location Query
 * Created by claudio on 07/07/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightQuery {

  Airport origin;

  Airport destination;

  String departureAt;

  Integer days;

}
