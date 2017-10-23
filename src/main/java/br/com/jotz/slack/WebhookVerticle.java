package br.com.jotz.slack;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

/**
 * 
 * @author gustavojotz
 *
 */
public class WebhookVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(WebhookVerticle.class);

	final HttpClientOptions httpClientOptions = new HttpClientOptions()
			.setDefaultHost("hooks.slack.com")
			.setDefaultPort(443)
			.setSsl(true)
			.setTrustAll(true);

	@Override
	public void start(Future<Void> future) throws Exception {

		new ProxyConfig(config(), httpClientOptions).configure();

		final Router router = Router.router(vertx);

		JsonArray relays = config().getJsonArray("relays");

		// iterate over path
		relays.forEach(r -> {

			JsonObject relay = ((JsonObject) r);
			String path = relay.getString("path");

			Route route = router.route(path);		
			
			route.handler(routingContext -> {
				
				routingContext.request().bodyHandler(body -> {

					HttpClient client = vertx.createHttpClient(httpClientOptions);
					
					JsonArray hooks = relay.getJsonArray("hooks");

					try {
						// iterate over webhooks
						hooks.forEach(h -> {
							
							JsonObject hook = ((JsonObject) h);
							
							JsonObject payload = decode(body);
							
							new PayloadConfigChannel(hook, payload).configure();
							
							String team = hook.getString("team");
	
							String param = routingContext.request().getParam("team");
							if (param != null && !param.isEmpty() && !param.equalsIgnoreCase(team)) {
								return;
							}
	
							// remove slack host, it's prepended with default
							String requestURI = hook.getString("url").replaceFirst("https://hooks.slack.com/", "/");
	
							client.post(requestURI, response -> {
								logger.info(String.format("[%s] [%s] HTTP %s: %s", path, team, response.statusCode(), response.statusMessage()));
							}).exceptionHandler(e -> {
								logger.error(e.getMessage(), e);
							}).putHeader("Content-Type", "text/html").end(payload.encode());
	
						});
	
						routingContext.response().putHeader("Content-Type", "text/html").end("OK");
						
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						routingContext.response().putHeader("Content-Type", "text/html").setStatusCode(400).setStatusMessage("Bad Request").end("invalid_payload");
					}
					
				});
				
			});
			
		});

		vertx.createHttpServer()
				.requestHandler(router::accept)
				.listen(config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						future.complete();
					} else {
						future.fail(result.cause());
					}
				});
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	protected JsonObject decode(final Buffer buffer) {
		try {
			return new JsonObject(URLDecoder.decode(buffer.toString().replaceFirst("payload=", ""), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
