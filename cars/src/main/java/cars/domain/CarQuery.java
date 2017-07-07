package cars.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Car Query
 * Created by claudio on 07/07/17.
 */
@Data
public class CarQuery {

    Airport airport;

    LocalDateTime pickUp;

    LocalDateTime dropOf;

    public String pickUp(){
        return this.pickUp.toString();
    }

    public String dropOf(){
        return this.dropOf.toString();
    }

}
