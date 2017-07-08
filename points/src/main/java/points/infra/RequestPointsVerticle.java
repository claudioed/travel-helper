package points.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import java.util.List;
import lombok.SneakyThrows;
import points.domain.PointQuery;
import points.infra.response.PointResponse;

/**
 * Request Hotels
 * Created by claudio on 07/07/17.
 */
public class RequestPointsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestPointsVerticle.class);

  private static final String POINTS_URI = "https://api.sandbox.amadeus.com/v1.2/points-of-interest/yapq-search-text?apikey=%s&city_name=%s";

  private static final String POINTS_DATA_STREAM = "available-points-eb";

  private static final String POINTS_REQUESTER_EB = "request-points-eb";

  @Override
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(POINTS_REQUESTER_EB, handler -> {
      try {
        final PointQuery pointQuery = MAPPER.readValue(handler.body().toString(), PointQuery.class);
        final String target = String.format(POINTS_URI, apiKey, pointQuery.getPlace());
        webClient.getAbs(target).rxSend().subscribe(bufferHttpResponse -> {
          try {
            final PointResponse pointResponse = MAPPER.readValue(bufferHttpResponse.bodyAsString(), PointResponse.class);
            vertx.eventBus().send(POINTS_DATA_STREAM, MAPPER.writeValueAsString(pointResponse));
          } catch (IOException e) {
            LOGGER.error("Error on deserialize points",e);
          }
        }, throwable -> LOGGER.error("Error on try to get POINTS", throwable));
      } catch (IOException e) {
        LOGGER.error("Error on deserialize object");
      }
    });
  }

}
