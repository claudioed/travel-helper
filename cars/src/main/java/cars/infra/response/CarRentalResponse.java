package cars.infra.response;

import lombok.Data;

import java.util.List;

/**
 * Car Rental Response
 * Created by claudio on 07/07/17.
 */
@Data
public class CarRentalResponse {

    List<CarRentalData> results;

}
