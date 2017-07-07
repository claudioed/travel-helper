package cars.infra.response;

import lombok.Data;

/**
 * Vehicles rates
 * Created by claudio on 07/07/17.
 */
@Data
public class VehicleRate {

    String type;

    VehiclePrice price;

}
