// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libogg;

public class ogg_sync_state {

	byte[] data;		// unsigned char
	int storage;
	int fill;
	int returned;

	int unsynced;
	int headerbytes;
	int bodybytes;

	public ogg_sync_state() {}
}
