package cars.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Airport
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Airport {

  String value;

}
