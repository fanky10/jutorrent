package com.fanky10.jutorrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TorrentList extends HashMap<String, Torrent> {

	private static final Logger logger = LoggerFactory.getLogger(TorrentList.class);

	private final UTorrent utorrent;
	private final ExecutorService executor;
	private final List<TorrentListener> listeners;

	private String identifier;

	public TorrentList(UTorrent utorrent, ExecutorService executor) {
		this.utorrent = utorrent;
		this.executor = executor;

		listeners = new ArrayList<TorrentListener>();

		identifier = null;
	}

	void addListener(TorrentListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	void removeListener(TorrentListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@SuppressWarnings("unchecked")
	synchronized void update() {
		final Map<String, Object> map = utorrent.getMap("list=1" + (identifier == null ? "" : "&cid=" + identifier));

		// TODO: Labels

		List<List<Object>> torrents =  null;
		if (map.containsKey("torrents"))
			torrents = (List<List<Object>>) map.get("torrents");
		else if (map.containsKey("torrentp"))
			torrents = (List<List<Object>>) map.get("torrentp");

		// If we have some new or modified torrents, add/update them
		if (torrents != null) {
			for (List<Object> fields : torrents) {
				final String hash = (String) fields.get(Torrent.FIELD_HASH);

				Torrent torrent = super.get(hash);
				boolean added = false;

				if (torrent == null) {
					torrent = new Torrent(hash, utorrent);
					added = true;
					super.put(hash, torrent);
				}

				if (added)
					this.onAdded(torrent, fields);
				else
					this.onUpdate(torrent, fields);
			}
		}

		// If we have any removed torrents, remove them
		if (map.containsKey("torrentm")) {
			final List<Object> removed = (List<Object>) map.get("torrentm");

			for (Object hash : removed) {
				final Torrent torrent = super.remove(hash);
				if (torrent == null)
					continue;

				this.onRemove(torrent);
			}
		}

		identifier = (String) map.get("torrentc");
	}

	private void onAdded(final Torrent torrent, List<Object> fields) {
		// Update first otherwise we wont have a name!
		torrent.update(fields);

		// Only trigger events if this isn't the initial query
		if (identifier == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("Added {}", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onAdded(torrent);
					}
				});
			}
		}

		this.handleStatusChange(torrent, torrent.getStatusCode());
	}

	private void onUpdate(Torrent torrent, List<Object> fields) {
		// Note the old status code to check for changes
		final int oldStatus = torrent.getStatusCode();
		final boolean oldCompleted = torrent.isCompleted();

		// Update
		torrent.update(fields);

		// Only trigger events if this isn't the initial query
		if (identifier == null)
			return;

		final int change = Math.abs(torrent.getStatusCode() - oldStatus);
		this.handleStatusChange(torrent, change);

		// We just completed
		if (!oldCompleted && torrent.isCompleted())
			this.onComplete(torrent);
	}

	private void handleStatusChange(Torrent torrent, int change) {
		if (change <= 0)
			return;

		if ((change & Torrent.STATUS_LOADED) != 0) {
			if (torrent.isLoaded())
				this.onLoad(torrent);
		}

		if ((change & Torrent.STATUS_QUEUED) != 0) {
			this.onQueue(torrent, torrent.isQueued());
		}

		if ((change & Torrent.STATUS_PAUSED) != 0) {
			this.onPause(torrent, torrent.isPaused());
		}

		if ((change & Torrent.STATUS_ERROR) != 0) {
			if (torrent.isError())
				this.onError(torrent);
		}

		if ((change & Torrent.STATUS_CHECKED) != 0) {

		}

		if ((change & Torrent.STATUS_START_AFTER_CHECK) != 0) {

		}

		if ((change & Torrent.STATUS_CHECKING) != 0) {
			this.onChecking(torrent, torrent.isChecking());
		}

		if ((change & Torrent.STATUS_STARTED) != 0) {
			this.onStart(torrent, torrent.isStarted());
		}
	}

	private void onRemove(final Torrent torrent) {
		if (logger.isDebugEnabled())
			logger.debug("Removed {}", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onRemove(torrent);
					}
				});
			}
		}
	}

	private void onStart(final Torrent torrent, final boolean started) {
		if (logger.isDebugEnabled())
			logger.debug("{} {}", started ? "Started" : "Stopped", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onStart(torrent, started);
					}
				});
			}
		}
	}

	private void onChecking(final Torrent torrent, final boolean checking) {
		if (logger.isDebugEnabled())
			logger.debug("{} {}", checking ? "Checking" : "Checked", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onChecking(torrent, checking);
					}
				});
			}
		}
	}

	private void onError(final Torrent torrent) {
		if (logger.isDebugEnabled())
			logger.debug("Error {}", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onError(torrent);
					}
				});
			}
		}
	}

	private void onPause(final Torrent torrent, final boolean paused) {
		if (logger.isDebugEnabled())
			logger.debug("{} {}", paused ? "Paused" : "Resumed", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onPause(torrent, paused);
					}
				});
			}
		}
	}

	private void onQueue(final Torrent torrent, final boolean queued) {
		if (logger.isDebugEnabled())
			logger.debug("{} {}", queued ? "Queued" : "Unqueued", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onQueue(torrent, queued);
					}
				});
			}
		}
	}

	private void onLoad(final Torrent torrent) {
		if (logger.isDebugEnabled())
			logger.debug("Loaded {}", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onLoad(torrent);
					}
				});
			}
		}
	}

	private void onComplete(final Torrent torrent) {
		if (logger.isDebugEnabled())
			logger.debug("Completed {}", torrent);

		synchronized (listeners) {
			for (final TorrentListener listener : listeners) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onComplete(torrent);
					}
				});
			}
		}
	}
}
