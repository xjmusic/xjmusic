-- -----------------------------------------------------
-- Table `link_message`
-- -----------------------------------------------------
CREATE TABLE `link_message` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `link_id`    BIGINT(20) UNSIGNED NOT NULL,
  `type`       VARCHAR(255)        NOT NULL,
  `body`       TEXT                NOT NULL,
  `created_at` TIMESTAMP           NULL     DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP           NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `message_fk_link_idx` (`link_id`),
  CONSTRAINT `message_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;
