// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

class highlevel_byblocktype {

	float tone_mask_setting;
	float tone_peaklimit_setting;
	float noise_bias_setting;
	float noise_compand_setting;


	public highlevel_byblocktype( float _tone_mask_setting, float _tone_peaklimit_setting, float _noise_bias_setting, float _noise_compand_setting ) {

		tone_mask_setting = _tone_mask_setting;
		tone_peaklimit_setting = _tone_peaklimit_setting;
		noise_bias_setting = _noise_bias_setting;
		noise_compand_setting = _noise_compand_setting;
	}
}
