package com.fanky10.jutorrent;

public interface TorrentListener {
	public void onComplete(Torrent torrent);
	public void onLoad(Torrent torrent);
	public void onQueue(Torrent torrent, boolean queued);
	public void onPause(Torrent torrent, boolean paused);
	public void onError(Torrent torrent);
	public void onChecking(Torrent torrent, boolean checking);
	public void onStart(Torrent torrent, boolean started);
	public void onRemove(Torrent torrent);
	public void onAdded(Torrent torrent);
}
