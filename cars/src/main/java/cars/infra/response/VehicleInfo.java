package cars.infra.response;

import lombok.Data;

/**
 * Vehicle Info
 * Created by claudio on 07/07/17.
 */
@Data
public class VehicleInfo {

    String transmission;

    Boolean airConditioning;

    String category;

    String type;

}
