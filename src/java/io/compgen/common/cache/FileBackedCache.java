package io.compgen.common.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;

import io.compgen.common.io.DataIO;

//
// Log-file format:
//
// uint32 key_len
// byte[key_len] key
// uint32 value_len
// byte[value_len] value
//
// binary and concatenated (LittleEndian)
//
// removed entries are added at the end with zero length for the value
// eventually, there should be a vacuum command to rewrite the log to remove
// deleted entries
//
// Note: This is better used in the context of a TieredCache with a LRUCache in front.

public class FileBackedCache<K extends Serializable,V extends Serializable> implements Cache<K, V> {

	protected final RandomAccessFile raf;
	protected boolean compress;
	protected boolean timestamp;
	protected long maxAge=-1;
	
	// index is stored in memory
	// key value-start length
	
	protected Map<K, Long> index = new HashMap<K, Long>();
	
	public FileBackedCache(String filename) throws IOException {
		this(new File(filename), false, false);
	}
	public FileBackedCache(File file) throws IOException {
		this(file, false, false);
	}
	public FileBackedCache(String filename, boolean compress) throws IOException {
		this(new File(filename), compress, false);
	}
	public FileBackedCache(String filename, boolean compress, boolean timestamp) throws IOException {
		this(new File(filename), compress, timestamp);
	}
	public FileBackedCache(File file, boolean compress, boolean timestamp) throws IOException {
		this(file, compress, timestamp, -1);
	}

	public FileBackedCache(File file, boolean compress) throws IOException {
		this(file, compress, false);
	}
	
	public FileBackedCache(String filename, boolean compress, boolean timestamp, long maxAgeSecs) throws IOException {
		this(new File(filename), compress, timestamp, maxAgeSecs);
	}
	
	public FileBackedCache(File file, boolean compress, boolean timestamp, long maxAgeSecs) throws IOException {
		
		this.maxAge = maxAgeSecs;

		if (!file.exists()) {
			this.compress = compress;
			this.timestamp = timestamp;
			this.raf = new RandomAccessFile(file, "rw");
			writeHeader();
		} else {
			this.raf = new RandomAccessFile(file, "rw");
			read();
		}
	}
	
	private void read() throws IOException {
		raf.seek(0);
		byte[] magic = DataIO.readRawBytes(raf, 4);
		assert magic[0] == 'C';
		assert magic[1] == 'G';
		assert magic[2] == 'C';
		assert magic[3] == 1;
		
//		System.err.println("magic: " + StringUtils.byteArrayToString(magic));
		
		long headerLen = DataIO.readUint32(raf);
//		System.err.println("headerLen: " + headerLen);
		byte compressByte = (byte) DataIO.readByte(raf);
		this.compress = (compressByte & 0x1) == 0x1;
		this.timestamp = (compressByte & 0x2) == 0x2;
//		System.err.println("compress: " + compressByte);
		
		raf.seek(4 + headerLen + 4);

		try {
		
			while (raf.getFilePointer() < raf.length()) {
				boolean expired = false;
				long pos = raf.getFilePointer();
//				System.err.println(pos);
				if (timestamp) {
					long tstamp = DataIO.readUint64(raf);
					
					if (maxAge > 0) {
						long ageMillis = System.currentTimeMillis() - tstamp;
						if (maxAge * 1000 < ageMillis) {
							expired = true;
						}
					}				
				}
				long keyLenL = DataIO.readUint32(raf);
				if (keyLenL > 0x7FFFFFFF) {
					System.err.println("Error! key too big!");
				}
				int keyLen = (int) (keyLenL & 0x7FFFFFFF);
//				System.err.println("  key_len: "+ keyLenL+" => (int) " + keyLen);
				byte[] keyBytes = DataIO.readRawBytes(raf, keyLen);
//				System.err.println("    key[]: "+ keyBytes);

				long valLenL = DataIO.readUint32(raf);
				if (valLenL > 0x7FFFFFFF) {
					System.err.println("Error! value too big!");
				}

				int valLen = (int) (valLenL & 0x7FFFFFFF);
//				System.err.println("  val_len: "+ valLen);
				
				raf.skipBytes(valLen);
				
				ByteArrayInputStream bis = new ByteArrayInputStream(keyBytes);
				ObjectInputStream ois;
				if (this.compress) {
						ois = new ObjectInputStream(new DeflaterInputStream(bis));
				} else {
					ois = new ObjectInputStream(bis);
				}
				@SuppressWarnings("unchecked")
				K key = (K) ois.readObject();
				ois.close();

				if (!expired) {
					if (valLen == 0) {
						index.put(key,  null);
					} else {
						index.put(key,  pos);
					}
				}
				
//				System.err.println("Found key: " + key + ", pos: "+pos+", val_len: "+valLen);
				
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
	public void close() throws IOException {
		this.raf.close();
	}
	
	private void writeHeader() throws IOException {
		raf.seek(0);
		// magic
		DataIO.writeRawBytes(raf, new byte[]{'C','G','C',1});
		
		// header_len
		DataIO.writeUint32(raf, 1);
		
		// compressed?
		int flag = 0;
		if (compress) {
			flag |= 0x1;
		}
		if (timestamp) {
			flag |= 0x2;
		}
		DataIO.writeRawByte(raf,(byte)flag);
	}
	
	public void put(K key, V val) {		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			if (compress) {
					oos = new ObjectOutputStream(new DeflaterOutputStream(bos));
			} else {
				oos = new ObjectOutputStream(bos);
			}
			oos.writeObject(key);
			oos.flush();
			oos.close();
	
			byte[] keybytes = bos.toByteArray();
			raf.seek(raf.length());
			
			byte[] valuebytes;
			
			if (val == null) {
				// remove from cache
				index.put(key, null);
				valuebytes = new byte[0];

			} else {
				bos = new ByteArrayOutputStream();
				if (compress) {
					oos = new ObjectOutputStream(new DeflaterOutputStream(bos));
				} else {
					oos = new ObjectOutputStream(bos);
				}
				oos.writeObject(val);
				oos.flush();
				oos.close();
				
				valuebytes = bos.toByteArray();
				index.put(key, raf.getFilePointer());
			}				

			if (timestamp) {
				DataIO.writeUint64(raf, System.currentTimeMillis());
			}
			
			DataIO.writeUint32(raf, keybytes.length   & 0x7FFFFFFF);
			DataIO.writeRawBytes(raf, keybytes);
			DataIO.writeUint32(raf, valuebytes.length & 0x7FFFFFFF);
			DataIO.writeRawBytes(raf, valuebytes);
//			System.err.println("Writing key ("+keybytes.length+"): " + StringUtils.byteArrayToString(keybytes) + ", pos: "+raf.length()+", val_len: "+valuebytes.length);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public V remove(K k) {
		V val = null;
		if (containsKey(k)) {
			val = get(k);
			put(k,null);
		}
		return val;
	}

	@Override
	public V get(K k) {
		if (!containsKey(k)) {
			return null;
		}
		try {
			Long pos = index.get(k);
			raf.seek(pos);
			long tstamp=0;
//			System.err.println(pos);
			if (timestamp) {
				tstamp = DataIO.readUint64(raf);
				
				if (maxAge > 0) {
					long ageMillis = System.currentTimeMillis() - tstamp;
					if (maxAge * 1000 < ageMillis) {
						// timed out
						put(k, null);
						return null;
					}
				}				
			}
			int keyLen = (int) (DataIO.readUint32(raf) & 0x7FFFFFFF);
			raf.skipBytes(keyLen);
			int valLen = (int) (DataIO.readUint32(raf) & 0x7FFFFFFF);
			byte[] valBytes = DataIO.readRawBytes(raf, valLen);
			
			ByteArrayInputStream bis = new ByteArrayInputStream(valBytes);
			ObjectInputStream ois;
			if (compress) {
					ois = new ObjectInputStream(new DeflaterInputStream(bis));
			} else {
				ois = new ObjectInputStream(bis);
			}
			@SuppressWarnings("unchecked")
			V val = (V) ois.readObject();
//			System.err.println("Found value: " + k + ", pos: "+pos+", value: "+val);
			ois.close();
			return val;
			
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clear() {
		for (K k: index.keySet()) {
			remove(k);
		}
	}

	@Override
	public boolean containsKey(K k) {
		return index.containsKey(k) && index.get(k)!=null;
	}
}
