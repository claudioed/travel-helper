package api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Airport Query
 *
 * @author claudioed on 08/07/17. Project travel-helper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportQuery {

  String origin;

  String destination;

}
