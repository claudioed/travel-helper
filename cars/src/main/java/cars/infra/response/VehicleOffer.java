package cars.infra.response;

import lombok.Data;

import java.util.List;

/**
 * Vehicle Offer
 * Created by claudio on 07/07/17.
 */
@Data
public class VehicleOffer {

    VehicleInfo vehicleInfo;

    List<VehicleRate> rates;

    VehicleTotalPrice estimatedTotal;

}
