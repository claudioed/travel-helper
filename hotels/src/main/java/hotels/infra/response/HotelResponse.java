package hotels.infra.response;

import lombok.Data;

import java.util.List;

/**
 * Hotel Response
 * Created by claudio on 07/07/17.
 */
@Data
public class HotelResponse {

    List<HotelData> results;

}
