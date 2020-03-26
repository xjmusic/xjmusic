// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

class vorbis_info_mode {

	int blockflag;
	int windowtype;
	int transformtype;
	int mapping;


	public vorbis_info_mode( int _blockflag, int _windowtype, int _transformtype, int _mapping ) {

		blockflag = _blockflag;
		windowtype = _windowtype;
		transformtype = _transformtype;
		mapping = _mapping;
	}

	public vorbis_info_mode( vorbis_info_mode src ) {

		this( src.blockflag, src.windowtype, src.transformtype, src.mapping );
	}
}
