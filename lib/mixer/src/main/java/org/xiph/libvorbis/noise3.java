// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.P_BANDS;
import static org.xiph.libvorbis.vorbis_constants.integer_constants.P_NOISECURVES;

public class noise3 {

	int[][] data;		// data[P_NOISECURVES][17]


	public noise3( int[][] _data ) {

		data = new int[ P_NOISECURVES ][ P_BANDS ];
		for ( int i=0; i < _data.length; i++ )
			System.arraycopy( _data[i], 0, data[i], 0, _data[i].length );
	}

	public noise3( noise3 src ) {

		this( src.data );
	}
}
