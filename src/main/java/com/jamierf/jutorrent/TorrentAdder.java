package com.jamierf.jutorrent;

import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;

class TorrentAdder {

	private final UTorrent utorrent;

	private final CountDownLatch latch;
	private final TorrentListener listener;

	private Torrent result;

	public TorrentAdder(UTorrent utorrent, final TorrentFile file) throws FileNotFoundException {
		this.utorrent = utorrent;

		latch = new CountDownLatch(1);
		listener = new SimpleTorrentListener() {
			@Override
			public void onAdded(Torrent torrent) {
				if (file.getHash().equals(torrent.getHash())) {
					result = torrent;
					latch.countDown();
				}
			}
		};

		result = null;

		utorrent.addListener(listener);

		utorrent._addTorrent(file);
	}

	public Torrent get() {
		try { latch.await(); } catch (InterruptedException e) { }

		utorrent.removeListener(listener);

		return result;
	}
}
