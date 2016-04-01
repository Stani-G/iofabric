package com.iotracks.iofabric.message_bus;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.utils.BytesUtil;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

/**
 * archives received {@link Message} from {@link Element}
 * 
 * @author saeid
 *
 */
public class MessageArchive {
	private final byte HEADER_SIZE = 33;
	private final short MAXIMUM_MESSAGE_PER_FILE = 1000;
	private final int MAXIMUM_ARCHIVE_SIZE_MB = 1;

	private final String name;
	private String diskDirectory;
	private String currentFileName;
	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	
	public MessageArchive(String name) {
		this.name = name;
		init();
	}
	
	/**
	 * sets the file name for {@link Message} to be archived
	 * 
	 */
	protected void init() {
		currentFileName = "";
		diskDirectory = Configuration.getDiskDirectory() + "messages/archive/";
		
		File lastFile = null;
		long lastFileTimestamp = 0;
		final File workingDirectory = new File(diskDirectory);
		if (!workingDirectory.exists())
			workingDirectory.mkdirs();
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.substring(0, name.length()).equals(name) && fileName.substring(fileName.indexOf(".")).equals(".idx");
			}
		};
		
		for (File file : workingDirectory.listFiles(filter)) {
			if (!file.isFile())
				continue;
			String filename = file.getName();
			if (filename.substring(0, name.length()).equals(name)) {
				String timestampStr = filename.substring(name.length() + 1, filename.indexOf("."));
				long timestamp = Long.parseLong(timestampStr);
				if (timestamp > lastFileTimestamp) {
					lastFileTimestamp = timestamp;
					lastFile = file; 
				}
			}
		}
		
		if (lastFileTimestamp > 0 && lastFile.length() < ((HEADER_SIZE + Long.BYTES) * MAXIMUM_MESSAGE_PER_FILE))
			currentFileName = lastFile.getPath();
	}
	
	/**
	 * opens index and data file
	 * 
	 * @param timestamp- timestamp of first {@link Message} in the file
	 * @throws Exception
	 */
	private void openFiles(long timestamp) throws Exception {
		if (currentFileName.equals(""))
			currentFileName = diskDirectory + name + "_" + timestamp + ".idx";
		indexFile = new RandomAccessFile(new File(currentFileName), "rw");
		dataFile = new RandomAccessFile(new File(currentFileName.substring(0, currentFileName.indexOf(".")) + ".iomsg"), "rw");
	}
	
	/**
	 * archives {@link Message} to file. If size of the data file becomes more than
	 * defined value, creates a new file 
	 * 
	 * @param message - {@link Message} to be archived
	 * @param timestamp - timestamp of the {@link Message}
	 * @throws Exception
	 */
	protected void save(byte[] message, long timestamp) throws Exception {
		if (indexFile == null)
			openFiles(timestamp);
		
		if ((message.length + dataFile.length()) >= (MAXIMUM_ARCHIVE_SIZE_MB * 1_000_000)) {
			close();
			openFiles(timestamp);
		}
		indexFile.seek(indexFile.length());
		dataFile.seek(dataFile.length());
		long dataPos = dataFile.getFilePointer();
		
		indexFile.write(message, 0, HEADER_SIZE);
		indexFile.writeLong(dataPos);
		dataFile.write(message, HEADER_SIZE, message.length - HEADER_SIZE);
	}
	
	/**
	 * closes index and data files
	 * 
	 */
	public void close() {
		try {
			currentFileName = "";
			if (indexFile != null)
				indexFile.close();
			if (dataFile != null)
				dataFile.close();
			currentFileName = "";
		} catch (Exception e) {}
	}
	
	/**
	 * computes {@link Message} size
	 * 
	 * @param header - header of the {@link Message}
	 * @return int
	 */
	private int getDataSize(byte[] header) {
		int size = 0;
		size = header[2];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 3, 5));
		size += header[5];
		size += header[6];
		size += header[7];
		size += header[8];
		size += header[9];
		size += header[10];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 11, 13));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 13, 15));
		size += header[15];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 16, 18));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 18, 20));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 20, 22));
		size += header[22];
		size += header[23];
		size += header[24];
		size += BytesUtil.bytesToInteger(BytesUtil.copyOfRange(header, 25, 29));
		size += BytesUtil.bytesToInteger(BytesUtil.copyOfRange(header, 29, 33));
		return size;
	}

	/**
	 * retrieves list of {@link Message} sent by this {@link Element} within the time frame 
	 * 
	 * @param from - beginning of time frame in milliseconds
	 * @param to - end of time frame in milliseconds
	 * @return list of {@link Message}
	 */
	public List<Message> messageQuery(long from, long to) {
		List<Message> result = new ArrayList<>();
		
		File workingDirectory = new File(diskDirectory);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.substring(0, name.length()).equals(name) && fileName.substring(fileName.indexOf(".")).equals(".idx");
			}
		};
		File[] listOfFiles = workingDirectory.listFiles(filter);
		Arrays.sort(listOfFiles);
		
		Stack<File> resultSet = new Stack<>();
		int i = listOfFiles.length - 1;
		for (; i >= 0; i--) {
			File file = listOfFiles[i];
			if (!file.isFile())
				continue;
			long timestamp = Long.parseLong(file.getName().substring(name.length() + 1, file.getName().indexOf(".")));
			if (timestamp < from)
				break;
			if (timestamp >= from && timestamp <= to)
				resultSet.push(file);
		}
		if (i >= 0)
			resultSet.push(listOfFiles[i]);
		
		byte[] header = new byte[HEADER_SIZE];
		while (!resultSet.isEmpty()) {
			File file = resultSet.pop();
			String fileName = file.getName();
			try {
				RandomAccessFile indexFile = new RandomAccessFile(new File(diskDirectory + fileName), "r");
				RandomAccessFile dataFile = new RandomAccessFile(new File(diskDirectory + fileName.substring(0, fileName.indexOf(".")) + ".iomsg"), "r");
				long dataFileLength = dataFile.length();
				while (indexFile.getFilePointer() < indexFile.length()) {
					indexFile.read(header, 0, HEADER_SIZE);
					if (((header[0] * 256) + header[1]) != 4)
						throw new Exception("invalid index file format");
					long dataPos = indexFile.readLong();
					int dataSize = getDataSize(header);
					if (dataPos + dataSize > dataFileLength || dataSize > dataFileLength)
						throw new Exception("invalid data file format");
					byte[] data = new byte[dataSize];
					dataFile.read(data, 0, dataSize);
					Message message = new Message(header, data);
					if (message.getTimestamp() < from || message.getTimestamp() > to)
						continue;
					result.add(message);
				}
				indexFile.close();
				dataFile.close();
			} catch (Exception e) {
				LoggingService.logWarning("Message Archive", e.getMessage());
			}
		}
		
		return result;
	}
}
