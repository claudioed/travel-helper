package hotels.infra.response;

import lombok.Data;

/**
 * Hotel Data
 * Created by claudio on 07/07/17.
 */
@Data
public class HotelData {

    String propertyCode;

    String propertyName;

    HotelTotalPrice totalPrice;

}
