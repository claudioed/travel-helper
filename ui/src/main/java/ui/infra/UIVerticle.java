/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

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
