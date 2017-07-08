package cars.infra;

import cars.domain.CarQuery;
import cars.infra.response.CarRentalResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * Request Cars
 * Created by claudio on 07/07/17.
 */
public class RequestCarsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestCarsVerticle.class);

  private static final String CARS_URI = "https://api.sandbox.amadeus.com/v1.2/cars/search-airport?apikey=%s&location=%s&pick_up=%s&drop_off=%s";

  private static final String CARS_DATA_STREAM = "available-cars-eb";

  private static final String CARS_REQUESTER_EB = "request-car-eb";

  @Override
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(CARS_REQUESTER_EB, handler -> {
      try {
        final CarQuery carQuery = MAPPER.readValue(handler.body().toString(), CarQuery.class);
        final String target = String
            .format(CARS_URI, apiKey, carQuery.getAirport().getValue(), carQuery.getPickUp(),
                carQuery.getDropOf());
        webClient.getAbs(target).rxSend().subscribe(bufferHttpResponse -> {
          try {
            final CarRentalResponse carRentalResponse = MAPPER
                .readValue(bufferHttpResponse.bodyAsString(), CarRentalResponse.class);

            Observable.from(carRentalResponse.getResults()).delaySubscription(2, TimeUnit.SECONDS)
                .subscribe(data -> {
                  try {
                    vertx.eventBus().send(CARS_DATA_STREAM, MAPPER.writeValueAsString(data));
                  } catch (JsonProcessingException e) {
                    LOGGER.error("Error on deserialize car element", e);
                  }
                });
          } catch (IOException e) {
            LOGGER.error("Error on deserialize car response", e);
          }
        });

      } catch (IOException e) {
        LOGGER.error("Error on deserialize car query", e);
      }
    });
  }
}
