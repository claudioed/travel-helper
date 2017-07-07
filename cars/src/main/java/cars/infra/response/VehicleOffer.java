package cars.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Vehicle Offer
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleOffer {

  VehicleInfo vehicleInfo;

  List<VehicleRate> rates;

  VehicleTotalPrice estimatedTotal;

}
