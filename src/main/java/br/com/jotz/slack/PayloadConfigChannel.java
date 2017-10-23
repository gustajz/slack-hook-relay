package br.com.jotz.slack;

import io.vertx.core.json.JsonObject;

/**
 * 
 * @author gustavojotz
 *
 */
public class PayloadConfigChannel {
	
	static final String CHANNEL = "channel";
	
	private final JsonObject hook;
	
	private final JsonObject payload;
	
	public PayloadConfigChannel(final JsonObject hook, final JsonObject payload) {
		this.hook = hook;
		this.payload = payload;
	}

	public void configure() {
		String channel = hook.getString(CHANNEL);
		
		if (channel != null && payload.getString(CHANNEL) == null) {
			payload.put(CHANNEL, channel);
		}
	}

}