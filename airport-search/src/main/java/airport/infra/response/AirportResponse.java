package airport.infra.response;

import java.util.List;
import lombok.Data;

/**
 * Airport response
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
public class AirportResponse {

  List<Airport> airports;

  public Airport first(){
    return airports.get(0);
  }

}
