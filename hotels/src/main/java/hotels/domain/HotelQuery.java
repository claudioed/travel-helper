package hotels.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Hotel Query
 * Created by claudio on 07/07/17.
 */
@Data
public class HotelQuery {

    Airport airport;

    LocalDateTime checkIn;

    LocalDateTime checkOut;

    public String checkIn(){
        return this.checkIn.toString();
    }

    public String checkOut(){
        return this.checkOut.toString();
    }

}
