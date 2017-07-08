package points.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {

  String title;

  @JsonProperty("main_image")
  String mainImage;

}
