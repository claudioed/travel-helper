#!/usr/bin/env bash

mvn clean install

echo "Running airport..."
nohup java -jar airport-search/target/airport-helper.jar > airport.log &

echo "Running api..."
nohup java -jar api/target/api-helper.jar > api.log &

echo "Running cars..."
nohup java -jar cars/target/cars-helper.jar > cars.log &

echo "Running flights..."
nohup java -jar flights/target/flights-helper.jar > flights.log &

echo "Running hotels..."
nohup java -jar hotels/target/hotels-helper.jar > hotels.log &

echo "Running points..."
nohup java -jar points/target/points-helper.jar > points.log &

echo "Running UI..."
nohup java -jar ui/target/ui.jar > ui.log &