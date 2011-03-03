package de.uniluebeck.itm.datenlogger;

import java.io.IOException;

import de.uniluebeck.itm.devicedriver.ConnectionEvent;
import de.uniluebeck.itm.devicedriver.ConnectionListener;
import de.uniluebeck.itm.devicedriver.Device;
import de.uniluebeck.itm.devicedriver.MessagePacket;
import de.uniluebeck.itm.devicedriver.MessagePacketListener;
import de.uniluebeck.itm.devicedriver.PacketType;
import de.uniluebeck.itm.devicedriver.async.DeviceAsync;
import de.uniluebeck.itm.devicedriver.async.OperationQueue;
import de.uniluebeck.itm.devicedriver.async.QueuedDeviceAsync;
import de.uniluebeck.itm.devicedriver.async.thread.PausableExecutorOperationQueue;
import de.uniluebeck.itm.devicedriver.generic.iSenseSerialPortConnection;
import de.uniluebeck.itm.devicedriver.jennic.JennicDevice;
import de.uniluebeck.itm.devicedriver.mockdevice.MockConnection;
import de.uniluebeck.itm.devicedriver.mockdevice.MockDevice;
import de.uniluebeck.itm.devicedriver.pacemate.PacemateDevice;
import de.uniluebeck.itm.devicedriver.serialport.SerialPortConnection;
import de.uniluebeck.itm.devicedriver.telosb.TelosbDevice;
import de.uniluebeck.itm.devicedriver.telosb.TelosbSerialPortConnection;
import de.uniluebeck.itm.tcp.client.RemoteConnection;
import de.uniluebeck.itm.tcp.client.RemoteDevice;

/**
 * Class Datalogger. Functions to registrate a datalogger on a sensornode and
 * print the messages on the console or in a file.
 */
public class Datalogger {

	private String port;
	private String server;
	private String user;
	private String password;
	private boolean started = false;
	private String device_parameter;
	private DeviceAsync deviceAsync;
	private MessagePacketListener listener;
	private String id;
	private PausableWriter writer;
	private RemoteConnection connection = null;

	/**
	 * Instantiates a new datalogger.
	 */
	public Datalogger(PausableWriter writer, String user, String password, String port, String server, String device, String id) {
		this.writer = writer;
		this.user = user;
		this.password = password;
		this.port = port;
		this.server = server;
		this.device_parameter = device;
		this.id = id;
	}
	
	public Datalogger(){
		
	}

	public boolean isStarted() {
		return started;
	}

	public PausableWriter getWriter() {
		return writer;
	}

	public void setWriter(PausableWriter writer) {
		this.writer = writer;
	}

	/**
	 * Connect. Method to connect to the tcp-server or to a local sensornode.
	 */
	public void connect() {
		if (server != null) {
			// Connect to the TCP-Server.
			connection = new RemoteConnection();

			connection.connect(user + ":" + password + "@" + server
					+ ":" + port + "/" + id);
			System.out.println("Connected");

			deviceAsync = new RemoteDevice(connection);
		} else {
			if (device_parameter != null) {
				final OperationQueue queue = new PausableExecutorOperationQueue();
				Device<?> device = null;
				if (device_parameter.equals("jennec")) {
					// Connect to the local jennec-device.
					SerialPortConnection jennic_connection = new iSenseSerialPortConnection();
					jennic_connection.addListener(new ConnectionListener() {
						@Override
						public void onConnectionChange(ConnectionEvent event) {
							if (event.isConnected()) {
								System.out
										.println("Connection established with port "
												+ event.getUri());
							}
						}
					});
					device = new JennicDevice(jennic_connection);
					jennic_connection.connect(port);
				} else if (device_parameter.equals("pacemate")) {
					// Connect to the local pacemate-device.
					SerialPortConnection pacemate_connection = new iSenseSerialPortConnection();
					pacemate_connection.addListener(new ConnectionListener() {
						@Override
						public void onConnectionChange(ConnectionEvent event) {
							if (event.isConnected()) {
								System.out
										.println("Connection established with port "
												+ event.getUri());
							}
						}
					});
					device = new PacemateDevice(pacemate_connection);
					pacemate_connection.connect(port);
				} else if (device_parameter.equals("telosb")) {
					// Connect to the local telosb-device
					SerialPortConnection telosb_connection = new TelosbSerialPortConnection();
					telosb_connection.addListener(new ConnectionListener() {
						@Override
						public void onConnectionChange(ConnectionEvent event) {
							if (event.isConnected()) {
								System.out
										.println("Connection established with port "
												+ event.getUri());
							}
						}
					});
					device = new TelosbDevice(telosb_connection);
					telosb_connection.connect(port);
				}else if(device_parameter.equals("mock")){
					final MockConnection connection = new MockConnection();
					device = new MockDevice(connection);
					connection.connect("MockPort");
					System.out.println("Connected");
				}
				deviceAsync = new QueuedDeviceAsync(queue, device);
			}
		}
	}
	
	/**
	 * Startlog. Registers a message packet listener on the connected device and
	 * handles the incoming data.
	 */
	public void startlog() {
		started = true;

		System.out.println("Message packet listener added");
		listener = new MessagePacketListener() {
			@Override
			public void onMessagePacketReceived(
					de.uniluebeck.itm.devicedriver.event.MessageEvent<MessagePacket> event) {
				writer.write(event.getMessage().getContent(), event.getMessage().getType());
			}
		};
		deviceAsync.addListener(listener, PacketType.LOG);
	}

	/**
	 * Stoplog. Remove the registered Listener and close the writer.
	 */
	public void stoplog() {
		deviceAsync.removeListener(listener);
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Error while closing the writer.");
		}
		started = false;
		if(connection != null){
			connection.shutdown(false);
		}
		System.out.println("\nEnd of Logging.");
	}
}