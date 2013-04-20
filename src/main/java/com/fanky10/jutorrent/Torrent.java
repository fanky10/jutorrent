package com.fanky10.jutorrent;

import java.util.List;

public class Torrent {

	static final int STATUS_STARTED = 1;
	static final int STATUS_CHECKING = 2;
	static final int STATUS_START_AFTER_CHECK = 4;
	static final int STATUS_CHECKED = 8;
	static final int STATUS_ERROR = 16;
	static final int STATUS_PAUSED = 32;
	static final int STATUS_QUEUED = 64;
	static final int STATUS_LOADED = 128;

	static final int FIELD_HASH = 0;
	static final int FIELD_STATUS = 1;
	static final int FIELD_NAME = 2;
	static final int FIELD_SIZE = 3;
	static final int FIELD_PROGRESS = 4;
	static final int FIELD_DOWNLOADED = 5;
	static final int FIELD_UPLOADED = 6;
	static final int FIELD_RATIO = 7;
	static final int FIELD_UPLOAD_SPEED = 8;
	static final int FIELD_DOWNLOAD_SPEED = 9;
	static final int FIELD_ETA = 10;
	static final int FIELD_LABEL = 11;
	static final int FIELD_PEERS_CONNECTED = 12;
	static final int FIELD_PEERS_IN_SWARM = 13;
	static final int FIELD_SEEDS_CONNECTED = 14;
	static final int FIELD_SEEDS_IN_SWARM = 15;
	static final int FIELD_AVAILABILITY = 16;
	static final int FIELD_ORDER = 17;
	static final int FIELD_REMAINING = 18;

	private static final String ACTION_CALL = "action=%s&hash=%s";

	private final UTorrent utorrent;
	private final String hash;

	private int status;
	private String name;
	private long size;
	private int progress;
	private long downloaded;
	private long uploaded;
	private double ratio;
	private int uploadSpeed;
	private int downloadSpeed;
	private long eta;
	private String label;
	private int peersConnected;
	private int peersInSwarm;
	private int seedsConnected;
	private int seedsInSwarm;
	private int availability;
	private int order;
	private long remaining;

	public Torrent(String hash, UTorrent utorrent) {
		this.utorrent = utorrent;
		this.hash = hash;
	}

	synchronized void update(List<Object> fields) {
		this.status = ((Long) fields.get(FIELD_STATUS)).intValue();
		this.name = (String) fields.get(FIELD_NAME);
		this.size = (Long) fields.get(FIELD_SIZE);
		this.progress = ((Long) fields.get(FIELD_PROGRESS)).intValue() / 10;
		this.downloaded = (Long) fields.get(FIELD_DOWNLOADED);
		this.uploaded = (Long) fields.get(FIELD_UPLOADED);
		this.ratio = (Long) fields.get(FIELD_RATIO) / 1000;
		this.uploadSpeed = ((Long) fields.get(FIELD_UPLOAD_SPEED)).intValue();
		this.downloadSpeed = ((Long) fields.get(FIELD_DOWNLOAD_SPEED)).intValue();
		this.eta = (Long) fields.get(FIELD_ETA);
		this.label = (String) fields.get(FIELD_LABEL);
		this.peersConnected = ((Long) fields.get(FIELD_PEERS_CONNECTED)).intValue();
		this.peersInSwarm = ((Long) fields.get(FIELD_PEERS_IN_SWARM)).intValue();
		this.seedsConnected = ((Long) fields.get(FIELD_SEEDS_CONNECTED)).intValue();
		this.seedsInSwarm = ((Long) fields.get(FIELD_SEEDS_IN_SWARM)).intValue();
		this.availability = ((Long) fields.get(FIELD_AVAILABILITY)).intValue() / 10;
		this.order = ((Long) fields.get(FIELD_ORDER)).intValue();
		this.remaining = (Long) fields.get(FIELD_REMAINING);
	}

	public String getHash() {
		return hash;
	}

	public int getStatusCode() {
		return status;
	}

	public boolean isStarted() {
		return (status & STATUS_STARTED) != 0;
	}

	public boolean isChecking() {
		return (status & STATUS_CHECKING) != 0;
	}

	public boolean isStartAfterCheck() {
		return (status & STATUS_START_AFTER_CHECK) != 0;
	}

	public boolean isChecked() {
		return (status & STATUS_CHECKED) != 0;
	}

	public boolean isError() {
		return (status & STATUS_ERROR) != 0;
	}

	public boolean isPaused() {
		return (status & STATUS_PAUSED) != 0;
	}

	public boolean isQueued() {
		return (status & STATUS_QUEUED) != 0;
	}

	public boolean isLoaded() {
		return (status & STATUS_LOADED) != 0;
	}

	public boolean isCompleted() {
		return progress >= 100;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public int getProgress() {
		return progress;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public long getUploaded() {
		return uploaded;
	}

	public double getRatio() {
		return ratio;
	}

	public int getUploadSpeed() {
		return uploadSpeed;
	}

	public int getDownloadSpeed() {
		return downloadSpeed;
	}

	public long getETA() {
		return eta;
	}

	public String getLabel() {
		return label;
	}

	public int getOrder() {
		return order;
	}

	public long getRemaining() {
		return remaining;
	}

	public int getPeersConnected() {
		return peersConnected;
	}

	public int getPeersInSwarm() {
		return peersInSwarm;
	}

	public int getSeedsConnected() {
		return seedsConnected;
	}

	public int getSeedsInSwarm() {
		return seedsInSwarm;
	}

	public int getAvailability() {
		return availability;
	}

	public void start(boolean force) {
		utorrent.get(String.format(ACTION_CALL, force ? "forcestart" : "start", hash));
	}

	public void stop() {
		utorrent.get(String.format(ACTION_CALL, "stop", hash));
	}

	public void pause() {
		utorrent.get(String.format(ACTION_CALL, "pause", hash));
	}

	public void unpause() {
		utorrent.get(String.format(ACTION_CALL, "unpause", hash));
	}

	public void recheck() {
		utorrent.get(String.format(ACTION_CALL, "recheck", hash));
	}

	public void remove(boolean removeData) {
		utorrent.get(String.format(ACTION_CALL, removeData ? "removedata" : "remove", hash));
	}

	// TODO: Files
	// TODO: Properties
	// TODO: Label
	// TODO: Priority
	// TODO: Queue order

	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Torrent))
			return false;

		final Torrent t = (Torrent) o;
		return hash.equals(t.hash);
	}

	@Override
	public String toString() {
		return "torrent['" + name + "']";
	}
}
