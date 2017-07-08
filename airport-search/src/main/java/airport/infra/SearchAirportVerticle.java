package airport.infra;

import airport.domain.AirportQuery;
import airport.domain.TravelAirport;
import airport.infra.response.AirportResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import rx.Single;

/**
 * Search Airport Verticle
 *
 * @author claudioed on 08/07/17. Project travel-helper
 */
public class SearchAirportVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchAirportVerticle.class);

  private static final String AIRPORT_URI = "https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey=%s&term=%s";

  private static final String AIRPORT_DATA_STREAM = "available-airport-eb";

  private static final String AIRPORT_REQUESTER_EB = "request-airports-eb";

  @Override
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(AIRPORT_REQUESTER_EB, handler -> {
      try {
        final AirportQuery query = MAPPER.readValue(handler.body().toString(), AirportQuery.class);

        final String originUrl = String.format(AIRPORT_URI, apiKey, query.getOrigin());
        final String destinationUrl = String.format(AIRPORT_URI, apiKey, query.getDestination());

        final Single<HttpResponse<Buffer>> originResponse = webClient.get(originUrl).rxSend();
        final Single<HttpResponse<Buffer>> destinationResponse = webClient.get(destinationUrl)
            .rxSend();

        originResponse.zipWith(destinationResponse,
            (firstResponse, secondResponse) -> {
              try {
                final AirportResponse originResponse1 = MAPPER.readValue(firstResponse.bodyAsString(), AirportResponse.class);
                final AirportResponse destinationResponse1 = MAPPER.readValue(secondResponse.bodyAsString(), AirportResponse.class);
                return TravelAirport.builder().destination(destinationResponse1.first())
                    .origin(originResponse1.first()).build();
              } catch (IOException e) {
                LOGGER.error("Error on deserialize airport", e);
                throw new RuntimeException(e);
              }
            }).subscribe(data -> {
          try {
            vertx.eventBus().send(AIRPORT_DATA_STREAM,MAPPER.writeValueAsString(data));
          } catch (JsonProcessingException e) {
            LOGGER.error("Error on send message to airport event bus", e);
          }
        });
      } catch (IOException e) {
        LOGGER.error("Error on deserialize object", e);
      }
    });
  }
}
