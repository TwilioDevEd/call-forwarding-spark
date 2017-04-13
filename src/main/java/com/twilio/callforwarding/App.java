package com.twilio.callforwarding;

import spark.servlet.SparkApplication;

import static spark.Spark.*;

public class App implements SparkApplication {

  @Override
  public void init() {
    get("/ping", (req, res) -> "pong");

  }
}
