package hotels.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hotels.domain.HotelQuery;
import hotels.infra.response.HotelResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import lombok.SneakyThrows;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Request Hotels
 * Created by claudio on 07/07/17.
 */
public class RequestHotelsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHotelsVerticle.class);

  private static final String HOTELS_URI = "https://api.sandbox.amadeus.com/v1.2/hotels/search-airport?apikey=%s&location=%s&check_in=%s&check_out=%s";

  private static final String HOTELS_DATA_STREAM = "available-hotels-eb";

  private static final String HOTELS_REQUESTER_EB = "request-hotel-eb";

  @Override
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(HOTELS_REQUESTER_EB, handler -> {
      try {
        final HotelQuery hotelQuery = MAPPER.readValue(handler.body().toString(), HotelQuery.class);
        LOGGER.info("Searching hotels in " + hotelQuery.getAirport().getLabel());
        final String target = String
            .format(HOTELS_URI, apiKey, hotelQuery.getAirport().getValue(), hotelQuery.getCheckIn(),
                hotelQuery.getCheckOut());
        webClient.getAbs(target).rxSend().subscribe(bufferHttpResponse -> {
          try {
            final HotelResponse hotelResponse = MAPPER.readValue(bufferHttpResponse.bodyAsString(), HotelResponse.class);
            Observable.from(hotelResponse.getResults()).delaySubscription(5, TimeUnit.SECONDS)
                .subscribe(data -> {
                  try {
                    LOGGER.info("Sending new hotel " + data.getPropertyName());
                    vertx.eventBus().send(HOTELS_DATA_STREAM, MAPPER.writeValueAsString(data));
                  } catch (JsonProcessingException e) {
                    LOGGER.error("Error on serialize object",e);
                  }
                });
          } catch (Exception ex) {
            LOGGER.error("Error on deserialize object");
          }
        }, throwable -> LOGGER.error("Error on try to get HOTELS", throwable));
      } catch (IOException e) {
        LOGGER.error("Error on deserialize object");
      }

    });
  }

}
