package de.uniluebeck.itm.tcp.server.operations;

import org.apache.shiro.subject.Subject;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import de.uniluebeck.itm.devicedriver.MessagePacket;
import de.uniluebeck.itm.devicedriver.async.OperationHandle;
import de.uniluebeck.itm.tcp.server.utils.ClientID;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.EmptyAnswer;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.sendData;

/**
 * The send Operation
 * @author Andreas Maier
 *
 */
public class SendOperation extends AbstractWriteOperation<Void> {

	/**
	 * the request of type sendData
	 */
	private sendData request = null;
	
	/**
	 * Constructor
	 * @param controller the RpcController for a send Operation
	 * @param done the RpcCallback<EmptyAnswer> for a send Operation
	 * @param user the Shiro-User-Object a send Operation
	 * @param id the ClientID-Instance for a send Operation
	 * @param request the sendData request for a send Operation
	 */
	public SendOperation(final RpcController controller, final RpcCallback<EmptyAnswer> done, final Subject user, final ClientID id, final sendData request) {
		super(controller, done, user, id, request.getOperationKey());
		this.request =  request;
	}

	@Override
	protected OperationHandle<Void> operate() {
		final MessagePacket packet = new MessagePacket(request.getType(), request.getDataList().get(0).toByteArray());
		
		// erzeugen eines OperationHandle zu der Operation
		return getDeviceAsync().send(packet, request.getTimeout(), getAsyncAdapter());
	}
	
	

}
