package com.twilio.callforwarding.db;

import com.twilio.callforwarding.models.Senator;
import com.twilio.callforwarding.models.State;
import com.twilio.callforwarding.models.Zipcode;
import com.twilio.callforwarding.repositories.SenatorRepository;
import com.twilio.callforwarding.repositories.StateRepository;
import com.twilio.callforwarding.repositories.ZipcodeRepository;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DbSeedHelper {

    private SenatorRepository senatorRepository;
    private StateRepository stateRepository;
    private ZipcodeRepository zipcodeRepository;

    public DbSeedHelper() {
        this.senatorRepository = new SenatorRepository();
        this.stateRepository = new StateRepository();
        this.zipcodeRepository = new ZipcodeRepository();
    }

    public void seedZipcodes() {
        URL zipcodesResource = this.getClass().getResource("/seed/free-zipcode-database.csv");
        InsertBatch<Zipcode> batch = new InsertBatch<>(b -> zipcodeRepository.bulkCreate(b));
        try(Stream<String> stream = Files.lines(Paths.get(zipcodesResource.toURI()))) {
            stream.map(line -> Arrays.stream(line.split(","))
                                     .map(p -> p.replace("\"", ""))
                                     .collect(Collectors.toList()))
                .filter(lineParts -> lineParts.get(0).matches("^[0-9]+$"))
                .forEach(lineParts -> {
                    Integer zipcodeNumber = Integer.valueOf(lineParts.get(0));
                    Zipcode zipcode = new Zipcode(zipcodeNumber, lineParts.get(3));
                    batch.add(zipcode);
                });
            batch.flush();
        } catch (IOException|URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void seedStatesAndSenators() {
        URL senatorsResource = this.getClass().getResource("/seed/senators.json");
        try {
            InputStream senatorsInputStream = Files.newInputStream(Paths.get(senatorsResource.toURI()));
            JsonObject root = Json.createReader(senatorsInputStream).readObject();

            List<JsonString> states = root.getJsonArray("states")
                                          .getValuesAs(JsonString.class);
            states.stream()
                .filter(state -> root.containsKey(state.getString()))
                .forEach(stateJsonObject -> {
                    String stateName = stateJsonObject.getString();
                    State persistedState = stateRepository.create(new State(stateName));
                    root.getJsonArray(stateName)
                            .getValuesAs(JsonObject.class)
                            .stream()
                            .forEach(senatorJson -> {
                                persistSenator(persistedState, senatorJson);
                            });
                });
        } catch (IOException|URISyntaxException|NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void persistSenator(State persistedState, JsonObject senatorJson) {
        Senator senator = new Senator(
                senatorJson.getString("name"),
                senatorJson.getString("phone"),
                persistedState);
        senatorRepository.create(senator);
    }

    public void seedDb() {
        long countZipcode = zipcodeRepository.count();
        if(countZipcode == 0) {
            seedZipcodes();
        }
        long countSenator = senatorRepository.count();
        if(countSenator == 0) {
            seedStatesAndSenators();
        }
    }
}

class InsertBatch<T> {
    private List<T> batch;
    private Consumer<List<T>> action;

    public InsertBatch(Consumer<List<T>> action) {
        this.action = action;
        this.batch = new ArrayList<>();
    }

    public void add(T element) {
        batch.add(element);
        if(batch.size() > 1000) {
            flush();
        }
    }

    public void flush() {
        action.accept(batch);
        batch = new ArrayList<>();
    }
}