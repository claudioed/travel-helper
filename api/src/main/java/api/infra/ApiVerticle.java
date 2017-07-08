package api.infra;

import api.domain.AirportQuery;
import api.domain.TravelQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import java.io.IOException;

/**
 * Api Verticle
 * @author claudioed on 08/07/17. Project travel-helper
 */
public class ApiVerticle extends AbstractVerticle {

  private static final String AIRPORT_REQUESTER_EB = "request-airports-eb";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiVerticle.class);

  @Override
  public void start() throws Exception {
    final Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/api/travel").handler(ctx -> {
      try {
        final TravelQuery query = MAPPER.readValue(ctx.getBodyAsString("utf-8"), TravelQuery.class);
        LOGGER.info(String.format("Receiving travel request from %s to %s",query.getOrigin(),query.getDestination()));
        final AirportQuery airportQuery = AirportQuery.builder().destination(query.getDestination())
            .origin(query.getOrigin()).build();
        vertx.eventBus().publish(AIRPORT_REQUESTER_EB,MAPPER.writeValueAsString(airportQuery));
        ctx.response().putHeader("Content-Type", "application/json");
        ctx.response().end();
      } catch (IOException e) {
        LOGGER.error("Error on deserialize travel query object",e);
        ctx.response().setStatusCode(500).end();
      }
    });
    vertx.createHttpServer().requestHandler(router::accept).listen(8888);
  }
}
