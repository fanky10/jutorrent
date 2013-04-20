package com.fanky10.jutorrent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("serial")
public class TorrentFile extends File {

	public static class Chunk {
		private final String name;
		private final long length;
		private final byte[] checksum;

		public Chunk(String name, long length, byte[] checksum) {
			this.name = name;
			this.length = length;
			this.checksum = checksum;
		}

		public String getName() {
			return name;
		}

		public long getLength() {
			return length;
		}

		public byte[] getChecksum() {
			return checksum;
		}

		@Override
		public String toString() {
			return "chunk['" + name + "']";
		}
	}

	private static String hash(Map<String, Object> info) throws IOException {
		return null;
	}

	private static String getString(Object o)
			throws UnsupportedEncodingException {
		return null;
	}

	private final String hash = null;

	private final URI announce = null;
	private final String comment = null;

	private final boolean priv = true;

	private final String name = null;
	private final Collection<Chunk> chunks = null;
	private final long size = 0L;

	public TorrentFile(File file) throws IOException {
		this(file.getAbsolutePath());
	}

	public TorrentFile(String pathname) throws IOException {
		super(pathname);
	}

	public String getHash() {
		return hash;
	}

	public URI getAnnounce() {
		return announce;
	}

	public String getComment() {
		return comment;
	}

	public boolean isPrivate() {
		return priv;
	}

	@Override
	public String getName() {
		return name;
	}

	public Collection<Chunk> getChunks() {
		return chunks;
	}

	public int getNumFiles() {
		return chunks.size();
	}

	public long getTotalSize() {
		return size;
	}

	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Torrent))
			return false;

		final TorrentFile tf = (TorrentFile) o;
		return hash.equals(tf.hash);
	}

	@Override
	public String toString() {
		return "torrentfile['" + name + "']";
	}
}
