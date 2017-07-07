package flights.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Flight
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {

  String destination;

  @JsonProperty("departure_date")
  String departureDate;

  @JsonProperty("return_date")
  String returnDate;

  String price;

  String airline;

}
