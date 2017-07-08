package api.domain.points;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point Query
 * Created by claudio on 07/07/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointQuery {

  String place;

}
