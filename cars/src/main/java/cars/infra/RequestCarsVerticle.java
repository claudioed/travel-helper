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

package cars.infra;

import cars.domain.CarQuery;
import cars.infra.response.CarRentalResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Single;

/**
 * Request Cars
 * Created by claudio on 07/07/17.
 */
public class RequestCarsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestCarsVerticle.class);

  private static final String CARS_URI = "https://api.sandbox.amadeus.com/v1.2/cars/search-airport?apikey=%s&location=%s&pick_up=%s&drop_off=%s";

  private static final String CARS_DATA_STREAM = "available-cars-eb";

  private static final String CARS_REQUESTER_EB = "request-car-eb";

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

    final CircuitBreaker circuitBreaker = CircuitBreaker.create("cars-cb",this.vertx,cbOptions);

    this.vertx.eventBus().consumer(CARS_REQUESTER_EB, handler -> {
      try {
        final CarQuery carQuery = MAPPER.readValue(handler.body().toString(), CarQuery.class);
        LOGGER.info(String.format("Receiving car request for %s ", carQuery.getAirport().getValue()));
        final String target = String
            .format(CARS_URI, apiKey, carQuery.getAirport().getValue(), carQuery.getPickUp(),
                carQuery.getDropOf());
        final Single<HttpResponse<Buffer>> httpResponseSingle = webClient.getAbs(target).rxSend();
        circuitBreaker
            .rxExecuteCommand(
                (Handler<Future<HttpResponse<Buffer>>>) future -> httpResponseSingle.subscribe(future::complete)).subscribe(el -> {
          try {
            final CarRentalResponse carRentalResponse = MAPPER.readValue(el.bodyAsString(), CarRentalResponse.class);
            vertx.eventBus().send(CARS_DATA_STREAM, MAPPER.writeValueAsString(carRentalResponse));
          } catch (IOException e) {
            LOGGER.error("Error on deserialize cars",e);
          }
        });
      } catch (IOException e) {
        LOGGER.error("Error on deserialize car query", e);
      }
    });
  }
}
