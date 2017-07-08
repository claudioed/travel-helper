package points.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * Points Response
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointResponse {

  @JsonProperty("current_city")
  City city;

  @JsonProperty("points_of_interest")
  List<Point> points;

}
