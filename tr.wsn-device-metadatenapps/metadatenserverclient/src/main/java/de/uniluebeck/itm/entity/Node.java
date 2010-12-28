package de.uniluebeck.itm.entity;


import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * A Node Entity is described.
 */

@Entity
@Table(catalog = "metadaten_db", name = "node")
public class Node {

	@Id
	@Attribute
	@Column(name = "id", nullable = false, length = 100)
	private String id;

	/**
	 * Node Defaults Elements.
	 */

	@Element(required= false)
	@Column(name = "microcontroller", length = 100)
	private String microcontroller;

	@Element(required= false)
	@Column(name = "ipAdress", length = 100)
	private String ipAddress;

	@Element(required= false)
	@Column(name = "description", length = 100)
	private String description;

	@Element
	private short port;

	@Element(required= false)
	private Date timestamp;

	/**
	 * List with the capability elements of the entity.
	 */
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "parentnode")
	// @OneToMany(mappedBy = "nodeId")
	@ElementList(inline = true)
	private List<Capability> capabilityList;

	/**
	 * Requires function for deserializing objects.
	 */
	public Node() {
		this.id="";
		this.ipAddress="";
		this.description="";
		this.microcontroller="";
		this.port=0;
		this.timestamp=new Date();
	}

	/**
	 * Constructor Method.
	 * 
	 * @param microcontoller
	 * @param ipAddress
	 * @param softwareRevision
	 * @param otapVersion
	 * @param description
	 * @param capList
	 */
	public Node(String identity, String microcontoller, String IpAddress,
			String description, List capList) {
		setId(identity);
		setMicrocontroller(microcontoller);
		setIpAddress(IpAddress);

		setDescription(description);
		setCapabilityList(capList);
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getMicrocontroller() {
		return microcontroller;
	}

	public void setMicrocontroller(String microcontroller) {
		this.microcontroller = microcontroller;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPort(short port) {
		this.port = port;
	}

	public short getPort() {
		return this.port;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public List<Capability> getCapabilityList() {
//		return capabilityList;
		if (null == capabilityList) {
	        return Collections.EMPTY_LIST;
	    }
	    return Collections.unmodifiableList(capabilityList);
	}

	public void setCapabilityList(List<Capability> capabilityList) {
		this.capabilityList = capabilityList;
	}

}
