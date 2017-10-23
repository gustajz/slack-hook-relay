package br.com.jotz.slack;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;

/**
 * 
 * @author gustavojotz
 *
 */
public class ProxyConfig {
	
	private final JsonObject config;

	private final HttpClientOptions httpClientOptions;
	
	public ProxyConfig(final JsonObject config, final HttpClientOptions httpClientOptions) {
		this.config = config;
		this.httpClientOptions = httpClientOptions;
	}

	public void configure() {
		JsonObject proxy = config.getJsonObject("proxy");
		if (proxy != null) {
			httpClientOptions.setProxyOptions(new ProxyOptions(proxy));
		}
	}		

}