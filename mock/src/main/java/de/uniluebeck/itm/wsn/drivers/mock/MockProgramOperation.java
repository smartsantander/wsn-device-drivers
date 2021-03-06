package de.uniluebeck.itm.wsn.drivers.mock;

import com.google.common.util.concurrent.TimeLimiter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.wsn.drivers.core.operation.OperationListener;
import de.uniluebeck.itm.wsn.drivers.core.operation.ProgramOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.TimeLimitedOperation;
import de.uniluebeck.itm.wsn.drivers.core.serialport.SerialPortProgrammingMode;

import javax.annotation.Nullable;
import java.util.Arrays;


/**
 * The operation for programming the <code>MockDevice</code>.
 *
 * @author Malte Legenhausen
 * @author Daniel Bimschas
 */
public class MockProgramOperation extends TimeLimitedOperation<Void> implements ProgramOperation {

	private final MockDevice device;

	/**
	 * The binary image that has to be written to the device.
	 */
	private final byte[] binaryImage;

	/**
	 * The configuration that will store the binary image.
	 */
	private MockConfiguration configuration;

	@Inject
	public MockProgramOperation(final TimeLimiter timeLimiter,
								final MockConfiguration configuration,
								final MockDevice device,
								@Assisted byte[] binaryImage,
								@Assisted final long timeoutMillis,
								@Assisted @Nullable final OperationListener<Void> operationCallback) {

		super(timeLimiter, timeoutMillis, operationCallback);

		this.configuration = configuration;
		this.device = device;
		this.binaryImage = binaryImage;
	}

	@Override
	@SerialPortProgrammingMode
	protected Void callInternal() throws Exception {

		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
			progress(i * 0.1f);
		}

		configuration.setFlashRom(Arrays.copyOf(binaryImage, binaryImage.length));
		device.reset();

		return null;
	}
}
