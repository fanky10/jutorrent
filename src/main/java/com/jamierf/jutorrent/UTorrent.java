package com.jamierf.jutorrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UTorrent {

	private static final Logger logger = LoggerFactory.getLogger(UTorrent.class);

	private final String baseURL;
	private final String token;

	private final HttpClient client;

	private final TorrentList torrents;

	public UTorrent(InetSocketAddress address, String username, String password, int delay) {
		client = new HttpClient();
		client.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), new UsernamePasswordCredentials(username, password));

		baseURL = "http://" + address.getHostString() + ":" + address.getPort() + "/gui/";
		token = this.getToken();

		torrents = new TorrentList(this, Executors.newCachedThreadPool());
		torrents.update();

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
			public void run() {
				torrents.update();
			}
		}, delay, delay, TimeUnit.SECONDS);
	}

	public void addListener(TorrentListener listener) {
		torrents.addListener(listener);
	}

	public void removeListener(TorrentListener listener) {
		torrents.removeListener(listener);
	}

	String get(String query) {
		final HttpMethod request = new GetMethod(baseURL + "?token=" + token + "&" + query);
		return this.call(request);
	}

	private synchronized String call(HttpMethod request) {
		try {
			try {
				final int result = client.executeMethod(request);
				if (result != HttpStatus.SC_OK)
					throw new IOException("Receied non-OK response code " + result + " for URI: " + request.getURI());

				return request.getResponseBodyAsString();
			}
			finally {
				request.releaseConnection();
			}
		}
		catch (IOException e) {
			if (logger.isWarnEnabled())
				logger.warn("Failed to call web API", e);

			return null;
		}
	}

	private String getToken() {
		final String result = this.call(new GetMethod(baseURL + "token.html"));

		final Pattern regex = Pattern.compile(">([^<]+)<");
		final Matcher matcher = regex.matcher(result);

		if (!matcher.find()) {
			if (logger.isWarnEnabled())
				logger.warn("Failed to find auth token");

			return null;
		}

		return matcher.group(1);
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> getMap(String query) {
		final String result = this.get(query);
		final JSONParser parser = new JSONParser();

		try {
			return (Map<String, Object>) parser.parse(result);
		}
		catch (ParseException e) {
			if (logger.isWarnEnabled())
				logger.warn("Failed to parse JSON response", e);

			return new HashMap<String, Object>();
		}
	}

	// TODO: Make into a ServerVersion object
	@SuppressWarnings("unchecked")
	public Map<String, Object> getVersion() {
		final Map<String, Object> settings = this.getMap("action=getversion");
		return (Map<String, Object>) settings.get("version");
	}

	public Map<String, Torrent> getTorrents() {
		return torrents;
	}

	public Torrent addTorrent(TorrentFile file) throws FileNotFoundException {
		final Part[] parts = {
			new FilePart("torrent_file", file)
		};

		final PostMethod request = new PostMethod(baseURL + "?token=" + token + "&" + "action=add-file");
		request.setRequestEntity(new MultipartRequestEntity(parts, request.getParams()));

		this.call(request);



		return null; // TODO
	}
}
