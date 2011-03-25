package de.uniluebeck.itm.metadatenservice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.metadaten.metadatenservice.metadatacollector.IMetaDataCollector;
import de.uniluebeck.itm.metadaten.serverclient.metadataclienthelper.ConfigReader;
import de.uniluebeck.itm.metadatenservice.config.ConfigData;
import de.uniluebeck.itm.metadatenservice.config.Node;

public class MetaDatenService extends TimerTask implements iMetaDatenService {
	/**Logger*/
	private static Logger log = LoggerFactory.getLogger(MetaDatenService.class);
	private ClientStub stub = null;
	private List<IMetaDataCollector> collector = new ArrayList<IMetaDataCollector>();
	private ConfigData config = new ConfigData();
	private File sensors;
	Timer timer = new Timer();
	int count = 0;

	public MetaDatenService(File configpath, File sensors) throws Exception {
		this.sensors = sensors;
		config = ConfigReader.readConfigFile(configpath);
		log.info("remove old data of this TCP-Server" + config.getPassword());
		stub = new ClientStub(config.getUsername(), config.getPassword(),
				config.getServerIP(), config.getServerPort().intValue(), config
						.getClientPort().intValue());
		try {
			removeData();
		} catch (final NullPointerException npe) {
			log.error(npe.getMessage(),npe);
		}

		// nach 2 Sek gehts los
		// timer.schedule ( new Task(), 2000 );

		// nach 1 Sek gehts los und dann alle 5 Sekunden
		timer.schedule(this, 10000, config.getUpdateRate().intValue());
	}

	@Override
	public void run() {
		log.info("start refreshrun - connect");
		count = 0;
		try {
			stub.connect(config.getUsername(), config.getPassword());
			log.info("Refreshrun connected");
			for (int i = 0; i < collector.size(); i++) {
				refreshNodeSync(collector.get(i).collect(sensors));
				log.info("Node with ID: "
						+ collector.get(i).collect(sensors).getNodeid()
						+ "refreshed in directory");
			}
		} catch (Exception e) {
			log.error("xxx" + e.getMessage());
		}
		log.info("refreshrun disconnect:");
		try {
			stub.disconnect();
		} catch (final NullPointerException npe) {
			log.error(npe.getMessage(),npe);
		}
		log.info("refreshrun disconnected:");
	}


	// /**
	// * Load of ConfigData needed for communication
	// *
	// * @param fileurl
	// * @return
	// */
	// public ConfigData loadConfig(File source) {
	// ConfigData config = new ConfigData();
	// Serializer serializer = new Persister();
	// log.debug("ConfigFile:" + source.getName() + source.toString());
	// try {
	// config = serializer
	// .read(de.uniluebeck.itm.metadaten.metadatenservice.entity.ConfigData.class,
	// source);
	// // serializer.read(ConfigData, source);
	// } catch (Exception e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// log.debug("Config:" + config.getPassword() + config.getServerIP()
	// + config.getUsername() + config.getServerPort()
	// + config.getClientport());
	// return config;
	// }
	//

	/**
	 * Adds an node to the directory - no existing connection needed
	 */
	@Override
	public void addNode(Node node, final AsyncCallback<String> callback) {
		// stub.connect(config.getUsername(), config.getPassword());
		stub.add(node, new AsyncCallback<String>() {
			@Override
			public void onCancel() {
			}

			@Override
			public void onFailure(Throwable throwable) {
				callback.onFailure(throwable);
				// stub.disconnect();
			}

			@Override
			public void onSuccess(String result) {
				callback.onSuccess(result);
				// stub.disconnect();

			}

			@Override
			public void onProgressChange(float fraction) {

			}
		});
	}

	@Override
	public void removeNode(Node node, AsyncCallback<String> callback) {
		// TODO Clientseitige Implementierung

	}

	/**
	 * Refreshes the Nodeentry in the directory - Needs a existing connection to
	 * the server
	 */
	@Override
	public void refreshNode(Node node, final AsyncCallback<String> callback) {
		stub.refresh(node, new AsyncCallback<String>() {
			@Override
			public void onCancel() {
			}

			@Override
			public void onFailure(Throwable throwable) {
				log.error(throwable.getMessage());
				callback.onFailure(throwable);

			}

			@Override
			public void onSuccess(String result) {
				callback.onSuccess(result);

			}

			@Override
			public void onProgressChange(float fraction) {

			}
		});
	}

	/**
	 * Refreshes the Nodeentry in the directory - Needs a existing connection to
	 * the server Uses sync-Operation
	 */
	@Override
	public void refreshNodeSync(Node node) {
		stub.refreshSync(node);
	}

	@Override
	public void removeData() {
		stub.connect(config.getUsername(), config.getPassword());
		stub.removeAllData();
		stub.disconnect();

	}

	public List<IMetaDataCollector> getCollector() {
		return collector;
	}

	public void setCollector(List<IMetaDataCollector> collector) {
		this.collector = collector;
	}

	@Override
	public void addMetaDataCollector(IMetaDataCollector mdcollector) {
		collector.add(mdcollector);
		// stub.connect(config.getUsername(), config.getPassword());
		// stub.add(mdcollector.collect(config.getWisemlFile()), new
		// AsyncCallback<String>(){
		// @Override
		// public void onCancel() {
		// }
		// @Override
		// public void onFailure(Throwable throwable) {
		// System.out.println(throwable.getMessage());
		// // stub.disconnect();
		// }
		// @Override
		// public void onSuccess(String result) {
		// log.info("Gesendet");
		// stub.disconnect();
		// log.info("Gesendet und getrennt");
		// }
		// @Override
		// public void onProgressChange(float fraction) {
		//
		// }});
		// log.info("und wieder neu connecten");
	}

	@Override
	public void removeMetaDataCollector(IMetaDataCollector mdcollector) {
		collector.remove(mdcollector);
	}

}