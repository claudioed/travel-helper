package api.infra;

import api.domain.AirportQuery;
import api.domain.TravelQuery;
import api.domain.airport.TravelAirports;
import api.domain.car.CarQuery;
import api.domain.points.PointQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Api Verticle
 * @author claudioed on 08/07/17. Project travel-helper
 */
public class ApiVerticle extends AbstractVerticle {

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
        vertx.eventBus().send(Endpoints.AIRPORT_REQUESTER_EB,MAPPER.writeValueAsString(airportQuery),reply -> {
          if(reply.succeeded()){
            try {
              final TravelAirports airports = MAPPER.readValue(reply.result().body().toString(), TravelAirports.class);
              LOGGER.info(String.format("travel airports RECEIVED from %s to %s",airports.getOrigin().toString(),airports.getDestination().toString()));
              final String pickUp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
              final String dropOf = LocalDateTime.now().plusDays(8)
                  .format(DateTimeFormatter.ISO_DATE);
              final CarQuery carQuery = CarQuery.builder().airport(airports.getDestination())
                  .pickUp(pickUp).dropOf(dropOf).build();
              vertx.eventBus().publish(Endpoints.CARS_REQUESTER_EB,MAPPER.writeValueAsString(carQuery));
              final PointQuery pointQuery = PointQuery.builder().place(query.getDestination()).build();
              vertx.eventBus().publish(Endpoints.POINTS_REQUESTER_EB,MAPPER.writeValueAsString(pointQuery));
            } catch (IOException e) {
              LOGGER.error("Error on deserialize travel airports",e);
            }
          }
        });
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
