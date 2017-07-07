package cars.infra.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Vehicle Offer
 * Created by claudio on 07/07/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleOffer {

  @JsonProperty("vehicle_info")
  VehicleInfo vehicleInfo;

  List<VehicleRate> rates;

  @JsonProperty("estimated_total")
  VehicleTotalPrice estimatedTotal;

}
