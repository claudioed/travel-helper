package cars.infra;

import cars.domain.CarQuery;
import cars.infra.response.CarRentalResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Request Cars
 * Created by claudio on 07/07/17.
 */
public class RequestCarsVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCarsVerticle.class);

    private static final String CARS_URI = "https://api.sandbox.amadeus.com/v1.2/cars/search-airport?apikey=%s&location=%s&pick_up=%s&drop_off=%s";

    private static final String CARS_DATA_STREAM = "available-cars-eb";

    private static final String CARS_REQUESTER_EB = "request-car-eb";

    @Override
    public void start() throws Exception {
        final String apiKey = System.getenv("AMADEUS_API_KEY");
        final WebClient webClient = WebClient.create(this.vertx);
        this.vertx.eventBus().consumer(CARS_REQUESTER_EB, handler -> {
            final CarQuery carQuery = Json.decodeValue(handler.body().toString(), CarQuery.class);
            final String target = String.format(CARS_URI, apiKey, carQuery.getAirport().getAirport(), carQuery.pickUp(), carQuery.dropOf());
            webClient.get(target).rxSend().subscribe(bufferHttpResponse -> {
                final CarRentalResponse carRentalResponse = Json.decodeValue(bufferHttpResponse.bodyAsString(), CarRentalResponse.class);
                Observable.from(carRentalResponse.getResults()).delaySubscription(2, TimeUnit.SECONDS).subscribe(data -> vertx.eventBus().send(CARS_DATA_STREAM, data));
            }, throwable -> LOGGER.error("Error on try to get cars", throwable));
        });
    }
}