// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

public class encode_aux_threshmatch {

	float[] quantthresh;
	int[] quantmap;			// long *quantmap
	int quantvals;
	int threshvals;


	public encode_aux_threshmatch( float[] _quantthresh, int[] _quantmap, int _quantvals, int _threshvals ) {

		if ( _quantthresh == null )
			quantthresh = null;
		else
			quantthresh = (float[])_quantthresh.clone();

		if ( _quantmap == null )
			quantmap = null;
		else
			quantmap = (int[])_quantmap.clone();

		quantvals = _quantvals;
		threshvals = _threshvals;
	}

	public encode_aux_threshmatch( encode_aux_threshmatch src ) {

		this( src.quantthresh, src.quantmap, src.quantvals, src.threshvals );
	}
}
