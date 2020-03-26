// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libogg;

public class ogg_packet {

	public byte[] packet;	// unsigned char *packet;
	public int bytes;	// long
	public int b_o_s;	// long
	public int e_o_s;	// long

	public int granulepos;	// ogg_int64_t

	public int packetno;	// ogg_int64_t

	// sequence number for decode; the framing knows where there's a hole in the data, but we need
	// coupling so that the codec (which is in a seperate abstraction layer) also knows about the gap


	public ogg_packet() {}

}
