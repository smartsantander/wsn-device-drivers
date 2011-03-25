package de.uniluebeck.itm.rsc.drivers.isense;

import gnu.io.SerialPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.rsc.drivers.core.Monitor;
import de.uniluebeck.itm.rsc.drivers.core.operation.AbstractOperation;
import de.uniluebeck.itm.rsc.drivers.core.operation.ResetOperation;
import de.uniluebeck.itm.rsc.drivers.core.serialport.SerialPortConnection;

public class iSenseResetOperation extends AbstractOperation<Void> implements ResetOperation {
	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(iSenseResetOperation.class);
	
	private final SerialPortConnection connection;

	public iSenseResetOperation(final SerialPortConnection connection) {
		this.connection = connection;
	}

	@Override
	public Void execute(Monitor monitor) throws Exception {
		log.debug("Resetting device...");
		SerialPort serialPort = connection.getSerialPort();
		serialPort.setDTR(true);
		monitor.onProgressChange(0.5f);
		Thread.sleep(200);
		serialPort.setDTR(false);
		monitor.onProgressChange(1.0f);
		log.debug("Device resetted");
		return null;
	}

}