package de.uniluebeck.itm.datenlogger;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The Class ByteFileWriter. Overrides the write-method of the FileWriter to
 * write the content in bytes.
 */
public class ByteFileWriter extends AbstractFileWriter {

	FileOutputStream writer;

	/*
	 * @see de.uniluebeck.itm.datenlogger.AbstractFileWriter#write(byte[], int)
	 */
	@Override
	public void write(final byte[] content, final int messageType) {
		try {
			String temp = "";
			for (byte a : content) {
				temp += a + " ";
			}
			temp += "\n";
			writer.write(temp.getBytes());
		} catch (IOException e) {
			System.out.println("Cannot write to file.");
		}
	}

	/*
	 * @see
	 * de.uniluebeck.itm.datenlogger.AbstractFileWriter#setLocation(java.lang
	 * .String)
	 */
	@Override
	public void setLocation(final String location) {
		try {
			this.writer = new FileOutputStream(location);
		} catch (IOException e) {
			System.out.println("FileOutputStream could not be created.");
		}
	}

	/*
	 * @see de.uniluebeck.itm.datenlogger.AbstractFileWriter#convert(byte[])
	 */
	@Override
	public String convert(final byte[] content) {
		return "";
	}

	/*
	 * @see de.uniluebeck.itm.datenlogger.AbstractFileWriter#close()
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}

}