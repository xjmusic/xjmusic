-- -----------------------------------------------------
-- [#166690830] Program handles its own entities
-- -----------------------------------------------------

CREATE TABLE `program`
(
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id`    bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `state`      varchar(255)        NOT NULL,
  `type`       varchar(255)        NOT NULL,
  `name`       varchar(255)        NOT NULL,
  `density`    float unsigned      NOT NULL,
  `key`        varchar(255)        NOT NULL,
  `tempo`      float unsigned      NOT NULL,
  `created_at` timestamp           NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp           NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `content`    json                NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `program_fk_user` (`user_id`),
  KEY `program_fk_library` (`library_id`),
  CONSTRAINT `program_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `program_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB
  AUTO_INCREMENT = 75807
  DEFAULT CHARSET = latin1
