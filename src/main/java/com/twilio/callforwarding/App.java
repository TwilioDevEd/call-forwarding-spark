package com.twilio.callforwarding;

import com.twilio.callforwarding.db.DbSeedHelper;
import com.twilio.callforwarding.logging.LoggingFilter;
import com.twilio.callforwarding.models.Senator;
import com.twilio.callforwarding.models.Zipcode;
import com.twilio.callforwarding.repositories.SenatorRepository;
import com.twilio.callforwarding.repositories.StateRepository;
import com.twilio.callforwarding.repositories.ZipcodeRepository;
import com.twilio.twiml.*;
import com.twilio.twiml.Number;
import spark.servlet.SparkApplication;
import spark.utils.StringUtils;

import java.util.List;

import static spark.Spark.*;

public class App implements SparkApplication {

    private static final String APPLICATION_XML = "application/xml";
    private SenatorRepository senatorRepository;
    private StateRepository stateRepository;
    private ZipcodeRepository zipcodeRepository;

    public App() {
        this.senatorRepository = new SenatorRepository();
        this.stateRepository = new StateRepository();
        this.zipcodeRepository = new ZipcodeRepository();
    }

    @Override
    public void init() {

        DbSeedHelper dbSeedHelper = new DbSeedHelper();
        dbSeedHelper.seedDb();

        afterAfter(new LoggingFilter());

        get("/ping", (req, res) -> "pong");

        // Verify or collect State information.
        post("/callcongress/welcome", (request, response) -> {
            String fromState = request.params("FromState");

            VoiceResponse.Builder builder = new VoiceResponse.Builder();
            if (StringUtils.isNotBlank(fromState)) {
                builder.say(new Say.Builder(
                        String.format("Thank you for calling congress! It looks like" +
                            "you\'re calling from %s." +
                            "If this is correct, please press 1. Press 2 if" +
                            "this is not your current state of residence.", fromState)).build());
                builder.gather(new Gather.Builder()
                        .numDigits(1)
                        .action("/callcongress/set-state")
                        .method(Method.POST)
                        .build());
            } else {
                builder.say(new Say.Builder(
                        "Thank you for calling Call Congress! If you wish to" +
                        "call your senators, please enter your 5 - digit zip code.").build());
                builder.gather(new Gather.Builder()
                        .numDigits(5)
                        .action("/callcongress/state-lookup")
                        .method(Method.POST)
                        .build());
            }
            response.type(APPLICATION_XML);
            return builder.build().toXml();
        });

        // If our state guess is wrong, prompt user for zip code.
        post("/callcongress/collect-zip", (request, response) -> {
            VoiceResponse.Builder builder = new VoiceResponse.Builder();
            builder.say(new Say.Builder("If you wish to call your senators, please " +
                    "enter your 5-digit zip code.").build());
            builder.gather(new Gather.Builder()
                    .numDigits(5)
                    .action("/callcongress/state-lookup")
                    .method(Method.POST)
                    .build());

            response.type(APPLICATION_XML);
            return builder.build().toXml();
        });

        // Look up state from given zipcode.
        // Once state is found, redirect to call_senators for forwarding.
        post("/callcongress/state-lookup", (request, response) -> {
            String zipcode = request.params("Digits");

            // NB: We don't do any error handling for a missing/erroneous zip code
            // in this sample application. You, gentle reader, should to handle that
            // edge case before deploying this code.
            response.type("application/xml");
            Zipcode zipcodeObject = zipcodeRepository.getFirstResultFilteredByZipcode(zipcode);

            response.redirect("/callcongress/call-senators/" + zipcodeObject.getState());
            return "";
        });

        // Set state for senator call list.
        // Set user's state from confirmation or user-provided Zip.
        // Redirect to call_senators route.
        post("/callcongress/set-state", (request, response) -> {
            // Get the digit pressed by the user
            String digits = request.params("Digits");

            if(digits.equals("1")) {
                String callerState = request.params("CallerState");
                response.redirect("/callcongress/call-senators/" + callerState);
            } else {
                response.redirect("/callcongress/collect-zip");
            }
            return "";
        });

        // Route for connecting caller to both of their senators.
        post("/callcongress/call-senators/:state", (request, response) -> {
            String state = request.params("state");

            List<Senator> senators = stateRepository
                    .findByStateName(state)
                    .getSenators();

            VoiceResponse.Builder builder = new VoiceResponse.Builder();
            Senator firstCall = senators.get(0);
            Senator secondCall = senators.get(1);
            String sayMessage = String.format("Connecting you to {}. " +
                    "After the senator's office ends the call, you will " +
                    "be re-directed to {}.",
                    firstCall.getName(),
                    secondCall.getName());
            builder.say(new Say.Builder(sayMessage).build());
            builder.dial(new Dial.Builder()
                    .number(new Number.Builder(firstCall.getPhone()).build())
                    .action("/callcongress/call-second-senator/" + secondCall.getId())
                    .build());

            response.type(APPLICATION_XML);
            return builder.build().toXml();
        });

        // Forward the caller to their second senator.
        post("/callcongress/call-second-senator/:senatorId", (request, response) -> {
            Senator senator = senatorRepository.find(Long.valueOf(request.params("senatorId")));
            VoiceResponse.Builder builder = new VoiceResponse.Builder();

            String sayMessage = String.format("Connecting you to {}.", senator.getName());
            builder.say(new Say.Builder(sayMessage).build());

            builder.dial(new Dial.Builder()
                    .number(new Number.Builder(senator.getPhone()).build())
                    .action("/callcongress/goodbye")
                    .build());

            response.type(APPLICATION_XML);
            return builder.build().toXml();
        });

        // Thank user & hang up.
        post("/callcongress/goodbye", (request, response) -> {
            VoiceResponse.Builder builder = new VoiceResponse.Builder();
            builder.say(new Say.Builder("Thank you for using Call Congress! " +
                    "Your voice makes a difference. Goodbye.").build());
            builder.hangup(new Hangup());

            return builder.build().toXml();
        });

    }
}
