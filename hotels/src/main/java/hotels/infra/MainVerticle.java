package hotels.infra;

import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Hotels main verticle
 * @author claudioed on 07/07/17. Project travel-helper
 */
public class MainVerticle extends AbstractVerticle{

  @Override
  public void start() throws Exception {
    ClusterManager mgr = new HazelcastClusterManager();
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      final Vertx vertx = res.result();
      vertx.deployVerticle(RequestHotelsVerticle.class.getName());
    });
  }

}