package com.twilio.callforwarding;

import com.twilio.callforwarding.controllers.CallCongressController;
import com.twilio.callforwarding.db.DbSeedHelper;
import com.twilio.callforwarding.logging.LoggingFilter;
import spark.servlet.SparkApplication;

import static spark.Spark.afterAfter;
import static spark.Spark.post;
import static spark.Spark.get;

public class App implements SparkApplication {

    private CallCongressController callCongressController;

    public App() {
        this.callCongressController = new CallCongressController();
    }

    @Override
    public void init() {

        DbSeedHelper dbSeedHelper = new DbSeedHelper();
        dbSeedHelper.seedDb();

        afterAfter(new LoggingFilter());

        post("/callcongress/welcome", callCongressController.welcomeRoute);

        post("/callcongress/collect-zip", callCongressController.collectZipRoute);
        get("/callcongress/collect-zip", callCongressController.collectZipRoute);

        post("/callcongress/state-lookup", callCongressController.stateLookupRoute);

        post("/callcongress/set-state", callCongressController.setStateRoute);

        post("/callcongress/call-senators/:state",
                callCongressController.callSenatorsRoute);
        get("/callcongress/call-senators/:state",
                callCongressController.callSenatorsRoute);

        post("/callcongress/call-second-senator/:senatorId",
                callCongressController.callSecondSenatorRoute);

        post("/callcongress/goodbye", callCongressController.goodbyeRoute);

    }
}
