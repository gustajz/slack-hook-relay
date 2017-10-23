FROM vertx/vertx3

ENV VERTICLE_NAME br.com.jotz.slack.WebhookVerticle

ENV VERTICLE_FILE target/slack-hook-relay-1.0.0-SNAPSHOT.jar

ENV VERTX_OPTIONS "-conf conf/config.json"

ENV CONFIG_FILE config.json

ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY $CONFIG_FILE $VERTICLE_HOME/conf/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["vertx run -cp $VERTICLE_HOME/*.jar $VERTICLE_NAME $VERTX_OPTIONS"]