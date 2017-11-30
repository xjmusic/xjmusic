-- -----------------------------------------------------
-- Table `platform_message`
-- -----------------------------------------------------
CREATE TABLE `platform_message` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `type`       VARCHAR(255)        NOT NULL,
  `body`       TEXT                NOT NULL,
  `created_at` TIMESTAMP           NULL     DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP           NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;
