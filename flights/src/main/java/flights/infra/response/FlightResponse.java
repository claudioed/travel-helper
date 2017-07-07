package flights.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import flights.domain.Flight;
import lombok.Data;

import java.util.List;

/**
 * Flight Response
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightResponse {

  String origin;

  String currency;

  List<Flight> results;

}
