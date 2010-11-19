package de.uniluebeck.itm.metadaten.metadatenserver;

import java.util.concurrent.Executors;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.log4j.BasicConfigurator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;



import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerBootstrap;


import de.uniluebeck.itm.devicedriver.MacAddress;
import de.uniluebeck.itm.devicedriver.async.AsyncCallback;
import de.uniluebeck.itm.devicedriver.async.OperationHandle;
import de.uniluebeck.itm.metadaten.files.MetaDataService.Identification;
import de.uniluebeck.itm.metadaten.files.MetaDataService.MetaDataStream;
import de.uniluebeck.itm.metadaten.files.MetaDataService.Node;
import de.uniluebeck.itm.metadaten.files.MetaDataService.Operations;
import de.uniluebeck.itm.metadaten.files.MetaDataService.SearchRequest;
import de.uniluebeck.itm.metadaten.files.MetaDataService.SearchResponse;
import de.uniluebeck.itm.metadaten.files.MetaDataService.VOID;
import de.uniluebeck.itm.tr.util.TimedCache;

public class MetaDatenServer {

	//private static Log log = LogFactory.getLog(MetaDatenServer.class);
	
	// werden nach 30 min alle eintraege des Cache geloescht?
	// wie Timeout fuer einen Eintrag neu starten?
	private static TimedCache<RpcClientChannel,ClientID> idList = new TimedCache<RpcClientChannel,ClientID>();
	private static TimedCache<RpcClientChannel,Subject> authList = new TimedCache<RpcClientChannel,Subject>();

	public static void main (String[] args){
		
		//BasicConfigurator.configure();
		System.out.println("Initialisierung des Servers");
		// setzen der server-Informationen
		PeerInfo serverInfo = new PeerInfo("localhost", 8080);
		
		// setzen des ThreadPools
		 RpcServerCallExecutor executor = new ThreadPoolCallExecutor(10, 10);
	        
		 // setzen des bootstraps
		 DuplexTcpServerBootstrap bootstrap = new DuplexTcpServerBootstrap(
				 serverInfo,
				 new NioServerSocketChannelFactory(
						 Executors.newCachedThreadPool(),
						 Executors.newCachedThreadPool()),
				 executor);
	        
		 // setzen eines ConnectionLoggers
	        RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
	        RpcConnectionEventListener listener = new RpcConnectionEventListener() {
				
				@Override
				public void connectionReestablished(RpcClientChannel clientChannel) {
					System.out.println("connectionReestablished " + clientChannel);
				}
				
				@Override
				public void connectionOpened(RpcClientChannel clientChannel) {
					System.out.println("connectionOpened " + clientChannel);	
				}
				
				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					System.out.println("connectionLost " + clientChannel);
				}
				
				@Override
				public void connectionChanged(RpcClientChannel clientChannel) {
					System.out.println("connectionChanged " + clientChannel);
				}
			};
			rpcEventNotifier.setEventListener(listener);
	    	bootstrap.registerConnectionEventListener(rpcEventNotifier);
	        
	        
	    	// registrieren der benutzten Services
//	    	bootstrap.getRpcServiceRegistry().registerService(TestOperations.newReflectiveService(new TestOperationsImpl()));
	    	bootstrap.getRpcServiceRegistry().registerService(Operations.newReflectiveService(new OperationsImpl()));

	    	// starten des Servers
	    	bootstrap.bind();
	    	
	    	// ein wenig Kommunikation
	    	System.out.println("Serving started: " + bootstrap);
	    	
	    	/* Initialiesieren von Shiro */
	    	
	    	Factory<SecurityManager> factory = new IniSecurityManagerFactory("shiro.ini");
	        SecurityManager securityManager = factory.getInstance();
	        SecurityUtils.setSecurityManager(securityManager);	        
			
	}
	
//	// Testklassen
//	static class TestOperationsImpl implements TestOperations.Interface {
//
//		// setzen einer Nachricht auf dem MetaDatenServer
//		@Override
//		public void setMessage(RpcController controller, STRING request,
//				RpcCallback<VOID> done) {
//			
//			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
//			if(user==null || !user.isAuthenticated()){
//				controller.setFailed("Sie sind nicht authentifiziert!");
//				done.run(null);
//				return;
//			}
//			
//			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
//			
//			// herausfinden des TCP-Channel und finden der Userspezifischen Klasse
//			// ein Channel kann fuer mehrere Operationen offen bleiben
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			
//			// aber die handle muessen zu jeder Operation eindeutig zuweisbar sein
//			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle<Void> handle = test.setMessage();
//			
//			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
//			// setzen der Nachricht auf dem MetaDatenServer
//			id.setMessage(request.getQuery());
//			
//			// ausfuehren des Callbacks
//			done.run(VOID.newBuilder().build());
//		}
//
//		// abrufen einer Nachricht vom MetaDatenServer
//		@Override
//		public void getMessage(RpcController controller, VOID request,
//				RpcCallback<STRING> done) {
//			
//			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
//			if(user==null || !user.isAuthenticated()){
//				controller.setFailed("Sie sind nicht authentifiziert!");
//				done.run(null);
//				return;
//			}
//			
//			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
//			
//			// identifizieren des Users mit dem Channel
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle<Void> handle = test.getMessage();
//			
//			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
//			
//			// ausfuehren des Callbacks mit der Nachricht
//			done.run(STRING.newBuilder().setQuery(id.getMessage()).setOperationKey(controller.toString()).build());
//
//		}
//	}f
	
	// eigentliche Operationen, die spaeter verwendet werden sollen
	static class OperationsImpl implements Operations.Interface {
		
		// Methode zum verbinden auf den MetaDatenServer
		// hier sollte die Authentifikation stattfinden

		@Override
		public void connect(RpcController controller, Identification request,
				RpcCallback<VOID> done) {
						
			// eine Moeglichkeit den benutzten channel zu identifizieren
			RpcClientChannel channel = ServerRpcController.getRpcChannel(controller);
			
			// erzeugen einer channel bezogenen User Instanz
			ClientID id = new ClientID();
			
			// Abgleich der Userdaten
			System.out.println("Passwort wird �berpr�ft");
			/*Shiro:*/
			Subject currentUser = SecurityUtils.getSubject();
			
	        if (!currentUser.isAuthenticated()) {
	            UsernamePasswordToken token = new UsernamePasswordToken(request.getUsername(), request.getPassword());
	            token.setRememberMe(true);
	            try {
	            	
	                currentUser.login(token);
	                // eintragen der ClientID-Instanz zusammen mit den benutzten Channel in eine Liste
					idList.put(channel, id);
					authList.put(channel, currentUser);
			        // ausfuehren des Callback
			        done.run(VOID.newBuilder().build());
					
	            } catch (UnknownAccountException uae) {
	            	controller.setFailed("There is no user with username of " + token.getPrincipal());
	            	done.run(null);
	            	return;
	            } catch (IncorrectCredentialsException ice) {
	            	controller.setFailed("Password for account " + token.getPrincipal() + " was incorrect!");
	            	done.run(null);
	            	return;
	            } catch (LockedAccountException lae) {
	            	controller.setFailed("The account for username " + token.getPrincipal() + " is locked.  " +
	                        "Please contact your administrator to unlock it.");
	            	done.run(null);
	            	return;
	            } catch (AuthenticationException ae) {
	            	controller.setFailed(ae.getMessage());
	            	done.run(null);
	            	return;
	            }
	        }
			/*Shiro END*/

		}

		@Override
		public void add(RpcController controller, Node request,
				RpcCallback<VOID> done) {
			// TODO Auto-generated method stub
			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
			if(user==null || !user.isAuthenticated()){
				controller.setFailed("Sie sind nicht authentifiziert!");
				done.run(null);
				
				return;
			}
			System.out.println("Knoten mit Micocontroller: " +request.getMicrocontroller() + "wurde dem Verzeichnis zugef�gt");
//			
			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
			
			// identifizieren des Users mit dem Channel
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle <Void> handle = test.program(null, request.getTimeout(), new AsyncCallback<Void>(){
//
//				@Override
//				public void onCancel() {
//					System.out.println("Abbruch im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onFailure(Throwable throwable) {
//					System.out.println("Fehler im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onProgressChange(float fraction) {
//					System.out.println("change im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onSuccess(Void result) {
//					System.out.println("jup es geht im TCP-MetaDatenServer");
//				}});
			
			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
			
			// ausfuehren des Callbacks
//			done.run(VOID.newBuilder().build());
			
		}

		@Override
		public void remove(RpcController controller, Node request,
				RpcCallback<VOID> done) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void refresh(RpcController controller, Node request,
				RpcCallback<VOID> done) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void search(RpcController controller, SearchRequest request,
				RpcCallback<SearchResponse> done) {
			// TODO Auto-generated method stub
			
		}

//		// Methode um Device zu Programmieren
//		@Override
//		public void program(RpcController controller, ProgramPacket request,
//				RpcCallback<VOID> done) {
//			
//			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
//			if(user==null || !user.isAuthenticated()){
//				controller.setFailed("Sie sind nicht authentifiziert!");
//				done.run(null);
//				return;
//			}
//			
//			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
//			
//			// identifizieren des Users mit dem Channel
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle <Void> handle = test.program(null, request.getTimeout(), new AsyncCallback<Void>(){
//
//				@Override
//				public void onCancel() {
//					System.out.println("Abbruch im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onFailure(Throwable throwable) {
//					System.out.println("Fehler im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onProgressChange(float fraction) {
//					System.out.println("change im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onSuccess(Void result) {
//					System.out.println("jup es geht im TCP-MetaDatenServer");
//				}});
//			
//			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
//			
//			// ausfuehren des Callbacks
//			done.run(VOID.newBuilder().build());
//		}
//
//		// reagieren auf ein getState-Aufruf
//		@Override
//		public void getState(RpcController controller, VOID request,
//				RpcCallback<STRING> done) {
//			
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			OperationHandle<Void> handle = id.getHandleList(request.getOperationKey());			
//			done.run(STRING.newBuilder().setQuery(handle.getState().getName()).build());
//		}
//
//		// reagieren auf ein cancel-Aufruf
//		@Override
//		public void cancelHandle(RpcController controller, VOID request,
//				RpcCallback<VOID> done) {
//	
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			OperationHandle<Void> handle = id.getHandleList(request.getOperationKey());
//			System.out.println("canceled: "+controller.isCanceled());
//			if(controller.isCanceled()){
//				handle.cancel();
//				done.run(VOID.newBuilder().build());
//			}
//		}
//
//		// reagieren auf ein get-Aufruf
//		@Override
//		public void getHandle(RpcController controller, VOID request,
//				RpcCallback<VOID> done) {
//			
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			OperationHandle<Void> handle = id.getHandleList(request.getOperationKey());
//			handle.get();
//			done.run(VOID.newBuilder().build());
//		}
//
//		@Override
//		public void writeMac(RpcController controller, MacData request,
//				RpcCallback<VOID> done) {
//			
//			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
//			if(user==null || !user.isAuthenticated()){
//				controller.setFailed("Sie sind nicht authentifiziert!");
//				done.run(null);
//				return;
//			}
//			
//			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
//			
//			// identifizieren des Users mit dem Channel
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle <Void> handle = test.writeMac(new MacAddress(request.toByteArray()), request.getTimeout(), new AsyncCallback<Void>(){
//
//				@Override
//				public void onCancel() {
//					System.out.println("Abbruch im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onFailure(Throwable throwable) {
//					System.out.println("Fehler im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onProgressChange(float fraction) {
//					System.out.println("change im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onSuccess(Void result) {
//					System.out.println("jup es geht im TCP-MetaDatenServer");
//				}});
//			
//			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
//			
//			// ausfuehren des Callbacks
//			done.run(VOID.newBuilder().build());
//			
//		}
//
//		@Override
//		public void writeFlash(RpcController controller, FlashData request,
//				RpcCallback<VOID> done) {
//			
//			Subject user = authList.get(ServerRpcController.getRpcChannel(controller));
//			if(user==null || !user.isAuthenticated()){
//				controller.setFailed("Sie sind nicht authentifiziert!");
//				done.run(null);
//				return;
//			}
//			
//			// erstellen einer Klasse zum Testen der OperationHandle
//			Main test = new Main();
//			
//			// identifizieren des Users mit dem Channel
//			ClientID id = idList.get(ServerRpcController.getRpcChannel(controller));
//			
//			// erzeugen eines OperationHandle zur der Operation
//			OperationHandle <Void> handle = test.writeFlash(request.getAddress(),request.toByteArray(),request.getLength(),request.getTimeout(),new AsyncCallback<Void>(){
//
//				@Override
//				public void onCancel() {
//					System.out.println("Abbruch im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onFailure(Throwable throwable) {
//					System.out.println("Fehler im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onProgressChange(float fraction) {
//					System.out.println("change im TCP-MetaDatenServer");
//				}
//
//				@Override
//				public void onSuccess(Void result) {
//					System.out.println("jup es geht im TCP-MetaDatenServer");
//				}});
//			
//			// ein channel-einzigartiger OperationKey wird vom Client zu jeder Operation mitgeschickt
//			id.setHandleList(request.getOperationKey(), handle);
//			
//			// ausfuehren des Callbacks
//			done.run(VOID.newBuilder().build());
//			
//		}
//
//		@Override
//		public void eraseFlash(RpcController controller, VOID request,
//				RpcCallback<VOID> done) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void readFlash(RpcController controller, FlashData request,
//				RpcCallback<ByteData> done) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void readMac(RpcController controller, VOID request,
//				RpcCallback<MacData> done) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void reset(RpcController controller, VOID request,
//				RpcCallback<VOID> done) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void send(RpcController controller, sendData request,
//				RpcCallback<VOID> done) {
//			// TODO Auto-generated method stub
//			
//		}





		
			
		
	}
		
	
}
