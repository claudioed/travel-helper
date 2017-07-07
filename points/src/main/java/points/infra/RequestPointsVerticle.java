package points.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import lombok.SneakyThrows;
import points.domain.PointQuery;

/**
 * Request Hotels
 * Created by claudio on 07/07/17.
 */
public class RequestPointsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestPointsVerticle.class);

  private static final String POINTS_URI = "https://api.sandbox.amadeus.com/v1.2/points-of-interest/yapq-search-text?apikey=%s&city_name=%s";

  private static final String POINTS_DATA_STREAM = "";

  private static final String POINTS_REQUESTER_EB = "";

  @Override
  @SneakyThrows
  public void start() throws Exception {
    final String apiKey = System.getenv("AMADEUS_API_KEY");
    final WebClient webClient = WebClient.create(this.vertx);
    this.vertx.eventBus().consumer(POINTS_REQUESTER_EB, handler -> {
      final PointQuery pointQuery = MAPPER.readValue(handler.body().toString(), PointQuery.class);
      final String target = String.format(POINTS_URI, apiKey, pointQuery.getPlace());
      webClient.get(target).rxSend().subscribe(bufferHttpResponse -> {
        final JsonObject data = new JsonObject(bufferHttpResponse.bodyAsString());
        vertx.eventBus().send(POINTS_DATA_STREAM, data);
      }, throwable -> LOGGER.error("Error on try to get POINTS", throwable));
    });
  }
}
