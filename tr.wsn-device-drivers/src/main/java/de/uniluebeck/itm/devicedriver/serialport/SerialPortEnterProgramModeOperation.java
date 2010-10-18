package de.uniluebeck.itm.devicedriver.serialport;

import gnu.io.SerialPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.devicedriver.Monitor;
import de.uniluebeck.itm.devicedriver.exception.EnterProgramModeException;
import de.uniluebeck.itm.devicedriver.operation.AbstractOperation;
import de.uniluebeck.itm.devicedriver.operation.EnterProgramModeOperation;

public class SerialPortEnterProgramModeOperation extends AbstractOperation<Void> implements EnterProgramModeOperation {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(SerialPortEnterProgramModeOperation.class);
	
	private final SerialPort serialPort;
	
	public SerialPortEnterProgramModeOperation(SerialPortConnection connection) {
		this.serialPort = connection.getSerialPort();
	}
	
	@Override
	public Void execute(Monitor monitor) throws Exception {
		log.debug("Entering program mode");
		try {
			serialPort.setDTR(true);
			monitor.onProgressChange(0.25f);
			Thread.sleep(200);
			serialPort.setRTS(true);
			monitor.onProgressChange(0.5f);
			Thread.sleep(200);
			serialPort.setDTR(false);
			monitor.onProgressChange(0.75f);
			Thread.sleep(200);
			serialPort.setRTS(false);
			monitor.onProgressChange(1.0f);
		} catch (Exception e) {
			log.error("Unable to enter program mode.");
			throw new EnterProgramModeException("Unable to enter program mode.");
		}
		log.debug("Program mode entered");
		return null;
	}

}