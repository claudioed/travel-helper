package airport.domain;

import airport.infra.response.Airport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Travel airports
 *
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelAirport {

  Airport origin;

  Airport destination;

}
