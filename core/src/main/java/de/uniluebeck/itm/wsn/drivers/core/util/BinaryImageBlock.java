/**********************************************************************************************************************
 * Copyright (c) 2010, coalesenses GmbH                                                                               *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the coalesenses GmbH nor the names of its contributors may be used to endorse or promote     *
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.wsn.drivers.core.util;



/**
 * A data block of a binary file that is intended to be written to a device's flash memory.
 * The data block consists of the address within flash memory that the data should be written to, and
 * a portion of actual data. The size of the data portion depends on the device type and
 * hence on the type of bin file that provides the data block for writing.
 * 
 * @author Friedemann Wesner
 */
public class BinaryImageBlock {
	
	/**
	 * Address in flash memory that this segment will be written to.
	 */
	private final int address;
	
	/**
	 * Actual block of data bytes of the segment.
	 */
	private final byte[] data;
	
	/**
	 * Constructor.
	 * 
	 * @param address Start address of the data block.
	 * @param data The data for this block.
	 */
	public BinaryImageBlock(final int address, final byte[] data) {
		this.address = address;
		if (data == null) {
			this.data = new byte[0];
		} else {
			this.data = data;
		}
	}
	
	/**
	 * Getter for the block starting address.
	 * 
	 * @return The address.
	 */
	public int getAddress() {
		return address;
	}
	
	/**
	 * Getter for the data of the block.
	 * 
	 * @return The data as byte array.
	 */
	public byte[] getData() {
		return data;
	}
}
