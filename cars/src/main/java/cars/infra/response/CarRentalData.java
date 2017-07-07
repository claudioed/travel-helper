package cars.infra.response;

import lombok.Data;

/**
 * Car Rental Data
 * Created by claudio on 07/07/17.
 */
@Data
public class CarRentalData {

    Provider provider;

    String airport;

    VehicleOffer cars;

}
