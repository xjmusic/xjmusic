-- -----------------------------------------------------
-- Table `audio`
-- See [#166] Refactoring: Only store waveform key (not complete URL) in order to allow for uploading to S3 then distribution via *.outright.io -> CloudFront -> s3
-- -----------------------------------------------------
ALTER TABLE `audio` CHANGE `waveform_url` `waveform_key` VARCHAR(2047) NOT NULL;
