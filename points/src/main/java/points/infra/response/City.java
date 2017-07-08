package points.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * City response
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class City {

  String name;

}
