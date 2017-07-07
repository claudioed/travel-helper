package hotels.infra;

import hotels.domain.HotelQuery;
import hotels.infra.response.HotelResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Request Hotels
 * Created by claudio on 07/07/17.
 */
public class RequestHotelsVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHotelsVerticle.class);

    private static final String HOTELS_URI = "https://api.sandbox.amadeus.com/v1.2/hotels/search-airport?apikey=%s&location=%s&check_in=%s&check_out=%s";

    private static final String HOTELS_DATA_STREAM = "available-hotels-eb";

    private static final String HOTELS_REQUESTER_EB = "request-hotel-eb";

    @Override
    public void start() throws Exception {
        final String apiKey = System.getenv("AMADEUS_API_KEY");
        final WebClient webClient = WebClient.create(this.vertx);
        this.vertx.eventBus().consumer(HOTELS_REQUESTER_EB, handler -> {
            final HotelQuery hotelQuery = Json.decodeValue(handler.body().toString(), HotelQuery.class);
            final String target = String.format(HOTELS_URI, apiKey, hotelQuery.getAirport().getAirport(), hotelQuery.checkIn(), hotelQuery.checkOut());
            webClient.get(target).rxSend().subscribe(bufferHttpResponse -> {
                final HotelResponse hotelResponse = Json.decodeValue(bufferHttpResponse.bodyAsString(), HotelResponse.class);
                Observable.from(hotelResponse.getResults()).delaySubscription(2, TimeUnit.SECONDS).subscribe(data -> vertx.eventBus().send(HOTELS_DATA_STREAM, data));
            }, throwable -> LOGGER.error("Error on try to get HOTELS", throwable));
        });
    }

}
