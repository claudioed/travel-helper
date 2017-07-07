package hotels.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Hotel Data
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelData {

  String propertyCode;

  String propertyName;

  HotelTotalPrice totalPrice;

}
