// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

class envelope_band {

	int begin;
	int end;
	float[] window;
	float total;


	public envelope_band( int _begin, int _end, float[] _window, float _total ) {

		begin = _begin;
		end = _end;

		window = new float[ _window.length ];
		System.arraycopy( _window, 0, window, 0, _window.length );

		total = _total;
	}

	public envelope_band( envelope_band src ) {

		this( src.begin, src.end, src.window, src.total );
	}

	public envelope_band() {}
}
