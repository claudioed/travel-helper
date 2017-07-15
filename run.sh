#!/usr/bin/env bash

mvn clean install

export AMADEUS_API_KEY=vAXkLMLUJZZGf1XTD9Lq7vrQOrJUCQkt

echo "Running airport..."
cd airport-search
mvn vertx:run &

echo "Running api..."
cd ..
cd api
mvn vertx:run &

echo "Running cars..."
cd ..
cd cars
mvn vertx:run &

echo "Running flights..."
cd ..
cd flights
mvn vertx:run &

echo "Running hotels..."
cd ..
cd hotels
mvn vertx:run &

echo "Running points..."
cd ..
cd points
mvn vertx:run &
