package com.twilio.callforwarding;

import com.twilio.callforwarding.logging.LoggingFilter;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

public class App implements SparkApplication {

  @Override
  public void init() {
    get("/ping", (req, res) -> "pong");

    afterAfter(new LoggingFilter());

  }
}
