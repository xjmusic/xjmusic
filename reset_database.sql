# Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

-- MySQL dump 10.13  Distrib 5.7.23, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: xj
-- ------------------------------------------------------
-- Server version	5.6.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `xj`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `xj` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `xj`;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_user`
--

DROP TABLE IF EXISTS `account_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `account_user_fk_account_idx` (`account_id`),
  KEY `account_user_fk_user_idx` (`user_id`),
  CONSTRAINT `account_user_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `account_user_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `arrangement`
--

DROP TABLE IF EXISTS `arrangement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrangement` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `choice_id` bigint(20) unsigned NOT NULL,
  `voice_id` bigint(20) unsigned NOT NULL,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `arrangement_fk_choice_idx` (`choice_id`),
  KEY `arrangement_fk_voice_idx` (`voice_id`),
  KEY `arrangement_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `arrangement_fk_choice` FOREIGN KEY (`choice_id`) REFERENCES `choice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_voice` FOREIGN KEY (`voice_id`) REFERENCES `voice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=7356182 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio`
--

DROP TABLE IF EXISTS `audio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `waveform_key` varchar(2047) NOT NULL,
  `start` float unsigned DEFAULT NULL,
  `length` float unsigned DEFAULT NULL,
  `tempo` float unsigned NOT NULL,
  `pitch` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `audio_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `audio_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=603 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio_chord`
--

DROP TABLE IF EXISTS `audio_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `audio_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_audio_idx` (`audio_id`),
  CONSTRAINT `chord_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio_event`
--

DROP TABLE IF EXISTS `audio_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `audio_id` bigint(20) unsigned NOT NULL,
  `velocity` float unsigned NOT NULL,
  `tonality` float unsigned NOT NULL,
  `inflection` varchar(63) NOT NULL,
  `position` float NOT NULL,
  `duration` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `event_fk_audio_idx` (`audio_id`),
  CONSTRAINT `event_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=571 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain`
--

DROP TABLE IF EXISTS `chain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `state` varchar(255) NOT NULL,
  `start_at` timestamp(6) NULL DEFAULT NULL,
  `stop_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `embed_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_embed_key` (`embed_key`),
  KEY `chain_fk_account` (`account_id`),
  CONSTRAINT `chain_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=198 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_config`
--

DROP TABLE IF EXISTS `chain_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(32768) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_config_fk_chain_idx` (`chain_id`),
  CONSTRAINT `chain_config_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_instrument`
--

DROP TABLE IF EXISTS `chain_instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_instrument` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_instrument_fk_chain_idx` (`chain_id`),
  KEY `chain_instrument_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `chain_instrument_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_instrument_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_library`
--

DROP TABLE IF EXISTS `chain_library`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_library` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_library_fk_chain_idx` (`chain_id`),
  KEY `chain_library_fk_library_idx` (`library_id`),
  CONSTRAINT `chain_library_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_library_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=174 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_sequence`
--

DROP TABLE IF EXISTS `chain_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_sequence` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_sequence_fk_chain` (`chain_id`),
  KEY `chain_sequence_fk_sequence` (`sequence_id`),
  CONSTRAINT `chain_sequence_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_sequence_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `choice`
--

DROP TABLE IF EXISTS `choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `choice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `transpose` int(11) NOT NULL,
  `pattern_offset` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `choice_fk_segment` (`segment_id`),
  KEY `choice_fk_sequence` (`sequence_id`),
  CONSTRAINT `choice_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4229612 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument`
--

DROP TABLE IF EXISTS `instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(1023) NOT NULL,
  `density` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `instrument_fk_library_idx` (`library_id`),
  KEY `instrument_fk_user` (`user_id`),
  CONSTRAINT `instrument_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `instrument_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_meme`
--

DROP TABLE IF EXISTS `instrument_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `meme_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `library`
--

DROP TABLE IF EXISTS `library`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `library` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `library_fk_account_idx` (`account_id`),
  CONSTRAINT `library_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern`
--

DROP TABLE IF EXISTS `pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `total` int(10) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  `meter_super` int(11) DEFAULT NULL,
  `meter_sub` int(11) DEFAULT NULL,
  `meter_swing` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_fk_sequence` (`sequence_id`),
  CONSTRAINT `pattern_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=219 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_chord`
--

DROP TABLE IF EXISTS `pattern_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_chord_fk_pattern` (`pattern_id`),
  CONSTRAINT `pattern_chord_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1280 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_event`
--

DROP TABLE IF EXISTS `pattern_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `voice_id` bigint(20) unsigned NOT NULL,
  `velocity` float unsigned NOT NULL,
  `tonality` float unsigned NOT NULL,
  `inflection` varchar(63) NOT NULL,
  `position` float NOT NULL,
  `duration` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_event_fk_pattern` (`pattern_id`),
  KEY `pattern_event_fk_voice` (`voice_id`),
  CONSTRAINT `pattern_event_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pattern_event_fk_voice` FOREIGN KEY (`voice_id`) REFERENCES `voice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1293 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_meme`
--

DROP TABLE IF EXISTS `pattern_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_meme_fk_pattern` (`pattern_id`),
  CONSTRAINT `pattern_meme_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `platform_message`
--

DROP TABLE IF EXISTS `platform_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `platform_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=542 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schema_version`
--

DROP TABLE IF EXISTS `schema_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schema_version` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `schema_version_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment`
--

DROP TABLE IF EXISTS `segment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `state` varchar(255) NOT NULL,
  `begin_at` timestamp(6) NULL DEFAULT NULL,
  `end_at` timestamp(6) NULL DEFAULT NULL,
  `total` int(10) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `waveform_key` varchar(2047) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_chain_offset_index` (`chain_id`,`offset`),
  CONSTRAINT `segment_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2939432 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_chord`
--

DROP TABLE IF EXISTS `segment_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_segment` (`segment_id`),
  CONSTRAINT `chord_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9324974 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_meme`
--

DROP TABLE IF EXISTS `segment_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_segment` (`segment_id`),
  CONSTRAINT `meme_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4868136 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_message`
--

DROP TABLE IF EXISTS `segment_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `message_fk_segment` (`segment_id`),
  CONSTRAINT `message_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1492787 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `density` float unsigned NOT NULL,
  `key` varchar(255) NOT NULL,
  `tempo` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_fk_user` (`user_id`),
  KEY `sequence_fk_library` (`library_id`),
  CONSTRAINT `sequence_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sequence_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sequence_meme`
--

DROP TABLE IF EXISTS `sequence_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_meme_fk_sequence` (`sequence_id`),
  CONSTRAINT `sequence_meme_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(1023) DEFAULT NULL,
  `avatar_url` varchar(1023) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_access_token`
--

DROP TABLE IF EXISTS `user_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_access_token` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_auth_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `access_token` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_access_token_fk_user_idx` (`user_id`),
  KEY `user_access_token_fk_user_auth_idx` (`user_auth_id`),
  CONSTRAINT `user_access_token_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_access_token_fk_user_auth` FOREIGN KEY (`user_auth_id`) REFERENCES `user_auth` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=266 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_auth`
--

DROP TABLE IF EXISTS `user_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_auth` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `external_access_token` varchar(1023) NOT NULL,
  `external_refresh_token` varchar(1023) DEFAULT NULL,
  `external_account` varchar(1023) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_auth_fk_user_idx` (`user_id`),
  CONSTRAINT `user_auth_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_role_fk_user_idx` (`user_id`),
  CONSTRAINT `user_role_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voice`
--

DROP TABLE IF EXISTS `voice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(1023) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `voice_fk_sequence` (`sequence_id`),
  CONSTRAINT `voice_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Current Database: `xj_test`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `xj_test` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `xj_test`;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_user`
--

DROP TABLE IF EXISTS `account_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `account_user_fk_account_idx` (`account_id`),
  KEY `account_user_fk_user_idx` (`user_id`),
  CONSTRAINT `account_user_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `account_user_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `arrangement`
--

DROP TABLE IF EXISTS `arrangement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrangement` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `choice_id` bigint(20) unsigned NOT NULL,
  `voice_id` bigint(20) unsigned NOT NULL,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `arrangement_fk_choice_idx` (`choice_id`),
  KEY `arrangement_fk_voice_idx` (`voice_id`),
  KEY `arrangement_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `arrangement_fk_choice` FOREIGN KEY (`choice_id`) REFERENCES `choice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_voice` FOREIGN KEY (`voice_id`) REFERENCES `voice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio`
--

DROP TABLE IF EXISTS `audio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `waveform_key` varchar(2047) NOT NULL,
  `start` float unsigned DEFAULT NULL,
  `length` float unsigned DEFAULT NULL,
  `tempo` float unsigned NOT NULL,
  `pitch` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `audio_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `audio_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio_chord`
--

DROP TABLE IF EXISTS `audio_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `audio_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_audio_idx` (`audio_id`),
  CONSTRAINT `chord_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audio_event`
--

DROP TABLE IF EXISTS `audio_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `audio_id` bigint(20) unsigned NOT NULL,
  `velocity` float unsigned NOT NULL,
  `tonality` float unsigned NOT NULL,
  `inflection` varchar(63) NOT NULL,
  `position` float NOT NULL,
  `duration` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `event_fk_audio_idx` (`audio_id`),
  CONSTRAINT `event_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain`
--

DROP TABLE IF EXISTS `chain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `state` varchar(255) NOT NULL,
  `start_at` timestamp(6) NULL DEFAULT NULL,
  `stop_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `embed_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_embed_key` (`embed_key`),
  KEY `chain_fk_account` (`account_id`),
  CONSTRAINT `chain_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_config`
--

DROP TABLE IF EXISTS `chain_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(32768) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_config_fk_chain_idx` (`chain_id`),
  CONSTRAINT `chain_config_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_instrument`
--

DROP TABLE IF EXISTS `chain_instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_instrument` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_instrument_fk_chain_idx` (`chain_id`),
  KEY `chain_instrument_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `chain_instrument_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_instrument_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_library`
--

DROP TABLE IF EXISTS `chain_library`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_library` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_library_fk_chain_idx` (`chain_id`),
  KEY `chain_library_fk_library_idx` (`library_id`),
  CONSTRAINT `chain_library_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_library_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_sequence`
--

DROP TABLE IF EXISTS `chain_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_sequence` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_sequence_fk_chain` (`chain_id`),
  KEY `chain_sequence_fk_sequence` (`sequence_id`),
  CONSTRAINT `chain_sequence_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_sequence_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `choice`
--

DROP TABLE IF EXISTS `choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `choice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `transpose` int(11) NOT NULL,
  `pattern_offset` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `choice_fk_segment` (`segment_id`),
  KEY `choice_fk_sequence` (`sequence_id`),
  CONSTRAINT `choice_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument`
--

DROP TABLE IF EXISTS `instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(1023) NOT NULL,
  `density` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `instrument_fk_library_idx` (`library_id`),
  KEY `instrument_fk_user` (`user_id`),
  CONSTRAINT `instrument_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `instrument_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_meme`
--

DROP TABLE IF EXISTS `instrument_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instrument_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `instrument_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `meme_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `library`
--

DROP TABLE IF EXISTS `library`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `library` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `library_fk_account_idx` (`account_id`),
  CONSTRAINT `library_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern`
--

DROP TABLE IF EXISTS `pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `total` int(10) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  `meter_super` int(11) DEFAULT NULL,
  `meter_sub` int(11) DEFAULT NULL,
  `meter_swing` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_fk_sequence` (`sequence_id`),
  CONSTRAINT `pattern_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_chord`
--

DROP TABLE IF EXISTS `pattern_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_chord_fk_pattern` (`pattern_id`),
  CONSTRAINT `pattern_chord_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_event`
--

DROP TABLE IF EXISTS `pattern_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `voice_id` bigint(20) unsigned NOT NULL,
  `velocity` float unsigned NOT NULL,
  `tonality` float unsigned NOT NULL,
  `inflection` varchar(63) NOT NULL,
  `position` float NOT NULL,
  `duration` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_event_fk_pattern` (`pattern_id`),
  KEY `pattern_event_fk_voice` (`voice_id`),
  CONSTRAINT `pattern_event_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pattern_event_fk_voice` FOREIGN KEY (`voice_id`) REFERENCES `voice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern_meme`
--

DROP TABLE IF EXISTS `pattern_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_meme_fk_pattern` (`pattern_id`),
  CONSTRAINT `pattern_meme_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `platform_message`
--

DROP TABLE IF EXISTS `platform_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `platform_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schema_version`
--

DROP TABLE IF EXISTS `schema_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schema_version` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `schema_version_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment`
--

DROP TABLE IF EXISTS `segment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `state` varchar(255) NOT NULL,
  `begin_at` timestamp(6) NULL DEFAULT NULL,
  `end_at` timestamp(6) NULL DEFAULT NULL,
  `total` int(10) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `waveform_key` varchar(2047) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_chain_offset_index` (`chain_id`,`offset`),
  CONSTRAINT `segment_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_chord`
--

DROP TABLE IF EXISTS `segment_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_segment` (`segment_id`),
  CONSTRAINT `chord_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_meme`
--

DROP TABLE IF EXISTS `segment_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_segment` (`segment_id`),
  CONSTRAINT `meme_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segment_message`
--

DROP TABLE IF EXISTS `segment_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `segment_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `segment_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `message_fk_segment` (`segment_id`),
  CONSTRAINT `message_fk_segment` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `library_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `density` float unsigned NOT NULL,
  `key` varchar(255) NOT NULL,
  `tempo` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_fk_user` (`user_id`),
  KEY `sequence_fk_library` (`library_id`),
  CONSTRAINT `sequence_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sequence_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sequence_meme`
--

DROP TABLE IF EXISTS `sequence_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_meme_fk_sequence` (`sequence_id`),
  CONSTRAINT `sequence_meme_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(1023) DEFAULT NULL,
  `avatar_url` varchar(1023) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_access_token`
--

DROP TABLE IF EXISTS `user_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_access_token` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_auth_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `access_token` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_access_token_fk_user_idx` (`user_id`),
  KEY `user_access_token_fk_user_auth_idx` (`user_auth_id`),
  CONSTRAINT `user_access_token_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_access_token_fk_user_auth` FOREIGN KEY (`user_auth_id`) REFERENCES `user_auth` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_auth`
--

DROP TABLE IF EXISTS `user_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_auth` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `external_access_token` varchar(1023) NOT NULL,
  `external_refresh_token` varchar(1023) DEFAULT NULL,
  `external_account` varchar(1023) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_auth_fk_user_idx` (`user_id`),
  CONSTRAINT `user_auth_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user_role_fk_user_idx` (`user_id`),
  CONSTRAINT `user_role_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voice`
--

DROP TABLE IF EXISTS `voice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(1023) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `voice_fk_sequence` (`sequence_id`),
  CONSTRAINT `voice_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-08-30  8:54:00




















#-------------
USE `xj`;

-- MySQL dump 10.13  Distrib 5.7.23, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: xj
-- ------------------------------------------------------
-- Server version	5.6.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'Alpha','2017-02-10 00:03:21','2017-05-03 22:15:24'),(2,'Ambience™','2017-02-10 00:03:21','2018-01-02 00:44:07'),(4,'Dave Cole Sandbox','2018-02-09 20:51:30','2018-02-09 20:51:30'),(5,'Mark Stewart Sandbox','2018-02-09 22:31:42','2018-02-09 22:31:42');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_user`
--

LOCK TABLES `account_user` WRITE;
/*!40000 ALTER TABLE `account_user` DISABLE KEYS */;
INSERT INTO `account_user` VALUES (1,1,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(2,1,2,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(4,2,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(5,1,3,'2017-03-08 02:27:34','2017-03-08 02:27:34'),(7,1,5,'2017-03-10 05:39:37','2017-03-10 05:39:37'),(8,1,6,'2017-04-17 21:59:20','2017-04-17 21:59:20'),(9,1,7,'2017-04-17 21:59:24','2017-04-17 21:59:24'),(12,1,12,'2017-05-18 17:39:30','2017-05-18 17:39:30'),(13,1,14,'2017-06-26 14:48:43','2017-06-26 14:48:43'),(14,1,22,'2017-07-13 15:18:39','2017-07-13 15:18:39'),(15,1,23,'2017-07-17 18:47:27','2017-07-17 18:47:27'),(16,1,24,'2017-07-17 18:47:33','2017-07-17 18:47:33'),(17,1,13,'2017-07-30 16:28:43','2017-07-30 16:28:43'),(18,1,25,'2017-07-30 16:30:06','2017-07-30 16:30:06'),(19,1,27,'2017-08-25 19:47:29','2017-08-25 19:47:29'),(20,2,3,'2017-08-25 19:47:40','2017-08-25 19:47:40'),(21,2,27,'2017-08-25 19:47:45','2017-08-25 19:47:45'),(23,1,28,'2017-12-07 19:01:44','2017-12-07 19:01:44'),(24,1,30,'2017-12-08 20:46:14','2017-12-08 20:46:14'),(25,2,6,'2018-01-07 21:37:56','2018-01-07 21:37:56'),(26,2,7,'2018-01-07 21:38:00','2018-01-07 21:38:00'),(27,4,3,'2018-02-09 20:51:36','2018-02-09 20:51:36'),(28,5,27,'2018-02-09 22:31:55','2018-02-09 22:31:55'),(29,2,14,'2018-02-27 19:54:45','2018-02-27 19:54:45');
/*!40000 ALTER TABLE `account_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio`
--

LOCK TABLES `audio` WRITE;
/*!40000 ALTER TABLE `audio` DISABLE KEYS */;
INSERT INTO `audio` VALUES (2,3,'Kick','80454e35-5693-4b42-aa6a-218383a9f584-instrument-3-audio.wav',0,0.702,120,57.495,'2017-04-21 16:41:03','2017-07-26 22:08:30','Published'),(3,3,'Kick Long','ed1957b9-eea0-42f8-8493-b8874e1a6bf9-instrument-3-audio.wav',0,0.865,120,57.05,'2017-04-21 18:52:17','2017-07-26 22:08:30','Published'),(4,3,'Hihat Closed','0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14-instrument-3-audio.wav',0,0.053,120,6300,'2017-04-21 19:33:05','2017-07-26 22:08:30','Published'),(5,3,'Maracas','ffe4edd6-5b83-4ac9-8e69-156ddb06762f-instrument-3-audio.wav',0,0.026,120,190.086,'2017-04-21 19:38:16','2017-07-26 22:08:30','Published'),(6,3,'Snare','7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe-instrument-3-audio.wav',0,0.093,120,177.823,'2017-04-21 19:42:59','2017-07-26 22:08:30','Published'),(7,3,'Tom','a6bf0d86-6b45-4cf1-b404-2242095c7876-instrument-3-audio.wav',0,0.36,120,104.751,'2017-04-21 19:43:58','2017-07-26 22:08:30','Published'),(8,3,'Claves','aea2483c-7707-4100-aa86-b680668cd1a0-instrument-3-audio.wav',0,0.03,120,2594,'2017-04-23 23:59:47','2017-07-26 22:08:30','Published'),(9,3,'Conga','f772f19f-b51b-414e-9dc8-8ceb23faa779-instrument-3-audio.wav',0,0.26,120,213,'2017-04-24 00:03:32','2017-07-26 22:08:30','Published'),(10,3,'Conga High','c0975d3a-4f26-44b2-a9d3-800320bfa3e1-instrument-3-audio.wav',0,0.179,120,397.297,'2017-04-24 00:05:34','2017-07-26 22:08:30','Published'),(11,3,'Tom High','aea1351b-bb96-4487-8feb-ae8ad3e499ad-instrument-3-audio.wav',0,0.2,120,190.909,'2017-04-24 02:18:29','2017-07-26 22:08:30','Published'),(12,3,'Clap','ce0662a2-3f7e-425b-8105-fb639d395235-instrument-3-audio.wav',0,0.361,120,1102.5,'2017-04-24 02:21:04','2017-07-26 22:08:30','Published'),(13,3,'Cowbell','aaa877a8-0c89-4781-93f8-69c722285b2a-instrument-3-audio.wav',0,0.34,120,268.902,'2017-04-24 02:22:47','2017-07-26 22:08:30','Published'),(14,3,'Cymbal Crash','37a35a63-23e4-4ef6-a78e-db2577aa9a00-instrument-3-audio.wav',0,2.229,120,109.701,'2017-04-24 02:24:03','2017-07-26 22:08:30','Published'),(15,3,'Hihat Open','020ad575-af86-4fe2-a869-957d50d59ac4-instrument-3-audio.wav',0,0.598,120,7350,'2017-04-24 02:25:31','2017-07-26 22:08:30','Published'),(16,3,'Snare Rim','58fd7eae-b55e-4567-9c27-ead64b83488a-instrument-3-audio.wav',0,0.014,120,445.445,'2017-04-24 02:26:53','2017-07-26 22:08:30','Published'),(22,4,'Hihat Closed 9','0f28ef83-2213-4bbb-ae68-3eecc201ead3-instrument-4-audio.wav',0,0.849,120,648.529,'2017-06-15 22:43:24','2017-07-26 22:08:30','Published'),(23,4,'Hihat Closed 7','e15dc427-b556-4a72-bec8-6b59c6d8bbc8-instrument-4-audio.wav',0.003,0.962,120,8820,'2017-06-15 22:44:55','2017-07-26 22:08:30','Published'),(24,4,'Hihat Closed 8','cb1ffbff-c31d-4e06-9d84-649c1f257a24-instrument-4-audio.wav',0,0.905,120,648.529,'2017-06-15 22:45:52','2017-07-26 22:08:30','Published'),(26,4,'Snare Rim','7b2d94b3-c218-498b-906e-11c313054cd1-instrument-4-audio.wav',0,1.147,120,239.674,'2017-06-15 22:56:58','2017-07-26 22:08:30','Published'),(27,4,'Hihat Open 5','bf2c9ad8-ceb4-4c7e-98ae-a9c561680a1f-instrument-4-audio.wav',0.003,1.115,120,648.529,'2017-06-15 23:04:16','2017-07-26 22:08:30','Published'),(28,4,'Hihat Open 7','4c3c5673-e8f1-4452-ad8c-5466cce0492d-instrument-4-audio.wav',0,2,120,648.529,'2017-06-15 23:06:14','2017-07-26 22:08:30','Published'),(29,4,'Hihat Open 6','9a57a402-98e9-4ceb-86c2-ea60607b56d1-instrument-4-audio.wav',0,0.809,120,648.529,'2017-06-15 23:07:41','2017-07-26 22:08:30','Published'),(30,4,'Stick Side 7','ea042c27-551b-44c7-998b-1df185d319cf-instrument-4-audio.wav',0.003,0.159,120,436.634,'2017-06-15 23:11:51','2017-07-26 22:08:30','Published'),(31,4,'Stick Side 6','0d65a838-e76f-407d-a06b-6485d67ba44c-instrument-4-audio.wav',0,0.335,120,2321.05,'2017-06-15 23:13:00','2017-07-26 22:08:30','Published'),(32,4,'Stick Side 5','99f7dbea-c1fb-419e-ad44-c90804516aa3-instrument-4-audio.wav',0,0.248,120,1837.5,'2017-06-15 23:14:30','2017-07-26 22:08:30','Published'),(33,4,'Snare Rim 7','12e36076-5944-4101-a41b-b39136cf78a4-instrument-4-audio.wav',0,0.461,120,254.913,'2017-06-15 23:15:43','2017-07-26 22:08:30','Published'),(34,4,'Snare Rim 6','5a840f38-7623-442b-b9a9-a0ff1927c7a0-instrument-4-audio.wav',0,0.527,120,245,'2017-06-15 23:16:36','2017-07-26 22:08:30','Published'),(35,4,'Snare Rim 5','d404857a-6bf8-43c4-ad76-5259945d16fe-instrument-4-audio.wav',0,0.463,120,181.481,'2017-06-15 23:17:44','2017-07-26 22:08:30','Published'),(36,4,'Tom High','4888db8b-1c81-4178-8af5-332ae7067ca8-instrument-4-audio.wav',0.002,0.42,120,187.66,'2017-06-15 23:20:38','2017-07-26 22:08:30','Published'),(37,4,'Snare 3','d373a2f8-8c8f-4afa-b7e3-c21623d15f42-instrument-4-audio.wav',0.008,0.404,120,2450,'2017-06-15 23:21:50','2017-07-26 22:08:30','Published'),(38,4,'Tom','d5bcc3a5-d98f-434f-8fcb-987f1913a684-instrument-4-audio.wav',0.009,0.445,120,225,'2017-06-15 23:22:45','2017-07-26 22:08:30','Published'),(39,4,'Conga High','511f5a68-1eca-4ca3-9713-956a219d734c-instrument-4-audio.wav',0.002,0.425,120,187.66,'2017-06-15 23:24:15','2017-07-26 22:08:30','Published'),(40,4,'Conga','2059cab7-8052-46cf-8fd1-2930cfe5ce59-instrument-4-audio.wav',0.001,0.547,120,183.231,'2017-06-15 23:25:03','2017-07-26 22:08:30','Published'),(41,4,'Snare 5','cce1763b-fca3-49c5-9024-c665c1fea7f3-instrument-4-audio.wav',0.008,0.407,120,180.738,'2017-06-15 23:25:58','2017-07-26 22:08:30','Published'),(42,4,'Snare 4','511168e1-3291-4ec8-a6ac-652249206287-instrument-4-audio.wav',0.008,0.439,120,204.167,'2017-06-15 23:27:04','2017-07-26 22:08:30','Published'),(43,4,'Kick 7','2fd75fb8-b968-46ba-8c43-ac6ad2db9a80-instrument-4-audio.wav',0.008,0.537,120,43.534,'2017-06-16 00:20:54','2017-07-26 22:08:30','Published'),(44,4,'Kick 3','3a79549f-cf7b-4338-8756-f75b3fc5deaa-instrument-4-audio.wav',0.005,0.742,120,52.128,'2017-06-16 00:24:47','2017-07-26 22:08:30','Published'),(45,4,'Kick 3','c076a674-1626-4b22-bc07-a639ca90b363-instrument-4-audio.wav',0.01,0.677,120,56.178,'2017-06-16 00:27:07','2017-07-26 22:08:30','Published'),(48,4,'Tom Low 5','246190da-65fd-41a9-a943-2c8e3b763fa5-instrument-4-audio.wav',0,0.73,120,84.483,'2017-06-16 00:33:57','2017-07-26 22:08:30','Published'),(49,4,'Tom 5','bf45a337-c86a-4c44-9663-06093d3ca9ba-instrument-4-audio.wav',0,0.59,120,90.928,'2017-06-16 00:35:25','2017-07-26 22:08:30','Published'),(50,4,'Tom High 5','83294480-eef2-4171-8d69-8f16092557df-instrument-4-audio.wav',0.003,0.444,120,126,'2017-06-16 00:36:37','2017-07-26 22:08:30','Published'),(51,4,'Kick Long 2','b12bf5ff-ebec-47e3-9259-6cd0c9f57724-instrument-4-audio.wav',0.01,1.476,120,59.036,'2017-06-16 00:39:02','2017-07-26 22:08:30','Published'),(54,4,'Clap 1','27b08205-9921-4d48-bc54-ba4110fe238f-instrument-4-audio.wav',0,0.572,120,185.294,'2017-06-16 02:15:47','2017-07-26 22:08:30','Published'),(55,4,'Clap 2','81f55d83-39fe-4832-99bf-4e4f3af69496-instrument-4-audio.wav',0,0.684,120,188.462,'2017-06-16 02:17:11','2017-07-26 22:08:30','Published'),(56,3,'Kick 2','a731fc44-5ae0-4e9f-a728-edfe1895da4b-instrument-3-audio.wav',0,0.34,120,69.122,'2017-06-16 03:01:06','2017-07-26 22:08:30','Published'),(57,3,'Kick Long 2','84b1974c-02b0-406f-b78e-21414282986e-instrument-3-audio.wav',0,1.963,120,60.494,'2017-06-16 03:04:09','2017-07-26 22:08:30','Published'),(58,3,'Tom High 2','618bc8e5-f51f-4635-895c-5bd6522f8d8c-instrument-3-audio.wav',0.002,0.411,120,201.37,'2017-06-16 03:06:30','2017-07-26 22:08:30','Published'),(59,3,'Tom Low 2','014c8939-c9e7-4911-9620-9c4075a3b4a2-instrument-3-audio.wav',0,0.701,120,111.646,'2017-06-16 03:07:20','2017-07-26 22:08:30','Published'),(60,3,'Tom 2','3fcb76bf-6168-4aef-a160-facd1bb18071-instrument-3-audio.wav',0,0.488,120,149.492,'2017-06-16 03:09:50','2017-07-26 22:08:30','Published'),(61,3,'Clap 2','9a3e9e07-b1dd-44a5-9399-3b6c11bd72b1-instrument-3-audio.wav',0.002,0.356,120,1225,'2017-06-16 03:13:28','2017-07-26 22:08:30','Published'),(62,3,'Clap 3','f24484dd-b879-42c5-9c2a-71857555c319-instrument-3-audio.wav',0,0.734,120,980,'2017-06-16 03:14:41','2017-07-26 22:08:30','Published'),(63,3,'Maracas 2','f20dcce7-a936-446c-8692-c8caf37d8896-instrument-3-audio.wav',0.009,0.43,120,11025,'2017-06-16 03:17:11','2017-07-26 22:08:30','Published'),(64,4,'Cowbell','392a388d-8e32-46f9-ad57-b3bd29929262-instrument-4-audio.wav',0.002,0.298,120,525,'2017-06-16 03:20:04','2017-07-26 22:08:30','Published'),(65,4,'Cymbal Crash 1','378df92f-aec2-4a5c-9243-d08384971761-instrument-4-audio.wav',0.018,1.878,120,1297.06,'2017-06-16 03:21:46','2017-07-26 22:08:30','Published'),(66,4,'Cymbal Crash 2','b921f58d-1ce0-4c1e-82d0-08479c25bfff-instrument-4-audio.wav',0.01,3.241,120,469.149,'2017-06-16 03:24:03','2017-07-26 22:08:30','Published'),(67,4,'Cymbal Crash 3','484d5dc0-4627-477d-8de7-f4c30cc4f538-instrument-4-audio.wav',0.01,3.044,120,181.481,'2017-06-16 03:25:34','2017-07-26 22:08:30','Published'),(68,3,'Cymbal Crash 2','bb3e2a48-8f59-4ad0-a05f-30aca579524f-instrument-3-audio.wav',0,2,120,816.667,'2017-06-16 03:28:35','2017-07-26 22:08:30','Published'),(69,5,'Hihat Closed A_3','86d61872-a9bf-4b68-b4df-397be09bfe5c-instrument-5-audio.wav',0.007,1.051,120,3428.57,'2017-06-20 23:11:42','2017-07-27 17:35:01','Published'),(70,5,'Hihat Closed A_4','92f61e58-7225-48bb-91f3-b71fcf7aef5a-instrument-5-audio.wav',0,0.623,120,888.889,'2017-06-20 23:17:39','2017-07-27 17:35:07','Published'),(71,5,'Hihat Closed A_5','8a536dae-3727-488f-8895-a0b047620a38-instrument-5-audio.wav',0.001,0.537,120,888.889,'2017-06-20 23:19:00','2017-07-27 17:34:31','Published'),(72,5,'Hihat Closed A_6','e173c291-60d6-4f9a-a422-d2d8c99bd9b3-instrument-5-audio.wav',0.003,0.425,120,3428.57,'2017-06-20 23:35:06','2017-07-27 17:34:36','Published'),(73,5,'Hihat Closed A_7','de082694-4a02-48a4-92d1-83c2d2b7dd92-instrument-5-audio.wav',0.001,0.6,120,1263.16,'2017-06-20 23:36:34','2017-07-27 17:34:40','Published'),(74,5,'Hihat Closed A_8','7cbe09b2-5fe6-4d7a-b5fa-2f85624e91f5-instrument-5-audio.wav',0,0.73,120,1200,'2017-06-20 23:37:43','2017-07-27 17:34:43','Published'),(75,5,'Hihat Closed A_9','96df8da4-5be9-4a0f-a97b-5f8c0d28f161-instrument-5-audio.wav',0,0.432,120,1454.55,'2017-06-20 23:38:52','2017-07-27 17:34:48','Published'),(76,5,'Hihat Closed A_10','e4a06acb-c375-4e9b-a5ce-153b815fe6cb-instrument-5-audio.wav',0.002,0.307,120,3000,'2017-06-20 23:40:36','2017-07-27 17:34:58','Published'),(77,5,'Hihat Open F_1','4eb40925-8e37-4801-ba2e-cce991c97093-instrument-5-audio.wav',0,0.969,120,428.155,'2017-06-20 23:45:30','2017-07-27 17:34:01','Published'),(78,5,'Hihat Open F_2','13db8e43-4266-444a-9edd-c5a5cb2442b4-instrument-5-audio.wav',0,1.506,120,182.988,'2017-06-20 23:46:32','2017-07-27 17:34:05','Published'),(79,5,'Hihat Open F_3','3f0dbe3a-d11a-4e9f-a642-befe5747dd01-instrument-5-audio.wav',0,2.567,120,183.75,'2017-06-20 23:47:22','2017-07-27 17:34:08','Published'),(80,5,'Hihat Open F_4','57ff6b97-fedb-4e3f-b963-840ba8fd101b-instrument-5-audio.wav',0.035,2.617,120,416.038,'2017-06-20 23:48:41','2017-07-27 17:33:29','Published'),(81,5,'Hihat Open F_5','70c7404e-1f17-4a32-8f4a-ff28e7d5797c-instrument-5-audio.wav',0,2.734,120,420,'2017-06-20 23:49:48','2017-07-27 17:33:34','Published'),(82,5,'Hihat Open F_6','ed5b3f4c-a6e3-424b-b8ba-34c317640903-instrument-5-audio.wav',0,1.348,120,432.353,'2017-06-20 23:50:30','2017-07-27 17:33:38','Published'),(83,5,'Hihat Open F_7','8d7c72dc-92bb-4ffa-82ff-13750c8ddbfc-instrument-5-audio.wav',0,2.264,120,183.75,'2017-06-20 23:51:23','2017-07-27 17:33:42','Published'),(84,5,'Hihat Open F_8','7eae03f7-d1aa-42e2-a928-ff6f7b00b25d-instrument-5-audio.wav',0,2.595,120,182.988,'2017-06-20 23:51:59','2017-07-27 17:33:48','Published'),(85,5,'Tom H_1','2f4bf7a2-744e-47cc-b5c2-da0a846cab91-instrument-5-audio.wav',0,1.008,120,1422.58,'2017-06-20 23:54:11','2017-07-27 17:27:27','Published'),(86,5,'Tom H_2','2c2d8ba8-911b-4480-a774-c37102c12e90-instrument-5-audio.wav',0.009,2.036,120,1378.12,'2017-06-20 23:56:11','2017-07-27 17:27:52','Published'),(87,5,'Tom H_8','0e5c97c1-ad2a-4cb5-a1f5-10224c7cec3c-instrument-5-audio.wav',0,2.698,120,1633.33,'2017-06-20 23:58:39','2017-07-27 17:27:11','Published'),(88,5,'Tom H_7','2a525acb-dc9a-47f4-b105-89dc3332d78b-instrument-5-audio.wav',0,1.738,120,1764,'2017-06-20 23:59:37','2017-07-27 17:26:57','Published'),(89,5,'Tom H_6','91f5c7de-609d-48fd-a527-c7b132ee2af5-instrument-5-audio.wav',0,2.984,120,1336.36,'2017-06-21 00:00:19','2017-07-27 17:26:42','Published'),(90,5,'Tom H_5','c18a2f87-df5f-421a-aa59-89fda817210c-instrument-5-audio.wav',0,3.133,120,189.27,'2017-06-21 00:01:06','2017-07-27 17:26:10','Published'),(91,5,'Tom H_4','ee21d28c-6102-4ad7-96a5-49cf5ccaf266-instrument-5-audio.wav',0,2.815,120,186.076,'2017-06-21 00:01:52','2017-07-27 17:26:06','Published'),(92,5,'Tom H_3','8494ac91-a1ef-4045-9f1f-3a1b4a53ee3d-instrument-5-audio.wav',0,2.346,120,1378.12,'2017-06-21 00:02:27','2017-07-27 17:28:04','Published'),(93,5,'Snare Q_1','21369f18-b2b6-4d8b-bd28-de36f294b67e-instrument-5-audio.wav',0,1.206,120,5512.5,'2017-06-21 00:13:47','2017-07-27 17:19:35','Published'),(94,5,'Snare Q_11','88ba75c5-9727-43a3-9ef0-856abe729f78-instrument-5-audio.wav',0,1.524,120,6300,'2017-06-21 00:14:33','2017-07-27 17:19:52','Published'),(95,5,'Snare Q_10','b14d6a26-1e35-4f7c-bbfb-6fd262c2d35f-instrument-5-audio.wav',0,1.631,120,1378.12,'2017-06-21 00:15:27','2017-07-27 17:19:44','Published'),(96,5,'Snare Q_9','0818bf78-3838-43a5-8665-7f8f2814bfc4-instrument-5-audio.wav',0.003,0.583,120,249.153,'2017-06-21 00:49:26','2017-07-27 17:18:07','Published'),(97,5,'Snare Q_8','725e8281-c845-4a87-9a37-9117b1e6a830-instrument-5-audio.wav',0.002,0.799,120,355.645,'2017-06-21 00:51:02','2017-07-27 17:19:07','Published'),(98,5,'Snare Q_7','7fd96254-d9cf-4ad6-9899-dee564543853-instrument-5-audio.wav',0.001,0.653,120,5512.5,'2017-06-21 00:52:59','2017-07-27 17:18:59','Published'),(99,5,'Snare Q_6','83fbed4b-648c-4886-9079-f220fb0dc9fb-instrument-5-audio.wav',0.001,0.659,120,134.451,'2017-06-21 00:54:25','2017-07-27 17:18:54','Published'),(100,5,'Snare Q_5','62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav',0.002,0.405,120,1025.58,'2017-06-21 00:55:25','2017-07-27 17:18:48','Published'),(101,5,'Snare Q_4','8e17510c-a877-42a6-addc-95ef7d559757-instrument-5-audio.wav',0.001,1.257,120,5512.5,'2017-06-21 00:56:51','2017-07-27 17:18:40','Published'),(102,5,'Snare Q_3','a448d6b9-4669-4f17-883a-8dd8c5ce0b8e-instrument-5-audio.wav',0,0.915,120,5512.5,'2017-06-21 00:58:15','2017-07-27 17:20:02','Published'),(103,5,'Snare Q_2','23d5847f-56e6-4b79-99ad-6dfd13b9c5b3-instrument-5-audio.wav',0.001,1.008,120,6300,'2017-06-21 00:59:33','2017-07-27 17:19:57','Published'),(104,5,'Conga M_8','1c5f4752-e790-47a0-b0d9-4eedd54b24a5-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:04:11','2017-07-27 17:20:52','Published'),(105,5,'Tom L_1','568d1c74-a43e-44fc-ab53-0d1d701f6f0f-instrument-5-audio.wav',0,0.851,120,364.463,'2017-06-21 01:05:28','2017-07-27 17:24:19','Published'),(106,5,'Conga M_9','2d2d76f7-9d76-41c6-9e55-0b94703d487c-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:06:20','2017-07-27 17:20:58','Published'),(107,5,'Conga M_7','02dde877-01b4-432d-8d22-f1458917154b-instrument-5-audio.wav',0.001,0.502,120,420,'2017-06-21 01:07:04','2017-07-27 17:20:46','Published'),(108,5,'Conga M_6','3bdc44e7-e464-4a0f-a080-ab3d529ac9dc-instrument-5-audio.wav',0.001,0.512,120,612.5,'2017-06-21 01:07:52','2017-07-27 17:20:40','Published'),(109,5,'Conga M_5','0e47652d-265b-4c83-8c4f-c14a34fc9689-instrument-5-audio.wav',0,0.466,120,612.5,'2017-06-21 01:08:48','2017-07-27 17:20:34','Published'),(110,5,'Conga M_4','f6e912f5-d582-4044-b73b-6e004bb32a15-instrument-5-audio.wav',0,0.6,120,612.5,'2017-06-21 01:09:30','2017-07-27 17:22:48','Published'),(111,5,'Conga M_3','710b3011-cb1e-4065-a514-1e6e4fd19bec-instrument-5-audio.wav',0,0.427,120,612.5,'2017-06-21 01:10:06','2017-07-27 17:22:42','Published'),(112,5,'Conga M_3','c8d1affb-9b7c-4661-bf31-cd80dc2a9ce1-instrument-5-audio.wav',0,0.602,120,588,'2017-06-21 01:10:58','2017-07-27 17:22:38','Published'),(113,5,'Conga M_1','983fc7a1-a1ef-466f-be44-cc1e227ae449-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:11:49','2017-07-27 17:22:34','Published'),(114,5,'Conga M_1','faf2e9c6-6b12-445e-9b2c-93966451ff5e-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:12:52','2017-07-27 17:21:29','Published'),(115,5,'Tom L_10','38c92218-882d-4714-a493-14261e07c4fa-instrument-5-audio.wav',0,0.741,120,302.055,'2017-06-21 01:13:30','2017-07-27 17:24:25','Published'),(116,5,'Tom L_9','50f516a9-faaa-4091-848d-651d96ecc7be-instrument-5-audio.wav',0,0.751,120,176.4,'2017-06-21 01:14:17','2017-07-27 17:24:05','Published'),(117,5,'Tom L_8','f1bac880-fede-4c5d-9249-956f5e179d62-instrument-5-audio.wav',0,0.835,120,290.132,'2017-06-21 01:15:03','2017-07-27 17:24:01','Published'),(119,5,'Tom L_7','b51678cb-50a0-4994-980a-62bf126ca445-instrument-5-audio.wav',0.001,0.674,120,531.325,'2017-06-21 01:19:44','2017-07-27 17:23:58','Published'),(120,5,'Tom L_6','01e988c0-3821-4ba2-8223-70643f3c27cf-instrument-5-audio.wav',0,0.736,120,408.333,'2017-06-21 01:20:53','2017-07-27 17:23:54','Published'),(121,5,'Tom L_5','f6f79c74-f1e0-459b-9728-46f59bd14ee7-instrument-5-audio.wav',0.001,0.608,120,428.155,'2017-06-21 01:21:54','2017-07-27 17:23:50','Published'),(122,5,'Tom L_4','2b9af025-2616-4d03-890f-b74df3413abe-instrument-5-audio.wav',0,0.592,120,11025,'2017-06-21 01:22:35','2017-07-27 17:24:36','Published'),(123,5,'Tom L_3','dd32a686-ef3a-43c4-a3e1-13353d067026-instrument-5-audio.wav',0,0.624,120,110.526,'2017-06-21 01:23:21','2017-07-27 17:24:32','Published'),(124,5,'Tom L_2','6ffdef87-909f-4b67-a2f7-fadbb3a76e33-instrument-5-audio.wav',0,0.528,120,257.895,'2017-06-21 01:23:58','2017-07-27 17:24:29','Published'),(126,3,'Vocal Hie','0248ed87-19e8-449c-9211-4722d6ab8342-instrument-3-audio.wav',0.08,0.477,120,364.463,'2017-06-23 23:53:49','2017-07-26 22:08:31','Published'),(127,3,'Vocal Ahh','d35678fa-f163-433d-8741-250a530b5532-instrument-3-audio.wav',0.012,1.037,120,948.696,'2017-06-23 23:55:53','2017-07-26 22:08:31','Published'),(128,3,'Vocal Hoo','54d3503d-af44-4480-a0d0-8044fb403c5a-instrument-3-audio.wav',0.079,0.45,120,205.116,'2017-06-23 23:57:01','2017-07-26 22:08:31','Published'),(129,3,'Vocal Haa','79b9c4f4-037a-4f6f-bc51-7a7a2dff5528-instrument-3-audio.wav',0.053,0.36,120,864.706,'2017-06-23 23:57:45','2017-07-26 22:08:31','Published'),(132,3,'Vocal Eow','0e2d5fb2-9d40-4741-9da8-bc9943722d66-instrument-3-audio.wav',0.045,0.486,120,383.478,'2017-06-24 00:00:25','2017-07-26 22:08:31','Published'),(133,3,'Vocal Grunt Ooh 2','8896e8d4-0c31-4dd8-93ff-6982a30febdb-instrument-3-audio.wav',0.015,0.247,120,404.587,'2017-06-24 00:10:49','2017-07-26 22:08:31','Published'),(134,3,'Vocal Grunt Ooh','ef489ad1-fb9d-4e77-9b5c-a7b3570c8c09-instrument-3-audio.wav',0.011,0.213,120,1696.15,'2017-06-24 00:11:31','2017-07-26 22:08:31','Published'),(135,4,'Vocal JB Get','e5e8a85b-1c3c-46b5-8394-3b44b5c7e6e1-instrument-4-audio.wav',0.027,0.311,120,386.842,'2017-06-24 00:13:30','2017-07-26 22:08:31','Published'),(136,4,'Vocal JB Baz','76a3e02c-979c-4d64-9bab-3b1a91d3635d-instrument-4-audio.wav',0.018,0.405,120,918.75,'2017-06-24 00:14:55','2017-07-26 22:08:31','Published'),(137,4,'Vocal JB Get 2','22efe6d1-3dea-45a5-906c-1e4bd4465606-instrument-4-audio.wav',0.027,0.29,120,386.842,'2017-06-24 00:16:15','2017-07-26 22:08:31','Published'),(138,4,'Vocal JB Baz2','94bd651e-ce98-4b09-95b8-6e36819e2721-instrument-4-audio.wav',0.032,0.29,120,367.5,'2017-06-24 00:17:37','2017-07-26 22:08:31','Published'),(139,4,'Vocal JB Uhh','3bc65d7a-00a0-42cc-9d15-292f9fbe98ee-instrument-4-audio.wav',0,0.408,120,474.194,'2017-06-24 00:20:34','2017-07-26 22:08:31','Published'),(140,4,'Vocal Woo','c7b78912-493a-4e19-a023-10a6b334e2b3-instrument-4-audio.wav',0.01,0.522,120,464.211,'2017-06-24 00:22:32','2017-07-26 22:08:31','Published'),(141,4,'Vocal JB Me','3fbbf18b-eb45-4375-8bd2-efd5e490c4cb-instrument-4-audio.wav',14,0.336,120,367.5,'2017-06-24 00:23:45','2017-07-26 22:08:31','Published'),(143,4,'Vocal JB Hit','686906da-cc85-4abb-a902-121e98def35d-instrument-4-audio.wav',0.05,0.313,120,512.791,'2017-06-24 00:25:58','2017-07-26 22:08:31','Published'),(144,4,'Vocal Hey','5d808588-5930-4075-a034-4f96b0e2b06f-instrument-4-audio.wav',0.046,0.453,120,760.345,'2017-06-24 00:26:50','2017-07-26 22:08:31','Published'),(145,4,'Vocal Ehh','7806beda-4655-4323-adb0-d9a41d2fc939-instrument-4-audio.wav',0.018,0.297,120,648.529,'2017-06-24 00:27:36','2017-07-26 22:08:31','Published'),(146,4,'Vocal Eh','a6049156-69e0-4128-a4b1-6a17ee4ca0bd-instrument-4-audio.wav',0.018,0.449,120,668.182,'2017-06-24 00:28:28','2017-07-26 22:08:31','Published'),(147,5,'Vocal Watch Me','649a2969-6b98-4201-89fc-968d6414f578-instrument-5-audio.wav',0.05,0.807,120,1225,'2017-06-24 00:29:58','2017-07-26 22:08:31','Published'),(148,5,'Vocal Play It','53fc9c8c-2412-4133-b088-9bac349e6794-instrument-5-audio.wav',0.064,0.358,120,116.053,'2017-06-24 00:31:33','2017-07-26 22:08:31','Published'),(149,5,'Vocal Hoh','5709e633-bd69-407b-b6ba-420395b221de-instrument-5-audio.wav',0.028,0.476,120,689.062,'2017-06-24 00:32:33','2017-07-26 22:08:31','Published'),(150,5,'Vocal Woah','7ac9d00c-0b24-49ad-8cbb-c586ac0f080f-instrument-5-audio.wav',0.02,0.488,120,604.11,'2017-06-24 00:33:44','2017-07-26 22:08:31','Published'),(151,5,'Vocal What 3','489c5976-cbda-4449-a8cf-67d653b77dbf-instrument-5-audio.wav',0.04,0.407,120,370.588,'2017-06-24 00:34:40','2017-07-26 22:08:31','Published'),(152,5,'Vocal What 2','70d22a2a-a888-460f-9dfa-01bae076adfe-instrument-5-audio.wav',0.027,0.276,120,416.038,'2017-06-24 00:35:28','2017-07-26 22:08:31','Published'),(153,5,'Vocal What 1','cccc3d64-9cb9-468d-be42-e1ec29ba65b1-instrument-5-audio.wav',0.058,0.401,120,390.265,'2017-06-24 00:36:13','2017-07-26 22:08:31','Published'),(154,5,'Vocal Oobah','a7779c99-55b0-4067-819d-a8203a157cd6-instrument-5-audio.wav',0,0.904,120,397.297,'2017-06-24 00:37:11','2017-07-26 22:08:31','Published'),(166,5,'Kick 11_339','dfe7c338-dd80-42ee-94da-19bc53489ca7-instrument-5-audio.wav',0,0.569,120,69.014,'2017-07-27 22:35:51','2017-07-27 22:35:51','Published'),(167,5,'Kick 12_339','ccfc6b74-c939-481f-b59d-caced86b2528-instrument-5-audio.wav',0,0.457,120,3675,'2017-07-27 22:36:53','2017-07-27 22:36:53','Published'),(174,5,'Kick 24_339','a0d1938b-9f3d-47b3-a98f-fb0a429e6df7-instrument-5-audio.wav',0.013,0.547,120,88.024,'2017-07-27 23:07:42','2017-07-27 23:07:42','Published'),(176,5,'Kick 28_339','cd42b8a7-c820-43a2-beb1-f5fec4634050-instrument-5-audio.wav',0.024,0.653,120,980,'2017-07-27 23:13:10','2017-07-27 23:13:10','Published'),(180,3,'Vocal How','f70ead8e-f770-4782-83ce-854a1cb3c640-instrument-3-audio.wav',0.074,0.454,120,284.516,'2017-12-11 04:58:58','2017-12-11 04:58:58','Published'),(184,6,'C','9ca392b5-3061-4f3f-b1fe-2cc85217b80f-instrument-6-audio.wav',0,0.805,88,722.951,'2017-12-14 07:15:15','2017-12-14 07:15:15','Published'),(185,6,'D','a081c1d1-fe83-4f87-9073-637b72c1245f-instrument-6-audio.wav',0,1.837,88,250.568,'2017-12-14 07:16:36','2017-12-14 07:16:36','Published'),(186,6,'E','74eec84a-d3b5-49b4-ad44-647c7698a327-instrument-6-audio.wav',0,0.895,88,588,'2017-12-14 07:18:40','2017-12-14 07:18:40','Published'),(187,6,'F','b17c79e7-975e-482f-b051-4ad736762b66-instrument-6-audio.wav',0,0.802,88,773.684,'2017-12-14 07:19:49','2017-12-14 07:19:49','Published'),(190,7,'Shakuhachi','e02b5c6c-21a8-47b9-94fc-aaa5d1b2975f-instrument-7-audio.wav',0,2.681,88,525,'2017-12-14 08:11:24','2017-12-14 08:11:24','Published'),(191,7,'Pan Flute','de11db96-dfee-4fc3-8a02-3285d3bd2d80-instrument-7-audio.wav',0,1.624,88,518.824,'2017-12-14 08:15:40','2017-12-14 08:15:40','Published'),(192,6,'Taiko','12a5c53f-dd1d-4ef0-abcd-936871fb2625-instrument-6-audio.wav',0,2.006,88,2205,'2017-12-14 08:18:44','2017-12-14 08:18:44','Published'),(193,8,'Sitar','9dc36d01-fd2e-49f7-a75b-545897962c9d-instrument-8-audio.wav',0,2.424,88,262.5,'2017-12-14 08:20:32','2017-12-14 08:20:32','Published'),(194,6,'Kalimba','52f40ec2-3d7c-492b-a8ee-825be0654234-instrument-6-audio.wav',0,1.175,88,262.5,'2017-12-14 08:22:59','2017-12-14 08:22:59','Published'),(195,9,'Bass Pad','69dfbe99-bca5-4171-bbae-b69c4599531e-instrument-9-audio.wav',0,4.073,88,49.606,'2017-12-14 08:26:47','2017-12-14 08:26:47','Published'),(196,9,'Omen Pad','0732ee48-5a9b-4a1d-bafd-e8c2ef23231d-instrument-9-audio.wav',0,4.45,88,65.333,'2017-12-14 08:28:08','2017-12-14 08:28:08','Published'),(197,10,'Whale Pad','c477ff4c-3212-4cfe-8712-6add5f697a98-instrument-10-audio.wav',0,3.249,88,226.154,'2017-12-14 08:30:10','2017-12-14 08:30:10','Published'),(199,12,'Shami','a166a69f-8944-4577-9a68-8b323dff7a68-instrument-12-audio.wav',0.006,0.999,88,262.5,'2017-12-14 08:37:20','2017-12-14 08:37:39','Published'),(200,12,'Koto','eb8bca2c-994f-4e62-9bf6-1242acc79d21-instrument-12-audio.wav',0,1.294,88,132.831,'2017-12-14 08:38:53','2017-12-14 08:38:53','Published'),(201,12,'Shamisen','0e57fd93-11b6-49d8-b617-2d1b8e657180-instrument-12-audio.wav',0,1,88,262.5,'2017-12-14 08:45:08','2017-12-14 08:45:08','Published'),(206,12,'Shamisen','e5eff131-8813-48bc-9bda-d378b3eeee9a-instrument-12-audio.wav',0.005,1.353,88,264.072,'2017-12-14 09:27:14','2017-12-14 09:27:14','Published'),(267,19,'Tom Low 5','14f44f7d-7538-4624-8010-d742938b4518-instrument-19-audio.wav',0,0.73,120,84.483,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(268,19,'Tom High 5','b21ad18d-7536-4f8c-8391-988b2c6f3442-instrument-19-audio.wav',0.003,0.444,120,126,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(269,19,'Tom High','d56560f3-5d94-40ee-a692-97403bd6f080-instrument-19-audio.wav',0.002,0.42,120,187.66,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(270,19,'Tom 5','2cebd974-59fa-404e-8839-d42c104f5b85-instrument-19-audio.wav',0,0.59,120,90.928,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(271,19,'Tom','bbdcc9f9-7ac6-4cc9-bb53-d9b39286d43f-instrument-19-audio.wav',0.009,0.445,120,225,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(272,19,'Stick Side 7','c0996bd7-4ea8-4f6b-bd95-ed4dd8c53d79-instrument-19-audio.wav',0.003,0.159,120,436.634,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(273,19,'Stick Side 6','58daaf07-34ea-423c-b06f-b77a6892e7a6-instrument-19-audio.wav',0,0.335,120,2321.05,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(274,19,'Stick Side 5','792ef8bc-ac8f-41ea-9f65-47f302ba5424-instrument-19-audio.wav',0,0.248,120,1837.5,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(275,19,'Snare Rim 7','e86c6c17-2fae-409b-ba7e-4652de8a356d-instrument-19-audio.wav',0,0.461,120,254.913,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(276,19,'Snare Rim 6','1510f9c1-06c9-4446-b5c8-dd5e05795cd2-instrument-19-audio.wav',0,0.527,120,245,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(277,19,'Snare Rim 5','19673bc5-397d-4f95-81e5-fa70d25aadbb-instrument-19-audio.wav',0,0.463,120,181.481,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(278,19,'Snare Rim','d342a0e9-c065-4a2f-97f0-28f761ff11db-instrument-19-audio.wav',0,1.147,120,239.674,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(279,19,'Snare 5','2a57bffa-b637-47a2-a589-e9cd22a69ec0-instrument-19-audio.wav',0.008,0.407,120,180.738,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(280,19,'Snare 4','97a1200b-f471-46da-b4de-df0b5210eb4d-instrument-19-audio.wav',0.008,0.439,120,204.167,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(281,19,'Snare 3','6278f7cc-71cb-43d0-b0c6-0a195c82bf5a-instrument-19-audio.wav',0.008,0.404,120,2450,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(282,19,'Kick Long 2','dd6f31a1-6deb-4bcd-8ae9-324d040f6a06-instrument-19-audio.wav',0.01,1.476,120,59.036,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(283,19,'Kick 7','9ff7665c-30ba-4615-b843-115605508ac4-instrument-19-audio.wav',0.008,0.537,120,43.534,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(284,19,'Kick 3','b3317d69-1b4b-4b03-8821-121fb03e63db-instrument-19-audio.wav',0.005,0.742,120,52.128,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(285,19,'Kick 3','955f55f6-51c2-467a-b2a3-7dab16653398-instrument-19-audio.wav',0.01,0.677,120,56.178,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(286,19,'Hihat Open 7','2bcfe85c-fc65-4b62-9165-14a05c50cc46-instrument-19-audio.wav',0,2,120,648.529,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(287,19,'Hihat Open 6','c53ba20f-6e11-414c-8ecf-64e8c789419e-instrument-19-audio.wav',0,0.809,120,648.529,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(288,19,'Hihat Open 5','16e2daf3-a7be-4848-b3ba-ea66cb406bb3-instrument-19-audio.wav',0.003,1.115,120,648.529,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(289,19,'Hihat Closed 9','ed1f078c-5142-4b82-8ac0-103378c43ee8-instrument-19-audio.wav',0,0.849,120,648.529,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(290,19,'Hihat Closed 8','86f8f692-2ce9-4613-af27-ec5c9a4f568d-instrument-19-audio.wav',0,0.905,120,648.529,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(291,19,'Hihat Closed 7','b4bebb5b-502b-4dfa-8042-0bd5737cc94c-instrument-19-audio.wav',0.003,0.962,120,8820,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(292,19,'Cymbal Crash 3','a6b28ffc-7083-43d3-9a7e-d301d782b005-instrument-19-audio.wav',0.01,3.044,120,181.481,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(293,19,'Cymbal Crash 2','fd6221db-041b-4876-8ffe-b3e759477713-instrument-19-audio.wav',0.01,3.241,120,469.149,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(294,19,'Cymbal Crash 1','8d7dddf3-9007-446b-b6cc-f846a1bce7a5-instrument-19-audio.wav',0.018,1.878,120,1297.06,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(295,19,'Cowbell','854d3a63-87fc-4158-a697-163a39042caf-instrument-19-audio.wav',0.002,0.298,120,525,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(296,19,'Conga High','13a0a864-8667-4c50-9a74-96efeef74c0d-instrument-19-audio.wav',0.002,0.425,120,187.66,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(297,19,'Conga','4dfa1246-e31b-4673-924a-f5ebb01cedc9-instrument-19-audio.wav',0.001,0.547,120,183.231,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(298,19,'Clap 2','ba4a7b0c-98d6-4857-bda2-64ac758889c6-instrument-19-audio.wav',0,0.684,120,188.462,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(299,19,'Clap 1','778c4db5-2a29-4838-8a8a-0c290de375de-instrument-19-audio.wav',0,0.572,120,185.294,'2017-12-22 19:39:27','2017-12-22 19:39:27','Published'),(309,20,'Tom High 2','6654ddbe-1e2c-4ada-9fc6-2004b4ae64cd-instrument-20-audio.wav',0.002,0.411,120,201.37,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(310,20,'Tom High','cbe69702-d41c-428b-b9cf-ca7696407b1c-instrument-20-audio.wav',0,0.2,120,190.909,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(311,20,'Tom 2','12c47c5a-a730-46ed-b57c-64c8b23066f0-instrument-20-audio.wav',0,0.488,120,149.492,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(312,20,'Tom','7083dbaa-1ac1-423a-9569-c0d880d57b1d-instrument-20-audio.wav',0,0.36,120,104.751,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(313,20,'Snare Rim','af59eb26-e808-4559-ba95-c75406026e98-instrument-20-audio.wav',0,0.014,120,445.445,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(314,20,'Snare','0a1b6f6b-1546-4b2e-a2a9-0a4228ea04eb-instrument-20-audio.wav',0,0.093,120,177.823,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(315,20,'Maracas 2','bc1d0f01-5cf9-4014-8246-cf5191fbae05-instrument-20-audio.wav',0.009,0.43,120,11025,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(316,20,'Maracas','b44bda07-4a94-4eee-90e1-9710fc947e45-instrument-20-audio.wav',0,0.026,120,190.086,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(317,20,'Kick Long 2','280edd64-1b77-4659-a6db-4f27d5b124b1-instrument-20-audio.wav',0,1.963,120,60.494,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(318,20,'Kick Long','97aeb3bd-60f8-4312-9fd7-5ab8795cf391-instrument-20-audio.wav',0,0.865,120,57.05,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(319,20,'Kick 2','2c41b011-dcee-4c5f-88e2-f8d5f4f6d4c6-instrument-20-audio.wav',0,0.34,120,69.122,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(320,20,'Kick','e8082919-6eaf-49ed-a760-b7b87bbaa2a2-instrument-20-audio.wav',0,0.702,120,57.495,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(321,20,'Hihat Open','bd037293-8848-4fb7-a1d3-3f36cd001fbf-instrument-20-audio.wav',0,0.598,120,7350,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(322,20,'Hihat Closed','fac590f3-4326-4676-a347-2de527569d44-instrument-20-audio.wav',0,0.053,120,6300,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(323,20,'Cymbal Crash 2','e060166a-84e4-44e6-b3cf-261163bb99e4-instrument-20-audio.wav',0,2,120,816.667,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(324,20,'Cymbal Crash','04633daf-ba66-4651-923b-c037752300ff-instrument-20-audio.wav',0,2.229,120,109.701,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(325,20,'Cowbell','4d84aac4-3183-4ef2-98ff-e75552f3fd67-instrument-20-audio.wav',0,0.34,120,268.902,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(326,20,'Conga High','a128f13e-95e5-4f76-a759-21f624a90d32-instrument-20-audio.wav',0,0.179,120,397.297,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(327,20,'Conga','81104382-d7b0-48a9-8491-8630566e7924-instrument-20-audio.wav',0,0.26,120,213,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(328,20,'Claves','d158a3c2-8cdf-4910-9ce2-f00c1f388b24-instrument-20-audio.wav',0,0.03,120,2594,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(329,20,'Clap 3','9e6358e0-f643-4e94-8464-cd08f4b71f31-instrument-20-audio.wav',0,0.734,120,980,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(330,20,'Clap 2','5e730a7c-06e7-4b9d-a10d-1b68b721dac0-instrument-20-audio.wav',0.002,0.356,120,1225,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(331,20,'Clap','28b9786c-f0c4-47e8-82f7-0f31477d1539-instrument-20-audio.wav',0,0.361,120,1102.5,'2017-12-22 19:39:49','2017-12-22 19:39:49','Published'),(404,6,'Kick 93','6ca449d5-15a1-4e71-9cdf-fd3fd05207e4-instrument-6-audio.wav',0,1.831,120,106.01,'2017-12-23 22:56:19','2017-12-23 22:56:19','Published'),(405,6,'Kick 92','b9e789ba-3a0a-401b-b5dc-35b3151e094f-instrument-6-audio.wav',0,1.463,120,63.728,'2017-12-23 22:57:23','2017-12-23 22:57:23','Published'),(406,6,'Kick 87','ef18c224-b84c-4eda-83a1-c54fb5c76961-instrument-6-audio.wav',0,1.158,120,64.663,'2017-12-23 22:58:49','2017-12-23 22:58:49','Published'),(407,6,'Kick 86','2e0a359e-aa47-4910-bd8b-c9a637d5b8dc-instrument-6-audio.wav',0,1.046,120,67.951,'2017-12-23 22:59:56','2017-12-23 22:59:56','Published'),(408,6,'Snare Rim 1','981b1042-a8de-436f-8968-6458b3d95c5f-instrument-6-audio.wav',0,0.342,120,280.892,'2017-12-23 23:01:13','2017-12-23 23:01:13','Published'),(409,6,'Snare Rim 2','62f1c244-1b95-40c9-a39d-26d3a21ca2a4-instrument-6-audio.wav',0,0.251,120,1050,'2017-12-23 23:02:14','2017-12-23 23:02:14','Published'),(410,6,'Snare Rim 3','e07d12a4-033a-47a1-9d05-c52a3d69785d-instrument-6-audio.wav',0,0.346,120,1002.27,'2017-12-23 23:03:06','2017-12-23 23:03:06','Published'),(411,6,'Snare Rim 4','ced8d22a-9087-4638-8705-d8736d3c3256-instrument-6-audio.wav',0,0.216,120,304.138,'2017-12-23 23:04:10','2017-12-23 23:04:10','Published'),(412,6,'Snare Rim 6','725a99f2-c1c4-4afb-9baa-8a9d755f4182-instrument-6-audio.wav',0,0.358,120,5512.5,'2017-12-23 23:05:19','2017-12-23 23:05:19','Published'),(413,6,'Shamisen','dff860d6-3999-41c5-8c16-28e767abc6b4-instrument-6-audio.wav',0,1,88,262.5,'2017-12-23 23:39:05','2017-12-23 23:39:05','Published'),(414,6,'Shamisen B','d7cd68a5-87f7-49e1-9ab3-0f7f68d8308d-instrument-6-audio.wav',0.005,1.353,88,264.072,'2017-12-23 23:39:40','2017-12-23 23:39:40','Published'),(415,6,'Shami','a712f331-5cfb-4ceb-b24b-6c3830332921-instrument-6-audio.wav',0.006,0.999,88,262.5,'2017-12-23 23:40:00','2017-12-23 23:40:00','Published'),(416,6,'Koto','ebdc0c16-e52d-4150-8fd6-c1344892d35a-instrument-6-audio.wav',0,1.294,88,132.831,'2017-12-23 23:40:17','2017-12-23 23:40:17','Published'),(419,22,'Snare 1','38a47c75-eea2-4b6b-ab42-e3ebe9f0029f-instrument-22-audio.wav',0.019,0.607,121,321.555,'2017-12-24 00:03:07','2017-12-24 00:03:07','Published'),(420,22,'Snare 2','9e63062b-63d6-4fc0-9774-432e052cb47f-instrument-22-audio.wav',0.015,0.489,121,917.771,'2017-12-24 00:04:41','2017-12-24 00:04:41','Published'),(421,22,'Snare 3','eaff069a-0dc7-4fe6-9e1b-3b36c9f0e07a-instrument-22-audio.wav',0.009,0.633,121,160.777,'2017-12-24 00:06:46','2017-12-24 00:06:46','Published'),(422,22,'Snare 4','80597075-5c1a-4ffd-a7d4-824e8bad1060-instrument-22-audio.wav',0.009,0.595,121,518.271,'2017-12-24 00:08:16','2017-12-24 00:08:16','Published'),(423,22,'Snare 5','47614269-76fe-4a2d-b089-47da3d3ec7b3-instrument-22-audio.wav',0.012,0.57,121,270.264,'2017-12-24 00:13:11','2017-12-24 00:13:11','Published'),(424,22,'Kick B3','3c75f2ab-8f03-45a2-8f45-70361ebe9d07-instrument-22-audio.wav',0,0.719,121,76.085,'2017-12-24 00:15:49','2017-12-24 00:15:49','Published'),(425,22,'Kick B4','284ed5d8-b6e8-4380-912b-7dadb5427cde-instrument-22-audio.wav',0,0.844,121,3671.08,'2017-12-24 00:17:17','2017-12-24 00:17:17','Published'),(426,22,'Tom 1','bf656793-0789-4a4d-a834-b4281a0878f4-instrument-22-audio.wav',0,0.658,121,156.216,'2017-12-24 00:18:45','2017-12-24 00:18:45','Published'),(427,22,'Tom 2','fced1e79-0638-4f09-adb2-0749e90b6472-instrument-22-audio.wav',0,0.207,121,157.896,'2017-12-24 00:20:18','2017-12-24 00:20:18','Published'),(428,22,'Tom 3','8b69aa81-09ce-4c31-aadb-7d4f9204f031-instrument-22-audio.wav',0,0.205,121,156.216,'2017-12-24 00:22:07','2017-12-24 00:22:07','Published'),(429,22,'Tom 4','14528423-3918-47d5-aa39-979b6773b79b-instrument-22-audio.wav',0,0.746,121,147.829,'2017-12-24 00:23:47','2017-12-24 00:23:47','Published'),(430,22,'Tom 5','0140b24b-1b3e-4c9c-bcdb-87588273d0aa-instrument-22-audio.wav',0,0.78,121,148.828,'2017-12-24 00:25:19','2017-12-24 00:25:19','Published'),(431,22,'Hihat Closed 1','ee74fcb7-6f30-4660-807d-972f248cbe09-instrument-22-audio.wav',0.014,0.357,121,1631.59,'2017-12-24 00:28:15','2017-12-24 00:28:15','Published'),(432,22,'Hihat Closed 2','b95b5c60-a4a2-4b61-9230-0efdf9f9d393-instrument-22-audio.wav',0.005,0.232,121,299.68,'2017-12-24 00:29:35','2017-12-24 00:31:08','Published'),(433,22,'Hihat Closed 3','430394e0-f4c1-4823-9977-070222f0d3cf-instrument-22-audio.wav',0.01,0.248,121,3388.69,'2017-12-24 00:30:44','2017-12-24 00:30:44','Published'),(434,22,'Hihat Closed 4','ed1bebe6-2268-48ed-9eae-eea970b5233a-instrument-22-audio.wav',0.005,0.447,121,4004.82,'2017-12-24 00:32:21','2017-12-24 00:32:21','Published'),(435,22,'Hihat Closed 5','58cad5f0-29be-4a48-909c-33805c8066ea-instrument-22-audio.wav',0,0.397,121,3671.08,'2017-12-24 00:34:07','2017-12-24 00:34:07','Published'),(436,22,'Hihat Open 1','9445b057-2460-4a15-800a-165d30a2fb62-instrument-22-audio.wav',0.042,0.426,121,4894.78,'2017-12-24 00:35:45','2017-12-24 00:35:45','Published'),(437,22,'Hihat Open 2','f225c914-f7d3-4182-9a95-2c895c8bf003-instrument-22-audio.wav',0.05,0.467,121,4894.78,'2017-12-24 00:37:12','2017-12-24 00:37:12','Published'),(438,22,'Hihat Open 3','21840b58-2a80-4fe3-9c40-95669a634059-instrument-22-audio.wav',0.048,0.453,121,4894.78,'2017-12-24 00:38:27','2017-12-24 00:38:27','Published'),(439,22,'Hihat Open 4','1af2f869-415c-460f-80dd-61d4e0dfbdc8-instrument-22-audio.wav',0.048,0.456,121,4894.78,'2017-12-24 00:39:33','2017-12-24 00:39:33','Published'),(440,23,'Kick 16','9ce45a64-75e3-4f73-9079-d262c7c804ce-instrument-23-audio.wav',0,0.808,121,59.274,'2017-12-24 01:17:41','2017-12-24 01:17:41','Published'),(441,23,'Kick 23','25479110-15ad-419c-9a77-f9fd4a185d3c-instrument-23-audio.wav',0,0.992,121,52.814,'2017-12-24 01:18:21','2017-12-24 01:18:21','Published'),(442,23,'Kick 32','debe29e9-31ff-4371-8107-54e5d46e9894-instrument-23-audio.wav',0,0.753,121,56.394,'2017-12-24 01:19:12','2017-12-24 01:19:12','Published'),(443,23,'Kick 38','ef62fa56-8e31-4a40-bd4c-6dc22db77be9-instrument-23-audio.wav',0,0.774,121,58.8,'2017-12-24 01:20:05','2017-12-24 01:20:05','Published'),(444,23,'Snare 3','1e1303f5-8103-4c8f-b09d-867d8c3e3938-instrument-23-audio.wav',0,1.057,121,270.552,'2017-12-24 01:24:04','2017-12-24 01:24:04','Published'),(447,23,'Snare 7','7e61bb84-ceb9-4c3e-b666-30e5c0de80f4-instrument-23-audio.wav',0,1.123,121,270.552,'2017-12-24 01:26:45','2017-12-24 01:26:45','Published'),(448,23,'Hihat Closed (Shaker) 1','51b05142-5586-47cc-a588-98bfd12420d4-instrument-23-audio.wav',0.025,0.1,121,2321.05,'2017-12-24 01:28:32','2017-12-24 01:36:40','Published'),(449,23,'Hihat Closed (Shaker) 3','50c919b3-f288-4161-b888-d7ef2cd69fca-instrument-23-audio.wav',0.015,0.087,121,6300,'2017-12-24 01:30:25','2017-12-24 01:36:33','Published'),(450,23,'Hihat Closed (Shaker) 5','c65c25e2-32e9-43e5-8560-27786502647d-instrument-23-audio.wav',0.015,0.092,121,6300,'2017-12-24 01:31:42','2017-12-24 01:36:26','Published'),(451,23,'Hihat Closed (Shaker) 4','b8307f46-3960-4de5-870c-62f5e5034f04-instrument-23-audio.wav',0.015,0.086,121,7350,'2017-12-24 01:32:45','2017-12-24 01:36:47','Published'),(452,23,'Hihat Open (Tambourine) 1','ab293ea7-b6cd-49f8-b36b-b6781ecf84ce-instrument-23-audio.wav',0,0.297,121,6300,'2017-12-24 01:33:52','2017-12-24 01:37:03','Published'),(453,23,'Hihat Open (Tambourine) 5','ed11b9f2-1196-4710-83ea-7d8e028c5473-instrument-23-audio.wav',0,0.262,121,2594.12,'2017-12-24 01:34:35','2017-12-24 01:37:11','Published'),(454,23,'Hihat Open (Tambourine) 4','96f48a20-75b5-46b4-9004-3ae9fd9fea52-instrument-23-audio.wav',0,0.148,121,4594.12,'2017-12-24 01:35:48','2017-12-24 01:35:48','Published'),(455,23,'Hihat Open (Tambourine) 2','20db0657-f27a-4eab-a9d5-a4be41f1b41e-instrument-23-audio.wav',0,0.321,121,6300,'2017-12-24 01:37:47','2017-12-24 01:37:47','Published'),(456,23,'Tom High A1','d1c31eb3-1023-4520-aead-7532e9495c59-instrument-23-audio.wav',0,1.065,121,164.552,'2017-12-24 01:42:42','2017-12-24 01:42:42','Published'),(457,23,'Tom Low A2','963bade6-33bf-4383-9966-af69f119413e-instrument-23-audio.wav',0,1.457,121,111.646,'2017-12-24 01:43:48','2017-12-24 01:43:48','Published'),(458,23,'Tom Low D2','1ed08be5-d05c-41ef-82a8-85473d31903b-instrument-23-audio.wav',0,0.997,121,96.248,'2017-12-24 01:45:11','2017-12-24 01:45:11','Published'),(459,23,'Tom High D1','de419cd8-22ff-4eac-bba4-02cea70bbd9a-instrument-23-audio.wav',0,0.859,121,179.268,'2017-12-24 01:46:51','2017-12-24 01:46:51','Published'),(460,23,'Tom High E1','b3a8d5f7-1429-427d-8863-89f4e57c945f-instrument-23-audio.wav',0,2.049,121,161.538,'2017-12-24 01:49:05','2017-12-24 01:49:05','Published'),(461,23,'Tom Low E4','52650a08-6ddc-4a88-ab8d-14d88444ac95-instrument-23-audio.wav',0,2.194,121,81.818,'2017-12-24 01:49:45','2017-12-24 01:49:45','Published'),(462,24,'Kick 70','fe338721-b9f8-49f9-a441-3029c49350c9-instrument-24-audio.wav',0,0.294,121,79.032,'2017-12-24 02:47:07','2017-12-24 02:47:34','Published'),(463,24,'Kick 0','5a95a1f6-9b3c-4e91-920c-1f5a3a2a9e59-instrument-24-audio.wav',0,0.1,121,153.659,'2017-12-24 02:48:24','2017-12-24 02:48:24','Published'),(466,24,'Kick 30','c94ab7a6-0b57-430f-86ac-9082406a5925-instrument-24-audio.wav',0,0.473,121,5512.5,'2017-12-24 02:51:45','2017-12-24 02:51:45','Published'),(467,24,'Snare 30','88a4d2f9-8a5f-4ffd-a4d1-ab4bf7dcc534-instrument-24-audio.wav',0,1.268,121,235.829,'2017-12-24 02:53:13','2017-12-24 02:53:13','Published'),(468,24,'Snare 34','f4b1b81e-c0ae-45d8-ba73-0c4645ca24fc-instrument-24-audio.wav',0,0.462,121,6300,'2017-12-24 02:53:52','2017-12-24 02:53:52','Published'),(469,24,'Snare 33','238a769c-c3c0-41f7-95b9-b115ad464828-instrument-24-audio.wav',0,1.289,121,240.984,'2017-12-24 02:54:29','2017-12-24 02:54:29','Published'),(470,24,'Snare 34','5898787a-56a5-409b-815d-342154751f01-instrument-24-audio.wav',0,1.372,121,172.266,'2017-12-24 02:55:33','2017-12-24 02:56:00','Published'),(471,24,'Snare 31','f5b656f3-6fad-42ee-aba2-6e322e1d7abf-instrument-24-audio.wav',0,1.834,121,175.697,'2017-12-24 02:56:25','2017-12-24 02:56:25','Published'),(472,23,'Snare 44','a1a6aa1e-e1d9-4746-a07c-f1202d66e725-instrument-23-audio.wav',0,0.458,121,11025,'2017-12-24 02:57:24','2017-12-24 02:57:24','Published'),(473,24,'Hihat Closed 1 Pedal','97981a8c-a285-4422-8a1b-c980a146aeeb-instrument-24-audio.wav',0.02,0.13,121,6300,'2017-12-24 02:59:59','2017-12-24 02:59:59','Published'),(474,24,'Hihat Closed 1','eb65e484-8c65-4224-8848-97e21dc3f50e-instrument-24-audio.wav',0.001,0.172,121,373.729,'2017-12-24 03:01:02','2017-12-24 03:01:02','Published'),(475,24,'Hihat Closed 1 Edge','0ca183cd-58de-4136-bd84-630729ac1105-instrument-24-audio.wav',0.003,0.204,121,537.805,'2017-12-24 03:02:05','2017-12-24 03:02:05','Published'),(476,24,'Hihat Closed 2','d820b5a2-5e2c-47fe-8b6e-1fd40cdd52fc-instrument-24-audio.wav',0,0.149,121,7350,'2017-12-24 03:03:13','2017-12-24 03:03:13','Published'),(477,24,'Hihat Open 1 Half','a13a7c70-269f-49fc-b556-8835c0ca8d90-instrument-24-audio.wav',0.001,0.457,121,331.579,'2017-12-24 03:04:16','2017-12-24 03:04:16','Published'),(478,24,'Hihat Open 1 Half Edge','6fa597cc-a604-47b6-b396-76e2db850ac7-instrument-24-audio.wav',0.01,0.59,121,518.824,'2017-12-24 03:05:05','2017-12-24 03:05:05','Published'),(479,24,'Hihat Open 1','79682fb7-e17f-4fe9-b345-7228fa71ddfb-instrument-24-audio.wav',0.01,1.39,121,331.579,'2017-12-24 03:06:02','2017-12-24 03:06:02','Published'),(480,24,'Hihat Open 2 Half Edge','a5e82f42-70f1-4b06-b75b-097d5ce97f0a-instrument-24-audio.wav',0.001,0.455,121,7350,'2017-12-24 03:07:08','2017-12-24 03:07:08','Published'),(481,24,'Tom B1','d2fe5ccf-3f3f-4d89-a852-3c1d7338ee5f-instrument-24-audio.wav',0,1.158,121,148.986,'2017-12-24 03:08:37','2017-12-24 03:08:37','Published'),(482,24,'Tom B2','435dc32e-19c5-44f1-9690-294499a8aa1e-instrument-24-audio.wav',0,1.55,121,136.533,'2017-12-24 03:09:26','2017-12-24 03:09:26','Published'),(483,24,'Tom B3','36c41b61-62ef-4ee8-a6d1-f6f25123036a-instrument-24-audio.wav',0,1.858,121,104.255,'2017-12-24 03:10:10','2017-12-24 03:10:10','Published'),(484,22,'Crash 2','492bd0f2-143a-40eb-8b2b-b064362a9fac-instrument-22-audio.wav',0,4.024,121,4009.09,'2018-01-05 09:51:48','2018-01-05 09:51:48','Published'),(485,22,'Crash 1','2f847d98-2509-458c-9010-65f405eb355a-instrument-22-audio.wav',0,4.032,121,4900,'2018-01-05 09:53:27','2018-01-05 09:53:27','Published'),(486,24,'Crash 7','c3c1f2bb-72b5-4980-9cc2-4ba3c839101e-instrument-24-audio.wav',0,3.175,121,678.462,'2018-01-05 09:56:08','2018-01-05 09:56:08','Published'),(487,6,'Crash 14','3684983b-b497-4ba5-b1bd-77eae0330b59-instrument-6-audio.wav',0,1.479,121,518.824,'2018-01-05 09:59:30','2018-01-05 09:59:30','Published'),(488,23,'Crash 12','00daf4ac-6b16-463b-9040-feebef0eb69c-instrument-23-audio.wav',0,1.768,121,4900,'2018-01-05 10:00:47','2018-01-05 10:00:47','Published'),(489,6,'Hihat Closed 15','237f9998-42ab-42ed-ae4a-d029ba8541d2-instrument-6-audio.wav',0,0.314,121,205.855,'2018-01-05 10:17:53','2018-01-05 10:18:56','Published'),(490,6,'Hihat Closed 14','946723e0-9ea9-45f5-8728-45e671ce9b42-instrument-6-audio.wav',0,0.293,121,620.465,'2018-01-05 10:20:08','2018-01-05 10:20:08','Published'),(491,6,'Hihat Open 14','f4a325f9-012d-45c3-b8b8-604638e6a540-instrument-6-audio.wav',0,0.422,121,149.84,'2018-01-05 10:22:35','2018-01-05 10:22:35','Published'),(492,6,'Hihat Open 12','b887f98f-fe87-4cc6-8e07-7d30d56cedf0-instrument-6-audio.wav',0,0.41,121,155.116,'2018-01-05 10:30:36','2018-01-05 10:30:36','Published'),(493,25,'Clap','d38499b7-cd9c-40b5-b858-69c6b867d614-instrument-25-audio.wav',0.002,0.159,121,595.946,'2018-02-09 21:34:06','2018-02-09 21:34:06','Published'),(495,27,'Hi-Hat w/ Reverb','9a8cd7f2-f68c-4425-8233-c6d77ed2cfcb-instrument-27-audio.wav',0.0002,2.5,121,3692.31,'2018-03-06 06:08:17','2018-03-06 06:08:17','Published'),(497,27,'Hi-Hat Dry','f4f989e9-fd15-48ca-9795-57bcfa72783d-instrument-27-audio.wav',0.0002,0.5,121,3692.31,'2018-03-06 06:16:45','2018-03-06 06:16:45','Published'),(499,27,'Hi-Hat Open','dc725dea-5c27-4f16-aba4-a9ba84ed313b-instrument-27-audio.wav',0,0.5,121,4800,'2018-03-06 06:21:35','2018-03-06 06:21:35','Published'),(500,27,'Hi-Hat Open w/ Reverb','0388acb2-a8ea-4e4f-a3b3-907153ba4ec1-instrument-27-audio.wav',0,2.125,121,4800,'2018-03-06 06:23:43','2018-03-06 06:23:43','Published'),(501,27,'Tambourine','8335ff25-b151-4ef3-a2c9-56dad8f2a2de-instrument-27-audio.wav',0,2.125,121,2823.53,'2018-03-06 06:26:41','2018-03-06 06:27:57','Published'),(502,27,'Spacey Shaker','2b7286b1-fb7f-406e-8b94-9a0d44506860-instrument-27-audio.wav',0,1.75,121,4800,'2018-03-06 06:42:50','2018-03-06 06:42:50','Published'),(503,27,'Spacey Clave','3091be9a-37c1-4c2c-bb4e-12d076584838-instrument-27-audio.wav',0,2.375,121,2823.53,'2018-03-06 06:46:08','2018-03-06 06:46:08','Published'),(504,27,'Clave w/ Reverb','8d02968d-1453-42db-a80d-fbca37f2997b-instrument-27-audio.wav',0,1.75,121,2823.53,'2018-03-06 06:48:58','2018-03-06 06:48:58','Published'),(505,27,'Spacey Cowbell High','2174c84d-c6f0-4acc-be17-b70f7932e014-instrument-27-audio.wav',0,2.625,121,705.882,'2018-03-06 06:51:48','2018-03-06 06:51:48','Published'),(506,27,'Spacey Cowbell Low','81cef961-c5fc-4952-ade8-b7e0fccdd7e0-instrument-27-audio.wav',0,2.5,121,452.83,'2018-03-06 06:54:04','2018-03-06 06:54:04','Published'),(507,27,'Rim Click w/ Long Reverb Tail','3d3d1672-ee60-48bd-b7cf-01ce33d040e2-instrument-27-audio.wav',0.0002,2.125,121,400,'2018-03-06 06:57:24','2018-03-06 06:58:43','Published'),(508,27,'Knocky Rim Click w/ Short Reverb Tail','3e646e1a-85c0-46b6-947c-a054689c07a2-instrument-27-audio.wav',0.0003,1.25,121,251.309,'2018-03-06 07:02:42','2018-03-06 07:02:42','Published'),(509,27,'Springy Clap','78e1797e-cd64-423d-ba8f-2183edeae8d6-instrument-27-audio.wav',0.0011,2.75,121,623.377,'2018-03-06 07:05:47','2018-03-06 07:05:47','Published'),(510,27,'Springy Clap 2: Return of the Springy Clap','a16ab5e9-0544-4430-9d96-acdfe82c8389-instrument-27-audio.wav',0.0005,2,121,8000,'2018-03-06 07:09:38','2018-03-06 07:09:38','Published'),(511,27,'Dry 808 Snare','0b4c697a-bdd4-4f7c-a0c9-91a74f0e493a-instrument-27-audio.wav',0,0.25,121,192,'2018-03-06 07:12:12','2018-03-06 07:12:12','Published'),(512,27,'Snare w/ Reverb Tail','6cce584f-d2a3-42ac-95e0-03f3656b5c79-instrument-27-audio.wav',0,1,121,2400,'2018-03-06 07:15:12','2018-03-06 07:15:12','Published'),(513,27,'Knocky Open Kick','75baff55-6fc4-4370-8d97-d0560de5c9b6-instrument-27-audio.wav',0.004,0.75,121,127.321,'2018-03-06 07:18:27','2018-03-06 07:19:12','Published'),(514,28,'Crispy Snare','baebb6b9-a4b9-4686-9c78-1222135faff3-instrument-28-audio.wav',0.002,0.25,121,187.5,'2018-03-20 00:43:21','2018-03-20 00:43:21','Published'),(515,28,'Fat Snare','f92fa9e7-a7e9-43c0-93fa-4df241b476a9-instrument-28-audio.wav',0,0.375,121,3692.31,'2018-03-20 00:48:03','2018-03-20 01:29:45','Published'),(516,28,'Long Snare','f4144bde-9ccd-4851-b855-f00280689b2a-instrument-28-audio.wav',0,0.375,121,142.433,'2018-03-20 00:51:42','2018-03-20 00:51:42','Published'),(517,28,'Snappy Rim Click','cb5fb85c-4aa5-4359-824f-bff600563ea0-instrument-28-audio.wav',0.0002,0.375,121,1170.73,'2018-03-20 00:54:37','2018-03-20 00:54:37','Published'),(518,28,'Punchy Kick','5b1c7886-c39e-4904-8621-670e7b40f789-instrument-28-audio.wav',0.012,0.375,121,67.321,'2018-03-20 00:59:11','2018-03-20 00:59:11','Published'),(519,28,'Open Kick','24012246-e99a-4391-a1e9-800b5fcf3e7d-instrument-28-audio.wav',0.0004,0.625,121,85.409,'2018-03-20 01:02:05','2018-03-20 01:02:50','Published'),(520,28,'Open Kick 2','6f884c6b-a519-4bff-aaac-ac9eb57fcade-instrument-28-audio.wav',0,0.625,121,127.321,'2018-03-20 01:05:31','2018-03-20 01:05:31','Published'),(521,28,'Open Kick 3','10290e3b-a443-4354-a82c-d153c21515ae-instrument-28-audio.wav',0.0002,0.375,121,124.031,'2018-03-20 01:14:12','2018-03-20 01:14:12','Published'),(522,28,'Punchy Kick 2','a736fa1d-cece-4f2b-b2ab-966969c4b9af-instrument-28-audio.wav',0.0001,0.375,121,8000,'2018-03-20 01:18:04','2018-03-20 01:18:04','Published'),(524,28,'Punchy Kick 3','ee1d1039-a388-4e5f-9504-77a40d59ab5f-instrument-28-audio.wav',0.0002,0.375,121,8000,'2018-03-20 01:23:15','2018-03-20 01:23:15','Published'),(525,28,'Punchy Kick 4','4066c8ba-28ad-464c-a87a-ebc6107b2de3-instrument-28-audio.wav',0.0002,0.375,121,95.618,'2018-03-20 01:25:11','2018-03-20 01:25:11','Published'),(526,28,'Djembe Palm','0dcb66b5-e8e7-41b3-87df-7e3f5c0cd32d-instrument-28-audio.wav',0,0.375,121,238.806,'2018-03-20 01:28:16','2018-03-20 01:28:16','Published'),(527,28,'Djembe Palm 2','06b4f0fa-13eb-460d-957b-f08a5fda76d2-instrument-28-audio.wav',0.0021,0.625,121,83.189,'2018-03-20 01:31:58','2018-03-20 01:31:58','Published'),(528,28,'Djembe Palm 3','02007c9d-6785-404f-a1c6-1d6fb1084636-instrument-28-audio.wav',0.0028,1,121,73.059,'2018-03-20 01:38:36','2018-03-20 01:38:36','Published'),(530,28,'Djembe Rattle','824ac18d-cf19-4ebe-9f4b-d2a16a20a526-instrument-28-audio.wav',0.0003,0.5,121,12000,'2018-03-20 01:43:03','2018-03-20 01:43:03','Published'),(531,28,'Djembe Slap ','4685744e-ab0c-4cc0-904e-9c2698df6e96-instrument-28-audio.wav',0.0004,0.375,121,238.806,'2018-03-20 01:46:52','2018-03-20 01:46:52','Published'),(532,28,'Djembe Slap 2','2d3b1bc7-919a-4419-abc4-25acdf57ee9d-instrument-28-audio.wav',0.0009,0.375,121,375,'2018-03-20 01:48:51','2018-03-20 01:48:51','Published'),(533,28,'Djembe Slap 3','1dc69f00-2372-4f05-91ae-82b0c6234a3f-instrument-28-audio.wav',0.0022,0.375,121,640,'2018-03-20 01:50:53','2018-03-20 01:50:53','Published'),(534,28,'Kenkeni','e5c79d56-7dca-4dfe-8d3e-c6cd4215d798-instrument-28-audio.wav',0.0047,0.5,121,80.672,'2018-03-20 01:53:07','2018-03-20 01:53:07','Published'),(535,28,'Kenkeni 2','9edcee06-73e2-4fcd-80c1-d0b58ecf30c6-instrument-28-audio.wav',0,1.5,121,85.258,'2018-03-20 01:54:58','2018-03-20 01:54:58','Published'),(536,28,'Kenkeni 3','554de69f-0434-42d0-989c-dc7a02179dec-instrument-28-audio.wav',0,1,121,46.967,'2018-03-20 01:57:06','2018-03-20 01:57:06','Published'),(537,28,'Kenkeni 4','5b5ae8cd-5987-49fa-9c48-bd461e0ef90a-instrument-28-audio.wav',0,1.25,121,125,'2018-03-20 01:59:39','2018-03-20 01:59:39','Published'),(538,28,'Kenkeni 5','ce6f3bb6-3d7d-4c2f-ab3a-70899d3f737e-instrument-28-audio.wav',0,0.5,121,77.922,'2018-03-20 02:01:16','2018-03-20 02:01:16','Published'),(539,28,'Kenkeni 6','8a4c6a10-841f-46d7-94e3-be8370624824-instrument-28-audio.wav',0,0.5,121,4363.64,'2018-03-20 02:03:00','2018-03-20 02:03:00','Published'),(540,28,'Kenkeni 7','ac215ada-a632-456d-9894-305ca6e13fc0-instrument-28-audio.wav',0.0003,1.375,121,110.092,'2018-03-20 02:05:36','2018-03-20 02:05:36','Published'),(541,28,'Dun Dun Bell','81fda5b3-33e5-48ac-9304-ead26b731275-instrument-28-audio.wav',0.0005,0.25,121,3428.57,'2018-03-20 02:07:44','2018-03-20 02:07:44','Published'),(542,28,'Dun Dun Da Bell','fad125f5-b8a1-449d-acad-24ac69d74043-instrument-28-audio.wav',0,0.25,121,1600,'2018-03-20 02:10:01','2018-03-20 02:10:01','Published'),(543,28,'Sangpan Bell','bac419d8-2391-4b62-b45c-71122f8c2df6-instrument-28-audio.wav',0.0001,0.25,121,615.385,'2018-03-20 02:12:06','2018-03-20 02:12:06','Published'),(544,28,'Shaker','74c870b3-df58-4aea-80ad-aa2d10b64bd4-instrument-28-audio.wav',0,0.203,121,6000,'2018-03-20 02:14:33','2018-03-20 02:14:33','Published'),(545,28,'Shaker 2','d48bc52b-95f4-4a5c-81f2-804ff13b84d3-instrument-28-audio.wav',0,0.203,121,5333.33,'2018-03-20 02:16:11','2018-03-20 02:16:11','Published'),(546,28,'Shaker 3','a20f3994-de53-4648-abb2-77370b098db5-instrument-28-audio.wav',0,0.203,121,12000,'2018-03-20 02:17:39','2018-03-20 02:17:39','Published'),(547,28,'Weird Snap','7793c912-032b-41e4-9fcf-c2d43df4389d-instrument-28-audio.wav',0.0006,0.25,121,8000,'2018-03-20 02:19:28','2018-03-20 02:19:28','Published'),(548,29,'Aggressive 909 Kick','e37114ef-b92d-4e30-9fd7-3be0ff42efb3-instrument-29-audio.wav',0.006,0.328,121,199.17,'2018-03-26 16:17:24','2018-03-26 16:17:24','Published'),(549,29,'Super Aggressive 909 Kick','4c5bd49a-7662-460c-8c3a-c9f5745a5882-instrument-29-audio.wav',0.0056,0.5,121,183.908,'2018-03-26 16:20:27','2018-03-26 16:20:27','Published'),(550,29,'Punchy Kick','c9edfab8-eab3-41a3-b58e-eb5d514fb134-instrument-29-audio.wav',0.0001,0.156,121,91.429,'2018-03-26 16:23:29','2018-03-26 16:23:29','Published'),(551,29,'Gated Industrial Kick','ef7b2620-beaa-44b3-a07e-ac0498ab7831-instrument-29-audio.wav',0.0003,0.594,121,86.331,'2018-03-26 16:25:29','2018-03-26 16:25:29','Published'),(552,29,'Clicky Kick','7ece3e86-84fd-4c77-820d-442ee2b217cb-instrument-29-audio.wav',0,0.266,121,85.258,'2018-03-26 16:27:48','2018-03-26 16:27:48','Published'),(553,29,'Muted Explosive Kick','48a784a4-a36a-41ab-ac21-e8738e6a2c54-instrument-29-audio.wav',0.003,0.375,121,86.799,'2018-03-26 16:29:42','2018-03-26 16:29:42','Published'),(554,29,'Open Industrial Kick','8d42e8e8-1286-4e71-b537-ceea1d925934-instrument-29-audio.wav',0.0035,0.594,121,80.402,'2018-03-26 16:32:02','2018-03-26 16:32:02','Published'),(555,29,'Clicky Industrial Kick','2e89ef50-4ded-49b5-9c2c-8c826eb0e49d-instrument-29-audio.wav',0,0.391,121,1920,'2018-03-26 16:33:53','2018-03-26 16:33:53','Published'),(556,29,'Moog Snare 2','dda386d5-d093-428a-a39c-78496d2d7ff3-instrument-29-audio.wav',0,0.375,121,139.535,'2018-03-26 16:36:00','2018-03-26 16:36:00','Published'),(557,29,'Moog Snare 1','be6cd4fc-9835-4ec3-9628-bbbae5655b69-instrument-29-audio.wav',0,0.5,121,125.326,'2018-03-26 16:37:27','2018-03-26 16:37:27','Published'),(558,29,'Moog Snare 3','a997bca3-4830-407a-b787-e2a569304a88-instrument-29-audio.wav',0,0.5,121,307.692,'2018-03-26 16:39:13','2018-03-26 16:39:13','Published'),(559,29,'Moog Snare 4','91944422-ddab-48e6-b152-627afa877399-instrument-29-audio.wav',0,0.25,121,137.931,'2018-03-26 16:40:44','2018-03-26 16:40:44','Published'),(560,29,'Closed Hat 3','8722e12a-c1bb-405f-b9da-c90f1c10fed0-instrument-29-audio.wav',0,0.188,121,393.443,'2018-03-26 16:46:06','2018-03-26 16:46:06','Published'),(561,29,'Closed Hat 1','886c3d7c-7ad3-4c7d-a694-7e3e76671538-instrument-29-audio.wav',0.0002,0.328,121,4800,'2018-03-26 16:49:34','2018-03-26 16:49:34','Published'),(562,29,'Closed Hat 2','effafa95-9756-4119-84b8-4452fb00b160-instrument-29-audio.wav',0.0002,0.188,121,666.667,'2018-03-26 16:51:02','2018-03-26 16:51:02','Published'),(563,29,'Closed Hat 4','e84f17a2-34e3-49ee-9c0a-aa71ecef0880-instrument-29-audio.wav',0.0001,0.25,121,5333.33,'2018-03-26 16:52:58','2018-03-26 16:52:58','Published'),(564,29,'CLOSED HAT 5','5c32dc94-496d-412d-b4db-4839d905b074-instrument-29-audio.wav',0.0001,0.375,121,5333.33,'2018-03-26 16:54:20','2018-03-26 16:54:20','Published'),(565,29,'Open To Closed Hat','979f0b39-c642-4f2d-b31b-f9dd327f6f94-instrument-29-audio.wav',0.0015,0.328,121,4800,'2018-03-26 16:56:43','2018-03-26 16:56:43','Published'),(566,29,'Crunchy Snare','c5d6a0cb-e103-417b-b450-77eb355099a3-instrument-29-audio.wav',0.0002,0.375,121,3200,'2018-03-26 16:58:21','2018-03-26 16:58:21','Published'),(567,29,'Flare Up Snare','5575ef02-03a6-458c-b0bb-a5c975819b7a-instrument-29-audio.wav',0.0048,0.375,121,8000,'2018-03-26 16:59:50','2018-03-26 16:59:50','Published'),(568,29,'Industrial Undulating Percussion','a3c983f4-b471-4603-8a94-25207a783056-instrument-29-audio.wav',0,0.625,121,126.649,'2018-03-26 17:02:01','2018-03-26 17:02:01','Published'),(569,29,'Popcorn Snare','c70e0246-bba7-448c-a45a-3876d9105f8a-instrument-29-audio.wav',0.0002,0.375,121,375,'2018-03-26 17:04:28','2018-03-26 17:04:28','Published'),(570,29,'Powering Down','1b6221ea-7f22-4845-b93f-50e901755c43-instrument-29-audio.wav',0,2,121,4000,'2018-03-26 17:05:52','2018-03-26 17:05:52','Published'),(571,29,'Tight Acoustic Snare','e1f9f407-41a9-4f71-b175-8ef43d38cbe9-instrument-29-audio.wav',0,0.375,121,3692.31,'2018-03-26 17:07:57','2018-03-26 17:07:57','Published'),(572,29,'Rough and Sandy Snare','6c177d34-fe6e-49a7-a44d-1516c49bfc26-instrument-29-audio.wav',0,0.625,121,80.402,'2018-03-26 17:09:45','2018-03-26 17:09:45','Published'),(573,29,'Rough and Sandy Crash','6f440597-40e8-4d9d-8855-6d889c827df6-instrument-29-audio.wav',0.0002,2,121,1230.77,'2018-03-26 17:11:09','2018-03-26 17:11:09','Published'),(574,29,'Small Snare','9bf9e365-c11a-4dd6-80a0-b2c2b4fa5d28-instrument-29-audio.wav',0,0.25,121,282.353,'2018-03-26 17:12:38','2018-03-26 17:12:38','Published'),(575,29,'Snappy Snare 2','086e9930-f691-45d8-a254-aaf3660f4e4a-instrument-29-audio.wav',0,0.375,121,214.286,'2018-03-26 17:14:01','2018-03-26 17:14:01','Published'),(576,29,'Snappy Snare 3','7f17ecca-4e1e-4177-bed5-3f5bcf273d65-instrument-29-audio.wav',0.0144,0.375,121,9600,'2018-03-26 17:15:28','2018-03-26 17:15:28','Published'),(577,29,'Snappy Snare','c9384a2c-a4fc-4b2f-8ea5-50069b4c3988-instrument-29-audio.wav',0.0001,0.375,121,1714.29,'2018-03-26 17:16:54','2018-03-26 17:16:54','Published'),(578,29,'Tom 1','203c9a5f-6d89-4072-b1d6-77c36bfe151d-instrument-29-audio.wav',0,0.25,121,167.832,'2018-03-26 17:18:30','2018-03-26 17:18:30','Published'),(579,29,'Tom 2','38adc853-2272-496f-b129-5805b9226a21-instrument-29-audio.wav',0,0.375,121,134.078,'2018-03-26 17:19:53','2018-03-26 17:19:53','Published'),(580,29,'Tom 3','f557e49a-abb3-4ccc-87e2-1595924237fa-instrument-29-audio.wav',0,0.375,121,107.383,'2018-03-26 17:21:21','2018-03-26 17:21:21','Published'),(581,30,'Cabasa','1a9f7a28-e746-438c-a04c-fc148c54ca63-instrument-30-audio.wav',0,0.25,121,4000,'2018-03-27 21:18:04','2018-03-27 21:18:04','Published'),(582,30,'Clap-like Percussion','9acfb60f-9112-4a6c-9697-f5270583c81b-instrument-30-audio.wav',0.0001,0.25,121,8000,'2018-03-27 21:20:08','2018-03-27 21:20:08','Published'),(583,30,'Dubbed Out Clave','20e27f8a-914f-4091-a2df-769069497ae8-instrument-30-audio.wav',0,6.215,121,1777.78,'2018-03-27 21:21:46','2018-03-27 21:21:46','Published'),(584,30,'Dubby Fog Horn','26532e58-f9d9-4e99-a916-0d10f83ad9a9-instrument-30-audio.wav',0.1083,9.25,121,121.212,'2018-03-27 21:23:21','2018-03-27 21:23:21','Published'),(585,30,'Kick with Heavy Attack and Heavy Sub','3d21fbf5-94fd-4f24-853a-f449bff74d6a-instrument-30-audio.wav',0.0001,0.875,121,103.448,'2018-03-27 21:25:53','2018-03-27 21:25:53','Published'),(586,30,'Metallic Snare','417cfa8c-f708-44e7-b06f-497dcc56d4a6-instrument-30-audio.wav',0.0001,0.25,121,322.148,'2018-03-27 21:27:20','2018-03-27 21:27:20','Published'),(587,30,'Knocky Muted Tom','95d5369a-71aa-4b2f-be0e-c72044091307-instrument-30-audio.wav',0.0006,0.219,121,193.548,'2018-03-27 21:28:55','2018-03-27 21:28:55','Published'),(588,30,'Digital Percussive Flam','fecb3546-cd26-4ffe-a226-6822a653b9de-instrument-30-audio.wav',0.0004,0.25,121,226.415,'2018-03-27 21:30:18','2018-03-27 21:30:18','Published'),(589,30,'Percussive Flam','9bd6fafa-0363-45d5-a17d-05ee8a40095b-instrument-30-audio.wav',0.0075,0.281,121,1263.16,'2018-03-27 21:33:02','2018-03-27 21:33:02','Published'),(590,30,'Scraper','1c06528f-5a40-420a-aa25-4f9cde9029ba-instrument-30-audio.wav',0.0002,0.281,121,6000,'2018-03-27 21:35:06','2018-03-27 21:35:06','Published'),(591,30,'Slammed Phasey Closed Hat 2','05f0cbaa-b02d-46e8-8bec-dcb96090038f-instrument-30-audio.wav',0,0.375,121,2666.67,'2018-03-27 21:36:57','2018-03-27 21:36:57','Published'),(592,30,'Slammed Phasey Closed Hat 1','3e08d363-5826-4d4a-974c-c6730c40a9aa-instrument-30-audio.wav',0,0.375,121,6000,'2018-03-27 21:38:19','2018-03-27 21:38:19','Published'),(593,30,'Electronic Small Snare','b94c3696-ca2a-4d61-b6fe-7108908f4074-instrument-30-audio.wav',0,0.344,121,4800,'2018-03-27 21:39:51','2018-03-27 21:39:51','Published'),(594,30,'Tight Dead Acoustic Snare','ef02250e-6420-45dd-92f2-21e736df1545-instrument-30-audio.wav',0.0003,0.25,121,428.571,'2018-03-27 21:41:41','2018-03-27 21:41:41','Published'),(595,30,'Tight Dead Snare 2','5ff14782-bf9b-4f0d-8529-bba612872b1d-instrument-30-audio.wav',0.0003,0.25,121,84.956,'2018-03-27 21:44:17','2018-03-27 21:44:17','Published'),(596,30,'Dead Studio Snare','0c460941-c090-4f71-9ff5-a632319cb5c9-instrument-30-audio.wav',0,0.25,121,186.047,'2018-03-27 21:46:26','2018-03-27 21:46:26','Published'),(597,30,'Basketball-like Snare','446caeea-a77e-47d8-a69f-1a44ec1f4f74-instrument-30-audio.wav',0.0002,0.25,121,4800,'2018-03-27 21:48:06','2018-03-27 21:48:06','Published'),(598,30,'Tabla','f34dfe75-5239-45d8-9b06-d1b6e4240199-instrument-30-audio.wav',0.0002,0.594,121,77.544,'2018-03-27 21:49:15','2018-03-27 21:49:15','Published'),(599,30,'Electronic Tom w/ Slapback','c9221c0a-7748-4d54-ba67-6edf709fd42b-instrument-30-audio.wav',0.001,0.875,121,3428.57,'2018-03-27 21:51:51','2018-03-27 21:51:51','Published'),(600,30,'Electronic Tom w/ Slapback 2','a2aa200b-5f88-47f8-a9f4-7ff5e12156d0-instrument-30-audio.wav',0.009,0.875,121,3428.57,'2018-03-27 21:54:24','2018-03-27 21:54:24','Published'),(601,30,'Undulating Low Tom/Kick','c23128ff-fe96-4e9b-b356-f1b3cb951a2d-instrument-30-audio.wav',0,0.625,121,269.663,'2018-03-27 21:56:03','2018-03-27 21:56:03','Published'),(602,30,'White Noise Crash','07812c0f-d7f6-425b-8964-53cafabaee53-instrument-30-audio.wav',0,0.594,121,1200,'2018-03-27 21:57:31','2018-03-27 21:57:31','Published');
/*!40000 ALTER TABLE `audio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_chord`
--

LOCK TABLES `audio_chord` WRITE;
/*!40000 ALTER TABLE `audio_chord` DISABLE KEYS */;
INSERT INTO `audio_chord` VALUES (2,191,'C',0,'2017-12-14 08:16:24','2017-12-14 08:16:24'),(3,190,'C',0,'2017-12-14 08:16:39','2017-12-14 08:16:39'),(4,193,'C',0,'2017-12-14 08:21:27','2017-12-14 08:21:27'),(5,195,'G',0,'2017-12-14 08:26:57','2017-12-14 08:26:57'),(6,196,'C',0,'2017-12-14 08:28:18','2017-12-14 08:28:18'),(7,197,'A',0,'2017-12-14 08:30:23','2017-12-14 08:30:23');
/*!40000 ALTER TABLE `audio_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_event`
--

LOCK TABLES `audio_event` WRITE;
/*!40000 ALTER TABLE `audio_event` DISABLE KEYS */;
INSERT INTO `audio_event` VALUES (1,2,1,1,'KICK',0.03,0.5,'X','2017-04-22 21:24:11','2017-04-22 21:28:14'),(2,3,1,1,'KICKLONG',0.025,0.5,'X','2017-04-22 21:24:54','2017-04-24 02:19:23'),(3,4,1,0.1,'HIHATCLOSED',0.025,0.1,'X','2017-04-22 21:26:58','2017-06-10 19:24:57'),(4,5,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-04-22 21:43:14','2017-04-22 21:43:14'),(5,6,1,0.4,'SNARE',0.002,0.091,'X','2017-04-22 21:45:06','2017-04-22 21:45:06'),(6,7,0.7,0.6,'TOM',0.002,0.35,'X','2017-04-22 21:46:12','2017-04-22 21:46:12'),(7,8,0.8,0.8,'CLAVES',0,0.05,'X','2017-04-24 00:03:50','2017-04-24 00:03:50'),(8,9,0.8,0.9,'CONGA',0.004,0.2,'X','2017-04-24 00:04:13','2017-04-24 00:04:13'),(9,11,1,1,'TOMHIGH',0.004,0.2,'X','2017-04-24 02:18:57','2017-04-24 02:18:57'),(10,10,1,1,'CONGAHIGH',0.005,0.2,'x','2017-04-24 02:20:10','2017-04-24 02:20:10'),(11,12,0.8,0.3,'CLAP',0.004,0.3,'x','2017-04-24 02:21:39','2017-06-04 04:30:00'),(12,13,1,0.5,'COWBELL',0.004,0.3,'x','2017-04-24 02:23:14','2017-04-24 02:23:14'),(13,14,1,0,'CYMBALCRASH',0,4,'x','2017-04-24 02:24:36','2017-06-16 03:26:40'),(14,15,0.5,0.1,'HIHATOPEN',0.002,0.59,'x','2017-04-24 02:25:56','2017-06-10 19:25:57'),(15,16,0.6,0.2,'SNARERIM',0.001,0.014,'x','2017-04-24 02:27:24','2017-06-10 19:27:09'),(16,22,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:35','2017-06-15 23:27:35'),(17,23,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:55','2017-06-15 23:27:55'),(18,24,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:28:31','2017-06-15 23:28:31'),(20,26,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:29:00','2017-06-15 23:29:00'),(21,27,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:14','2017-06-15 23:29:14'),(22,28,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:28','2017-06-15 23:29:28'),(23,29,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:40','2017-06-15 23:29:40'),(24,30,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:02','2017-06-15 23:30:02'),(25,31,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:22','2017-06-15 23:30:22'),(26,32,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:40','2017-06-15 23:30:40'),(27,33,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:30:54','2017-06-15 23:30:54'),(28,34,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:06','2017-06-15 23:31:06'),(29,35,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:17','2017-06-15 23:31:17'),(30,36,1,0.6,'TOMHIGH',0,1,'x','2017-06-15 23:31:59','2017-06-16 01:07:01'),(31,37,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:17','2017-06-15 23:32:17'),(32,38,1,0.6,'TOM',0,1,'x','2017-06-15 23:32:30','2017-06-16 01:07:14'),(33,39,1,0.6,'CONGAHIGH',0,1,'x','2017-06-15 23:32:39','2017-06-16 01:07:57'),(34,40,1,0.6,'CONGA',0,1,'x','2017-06-15 23:32:48','2017-06-16 01:08:10'),(35,41,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:59','2017-06-15 23:32:59'),(36,42,1,0.1,'SNARE',0,1,'x','2017-06-15 23:33:08','2017-06-15 23:33:08'),(37,44,1,1,'KICK',0,1,'x','2017-06-16 00:25:00','2017-06-16 00:25:00'),(38,43,1,1,'KICK',0,1,'x','2017-06-16 00:25:19','2017-06-16 00:25:19'),(39,45,1,1,'KICK',0,1,'x','2017-06-16 00:27:24','2017-06-16 00:27:24'),(42,48,1,0.6,'TOMLOW',0,1,'x','2017-06-16 00:34:38','2017-06-16 01:07:44'),(43,49,1,0.6,'TOM',0,1,'x','2017-06-16 00:36:12','2017-06-16 01:08:36'),(44,50,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 00:38:24','2017-06-16 01:08:52'),(45,51,1,1,'KICKLONG',0,1,'x','2017-06-16 01:10:01','2017-06-16 01:10:01'),(48,54,1,0.1,'CLAP',0,1,'x','2017-06-16 02:16:01','2017-06-16 02:16:01'),(49,56,1,1,'KICK',0,1,'x','2017-06-16 03:01:30','2017-06-16 03:01:30'),(50,57,1,1,'KICKLONG',0,1,'x','2017-06-16 03:04:19','2017-06-16 03:04:19'),(51,58,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 03:06:43','2017-06-16 03:06:43'),(52,59,1,0.6,'TOMLOW',0,1,'x','2017-06-16 03:07:39','2017-06-16 03:07:39'),(53,60,1,0.6,'TOM',0,1,'x','2017-06-16 03:10:02','2017-06-16 03:10:02'),(54,61,1,0.1,'CLAP',0,1,'x','2017-06-16 03:13:38','2017-06-16 03:13:38'),(55,62,1,0.1,'CLAP',0,1,'x','2017-06-16 03:14:51','2017-06-16 03:14:51'),(56,63,1,0.1,'MARACAS',0,1,'x','2017-06-16 03:17:20','2017-06-16 03:17:20'),(57,64,1,0.2,'COWBELL',0,1,'x','2017-06-16 03:20:15','2017-06-16 03:20:15'),(58,65,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:22:02','2017-06-16 03:24:34'),(59,66,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:24:20','2017-06-16 03:24:20'),(60,67,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:25:47','2017-06-16 03:25:47'),(61,68,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:29:05','2017-06-16 03:29:05'),(62,69,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:12:32','2017-06-20 23:16:38'),(63,70,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:17:52','2017-06-20 23:17:52'),(64,71,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:33:17','2017-06-20 23:33:17'),(65,72,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:35:34','2017-06-20 23:35:34'),(66,73,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:36:46','2017-06-20 23:36:46'),(67,74,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:37:56','2017-06-20 23:37:56'),(68,75,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:39:16','2017-06-20 23:39:16'),(69,76,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:40:49','2017-06-20 23:40:49'),(70,77,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:45:46','2017-06-20 23:45:46'),(71,78,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:46:41','2017-06-20 23:46:41'),(72,79,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:47:35','2017-06-20 23:47:35'),(73,80,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:48:55','2017-06-20 23:48:55'),(74,81,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:49:58','2017-06-20 23:49:58'),(75,82,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:50:42','2017-06-20 23:50:42'),(76,83,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:51:32','2017-06-20 23:51:32'),(77,84,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:52:08','2017-06-20 23:52:08'),(78,85,1,0.6,'TOM',0,1,'X','2017-06-20 23:54:26','2017-07-29 21:16:34'),(79,86,1,0.6,'KICK',0,1,'X','2017-06-20 23:56:22','2017-07-29 21:16:26'),(80,87,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:00','2017-07-29 21:15:54'),(81,88,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:46','2017-07-29 21:15:47'),(82,89,1,0.6,'TOM',0,1,'X','2017-06-21 00:00:29','2017-07-29 21:15:41'),(83,90,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:16','2017-07-29 21:15:34'),(84,91,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:59','2017-07-29 21:15:27'),(85,92,1,0.6,'TOM',0,1,'X','2017-06-21 00:02:39','2017-07-29 21:16:17'),(86,93,1,0.1,'SNARE',0,1,'X','2017-06-21 00:14:01','2017-06-21 00:14:01'),(87,94,1,0.15,'SNARE',0,1,'X','2017-06-21 00:14:49','2017-06-21 00:14:49'),(88,95,1,0.1,'SNARE',0,1,'X','2017-06-21 00:15:35','2017-06-21 00:15:35'),(89,96,1,0.15,'SNARE',0,1,'X','2017-06-21 00:49:39','2017-06-21 00:50:12'),(90,97,1,0.15,'SNARE',0,1,'X','2017-06-21 00:51:14','2017-06-21 00:51:14'),(91,98,1,0.15,'SNARE',0,1,'X','2017-06-21 00:53:11','2017-06-21 00:53:11'),(92,99,1,0.15,'SNARE',0,1,'X','2017-06-21 00:54:35','2017-06-21 00:54:35'),(93,100,1,0.15,'SNARE',0,1,'X','2017-06-21 00:55:37','2017-06-21 00:55:37'),(94,101,1,0.15,'SNARE',0,1,'X','2017-06-21 00:57:01','2017-06-21 00:57:01'),(95,102,1,0.15,'SNARE',0,1,'X','2017-06-21 00:58:31','2017-06-21 00:58:31'),(96,103,1,0.15,'SNARE',0,1,'X','2017-06-21 01:00:02','2017-06-21 01:00:02'),(97,104,1,0.6,'CONGA',0,1,'X','2017-06-21 01:04:26','2017-06-21 01:04:26'),(98,105,1,0.6,'TOM',0,1,'X','2017-06-21 01:05:37','2017-06-21 01:05:37'),(99,106,1,0.6,'CONGA',0,1,'X','2017-06-21 01:06:29','2017-06-21 01:06:29'),(100,107,1,0.6,'CONGA',0,1,'X','2017-06-21 01:07:21','2017-06-21 01:07:21'),(101,108,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:06','2017-06-21 01:08:13'),(102,109,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:59','2017-06-21 01:08:59'),(103,110,1,0.6,'CONGA',0,1,'X','2017-06-21 01:09:39','2017-06-21 01:09:39'),(104,111,1,0.6,'CONGA',0,1,'X','2017-06-21 01:10:23','2017-06-21 01:10:23'),(105,112,1,0.6,'CONGA',0,1,'X','2017-06-21 01:11:09','2017-06-21 01:11:09'),(106,113,1,0.6,'CONGA',0,1,'X','2017-06-21 01:12:01','2017-06-21 01:12:01'),(107,114,1,0.6,'CONGA',0,1,'X','2017-06-21 01:13:01','2017-06-21 01:13:01'),(108,115,1,0.6,'TOM',0,1,'X','2017-06-21 01:13:42','2017-06-21 01:13:42'),(109,116,1,0.6,'TOM',0,1,'X','2017-06-21 01:14:28','2017-06-21 01:14:28'),(110,117,1,0.6,'TOM',0,1,'X','2017-06-21 01:15:12','2017-06-21 01:15:12'),(111,119,1,0.6,'TOM',0,1,'X','2017-06-21 01:19:59','2017-06-21 01:19:59'),(112,120,1,0.6,'TOM',0,1,'X','2017-06-21 01:21:05','2017-06-21 01:21:05'),(113,121,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:02','2017-06-21 01:22:02'),(114,122,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:46','2017-06-21 01:22:46'),(115,123,1,0.6,'TOM',0,1,'X','2017-06-21 01:23:31','2017-06-21 01:23:31'),(116,124,1,0.6,'TOM',0,1,'X','2017-06-21 01:24:10','2017-06-21 01:24:10'),(118,126,1,0.3,'X',0,1,'0','2017-06-23 23:54:24','2017-06-23 23:54:24'),(119,127,1,0.3,'HEY',0,1,'X','2017-06-23 23:56:04','2017-06-23 23:56:04'),(120,128,1,0.3,'HEY',0,1,'X','2017-06-23 23:57:11','2017-06-23 23:57:11'),(121,129,1,0.3,'HEY',0,1,'X','2017-06-23 23:58:09','2017-06-23 23:58:09'),(124,132,1,0.3,'HEY',0,1,'X','2017-06-24 00:00:37','2017-06-24 00:00:37'),(125,133,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:08','2017-06-24 00:11:08'),(126,134,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:47','2017-06-24 00:11:47'),(127,135,1,0.3,'HEY',0,1,'X','2017-06-24 00:13:39','2017-06-24 00:13:39'),(128,136,1,0.3,'HEY',0,1,'X','2017-06-24 00:15:04','2017-06-24 00:15:04'),(129,137,1,0.3,'HEY',0,1,'X','2017-06-24 00:16:27','2017-06-24 00:16:27'),(130,138,1,0.3,'HEY',0,1,'X','2017-06-24 00:17:46','2017-06-24 00:17:46'),(131,139,1,0.3,'HEY',0,1,'X','2017-06-24 00:20:45','2017-06-24 00:20:45'),(132,140,1,0.3,'HEY',0,1,'X','2017-06-24 00:22:47','2017-06-24 00:22:47'),(133,141,1,0.3,'HEY',0,1,'X','2017-06-24 00:24:25','2017-06-24 00:24:25'),(134,143,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:08','2017-06-24 00:26:08'),(135,144,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:59','2017-06-24 00:26:59'),(136,145,1,0.3,'HEY',0,1,'X','2017-06-24 00:27:47','2017-06-24 00:27:47'),(137,147,1,0.3,'HEY',0,1,'X','2017-06-24 00:30:09','2017-06-24 00:30:09'),(138,148,1,0.3,'HEY',0,1,'X','2017-06-24 00:31:42','2017-06-24 00:31:42'),(139,149,1,0.3,'HEY',0,1,'X','2017-06-24 00:32:45','2017-06-24 00:32:45'),(140,150,1,0.3,'HEY',0,1,'X','2017-06-24 00:33:52','2017-06-24 00:33:52'),(141,151,1,0.3,'HEY',0,1,'X','2017-06-24 00:34:48','2017-06-24 00:34:48'),(142,152,1,0.3,'HEY',0,1,'X','2017-06-24 00:35:36','2017-06-24 00:35:36'),(143,153,1,0.3,'HEY',0,1,'X','2017-06-24 00:36:22','2017-06-24 00:36:22'),(144,154,1,0.3,'HEY',0.025,1,'X','2017-06-24 00:37:19','2017-07-24 20:21:27'),(150,166,2,1,'KICK',0,1,'x','2017-07-27 22:36:01','2017-12-03 03:28:37'),(151,167,2,1,'KICK',0,1,'x','2017-07-27 22:37:04','2017-12-03 03:28:45'),(158,174,2,1,'KICK',0,1,'x','2017-07-27 23:07:54','2017-12-03 03:28:15'),(160,176,2,1,'KICK',0,1,'x','2017-07-27 23:13:22','2017-12-03 03:27:51'),(165,184,1,1,'TOM',0,1,'Gb5','2017-12-14 07:15:30','2018-01-07 00:50:45'),(166,185,1,1,'TOM',0,2,'B3','2017-12-14 07:17:19','2018-01-07 00:50:55'),(167,186,1,1,'TOM',0,1,'D5','2017-12-14 07:18:59','2018-01-07 00:51:05'),(168,187,1,1,'TOM',0,1,'G5','2017-12-14 07:20:01','2018-01-07 00:51:15'),(171,190,1,1,'OOH',0,4,'C5','2017-12-14 08:12:26','2017-12-14 08:12:26'),(172,191,1,1,'OOH',0,2,'C5','2017-12-14 08:16:11','2017-12-14 08:16:11'),(173,192,1,0.3,'KICK',0,2,'Db3','2017-12-14 08:19:08','2017-12-23 05:55:18'),(174,193,1,1,'POING',0,3,'C4','2017-12-14 08:21:17','2017-12-14 08:21:17'),(175,194,1,0.8,'POOM',0,2,'C4','2017-12-14 08:23:33','2017-12-14 08:23:33'),(176,195,1,1,'BOOM',0,4,'G1','2017-12-14 08:27:14','2017-12-14 08:27:14'),(177,196,1,1,'WAOW',0,4,'C2','2017-12-14 08:28:38','2017-12-14 08:28:38'),(178,197,1,0.8,'OOH',0,4,'A3','2017-12-14 08:30:46','2017-12-14 08:30:46'),(180,199,1,0.6,'DOING',0,1,'c4','2017-12-14 08:37:55','2017-12-14 08:37:55'),(181,200,1,0.6,'DONG',0,1,'C3','2017-12-14 08:39:20','2017-12-14 08:39:20'),(182,201,1,0.5,'TUNG',0,1,'C4','2017-12-14 08:45:28','2017-12-14 08:45:28'),(187,206,1,0.6,'PLONG',0,1,'c4','2017-12-14 09:27:31','2017-12-14 09:27:31'),(244,267,1,0.6,'TOMLOW',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(245,268,1,0.6,'TOMHIGH',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(246,269,1,0.6,'TOMHIGH',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(247,270,1,0.6,'TOM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(248,274,1,0.3,'STICKSIDE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(249,272,1,0.3,'STICKSIDE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(250,271,1,0.6,'TOM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(251,273,1,0.3,'STICKSIDE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(252,275,1,0.1,'SNARERIM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(253,276,1,0.1,'SNARERIM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(254,278,1,0.1,'SNARERIM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(255,277,1,0.1,'SNARERIM',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(256,279,1,0.1,'SNARE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(257,281,1,0.1,'SNARE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(258,280,1,0.1,'SNARE',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(259,282,1,1,'KICKLONG',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(260,285,1,1,'KICK',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(261,283,1,1,'KICK',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(262,284,1,1,'KICK',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(263,287,1,0.1,'HIHATOPEN',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(264,286,1,0.1,'HIHATOPEN',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(265,288,1,0.1,'HIHATOPEN',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(266,289,1,0.1,'HIHATCLOSED',0,1,'X','2017-12-22 19:39:27','2017-12-22 19:39:27'),(267,291,1,0.1,'HIHATCLOSED',0,1,'X','2017-12-22 19:39:27','2017-12-22 19:39:27'),(268,292,1,0,'CYMBALCRASH',0,4,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(269,290,1,0.1,'HIHATCLOSED',0,1,'X','2017-12-22 19:39:27','2017-12-22 19:39:27'),(270,293,1,0,'CYMBALCRASH',0,4,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(271,294,1,0,'CYMBALCRASH',0,4,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(272,296,1,0.6,'CONGAHIGH',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(273,295,1,0.2,'COWBELL',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(274,297,1,0.6,'CONGA',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(275,299,1,0.1,'CLAP',0,1,'x','2017-12-22 19:39:27','2017-12-22 19:39:27'),(284,309,1,0.6,'TOMHIGH',0,1,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(285,310,1,1,'TOMHIGH',0.004,0.2,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(286,312,0.7,0.6,'TOM',0.002,0.35,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(287,311,1,0.6,'TOM',0,1,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(288,313,0.6,0.2,'SNARERIM',0.001,0.014,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(289,315,1,0.1,'MARACAS',0,1,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(290,316,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(291,314,1,0.4,'SNARE',0.002,0.091,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(292,317,1,1,'KICKLONG',0,1,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(293,318,1,1,'KICKLONG',0.025,0.5,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(294,319,1,1,'KICK',0,1,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(295,320,1,1,'KICK',0.03,0.5,'X','2017-12-22 19:39:49','2017-12-22 19:39:49'),(296,321,0.5,0.1,'HIHATOPEN',0.002,0.59,'x','2017-12-22 19:39:49','2017-12-22 19:39:49'),(297,322,1,0.1,'HIHATCLOSED',0.025,0.1,'X','2017-12-22 19:39:50','2017-12-22 19:39:50'),(298,323,1,0,'CYMBALCRASH',0,4,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(299,325,1,0.5,'COWBELL',0.004,0.3,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(300,327,0.8,0.9,'CONGA',0.004,0.2,'X','2017-12-22 19:39:50','2017-12-22 19:39:50'),(301,326,1,1,'CONGAHIGH',0.005,0.2,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(302,328,0.8,0.8,'CLAVES',0,0.05,'X','2017-12-22 19:39:50','2017-12-22 19:39:50'),(303,324,1,0,'CYMBALCRASH',0,4,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(304,331,0.8,0.3,'CLAP',0.004,0.3,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(305,329,1,0.1,'CLAP',0,1,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(306,330,1,0.1,'CLAP',0,1,'x','2017-12-22 19:39:50','2017-12-22 19:39:50'),(379,404,1,1,'KICK',0,1,'Ab2','2017-12-23 22:56:30','2017-12-23 22:56:30'),(380,405,1,1,'KICK',0,1,'C2','2017-12-23 22:57:41','2017-12-23 22:57:41'),(381,406,1,1,'KICK',0,1,'C2','2017-12-23 22:59:08','2017-12-23 22:59:08'),(382,407,1,1,'KICK',0,1,'Db2','2017-12-23 23:00:12','2017-12-23 23:00:12'),(383,408,1,0,'SNARE',0,1,'Db4','2017-12-23 23:01:28','2017-12-23 23:01:28'),(384,409,1,0,'SNARE',0,1,'C6','2017-12-23 23:02:27','2017-12-23 23:02:27'),(385,410,1,0,'SNARE',0,1,'B5','2017-12-23 23:03:21','2017-12-23 23:03:21'),(386,411,1,0,'SNARE',0,1,'Eb4','2017-12-23 23:04:23','2017-12-23 23:04:23'),(387,412,1,0,'SNARE',0,1,'F8','2017-12-23 23:05:30','2017-12-23 23:05:30'),(388,413,1,0.5,'TUNG',0,1,'C4','2017-12-23 23:39:06','2017-12-23 23:39:06'),(389,414,1,0.6,'PLONG',0,1,'c4','2017-12-23 23:39:41','2017-12-23 23:39:41'),(390,415,1,0.6,'DOING',0,1,'c4','2017-12-23 23:40:00','2017-12-23 23:40:00'),(391,416,1,0.6,'DONG',0,1,'C3','2017-12-23 23:40:17','2017-12-23 23:40:17'),(394,419,1,0,'SNARE',0,1,'E4','2017-12-24 00:03:27','2017-12-24 00:03:27'),(395,420,1,0,'SNARE',0,1,'Bb5','2017-12-24 00:04:59','2017-12-24 00:04:59'),(396,421,1,0,'SNARE',0,1,'E3','2017-12-24 00:07:00','2017-12-24 00:07:00'),(397,422,1,0,'SNARE',0,1,'C5','2017-12-24 00:08:27','2017-12-24 00:08:27'),(398,423,1,0,'SNARE',0,1,'Db4','2017-12-24 00:13:26','2017-12-24 00:13:26'),(399,424,1,1,'KICK',0,1,'Eb2','2017-12-24 00:16:04','2017-12-24 00:16:04'),(400,425,1,1,'KICK',0,1,'Bb4','2017-12-24 00:17:32','2017-12-24 00:17:32'),(401,426,1,1,'TOM',0,1,'Eb3','2017-12-24 00:19:03','2017-12-24 00:19:03'),(402,427,1,1,'TOM',0,1,'Eb3','2017-12-24 00:20:32','2017-12-24 00:20:32'),(403,428,1,1,'TOM',0,1,'Eb3','2017-12-24 00:22:23','2017-12-24 00:22:23'),(404,429,1,1,'TOM',0,1,'D3','2017-12-24 00:24:24','2017-12-24 00:24:24'),(405,430,1,1,'TOM',0,1,'D3','2017-12-24 00:26:21','2017-12-24 00:26:21'),(406,431,1,0,'HIHATCLOSED',0,0.5,'Ab6','2017-12-24 00:28:43','2017-12-24 00:28:43'),(407,432,1,0,'HIHATCLOSED',0,0.5,'D4','2017-12-24 00:29:55','2017-12-24 00:29:55'),(408,433,1,0,'HIHATCLOSED',0,0.5,'Ab7','2017-12-24 00:30:57','2017-12-24 00:30:57'),(409,434,1,0,'HIHATCLOSED',0,0.5,'B7','2017-12-24 00:33:11','2017-12-24 00:33:11'),(410,435,1,0,'HIHATCLOSED',0,0.5,'Bb7','2017-12-24 00:34:25','2017-12-24 00:34:25'),(411,436,1,0,'HIHATOPEN',0,0.5,'Eb8','2017-12-24 00:36:10','2017-12-24 00:36:10'),(412,437,1,0,'HIHATOPEN',0,0.5,'Eb8','2017-12-24 00:37:31','2017-12-24 00:37:31'),(413,438,1,0,'HIHATOPEN',0,0.5,'Eb8','2017-12-24 00:38:40','2017-12-24 00:38:40'),(414,439,1,0,'HIHATOPEN',0,0.5,'Eb8','2017-12-24 00:39:50','2017-12-24 00:39:50'),(415,440,1,1,'KICK',0,1,'Bb1','2017-12-24 01:17:53','2017-12-24 01:17:53'),(416,441,1,1,'KICK',0,1,'Ab1','2017-12-24 01:18:37','2017-12-24 01:18:37'),(417,442,1,1,'KICK',0,1,'A1','2017-12-24 01:19:22','2017-12-24 01:19:22'),(418,443,1,1,'KICK',0,1,'Bb1','2017-12-24 01:21:04','2017-12-24 01:21:04'),(419,444,1,0,'SNARE',0,1,'Db44','2017-12-24 01:24:23','2017-12-24 01:24:23'),(422,447,1,0,'SNARE',0,1,'Db4','2017-12-24 01:26:59','2017-12-24 01:26:59'),(423,449,1,0,'HIHATCLOSED',0,0.5,'G8','2017-12-24 01:30:40','2017-12-24 01:30:40'),(424,448,1,0,'HIHATCLOSED',0,0.5,'G8','2017-12-24 01:31:06','2017-12-24 01:31:06'),(425,450,1,0,'HIHATCLOSED',0,0.5,'G8','2017-12-24 01:31:56','2017-12-24 01:31:56'),(426,451,1,0,'HIHATCLOSED',0,0.5,'Bb8','2017-12-24 01:32:59','2017-12-24 01:32:59'),(427,452,1,0,'HIHATOPEN',0,0.5,'g8','2017-12-24 01:34:05','2017-12-24 01:34:05'),(428,453,1,0,'HIHATOPEN',0,0.5,'E7','2017-12-24 01:35:04','2017-12-24 01:35:04'),(429,454,1,0,'HIHATOPEN',0,0.5,'e7','2017-12-24 01:36:02','2017-12-24 01:36:02'),(430,455,1,0,'HIHATOPEN',0,0.5,'G8','2017-12-24 01:38:04','2017-12-24 01:38:04'),(431,456,1,1,'TOMHIGH',0,1,'E3','2017-12-24 01:42:58','2017-12-24 01:42:58'),(432,457,1,1,'TOMLOW',0,1,'A2','2017-12-24 01:44:13','2017-12-24 01:44:13'),(433,458,1,1,'TOMLOW',0,1,'G2','2017-12-24 01:45:28','2017-12-24 01:45:28'),(434,459,1,1,'TOMHIGH',0,1,'F3','2017-12-24 01:47:58','2017-12-24 01:47:58'),(435,460,1,1,'TOMHIGH',0,1,'E3','2017-12-24 01:49:18','2017-12-24 01:49:18'),(436,461,1,1,'TOMLOW',0,1,'e2','2017-12-24 01:49:56','2017-12-24 01:49:56'),(437,462,1,1,'KICK',0,1,'Eb2','2017-12-24 02:47:47','2017-12-24 02:47:47'),(438,463,1,1,'KICK',0,1,'Eb3','2017-12-24 02:48:35','2017-12-24 02:48:35'),(441,466,1,1,'KICK',0,1,'f8','2017-12-24 02:52:02','2017-12-24 02:52:02'),(442,467,1,0,'SNARE',0,1,'Bb3','2017-12-24 02:53:25','2017-12-24 02:53:25'),(443,468,1,0,'SNARE',0,1,'g8','2017-12-24 02:54:03','2017-12-24 02:54:03'),(444,469,1,0,'SNARE',0,1,'B','2017-12-24 02:54:40','2017-12-24 02:54:40'),(445,470,1,0,'SNARE',0,1,'f3','2017-12-24 02:55:45','2017-12-24 02:55:45'),(446,472,1,0,'SNARE',0,1,'f9','2017-12-24 02:57:34','2017-12-24 02:57:34'),(447,473,1,0,'HIHATCLOSED',0,0.5,'g8','2017-12-24 03:00:14','2017-12-24 03:00:14'),(448,474,1,0,'HIHATCLOSED',0,0.5,'Gb4','2017-12-24 03:01:18','2017-12-24 03:01:18'),(449,475,1,0,'HIHATCLOSED',0,0.5,'C5','2017-12-24 03:02:17','2017-12-24 03:02:17'),(450,476,1,0,'HIHATCLOSED',0,0.5,'Bb8','2017-12-24 03:03:28','2017-12-24 03:03:28'),(451,477,1,0,'HIHATOPEN',0,0.5,'e4','2017-12-24 03:04:29','2017-12-24 03:04:29'),(452,478,1,0,'HIHATOPEN',0,0.5,'c5','2017-12-24 03:05:18','2017-12-24 03:05:18'),(453,479,1,0,'HIHATOPEN',0,1,'e4','2017-12-24 03:06:15','2017-12-24 03:06:15'),(454,480,1,0,'HIHATOPEN',0,0.5,'Bb8','2017-12-24 03:07:23','2017-12-24 03:07:23'),(455,481,1,1,'TOM',0,1,'D3','2017-12-24 03:08:50','2017-12-24 03:08:50'),(456,482,1,1,'TOM',0,1,'Db3','2017-12-24 03:09:41','2017-12-24 03:09:41'),(457,483,1,1,'TOM',0,1,'Ab2','2017-12-24 03:10:22','2017-12-24 03:10:22'),(458,484,1,0,'CYMBALCRASH',0,4,'B7','2018-01-05 09:52:10','2018-01-05 09:52:10'),(459,485,1,0,'CYMBALCRASH',0,4,'Eb8','2018-01-05 09:53:45','2018-01-05 09:53:45'),(460,486,1,0,'CYMBALCRASH',0,4,'E5','2018-01-05 09:56:26','2018-01-05 09:56:26'),(461,487,1,0,'CYMBALCRASH',0,4,'C5','2018-01-05 09:59:48','2018-01-05 09:59:48'),(462,488,1,0,'CYMBALCRASH',0,1,'Eb8','2018-01-05 10:01:02','2018-01-05 10:01:02'),(463,489,1,0,'HIHATCLOSED',0,0.5,'Ab3','2018-01-05 10:18:38','2018-01-05 10:18:38'),(464,490,1,0,'HIHATCLOSED',0,0.5,'Eb5','2018-01-05 10:20:50','2018-01-05 10:20:50'),(465,491,1,0,'HIHATOPEN',0,0.5,'D3','2018-01-05 10:22:51','2018-01-05 10:22:51'),(466,492,1,0,'HIHATOPEN',0,0.5,'Eb3','2018-01-05 10:30:56','2018-01-05 10:30:56'),(467,493,1,0.1,'CLAP',0,1,'D5','2018-02-09 21:42:37','2018-02-09 21:42:37'),(469,495,1,0,'HIHATOPEN',0,1,'Bb7','2018-03-06 06:12:09','2018-03-06 06:12:09'),(470,497,1,0,'HIHATCLOSED',0,1,'Bb7','2018-03-06 06:17:57','2018-03-06 06:17:57'),(471,497,1,0,'HIHATCLOSED',0,1,'Bb7','2018-03-06 06:17:58','2018-03-06 06:17:58'),(473,500,1,0,'HIHATOPEN',0,1,'D8','2018-03-06 06:24:15','2018-03-06 06:24:15'),(474,501,1,0,'TAMBOURINE',0,1,'F7','2018-03-06 06:27:34','2018-03-06 06:27:34'),(475,502,1,0,'SHAKER',0,1,'D8','2018-03-06 06:44:00','2018-03-06 06:44:00'),(476,503,1,0,'CLAVES',0,1,'F7','2018-03-06 06:46:48','2018-03-06 06:46:48'),(477,504,1,0,'CLAVES',0,1,'F7','2018-03-06 06:49:18','2018-03-06 06:49:18'),(478,505,1,0,'COWBELL',0,1,'F5','2018-03-06 06:52:23','2018-03-06 06:52:23'),(479,506,1,0,'COWBELL',0,1,'A4','2018-03-06 06:54:44','2018-03-06 06:54:44'),(480,507,1,0,'SNARERIM',0,1,'G4','2018-03-06 06:58:15','2018-03-06 06:58:15'),(481,508,1,0,'SNARERIM',0,1,'B3','2018-03-06 07:03:07','2018-03-06 07:03:07'),(482,509,1,0,'CLAP',0,1,'Eb5','2018-03-06 07:06:12','2018-03-06 07:06:12'),(483,510,1,0,'CLAP',0,1,'B8','2018-03-06 07:10:07','2018-03-06 07:10:07'),(484,511,1,0,'SNARE',0,1,'G3','2018-03-06 07:12:44','2018-03-06 07:12:44'),(485,512,1,0,'SNARE',0,1,'D7','2018-03-06 07:15:33','2018-03-06 07:15:33'),(486,513,1,0,'KICK',0,1,'C3','2018-03-06 07:18:57','2018-03-06 07:18:57'),(487,514,1,0,'SNARE',0,1,'F#3','2018-03-20 00:45:38','2018-03-20 00:45:38'),(488,515,1,0,'SNARE',0,1,'Bb7','2018-03-20 00:49:19','2018-03-20 00:49:19'),(489,516,1,0,'SNARE',0,1,'C#3','2018-03-20 00:52:46','2018-03-20 00:52:46'),(490,517,1,0,'SNARERIM',0,1,'D6','2018-03-20 00:55:23','2018-03-20 00:55:23'),(491,518,1,0,'KICK',0,1,'C2','2018-03-20 01:00:07','2018-03-20 01:00:07'),(492,519,1,0,'KICK',0,1,'F2','2018-03-20 01:02:29','2018-03-20 01:02:29'),(493,520,1,0,'KICK',0,1,'C3','2018-03-20 01:06:18','2018-03-20 01:06:18'),(494,521,1,0,'KICK',0,1,'B2','2018-03-20 01:14:40','2018-03-20 01:14:40'),(495,522,1,0,'KICK',0,1,'B','2018-03-20 01:18:24','2018-03-20 01:18:24'),(497,524,1,0,'KICK',0,1,'B8','2018-03-20 01:23:33','2018-03-20 01:23:33'),(498,525,1,0,'KICK',0,1,'G2','2018-03-20 01:25:35','2018-03-20 01:25:35'),(499,526,1,0,'TOMLOWMID',0,1,'Bb3','2018-03-20 01:29:22','2018-03-20 01:29:22'),(500,527,1,0,'TOMLOW',0,1,'E2','2018-03-20 01:32:27','2018-03-20 01:32:27'),(501,528,1,0,'TOMLOW',0,1,'D2','2018-03-20 01:38:54','2018-03-20 01:38:54'),(503,531,1,0,'TOMLOW',0,1,'Bb3','2018-03-20 01:47:17','2018-03-20 01:47:17'),(504,532,1,0,'TOMLOW',0,1,'F#4','2018-03-20 01:49:22','2018-03-20 01:49:22'),(505,533,1,0,'TOMLOW',0,1,'Eb5','2018-03-20 01:51:17','2018-03-20 01:51:17'),(506,534,1,0,'TOMLOWMID',0,1,'E2','2018-03-20 01:53:37','2018-03-20 01:53:37'),(507,536,1,0,'TOMLOW',0,1,'F#1','2018-03-20 01:57:55','2018-03-20 01:57:55'),(508,537,1,0,'TOMLOWMID',0,1,'B2','2018-03-20 01:59:57','2018-03-20 01:59:57'),(509,538,1,0,'TOMLOW',0,1,'Eb2','2018-03-20 02:01:40','2018-03-20 02:01:40'),(510,539,1,0,'TOMLOW',0,1,'C#8','2018-03-20 02:03:27','2018-03-20 02:03:27'),(511,540,1,0,'TOMLOW',0,1,'A2','2018-03-20 02:06:00','2018-03-20 02:06:00'),(512,541,1,0,'AGOGOHIGH',0,1,'A7','2018-03-20 02:08:39','2018-03-20 02:08:39'),(513,542,1,0,'AGOGOLOW',0,1,'G6','2018-03-20 02:10:19','2018-03-20 02:10:19'),(514,543,1,0,'AGOGOHIGH',0,1,'Eb5','2018-03-20 02:12:50','2018-03-20 02:12:50'),(515,544,1,0,'SHAKER',0,1,'F#8','2018-03-20 02:14:59','2018-03-20 02:14:59'),(516,545,1,0,'SHAKER',0,1,'E8','2018-03-20 02:16:29','2018-03-20 02:16:29'),(517,546,1,0,'SHAKER',0,1,'F#9','2018-03-20 02:17:59','2018-03-20 02:17:59'),(518,547,1,0,'SNARERIM',0,1,'B8','2018-03-20 02:20:00','2018-03-20 02:20:00'),(519,548,1,0,'KICK',0,1,'G3','2018-03-26 16:18:06','2018-03-26 16:18:06'),(520,549,1,0,'KICK',0,1,'F#3','2018-03-26 16:20:43','2018-03-26 16:20:43'),(521,550,1,0,'KICK',0,1,'F#2','2018-03-26 16:23:52','2018-03-26 16:23:52'),(522,551,1,0,'KICK',0,1,'F2','2018-03-26 16:25:53','2018-03-26 16:25:53'),(523,552,1,0,'KICK',0,1,'F2','2018-03-26 16:28:01','2018-03-26 16:28:01'),(524,553,1,0,'KICK',0,1,'F2','2018-03-26 16:30:00','2018-03-26 16:30:00'),(525,554,1,0,'KICK',0,1,'E2','2018-03-26 16:32:19','2018-03-26 16:32:19'),(526,555,1,0,'KICK',0,1,'B6','2018-03-26 16:34:07','2018-03-26 16:34:07'),(527,556,1,0,'SNARE',0,1,'C#3','2018-03-26 16:36:31','2018-03-26 16:36:31'),(528,557,1,0,'SNARE',0,1,'B2','2018-03-26 16:37:40','2018-03-26 16:37:40'),(529,558,1,0,'SNARE',0,1,'Eb4','2018-03-26 16:39:27','2018-03-26 16:39:27'),(530,559,1,0,'SNARE',0,1,'C#3','2018-03-26 16:41:08','2018-03-26 16:41:08'),(531,560,1,0,'HIHATCLOSED',0,1,'G4','2018-03-26 16:47:04','2018-03-26 16:47:04'),(532,561,1,0,'HIHATCLOSED',0,1,'D8','2018-03-26 16:49:54','2018-03-26 16:49:54'),(533,562,1,0,'HIHATCLOSED',0,1,'E5','2018-03-26 16:51:23','2018-03-26 16:51:23'),(534,563,1,0,'HIHATCLOSED',0,1,'E8','2018-03-26 16:53:11','2018-03-26 16:53:11'),(535,564,1,0,'HIHATCLOSED',0,1,'E8','2018-03-26 16:54:41','2018-03-26 16:54:41'),(536,565,1,0,'HIHATOPEN',0,1,'D8','2018-03-26 16:57:01','2018-03-26 16:57:01'),(537,566,1,0,'SNARE',0,1,'G7','2018-03-26 16:58:44','2018-03-26 16:58:44'),(538,567,1,0,'SNARE',0,1,'B8','2018-03-26 17:00:08','2018-03-26 17:00:08'),(539,568,1,0,'TOMLOW',0,1,'B2','2018-03-26 17:03:10','2018-03-26 17:03:10'),(540,569,1,0,'SNARE',0,1,'F#4','2018-03-26 17:04:51','2018-03-26 17:04:51'),(541,570,1,0,'CYMBALCRASH',0,1,'B7','2018-03-26 17:06:26','2018-03-26 17:06:26'),(542,571,1,0,'SNARE',0,1,'Bb7','2018-03-26 17:08:20','2018-03-26 17:08:20'),(543,572,1,0,'SNARE',0,1,'E2','2018-03-26 17:10:00','2018-03-26 17:10:00'),(544,573,1,0,'CYMBALCRASH',0,1,'Eb6','2018-03-26 17:11:35','2018-03-26 17:11:35'),(545,574,1,0,'SNARE',0,1,'C#4','2018-03-26 17:12:59','2018-03-26 17:12:59'),(546,575,1,0,'SNARE',0,1,'A3','2018-03-26 17:14:14','2018-03-26 17:14:14'),(547,576,1,0,'SNARE',0,1,'D9','2018-03-26 17:15:43','2018-03-26 17:15:43'),(548,577,1,0,'SNARE',0,1,'A6','2018-03-26 17:17:08','2018-03-26 17:17:08'),(549,578,1,0,'TOMHIGH',0,1,'E3','2018-03-26 17:18:58','2018-03-26 17:18:58'),(550,579,1,0,'TOMHIGHMID',0,1,'C3','2018-03-26 17:20:23','2018-03-26 17:20:23'),(551,580,1,0,'TOMLOW',0,1,'A2','2018-03-26 17:21:39','2018-03-26 17:21:39'),(552,581,1,0,'CABASA',0,1,'B7','2018-03-27 21:18:48','2018-03-27 21:18:48'),(553,582,1,0,'CLAP',0,1,'B8','2018-03-27 21:20:32','2018-03-27 21:20:32'),(554,583,1,0,'CLAVE',0,1,'A6','2018-03-27 21:22:00','2018-03-27 21:22:00'),(555,584,1,0,'GONG',0,1,'B2','2018-03-27 21:23:57','2018-03-27 21:23:57'),(556,585,1,0,'KICK',0,1,'Ab2','2018-03-27 21:26:15','2018-03-27 21:26:15'),(557,586,1,0,'SNARE',0,1,'E4','2018-03-27 21:27:34','2018-03-27 21:27:34'),(558,588,1,0,'CLAVE',0,1,'A3','2018-03-27 21:31:56','2018-03-27 21:31:56'),(559,589,1,0,'CLAVE',0,1,'Eb6','2018-03-27 21:33:19','2018-03-27 21:33:19'),(560,590,1,0,'SHAKER',0,1,'F#8','2018-03-27 21:35:48','2018-03-27 21:35:48'),(561,591,1,0,'HIHATCLOSED',0,1,'E6','2018-03-27 21:37:17','2018-03-27 21:37:17'),(562,592,1,0,'HIHATCLOSED',0,1,'F#8','2018-03-27 21:38:41','2018-03-27 21:38:41'),(563,593,1,0,'SNARE',0,1,'D8','2018-03-27 21:40:07','2018-03-27 21:40:07'),(564,595,1,0,'SNARE',0,1,'F2','2018-03-27 21:44:40','2018-03-27 21:44:40'),(565,597,1,0,'SNARE',0,1,'D8','2018-03-27 21:48:19','2018-03-27 21:48:19'),(566,598,1,0,'TOMHIGH',0,1,'Eb2','2018-03-27 21:50:12','2018-03-27 21:50:12'),(567,599,1,0,'TOMHIGHMID',0,1,'A7','2018-03-27 21:52:19','2018-03-27 21:52:19'),(568,600,1,0,'TOMHIGH',0,1,'A7','2018-03-27 21:54:39','2018-03-27 21:54:39'),(569,601,1,0,'KICK',0,1,'Db4','2018-03-27 21:56:28','2018-03-27 21:56:28'),(570,602,1,0,'CYMBALCRASH',0,1,'D6','2018-03-27 21:58:00','2018-03-27 21:58:00');
/*!40000 ALTER TABLE `audio_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument`
--

LOCK TABLES `instrument` WRITE;
/*!40000 ALTER TABLE `instrument` DISABLE KEYS */;
INSERT INTO `instrument` VALUES (3,1,1,'percussive','Electronic',0.8,'2017-04-21 16:33:55','2017-06-16 02:19:40'),(4,1,1,'percussive','Acoustic',0.5,'2017-06-15 22:32:29','2017-06-15 22:32:29'),(5,1,1,'percussive','Pots & Pans',0.76,'2017-06-20 23:02:25','2017-07-27 17:12:15'),(6,1,3,'Percussive','Wind A (legacy)',0.618,'2017-12-13 06:54:32','2018-03-16 00:30:29'),(7,1,3,'Harmonic','Wind Flute Note',0.2,'2017-12-14 08:10:02','2017-12-14 08:10:02'),(8,1,3,'Harmonic','Water Sitar Harmony',0.35,'2017-12-14 08:19:43','2017-12-14 08:31:27'),(9,1,3,'Harmonic','Earth Bass Harmony',0.4,'2017-12-14 08:25:57','2017-12-14 08:31:37'),(10,1,3,'Harmonic','Water Whale Harmony',0.4,'2017-12-14 08:29:25','2017-12-14 08:29:25'),(12,1,3,'Harmonic','Fire String Hits',0.5,'2017-12-14 08:36:41','2017-12-23 23:30:20'),(19,1,3,'Percussive','Earth A (legacy)',0.618,'2017-12-22 19:39:26','2018-03-16 00:29:54'),(20,1,3,'Percussive','Fire A  (legacy)',0.618,'2017-12-22 19:39:49','2018-03-16 00:30:23'),(22,1,3,'Percussive','Earth B (legacy)',0.618,'2017-12-23 23:56:26','2018-03-16 00:30:06'),(23,1,3,'Percussive','Wind B (legacy)',0.618,'2017-12-24 01:14:13','2018-03-16 00:30:34'),(24,1,3,'Percussive','Water B (legacy)',0.618,'2017-12-24 02:43:48','2018-03-16 00:29:48'),(25,3,4,'Percussive','Flammy Clap',0.5,'2018-02-09 21:04:25','2018-02-09 21:04:25'),(26,1,3,'Percussive','Water Basic X',0.6,'2018-03-06 04:19:57','2018-03-16 00:28:38'),(27,3,3,'Percussive','Water Basic',0.6,'2018-03-06 06:04:31','2018-03-06 06:04:31'),(28,3,3,'Percussive','Earth Basic',0.6,'2018-03-20 00:34:47','2018-03-20 00:34:47'),(29,3,3,'Percussive','Fire Basic',0.6,'2018-03-26 16:13:37','2018-03-26 16:13:37'),(30,3,3,'Percussive','Wind Basic',0.6,'2018-03-27 21:16:43','2018-03-27 21:16:43');
/*!40000 ALTER TABLE `instrument` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument_meme`
--

LOCK TABLES `instrument_meme` WRITE;
/*!40000 ALTER TABLE `instrument_meme` DISABLE KEYS */;
INSERT INTO `instrument_meme` VALUES (2,3,'Classic','2017-04-23 23:13:29','2017-04-23 23:13:29'),(3,3,'Deep','2017-04-23 23:13:33','2017-04-23 23:13:33'),(4,3,'Acid','2017-04-23 23:13:36','2017-04-23 23:13:36'),(6,3,'Tech','2017-04-23 23:13:41','2017-04-23 23:13:41'),(7,3,'Electro','2017-04-23 23:13:43','2017-04-23 23:13:43'),(10,3,'Cool','2017-04-23 23:20:57','2017-04-23 23:20:57'),(11,3,'Hard','2017-04-23 23:20:59','2017-04-23 23:20:59'),(13,3,'Progressive','2017-04-23 23:23:43','2017-04-23 23:23:43'),(14,4,'Classic','2017-06-15 22:59:20','2017-06-15 22:59:20'),(16,4,'Tropical','2017-06-15 22:59:32','2017-06-15 22:59:32'),(17,4,'Hot','2017-06-15 22:59:35','2017-06-15 22:59:35'),(19,4,'Easy','2017-06-15 22:59:43','2017-06-15 22:59:43'),(20,4,'Progressive','2017-06-15 22:59:46','2017-06-15 22:59:46'),(21,5,'Classic','2017-06-21 01:25:37','2017-06-21 01:25:37'),(22,5,'Deep','2017-06-21 01:25:41','2017-06-21 01:25:41'),(23,5,'Hard','2017-06-21 01:25:58','2017-06-21 01:25:58'),(27,4,'Deep','2017-06-21 01:40:43','2017-06-21 01:40:43'),(28,4,'Hard','2017-06-21 01:40:56','2017-06-21 01:40:56'),(30,5,'Hot','2017-06-24 01:38:58','2017-06-24 01:38:58'),(31,5,'Cool','2017-06-24 01:39:02','2017-06-24 01:39:02'),(36,7,'Wind','2017-12-14 08:10:06','2017-12-14 08:10:06'),(37,8,'Water','2017-12-14 08:19:48','2017-12-14 08:19:48'),(38,9,'Earth','2017-12-14 08:26:02','2017-12-14 08:26:02'),(39,10,'Water','2017-12-14 08:29:31','2017-12-14 08:29:31'),(72,19,'Earth','2017-12-22 19:43:34','2017-12-22 19:43:34'),(73,20,'Fire','2017-12-22 19:44:41','2017-12-22 19:44:41'),(75,6,'Wind','2017-12-23 23:29:50','2017-12-23 23:29:50'),(76,22,'Earth','2017-12-23 23:56:44','2017-12-23 23:56:44'),(83,23,'Wind','2017-12-24 01:14:19','2017-12-24 01:14:19'),(84,24,'Water','2017-12-24 03:10:47','2017-12-24 03:10:47'),(85,27,'Water','2018-03-16 00:57:19','2018-03-16 00:57:19');
/*!40000 ALTER TABLE `instrument_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `library`
--

LOCK TABLES `library` WRITE;
/*!40000 ALTER TABLE `library` DISABLE KEYS */;
INSERT INTO `library` VALUES (1,'Pots and Pans #2',1,'2017-02-10 00:03:23','2017-06-21 01:33:46'),(3,'Cool Ambience™',2,'2017-12-12 17:58:00','2018-01-02 00:44:13'),(4,'Test Library',4,'2018-02-09 20:56:50','2018-02-09 20:56:50'),(5,'Test Library',5,'2018-02-09 22:32:02','2018-02-09 22:32:02');
/*!40000 ALTER TABLE `library` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES (6,1,1,'Rhythm','2-Step Shuffle Beat',0.62,'C',133,'2017-04-23 23:21:52','2018-02-03 00:56:45','Published'),(7,1,1,'Macro','Deep, from Hot to Cool',0.6,'C',133,'2017-05-01 18:59:22','2018-02-03 00:56:45','Published'),(8,1,1,'Macro','Deep, from Cool to Hot',0.6,'G minor',133,'2017-05-01 18:59:32','2018-02-03 00:56:45','Published'),(9,1,1,'Main','I\'ll House You',0.5,'C',133,'2017-05-13 00:04:19','2018-02-03 00:56:45','Published'),(11,27,3,'Main','Water Galq',0.5,'E-',121,'2017-12-12 22:05:13','2018-03-28 05:44:46','Published'),(12,1,3,'Macro','Earth to Fire',0.5,'Ebm',121,'2017-12-13 06:38:26','2018-02-03 00:56:45','Published'),(13,1,3,'Macro','Earth to Water',0.5,'Gm',121,'2017-12-13 06:40:22','2018-02-03 00:56:45','Published'),(14,1,3,'Macro','Earth to Wind',0.5,'Cm',121,'2017-12-13 06:42:39','2018-02-03 00:56:45','Published'),(15,1,3,'Macro','Fire to Earth',0.5,'G',121,'2017-12-13 06:43:43','2018-02-03 00:56:45','Published'),(16,1,3,'Macro','Fire to Water',0.5,'E',121,'2017-12-13 06:45:43','2018-02-03 00:56:45','Published'),(17,1,3,'Macro','Fire to Wind',0.5,'G',121,'2017-12-13 06:46:56','2018-02-03 00:56:45','Published'),(18,1,3,'Macro','Wind to Earth',0.5,'Ebm',121,'2017-12-13 06:48:06','2018-02-03 00:56:45','Published'),(19,1,3,'Macro','Wind to Fire',0.5,'Bm',121,'2017-12-13 06:49:09','2018-02-03 00:56:45','Published'),(20,1,3,'Macro','Wind to Water',0.5,'Ebm',121,'2017-12-13 06:49:56','2018-02-03 00:56:45','Published'),(29,1,3,'Rhythm','2-Step Shuffle',0.62,'C',121,'2017-12-22 06:43:19','2018-02-03 00:56:45','Published'),(30,1,3,'Macro','Water to Wind',0.5,'G',121,'2017-12-23 22:11:19','2018-02-03 00:56:45','Published'),(31,1,3,'Macro','Water to Fire',0.5,'C',121,'2017-12-23 22:12:58','2018-02-03 00:56:45','Published'),(32,1,3,'Macro','Water to Earth',0.5,'G',121,'2017-12-23 22:14:36','2018-02-03 00:56:45','Published'),(34,1,3,'Rhythm','Half-time 2-Step Shuffle',0.62,'C',121,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published'),(35,27,3,'Main','Fire Camp',0.6,'C',121,'2018-01-19 20:51:14','2018-03-28 05:38:23','Published'),(46,1,3,'Detail','Hella Dope 2 (legacy)',0.5,'C',120,'2018-02-08 08:04:27','2018-03-18 21:29:25','Published'),(47,27,3,'Main','Earth First',0.5,'Bb',121,'2018-02-25 07:46:29','2018-02-25 07:46:29','Published'),(48,27,3,'Main','Fire Babes',0.5,'E-',121,'2018-03-02 06:35:24','2018-03-02 06:35:24','Published'),(49,27,3,'Main','Water Me Up',0.5,'F',121,'2018-03-02 07:00:10','2018-03-02 07:00:10','Published'),(50,27,3,'Main','Wind Terb',0.5,'D-',121,'2018-03-02 07:21:04','2018-03-02 07:21:04','Published'),(52,27,3,'Main','Earth Rudy',0.5,'D',121,'2018-03-02 07:34:58','2018-03-02 07:34:58','Published'),(53,27,3,'Main','Wind Bagz',0.4,'Eb',121,'2018-03-19 01:31:40','2018-03-28 05:16:51','Published'),(54,27,3,'Main','Wind Mole',0.4,'F',121,'2018-03-19 01:31:44','2018-03-19 01:31:44','Published'),(55,27,3,'Main','Bert',0.5,'B',121,'2018-03-22 19:15:11','2018-03-22 19:15:11','Published'),(56,27,3,'Main','Bert',0.5,'B',121,'2018-03-22 19:15:43','2018-03-22 19:15:43','Published'),(57,27,3,'Main','Water Wibs',0.6,'C#-',121,'2018-03-22 19:26:28','2018-03-23 04:23:27','Published'),(59,27,3,'Main','Earthen Satay',0.5,'Db',121,'2018-03-22 19:48:06','2018-03-23 04:37:07','Published'),(60,27,3,'Main','Fire Tom Perez',0.5,'C-',121,'2018-03-22 19:50:57','2018-03-28 04:53:27','Published'),(61,27,3,'Main','Earth Earth',0.5,'C',121,'2018-03-22 19:50:57','2018-06-05 03:32:57','Published'),(62,27,3,'Main','Wind Wind',0.5,'F',121,'2018-03-22 19:50:58','2018-06-05 03:41:09','Published'),(63,27,3,'Main','Temporary',0.5,'C',121,'2018-03-22 19:50:59','2018-03-22 19:50:59','Published'),(64,27,3,'Main','Fire Fire',0.5,'C#-',121,'2018-03-22 19:50:59','2018-06-14 03:04:20','Published'),(65,27,3,'Main','Water Water',0.5,'C#',121,'2018-03-22 19:51:00','2018-06-05 03:54:32','Published'),(66,27,3,'Main','Earth Knyght',0.5,'Bb-',121,'2018-03-22 19:51:00','2018-03-23 04:29:01','Published');
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sequence_meme`
--

LOCK TABLES `sequence_meme` WRITE;
/*!40000 ALTER TABLE `sequence_meme` DISABLE KEYS */;
INSERT INTO `sequence_meme` VALUES (1,6,'Classic','2017-04-23 23:22:21','2017-04-23 23:22:21'),(2,6,'Deep','2017-04-23 23:22:23','2017-04-23 23:22:23'),(3,6,'Acid','2017-04-23 23:22:24','2017-04-23 23:22:24'),(5,6,'Tech','2017-04-23 23:22:28','2017-04-23 23:22:28'),(6,6,'Electro','2017-04-23 23:22:31','2017-04-23 23:22:31'),(7,6,'Tropical','2017-04-23 23:22:34','2017-04-23 23:22:34'),(8,6,'Hot','2017-04-23 23:22:36','2017-04-23 23:22:36'),(9,6,'Cool','2017-04-23 23:22:39','2017-04-23 23:22:39'),(10,6,'Hard','2017-04-23 23:22:40','2017-04-23 23:22:40'),(11,6,'Easy','2017-04-23 23:22:42','2017-04-23 23:22:42'),(12,6,'Progressive','2017-04-23 23:23:17','2017-04-23 23:23:17'),(15,7,'Deep','2017-05-01 18:59:46','2017-05-01 18:59:46'),(16,8,'Deep','2017-05-01 19:42:36','2017-05-01 19:42:36'),(17,9,'Deep','2017-05-13 00:04:41','2017-05-13 00:04:41'),(18,9,'Classic','2017-05-13 00:04:44','2017-05-13 00:04:44'),(34,9,'Hard','2017-06-16 04:26:57','2017-06-16 04:26:57'),(36,11,'Earth','2017-12-13 00:44:05','2017-12-13 00:44:05'),(68,35,'Earth','2018-01-19 20:56:22','2018-01-19 20:56:22'),(72,47,'Earth','2018-03-18 21:30:31','2018-03-18 21:30:31');
/*!40000 ALTER TABLE `sequence_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern`
--

LOCK TABLES `pattern` WRITE;
/*!40000 ALTER TABLE `pattern` DISABLE KEYS */;
INSERT INTO `pattern` VALUES (3,'Loop',6,'drop d beet',0,4,NULL,NULL,NULL,'2017-04-23 23:44:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(4,'Macro',7,'from Hot',0,0,0.7,'C',133,'2017-05-01 19:39:59','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(5,'Macro',7,'to Cool',1,0,0.5,'Bb Minor',133,'2017-05-01 19:40:18','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(6,'Macro',8,'from Cool',0,0,0.5,'G minor',133,'2017-05-01 19:43:06','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(7,'Macro',8,'to Hot',1,0,0.7,'C',133,'2017-05-01 19:43:26','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(8,'Main',9,'Drop',0,32,0.4,'C',133,'2017-05-13 00:05:29','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(9,'Main',9,'Breakdown A',1,16,0.6,'G minor',133,'2017-05-13 00:07:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(14,'Main',9,'Breakdown B',2,16,0.8,'G minor',133,'2017-07-27 17:40:32','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(21,'Macro',12,'Passion Volcano',0,0,NULL,'Ebm',NULL,'2017-12-13 06:39:29','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(22,'Macro',12,'Falling in Love',1,0,NULL,'Db',NULL,'2017-12-13 06:40:03','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(23,'Macro',13,'Nostalgia River',0,0,NULL,'Gm',NULL,'2017-12-13 06:42:02','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(24,'Macro',13,'Passage of Time',1,0,NULL,'C',NULL,'2017-12-13 06:42:21','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(25,'Macro',14,'Spring',0,0,NULL,'Cm',NULL,'2017-12-13 06:43:03','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(26,'Macro',14,'Tornado',1,0,NULL,'F',NULL,'2017-12-13 06:43:24','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(27,'Macro',15,'Lightning Strike',0,0,NULL,'G',NULL,'2017-12-13 06:43:58','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(28,'Macro',15,'Car Racing',1,0,NULL,'E7',NULL,'2017-12-13 06:44:24','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(29,'Macro',16,'Volcanic Island',0,0,NULL,'E',NULL,'2017-12-13 06:46:14','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(30,'Macro',16,'Sex on the Beach',1,0,NULL,'Am',NULL,'2017-12-13 06:46:37','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(31,'Macro',17,'Smoke in the Air',0,0,NULL,'G',NULL,'2017-12-13 06:47:25','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(32,'Macro',17,'Dreams',1,0,NULL,'E',NULL,'2017-12-13 06:47:43','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(33,'Macro',18,'Open Road Tumbleweed',0,0,NULL,'Ebm',NULL,'2017-12-13 06:48:26','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(34,'Macro',18,'Rolling Stone',1,0,NULL,'D',NULL,'2017-12-13 06:48:48','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(35,'Macro',19,'Stoke the Flames',0,0,NULL,'Bm',NULL,'2017-12-13 06:49:21','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(36,'Macro',19,'Inspiration Adventure',1,0,NULL,'E',NULL,'2017-12-13 06:49:41','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(37,'Macro',20,'Make Waves',0,NULL,NULL,'Ebm',NULL,'2017-12-13 06:50:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(38,'Macro',20,'Bon Voyage',1,0,NULL,'D',NULL,'2017-12-13 06:50:29','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(64,'Loop',29,'Loop A',0,4,NULL,NULL,NULL,'2017-12-22 06:43:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(65,'Macro',12,'Exploding',2,0,NULL,'B',NULL,'2017-12-23 21:50:57','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(66,'Macro',13,'Arrival',2,0,NULL,'F',NULL,'2017-12-23 21:54:53','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(67,'Macro',14,'Fall',2,0,NULL,'Cm',NULL,'2017-12-23 21:57:50','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(68,'Macro',15,'Defeat',2,0,NULL,'Am',NULL,'2017-12-23 22:00:15','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(69,'Macro',16,'Glory',2,0,NULL,'F',NULL,'2017-12-23 22:02:07','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(70,'Macro',17,'Waking',2,0,NULL,'Am',NULL,'2017-12-23 22:04:27','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(71,'Macro',18,'Freedom',2,0,NULL,'Bm',NULL,'2017-12-23 22:05:48','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(72,'Macro',19,'Wilderness',2,0,NULL,'A',NULL,'2017-12-23 22:08:59','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(73,'Macro',20,'Afloat',2,0,NULL,'A',NULL,'2017-12-23 22:10:24','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(74,'Macro',30,'Rain',0,0,NULL,'G',NULL,'2017-12-23 22:11:35','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(75,'Macro',30,'Fog',1,0,NULL,'C',NULL,'2017-12-23 22:12:04','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(76,'Macro',30,'Dew',2,0,NULL,'Am',NULL,'2017-12-23 22:12:28','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(77,'Macro',31,'Hydrant',0,0,NULL,'C',NULL,'2017-12-23 22:13:12','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(78,'Macro',31,'Engine',1,0,NULL,'Dm',NULL,'2017-12-23 22:13:32','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(79,'Macro',31,'Steam',2,0,NULL,'C',NULL,'2017-12-23 22:13:50','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(80,'Macro',32,'Irrigation',0,0,NULL,'G',NULL,'2017-12-23 22:14:53','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(81,'Macro',32,'Nourishment',1,0,NULL,'C',NULL,'2017-12-23 22:15:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(82,'Macro',32,'Growth',2,0,NULL,'Am',NULL,'2017-12-23 22:15:42','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(86,'Loop',29,'Loop B',0,4,NULL,NULL,NULL,'2018-01-05 07:39:54','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(87,'Outro',29,'Outro A',0,4,NULL,NULL,NULL,'2018-01-05 08:37:43','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(88,'Outro',29,'Outro B',0,4,NULL,NULL,NULL,'2018-01-05 08:38:44','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(91,'Intro',29,'Intro A',0,4,NULL,NULL,NULL,'2018-01-05 09:43:57','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(92,'Intro',29,'Intro B',0,4,NULL,NULL,NULL,'2018-01-05 10:04:13','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(99,'Loop',34,'Loop A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(100,'Loop',34,'Loop B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(101,'Outro',34,'Outro A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(102,'Outro',34,'Outro B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(103,'Intro',34,'Intro A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(104,'Intro',34,'Intro B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(165,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:28','2018-02-08 08:04:28','Published',NULL,NULL,NULL),(166,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:28','2018-02-08 08:04:28','Published',NULL,NULL,NULL),(167,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:29','2018-02-08 08:04:29','Published',NULL,NULL,NULL),(168,'Loop',46,'Library Superphase',0,16,NULL,NULL,NULL,'2018-02-08 08:04:29','2018-02-08 08:04:29','Published',NULL,NULL,NULL),(169,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:29','2018-02-08 08:04:29','Published',NULL,NULL,NULL),(170,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:30','2018-02-08 08:04:30','Published',NULL,NULL,NULL),(171,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:30','2018-02-08 08:04:30','Published',NULL,NULL,NULL),(172,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:30','2018-02-08 08:04:30','Published',NULL,NULL,NULL),(173,'Loop',46,'Library Superphase',0,8,NULL,NULL,NULL,'2018-02-08 08:04:31','2018-02-08 08:04:31','Published',NULL,NULL,NULL),(174,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:31','2018-02-08 08:04:31','Published',NULL,NULL,NULL),(175,'Loop',46,'Library Superphase',0,16,NULL,NULL,NULL,'2018-02-08 08:04:31','2018-02-08 08:04:31','Published',NULL,NULL,NULL),(176,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:31','2018-02-08 08:04:31','Published',NULL,NULL,NULL),(177,'Loop',46,'Library Superphase',0,16,NULL,NULL,NULL,'2018-02-08 08:04:32','2018-02-08 08:04:32','Published',NULL,NULL,NULL),(178,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:32','2018-02-08 08:04:32','Published',NULL,NULL,NULL),(179,'Loop',46,'Library Superphase',0,32,NULL,NULL,NULL,'2018-02-08 08:04:32','2018-02-08 08:04:32','Published',NULL,NULL,NULL),(180,'Main',47,'Verse',0,16,NULL,NULL,NULL,'2018-02-25 07:48:50','2018-02-25 07:49:02','Published',NULL,NULL,NULL),(181,'Main',47,'Chorus',1,32,NULL,NULL,NULL,'2018-02-25 07:55:39','2018-02-25 07:55:39','Published',NULL,NULL,NULL),(182,'Main',47,'Interlude',2,8,NULL,NULL,NULL,'2018-02-25 08:00:47','2018-02-25 08:00:47','Published',NULL,NULL,NULL),(183,'Main',48,'Verse',0,16,NULL,NULL,NULL,'2018-03-02 06:36:51','2018-03-02 06:36:51','Published',NULL,NULL,NULL),(184,'Main',48,'Prechorus',1,32,NULL,NULL,NULL,'2018-03-02 06:39:44','2018-03-02 06:48:15','Published',NULL,NULL,NULL),(185,'Main',48,'Chorus',2,16,NULL,NULL,NULL,'2018-03-02 06:50:11','2018-03-02 06:50:11','Published',NULL,NULL,NULL),(186,'Main',48,'Bridge',3,32,NULL,NULL,NULL,'2018-03-02 06:53:35','2018-03-02 06:53:35','Published',NULL,NULL,NULL),(187,'Main',49,'A',0,16,NULL,NULL,NULL,'2018-03-02 07:01:49','2018-03-02 07:01:49','Published',NULL,NULL,NULL),(188,'Main',49,'B',1,32,NULL,NULL,NULL,'2018-03-02 07:04:34','2018-03-02 07:04:34','Published',NULL,NULL,NULL),(189,'Main',49,'C',2,8,NULL,NULL,NULL,'2018-03-02 07:13:38','2018-03-02 07:13:38','Published',NULL,NULL,NULL),(190,'Main',50,'Intro',0,64,NULL,NULL,NULL,'2018-03-02 07:22:43','2018-03-02 07:22:43','Published',NULL,NULL,NULL),(191,'Main',50,'A',1,16,NULL,NULL,NULL,'2018-03-02 07:25:38','2018-03-02 07:25:38','Published',NULL,NULL,NULL),(192,'Main',50,'B',2,32,NULL,NULL,NULL,'2018-03-02 07:28:02','2018-03-02 07:29:29','Published',NULL,NULL,NULL),(193,'Main',50,'Interlude',3,64,NULL,NULL,NULL,'2018-03-02 07:32:02','2018-03-02 07:32:02','Published',NULL,NULL,NULL),(194,'Main',52,'A',0,32,NULL,'D',NULL,'2018-03-19 01:11:16','2018-03-19 01:11:16','Published',NULL,NULL,NULL),(195,'Main',52,'B',1,32,NULL,NULL,NULL,'2018-03-19 01:20:17','2018-03-19 01:24:27','Published',NULL,NULL,NULL),(196,'Main',52,'C',2,32,NULL,NULL,NULL,'2018-03-19 01:25:28','2018-03-19 01:25:48','Published',NULL,NULL,NULL),(197,'Main',57,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:24:41','2018-03-23 04:24:41','Published',NULL,NULL,NULL),(198,'Main',57,'B',1,16,NULL,NULL,NULL,'2018-03-23 04:25:53','2018-03-23 04:25:53','Published',NULL,NULL,NULL),(199,'Main',66,'I',0,32,NULL,NULL,NULL,'2018-03-23 04:30:25','2018-03-23 04:30:25','Published',NULL,NULL,NULL),(200,'Main',66,'A',1,64,NULL,NULL,NULL,'2018-03-23 04:30:56','2018-03-23 04:30:56','Published',NULL,NULL,NULL),(201,'Main',59,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:37:54','2018-03-23 04:37:54','Published',NULL,NULL,NULL),(202,'Main',59,'B',1,32,NULL,NULL,NULL,'2018-03-23 04:38:37','2018-03-23 04:38:37','Published',NULL,NULL,NULL),(203,'Main',59,'C',2,16,NULL,NULL,NULL,'2018-03-23 04:41:10','2018-03-23 04:41:10','Published',NULL,NULL,NULL),(204,'Main',54,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:45:59','2018-03-23 04:45:59','Published',NULL,NULL,NULL),(205,'Main',54,'B',1,16,NULL,NULL,NULL,'2018-03-23 04:49:50','2018-03-23 04:51:05','Published',NULL,NULL,NULL),(206,'Main',60,'A',0,32,NULL,NULL,NULL,'2018-03-28 04:55:06','2018-03-28 04:55:06','Published',NULL,NULL,NULL),(207,'Main',60,'B',1,16,NULL,NULL,NULL,'2018-03-28 05:03:48','2018-03-28 05:03:48','Published',NULL,NULL,NULL),(208,'Main',53,'A',0,32,NULL,NULL,NULL,'2018-03-28 05:18:04','2018-03-28 05:18:04','Published',NULL,NULL,NULL),(209,'Main',53,'B',1,32,NULL,NULL,NULL,'2018-03-28 05:21:09','2018-03-28 05:21:09','Published',NULL,NULL,NULL),(210,'Main',35,'A',0,16,NULL,NULL,NULL,'2018-03-28 05:32:19','2018-03-28 05:32:19','Published',NULL,NULL,NULL),(211,'Main',35,'B',1,32,NULL,NULL,NULL,'2018-03-28 05:35:23','2018-03-28 05:35:23','Published',NULL,NULL,NULL),(212,'Main',11,'A',0,12,NULL,NULL,NULL,'2018-03-28 05:45:53','2018-03-28 05:45:53','Published',NULL,NULL,NULL),(213,'Main',11,'B',1,12,NULL,NULL,NULL,'2018-03-28 05:52:35','2018-03-28 05:52:35','Published',NULL,NULL,NULL),(214,'Main',11,'X',2,16,NULL,NULL,NULL,'2018-03-28 05:58:54','2018-03-28 05:58:54','Published',NULL,NULL,NULL),(215,'Main',61,'A',0,32,NULL,NULL,NULL,'2018-06-05 03:34:53','2018-06-05 03:34:53','Published',NULL,NULL,NULL),(216,'Main',62,'A',0,64,NULL,NULL,NULL,'2018-06-05 03:41:34','2018-06-05 03:41:34','Published',NULL,NULL,NULL),(217,'Main',65,'A',0,32,NULL,NULL,NULL,'2018-06-05 03:59:09','2018-06-05 03:59:09','Published',NULL,NULL,NULL),(218,'Main',64,'A',0,32,NULL,NULL,NULL,'2018-06-14 03:07:47','2018-06-14 03:27:53','Published',NULL,NULL,NULL);
/*!40000 ALTER TABLE `pattern` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_chord`
--

LOCK TABLES `pattern_chord` WRITE;
/*!40000 ALTER TABLE `pattern_chord` DISABLE KEYS */;
INSERT INTO `pattern_chord` VALUES (7,3,'C',0,'2017-04-23 23:44:43','2017-04-23 23:44:43'),(8,8,'C major 7',0,'2017-05-13 00:05:58','2017-06-16 03:54:30'),(9,8,'Cm7',8,'2017-05-13 00:06:11','2017-06-16 03:55:17'),(10,8,'F7',12,'2017-05-13 00:06:28','2017-06-16 03:58:00'),(11,8,'Bb major 7',16,'2017-05-13 00:06:41','2017-06-16 03:58:13'),(12,9,'D',0,'2017-05-13 00:07:40','2017-06-16 04:00:16'),(13,9,'G',4,'2017-05-13 00:07:47','2017-06-16 04:00:22'),(14,9,'C',8,'2017-05-13 00:07:55','2017-06-16 04:00:31'),(15,9,'F7',12,'2017-05-13 00:08:01','2017-06-16 04:31:20'),(19,8,'Bb m7',24,'2017-06-16 03:59:02','2017-06-16 03:59:02'),(20,8,'Eb7',28,'2017-06-16 03:59:38','2017-06-16 03:59:38'),(21,8,'Ab major 7',30,'2017-06-16 03:59:46','2017-06-16 03:59:46'),(32,14,'E minor 7',0,'2017-07-27 17:41:02','2017-07-27 17:41:02'),(33,14,'Eb minor 7',4,'2017-07-27 17:41:11','2017-07-27 17:41:11'),(38,14,'D minor 7',8,'2017-07-30 23:22:08','2017-07-30 23:22:08'),(39,14,'Db minor 7',12,'2017-07-30 23:22:15','2017-07-30 23:22:15'),(364,64,'C',0,'2017-12-22 06:43:19','2017-12-22 06:43:19'),(368,86,'C',0,'2018-01-05 07:39:55','2018-01-05 07:39:55'),(369,87,'C',0,'2018-01-05 08:37:43','2018-01-05 08:37:43'),(370,88,'C',0,'2018-01-05 08:38:44','2018-01-05 08:38:44'),(372,91,'C',0,'2018-01-05 09:43:58','2018-01-05 09:43:58'),(373,92,'C',0,'2018-01-05 10:04:13','2018-01-05 10:04:13'),(380,99,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(381,100,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(382,101,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(383,102,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(384,103,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(385,104,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(903,165,'C Major',0,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(904,165,'A Minor Sixth',2,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(905,165,'E Minor',4,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(906,165,'E Minor Seventh',6,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(907,165,'E Minor Sixth',8,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(908,165,'D Major',10,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(909,165,'G Major',12,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(910,165,'G Major Ninth',14,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(911,165,'C Major',16,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(912,165,'D Major Ninth',18,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(913,165,'G Major',20,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(914,165,'D Minor Seventh',22,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(915,165,'G Minor Seventh',24,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(916,165,'C Minor Seventh',26,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(917,165,'F Major Seventh',28,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(918,165,'A Diminished Major',30,'2018-02-08 08:04:28','2018-02-08 08:04:28'),(919,166,'G Minor',0,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(920,166,'F Major',2,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(921,166,'D Minor',4,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(922,166,'C Major',6,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(923,166,'A Minor',8,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(924,166,'F Major',10,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(925,166,'D Minor Sixth',12,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(926,166,'A Minor',14,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(927,166,'A# Major',16,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(928,166,'G Minor Seventh',18,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(929,166,'A# Major Add Ninth',20,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(930,166,'G# Major Ninth',22,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(931,166,'D Minor Seventh',24,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(932,166,'G Major Seventh Add Ninth',26,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(933,166,'D# Major',28,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(934,166,'F Minor',30,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(935,167,'E Major',0,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(936,167,'Db Minor',2,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(937,167,'Gb Minor',4,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(938,167,'A Major',6,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(939,167,'B Major',8,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(940,167,'Eb Minor',10,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(941,167,'E Major',12,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(942,167,'F# Major',14,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(943,167,'C# Major',16,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(944,167,'E Major',18,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(945,167,'F# Major',20,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(946,167,'C# Major',22,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(947,167,'F# Major',24,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(948,167,'C# Major',26,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(949,167,'F# Major',28,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(950,167,'A Major Ninth',30,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(951,168,'G Major Seventh',0,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(952,168,'G Major',2,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(953,168,'Db Minor Seventh Omit Fifth',4,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(954,168,'C Major',6,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(955,168,'D Major',8,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(956,168,'Ab Minor Seventh Omit Fifth',10,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(957,168,'G Major',12,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(958,168,'A Minor',14,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(959,169,'A# Major',0,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(960,169,'G Minor Seventh',2,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(961,169,'C Minor Seventh',4,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(962,169,'A# Major',6,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(963,169,'C Minor Seventh',8,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(964,169,'F Major Sixth',10,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(965,169,'F Minor Sixth',12,'2018-02-08 08:04:29','2018-02-08 08:04:29'),(966,169,'G Major Seventh',14,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(967,169,'D# Major Sixth',16,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(968,169,'F Major Sixth',18,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(969,169,'F Minor Sixth',20,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(970,169,'G Major Seventh',22,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(971,169,'D# Major Sixth',24,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(972,169,'F Major Sixth',26,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(973,169,'F Major Seventh',28,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(974,169,'A Diminished Major',30,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(975,170,'Bb Minor Seventh',0,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(976,170,'F Minor Seventh',2,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(977,170,'D# Major',4,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(978,170,'F Minor',6,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(979,170,'Bb Minor Seventh',8,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(980,170,'F Minor Seventh',10,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(981,170,'D# Major',12,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(982,170,'F Minor',14,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(983,170,'Bb Minor Seventh',16,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(984,170,'G# Major',18,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(985,170,'G Minor Seventh',20,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(986,170,'C Minor Seventh',22,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(987,170,'A# Major',24,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(988,170,'F Major',26,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(989,170,'D# Major',28,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(990,170,'C Minor Seventh',30,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(991,171,'G Major Seventh',0,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(992,171,'E Minor Seventh',2,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(993,171,'A Major Seventh',4,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(994,171,'D Minor Seventh',6,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(995,171,'G Major Seventh Add Ninth',8,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(996,171,'C Minor Seventh',10,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(997,171,'C Minor Seventh',12,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(998,171,'A# Major',14,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(999,171,'F Major',16,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1000,171,'D Minor Sixth',18,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1001,171,'A Minor',20,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1002,171,'F Major',22,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1003,171,'G Minor',24,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1004,171,'D Major Ninth',26,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1005,171,'G Major',28,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1006,171,'G Major Ninth',30,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1007,172,'F# Major',0,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1008,172,'B Major',2,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1009,172,'E Major',4,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1010,172,'Db Minor',6,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1011,172,'Gb Minor',8,'2018-02-08 08:04:30','2018-02-08 08:04:30'),(1012,172,'A Major',10,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1013,172,'E Major',12,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1014,172,'A Major',14,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1015,172,'E Major',16,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1016,172,'A Major',18,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1017,172,'E Major',20,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1018,172,'Db Minor',22,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1019,172,'G Major',24,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1020,172,'A Minor Seventh',26,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1021,172,'G Minor Seventh',28,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1022,172,'A Minor Seventh',30,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1023,173,'F# Major',0,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1024,173,'B Major',2,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1025,173,'F Major Sixth',4,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1026,173,'C Minor Seventh',6,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1027,174,'F Major',0,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1028,174,'G Minor',2,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1029,174,'F Major',4,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1030,174,'A# Major',6,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1031,174,'C Major',8,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1032,174,'A Minor',10,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1033,174,'A Minor Seventh',12,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1034,174,'D Minor Seventh',14,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1035,174,'G Major Seventh',16,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1036,174,'E Minor Seventh',18,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1037,174,'D Major',20,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1038,174,'B Major',22,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1039,174,'Eb Minor',24,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1040,174,'E Major',26,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1041,174,'F# Major',28,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1042,174,'A Major Ninth',30,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1043,175,'G Minor',0,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1044,175,'D# Major',2,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1045,175,'F Minor Seventh',4,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1046,175,'G Major',6,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1047,175,'D Major',8,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1048,175,'E Minor Seventh',10,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1049,175,'A Major Seventh',12,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1050,175,'A Major Seventh',14,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1051,176,'E Major',0,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1052,176,'B Major',2,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1053,176,'C Major',4,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1054,176,'A Minor',6,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1055,176,'F Major',8,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1056,176,'D# Major',10,'2018-02-08 08:04:31','2018-02-08 08:04:31'),(1057,176,'D Major',12,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1058,176,'A# Diminished Major',14,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1059,176,'D Major Ninth',16,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1060,176,'C Minor Seventh',18,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1061,176,'C Minor Seventh',20,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1062,176,'C Minor Seventh',22,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1063,176,'A# Major',24,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1064,176,'F Major',26,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1065,176,'D# Major',28,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1066,176,'F Minor',30,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1067,177,'A Major Seventh',0,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1068,177,'D Minor',2,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1069,177,'A# Major',4,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1070,177,'C Major',6,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1071,177,'G Major',8,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1072,177,'D# Diminished Major',10,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1073,177,'G Major Seventh',12,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1074,177,'A Minor Seventh',14,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1075,178,'A# Major',0,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1076,178,'C Major',2,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1077,178,'F Major',4,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1078,178,'D Minor Sixth',6,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1079,178,'A Minor',8,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1080,178,'A Minor Seventh',10,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1081,178,'G Minor Seventh',12,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1082,178,'C Minor Seventh',14,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1083,178,'C Major',16,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1084,178,'F Major',18,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1085,178,'G Minor',20,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1086,178,'F Major',22,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1087,178,'C Minor Seventh',24,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1088,178,'F Major',26,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1089,178,'D Minor Sixth',28,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1090,178,'A Minor',30,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1091,179,'C Major',0,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1092,179,'A Major Seventh',2,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1093,179,'D Major',4,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1094,179,'G Major',6,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1095,179,'D# Diminished Major',8,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1096,179,'G Major',10,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1097,179,'G Major Ninth',12,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1098,179,'C Major',14,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1099,179,'A Major Seventh',16,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1100,179,'D# Major',18,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1101,179,'A Major Seventh',20,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1102,179,'D Minor Seventh',22,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1103,179,'G Major Seventh Add Ninth',24,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1104,179,'C Minor Seventh',26,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1105,179,'F Major Seventh',28,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1106,179,'C Minor Seventh',30,'2018-02-08 08:04:32','2018-02-08 08:04:32'),(1107,180,'Bb',0,'2018-02-25 07:51:08','2018-02-25 07:51:08'),(1108,180,'D-',4,'2018-02-25 07:51:23','2018-02-25 07:51:23'),(1109,180,'Eb',8,'2018-02-25 07:51:38','2018-02-25 07:51:38'),(1110,180,'Bb',16,'2018-02-25 07:52:39','2018-02-25 07:52:39'),(1111,180,'G-',20,'2018-02-25 07:52:54','2018-02-25 07:52:54'),(1112,180,'Eb',24,'2018-02-25 07:53:43','2018-02-25 07:53:43'),(1113,181,'F',0,'2018-02-25 07:55:58','2018-02-25 07:55:58'),(1114,181,'G-',4,'2018-02-25 07:56:24','2018-02-25 07:56:24'),(1115,181,'Eb',8,'2018-02-25 07:56:56','2018-02-25 07:57:27'),(1116,181,'Bb',12,'2018-02-25 07:57:49','2018-02-25 07:57:49'),(1117,181,'C-',16,'2018-02-25 07:58:08','2018-02-25 07:58:08'),(1118,181,'G-',20,'2018-02-25 07:58:27','2018-02-25 07:58:27'),(1119,181,'Eb',24,'2018-02-25 07:58:37','2018-02-25 07:58:37'),(1120,182,'D-',0,'2018-02-25 08:01:09','2018-02-25 08:01:09'),(1121,182,'Fsus4',1.5,'2018-02-25 08:02:08','2018-03-02 06:33:19'),(1122,183,'E-',0,'2018-03-02 06:37:10','2018-03-02 06:37:10'),(1123,183,'G',8,'2018-03-02 06:37:25','2018-03-02 06:37:25'),(1124,183,'A',12,'2018-03-02 06:38:12','2018-03-02 06:38:12'),(1125,184,'A-',0,'2018-03-02 06:41:00','2018-03-02 06:41:00'),(1126,184,'Bsus4',4,'2018-03-02 06:41:29','2018-03-02 06:41:29'),(1127,184,'C',8,'2018-03-02 06:42:27','2018-03-02 06:42:27'),(1128,184,'G',12,'2018-03-02 06:42:36','2018-03-02 06:42:36'),(1129,184,'F',16,'2018-03-02 06:45:50','2018-03-02 06:45:50'),(1130,184,'C/D',24,'2018-03-02 06:46:26','2018-03-02 06:46:26'),(1131,185,'E-',0,'2018-03-02 06:50:21','2018-03-02 06:50:21'),(1132,185,'C',4,'2018-03-02 06:51:06','2018-03-02 06:51:06'),(1133,185,'A-',8,'2018-03-02 06:51:13','2018-03-02 06:51:13'),(1134,185,'G',12,'2018-03-02 06:51:30','2018-03-02 06:51:30'),(1135,185,'D',14,'2018-03-02 06:51:36','2018-03-02 06:51:36'),(1136,186,'F',0,'2018-03-02 06:53:53','2018-03-02 06:53:53'),(1137,186,'A-',8,'2018-03-02 06:54:21','2018-03-02 06:54:21'),(1138,186,'D-',16,'2018-03-02 06:54:29','2018-03-02 06:54:50'),(1139,186,'C/G',24,'2018-03-02 06:56:22','2018-03-02 06:56:22'),(1140,187,'Fmaj7',0,'2018-03-02 07:02:02','2018-03-02 07:02:02'),(1141,187,'Ebmaj7',4,'2018-03-02 07:02:13','2018-03-02 07:02:13'),(1142,188,'Bb-7',0,'2018-03-02 07:06:34','2018-03-02 07:06:34'),(1143,188,'Gbmaj7',4,'2018-03-02 07:06:45','2018-03-02 07:06:45'),(1144,188,'Eb-7',12,'2018-03-02 07:07:07','2018-03-02 07:07:07'),(1145,188,'Bb-7',16,'2018-03-02 07:08:40','2018-03-02 07:08:40'),(1146,188,'Gbmaj7',20,'2018-03-02 07:08:51','2018-03-02 07:08:51'),(1147,188,'Gbmaj7/Ab',28,'2018-03-02 07:09:17','2018-03-02 07:09:17'),(1148,189,'Fmaj7',0,'2018-03-02 07:13:49','2018-03-02 07:13:49'),(1149,189,'Dbmaj7',3.5,'2018-03-02 07:14:42','2018-03-02 07:14:42'),(1150,189,'Bb-7',7.5,'2018-03-02 07:15:41','2018-03-02 07:15:41'),(1151,189,'Gbmaj7',11.5,'2018-03-02 07:16:09','2018-03-02 07:16:09'),(1152,190,'NC',0,'2018-03-02 07:22:54','2018-03-02 07:22:54'),(1153,191,'D-',0,'2018-03-02 07:25:57','2018-03-02 07:25:57'),(1154,191,'D-/G',3,'2018-03-02 07:26:09','2018-03-02 07:26:09'),(1155,191,'D-/F',11,'2018-03-02 07:26:28','2018-03-02 07:26:28'),(1156,192,'D-/C',0,'2018-03-02 07:29:45','2018-03-02 07:29:45'),(1157,192,'Bbmaj7',8,'2018-03-02 07:30:29','2018-03-02 07:30:29'),(1158,192,'D-/F',16,'2018-03-02 07:31:01','2018-03-02 07:31:01'),(1159,192,'D-/G',24,'2018-03-02 07:31:13','2018-03-02 07:31:13'),(1160,193,'NC',0,'2018-03-02 07:32:10','2018-03-02 07:32:10'),(1161,194,'D',0,'2018-03-19 01:11:31','2018-03-19 01:11:31'),(1162,194,'F#-',3.5,'2018-03-19 01:12:20','2018-03-19 01:12:20'),(1163,194,'G',7.5,'2018-03-19 01:12:39','2018-03-19 01:12:39'),(1164,194,'E-7',13.5,'2018-03-19 01:13:00','2018-03-19 01:13:00'),(1165,194,'D',16,'2018-03-19 01:13:13','2018-03-19 01:13:13'),(1166,194,'F#-',19.5,'2018-03-19 01:13:34','2018-03-19 01:13:34'),(1167,194,'G',23.5,'2018-03-19 01:13:50','2018-03-19 01:13:50'),(1168,194,'Gmaj7/A',29.5,'2018-03-19 01:14:59','2018-03-19 01:14:59'),(1169,195,'B-',0,'2018-03-19 01:20:31','2018-03-19 01:20:31'),(1170,195,'E-',4,'2018-03-19 01:20:47','2018-03-19 01:20:47'),(1171,195,'G',12,'2018-03-19 01:20:55','2018-03-19 01:20:55'),(1172,195,'B-',16,'2018-03-19 01:21:13','2018-03-19 01:21:13'),(1173,195,'E-',20,'2018-03-19 01:21:26','2018-03-19 01:21:26'),(1174,195,'G',28,'2018-03-19 01:23:58','2018-03-19 01:23:58'),(1175,196,'F#-',0,'2018-03-19 01:26:01','2018-03-19 01:26:01'),(1176,197,'C#-7',0,'2018-03-23 04:24:57','2018-03-23 04:24:57'),(1177,197,'D#-7',14.5,'2018-03-23 04:25:34','2018-03-23 04:25:34'),(1178,198,'G#-',0,'2018-03-23 04:26:31','2018-03-23 04:26:31'),(1179,198,'Amaj7/E',3.75,'2018-03-23 04:27:09','2018-03-23 04:27:09'),(1180,198,'Bmaj6',11.75,'2018-03-23 04:27:34','2018-03-23 04:27:34'),(1181,199,'Bb-',0,'2018-03-23 04:30:36','2018-03-23 04:30:36'),(1182,200,'Bb-',0,'2018-03-23 04:31:12','2018-03-23 04:31:12'),(1183,200,'Abmaj6/9',8,'2018-03-23 04:32:20','2018-03-23 04:32:20'),(1184,200,'Gbmaj7',16,'2018-03-23 04:32:33','2018-03-23 04:32:33'),(1185,200,'Db',24,'2018-03-23 04:32:55','2018-03-23 04:32:55'),(1186,200,'Eb-',28,'2018-03-23 04:33:05','2018-03-23 04:33:05'),(1187,200,'Bb-',32,'2018-03-23 04:33:17','2018-03-23 04:33:17'),(1188,200,'Abmaj6/9',40,'2018-03-23 04:33:36','2018-03-23 04:33:36'),(1189,200,'Gbmaj7',48,'2018-03-23 04:33:45','2018-03-23 04:33:45'),(1190,200,'Fsus4',56,'2018-03-23 04:34:03','2018-03-23 04:34:03'),(1191,200,'F',60,'2018-03-23 04:34:11','2018-03-23 04:34:11'),(1192,201,'Bb-',0,'2018-03-23 04:38:06','2018-03-23 04:38:06'),(1193,201,'Db',8,'2018-03-23 04:38:15','2018-03-23 04:38:15'),(1194,202,'Gb',0,'2018-03-23 04:39:01','2018-03-23 04:39:01'),(1195,202,'Ab',4,'2018-03-23 04:39:20','2018-03-23 04:39:20'),(1196,202,'Bb-',6,'2018-03-23 04:39:30','2018-03-23 04:39:30'),(1197,202,'Eb-',8,'2018-03-23 04:39:41','2018-03-23 04:39:41'),(1198,202,'Db/F',14,'2018-03-23 04:39:55','2018-03-23 04:39:55'),(1199,202,'Gb',16,'2018-03-23 04:40:06','2018-03-23 04:40:06'),(1200,202,'Ab',20,'2018-03-23 04:40:16','2018-03-23 04:40:16'),(1201,202,'Bb-',22,'2018-03-23 04:40:27','2018-03-23 04:40:27'),(1202,202,'Eb-',24,'2018-03-23 04:40:35','2018-03-23 04:40:35'),(1203,202,'Ab7sus4',28,'2018-03-23 04:40:53','2018-03-23 04:40:53'),(1204,203,'F-7',0,'2018-03-23 04:41:24','2018-03-23 04:41:24'),(1205,203,'Gbmaj6/9',8,'2018-03-23 04:41:48','2018-03-23 04:41:48'),(1206,203,'Ab7sus4',12,'2018-03-23 04:41:58','2018-03-23 04:41:58'),(1207,204,'Fsus4add3',0,'2018-03-23 04:46:22','2018-03-23 04:46:22'),(1208,204,'C7sus4',8,'2018-03-23 04:47:22','2018-03-23 04:47:22'),(1209,205,'Ebmaj6/9',0,'2018-03-23 04:50:43','2018-03-23 04:50:43'),(1210,205,'Fsus4/Gb',8,'2018-03-23 04:50:54','2018-03-23 04:50:54'),(1211,206,'C-',0,'2018-03-28 04:55:15','2018-03-28 04:55:15'),(1212,206,'Db',4,'2018-03-28 04:55:24','2018-03-28 04:55:24'),(1213,206,'Bb-',12,'2018-03-28 04:55:37','2018-03-28 04:55:37'),(1214,206,'C-',16,'2018-03-28 04:55:44','2018-03-28 04:55:44'),(1215,206,'Db',20,'2018-03-28 04:55:54','2018-03-28 04:55:54'),(1216,206,'E-',29.5,'2018-03-28 05:00:17','2018-03-28 05:00:17'),(1217,207,'Ab-',0,'2018-03-28 05:04:21','2018-03-28 05:04:21'),(1218,207,'B',4,'2018-03-28 05:04:28','2018-03-28 05:04:28'),(1219,207,'Eb-',8,'2018-03-28 05:04:39','2018-03-28 05:04:39'),(1220,208,'Ebmaj7',0,'2018-03-28 05:18:24','2018-03-28 05:18:24'),(1221,208,'G-7',8,'2018-03-28 05:18:44','2018-03-28 05:18:44'),(1222,208,'Ebmaj7',16,'2018-03-28 05:19:09','2018-03-28 05:19:09'),(1223,208,'G-7',24,'2018-03-28 05:19:23','2018-03-28 05:19:23'),(1224,208,'F-7',27.5,'2018-03-28 05:19:40','2018-03-28 05:19:40'),(1225,208,'Abmaj7',29.5,'2018-03-28 05:19:50','2018-03-28 05:19:50'),(1226,209,'Abmaj7',0,'2018-03-28 05:21:23','2018-03-28 05:21:23'),(1227,209,'F-7',8,'2018-03-28 05:21:30','2018-03-28 05:21:30'),(1228,209,'C-',16,'2018-03-28 05:23:40','2018-03-28 05:23:40'),(1229,209,'Bb7sus4',24,'2018-03-28 05:24:05','2018-03-28 05:24:05'),(1230,210,'Cmaj7',0,'2018-03-28 05:32:36','2018-03-28 05:32:36'),(1231,210,'Emaj7',3.5,'2018-03-28 05:32:45','2018-03-28 05:32:45'),(1232,210,'Abmaj7',7.5,'2018-03-28 05:33:06','2018-03-28 05:33:06'),(1233,210,'Fmaj7',11.5,'2018-03-28 05:33:28','2018-03-28 05:33:28'),(1234,211,'Dbmaj7',0,'2018-03-28 05:36:02','2018-03-28 05:36:02'),(1235,211,'Amaj7',3.5,'2018-03-28 05:36:16','2018-03-28 05:36:16'),(1236,211,'Fmaj7',8,'2018-03-28 05:36:25','2018-03-28 05:36:25'),(1237,211,'Dbmaj7',16,'2018-03-28 05:36:38','2018-03-28 05:36:38'),(1238,211,'Amaj7',19.5,'2018-03-28 05:36:49','2018-03-28 05:36:49'),(1239,211,'Fmaj7',24,'2018-03-28 05:37:02','2018-03-28 05:37:02'),(1240,212,'E-7',0,'2018-03-28 05:46:04','2018-03-28 05:47:21'),(1241,212,'Fmaj7',4,'2018-03-28 05:46:15','2018-03-28 05:46:15'),(1242,212,'A-7',8,'2018-03-28 05:47:31','2018-03-28 05:47:31'),(1243,213,'Dmaj6',0,'2018-03-28 05:53:22','2018-03-28 05:53:22'),(1244,213,'Cmaj7/E',4,'2018-03-28 05:53:33','2018-03-28 05:53:33'),(1245,213,'Cmaj7add9',8,'2018-03-28 05:53:49','2018-03-28 05:53:49'),(1246,214,'D/G',0,'2018-03-28 05:59:06','2018-03-28 05:59:06'),(1247,214,'C/G',8,'2018-03-28 05:59:14','2018-03-28 05:59:14'),(1248,215,'C',0,'2018-06-05 03:35:03','2018-06-05 03:35:03'),(1249,215,'A-7',4,'2018-06-05 03:35:12','2018-06-05 03:35:12'),(1250,215,'E-7',8,'2018-06-05 03:35:19','2018-06-05 03:35:19'),(1251,215,'Fmaj6',12,'2018-06-05 03:35:26','2018-06-05 03:35:26'),(1252,215,'C',16,'2018-06-05 03:35:34','2018-06-05 03:35:34'),(1253,215,'A-7',20,'2018-06-05 03:35:44','2018-06-05 03:35:44'),(1254,215,'D-7',24,'2018-06-05 03:35:55','2018-06-05 03:35:55'),(1255,215,'Fmaj6',27.5,'2018-06-05 03:36:27','2018-06-05 03:36:27'),(1256,215,'F/G',29.5,'2018-06-05 03:36:38','2018-06-05 03:36:38'),(1257,216,'F5',0,'2018-06-05 03:41:47','2018-06-05 03:41:47'),(1258,216,'F5/D',16,'2018-06-05 03:42:04','2018-06-05 03:42:04'),(1259,216,'F5/Db',32,'2018-06-05 03:42:21','2018-06-05 03:42:21'),(1260,216,'Abmaj6',48,'2018-06-05 03:43:28','2018-06-05 03:43:28'),(1261,216,'C7sus4',60,'2018-06-05 03:46:02','2018-06-05 03:46:02'),(1262,217,'C#maj7',0,'2018-06-05 03:59:30','2018-06-05 03:59:30'),(1263,217,'F#-7add9',1.5,'2018-06-05 03:59:51','2018-06-05 04:00:19'),(1264,217,'G#-7',8,'2018-06-05 04:00:13','2018-06-05 04:00:13'),(1265,217,'Emaj7add9',9.5,'2018-06-05 04:00:52','2018-06-05 04:00:52'),(1266,217,'C#maj7',16,'2018-06-05 04:01:24','2018-06-05 04:01:24'),(1267,217,'Amaj7',17.5,'2018-06-05 04:01:41','2018-06-05 04:01:41'),(1268,217,'G#-7',24,'2018-06-05 04:02:02','2018-06-05 04:02:02'),(1269,217,'Dmaj7add13',25.5,'2018-06-05 04:03:24','2018-06-05 04:03:24'),(1270,218,'C#-',0,'2018-06-14 03:08:03','2018-06-14 03:08:03'),(1271,218,'C#sus4/D',1.5,'2018-06-14 03:08:25','2018-06-14 03:08:25'),(1272,218,'Emaj7add9',3.5,'2018-06-14 03:08:56','2018-06-14 03:08:56'),(1273,218,'F#-6',5.5,'2018-06-14 03:09:22','2018-06-14 03:09:22'),(1274,218,'C#-7/B',8,'2018-06-14 03:10:10','2018-06-14 03:21:10'),(1275,218,'Emaj7/G#',16,'2018-06-14 03:10:54','2018-06-14 03:22:26'),(1276,218,'E/A',17.5,'2018-06-14 03:24:01','2018-06-14 03:24:01'),(1277,218,'E/B',19.5,'2018-06-14 03:24:20','2018-06-14 03:24:20'),(1278,218,'Bmaj6',21.5,'2018-06-14 03:24:41','2018-06-14 03:24:41'),(1279,218,'Badd4/C#',24,'2018-06-14 03:32:37','2018-06-14 03:35:13');
/*!40000 ALTER TABLE `pattern_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_meme`
--

LOCK TABLES `pattern_meme` WRITE;
/*!40000 ALTER TABLE `pattern_meme` DISABLE KEYS */;
INSERT INTO `pattern_meme` VALUES (5,6,'Cool','2017-05-01 19:43:30','2017-05-01 19:43:30'),(7,7,'Hot','2017-05-01 19:44:52','2017-05-01 19:44:52'),(8,4,'Hot','2017-05-01 19:45:58','2017-05-01 19:45:58'),(9,5,'Cool','2017-05-01 19:46:10','2017-05-01 19:46:10'),(10,4,'Tropical','2017-06-16 03:37:25','2017-06-16 03:37:25'),(11,5,'Electro','2017-06-16 03:38:03','2017-06-16 03:38:03'),(12,6,'Hard','2017-06-16 03:38:19','2017-06-16 03:38:19'),(13,7,'Easy','2017-06-16 03:38:40','2017-06-16 03:38:40'),(15,14,'Hard','2017-07-29 23:48:20','2017-07-29 23:48:20'),(22,21,'Earth','2017-12-13 06:39:45','2017-12-13 06:39:45'),(23,22,'Fire','2017-12-13 06:40:10','2017-12-13 06:40:10'),(24,23,'Earth','2017-12-13 06:42:09','2017-12-13 06:42:09'),(25,24,'Water','2017-12-13 06:42:26','2017-12-13 06:42:26'),(26,25,'Earth','2017-12-13 06:43:08','2017-12-13 06:43:08'),(27,26,'Wind','2017-12-13 06:43:31','2017-12-13 06:43:31'),(28,27,'Fire','2017-12-13 06:44:02','2017-12-13 06:44:02'),(29,28,'Earth','2017-12-13 06:44:33','2017-12-13 06:44:33'),(30,29,'Fire','2017-12-13 06:46:18','2017-12-13 06:46:18'),(31,30,'Water','2017-12-13 06:46:44','2017-12-13 06:46:44'),(32,31,'Fire','2017-12-13 06:47:29','2017-12-13 06:47:29'),(33,32,'Wind','2017-12-13 06:47:51','2017-12-13 06:47:51'),(34,33,'Wind','2017-12-13 06:48:33','2017-12-13 06:48:33'),(35,34,'Earth','2017-12-13 06:48:55','2017-12-13 06:48:55'),(36,35,'Wind','2017-12-13 06:49:27','2017-12-13 06:49:27'),(37,36,'Fire','2017-12-13 06:49:47','2017-12-13 06:49:47'),(38,37,'Wind','2017-12-13 06:50:16','2017-12-13 06:50:16'),(39,38,'Water','2017-12-13 06:50:33','2017-12-13 06:50:33'),(66,65,'Fire','2017-12-23 21:51:03','2017-12-23 21:51:03'),(67,22,'Earth','2017-12-23 21:51:12','2017-12-23 21:51:12'),(68,66,'Water','2017-12-23 21:54:58','2017-12-23 21:54:58'),(69,24,'Earth','2017-12-23 21:55:07','2017-12-23 21:55:07'),(70,67,'Wind','2017-12-23 21:58:02','2017-12-23 21:58:02'),(71,26,'Earth','2017-12-23 21:58:10','2017-12-23 21:58:10'),(72,68,'Earth','2017-12-23 22:00:20','2017-12-23 22:00:20'),(73,28,'Fire','2017-12-23 22:00:39','2017-12-23 22:00:39'),(74,69,'Water','2017-12-23 22:02:13','2017-12-23 22:02:13'),(75,30,'Fire','2017-12-23 22:02:20','2017-12-23 22:02:20'),(76,32,'Fire','2017-12-23 22:04:01','2017-12-23 22:04:01'),(77,70,'Wind','2017-12-23 22:04:33','2017-12-23 22:04:33'),(78,34,'Wind','2017-12-23 22:05:35','2017-12-23 22:05:35'),(79,71,'Earth','2017-12-23 22:05:54','2017-12-23 22:05:54'),(80,72,'Fire','2017-12-23 22:09:04','2017-12-23 22:09:04'),(81,36,'Wind','2017-12-23 22:09:21','2017-12-23 22:09:21'),(82,38,'Wind','2017-12-23 22:10:10','2017-12-23 22:10:10'),(83,73,'Water','2017-12-23 22:10:31','2017-12-23 22:10:31'),(84,74,'Water','2017-12-23 22:11:41','2017-12-23 22:11:41'),(85,75,'Water','2017-12-23 22:12:10','2017-12-23 22:12:10'),(86,75,'Wind','2017-12-23 22:12:14','2017-12-23 22:12:14'),(87,76,'Wind','2017-12-23 22:12:33','2017-12-23 22:12:33'),(88,77,'Water','2017-12-23 22:13:17','2017-12-23 22:13:17'),(89,78,'Water','2017-12-23 22:13:36','2017-12-23 22:13:36'),(90,78,'Fire','2017-12-23 22:13:39','2017-12-23 22:13:39'),(91,79,'Fire','2017-12-23 22:13:54','2017-12-23 22:13:54'),(92,80,'Water','2017-12-23 22:14:58','2017-12-23 22:14:58'),(93,81,'Water','2017-12-23 22:15:25','2017-12-23 22:15:25'),(94,81,'Earth','2017-12-23 22:15:29','2017-12-23 22:15:29'),(95,82,'Earth','2017-12-23 22:15:47','2017-12-23 22:15:47');
/*!40000 ALTER TABLE `pattern_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_event`
--

LOCK TABLES `pattern_event` WRITE;
/*!40000 ALTER TABLE `pattern_event` DISABLE KEYS */;
INSERT INTO `pattern_event` VALUES (270,3,4,0.3,1,'KICKLONG',2.5,0.5,'C2','2017-06-02 23:57:53','2018-01-03 21:36:07'),(274,3,4,1,0.2,'SNARE',1,1,'G8','2017-06-02 23:58:37','2018-01-03 21:36:07'),(275,3,5,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-06-03 00:09:06','2018-01-03 21:36:07'),(276,3,5,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-06-03 00:10:09','2018-01-03 21:36:07'),(277,3,5,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-06-03 00:10:14','2018-01-03 21:36:07'),(278,3,5,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-06-03 00:10:19','2018-01-03 21:36:07'),(280,3,5,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2017-06-03 00:11:48','2018-01-05 09:32:21'),(281,3,5,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2017-06-03 00:11:52','2018-01-05 09:32:21'),(282,3,5,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-06-03 00:11:57','2018-01-03 21:36:07'),(283,3,5,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2017-06-03 00:12:02','2018-01-05 09:32:21'),(284,3,5,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2017-06-03 00:12:32','2018-01-05 09:32:21'),(285,3,5,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2017-06-03 00:12:37','2018-01-05 09:32:21'),(286,3,5,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2017-06-03 00:12:41','2018-01-05 09:32:21'),(287,3,5,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2017-06-03 00:12:46','2018-01-05 09:32:21'),(288,3,5,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-06-03 00:13:33','2018-01-03 21:36:07'),(290,3,5,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2017-06-03 00:13:43','2018-01-05 09:32:21'),(291,3,5,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-06-03 00:13:47','2018-01-03 21:36:07'),(294,3,4,0.2,1,'KICK',2.25,0.2,'F#2','2017-06-04 04:26:37','2018-01-05 09:32:21'),(301,3,5,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-06-04 04:49:14','2018-01-03 21:36:07'),(302,3,5,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-06-04 04:51:11','2018-01-03 21:36:07'),(303,3,5,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-06-04 04:51:23','2018-01-03 21:36:07'),(304,3,5,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-06-04 04:51:28','2018-01-03 21:36:07'),(305,3,5,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-06-04 04:51:34','2018-01-03 21:36:07'),(306,3,5,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2017-06-04 04:51:43','2018-01-05 09:32:21'),(314,3,8,0.1,0.6,'TOM',0.5,0.75,'C6','2017-06-11 19:51:53','2018-01-03 21:36:07'),(315,3,8,0.05,0.6,'TOM',1.25,0.7,'G5','2017-06-11 19:52:17','2018-01-05 09:32:21'),(316,3,8,0.2,0.6,'TOM',2,1,'C5','2017-06-11 19:53:15','2018-01-03 21:36:07'),(318,3,8,0.1,0.6,'CONGA',0,1,'F5','2017-06-11 20:15:46','2018-01-03 21:36:07'),(320,3,8,0.1,0.6,'TOM',3.5,0.5,'G3','2017-06-11 20:16:54','2018-01-03 21:36:07'),(322,3,4,0.1,0.2,'SNARE',1.75,0.2,'G5','2017-06-12 19:14:16','2018-01-05 09:32:21'),(323,3,8,0.05,0.5,'COWBELL',2,1,'F5','2017-06-12 19:20:22','2018-01-03 21:36:07'),(329,3,8,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2017-06-15 22:52:40','2018-01-05 09:32:21'),(339,3,12,1,1,'KICKLONG',0,1,'C2','2017-12-07 03:43:32','2018-01-03 21:36:07'),(341,3,12,0.8,1,'KICK',2.5,1,'C2','2017-12-07 03:43:52','2018-01-03 21:36:07'),(343,3,4,1,0.1,'SNARE',3,1,'G8','2017-12-07 08:17:58','2018-01-03 21:36:07'),(371,3,17,0.05,0.5,'TOMLOW',2,0.8,'G4','2017-12-21 00:38:41','2018-01-07 00:38:37'),(372,3,17,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2017-12-21 00:39:01','2018-01-07 00:38:37'),(373,3,17,0.1,0.5,'TOMLOW',3.5,1,'G4','2017-12-21 00:39:18','2018-01-07 00:38:37'),(374,3,17,0.05,0.5,'TOMLOW',1,0.5,'G4','2017-12-21 00:39:32','2018-01-07 00:38:37'),(375,3,17,0.1,0.5,'TOMLOW',1,1,'G4','2017-12-21 00:39:41','2018-01-07 00:38:37'),(376,64,18,1,0.2,'SNARE',1,1,'G8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(378,64,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2017-12-22 06:43:19','2018-01-05 09:32:21'),(379,64,18,0.2,1,'KICK',2.25,0.2,'F#2','2017-12-22 06:43:19','2018-01-05 09:32:21'),(380,64,18,0.3,1,'KICKLONG',2.5,0.5,'C2','2017-12-22 06:43:19','2018-01-03 21:36:07'),(382,64,18,1,0.1,'SNARE',3,1,'G8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(383,64,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-12-22 06:43:19','2018-01-03 21:36:07'),(384,64,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(385,64,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(386,64,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(387,64,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(388,64,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-12-22 06:43:19','2018-01-03 21:36:07'),(389,64,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(390,64,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(391,64,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(392,64,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(393,64,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(394,64,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2017-12-22 06:43:20','2018-01-05 09:32:21'),(395,64,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(396,64,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(397,64,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(398,64,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(399,64,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(400,64,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2017-12-22 06:43:20','2018-01-05 09:32:21'),(401,64,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(402,64,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(403,64,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(404,64,20,0.1,0.6,'CONGA',0,1,'F5','2017-12-22 06:43:20','2018-01-03 21:36:07'),(405,64,20,0.1,0.6,'TOM',0.5,0.75,'C6','2017-12-22 06:43:20','2018-01-03 21:36:07'),(406,64,20,0.05,0.6,'TOM',1.25,0.7,'G5','2017-12-22 06:43:20','2018-01-05 09:32:21'),(407,64,20,0.2,0.6,'TOM',2,1,'C5','2017-12-22 06:43:20','2018-01-03 21:36:07'),(409,64,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2017-12-22 06:43:20','2018-01-05 09:32:21'),(410,64,20,0.1,0.6,'TOM',3.5,0.5,'G3','2017-12-22 06:43:20','2018-01-03 21:36:07'),(418,64,23,1,1,'KICKLONG',0,1,'C2','2017-12-22 06:43:20','2018-01-03 21:36:07'),(419,64,23,0.8,1,'KICK',2.5,1,'C2','2017-12-22 06:43:20','2018-01-03 21:36:07'),(420,64,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(423,64,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(424,64,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(548,86,20,0.1,0.6,'CONGA',0,1,'F5','2018-01-05 07:39:55','2018-01-05 07:39:55'),(549,86,23,1,1,'KICKLONG',0,1,'C2','2018-01-05 07:39:55','2018-01-05 07:39:55'),(550,86,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(551,86,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(552,86,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 07:39:55','2018-01-05 07:39:55'),(553,86,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(554,86,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(555,86,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 07:39:55','2018-01-07 00:38:37'),(556,86,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(557,86,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(558,86,18,1,0.2,'SNARE',1,1,'G8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(560,86,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(561,86,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 07:39:55','2018-01-05 09:32:21'),(562,86,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(563,86,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(564,86,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(565,86,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 07:39:55','2018-01-05 09:32:21'),(566,86,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(569,86,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 07:39:55','2018-01-05 09:32:21'),(571,86,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 07:39:55','2018-01-05 07:39:55'),(572,86,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(573,86,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(574,86,18,0.3,1,'KICKLONG',2.5,0.5,'C2','2018-01-05 07:39:55','2018-01-05 07:39:55'),(575,86,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(577,86,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(578,86,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(580,86,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 09:32:21'),(581,86,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(582,86,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 07:39:55','2018-01-05 09:32:21'),(583,86,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(584,86,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(585,86,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 07:39:55','2018-01-05 07:39:55'),(586,86,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 07:39:55','2018-01-07 00:38:37'),(587,86,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(588,86,23,0.8,1,'KICK',2,1,'C2','2018-01-05 08:36:52','2018-01-05 08:36:52'),(589,87,20,0.1,0.6,'CONGA',0,1,'F5','2018-01-05 08:37:43','2018-01-05 08:37:43'),(590,87,23,1,1,'KICKLONG',0,1,'C2','2018-01-05 08:37:43','2018-01-05 08:37:43'),(591,87,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 08:37:43','2018-01-05 08:37:43'),(592,87,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(593,87,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 08:37:43','2018-01-05 08:37:43'),(594,87,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(595,87,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(596,87,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 08:37:43','2018-01-07 00:38:37'),(597,87,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(598,87,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 08:37:43','2018-01-05 08:37:43'),(599,87,18,1,0.2,'SNARE',1,1,'G8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(601,87,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(602,87,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 08:37:43','2018-01-05 09:32:21'),(603,87,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(604,87,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(605,87,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(606,87,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 08:37:43','2018-01-05 09:32:21'),(607,87,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(608,87,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 08:37:44','2018-01-05 08:37:44'),(609,87,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 08:37:44','2018-01-05 08:37:44'),(610,87,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 08:37:44','2018-01-05 09:32:21'),(611,87,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 08:37:44','2018-01-05 09:32:21'),(612,87,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 08:37:44','2018-01-05 08:37:44'),(613,87,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(614,87,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(615,87,18,0.3,1,'KICKLONG',2.5,0.5,'C2','2018-01-05 08:37:44','2018-01-05 08:37:44'),(616,87,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(618,87,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(621,87,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 09:32:21'),(622,87,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(623,87,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 08:37:44','2018-01-05 09:32:21'),(624,87,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(625,87,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(626,87,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 08:37:44','2018-01-05 08:37:44'),(627,87,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 08:37:44','2018-01-07 00:38:37'),(628,87,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(629,88,23,1,1,'KICKLONG',0,1,'C2','2018-01-05 08:38:44','2018-01-05 08:38:44'),(630,88,20,0.1,0.6,'CONGA',0,1,'F5','2018-01-05 08:38:44','2018-01-05 08:38:44'),(631,88,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 08:38:44','2018-01-05 08:38:44'),(632,88,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 08:38:44','2018-01-05 09:32:21'),(633,88,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 08:38:44','2018-01-05 08:38:44'),(634,88,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 08:38:44','2018-01-05 08:38:44'),(635,88,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 08:38:44','2018-01-05 08:38:44'),(636,88,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 08:38:44','2018-01-07 00:38:37'),(637,88,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 08:38:44','2018-01-05 09:32:21'),(638,88,18,1,0.2,'SNARE',1,1,'G8','2018-01-05 08:38:44','2018-01-05 08:38:44'),(640,88,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(641,88,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(642,88,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 08:38:45','2018-01-05 09:32:21'),(643,88,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(644,88,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(645,88,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(646,88,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 08:38:45','2018-01-05 09:32:21'),(647,88,23,0.8,1,'KICK',1.75,1,'C2','2018-01-05 08:38:45','2018-01-05 10:06:35'),(648,88,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 08:38:45','2018-01-05 08:38:45'),(649,88,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(650,88,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 08:38:45','2018-01-05 09:32:21'),(651,88,18,0.1,1,'SNARE',2.25,0.2,'F#2','2018-01-05 08:38:45','2018-01-05 10:07:09'),(652,88,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(654,88,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(656,88,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(657,88,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 08:38:45','2018-01-07 00:38:37'),(658,88,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(659,88,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(661,88,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 09:32:21'),(662,88,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(663,88,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 08:38:45','2018-01-05 09:32:21'),(664,88,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(665,88,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(666,88,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 08:38:45','2018-01-05 08:38:45'),(667,88,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 08:38:45','2018-01-07 00:38:37'),(668,88,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(669,88,18,0.5,0.1,'SNARE',2.5,1,'G8','2018-01-05 08:39:07','2018-01-05 10:07:25'),(724,91,20,0.1,0.6,'CONGA',0,1,'F5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(725,91,23,1,1,'KICKLONG',0,1,'C2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(726,91,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(727,91,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(728,91,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 09:43:58','2018-01-05 09:43:58'),(729,91,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(730,91,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(731,91,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(732,91,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(733,91,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(734,91,18,1,0.2,'SNARE',1,1,'G8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(736,91,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(737,91,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(738,91,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(739,91,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(740,91,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(741,91,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(742,91,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(743,91,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(744,91,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(745,91,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(746,91,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(747,91,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(748,91,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(749,91,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(750,91,18,0.3,1,'KICKLONG',2.5,0.5,'C2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(751,91,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(752,91,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(753,91,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(754,91,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(756,91,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(757,91,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(758,91,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(759,91,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(760,91,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(761,91,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 09:43:58','2018-01-05 09:43:58'),(762,91,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(763,91,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(764,91,20,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 09:47:17','2018-01-06 23:29:10'),(765,91,19,0.125,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 09:57:22','2018-01-06 23:29:10'),(766,91,24,0.0625,0,'CYMBALCRASH',3,4,'F5','2018-01-05 09:57:47','2018-01-06 23:29:10'),(768,92,20,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 10:04:13','2018-01-06 23:29:10'),(769,92,20,1,1,'KICKLONG',1,4,'C5','2018-01-05 10:04:13','2018-01-05 15:51:19'),(770,92,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(771,92,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(772,92,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 10:04:13','2018-01-05 10:04:13'),(773,92,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 10:04:13','2018-01-07 00:38:37'),(774,92,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(775,92,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(776,92,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(777,92,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(780,92,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 10:04:13','2018-01-05 10:04:13'),(781,92,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(782,92,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(783,92,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(784,92,19,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 10:04:13','2018-01-06 23:29:10'),(785,92,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(786,92,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 10:04:13','2018-01-05 10:04:13'),(787,92,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(788,92,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 10:04:14','2018-01-05 10:04:14'),(789,92,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 10:04:14','2018-01-05 10:04:14'),(790,92,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(791,92,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 10:04:14','2018-01-05 10:04:14'),(792,92,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(793,92,18,0.3,1,'KICKLONG',2.5,0.5,'C2','2018-01-05 10:04:14','2018-01-05 10:04:14'),(794,92,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(795,92,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 10:04:14','2018-01-05 10:04:14'),(796,92,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(797,92,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 10:04:14','2018-01-07 00:38:37'),(798,92,24,0.03,0,'CYMBALCRASH',3,4,'F5','2018-01-05 10:04:14','2018-01-06 23:29:10'),(800,92,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(801,92,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(802,92,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(803,92,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(804,92,20,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 10:04:14','2018-01-05 10:04:14'),(805,92,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(806,92,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(807,92,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 10:04:14','2018-01-05 10:04:14'),(808,92,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 10:04:14','2018-01-07 00:38:37'),(809,92,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(1048,99,37,1,1,'KICKLONG',0,4,'C5','2018-01-05 14:54:11','2018-01-07 02:14:01'),(1050,99,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1052,99,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1053,100,37,1,1,'KICKLONG',0,4,'C5','2018-01-05 14:54:11','2018-01-07 02:13:42'),(1054,102,37,1,1,'KICKLONG',0,4,'C5','2018-01-05 14:54:11','2018-01-07 02:14:39'),(1056,99,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1057,100,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1058,101,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1060,99,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1062,100,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1063,102,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1064,101,37,1,1,'KICKLONG',0,4,'c5','2018-01-05 14:54:11','2018-01-07 02:14:16'),(1065,99,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1066,103,37,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1067,100,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1068,102,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1069,101,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1070,99,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1071,103,37,1,1,'KICKLONG',0,4,'C5','2018-01-05 14:54:11','2018-01-07 02:14:51'),(1072,100,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1073,102,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1074,101,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1075,99,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1076,103,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1077,100,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1078,104,37,1,1,'KICKLONG',0,4,'C5','2018-01-05 14:54:11','2018-01-07 02:15:06'),(1079,102,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1080,101,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1081,103,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1082,99,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1083,100,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1084,102,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1085,101,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1086,103,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1087,104,37,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1089,100,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1090,102,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1091,101,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1092,103,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1095,104,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1096,102,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1097,101,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1098,103,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1099,99,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1101,102,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1103,103,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1104,99,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1105,104,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1106,100,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1109,103,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1110,99,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1111,100,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1112,104,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1113,101,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1115,103,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1116,99,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1117,100,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1118,101,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1119,102,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1120,99,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1122,100,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1123,104,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1126,101,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1127,102,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1128,100,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1129,104,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1130,99,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1131,103,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1132,101,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1133,102,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1134,100,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1136,103,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1137,104,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1139,102,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1140,101,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1141,99,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1142,103,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1144,102,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1145,101,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1146,104,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1147,99,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1148,103,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1152,103,36,0.125,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1154,101,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1155,104,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1156,100,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1158,103,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1160,101,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1161,100,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1162,102,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1163,104,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1164,99,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1166,100,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1167,101,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1168,102,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1169,99,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1170,103,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1171,100,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1172,104,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1173,102,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1174,101,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1175,103,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1180,104,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1181,103,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1182,99,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1186,103,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1187,104,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1188,99,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1189,100,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1190,101,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1192,99,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1194,100,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1195,101,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1196,103,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1197,104,36,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1198,102,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1203,102,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1204,104,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1207,101,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1208,103,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1209,102,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1210,99,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1211,100,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1212,101,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1215,102,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1216,99,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1217,100,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1218,101,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1219,103,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1220,102,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1221,99,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1222,104,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1223,100,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1224,101,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1225,103,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1227,99,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1228,100,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1229,101,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1230,103,39,0.0625,0,'CYMBALCRASH',3,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1232,104,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1233,99,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1234,100,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1235,101,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1236,102,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1238,99,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1239,104,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1240,102,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1241,100,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1242,101,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1244,99,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1245,102,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1246,104,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1247,100,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1248,103,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1249,99,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1250,101,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1251,100,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1252,102,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1253,103,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1254,101,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1256,102,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1257,103,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1258,101,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1259,104,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1260,102,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1261,103,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1262,102,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1263,103,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1265,102,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1266,103,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1267,104,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1268,103,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1269,103,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1271,103,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1272,104,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1273,104,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1275,104,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1277,104,39,0.03,0,'CYMBALCRASH',3,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1278,104,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1279,104,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1280,104,37,0.3,0.6,'CONGAHIGH',3.25,0.6,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1281,104,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1282,104,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1283,104,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1284,104,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1285,104,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1286,99,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:09:36','2018-01-07 02:09:36'),(1287,100,37,0.4,0,'SNARE',2,1,'g5','2018-01-07 02:10:12','2018-01-07 02:13:24'),(1288,101,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:10:51','2018-01-07 02:10:51'),(1289,101,37,0.4,0,'SNARE',3.5,1,'g5','2018-01-07 02:11:06','2018-01-07 02:11:06'),(1290,102,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:11:31','2018-01-07 02:11:31'),(1291,103,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:12:11','2018-01-07 02:12:11'),(1292,104,37,0.4,0,'SNARE',2,1,'g5','2018-01-07 02:12:55','2018-01-07 02:13:04');
/*!40000 ALTER TABLE `pattern_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'ebroot','2017-02-04 17:36:22',142,1),(2,'2','account','SQL','V2__account.sql',-728725086,'ebroot','2017-02-04 17:36:23',117,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'ebroot','2017-02-04 17:36:23',54,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'ebroot','2017-02-04 17:36:23',387,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'ebroot','2017-02-04 17:36:23',226,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'ebroot','2017-02-04 17:36:24',525,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'ebroot','2017-02-04 17:36:24',162,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'ebroot','2017-02-04 17:36:24',64,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'ebroot','2017-02-04 17:36:24',51,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'ebroot','2017-02-04 17:36:24',36,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'ebroot','2017-02-04 17:36:24',13,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'ebroot','2017-02-04 17:36:24',48,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'ebroot','2017-02-04 17:36:25',54,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'ebroot','2017-02-04 17:36:25',54,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'ebroot','2017-02-04 17:36:25',48,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'ebroot','2017-02-04 17:36:25',89,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'ebroot','2017-02-04 17:36:25',130,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'ebroot','2017-02-04 17:36:25',382,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'ebroot','2017-02-04 17:36:26',241,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'ebroot','2017-02-04 17:36:26',143,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'ebroot','2017-02-05 23:06:15',315,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'ebroot','2017-02-07 03:21:14',29,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'ebroot','2017-02-07 03:21:14',125,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'ebroot','2017-02-10 00:03:21',205,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'ebroot','2017-02-10 00:03:21',107,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'ebroot','2017-02-10 00:03:25',3684,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'ebroot','2017-02-13 19:04:58',239,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'ebroot','2017-02-14 04:55:49',175,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'ebroot','2017-02-14 04:55:50',126,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'ebroot','2017-02-14 04:55:50',139,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'ebroot','2017-02-19 22:29:51',207,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'ebroot','2017-02-19 22:29:51',29,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'ebroot','2017-04-21 16:24:11',40,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'ebroot','2017-04-28 14:57:19',58,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'ebroot','2017-04-28 14:57:19',52,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'ebroot','2017-04-28 14:57:19',53,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'ebroot','2017-04-28 14:57:19',78,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'ebroot','2017-05-01 19:09:45',95,1),(39,'40','ALTER choice MODIFY COLUMN phase offset ULONG','SQL','V40__ALTER_choice_MODIFY_COLUMN_phase_offset_ULONG.sql',-240451169,'ebroot','2017-05-18 00:34:09',63,1),(40,'41','CREATE TABLE link meme','SQL','V41__CREATE_TABLE_link_meme.sql',-18883080,'ebroot','2017-05-18 00:34:09',51,1),(41,'42','ALTER phase link INT total','SQL','V42__ALTER_phase_link_INT_total.sql',-1400879099,'ebroot','2017-05-18 00:34:10',122,1),(42,'43','CREATE TABLE link message','SQL','V43__CREATE_TABLE_link_message.sql',1616909549,'ebroot','2017-05-18 00:34:10',46,1),(43,'44','ALTER pick BELONGS TO arrangement DROP morph point','SQL','V44__ALTER_pick_BELONGS_TO_arrangement_DROP_morph_point.sql',449955118,'ebroot','2017-05-26 00:58:12',563,1),(44,'45','ALTER link ADD COLUMN waveform key','SQL','V45__ALTER_link_ADD_COLUMN_waveform_key.sql',-98370,'ebroot','2017-06-01 16:53:07',811,1),(45,'46','ALTER audio ADD COLUMN state','SQL','V46__ALTER_audio_ADD_COLUMN_state.sql',-1300058820,'ebroot','2017-06-04 21:28:24',161,1),(46,'47','ALTER chain ADD COLUMN embed key','SQL','V47__ALTER_chain_ADD_COLUMN_embed_key.sql',317233573,'ebroot','2017-10-15 09:45:02',903,1),(47,'48','CREATE TABLE platform message','SQL','V48__CREATE_TABLE_platform_message.sql',-1332226532,'ebroot','2017-12-02 07:28:17',114,1),(48,'49','CREATE pattern DEPRECATES idea','SQL','V49__CREATE_pattern_DEPRECATES_idea.sql',517513730,'ebroot','2017-12-07 05:37:18',3380,1),(49,'50','REFACTOR voice BELONGS TO pattern','SQL','V50__REFACTOR_voice_BELONGS_TO_pattern.sql',1202195806,'ebroot','2018-01-03 21:36:08',712,1),(50,'51','DROP TABLE pick','SQL','V51__DROP_TABLE_pick.sql',-319463966,'ebroot','2018-01-04 03:41:36',849,1),(51,'52','ALTER phase ADD COLUMN type','SQL','V52__ALTER_phase_ADD_COLUMN_type.sql',-95957482,'ebroot','2018-01-05 07:32:08',602,1),(52,'53','ALTER chord MODIFY COLUMN position INTEGER','SQL','V53__ALTER_chord_MODIFY_COLUMN_position_INTEGER.sql',523400926,'ebroot','2018-01-10 21:53:43',4877,1),(53,'54','RENAME voice event TO phase event','SQL','V54__RENAME_voice_event_TO_phase_event.sql',-370585949,'ebroot','2018-01-17 06:06:10',56,1),(54,'55','ALTER pattern phase ADD COLUMN state','SQL','V55__ALTER_pattern_phase_ADD_COLUMN_state.sql',-1299872216,'ebroot','2018-02-03 00:56:45',460,1),(55,'56','ALTER chord MODIFY COLUMN position FLOAT','SQL','V56__ALTER_chord_MODIFY_COLUMN_position_FLOAT.sql',-894225407,'ebroot','2018-02-06 21:11:43',15080,1),(56,'57','REFACTORING chain segment sequence pattern','SQL','V57__REFACTORING_chain_segment_sequence_pattern.sql',-1235024870,'root','2018-08-30 15:48:36',4256,1),(57,'58','ALTER pattern ADD COLUMNS meter','SQL','V58__ALTER_pattern_ADD_COLUMNS_meter.sql',1342735981,'root','2018-08-30 15:48:36',389,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Charney Kaye','charneykaye@gmail.com','https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'Chris Luken','christopher.luken@gmail.com','https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'David Cole','davecolemusic@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-03-08 02:26:51','2017-03-08 02:26:51'),(4,'Shannon Holloway','shannon.holloway@gmail.com','https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50','2017-03-08 18:14:53','2017-03-08 18:14:53'),(5,'Lev Kaye','lev@kaye.com','https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50','2017-03-09 23:47:12','2017-03-09 23:47:12'),(6,'Justin Knowlden (gus)','gus@gusg.us','https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50','2017-04-14 20:41:41','2017-04-14 20:41:41'),(7,'dave farkas','sakrafd@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-14 20:42:36','2017-04-14 20:42:36'),(8,'Aji Putra','aji.perdana.putra@gmail.com','https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50','2017-04-21 17:33:25','2017-04-21 17:33:25'),(9,'live espn789','scoreplace@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-21 19:13:22','2017-04-21 19:13:22'),(10,'Dmitry Solomadin','dmitry.solomadin@gmail.com','https://lh6.googleusercontent.com/-Ns78xq2VzKk/AAAAAAAAAAI/AAAAAAAAE44/ZOuBZnZqYeU/photo.jpg?sz=50','2017-05-03 21:09:33','2017-05-03 21:09:33'),(11,'Michael Prolagaev','prolagaev@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-05-04 16:13:06','2017-05-04 16:13:06'),(12,'Charney Kaye','nick.c.kaye@gmail.com','https://lh5.googleusercontent.com/-_oXIqxZhTkk/AAAAAAAAAAI/AAAAAAAAUks/dg9oNRfPFco/photo.jpg?sz=50','2017-05-18 17:37:32','2017-05-18 17:37:32'),(13,'Charney Kaye','charney@outrightmental.com','https://lh5.googleusercontent.com/-3yrpEvNKIvE/AAAAAAAAAAI/AAAAAAAAASc/Gls7ZJcVqCk/photo.jpg?sz=50','2017-06-19 20:39:46','2017-06-19 20:39:46'),(14,'Philip Z. Kimball','pzkimball@pzklaw.com','https://lh4.googleusercontent.com/-xnsM2SBKwaE/AAAAAAAAAAI/AAAAAAAAABs/uJouNj6fMgw/photo.jpg?sz=50','2017-06-26 13:56:57','2017-06-26 13:56:57'),(15,'Janae\' Leonard','janaeleo55@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-06-28 09:30:40','2017-06-28 09:30:40'),(16,'yuan liu','minamotoclan@gmail.com','https://lh6.googleusercontent.com/-4orhpHPwHN4/AAAAAAAAAAI/AAAAAAAAFGc/HYueBarZIwA/photo.jpg?sz=50','2017-07-03 03:16:24','2017-07-03 03:16:24'),(17,'Nick Podgurski','nickpodgurski@gmail.com','https://lh5.googleusercontent.com/-Cly5aKHLBMc/AAAAAAAAAAI/AAAAAAAAAYQ/wu8BxP-Zwxk/photo.jpg?sz=50','2017-07-04 03:59:02','2017-07-04 03:59:02'),(18,'Brian Sweeny','brian@vibesinternational.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-05 16:01:53','2017-07-05 16:01:53'),(19,'John Bennett','johnalsobennett@gmail.com','https://lh6.googleusercontent.com/-kFMmioNSrEM/AAAAAAAAAAI/AAAAAAAABfg/SfT2vo__XgI/photo.jpg?sz=50','2017-07-06 15:08:32','2017-07-06 15:08:32'),(20,'Aditi Hebbar','adhebbar@gmail.com','https://lh4.googleusercontent.com/-gUnZUky1WtE/AAAAAAAAAAI/AAAAAAAAEJ8/sFumIpFdaUA/photo.jpg?sz=50','2017-07-07 08:42:46','2017-07-07 08:42:46'),(21,'HANKYOL CHO','hankyolcho@mail.adelphi.edu','https://lh3.googleusercontent.com/-skrgmZw2fas/AAAAAAAAAAI/AAAAAAAAAAA/iwMwVr_CL2U/photo.jpg?sz=50','2017-07-10 14:10:03','2017-07-10 14:10:03'),(22,'Charles Frantz','charlesfrantz@gmail.com','https://lh4.googleusercontent.com/-WtgVMTchHkY/AAAAAAAAAAI/AAAAAAAAAMU/4hX0mxVuIBE/photo.jpg?sz=50','2017-07-13 14:28:39','2017-07-13 14:28:39'),(23,'Alice Gamarnik','ajgamarnik@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-14 16:25:46','2017-07-14 16:25:46'),(24,'liu xin','xinliu2530@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-17 18:46:18','2017-07-17 18:46:18'),(25,'Outright Mental','outrightmental@gmail.com','https://lh5.googleusercontent.com/-2HcQgfYoQRU/AAAAAAAAAAI/AAAAAAAAANE/-ttDusZjeuk/photo.jpg?sz=50','2017-07-30 16:26:49','2017-07-30 16:26:49'),(26,'Joey Lorjuste','joeylorjuste@gmail.com','https://lh4.googleusercontent.com/-WPQgkyb-M5A/AAAAAAAAAAI/AAAAAAAAH-Q/Lf9IG0JJl5c/photo.jpg?sz=50','2017-08-20 19:25:12','2017-08-20 19:25:12'),(27,'Mark Stewart','mark.si.stewart@gmail.com','https://lh3.googleusercontent.com/-PtMRcK_-Bkg/AAAAAAAAAAI/AAAAAAAAASs/YlN0XjZSvdg/photo.jpg?sz=50','2017-08-25 19:30:40','2017-08-25 19:30:40'),(28,'Rosalind Kaye','rckaye@kaye.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-16 00:11:49','2017-10-16 00:11:49'),(29,'Matthew DellaRatta','mdellaratta8@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-17 00:00:36','2017-10-17 00:00:36'),(30,'Justice Whitaker','justice512@gmail.com','https://lh5.googleusercontent.com/-Y9sCwQKldqA/AAAAAAAAAAI/AAAAAAAAADE/3wU9xJLYRG0/photo.jpg?sz=50','2017-12-08 20:45:40','2017-12-08 20:45:40'),(31,'Ed Carney','ed@steirmancpas.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-12-13 15:13:49','2017-12-13 15:13:49'),(32,'Tamil Selvan','prtamil@gmail.com','https://lh6.googleusercontent.com/-iVWQ0iJwSvY/AAAAAAAAAAI/AAAAAAAAAJo/KlOXVs2XwFI/photo.jpg?sz=50','2018-02-04 08:50:11','2018-02-04 08:50:11'),(33,'Riyadh Abdullatif','coldmo@gmail.com','https://lh6.googleusercontent.com/-NQk0LpgjTc0/AAAAAAAAAAI/AAAAAAAAAGk/SCEchWKOh7g/photo.jpg?sz=50','2018-02-26 21:36:44','2018-02-26 21:36:44'),(34,'Ken Kaye','ken@kaye.com','https://lh3.googleusercontent.com/-r0rl7N0eE7g/AAAAAAAAAAI/AAAAAAAAAEc/IC1Dir_2XjE/photo.jpg?sz=50','2018-05-15 12:24:58','2018-05-15 12:24:58'),(35,'Eden Zhong','hydrosulfate@gmail.com','https://lh3.googleusercontent.com/-Ty-LN9tk8TQ/AAAAAAAAAAI/AAAAAAAADi4/J1bPsII4IFY/photo.jpg?sz=50','2018-06-04 20:57:29','2018-06-04 20:57:29');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'user',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'admin',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'user',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,'artist',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(5,'artist',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(8,'user',4,'2017-03-08 18:14:53','2017-03-08 18:14:53'),(9,'artist',4,'2017-03-09 17:48:55','2017-03-09 17:48:55'),(10,'user',5,'2017-03-09 23:47:12','2017-03-09 23:47:12'),(11,'artist',5,'2017-03-10 05:39:23','2017-03-10 05:39:23'),(16,'user',8,'2017-04-21 17:33:25','2017-04-21 17:33:25'),(17,'user',9,'2017-04-21 19:13:22','2017-04-21 19:13:22'),(22,'user',12,'2017-05-18 17:37:32','2017-05-18 17:37:32'),(23,'artist',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(24,'engineer',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(25,'user',13,'2017-06-19 20:39:46','2017-06-19 20:39:46'),(26,'user',14,'2017-06-26 13:56:57','2017-06-26 13:56:57'),(27,'artist',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(28,'engineer',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(29,'user',15,'2017-06-28 09:30:40','2017-06-28 09:30:40'),(30,'user',16,'2017-07-03 03:16:24','2017-07-03 03:16:24'),(31,'user',17,'2017-07-04 03:59:02','2017-07-04 03:59:02'),(32,'user',18,'2017-07-05 16:01:53','2017-07-05 16:01:53'),(33,'user',19,'2017-07-06 15:08:32','2017-07-06 15:08:32'),(34,'user',20,'2017-07-07 08:42:46','2017-07-07 08:42:46'),(35,'banned',10,'2017-07-07 20:53:49','2017-07-07 20:53:49'),(36,'banned',11,'2017-07-07 20:53:55','2017-07-07 20:53:55'),(37,'user',21,'2017-07-10 14:10:03','2017-07-10 14:10:03'),(38,'user',22,'2017-07-13 14:28:39','2017-07-13 14:28:39'),(39,'artist',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(40,'engineer',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(43,'user',23,'2017-07-14 16:25:46','2017-07-14 16:25:46'),(44,'user',24,'2017-07-17 18:46:18','2017-07-17 18:46:18'),(45,'artist',24,'2017-07-17 18:46:58','2017-07-17 18:46:58'),(47,'artist',23,'2017-07-17 18:47:04','2017-07-17 18:47:04'),(49,'user',25,'2017-07-30 16:26:49','2017-07-30 16:26:49'),(50,'artist',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(51,'engineer',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(52,'artist',13,'2017-07-30 16:27:43','2017-07-30 16:27:43'),(53,'user',26,'2017-08-20 19:25:12','2017-08-20 19:25:12'),(54,'user',27,'2017-08-25 19:30:40','2017-08-25 19:30:40'),(55,'artist',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(56,'engineer',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(57,'user',28,'2017-10-16 00:11:49','2017-10-16 00:11:49'),(58,'user',29,'2017-10-17 00:00:36','2017-10-17 00:00:36'),(59,'user',30,'2017-12-08 20:45:40','2017-12-08 20:45:40'),(60,'artist',30,'2017-12-08 20:47:55','2017-12-08 20:47:55'),(61,'engineer',1,'2017-12-12 06:57:46','2017-12-12 06:57:46'),(62,'user',31,'2017-12-13 15:13:49','2017-12-13 15:13:49'),(63,'User',3,'2018-01-05 16:56:10','2018-01-05 16:56:10'),(64,'Artist',3,'2018-01-05 16:56:14','2018-01-05 16:56:14'),(65,'Engineer',3,'2018-01-05 16:56:18','2018-01-05 16:56:18'),(67,'Admin',6,'2018-01-07 21:34:23','2018-01-07 21:34:23'),(68,'Admin',7,'2018-01-07 21:34:52','2018-01-07 21:34:52'),(69,'Engineer',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(70,'Artist',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(71,'User',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(72,'Engineer',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(73,'Artist',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(74,'User',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(75,'User',32,'2018-02-04 08:50:11','2018-02-04 08:50:11'),(76,'User',33,'2018-02-26 21:36:44','2018-02-26 21:36:44'),(77,'Admin',27,'2018-03-22 19:22:02','2018-03-22 19:22:02'),(78,'User',34,'2018-05-15 12:24:58','2018-05-15 12:24:58'),(79,'User',35,'2018-06-04 20:57:29','2018-06-04 20:57:29');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice`
--

LOCK TABLES `voice` WRITE;
/*!40000 ALTER TABLE `voice` DISABLE KEYS */;
INSERT INTO `voice` VALUES (4,6,'percussive','BoomClap','2017-04-23 23:45:07','2018-01-03 21:36:07'),(5,6,'percussive','Locomotion','2017-06-03 00:04:07','2018-01-03 21:36:07'),(8,6,'percussive','Jangle','2017-06-11 19:50:10','2018-01-03 21:36:07'),(10,6,'percussive','Vocal','2017-06-23 23:43:10','2018-01-03 21:36:07'),(11,6,'percussive','Vocal Echo','2017-06-24 01:29:49','2018-01-03 21:36:07'),(12,6,'Percussive','2x4 Stomp','2017-12-07 03:43:08','2018-01-03 21:36:07'),(17,6,'Percussive','Clave','2017-12-21 00:33:48','2018-01-03 21:36:07'),(18,29,'Percussive','BoomClap','2017-12-22 06:43:19','2018-01-03 21:36:07'),(19,29,'Percussive','Locomotion','2017-12-22 06:43:19','2018-01-03 21:36:07'),(20,29,'Percussive','Jangle','2017-12-22 06:43:20','2018-01-03 21:36:07'),(23,29,'Percussive','Stomp','2017-12-22 06:43:20','2018-03-16 00:46:18'),(24,29,'Percussive','Clave','2017-12-22 06:43:20','2018-01-03 21:36:07'),(36,34,'Percussive','Locomotion','2018-01-05 14:54:11','2018-01-05 14:54:11'),(37,34,'Percussive','Jangle','2018-01-05 14:54:11','2018-01-05 14:54:11'),(39,34,'Percussive','Clave','2018-01-05 14:54:11','2018-01-05 14:54:11');
/*!40000 ALTER TABLE `voice` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-08-30  8:54:00




















#-------------
USE `xj_test`;

-- MySQL dump 10.13  Distrib 5.7.23, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: xj_test
-- ------------------------------------------------------
-- Server version	5.6.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'root','2018-08-30 15:48:58',154,1),(2,'2','account','SQL','V2__account.sql',-728725086,'root','2018-08-30 15:48:58',157,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'root','2018-08-30 15:48:58',79,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'root','2018-08-30 15:48:59',642,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'root','2018-08-30 15:49:00',396,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'root','2018-08-30 15:49:00',331,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'root','2018-08-30 15:49:00',373,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'root','2018-08-30 15:49:00',35,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'root','2018-08-30 15:49:00',85,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'root','2018-08-30 15:49:01',91,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'root','2018-08-30 15:49:01',14,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'root','2018-08-30 15:49:01',31,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'root','2018-08-30 15:49:01',101,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'root','2018-08-30 15:49:01',99,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'root','2018-08-30 15:49:01',133,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'root','2018-08-30 15:49:01',146,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'root','2018-08-30 15:49:02',363,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'root','2018-08-30 15:49:03',992,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'root','2018-08-30 15:49:03',661,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'root','2018-08-30 15:49:04',362,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'root','2018-08-30 15:49:04',477,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'root','2018-08-30 15:49:04',174,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'root','2018-08-30 15:49:05',325,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'root','2018-08-30 15:49:05',491,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'root','2018-08-30 15:49:06',332,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'root','2018-08-30 15:49:13',6857,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'root','2018-08-30 15:49:13',660,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'root','2018-08-30 15:49:14',419,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'root','2018-08-30 15:49:14',358,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'root','2018-08-30 15:49:14',367,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'root','2018-08-30 15:49:15',486,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'root','2018-08-30 15:49:15',56,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'root','2018-08-30 15:49:15',19,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'root','2018-08-30 15:49:15',79,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'root','2018-08-30 15:49:15',101,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'root','2018-08-30 15:49:15',94,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'root','2018-08-30 15:49:16',126,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'root','2018-08-30 15:49:16',115,1),(39,'40','ALTER choice MODIFY COLUMN phase offset ULONG','SQL','V40__ALTER_choice_MODIFY_COLUMN_phase_offset_ULONG.sql',-240451169,'root','2018-08-30 15:49:16',171,1),(40,'41','CREATE TABLE link meme','SQL','V41__CREATE_TABLE_link_meme.sql',-18883080,'root','2018-08-30 15:49:16',89,1),(41,'42','ALTER phase link INT total','SQL','V42__ALTER_phase_link_INT_total.sql',-1400879099,'root','2018-08-30 15:49:16',375,1),(42,'43','CREATE TABLE link message','SQL','V43__CREATE_TABLE_link_message.sql',1616909549,'root','2018-08-30 15:49:16',83,1),(43,'44','ALTER pick BELONGS TO arrangement DROP morph point','SQL','V44__ALTER_pick_BELONGS_TO_arrangement_DROP_morph_point.sql',449955118,'root','2018-08-30 15:49:17',194,1),(44,'45','ALTER link ADD COLUMN waveform key','SQL','V45__ALTER_link_ADD_COLUMN_waveform_key.sql',-98370,'root','2018-08-30 15:49:17',127,1),(45,'46','ALTER audio ADD COLUMN state','SQL','V46__ALTER_audio_ADD_COLUMN_state.sql',-1300058820,'root','2018-08-30 15:49:17',240,1),(46,'47','ALTER chain ADD COLUMN embed key','SQL','V47__ALTER_chain_ADD_COLUMN_embed_key.sql',317233573,'root','2018-08-30 15:49:17',173,1),(47,'48','CREATE TABLE platform message','SQL','V48__CREATE_TABLE_platform_message.sql',-1332226532,'root','2018-08-30 15:49:17',76,1),(48,'49','CREATE pattern DEPRECATES idea','SQL','V49__CREATE_pattern_DEPRECATES_idea.sql',517513730,'root','2018-08-30 15:49:19',1550,1),(49,'50','REFACTOR voice BELONGS TO pattern','SQL','V50__REFACTOR_voice_BELONGS_TO_pattern.sql',1202195806,'root','2018-08-30 15:49:20',1078,1),(50,'51','DROP TABLE pick','SQL','V51__DROP_TABLE_pick.sql',-319463966,'root','2018-08-30 15:49:20',80,1),(51,'52','ALTER phase ADD COLUMN type','SQL','V52__ALTER_phase_ADD_COLUMN_type.sql',-95957482,'root','2018-08-30 15:49:20',243,1),(52,'53','ALTER chord MODIFY COLUMN position INTEGER','SQL','V53__ALTER_chord_MODIFY_COLUMN_position_INTEGER.sql',523400926,'root','2018-08-30 15:49:21',488,1),(53,'54','RENAME voice event TO phase event','SQL','V54__RENAME_voice_event_TO_phase_event.sql',-370585949,'root','2018-08-30 15:49:21',35,1),(54,'55','ALTER pattern phase ADD COLUMN state','SQL','V55__ALTER_pattern_phase_ADD_COLUMN_state.sql',-1299872216,'root','2018-08-30 15:49:22',522,1),(55,'56','ALTER chord MODIFY COLUMN position FLOAT','SQL','V56__ALTER_chord_MODIFY_COLUMN_position_FLOAT.sql',-894225407,'root','2018-08-30 15:49:22',499,1),(56,'57','REFACTORING chain segment sequence pattern','SQL','V57__REFACTORING_chain_segment_sequence_pattern.sql',-1235024870,'root','2018-08-30 15:49:26',4047,1),(57,'58','ALTER pattern ADD COLUMNS meter','SQL','V58__ALTER_pattern_ADD_COLUMNS_meter.sql',1342735981,'root','2018-08-30 15:49:27',357,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-08-30  8:54:00
