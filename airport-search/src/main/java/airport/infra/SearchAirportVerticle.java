package airport.infra;

import airport.domain.AirportQuery;
import airport.domain.TravelAirport;
import airport.infra.response.Airport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import java.util.List;
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
        LOGGER.info(String.format("Receiving airport search from %s to %s ", query.getOrigin(),query.getDestination()));
        final String originUrl = String.format(AIRPORT_URI, apiKey, query.getOrigin());
        final String destinationUrl = String.format(AIRPORT_URI, apiKey, query.getDestination());
        final Single<HttpResponse<Buffer>> originResponse = webClient.getAbs(originUrl).rxSend();
        final Single<HttpResponse<Buffer>> destinationResponse = webClient.getAbs(destinationUrl)
            .rxSend();
        originResponse.zipWith(destinationResponse,
            (firstResponse, secondResponse) -> {
              try {
                List<Airport> originAirports = MAPPER.readValue(firstResponse.bodyAsString(), AIRPORT_RESPONSE);
                List<Airport> destinationAirports = MAPPER.readValue(secondResponse.bodyAsString(), AIRPORT_RESPONSE);

                final TravelAirport travelAirport = TravelAirport.builder()
                    .destination(destinationAirports.get(0))
                    .origin(originAirports.get(0)).build();
                LOGGER.info(String.format("Success on search airport from %s to %s ", travelAirport.getOrigin().toString(),travelAirport.getDestination().toString()));
                return travelAirport;
              } catch (IOException e) {
                LOGGER.error("Error on deserialize airport", e);
                throw new RuntimeException(e);
              }
            }).subscribe(data -> {
          try {
            handler.reply(MAPPER.writeValueAsString(data));
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

  public static final TypeReference<List<Airport>> AIRPORT_RESPONSE = new TypeReference<List<Airport>>() {};

}
