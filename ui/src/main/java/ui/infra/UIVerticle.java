package ui.infra;

import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

/**
 * UI verticle
 * @author claudioed on 08/07/17. Project travel-helper
 */
public class UIVerticle extends AbstractVerticle{

  @Override
  public void start() throws Exception {
    final Router router = Router.router(vertx);
    BridgeOptions opts = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddress("available-flights-eb"))
        .addOutboundPermitted(new PermittedOptions().setAddress("available-hotels-eb"))
        .addOutboundPermitted(new PermittedOptions().setAddress("available-cars-eb"))
        .addOutboundPermitted(new PermittedOptions().setAddress("available-points-eb"));

    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route("/eventbus/*").handler(ebHandler);
    router.route().handler(StaticHandler.create());
    vertx.createHttpServer().requestHandler(router::accept).listen(9090);
  }

}
