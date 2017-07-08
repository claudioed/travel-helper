package hotels.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Hotel Data
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelData {

  @JsonProperty("property_code")
  String propertyCode;

  @JsonProperty("property_name")
  String propertyName;

  @JsonProperty("total_price")
  HotelTotalPrice totalPrice;

}
