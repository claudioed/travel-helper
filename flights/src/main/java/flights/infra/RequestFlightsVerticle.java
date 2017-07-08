package flights.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import flights.domain.FlightQuery;
import flights.infra.response.FlightResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import lombok.SneakyThrows;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Request FLIGHTS
 * Created by claudio on 07/07/17.
 */
public class RequestFlightsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestFlightsVerticle.class);

  private static final String FLIGHTS_URI = "https://api.sandbox.amadeus.com/v1.2/flights/extensive-search?apikey=%s&origin=%s&destination=%s&departure_date=%s&duration=%s";

  private static final String FLIGHTS_DATA_STREAM = "available-flights-eb";

  private static final String FLIGHTS_REQUESTER_EB = "request-flight-eb";

  @Override
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(FLIGHTS_REQUESTER_EB, handler -> {
      try {
        final FlightQuery flightQuery = MAPPER.readValue(handler.body().toString(), FlightQuery.class);
        final String target = String.format(FLIGHTS_URI, apiKey, flightQuery.getOrigin().getValue(),
            flightQuery.getDestination().getValue(), flightQuery.departure(), flightQuery.getDays());
        webClient.get(target).rxSend().subscribe(bufferHttpResponse -> {
          try {
            final FlightResponse flightResponse = MAPPER.readValue(bufferHttpResponse.bodyAsString(), FlightResponse.class);
            Observable.from(flightResponse.getResults()).delaySubscription(2, TimeUnit.SECONDS)
                .subscribe(data -> vertx.eventBus().send(FLIGHTS_DATA_STREAM, data));
          } catch (IOException e) {
            LOGGER.error("Error on deserialize flight response",e);
          }
        }, throwable -> LOGGER.error("Error on try to get flights", throwable));
      } catch (IOException e) {
        LOGGER.error("Error on deserialize flight query",e);
      }
    });
  }
}
