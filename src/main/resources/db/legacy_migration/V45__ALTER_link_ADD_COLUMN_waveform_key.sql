---
--- [#267] Link has `waveform_key` referencing xj-link-* S3 bucket object key
---
ALTER TABLE `link`
  ADD COLUMN `waveform_key` VARCHAR(2047) DEFAULT NULL;
