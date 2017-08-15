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

package flights.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flights.domain.FlightQuery;
import flights.infra.response.FlightResponse;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import lombok.SneakyThrows;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import rx.Single;

/**
 * Request FLIGHTS
 * Created by claudio on 07/07/17.
 */
public class RequestFlightsVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestFlightsVerticle.class);

  private static final String FLIGHTS_URI = "https://api.sandbox.amadeus.com/v1.2/flights/extensive-search?apikey=%s&origin=%s&destination=%s&departure_date=%s&duration=%s";

  private static final String FLIGHTS_DATA_STREAM = "available-flights-eb";

  private static final String FLIGHTS_REQUESTER_EB = "request-flight-eb";

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

    final CircuitBreaker circuitBreaker = CircuitBreaker.create("flights-cb",this.vertx,cbOptions);

    this.vertx.eventBus().consumer(FLIGHTS_REQUESTER_EB, handler -> {
      try {
        final FlightQuery flightQuery = MAPPER.readValue(handler.body().toString(), FlightQuery.class);
        LOGGER.info(String.format("Receiving flight request from %s to %s",flightQuery.getOrigin().getLabel(),flightQuery.getDestination().getLabel()));
        final String target = String.format(FLIGHTS_URI, apiKey, flightQuery.getOrigin().getValue(),
            flightQuery.getDestination().getValue(), flightQuery.getDepartureAt(), flightQuery.getDays());

        final Single<HttpResponse<Buffer>> httpResponseSingle = webClient.getAbs(target).rxSend();
        circuitBreaker
            .rxExecuteCommand(
                (Handler<Future<HttpResponse<Buffer>>>) future -> httpResponseSingle.subscribe(future::complete)).subscribe(el -> {
          try {
            final FlightResponse flightsResponse = MAPPER.readValue(el.bodyAsString(), FlightResponse.class);
            vertx.eventBus().send(FLIGHTS_DATA_STREAM, MAPPER.writeValueAsString(flightsResponse));
          } catch (IOException e) {
            LOGGER.error("Error on deserialize flights",e);
          }
        });

      } catch (IOException e) {
        LOGGER.error("Error on deserialize flight query",e);
      }
    });
  }
}
