package com.jamierf.jutorrent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.ardverk.coding.BencodingInputStream;
import org.ardverk.coding.BencodingOutputStream;
import org.ardverk.coding.BencodingUtils;

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
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final BencodingOutputStream out = new BencodingOutputStream(buffer);

		out.writeMap(info);

		return DigestUtils.shaHex(buffer.toString()).toUpperCase();
	}

	private final String hash;

	private final URI announce;
	private final String comment;

	private final boolean priv;

	private final String name;
	private final Collection<Chunk> chunks;
	private final long size;

	public TorrentFile(String pathname) throws IOException {
		super (pathname);

		final BencodingInputStream in = new BencodingInputStream(new FileInputStream(this), true);

		try {
			final Map<String, Object> map = in.readMap();

			if (!map.containsKey("announce"))
				throw new IOException("Torrent file must contain announce key");

			// announce: The announce URL of the tracker.
			announce = new URI((String) map.get("announce"));

			// TODO: announce-list: (optional) this is an extention to the official specification, offering backwards-compatibility. (list of lists of strings).
			// TODO: creation date: (optional) the creation time of the torrent, in standard UNIX epoch format (integer, seconds since 1-Jan-1970 00:00:00 UTC)

			// comment: (optional) free-form textual comments of the author.
			comment = map.containsKey("comment") ? (String) map.get("comment") : "";

			// TODO: created by: (optional) name and version of the program used to create the .torrent.
			// TODO: encoding:  (optional) the string encoding format used to generate the pieces part of the info dictionary in the .torrent metafile.

			if (!map.containsKey("info"))
				throw new IOException("Torrent file must contain info key");

			@SuppressWarnings("unchecked")
			final Map<String, Object> info = (Map<String, Object>) map.get("info");

			// TODO: piece length: number of bytes in each piece.
			final int pieceLength = ((BigInteger) info.get("piece length")).intValue();

			// TODO: pieces: string consisting of the concatenation of all 20-byte SHA1 hash values, one per piece (byte string, i.e. not urlencoded).
			final String pieces = (String) info.get("pieces");

			// private:  (optional) If true, the client MUST publish its presence to get other peers ONLY via the trackers explicitly described in the metainfo file.
			priv = info.containsKey("private") ? info.get("private").equals(BencodingUtils.TRUE) : false;

			// Generate the torrent hash
			hash = TorrentFile.hash(info);

			chunks = new LinkedList<Chunk>();

			// Multiple file mode
			if (info.containsKey("files")) {
				// name: the file path of the directory in which to store all the files. This is purely advisory.
				name = (String) info.get("name");

				int size = 0;

				@SuppressWarnings("unchecked")
				final List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
				for (Map<String, Object> f : files) {
					@SuppressWarnings("unchecked")
					final String path = StringUtils.join((List<String>) f.get("path"), File.pathSeparator);
					final long length = ((BigInteger) f.get("length")).longValue();

					final String md5sum = (String) f.get("md5sum");
					final byte[] checksum = md5sum == null ? new byte[0] : Hex.decodeHex(md5sum.toCharArray());

					chunks.add(new Chunk(
						path,
						length,
						checksum
					));

					size += length;
				}

				this.size = size;
			}
			// Single file mode
			else {
				// name: the filename. This is purely advisory.
				name = (String) info.get("name");
				size = ((BigInteger) info.get("length")).longValue();

				final String md5sum = (String) info.get("md5sum");
				final byte[] checksum = md5sum == null ? new byte[0] : Hex.decodeHex(md5sum.toCharArray());

				chunks.add(new Chunk(
					name,
					size,
					checksum
				));
			}
		}
		catch (URISyntaxException e) {
			throw new IOException("Malformed announce URI: " + e.getMessage());
		}
		catch (DecoderException e) {
			throw new IOException("Malformed md5sum: " + e.getMessage());
		}
		finally {
			in.close();
		}
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
		return "torrent['" + name + "']";
	}
}
