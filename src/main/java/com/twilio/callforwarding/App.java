package com.twilio.callforwarding;

import com.twilio.callforwarding.logging.LoggingFilter;
import com.twilio.callforwarding.db.DbSeedHelper;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

public class App implements SparkApplication {

  @Override
  public void init() {

    DbSeedHelper dbSeedHelper = new DbSeedHelper();
    dbSeedHelper.seedDb();

    get("/ping", (req, res) -> "pong");

    afterAfter(new LoggingFilter());

  }
}
