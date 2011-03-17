package de.uniluebeck.itm.tcp.server.operations;

import org.apache.shiro.subject.Subject;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import de.uniluebeck.itm.devicedriver.ChipType;
import de.uniluebeck.itm.devicedriver.async.OperationHandle;
import de.uniluebeck.itm.tcp.server.utils.ClientID;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.ChipData;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.EmptyAnswer;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.ReverseAnswer;
import de.uniluebeck.itm.tcp.server.utils.MessageServiceFiles.Timeout;

/**
 * the getChipType Operation
 * @author Andreas Maier
 *
 */
public class GetChipTypeOperation extends AbstractReadOperation<ChipType> {

	/**
	 * the request of type Timeout
	 */
	private Timeout request = null;
	
	/**
	 * Constructor
	 * @param controller the RpcController for a getChipType Operation
	 * @param done the RpcCallback<EmptyAnswer> for a getChipType Operation
	 * @param user the Shiro-User-Object a getChipType Operation
	 * @param id the ClientID-Instance for a getChipType Operation
	 * @param request the Timeout request for a getChipType Operation
	 */
	public GetChipTypeOperation(final RpcController controller, final RpcCallback<EmptyAnswer> done, final Subject user, final ClientID id, final Timeout request) {
		super(controller, done, user, id, request.getOperationKey());
		this.request =  request;
	}

	@Override
	/* ueberschreiben der standard OnSuccess-Methode, da hier ein ChipType als Antwort benoetigt wird */
	public void setOnSuccess(final ChipType result) {
		// ausfuehren des Callbacks
		if(!getId().getCalledGet(request.getOperationKey())){
			final ChipData chipData = ChipData.newBuilder().setOperationKey(request.getOperationKey()).setType(result.name()).build();
			getMessage().reverseSuccess(ReverseAnswer.newBuilder().setChipData(chipData).build());
		}
	}

	@Override
	protected OperationHandle<ChipType> operate() {
		return getDeviceAsync().getChipType(request.getTimeout(),getAsyncAdapter());
	}
}
