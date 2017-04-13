<a href="https://www.twilio.com">
  <img src="https://static0.twilio.com/marketing/bundles/marketing/img/logos/wordmark-red.svg" alt="Twilio" width="250" />
</a>

# Advanced Call Forwarding with Java, Spark, and Twilio

[![Build Status](https://travis-ci.org/TwilioDevEd/call-forwarding-spark.svg?branch=master)](https://travis-ci.org/TwilioDevEd/call-forwarding-spark)

Learn how to use [Twilio](https://www.twilio.com) to forward a series of phone calls to your state senators.

## Local Development
This project is built using the [Spark](http://sparkjava.com/) web framework.

To run the app locally, follow these steps:

1. Clone this repository and `cd` into it.
    ```bash
    git clone git@github.com:TwilioDevEd/call-forwarding-spark.git
    cd call-forwarding-spark
    ```

1. Copy the sample configuration file and edit it to match your configuration.
    ```bash
    cp .env.example .env
    ```
    You can find your TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN under your [Twilio Console](http://www.twilio.com/console/). You can buy Twilio phone numbers at Twilio numbers [TWILIO_NUMBER](https://www.twilio.com/console/phone-numbers) should be set to the phone number you purchased above.

1. Create application database:
    Make sure you have installed [PostgreSQL](http://www.postgresql.org/). If on a Mac, I recommend [Postgres.app](http://postgresapp.com).

    ```bash
    createdb call_forwarding
    ```

1. Expose your application to the internet using [ngrok](https://www.twilio.com/blog/2015/09/6-awesome-reasons-to-use-ngrok-when-testing-webhooks.html). In a separate terminal session, start ngrok with:
    ```bash
    ngrok http 9292
    ```
    Once you have started ngrok, update your TwiML application's voice URL setting to use your ngrok hostname. It will look something like this in your Twilio [console](https://www.twilio.com/console/phone-numbers/):
    ```
    https://d06f533b.ngrok.io/callcongress/welcome
    ```

1. Start your development server:
    ```bash
    ./gradlew run
    ```
    Once ngrok is running, open up your browser and go to your ngrok URL.

## Run the Tests
  ```bash
  ./gradlew test
  ```

## Meta
* No warranty expressed or implied. Software is as is. Diggity.
* [MIT License](https://opensource.org/licenses/mit-license.html)
* Lovingly crafted by Twilio Developer Education.
