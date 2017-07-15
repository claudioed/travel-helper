package points.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import points.domain.PointQuery;
import points.infra.response.PointResponse;
import rx.Single;

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

    final CircuitBreakerOptions cbOptions = new CircuitBreakerOptions()
        .setMaxFailures(10)
        .setTimeout(4000L)
        .setResetTimeout(8000L)
        .setMaxRetries(2)
        .setFallbackOnFailure(true);

    final CircuitBreaker circuitBreaker = CircuitBreaker.create("points-cb",this.vertx,cbOptions);

    this.vertx.eventBus().consumer(POINTS_REQUESTER_EB, handler -> {
      try {
        final PointQuery pointQuery = MAPPER.readValue(handler.body().toString(), PointQuery.class);
        LOGGER.info(String.format("Receiving request points %s ",pointQuery.getPlace()));
        final String target = String.format(POINTS_URI, apiKey, pointQuery.getPlace());
        final Single<HttpResponse<Buffer>> httpResponseSingle = webClient.getAbs(target).rxSend();
        circuitBreaker
            .rxExecuteCommand(
                (Handler<Future<HttpResponse<Buffer>>>) future -> httpResponseSingle.subscribe(future::complete)).subscribe(el -> {
          try {
                final PointResponse pointResponse = MAPPER.readValue(el.bodyAsString(), PointResponse.class);
                vertx.eventBus().send(POINTS_DATA_STREAM, MAPPER.writeValueAsString(pointResponse));
              } catch (IOException e) {
                LOGGER.error("Error on deserialize points",e);
              }
        },throwable -> {
          LOGGER.error("Error on call places api",throwable);
        });
      } catch (IOException e) {
        LOGGER.error("Error on deserialize object");
      }
    });
  }

}
