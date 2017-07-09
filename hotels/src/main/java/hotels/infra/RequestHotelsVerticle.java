package hotels.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hotels.domain.HotelQuery;
import hotels.infra.response.HotelResponse;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import lombok.SneakyThrows;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import rx.Single;

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

    final CircuitBreakerOptions cbOptions = new CircuitBreakerOptions()
        .setMaxFailures(10)
        .setTimeout(4000L)
        .setResetTimeout(8000L)
        .setMaxRetries(2)
        .setFallbackOnFailure(true);

    final CircuitBreaker circuitBreaker = CircuitBreaker.create("hotels-cb",this.vertx,cbOptions);

    this.vertx.eventBus().consumer(HOTELS_REQUESTER_EB, handler -> {
      try {
        final HotelQuery hotelQuery = MAPPER.readValue(handler.body().toString(), HotelQuery.class);
        LOGGER.info("Searching hotels in " + hotelQuery.getAirport().getLabel());
        final String target = String
            .format(HOTELS_URI, apiKey, hotelQuery.getAirport().getValue(), hotelQuery.getCheckIn(),
                hotelQuery.getCheckOut());
        final Single<HttpResponse<Buffer>> httpResponseSingle = webClient.getAbs(target).rxSend();
        circuitBreaker
            .rxExecuteCommand(
                (Handler<Future<HttpResponse<Buffer>>>) future -> httpResponseSingle.subscribe(future::complete)).subscribe(el -> {
          try {
            final HotelResponse hotelResponse = MAPPER.readValue(el.bodyAsString(), HotelResponse.class);
            vertx.eventBus().send(HOTELS_DATA_STREAM, MAPPER.writeValueAsString(hotelResponse));
          } catch (IOException e) {
            LOGGER.error("Error on deserialize hotels",e);
          }
        });
      } catch (IOException e) {
        LOGGER.error("Error on deserialize hotel query");
      }
    });
  }

}
