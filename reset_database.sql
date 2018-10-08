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




















# Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.





















#-------------
USE `xj`;

-- MySQL dump 10.13  Distrib 5.5.60, for Linux (x86_64)
--
-- Host: xj-prod.c4xei3cvxtkt.us-east-1.rds.amazonaws.com    Database: ebdb
-- ------------------------------------------------------
-- Server version	5.7.16-log

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
INSERT INTO `audio` VALUES (2,3,'Kick','80454e35-5693-4b42-aa6a-218383a9f584-instrument-3-audio.wav',0,0.702,120,57.495,'2017-04-21 16:41:03','2017-07-26 22:08:30','Published'),(3,3,'Kick Long','ed1957b9-eea0-42f8-8493-b8874e1a6bf9-instrument-3-audio.wav',0,0.865,120,57.05,'2017-04-21 18:52:17','2017-07-26 22:08:30','Published'),(4,3,'Hihat Closed','0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14-instrument-3-audio.wav',0,0.053,120,6300,'2017-04-21 19:33:05','2017-07-26 22:08:30','Published'),(5,3,'Maracas','ffe4edd6-5b83-4ac9-8e69-156ddb06762f-instrument-3-audio.wav',0,0.026,120,190.086,'2017-04-21 19:38:16','2017-07-26 22:08:30','Published'),(6,3,'Snare','7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe-instrument-3-audio.wav',0,0.093,120,177.823,'2017-04-21 19:42:59','2017-07-26 22:08:30','Published'),(7,3,'Tom','a6bf0d86-6b45-4cf1-b404-2242095c7876-instrument-3-audio.wav',0,0.36,120,104.751,'2017-04-21 19:43:58','2017-07-26 22:08:30','Published'),(8,3,'Claves','aea2483c-7707-4100-aa86-b680668cd1a0-instrument-3-audio.wav',0,0.03,120,2594,'2017-04-23 23:59:47','2017-07-26 22:08:30','Published'),(9,3,'Conga','f772f19f-b51b-414e-9dc8-8ceb23faa779-instrument-3-audio.wav',0,0.26,120,213,'2017-04-24 00:03:32','2017-07-26 22:08:30','Published'),(10,3,'Conga High','c0975d3a-4f26-44b2-a9d3-800320bfa3e1-instrument-3-audio.wav',0,0.179,120,397.297,'2017-04-24 00:05:34','2017-07-26 22:08:30','Published'),(11,3,'Tom High','aea1351b-bb96-4487-8feb-ae8ad3e499ad-instrument-3-audio.wav',0,0.2,120,190.909,'2017-04-24 02:18:29','2017-07-26 22:08:30','Published'),(12,3,'Clap','ce0662a2-3f7e-425b-8105-fb639d395235-instrument-3-audio.wav',0,0.361,120,1102.5,'2017-04-24 02:21:04','2017-07-26 22:08:30','Published'),(13,3,'Cowbell','aaa877a8-0c89-4781-93f8-69c722285b2a-instrument-3-audio.wav',0,0.34,120,268.902,'2017-04-24 02:22:47','2017-07-26 22:08:30','Published'),(14,3,'Cymbal Crash','37a35a63-23e4-4ef6-a78e-db2577aa9a00-instrument-3-audio.wav',0,2.229,120,109.701,'2017-04-24 02:24:03','2017-07-26 22:08:30','Published'),(15,3,'Hihat Open','020ad575-af86-4fe2-a869-957d50d59ac4-instrument-3-audio.wav',0,0.598,120,7350,'2017-04-24 02:25:31','2017-07-26 22:08:30','Published'),(16,3,'Snare Rim','58fd7eae-b55e-4567-9c27-ead64b83488a-instrument-3-audio.wav',0,0.014,120,445.445,'2017-04-24 02:26:53','2017-07-26 22:08:30','Published'),(22,4,'Hihat Closed 9','0f28ef83-2213-4bbb-ae68-3eecc201ead3-instrument-4-audio.wav',0,0.849,120,648.529,'2017-06-15 22:43:24','2017-07-26 22:08:30','Published'),(23,4,'Hihat Closed 7','e15dc427-b556-4a72-bec8-6b59c6d8bbc8-instrument-4-audio.wav',0.003,0.962,120,8820,'2017-06-15 22:44:55','2017-07-26 22:08:30','Published'),(24,4,'Hihat Closed 8','cb1ffbff-c31d-4e06-9d84-649c1f257a24-instrument-4-audio.wav',0,0.905,120,648.529,'2017-06-15 22:45:52','2017-07-26 22:08:30','Published'),(26,4,'Snare Rim','7b2d94b3-c218-498b-906e-11c313054cd1-instrument-4-audio.wav',0,1.147,120,239.674,'2017-06-15 22:56:58','2017-07-26 22:08:30','Published'),(27,4,'Hihat Open 5','bf2c9ad8-ceb4-4c7e-98ae-a9c561680a1f-instrument-4-audio.wav',0.003,1.115,120,648.529,'2017-06-15 23:04:16','2017-07-26 22:08:30','Published'),(28,4,'Hihat Open 7','4c3c5673-e8f1-4452-ad8c-5466cce0492d-instrument-4-audio.wav',0,2,120,648.529,'2017-06-15 23:06:14','2017-07-26 22:08:30','Published'),(29,4,'Hihat Open 6','9a57a402-98e9-4ceb-86c2-ea60607b56d1-instrument-4-audio.wav',0,0.809,120,648.529,'2017-06-15 23:07:41','2017-07-26 22:08:30','Published'),(30,4,'Stick Side 7','ea042c27-551b-44c7-998b-1df185d319cf-instrument-4-audio.wav',0.003,0.159,120,436.634,'2017-06-15 23:11:51','2017-07-26 22:08:30','Published'),(31,4,'Stick Side 6','0d65a838-e76f-407d-a06b-6485d67ba44c-instrument-4-audio.wav',0,0.335,120,2321.05,'2017-06-15 23:13:00','2017-07-26 22:08:30','Published'),(32,4,'Stick Side 5','99f7dbea-c1fb-419e-ad44-c90804516aa3-instrument-4-audio.wav',0,0.248,120,1837.5,'2017-06-15 23:14:30','2017-07-26 22:08:30','Published'),(33,4,'Snare Rim 7','12e36076-5944-4101-a41b-b39136cf78a4-instrument-4-audio.wav',0,0.461,120,254.913,'2017-06-15 23:15:43','2017-07-26 22:08:30','Published'),(34,4,'Snare Rim 6','5a840f38-7623-442b-b9a9-a0ff1927c7a0-instrument-4-audio.wav',0,0.527,120,245,'2017-06-15 23:16:36','2017-07-26 22:08:30','Published'),(35,4,'Snare Rim 5','d404857a-6bf8-43c4-ad76-5259945d16fe-instrument-4-audio.wav',0,0.463,120,181.481,'2017-06-15 23:17:44','2017-07-26 22:08:30','Published'),(36,4,'Tom High','4888db8b-1c81-4178-8af5-332ae7067ca8-instrument-4-audio.wav',0.002,0.42,120,187.66,'2017-06-15 23:20:38','2017-07-26 22:08:30','Published'),(37,4,'Snare 3','d373a2f8-8c8f-4afa-b7e3-c21623d15f42-instrument-4-audio.wav',0.008,0.404,120,2450,'2017-06-15 23:21:50','2017-07-26 22:08:30','Published'),(38,4,'Tom','d5bcc3a5-d98f-434f-8fcb-987f1913a684-instrument-4-audio.wav',0.009,0.445,120,225,'2017-06-15 23:22:45','2017-07-26 22:08:30','Published'),(39,4,'Conga High','511f5a68-1eca-4ca3-9713-956a219d734c-instrument-4-audio.wav',0.002,0.425,120,187.66,'2017-06-15 23:24:15','2017-07-26 22:08:30','Published'),(40,4,'Conga','2059cab7-8052-46cf-8fd1-2930cfe5ce59-instrument-4-audio.wav',0.001,0.547,120,183.231,'2017-06-15 23:25:03','2017-07-26 22:08:30','Published'),(41,4,'Snare 5','cce1763b-fca3-49c5-9024-c665c1fea7f3-instrument-4-audio.wav',0.008,0.407,120,180.738,'2017-06-15 23:25:58','2017-07-26 22:08:30','Published'),(42,4,'Snare 4','511168e1-3291-4ec8-a6ac-652249206287-instrument-4-audio.wav',0.008,0.439,120,204.167,'2017-06-15 23:27:04','2017-07-26 22:08:30','Published'),(43,4,'Kick 7','2fd75fb8-b968-46ba-8c43-ac6ad2db9a80-instrument-4-audio.wav',0.008,0.537,120,43.534,'2017-06-16 00:20:54','2017-07-26 22:08:30','Published'),(44,4,'Kick 3','3a79549f-cf7b-4338-8756-f75b3fc5deaa-instrument-4-audio.wav',0.005,0.742,120,52.128,'2017-06-16 00:24:47','2017-07-26 22:08:30','Published'),(45,4,'Kick 3','c076a674-1626-4b22-bc07-a639ca90b363-instrument-4-audio.wav',0.01,0.677,120,56.178,'2017-06-16 00:27:07','2017-07-26 22:08:30','Published'),(48,4,'Tom Low 5','246190da-65fd-41a9-a943-2c8e3b763fa5-instrument-4-audio.wav',0,0.73,120,84.483,'2017-06-16 00:33:57','2017-07-26 22:08:30','Published'),(49,4,'Tom 5','bf45a337-c86a-4c44-9663-06093d3ca9ba-instrument-4-audio.wav',0,0.59,120,90.928,'2017-06-16 00:35:25','2017-07-26 22:08:30','Published'),(50,4,'Tom High 5','83294480-eef2-4171-8d69-8f16092557df-instrument-4-audio.wav',0.003,0.444,120,126,'2017-06-16 00:36:37','2017-07-26 22:08:30','Published'),(51,4,'Kick Long 2','b12bf5ff-ebec-47e3-9259-6cd0c9f57724-instrument-4-audio.wav',0.01,1.476,120,59.036,'2017-06-16 00:39:02','2017-07-26 22:08:30','Published'),(54,4,'Clap 1','27b08205-9921-4d48-bc54-ba4110fe238f-instrument-4-audio.wav',0,0.572,120,185.294,'2017-06-16 02:15:47','2017-07-26 22:08:30','Published'),(55,4,'Clap 2','81f55d83-39fe-4832-99bf-4e4f3af69496-instrument-4-audio.wav',0,0.684,120,188.462,'2017-06-16 02:17:11','2017-07-26 22:08:30','Published'),(56,3,'Kick 2','a731fc44-5ae0-4e9f-a728-edfe1895da4b-instrument-3-audio.wav',0,0.34,120,69.122,'2017-06-16 03:01:06','2017-07-26 22:08:30','Published'),(57,3,'Kick Long 2','84b1974c-02b0-406f-b78e-21414282986e-instrument-3-audio.wav',0,1.963,120,60.494,'2017-06-16 03:04:09','2017-07-26 22:08:30','Published'),(58,3,'Tom High 2','618bc8e5-f51f-4635-895c-5bd6522f8d8c-instrument-3-audio.wav',0.002,0.411,120,201.37,'2017-06-16 03:06:30','2017-07-26 22:08:30','Published'),(59,3,'Tom Low 2','014c8939-c9e7-4911-9620-9c4075a3b4a2-instrument-3-audio.wav',0,0.701,120,111.646,'2017-06-16 03:07:20','2017-07-26 22:08:30','Published'),(60,3,'Tom 2','3fcb76bf-6168-4aef-a160-facd1bb18071-instrument-3-audio.wav',0,0.488,120,149.492,'2017-06-16 03:09:50','2017-07-26 22:08:30','Published'),(61,3,'Clap 2','9a3e9e07-b1dd-44a5-9399-3b6c11bd72b1-instrument-3-audio.wav',0.002,0.356,120,1225,'2017-06-16 03:13:28','2017-07-26 22:08:30','Published'),(62,3,'Clap 3','f24484dd-b879-42c5-9c2a-71857555c319-instrument-3-audio.wav',0,0.734,120,980,'2017-06-16 03:14:41','2017-07-26 22:08:30','Published'),(63,3,'Maracas 2','f20dcce7-a936-446c-8692-c8caf37d8896-instrument-3-audio.wav',0.009,0.43,120,11025,'2017-06-16 03:17:11','2017-07-26 22:08:30','Published'),(64,4,'Cowbell','392a388d-8e32-46f9-ad57-b3bd29929262-instrument-4-audio.wav',0.002,0.298,120,525,'2017-06-16 03:20:04','2017-07-26 22:08:30','Published'),(65,4,'Cymbal Crash 1','378df92f-aec2-4a5c-9243-d08384971761-instrument-4-audio.wav',0.018,1.878,120,1297.06,'2017-06-16 03:21:46','2017-07-26 22:08:30','Published'),(66,4,'Cymbal Crash 2','b921f58d-1ce0-4c1e-82d0-08479c25bfff-instrument-4-audio.wav',0.01,3.241,120,469.149,'2017-06-16 03:24:03','2017-07-26 22:08:30','Published'),(67,4,'Cymbal Crash 3','484d5dc0-4627-477d-8de7-f4c30cc4f538-instrument-4-audio.wav',0.01,3.044,120,181.481,'2017-06-16 03:25:34','2017-07-26 22:08:30','Published'),(68,3,'Cymbal Crash 2','bb3e2a48-8f59-4ad0-a05f-30aca579524f-instrument-3-audio.wav',0,2,120,816.667,'2017-06-16 03:28:35','2017-07-26 22:08:30','Published'),(69,5,'Hihat Closed A_3','86d61872-a9bf-4b68-b4df-397be09bfe5c-instrument-5-audio.wav',0.007,1.051,120,3428.57,'2017-06-20 23:11:42','2017-07-27 17:35:01','Published'),(70,5,'Hihat Closed A_4','92f61e58-7225-48bb-91f3-b71fcf7aef5a-instrument-5-audio.wav',0,0.623,120,888.889,'2017-06-20 23:17:39','2017-07-27 17:35:07','Published'),(71,5,'Hihat Closed A_5','8a536dae-3727-488f-8895-a0b047620a38-instrument-5-audio.wav',0.001,0.537,120,888.889,'2017-06-20 23:19:00','2017-07-27 17:34:31','Published'),(72,5,'Hihat Closed A_6','e173c291-60d6-4f9a-a422-d2d8c99bd9b3-instrument-5-audio.wav',0.003,0.425,120,3428.57,'2017-06-20 23:35:06','2017-07-27 17:34:36','Published'),(73,5,'Hihat Closed A_7','de082694-4a02-48a4-92d1-83c2d2b7dd92-instrument-5-audio.wav',0.001,0.6,120,1263.16,'2017-06-20 23:36:34','2017-07-27 17:34:40','Published'),(74,5,'Hihat Closed A_8','7cbe09b2-5fe6-4d7a-b5fa-2f85624e91f5-instrument-5-audio.wav',0,0.73,120,1200,'2017-06-20 23:37:43','2017-07-27 17:34:43','Published'),(75,5,'Hihat Closed A_9','96df8da4-5be9-4a0f-a97b-5f8c0d28f161-instrument-5-audio.wav',0,0.432,120,1454.55,'2017-06-20 23:38:52','2017-07-27 17:34:48','Published'),(76,5,'Hihat Closed A_10','e4a06acb-c375-4e9b-a5ce-153b815fe6cb-instrument-5-audio.wav',0.002,0.307,120,3000,'2017-06-20 23:40:36','2017-07-27 17:34:58','Published'),(77,5,'Hihat Open F_1','4eb40925-8e37-4801-ba2e-cce991c97093-instrument-5-audio.wav',0,0.969,120,428.155,'2017-06-20 23:45:30','2017-07-27 17:34:01','Published'),(78,5,'Hihat Open F_2','13db8e43-4266-444a-9edd-c5a5cb2442b4-instrument-5-audio.wav',0,1.506,120,182.988,'2017-06-20 23:46:32','2017-07-27 17:34:05','Published'),(79,5,'Hihat Open F_3','3f0dbe3a-d11a-4e9f-a642-befe5747dd01-instrument-5-audio.wav',0,2.567,120,183.75,'2017-06-20 23:47:22','2017-07-27 17:34:08','Published'),(80,5,'Hihat Open F_4','57ff6b97-fedb-4e3f-b963-840ba8fd101b-instrument-5-audio.wav',0.035,2.617,120,416.038,'2017-06-20 23:48:41','2017-07-27 17:33:29','Published'),(81,5,'Hihat Open F_5','70c7404e-1f17-4a32-8f4a-ff28e7d5797c-instrument-5-audio.wav',0,2.734,120,420,'2017-06-20 23:49:48','2017-07-27 17:33:34','Published'),(82,5,'Hihat Open F_6','ed5b3f4c-a6e3-424b-b8ba-34c317640903-instrument-5-audio.wav',0,1.348,120,432.353,'2017-06-20 23:50:30','2017-07-27 17:33:38','Published'),(83,5,'Hihat Open F_7','8d7c72dc-92bb-4ffa-82ff-13750c8ddbfc-instrument-5-audio.wav',0,2.264,120,183.75,'2017-06-20 23:51:23','2017-07-27 17:33:42','Published'),(84,5,'Hihat Open F_8','7eae03f7-d1aa-42e2-a928-ff6f7b00b25d-instrument-5-audio.wav',0,2.595,120,182.988,'2017-06-20 23:51:59','2017-07-27 17:33:48','Published'),(85,5,'Tom H_1','2f4bf7a2-744e-47cc-b5c2-da0a846cab91-instrument-5-audio.wav',0,1.008,120,1422.58,'2017-06-20 23:54:11','2017-07-27 17:27:27','Published'),(86,5,'Tom H_2','2c2d8ba8-911b-4480-a774-c37102c12e90-instrument-5-audio.wav',0.009,2.036,120,1378.12,'2017-06-20 23:56:11','2017-07-27 17:27:52','Published'),(87,5,'Tom H_8','0e5c97c1-ad2a-4cb5-a1f5-10224c7cec3c-instrument-5-audio.wav',0,2.698,120,1633.33,'2017-06-20 23:58:39','2017-07-27 17:27:11','Published'),(88,5,'Tom H_7','2a525acb-dc9a-47f4-b105-89dc3332d78b-instrument-5-audio.wav',0,1.738,120,1764,'2017-06-20 23:59:37','2017-07-27 17:26:57','Published'),(89,5,'Tom H_6','91f5c7de-609d-48fd-a527-c7b132ee2af5-instrument-5-audio.wav',0,2.984,120,1336.36,'2017-06-21 00:00:19','2017-07-27 17:26:42','Published'),(90,5,'Tom H_5','c18a2f87-df5f-421a-aa59-89fda817210c-instrument-5-audio.wav',0,3.133,120,189.27,'2017-06-21 00:01:06','2017-07-27 17:26:10','Published'),(91,5,'Tom H_4','ee21d28c-6102-4ad7-96a5-49cf5ccaf266-instrument-5-audio.wav',0,2.815,120,186.076,'2017-06-21 00:01:52','2017-07-27 17:26:06','Published'),(92,5,'Tom H_3','8494ac91-a1ef-4045-9f1f-3a1b4a53ee3d-instrument-5-audio.wav',0,2.346,120,1378.12,'2017-06-21 00:02:27','2017-07-27 17:28:04','Published'),(93,5,'Snare Q_1','21369f18-b2b6-4d8b-bd28-de36f294b67e-instrument-5-audio.wav',0,1.206,120,5512.5,'2017-06-21 00:13:47','2017-07-27 17:19:35','Published'),(94,5,'Snare Q_11','88ba75c5-9727-43a3-9ef0-856abe729f78-instrument-5-audio.wav',0,1.524,120,6300,'2017-06-21 00:14:33','2017-07-27 17:19:52','Published'),(95,5,'Snare Q_10','b14d6a26-1e35-4f7c-bbfb-6fd262c2d35f-instrument-5-audio.wav',0,1.631,120,1378.12,'2017-06-21 00:15:27','2017-07-27 17:19:44','Published'),(96,5,'Snare Q_9','0818bf78-3838-43a5-8665-7f8f2814bfc4-instrument-5-audio.wav',0.003,0.583,120,249.153,'2017-06-21 00:49:26','2017-07-27 17:18:07','Published'),(97,5,'Snare Q_8','725e8281-c845-4a87-9a37-9117b1e6a830-instrument-5-audio.wav',0.002,0.799,120,355.645,'2017-06-21 00:51:02','2017-07-27 17:19:07','Published'),(98,5,'Snare Q_7','7fd96254-d9cf-4ad6-9899-dee564543853-instrument-5-audio.wav',0.001,0.653,120,5512.5,'2017-06-21 00:52:59','2017-07-27 17:18:59','Published'),(99,5,'Snare Q_6','83fbed4b-648c-4886-9079-f220fb0dc9fb-instrument-5-audio.wav',0.001,0.659,120,134.451,'2017-06-21 00:54:25','2017-07-27 17:18:54','Published'),(100,5,'Snare Q_5','62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav',0.002,0.405,120,1025.58,'2017-06-21 00:55:25','2017-07-27 17:18:48','Published'),(101,5,'Snare Q_4','8e17510c-a877-42a6-addc-95ef7d559757-instrument-5-audio.wav',0.001,1.257,120,5512.5,'2017-06-21 00:56:51','2017-07-27 17:18:40','Published'),(102,5,'Snare Q_3','a448d6b9-4669-4f17-883a-8dd8c5ce0b8e-instrument-5-audio.wav',0,0.915,120,5512.5,'2017-06-21 00:58:15','2017-07-27 17:20:02','Published'),(103,5,'Snare Q_2','23d5847f-56e6-4b79-99ad-6dfd13b9c5b3-instrument-5-audio.wav',0.001,1.008,120,6300,'2017-06-21 00:59:33','2017-07-27 17:19:57','Published'),(104,5,'Conga M_8','1c5f4752-e790-47a0-b0d9-4eedd54b24a5-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:04:11','2017-07-27 17:20:52','Published'),(105,5,'Tom L_1','568d1c74-a43e-44fc-ab53-0d1d701f6f0f-instrument-5-audio.wav',0,0.851,120,364.463,'2017-06-21 01:05:28','2017-07-27 17:24:19','Published'),(106,5,'Conga M_9','2d2d76f7-9d76-41c6-9e55-0b94703d487c-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:06:20','2017-07-27 17:20:58','Published'),(107,5,'Conga M_7','02dde877-01b4-432d-8d22-f1458917154b-instrument-5-audio.wav',0.001,0.502,120,420,'2017-06-21 01:07:04','2017-07-27 17:20:46','Published'),(108,5,'Conga M_6','3bdc44e7-e464-4a0f-a080-ab3d529ac9dc-instrument-5-audio.wav',0.001,0.512,120,612.5,'2017-06-21 01:07:52','2017-07-27 17:20:40','Published'),(109,5,'Conga M_5','0e47652d-265b-4c83-8c4f-c14a34fc9689-instrument-5-audio.wav',0,0.466,120,612.5,'2017-06-21 01:08:48','2017-07-27 17:20:34','Published'),(110,5,'Conga M_4','f6e912f5-d582-4044-b73b-6e004bb32a15-instrument-5-audio.wav',0,0.6,120,612.5,'2017-06-21 01:09:30','2017-07-27 17:22:48','Published'),(111,5,'Conga M_3','710b3011-cb1e-4065-a514-1e6e4fd19bec-instrument-5-audio.wav',0,0.427,120,612.5,'2017-06-21 01:10:06','2017-07-27 17:22:42','Published'),(112,5,'Conga M_3','c8d1affb-9b7c-4661-bf31-cd80dc2a9ce1-instrument-5-audio.wav',0,0.602,120,588,'2017-06-21 01:10:58','2017-07-27 17:22:38','Published'),(113,5,'Conga M_1','983fc7a1-a1ef-466f-be44-cc1e227ae449-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:11:49','2017-07-27 17:22:34','Published'),(114,5,'Conga M_1','faf2e9c6-6b12-445e-9b2c-93966451ff5e-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:12:52','2017-07-27 17:21:29','Published'),(115,5,'Tom L_10','38c92218-882d-4714-a493-14261e07c4fa-instrument-5-audio.wav',0,0.741,120,302.055,'2017-06-21 01:13:30','2017-07-27 17:24:25','Published'),(116,5,'Tom L_9','50f516a9-faaa-4091-848d-651d96ecc7be-instrument-5-audio.wav',0,0.751,120,176.4,'2017-06-21 01:14:17','2017-07-27 17:24:05','Published'),(117,5,'Tom L_8','f1bac880-fede-4c5d-9249-956f5e179d62-instrument-5-audio.wav',0,0.835,120,290.132,'2017-06-21 01:15:03','2017-07-27 17:24:01','Published'),(119,5,'Tom L_7','b51678cb-50a0-4994-980a-62bf126ca445-instrument-5-audio.wav',0.001,0.674,120,531.325,'2017-06-21 01:19:44','2017-07-27 17:23:58','Published'),(120,5,'Tom L_6','01e988c0-3821-4ba2-8223-70643f3c27cf-instrument-5-audio.wav',0,0.736,120,408.333,'2017-06-21 01:20:53','2017-07-27 17:23:54','Published'),(121,5,'Tom L_5','f6f79c74-f1e0-459b-9728-46f59bd14ee7-instrument-5-audio.wav',0.001,0.608,120,428.155,'2017-06-21 01:21:54','2017-07-27 17:23:50','Published'),(122,5,'Tom L_4','2b9af025-2616-4d03-890f-b74df3413abe-instrument-5-audio.wav',0,0.592,120,11025,'2017-06-21 01:22:35','2017-07-27 17:24:36','Published'),(123,5,'Tom L_3','dd32a686-ef3a-43c4-a3e1-13353d067026-instrument-5-audio.wav',0,0.624,120,110.526,'2017-06-21 01:23:21','2017-07-27 17:24:32','Published'),(124,5,'Tom L_2','6ffdef87-909f-4b67-a2f7-fadbb3a76e33-instrument-5-audio.wav',0,0.528,120,257.895,'2017-06-21 01:23:58','2017-07-27 17:24:29','Published'),(126,3,'Vocal Hie','0248ed87-19e8-449c-9211-4722d6ab8342-instrument-3-audio.wav',0.08,0.477,120,364.463,'2017-06-23 23:53:49','2017-07-26 22:08:31','Published'),(127,3,'Vocal Ahh','d35678fa-f163-433d-8741-250a530b5532-instrument-3-audio.wav',0.012,1.037,120,948.696,'2017-06-23 23:55:53','2017-07-26 22:08:31','Published'),(128,3,'Vocal Hoo','54d3503d-af44-4480-a0d0-8044fb403c5a-instrument-3-audio.wav',0.079,0.45,120,205.116,'2017-06-23 23:57:01','2017-07-26 22:08:31','Published'),(129,3,'Vocal Haa','79b9c4f4-037a-4f6f-bc51-7a7a2dff5528-instrument-3-audio.wav',0.053,0.36,120,864.706,'2017-06-23 23:57:45','2017-07-26 22:08:31','Published'),(132,3,'Vocal Eow','0e2d5fb2-9d40-4741-9da8-bc9943722d66-instrument-3-audio.wav',0.045,0.486,120,383.478,'2017-06-24 00:00:25','2017-07-26 22:08:31','Published'),(133,3,'Vocal Grunt Ooh 2','8896e8d4-0c31-4dd8-93ff-6982a30febdb-instrument-3-audio.wav',0.015,0.247,120,404.587,'2017-06-24 00:10:49','2017-07-26 22:08:31','Published'),(134,3,'Vocal Grunt Ooh','ef489ad1-fb9d-4e77-9b5c-a7b3570c8c09-instrument-3-audio.wav',0.011,0.213,120,1696.15,'2017-06-24 00:11:31','2017-07-26 22:08:31','Published'),(135,4,'Vocal JB Get','e5e8a85b-1c3c-46b5-8394-3b44b5c7e6e1-instrument-4-audio.wav',0.027,0.311,120,386.842,'2017-06-24 00:13:30','2017-07-26 22:08:31','Published'),(136,4,'Vocal JB Baz','76a3e02c-979c-4d64-9bab-3b1a91d3635d-instrument-4-audio.wav',0.018,0.405,120,918.75,'2017-06-24 00:14:55','2017-07-26 22:08:31','Published'),(137,4,'Vocal JB Get 2','22efe6d1-3dea-45a5-906c-1e4bd4465606-instrument-4-audio.wav',0.027,0.29,120,386.842,'2017-06-24 00:16:15','2017-07-26 22:08:31','Published'),(138,4,'Vocal JB Baz2','94bd651e-ce98-4b09-95b8-6e36819e2721-instrument-4-audio.wav',0.032,0.29,120,367.5,'2017-06-24 00:17:37','2017-07-26 22:08:31','Published'),(139,4,'Vocal JB Uhh','3bc65d7a-00a0-42cc-9d15-292f9fbe98ee-instrument-4-audio.wav',0,0.408,120,474.194,'2017-06-24 00:20:34','2017-07-26 22:08:31','Published'),(140,4,'Vocal Woo','c7b78912-493a-4e19-a023-10a6b334e2b3-instrument-4-audio.wav',0.01,0.522,120,464.211,'2017-06-24 00:22:32','2017-07-26 22:08:31','Published'),(141,4,'Vocal JB Me','3fbbf18b-eb45-4375-8bd2-efd5e490c4cb-instrument-4-audio.wav',14,0.336,120,367.5,'2017-06-24 00:23:45','2017-07-26 22:08:31','Published'),(143,4,'Vocal JB Hit','686906da-cc85-4abb-a902-121e98def35d-instrument-4-audio.wav',0.05,0.313,120,512.791,'2017-06-24 00:25:58','2017-07-26 22:08:31','Published'),(144,4,'Vocal Hey','5d808588-5930-4075-a034-4f96b0e2b06f-instrument-4-audio.wav',0.046,0.453,120,760.345,'2017-06-24 00:26:50','2017-07-26 22:08:31','Published'),(145,4,'Vocal Ehh','7806beda-4655-4323-adb0-d9a41d2fc939-instrument-4-audio.wav',0.018,0.297,120,648.529,'2017-06-24 00:27:36','2017-07-26 22:08:31','Published'),(146,4,'Vocal Eh','a6049156-69e0-4128-a4b1-6a17ee4ca0bd-instrument-4-audio.wav',0.018,0.449,120,668.182,'2017-06-24 00:28:28','2017-07-26 22:08:31','Published'),(147,5,'Vocal Watch Me','649a2969-6b98-4201-89fc-968d6414f578-instrument-5-audio.wav',0.05,0.807,120,1225,'2017-06-24 00:29:58','2017-07-26 22:08:31','Published'),(148,5,'Vocal Play It','53fc9c8c-2412-4133-b088-9bac349e6794-instrument-5-audio.wav',0.064,0.358,120,116.053,'2017-06-24 00:31:33','2017-07-26 22:08:31','Published'),(149,5,'Vocal Hoh','5709e633-bd69-407b-b6ba-420395b221de-instrument-5-audio.wav',0.028,0.476,120,689.062,'2017-06-24 00:32:33','2017-07-26 22:08:31','Published'),(150,5,'Vocal Woah','7ac9d00c-0b24-49ad-8cbb-c586ac0f080f-instrument-5-audio.wav',0.02,0.488,120,604.11,'2017-06-24 00:33:44','2017-07-26 22:08:31','Published'),(151,5,'Vocal What 3','489c5976-cbda-4449-a8cf-67d653b77dbf-instrument-5-audio.wav',0.04,0.407,120,370.588,'2017-06-24 00:34:40','2017-07-26 22:08:31','Published'),(152,5,'Vocal What 2','70d22a2a-a888-460f-9dfa-01bae076adfe-instrument-5-audio.wav',0.027,0.276,120,416.038,'2017-06-24 00:35:28','2017-07-26 22:08:31','Published'),(153,5,'Vocal What 1','cccc3d64-9cb9-468d-be42-e1ec29ba65b1-instrument-5-audio.wav',0.058,0.401,120,390.265,'2017-06-24 00:36:13','2017-07-26 22:08:31','Published'),(154,5,'Vocal Oobah','a7779c99-55b0-4067-819d-a8203a157cd6-instrument-5-audio.wav',0,0.904,120,397.297,'2017-06-24 00:37:11','2017-07-26 22:08:31','Published'),(166,5,'Kick 11_339','dfe7c338-dd80-42ee-94da-19bc53489ca7-instrument-5-audio.wav',0,0.569,120,69.014,'2017-07-27 22:35:51','2017-07-27 22:35:51','Published'),(167,5,'Kick 12_339','ccfc6b74-c939-481f-b59d-caced86b2528-instrument-5-audio.wav',0,0.457,120,3675,'2017-07-27 22:36:53','2017-07-27 22:36:53','Published'),(174,5,'Kick 24_339','a0d1938b-9f3d-47b3-a98f-fb0a429e6df7-instrument-5-audio.wav',0.013,0.547,120,88.024,'2017-07-27 23:07:42','2017-07-27 23:07:42','Published'),(176,5,'Kick 28_339','cd42b8a7-c820-43a2-beb1-f5fec4634050-instrument-5-audio.wav',0.024,0.653,120,980,'2017-07-27 23:13:10','2017-07-27 23:13:10','Published'),(180,3,'Vocal How','f70ead8e-f770-4782-83ce-854a1cb3c640-instrument-3-audio.wav',0.074,0.454,120,284.516,'2017-12-11 04:58:58','2017-12-11 04:58:58','Published'),(190,7,'Shakuhachi','e02b5c6c-21a8-47b9-94fc-aaa5d1b2975f-instrument-7-audio.wav',0,2.681,88,525,'2017-12-14 08:11:24','2017-12-14 08:11:24','Published'),(191,7,'Pan Flute','de11db96-dfee-4fc3-8a02-3285d3bd2d80-instrument-7-audio.wav',0,1.624,88,518.824,'2017-12-14 08:15:40','2017-12-14 08:15:40','Published'),(193,8,'Sitar','9dc36d01-fd2e-49f7-a75b-545897962c9d-instrument-8-audio.wav',0,2.424,88,262.5,'2017-12-14 08:20:32','2017-12-14 08:20:32','Published'),(195,9,'Bass Pad','69dfbe99-bca5-4171-bbae-b69c4599531e-instrument-9-audio.wav',0,4.073,88,49.606,'2017-12-14 08:26:47','2017-12-14 08:26:47','Published'),(196,9,'Omen Pad','0732ee48-5a9b-4a1d-bafd-e8c2ef23231d-instrument-9-audio.wav',0,4.45,88,65.333,'2017-12-14 08:28:08','2017-12-14 08:28:08','Published'),(197,10,'Whale Pad','c477ff4c-3212-4cfe-8712-6add5f697a98-instrument-10-audio.wav',0,3.249,88,226.154,'2017-12-14 08:30:10','2017-12-14 08:30:10','Published'),(199,12,'Shami','a166a69f-8944-4577-9a68-8b323dff7a68-instrument-12-audio.wav',0.006,0.999,88,262.5,'2017-12-14 08:37:20','2017-12-14 08:37:39','Published'),(200,12,'Koto','eb8bca2c-994f-4e62-9bf6-1242acc79d21-instrument-12-audio.wav',0,1.294,88,132.831,'2017-12-14 08:38:53','2017-12-14 08:38:53','Published'),(201,12,'Shamisen','0e57fd93-11b6-49d8-b617-2d1b8e657180-instrument-12-audio.wav',0,1,88,262.5,'2017-12-14 08:45:08','2017-12-14 08:45:08','Published'),(206,12,'Shamisen','e5eff131-8813-48bc-9bda-d378b3eeee9a-instrument-12-audio.wav',0.005,1.353,88,264.072,'2017-12-14 09:27:14','2017-12-14 09:27:14','Published'),(493,25,'Clap','d38499b7-cd9c-40b5-b858-69c6b867d614-instrument-25-audio.wav',0.002,0.159,121,595.946,'2018-02-09 21:34:06','2018-02-09 21:34:06','Published'),(495,27,'Hi-Hat w/ Reverb','9a8cd7f2-f68c-4425-8233-c6d77ed2cfcb-instrument-27-audio.wav',0.0002,2.5,121,3692.31,'2018-03-06 06:08:17','2018-03-06 06:08:17','Published'),(497,27,'Hi-Hat Dry','f4f989e9-fd15-48ca-9795-57bcfa72783d-instrument-27-audio.wav',0.0002,0.5,121,3692.31,'2018-03-06 06:16:45','2018-03-06 06:16:45','Published'),(499,27,'Hi-Hat Open','dc725dea-5c27-4f16-aba4-a9ba84ed313b-instrument-27-audio.wav',0,0.5,121,4800,'2018-03-06 06:21:35','2018-03-06 06:21:35','Published'),(500,27,'Hi-Hat Open w/ Reverb','0388acb2-a8ea-4e4f-a3b3-907153ba4ec1-instrument-27-audio.wav',0,2.125,121,4800,'2018-03-06 06:23:43','2018-03-06 06:23:43','Published'),(501,27,'Tambourine','8335ff25-b151-4ef3-a2c9-56dad8f2a2de-instrument-27-audio.wav',0,2.125,121,2823.53,'2018-03-06 06:26:41','2018-03-06 06:27:57','Published'),(502,27,'Spacey Shaker','2b7286b1-fb7f-406e-8b94-9a0d44506860-instrument-27-audio.wav',0,1.75,121,4800,'2018-03-06 06:42:50','2018-03-06 06:42:50','Published'),(503,27,'Spacey Clave','3091be9a-37c1-4c2c-bb4e-12d076584838-instrument-27-audio.wav',0,2.375,121,2823.53,'2018-03-06 06:46:08','2018-03-06 06:46:08','Published'),(504,27,'Clave w/ Reverb','8d02968d-1453-42db-a80d-fbca37f2997b-instrument-27-audio.wav',0,1.75,121,2823.53,'2018-03-06 06:48:58','2018-03-06 06:48:58','Published'),(505,27,'Spacey Cowbell High','2174c84d-c6f0-4acc-be17-b70f7932e014-instrument-27-audio.wav',0,2.625,121,705.882,'2018-03-06 06:51:48','2018-03-06 06:51:48','Published'),(506,27,'Spacey Cowbell Low','81cef961-c5fc-4952-ade8-b7e0fccdd7e0-instrument-27-audio.wav',0,2.5,121,452.83,'2018-03-06 06:54:04','2018-03-06 06:54:04','Published'),(507,27,'Rim Click w/ Long Reverb Tail','3d3d1672-ee60-48bd-b7cf-01ce33d040e2-instrument-27-audio.wav',0.0002,2.125,121,400,'2018-03-06 06:57:24','2018-03-06 06:58:43','Published'),(508,27,'Knocky Rim Click w/ Short Reverb Tail','3e646e1a-85c0-46b6-947c-a054689c07a2-instrument-27-audio.wav',0.0003,1.25,121,251.309,'2018-03-06 07:02:42','2018-03-06 07:02:42','Published'),(509,27,'Springy Clap','78e1797e-cd64-423d-ba8f-2183edeae8d6-instrument-27-audio.wav',0.0011,2.75,121,623.377,'2018-03-06 07:05:47','2018-03-06 07:05:47','Published'),(510,27,'Springy Clap 2: Return of the Springy Clap','a16ab5e9-0544-4430-9d96-acdfe82c8389-instrument-27-audio.wav',0.0005,2,121,8000,'2018-03-06 07:09:38','2018-03-06 07:09:38','Published'),(511,27,'Dry 808 Snare','0b4c697a-bdd4-4f7c-a0c9-91a74f0e493a-instrument-27-audio.wav',0,0.25,121,192,'2018-03-06 07:12:12','2018-03-06 07:12:12','Published'),(512,27,'Snare w/ Reverb Tail','6cce584f-d2a3-42ac-95e0-03f3656b5c79-instrument-27-audio.wav',0,1,121,2400,'2018-03-06 07:15:12','2018-03-06 07:15:12','Published'),(513,27,'Knocky Open Kick','75baff55-6fc4-4370-8d97-d0560de5c9b6-instrument-27-audio.wav',0.004,0.75,121,127.321,'2018-03-06 07:18:27','2018-03-06 07:19:12','Published'),(514,28,'Crispy Snare','baebb6b9-a4b9-4686-9c78-1222135faff3-instrument-28-audio.wav',0.002,0.25,121,187.5,'2018-03-20 00:43:21','2018-03-20 00:43:21','Published'),(515,28,'Fat Snare','f92fa9e7-a7e9-43c0-93fa-4df241b476a9-instrument-28-audio.wav',0,0.375,121,3692.31,'2018-03-20 00:48:03','2018-03-20 01:29:45','Published'),(516,28,'Long Snare','f4144bde-9ccd-4851-b855-f00280689b2a-instrument-28-audio.wav',0,0.375,121,142.433,'2018-03-20 00:51:42','2018-03-20 00:51:42','Published'),(517,28,'Snappy Rim Click','cb5fb85c-4aa5-4359-824f-bff600563ea0-instrument-28-audio.wav',0.0002,0.375,121,1170.73,'2018-03-20 00:54:37','2018-03-20 00:54:37','Published'),(518,28,'Punchy Kick','5b1c7886-c39e-4904-8621-670e7b40f789-instrument-28-audio.wav',0.012,0.375,121,67.321,'2018-03-20 00:59:11','2018-03-20 00:59:11','Published'),(519,28,'Open Kick','24012246-e99a-4391-a1e9-800b5fcf3e7d-instrument-28-audio.wav',0.0004,0.625,121,85.409,'2018-03-20 01:02:05','2018-03-20 01:02:50','Published'),(520,28,'Open Kick 2','6f884c6b-a519-4bff-aaac-ac9eb57fcade-instrument-28-audio.wav',0,0.625,121,127.321,'2018-03-20 01:05:31','2018-03-20 01:05:31','Published'),(521,28,'Open Kick 3','10290e3b-a443-4354-a82c-d153c21515ae-instrument-28-audio.wav',0.0002,0.375,121,124.031,'2018-03-20 01:14:12','2018-03-20 01:14:12','Published'),(522,28,'Punchy Kick 2','a736fa1d-cece-4f2b-b2ab-966969c4b9af-instrument-28-audio.wav',0.0001,0.375,121,8000,'2018-03-20 01:18:04','2018-03-20 01:18:04','Published'),(524,28,'Punchy Kick 3','ee1d1039-a388-4e5f-9504-77a40d59ab5f-instrument-28-audio.wav',0.0002,0.375,121,8000,'2018-03-20 01:23:15','2018-03-20 01:23:15','Published'),(525,28,'Punchy Kick 4','4066c8ba-28ad-464c-a87a-ebc6107b2de3-instrument-28-audio.wav',0.0002,0.375,121,95.618,'2018-03-20 01:25:11','2018-03-20 01:25:11','Published'),(526,28,'Djembe Palm','0dcb66b5-e8e7-41b3-87df-7e3f5c0cd32d-instrument-28-audio.wav',0,0.375,121,238.806,'2018-03-20 01:28:16','2018-03-20 01:28:16','Published'),(527,28,'Djembe Palm 2','06b4f0fa-13eb-460d-957b-f08a5fda76d2-instrument-28-audio.wav',0.0021,0.625,121,83.189,'2018-03-20 01:31:58','2018-03-20 01:31:58','Published'),(528,28,'Djembe Palm 3','02007c9d-6785-404f-a1c6-1d6fb1084636-instrument-28-audio.wav',0.0028,1,121,73.059,'2018-03-20 01:38:36','2018-03-20 01:38:36','Published'),(530,28,'Djembe Rattle','824ac18d-cf19-4ebe-9f4b-d2a16a20a526-instrument-28-audio.wav',0.0003,0.5,121,12000,'2018-03-20 01:43:03','2018-03-20 01:43:03','Published'),(531,28,'Djembe Slap ','4685744e-ab0c-4cc0-904e-9c2698df6e96-instrument-28-audio.wav',0.0004,0.375,121,238.806,'2018-03-20 01:46:52','2018-03-20 01:46:52','Published'),(532,28,'Djembe Slap 2','2d3b1bc7-919a-4419-abc4-25acdf57ee9d-instrument-28-audio.wav',0.0009,0.375,121,375,'2018-03-20 01:48:51','2018-03-20 01:48:51','Published'),(533,28,'Djembe Slap 3','1dc69f00-2372-4f05-91ae-82b0c6234a3f-instrument-28-audio.wav',0.0022,0.375,121,640,'2018-03-20 01:50:53','2018-03-20 01:50:53','Published'),(534,28,'Kenkeni','e5c79d56-7dca-4dfe-8d3e-c6cd4215d798-instrument-28-audio.wav',0.0047,0.5,121,80.672,'2018-03-20 01:53:07','2018-03-20 01:53:07','Published'),(535,28,'Kenkeni 2','9edcee06-73e2-4fcd-80c1-d0b58ecf30c6-instrument-28-audio.wav',0,1.5,121,85.258,'2018-03-20 01:54:58','2018-03-20 01:54:58','Published'),(536,28,'Kenkeni 3','554de69f-0434-42d0-989c-dc7a02179dec-instrument-28-audio.wav',0,1,121,46.967,'2018-03-20 01:57:06','2018-03-20 01:57:06','Published'),(537,28,'Kenkeni 4','5b5ae8cd-5987-49fa-9c48-bd461e0ef90a-instrument-28-audio.wav',0,1.25,121,125,'2018-03-20 01:59:39','2018-03-20 01:59:39','Published'),(538,28,'Kenkeni 5','ce6f3bb6-3d7d-4c2f-ab3a-70899d3f737e-instrument-28-audio.wav',0,0.5,121,77.922,'2018-03-20 02:01:16','2018-03-20 02:01:16','Published'),(539,28,'Kenkeni 6','8a4c6a10-841f-46d7-94e3-be8370624824-instrument-28-audio.wav',0,0.5,121,4363.64,'2018-03-20 02:03:00','2018-03-20 02:03:00','Published'),(540,28,'Kenkeni 7','ac215ada-a632-456d-9894-305ca6e13fc0-instrument-28-audio.wav',0.0003,1.375,121,110.092,'2018-03-20 02:05:36','2018-03-20 02:05:36','Published'),(541,28,'Dun Dun Bell','81fda5b3-33e5-48ac-9304-ead26b731275-instrument-28-audio.wav',0.0005,0.25,121,3428.57,'2018-03-20 02:07:44','2018-03-20 02:07:44','Published'),(542,28,'Dun Dun Da Bell','fad125f5-b8a1-449d-acad-24ac69d74043-instrument-28-audio.wav',0,0.25,121,1600,'2018-03-20 02:10:01','2018-03-20 02:10:01','Published'),(543,28,'Sangpan Bell','bac419d8-2391-4b62-b45c-71122f8c2df6-instrument-28-audio.wav',0.0001,0.25,121,615.385,'2018-03-20 02:12:06','2018-03-20 02:12:06','Published'),(544,28,'Shaker','74c870b3-df58-4aea-80ad-aa2d10b64bd4-instrument-28-audio.wav',0,0.203,121,6000,'2018-03-20 02:14:33','2018-03-20 02:14:33','Published'),(545,28,'Shaker 2','d48bc52b-95f4-4a5c-81f2-804ff13b84d3-instrument-28-audio.wav',0,0.203,121,5333.33,'2018-03-20 02:16:11','2018-03-20 02:16:11','Published'),(546,28,'Shaker 3','a20f3994-de53-4648-abb2-77370b098db5-instrument-28-audio.wav',0,0.203,121,12000,'2018-03-20 02:17:39','2018-03-20 02:17:39','Published'),(547,28,'Weird Snap','7793c912-032b-41e4-9fcf-c2d43df4389d-instrument-28-audio.wav',0.0006,0.25,121,8000,'2018-03-20 02:19:28','2018-03-20 02:19:28','Published'),(548,29,'Aggressive 909 Kick','e37114ef-b92d-4e30-9fd7-3be0ff42efb3-instrument-29-audio.wav',0.006,0.328,121,199.17,'2018-03-26 16:17:24','2018-03-26 16:17:24','Published'),(549,29,'Super Aggressive 909 Kick','4c5bd49a-7662-460c-8c3a-c9f5745a5882-instrument-29-audio.wav',0.0056,0.5,121,183.908,'2018-03-26 16:20:27','2018-03-26 16:20:27','Published'),(550,29,'Punchy Kick','c9edfab8-eab3-41a3-b58e-eb5d514fb134-instrument-29-audio.wav',0.0001,0.156,121,91.429,'2018-03-26 16:23:29','2018-03-26 16:23:29','Published'),(551,29,'Gated Industrial Kick','ef7b2620-beaa-44b3-a07e-ac0498ab7831-instrument-29-audio.wav',0.0003,0.594,121,86.331,'2018-03-26 16:25:29','2018-03-26 16:25:29','Published'),(552,29,'Clicky Kick','7ece3e86-84fd-4c77-820d-442ee2b217cb-instrument-29-audio.wav',0,0.266,121,85.258,'2018-03-26 16:27:48','2018-03-26 16:27:48','Published'),(553,29,'Muted Explosive Kick','48a784a4-a36a-41ab-ac21-e8738e6a2c54-instrument-29-audio.wav',0.003,0.375,121,86.799,'2018-03-26 16:29:42','2018-03-26 16:29:42','Published'),(554,29,'Open Industrial Kick','8d42e8e8-1286-4e71-b537-ceea1d925934-instrument-29-audio.wav',0.0035,0.594,121,80.402,'2018-03-26 16:32:02','2018-03-26 16:32:02','Published'),(555,29,'Clicky Industrial Kick','2e89ef50-4ded-49b5-9c2c-8c826eb0e49d-instrument-29-audio.wav',0,0.391,121,1920,'2018-03-26 16:33:53','2018-03-26 16:33:53','Published'),(556,29,'Moog Snare 2','dda386d5-d093-428a-a39c-78496d2d7ff3-instrument-29-audio.wav',0,0.375,121,139.535,'2018-03-26 16:36:00','2018-03-26 16:36:00','Published'),(557,29,'Moog Snare 1','be6cd4fc-9835-4ec3-9628-bbbae5655b69-instrument-29-audio.wav',0,0.5,121,125.326,'2018-03-26 16:37:27','2018-03-26 16:37:27','Published'),(558,29,'Moog Snare 3','a997bca3-4830-407a-b787-e2a569304a88-instrument-29-audio.wav',0,0.5,121,307.692,'2018-03-26 16:39:13','2018-03-26 16:39:13','Published'),(559,29,'Moog Snare 4','91944422-ddab-48e6-b152-627afa877399-instrument-29-audio.wav',0,0.25,121,137.931,'2018-03-26 16:40:44','2018-03-26 16:40:44','Published'),(560,29,'Closed Hat 3','8722e12a-c1bb-405f-b9da-c90f1c10fed0-instrument-29-audio.wav',0,0.188,121,393.443,'2018-03-26 16:46:06','2018-03-26 16:46:06','Published'),(561,29,'Closed Hat 1','886c3d7c-7ad3-4c7d-a694-7e3e76671538-instrument-29-audio.wav',0.0002,0.328,121,4800,'2018-03-26 16:49:34','2018-03-26 16:49:34','Published'),(562,29,'Closed Hat 2','effafa95-9756-4119-84b8-4452fb00b160-instrument-29-audio.wav',0.0002,0.188,121,666.667,'2018-03-26 16:51:02','2018-03-26 16:51:02','Published'),(563,29,'Closed Hat 4','e84f17a2-34e3-49ee-9c0a-aa71ecef0880-instrument-29-audio.wav',0.0001,0.25,121,5333.33,'2018-03-26 16:52:58','2018-03-26 16:52:58','Published'),(564,29,'CLOSED HAT 5','5c32dc94-496d-412d-b4db-4839d905b074-instrument-29-audio.wav',0.0001,0.375,121,5333.33,'2018-03-26 16:54:20','2018-03-26 16:54:20','Published'),(565,29,'Open To Closed Hat','979f0b39-c642-4f2d-b31b-f9dd327f6f94-instrument-29-audio.wav',0.0015,0.328,121,4800,'2018-03-26 16:56:43','2018-03-26 16:56:43','Published'),(566,29,'Crunchy Snare','c5d6a0cb-e103-417b-b450-77eb355099a3-instrument-29-audio.wav',0.0002,0.375,121,3200,'2018-03-26 16:58:21','2018-03-26 16:58:21','Published'),(567,29,'Flare Up Snare','5575ef02-03a6-458c-b0bb-a5c975819b7a-instrument-29-audio.wav',0.0048,0.375,121,8000,'2018-03-26 16:59:50','2018-03-26 16:59:50','Published'),(568,29,'Industrial Undulating Percussion','a3c983f4-b471-4603-8a94-25207a783056-instrument-29-audio.wav',0,0.625,121,126.649,'2018-03-26 17:02:01','2018-03-26 17:02:01','Published'),(569,29,'Popcorn Snare','c70e0246-bba7-448c-a45a-3876d9105f8a-instrument-29-audio.wav',0.0002,0.375,121,375,'2018-03-26 17:04:28','2018-03-26 17:04:28','Published'),(570,29,'Powering Down','1b6221ea-7f22-4845-b93f-50e901755c43-instrument-29-audio.wav',0,2,121,4000,'2018-03-26 17:05:52','2018-03-26 17:05:52','Published'),(571,29,'Tight Acoustic Snare','e1f9f407-41a9-4f71-b175-8ef43d38cbe9-instrument-29-audio.wav',0,0.375,121,3692.31,'2018-03-26 17:07:57','2018-03-26 17:07:57','Published'),(572,29,'Rough and Sandy Snare','6c177d34-fe6e-49a7-a44d-1516c49bfc26-instrument-29-audio.wav',0,0.625,121,80.402,'2018-03-26 17:09:45','2018-03-26 17:09:45','Published'),(573,29,'Rough and Sandy Crash','6f440597-40e8-4d9d-8855-6d889c827df6-instrument-29-audio.wav',0.0002,2,121,1230.77,'2018-03-26 17:11:09','2018-03-26 17:11:09','Published'),(574,29,'Small Snare','9bf9e365-c11a-4dd6-80a0-b2c2b4fa5d28-instrument-29-audio.wav',0,0.25,121,282.353,'2018-03-26 17:12:38','2018-03-26 17:12:38','Published'),(575,29,'Snappy Snare 2','086e9930-f691-45d8-a254-aaf3660f4e4a-instrument-29-audio.wav',0,0.375,121,214.286,'2018-03-26 17:14:01','2018-03-26 17:14:01','Published'),(576,29,'Snappy Snare 3','7f17ecca-4e1e-4177-bed5-3f5bcf273d65-instrument-29-audio.wav',0.0144,0.375,121,9600,'2018-03-26 17:15:28','2018-03-26 17:15:28','Published'),(577,29,'Snappy Snare','c9384a2c-a4fc-4b2f-8ea5-50069b4c3988-instrument-29-audio.wav',0.0001,0.375,121,1714.29,'2018-03-26 17:16:54','2018-03-26 17:16:54','Published'),(578,29,'Tom 1','203c9a5f-6d89-4072-b1d6-77c36bfe151d-instrument-29-audio.wav',0,0.25,121,167.832,'2018-03-26 17:18:30','2018-03-26 17:18:30','Published'),(579,29,'Tom 2','38adc853-2272-496f-b129-5805b9226a21-instrument-29-audio.wav',0,0.375,121,134.078,'2018-03-26 17:19:53','2018-03-26 17:19:53','Published'),(580,29,'Tom 3','f557e49a-abb3-4ccc-87e2-1595924237fa-instrument-29-audio.wav',0,0.375,121,107.383,'2018-03-26 17:21:21','2018-03-26 17:21:21','Published'),(581,30,'Cabasa','1a9f7a28-e746-438c-a04c-fc148c54ca63-instrument-30-audio.wav',0,0.25,121,4000,'2018-03-27 21:18:04','2018-03-27 21:18:04','Published'),(582,30,'Clap-like Percussion','9acfb60f-9112-4a6c-9697-f5270583c81b-instrument-30-audio.wav',0.0001,0.25,121,8000,'2018-03-27 21:20:08','2018-03-27 21:20:08','Published'),(583,30,'Dubbed Out Clave','20e27f8a-914f-4091-a2df-769069497ae8-instrument-30-audio.wav',0,6.215,121,1777.78,'2018-03-27 21:21:46','2018-03-27 21:21:46','Published'),(584,30,'Dubby Fog Horn','26532e58-f9d9-4e99-a916-0d10f83ad9a9-instrument-30-audio.wav',0.1083,9.25,121,121.212,'2018-03-27 21:23:21','2018-03-27 21:23:21','Published'),(585,30,'Kick with Heavy Attack and Heavy Sub','3d21fbf5-94fd-4f24-853a-f449bff74d6a-instrument-30-audio.wav',0.0001,0.875,121,103.448,'2018-03-27 21:25:53','2018-03-27 21:25:53','Published'),(586,30,'Metallic Snare','417cfa8c-f708-44e7-b06f-497dcc56d4a6-instrument-30-audio.wav',0.0001,0.25,121,322.148,'2018-03-27 21:27:20','2018-03-27 21:27:20','Published'),(587,30,'Knocky Muted Tom','95d5369a-71aa-4b2f-be0e-c72044091307-instrument-30-audio.wav',0.0006,0.219,121,193.548,'2018-03-27 21:28:55','2018-03-27 21:28:55','Published'),(588,30,'Digital Percussive Flam','fecb3546-cd26-4ffe-a226-6822a653b9de-instrument-30-audio.wav',0.0004,0.25,121,226.415,'2018-03-27 21:30:18','2018-03-27 21:30:18','Published'),(589,30,'Percussive Flam','9bd6fafa-0363-45d5-a17d-05ee8a40095b-instrument-30-audio.wav',0.0075,0.281,121,1263.16,'2018-03-27 21:33:02','2018-03-27 21:33:02','Published'),(590,30,'Scraper','1c06528f-5a40-420a-aa25-4f9cde9029ba-instrument-30-audio.wav',0.0002,0.281,121,6000,'2018-03-27 21:35:06','2018-03-27 21:35:06','Published'),(591,30,'Slammed Phasey Closed Hat 2','05f0cbaa-b02d-46e8-8bec-dcb96090038f-instrument-30-audio.wav',0,0.375,121,2666.67,'2018-03-27 21:36:57','2018-03-27 21:36:57','Published'),(592,30,'Slammed Phasey Closed Hat 1','3e08d363-5826-4d4a-974c-c6730c40a9aa-instrument-30-audio.wav',0,0.375,121,6000,'2018-03-27 21:38:19','2018-03-27 21:38:19','Published'),(593,30,'Electronic Small Snare','b94c3696-ca2a-4d61-b6fe-7108908f4074-instrument-30-audio.wav',0,0.344,121,4800,'2018-03-27 21:39:51','2018-03-27 21:39:51','Published'),(594,30,'Tight Dead Acoustic Snare','ef02250e-6420-45dd-92f2-21e736df1545-instrument-30-audio.wav',0.0003,0.25,121,428.571,'2018-03-27 21:41:41','2018-03-27 21:41:41','Published'),(595,30,'Tight Dead Snare 2','5ff14782-bf9b-4f0d-8529-bba612872b1d-instrument-30-audio.wav',0.0003,0.25,121,84.956,'2018-03-27 21:44:17','2018-03-27 21:44:17','Published'),(596,30,'Dead Studio Snare','0c460941-c090-4f71-9ff5-a632319cb5c9-instrument-30-audio.wav',0,0.25,121,186.047,'2018-03-27 21:46:26','2018-03-27 21:46:26','Published'),(597,30,'Basketball-like Snare','446caeea-a77e-47d8-a69f-1a44ec1f4f74-instrument-30-audio.wav',0.0002,0.25,121,4800,'2018-03-27 21:48:06','2018-03-27 21:48:06','Published'),(598,30,'Tabla','f34dfe75-5239-45d8-9b06-d1b6e4240199-instrument-30-audio.wav',0.0002,0.594,121,77.544,'2018-03-27 21:49:15','2018-03-27 21:49:15','Published'),(599,30,'Electronic Tom w/ Slapback','c9221c0a-7748-4d54-ba67-6edf709fd42b-instrument-30-audio.wav',0.001,0.875,121,3428.57,'2018-03-27 21:51:51','2018-03-27 21:51:51','Published'),(600,30,'Electronic Tom w/ Slapback 2','a2aa200b-5f88-47f8-a9f4-7ff5e12156d0-instrument-30-audio.wav',0.009,0.875,121,3428.57,'2018-03-27 21:54:24','2018-03-27 21:54:24','Published'),(601,30,'Undulating Low Tom/Kick','c23128ff-fe96-4e9b-b356-f1b3cb951a2d-instrument-30-audio.wav',0,0.625,121,269.663,'2018-03-27 21:56:03','2018-03-27 21:56:03','Published'),(602,30,'White Noise Crash','07812c0f-d7f6-425b-8964-53cafabaee53-instrument-30-audio.wav',0,0.594,121,1200,'2018-03-27 21:57:31','2018-03-27 21:57:31','Published'),(604,31,'Kick 2','ab6c63c0-06e2-4bbe-afc7-f38fe4eba838-instrument-31-audio.wav',0,0.38,121,109.091,'2018-08-28 20:19:22','2018-08-28 20:19:22','Published'),(605,31,'Kick 3','cb9f17ef-6206-44a7-85c2-4284b1cbe024-instrument-31-audio.wav',0,0.772,121,57.831,'2018-08-28 20:21:12','2018-08-28 20:21:12','Published'),(607,31,'Kick 5','ff18ad0a-73fb-48fc-99cf-fcde25e547eb-instrument-31-audio.wav',0,0.762,121,93.385,'2018-08-28 20:24:57','2018-08-28 20:24:57','Published'),(608,31,'Kick 6','70175671-9e0a-4802-a442-cec930df1e13-instrument-31-audio.wav',0,0.678,121,90.226,'2018-08-28 20:26:35','2018-08-28 20:26:35','Published'),(609,31,'Kick 7','4c2b9e01-68fb-412e-b0c2-d0407efb05e1-instrument-31-audio.wav',0,0.813,121,57.831,'2018-08-28 20:28:09','2018-08-28 20:28:09','Published'),(610,31,'Kick 8','336b3234-55e7-4388-8598-49a1198c3d55-instrument-31-audio.wav',0,0.387,121,109.84,'2018-08-28 20:29:33','2018-08-28 20:29:33','Published'),(611,31,'Kick 9','fcadf35e-d970-4072-8923-c4f94a3c2386-instrument-31-audio.wav',0,0.524,121,125.984,'2018-08-28 20:31:46','2018-08-28 20:31:46','Published'),(613,31,'Kick 11','e24863e9-9d74-4113-9302-a99a8d88928a-instrument-31-audio.wav',0,0.396,121,100,'2018-08-28 20:36:48','2018-08-28 20:36:48','Published'),(614,31,'Kick 12','5c09ac3a-524f-4b63-8a86-dd4faa983320-instrument-31-audio.wav',0,0.447,121,95.238,'2018-08-28 20:38:19','2018-08-28 20:38:19','Published'),(615,31,'Kick 13','c557059c-3191-443a-ab80-d19f572cc1ce-instrument-31-audio.wav',0,0.432,121,78.049,'2018-08-28 20:46:28','2018-08-28 20:46:28','Published'),(616,31,'Kick 14','031c2f89-ff98-42e9-8f65-77c8ea901379-instrument-31-audio.wav',0,0.415,121,178.439,'2018-08-28 20:48:11','2018-08-28 20:48:11','Published'),(617,31,'Kick 15','71dd026c-3ff9-4abf-ac1b-6c6bb6992688-instrument-31-audio.wav',0,0.474,121,72.289,'2018-08-28 20:49:40','2018-08-28 20:49:40','Published'),(618,31,'Kick 16','3b80e6af-512f-41ac-849e-691b562a2f4c-instrument-31-audio.wav',0,0.416,121,79.077,'2018-08-28 20:51:06','2018-08-28 20:51:06','Published'),(619,31,'Kick 17','b35315e5-1c4f-43f2-afcc-be3c2ab1abbf-instrument-31-audio.wav',0,0.474,121,72.289,'2018-08-28 20:53:11','2018-08-28 20:53:11','Published'),(620,31,'Kick 18','8fafb486-38fd-4676-aa80-6984beab0cf3-instrument-31-audio.wav',0,0.442,121,77.295,'2018-08-28 20:54:40','2018-08-28 20:54:40','Published'),(621,31,'Kick 19','893cfb4b-69b8-4b16-a042-931b5368a045-instrument-31-audio.wav',0,0.418,121,88.725,'2018-08-28 20:59:22','2018-08-28 20:59:22','Published'),(622,31,'Kick 20','29eac327-79af-46d6-bdc9-7c04e9591f66-instrument-31-audio.wav',0,0.512,121,95.05,'2018-08-28 21:01:02','2018-08-28 21:01:02','Published'),(623,31,'Kick 21','a7ec1314-d3ff-4bbe-9884-b050beeb0709-instrument-31-audio.wav',0,0.766,121,97.959,'2018-08-28 21:02:26','2018-08-28 21:02:26','Published'),(624,31,'Kick 22','997c368d-5537-4668-833d-06001bbf8d87-instrument-31-audio.wav',0,0.813,121,60.15,'2018-08-28 21:04:28','2018-08-28 21:04:28','Published'),(625,31,'Kick 23','a477e828-9fbe-4827-b601-54413e3dd095-instrument-31-audio.wav',0,0.387,121,74.766,'2018-08-28 21:05:54','2018-08-28 21:05:54','Published'),(626,31,'Kick 24','545aaa55-07a2-4e00-9a77-07adc564ed8b-instrument-31-audio.wav',0,0.524,121,130.79,'2018-08-28 21:07:22','2018-08-28 21:07:22','Published'),(627,31,'Kick 25','0d957165-32ed-4bc0-b1ec-7e7319f4ae70-instrument-31-audio.wav',0,0.442,121,183.206,'2018-08-28 21:08:47','2018-08-28 21:08:47','Published'),(628,31,'Kick 26','a1c52a13-e7a4-49bf-854f-8f2368305b3f-instrument-31-audio.wav',0,0.405,121,114.286,'2018-08-28 21:16:58','2018-08-28 21:16:58','Published'),(629,31,'Kick 27','4bf621f4-84fd-4b9f-aab8-5a5758405422-instrument-31-audio.wav',0,0.447,121,95.238,'2018-08-28 21:19:20','2018-08-28 21:19:20','Published'),(630,31,'Kick 28','f2c959fe-9574-4a72-b313-2a1fcdf4dda3-instrument-31-audio.wav',0,0.432,121,72.948,'2018-08-28 21:20:50','2018-08-28 21:20:50','Published'),(631,31,'Kick 29','df0543c8-9a0b-4942-97d5-87c4e7910e46-instrument-31-audio.wav',0,0.415,121,83.189,'2018-08-28 21:22:35','2018-08-28 21:22:35','Published'),(632,31,'Kick 30','e974711c-0d28-40d7-a5a1-22420b43ae98-instrument-31-audio.wav',0,0.474,121,93.567,'2018-08-28 21:23:42','2018-08-28 21:23:42','Published'),(633,31,'Kick 31','ca39f467-0a37-4f48-999c-34ca8d0dfee7-instrument-31-audio.wav',0,0.416,121,59.553,'2018-08-28 21:58:00','2018-08-28 21:58:00','Published'),(634,31,'Kick 33','83abe818-f137-4a36-a909-101be2736d1c-instrument-31-audio.wav',0,0.442,121,60.606,'2018-08-28 21:59:35','2018-08-28 23:30:03','Published'),(635,31,'Kick 32','e09a0f7b-113b-4f09-a992-953aacf34248-instrument-31-audio.wav',0,0.418,121,98.765,'2018-08-28 22:01:13','2018-08-28 22:01:13','Published'),(637,31,'Kick 35','22c461ab-99be-4aa7-9982-db3e890696cd-instrument-31-audio.wav',0,0.766,121,94.488,'2018-08-28 23:33:08','2018-08-28 23:33:08','Published'),(638,31,'Kick 36','021d253b-f0fd-4858-9e51-4ce89318ba83-instrument-31-audio.wav',0,0.678,121,86.799,'2018-08-28 23:39:47','2018-08-28 23:39:47','Published'),(639,31,'Kick 37','d756bd4e-af20-4257-8f89-d2e6655bf6d5-instrument-31-audio.wav',0,0.387,121,86.957,'2018-08-28 23:42:37','2018-08-28 23:42:37','Published'),(640,31,'Kick 38 ','96075be4-0c3f-45b6-936a-f5afce156a06-instrument-31-audio.wav',0,0.542,121,125.654,'2018-08-28 23:45:19','2018-08-28 23:45:19','Published'),(641,31,'Snare 1','900653d0-335d-4861-b615-b2ec1e878150-instrument-31-audio.wav',0,0.542,121,3692.31,'2018-08-29 00:04:54','2018-08-29 00:05:26','Published'),(642,31,'Snare 2','90e24308-fc3f-4931-90e2-2589400e9a20-instrument-31-audio.wav',0,0.527,121,173.285,'2018-08-29 00:07:30','2018-08-29 00:07:30','Published'),(643,31,'Snare 3','d3ae2e3f-8bb8-4d5a-b3a6-a5e63f3abd3d-instrument-31-audio.wav',0,0.532,121,6000,'2018-08-29 00:09:24','2018-08-29 00:09:24','Published'),(644,31,'Snare 4','582674b4-7768-4d79-9cea-ce2142112cb8-instrument-31-audio.wav',0,0.581,121,6000,'2018-08-29 00:10:53','2018-08-29 00:10:53','Published'),(645,31,'Snare 4','218dc6b3-e41d-461c-8c61-ee9bdc6187d3-instrument-31-audio.wav',0,0.881,121,5333.33,'2018-08-29 00:12:46','2018-08-29 00:12:46','Published'),(646,31,'Snare 5','28aaebdc-9a57-41eb-a4f6-fa2757b8f523-instrument-31-audio.wav',0,0.93,121,320,'2018-08-29 00:14:44','2018-08-29 00:14:44','Published'),(647,31,'Snare 6','fb7fff6b-bce5-4953-a630-fefacb4f9286-instrument-31-audio.wav',0,0.93,121,320,'2018-08-29 00:20:18','2018-08-29 00:20:18','Published'),(648,31,'Snare 7','fe7d0229-0833-47f7-9253-40e2066b3176-instrument-31-audio.wav',0,0.719,121,6000,'2018-08-29 00:27:07','2018-08-29 00:27:07','Published'),(649,31,'Snare 8','452eef7e-e662-4599-9dd5-9a65acd04364-instrument-31-audio.wav',0,0.525,121,2086.96,'2018-08-29 00:29:25','2018-08-29 00:29:25','Published'),(650,31,'Snare 9','fd66964c-b23b-47d2-ad5b-2ce11ee59d7a-instrument-31-audio.wav',0,0.539,121,2285.71,'2018-08-29 00:36:24','2018-08-29 00:36:24','Published'),(653,31,'Snare 10','862618cb-52c8-47e6-bccc-af5f722a88b3-instrument-31-audio.wav',0,0.592,121,3428.57,'2018-08-29 00:46:46','2018-08-29 00:46:46','Published'),(654,31,'Snare 11','da11b4dc-2d66-4cd4-b953-e54a94d46294-instrument-31-audio.wav',0,0.932,121,3428.57,'2018-08-29 00:48:43','2018-08-29 00:48:43','Published'),(655,31,'Snare 13','954655d4-f738-41bc-8b84-9996bf28db9c-instrument-31-audio.wav',0,0.697,121,4000,'2018-08-29 00:50:42','2018-08-29 00:50:42','Published'),(656,31,'Snare 12','7a048cda-c8d6-4308-8dd9-b169f242720a-instrument-31-audio.wav',0,0.932,121,3428.57,'2018-08-29 00:52:17','2018-08-29 00:52:17','Published'),(657,31,'Snare 14','f710d993-30b5-4c3c-9839-c1cbbc6df68f-instrument-31-audio.wav',0.125,0.671,121,143.713,'2018-08-29 01:06:01','2018-08-29 01:06:01','Published'),(658,31,'Snare 15','37afd183-9742-40ce-97c8-b8f92ff9ed5e-instrument-31-audio.wav',0,0.529,121,4800,'2018-08-29 01:08:24','2018-08-29 01:08:24','Published'),(659,31,'Snare 16','9f5911e7-5a31-4d89-9eee-2104b9af0d61-instrument-31-audio.wav',0,0.543,121,6000,'2018-08-29 02:40:36','2018-08-29 02:40:36','Published'),(660,31,'Snare 17','4a6b4b40-24de-459a-8bd1-59b3f18b336e-instrument-31-audio.wav',0,0.586,121,3428.57,'2018-08-29 02:42:01','2018-08-29 02:42:01','Published'),(661,31,'Snare 18','c931be93-b65d-4e4b-aee6-da9eaac49975-instrument-31-audio.wav',0,0.885,121,5333.33,'2018-08-29 02:44:07','2018-08-29 02:44:07','Published'),(662,31,'Snare 19','7b829608-da30-4d04-aaf6-03284d65f667-instrument-31-audio.wav',0,0.925,121,311.688,'2018-08-29 02:45:15','2018-08-29 02:45:15','Published'),(663,31,'Snare 20','5f3c0092-2e25-4c29-9a6a-ad74db702836-instrument-31-audio.wav',0,0.721,121,180.451,'2018-08-29 02:46:35','2018-08-29 02:46:35','Published'),(664,31,'Snare 21','da2c17a7-1713-4070-a7ba-a5d17207458f-instrument-31-audio.wav',0,0.518,121,2086.96,'2018-08-29 02:48:52','2018-08-29 02:48:52','Published'),(665,31,'Snare 22','50d24cfc-3b18-4fa2-b614-5653a1e8b5b9-instrument-31-audio.wav',0,0.535,121,1333.33,'2018-08-29 02:50:05','2018-08-29 02:50:05','Published'),(666,31,'Snare 23','b932198b-2329-461a-9fd4-73abd928b826-instrument-31-audio.wav',0,0.546,121,9428.57,'2018-08-29 02:51:29','2018-08-29 02:51:29','Published'),(667,31,'Snare 24','8e5b0601-e894-4997-a69d-819a012d1aec-instrument-31-audio.wav',0,0.546,121,3428.57,'2018-08-29 02:52:25','2018-08-29 02:52:25','Published'),(668,31,'Snare 25','e6e3bd13-27ce-4bf7-bdfa-510d5841f4db-instrument-31-audio.wav',0,0.932,121,3428.57,'2018-08-29 02:53:40','2018-08-29 02:53:40','Published'),(669,31,'Snare 26','73651d65-331c-4ab4-89d6-7efd33b8e0a8-instrument-31-audio.wav',0,0.922,121,215.247,'2018-08-29 02:54:55','2018-08-29 02:54:55','Published'),(670,31,'Snare 27','9e503784-52ac-47a8-8ca4-8cdd481fcf67-instrument-31-audio.wav',0,0.145,121,3692.31,'2018-08-29 03:02:39','2018-08-29 03:02:39','Published'),(671,31,'Snare 28','f91482d8-14d9-4ffd-b0bd-035259397cc2-instrument-31-audio.wav',0,0.645,121,2526.32,'2018-08-29 03:03:58','2018-08-29 03:03:58','Published'),(672,31,'Snare 29','2c8b8474-f459-4621-8aee-26a0c492f124-instrument-31-audio.wav',0,0.585,121,5333.33,'2018-08-29 03:06:01','2018-08-29 03:06:01','Published'),(673,31,'Snare 30','4a2ea867-86ae-47c5-bf9b-ae9ad542187c-instrument-31-audio.wav',0,0.723,121,5333.33,'2018-08-29 03:07:38','2018-08-29 03:07:38','Published'),(674,31,'Snare 31','6b035b72-ef01-422c-ae2a-33a5c9433754-instrument-31-audio.wav',0,0.687,121,5333.33,'2018-08-29 03:09:39','2018-08-29 03:09:39','Published'),(675,31,'Snare 32','5500a4f1-5311-427d-a003-c64eb0af4333-instrument-31-audio.wav',0.002,0.942,121,5333.33,'2018-08-29 03:11:53','2018-08-29 03:11:53','Published'),(676,31,'Snare 33','f1168eaf-f5ad-4a4b-a261-697f51d503fc-instrument-31-audio.wav',0,0.979,121,311.688,'2018-08-29 03:13:13','2018-08-29 03:13:13','Published'),(677,31,'Snare 34','e75783da-652f-470b-ac41-347bcf6c2c80-instrument-31-audio.wav',0,0.773,121,6000,'2018-08-29 03:14:28','2018-08-29 03:14:28','Published'),(678,31,'Snare 35','ec7e1863-cec0-479b-b242-3f6779d62510-instrument-31-audio.wav',0,0.589,121,3200,'2018-08-29 03:15:43','2018-08-29 03:15:43','Published'),(679,31,'Snare 36','e3366e98-1be0-4de7-94f0-e2ad666102f3-instrument-31-audio.wav',0,0.849,121,3000,'2018-08-29 03:16:52','2018-08-29 03:16:52','Published'),(680,31,'Snare 37','7cc9d73c-3c48-4dc9-9df7-19a7b78c7c43-instrument-31-audio.wav',0,0.884,121,3428.57,'2018-08-29 03:18:11','2018-08-29 03:18:11','Published'),(681,31,'Snare 38','52dbfd5d-3c6f-43d7-9476-065533534726-instrument-31-audio.wav',0,0.85,121,3200,'2018-08-29 03:19:12','2018-08-29 03:19:12','Published'),(682,31,'Snare 39','dce40117-6210-4725-80fb-e45a910f6608-instrument-31-audio.wav',0,0.973,121,3200,'2018-08-29 03:20:04','2018-08-29 03:20:04','Published'),(683,31,'Snare 40','ad5162cc-17cc-47a6-927c-241e27ec90fd-instrument-31-audio.wav',0,1.033,121,3692.31,'2018-08-29 03:21:15','2018-08-29 03:21:15','Published'),(684,31,'Snare 41','8a4c9379-6b10-410e-b865-e619f1259064-instrument-31-audio.wav',0,0.887,121,4000,'2018-08-29 03:23:08','2018-08-29 03:23:08','Published'),(685,31,'Snare 42','6b507b9b-5446-4835-912a-b72f87e520f9-instrument-31-audio.wav',0.003,0.811,121,8000,'2018-08-29 03:24:34','2018-08-29 03:24:34','Published'),(686,31,'Snare 43','2a8946ae-651c-4af8-a49e-7c4e62e508ee-instrument-31-audio.wav',0,1.259,121,8000,'2018-08-29 03:26:25','2018-08-29 03:26:25','Published'),(687,31,'Snare 44','f6e0c316-68ed-4e6e-a4f9-113817076dfd-instrument-31-audio.wav',0,0.694,121,6000,'2018-08-29 03:27:40','2018-08-29 03:27:40','Published'),(688,31,'Snare 45','a5193fe2-2334-415b-94b1-c1e59e8b1dce-instrument-31-audio.wav',0,0.941,121,5333.33,'2018-08-29 03:28:46','2018-08-29 03:28:46','Published'),(689,31,'Tom 1','24df9e2a-8f23-4d4a-93fb-7d491d090c70-instrument-31-audio.wav',0,1.04,121,5333.33,'2018-08-29 04:08:25','2018-08-29 04:08:25','Published'),(690,31,'Tom 2','c00bb383-5220-44d8-a6e4-d52d798a5801-instrument-31-audio.wav',0,1.15,121,6000,'2018-08-29 04:42:30','2018-08-29 04:42:30','Published'),(691,31,'Tom 3','89614224-5284-4c70-86f5-6aec8edca200-instrument-31-audio.wav',0,1.1,121,6000,'2018-08-29 04:44:16','2018-08-29 04:44:16','Published'),(692,31,'Tom 4','a5c2ef5d-e593-42ee-a275-a964117539ba-instrument-31-audio.wav',0,1.014,121,141.176,'2018-08-29 04:46:08','2018-08-29 04:46:08','Published'),(693,31,'Tom 5','dc724a2a-c208-4fbc-900f-c0e48d7935da-instrument-31-audio.wav',0,1.098,121,145.897,'2018-08-29 04:47:37','2018-08-29 04:47:37','Published'),(694,31,'Tom 6','a81c1ddb-7911-4f60-a67a-74842c3f7f2c-instrument-31-audio.wav',0,1.157,121,6857.13,'2018-08-29 04:49:14','2018-08-29 04:49:14','Published'),(695,31,'Tom 7','e281a5c1-6abf-4c4b-9fe5-7b039364bb82-instrument-31-audio.wav',0,1.123,121,6000,'2018-08-29 04:50:22','2018-08-29 04:50:22','Published'),(696,31,'Tom 8','5025ecce-8c1c-456b-9934-5927bf3f6b11-instrument-31-audio.wav',0,0.945,121,6000,'2018-08-29 04:51:37','2018-08-29 04:51:37','Published'),(697,31,'Tom 9','8fc44d96-038f-406c-9709-0b6880087c91-instrument-31-audio.wav',0,1.299,121,8000,'2018-08-29 04:52:53','2018-08-29 04:52:53','Published'),(698,31,'Tom 10','949e0b37-4657-4d5a-8622-776017c0cbea-instrument-31-audio.wav',0,0.956,121,149.068,'2018-08-29 04:54:16','2018-08-29 04:54:16','Published'),(699,32,'Kick 1','897ffb89-a52b-4223-92a7-529dd458c45a-instrument-32-audio.wav',0,0.355,121,55.879,'2018-09-20 01:37:21','2018-09-20 01:37:21','Published'),(700,32,'Kick 2 ','63fb2984-fdf3-4100-86f1-5f70b77e0df8-instrument-32-audio.wav',0,0.306,121,111.111,'2018-09-20 01:39:07','2018-09-20 01:39:07','Published'),(701,32,'Kick 3','b0ceec8f-376c-479b-95b0-e61ed56de370-instrument-32-audio.wav',0.003,0.487,121,85.868,'2018-09-20 01:40:30','2018-09-20 01:40:30','Published'),(702,32,'Kick 4','41a86e00-13e1-43bd-b6a9-124dd8eab117-instrument-32-audio.wav',0.002,0.285,121,113.475,'2018-09-20 01:41:52','2018-09-20 01:41:52','Published'),(703,32,'Kick 5','44c4ce78-ca9c-4dda-a931-7266c0a9295b-instrument-32-audio.wav',0,0.269,121,100.84,'2018-09-20 01:43:03','2018-09-20 01:43:03','Published'),(704,32,'Kick 6','ec419cb6-de9d-4ebc-8e8b-3ae98541819b-instrument-32-audio.wav',0.006,0.36,121,78.947,'2018-09-20 01:44:22','2018-09-20 01:44:22','Published'),(705,32,'Kick 7','77b26556-ce32-4500-9955-4508915e8b8e-instrument-32-audio.wav',0,0.349,121,98.969,'2018-09-20 01:45:33','2018-09-20 01:45:33','Published'),(706,32,'Kick 8','1b7bb65b-9ce3-41db-bd80-9b7e2d8d07c1-instrument-32-audio.wav',0,0.269,121,80.672,'2018-09-20 01:46:52','2018-09-20 01:46:52','Published'),(707,32,'Kick 9','973a5ed8-3dd6-41db-a83a-1e3f52071a8e-instrument-32-audio.wav',0,0.264,121,75,'2018-09-20 01:48:00','2018-09-20 01:48:00','Published'),(708,32,'Kick 10','c6396c9f-1e57-46ff-acab-0f408ea87c2d-instrument-32-audio.wav',0,0.28,121,66.946,'2018-09-20 01:49:18','2018-09-20 01:49:18','Published'),(709,32,'Snare 1','c59874f8-5e5c-40e5-9761-75f32286c8b6-instrument-32-audio.wav',0,0.344,121,159.468,'2018-09-20 01:52:02','2018-09-20 01:52:02','Published'),(710,32,'Snare 2','f0bd8b02-29d2-42d5-a122-da1c58408f77-instrument-32-audio.wav',0,0.285,121,175.182,'2018-09-20 01:53:05','2018-09-20 01:53:05','Published'),(711,32,'Snare 3','c5091d09-3df0-4563-adc2-b36b4057a955-instrument-32-audio.wav',0,0.269,121,4000,'2018-09-20 01:54:30','2018-09-20 01:54:30','Published'),(712,32,'Snare 4','362e452b-e168-4e06-aa04-bad9866b0cad-instrument-32-audio.wav',0,0.232,121,279.07,'2018-09-20 01:55:27','2018-09-20 01:55:27','Published'),(713,32,'Snare 5','e3f9975f-1827-4853-a949-4a72fd01df41-instrument-32-audio.wav',0,0.243,121,333.333,'2018-09-20 01:56:56','2018-09-20 01:56:56','Published'),(714,32,'Snare 6','4070a2df-a5cd-434e-9902-7e607310de1d-instrument-32-audio.wav',0,0.333,121,163.265,'2018-09-20 01:59:04','2018-09-20 01:59:04','Published'),(715,32,'Snare 7','c49cd26a-64e2-4a50-9133-55b18ce389b2-instrument-32-audio.wav',0,0.232,121,169.014,'2018-09-20 02:00:13','2018-09-20 02:00:13','Published'),(716,32,'Snare 8','2e672449-b46e-4633-8758-72d18fb98b99-instrument-32-audio.wav',0,0.333,121,206.009,'2018-09-20 02:01:20','2018-09-20 02:01:20','Published'),(717,32,'Snare 9','9ca6169c-d5f6-4da7-ae33-901b909537d4-instrument-32-audio.wav',0,0.275,121,8000,'2018-09-20 02:03:53','2018-09-20 02:03:53','Published'),(718,32,'Snare 10','9b9db8ec-f772-4369-8250-ed750493e569-instrument-32-audio.wav',0,0.269,121,214.286,'2018-09-20 02:05:17','2018-09-20 02:05:17','Published'),(719,32,'Snare 11','0587a41b-ddf3-4802-a8b1-de671321a5c5-instrument-32-audio.wav',0,0.237,121,4000,'2018-09-20 02:06:56','2018-09-20 02:06:56','Published'),(720,32,'Hi-Hat 1','87dc20e3-67d8-4639-805f-3787d384a45a-instrument-32-audio.wav',0,0.239,121,12000,'2018-09-20 02:09:32','2018-09-20 02:09:32','Published'),(721,32,'Hi-Hat 2','56d29d62-3a85-4ba8-8987-ffcda52e49e8-instrument-32-audio.wav',0,0.297,121,9600,'2018-09-20 02:15:29','2018-09-20 02:15:29','Published'),(722,32,'Hi-Hat 3','b978d483-f212-4fe1-8b56-365d0084a3ea-instrument-32-audio.wav',0,0.26,121,12000,'2018-09-20 02:16:45','2018-09-20 02:17:18','Published'),(723,32,'Hi-Hat 4','15d9f7f2-be82-4a30-9d82-e00b5628c78a-instrument-32-audio.wav',0,0.287,121,8000,'2018-09-20 02:20:28','2018-09-20 02:20:28','Published'),(724,32,'Hi-Hat 5','6fbcbba6-8ec6-4c37-a378-bf108b9be5ed-instrument-32-audio.wav',0,0.244,121,6857.14,'2018-09-20 02:21:56','2018-09-20 02:21:56','Published'),(725,32,'Hi-Hat 7','3f1fc10d-de1a-470a-8745-1c3580407016-instrument-32-audio.wav',0,0.255,121,12000,'2018-09-20 02:24:04','2018-09-20 02:26:05','Published'),(726,32,'Hi-Hat 6','f2e4b541-9a12-4ccd-b8ec-831dfa481075-instrument-32-audio.wav',0,0.244,121,842.105,'2018-09-20 02:25:32','2018-09-20 02:25:32','Published'),(727,32,'Hi-Hat 8','d6add194-6fa5-4cb7-ba33-8cc353e18486-instrument-32-audio.wav',0,0.319,121,380.952,'2018-09-20 02:27:23','2018-09-20 02:27:23','Published'),(728,33,'Tom Low 5','3bfab5c2-974b-452d-b7ee-53347f0b12eb-instrument-33-audio.wav',0,0.73,120,84.483,'2018-10-06 21:09:41','2018-10-06 21:09:41','Published'),(729,33,'Tom High 5','acecd4e5-ec2e-40f9-a02e-a0903590c08b-instrument-33-audio.wav',0.003,0.444,120,126,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(730,33,'Tom High','bcb153cd-b375-4da0-877d-add68dfbac71-instrument-33-audio.wav',0.002,0.42,120,187.66,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(731,33,'Tom 5','4840ae0e-5b15-4ad3-893f-78ba35950c8f-instrument-33-audio.wav',0,0.59,120,90.928,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(732,33,'Tom','1493e34b-a70b-4c4b-b319-6fa154905d29-instrument-33-audio.wav',0.009,0.445,120,225,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(733,33,'Stick Side 7','34341f53-8995-4d09-9157-34f72feccaef-instrument-33-audio.wav',0.003,0.159,120,436.634,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(734,33,'Stick Side 6','610db17f-f525-4696-a6c9-122724e6bed5-instrument-33-audio.wav',0,0.335,120,2321.05,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(735,33,'Stick Side 5','a0f17131-c241-474d-8aa1-e955e3815e72-instrument-33-audio.wav',0,0.248,120,1837.5,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(736,33,'Snare Rim 7','907fadd8-8e79-46a2-b6ae-6ba1068becbf-instrument-33-audio.wav',0,0.461,120,254.913,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(737,33,'Snare Rim 6','24f783b2-e67e-44bd-a65d-d772569a62a7-instrument-33-audio.wav',0,0.527,120,245,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(738,33,'Snare Rim 5','1a1fb20f-a03c-4640-a6d6-c650b1120998-instrument-33-audio.wav',0,0.463,120,181.481,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(739,33,'Snare Rim','911ff421-989d-4c3d-8bc8-85eddc0d4d62-instrument-33-audio.wav',0,1.147,120,239.674,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(740,33,'Snare 5','ed2f4c2d-0d10-4131-b948-53dd3c8fbed9-instrument-33-audio.wav',0.008,0.407,120,180.738,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(741,33,'Snare 4','dcc530f8-ba21-4b98-aa3a-71168868b94a-instrument-33-audio.wav',0.008,0.439,120,204.167,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(742,33,'Snare 3','f3635b83-f52d-4d6b-a447-73718408762a-instrument-33-audio.wav',0.008,0.404,120,2450,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(743,33,'Kick Long 2','129a9d14-1cc3-4699-8428-457ee7cea45f-instrument-33-audio.wav',0.01,1.476,120,59.036,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(744,33,'Kick 7','713f3c48-10ed-4f90-90a4-a28afc53f33e-instrument-33-audio.wav',0.008,0.537,120,43.534,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(745,33,'Kick 3','94f456f1-832e-464b-8c0f-fd072e9c3d51-instrument-33-audio.wav',0.005,0.742,120,52.128,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(746,33,'Kick 3','b502bc7f-2569-48d4-a407-1f3be100c6c6-instrument-33-audio.wav',0.01,0.677,120,56.178,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(747,33,'Hihat Open 7','5ac13b1b-5314-4e10-b23f-3ee7e4ee832b-instrument-33-audio.wav',0,2,120,648.529,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(748,33,'Hihat Open 6','3cfaedd6-d3f9-4a64-9353-ce4b91f4c59b-instrument-33-audio.wav',0,0.809,120,648.529,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(749,33,'Hihat Open 5','a3cda1c6-d620-4b5e-a819-0ab5fc49097a-instrument-33-audio.wav',0.003,1.115,120,648.529,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(750,33,'Hihat Closed 9','8059bae2-1e79-4623-839e-dae5dc29cb3c-instrument-33-audio.wav',0,0.849,120,648.529,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(751,33,'Hihat Closed 8','03ebbe93-4900-4638-88d8-ddcc75a745ce-instrument-33-audio.wav',0,0.905,120,648.529,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(752,33,'Hihat Closed 7','cacdebae-527f-44ac-98f7-ba3bfeefa4fb-instrument-33-audio.wav',0.003,0.962,120,8820,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(753,33,'Cymbal Crash 3','2119399f-7d57-4256-8195-0fd5c9318865-instrument-33-audio.wav',0.01,3.044,120,181.481,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(754,33,'Cymbal Crash 2','ec5c6494-21d6-44fc-9b47-72d9c321b3e3-instrument-33-audio.wav',0.01,3.241,120,469.149,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(755,33,'Cymbal Crash 1','505cba94-a3ff-4e64-8b25-dbe25176e410-instrument-33-audio.wav',0.018,1.878,120,1297.06,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(756,33,'Cowbell','9602390a-51f8-44dc-a9a3-d40d9d26b509-instrument-33-audio.wav',0.002,0.298,120,525,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(757,33,'Conga High','bf0ee9b7-de10-4826-b48a-5c3ae361258c-instrument-33-audio.wav',0.002,0.425,120,187.66,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(758,33,'Conga','70fbac42-0c65-4acf-bc6d-216b656e1ab2-instrument-33-audio.wav',0.001,0.547,120,183.231,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(759,33,'Clap 2','00646186-d34e-4924-aefd-6c7f8c35cd2c-instrument-33-audio.wav',0,0.684,120,188.462,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(760,33,'Clap 1','27168cb8-ebc1-4e9a-aff1-b551cc7c9035-instrument-33-audio.wav',0,0.572,120,185.294,'2018-10-06 21:09:42','2018-10-06 21:09:42','Published'),(761,34,'Tom 5','4cf09790-cf28-4dd2-9588-5df94368cec1-instrument-34-audio.wav',0,0.78,121,148.828,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(762,34,'Tom 4','7456aef7-0c4c-4cc5-a509-089389157349-instrument-34-audio.wav',0,0.746,121,147.829,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(763,34,'Tom 3','f6b2905a-6c8c-4a5c-9f78-5fbced18d5ad-instrument-34-audio.wav',0,0.205,121,156.216,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(764,34,'Tom 2','1393d95f-a8eb-40b2-ad39-bc09edfbdbdf-instrument-34-audio.wav',0,0.207,121,157.896,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(765,34,'Tom 1','042d2f0a-6e50-4e46-8842-315cee1e9bbf-instrument-34-audio.wav',0,0.658,121,156.216,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(766,34,'Snare 5','4f6bfbe3-a8f1-4d49-bd8d-b3a4a8d1e3a5-instrument-34-audio.wav',0.012,0.57,121,270.264,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(767,34,'Snare 4','8d18a3b1-bc07-46ae-a704-12141ffde112-instrument-34-audio.wav',0.009,0.595,121,518.271,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(768,34,'Snare 3','6de10769-b73a-49c3-9e2d-a066caa2c4b6-instrument-34-audio.wav',0.009,0.633,121,160.777,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(769,34,'Snare 2','84511e45-abbe-46c2-926a-b305dbb053a7-instrument-34-audio.wav',0.015,0.489,121,917.771,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(770,34,'Snare 1','44b5b9b4-7316-432f-b092-f98bcdfd07ac-instrument-34-audio.wav',0.019,0.607,121,321.555,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(771,34,'Kick B4','35ac1df2-b427-4cb8-b8d0-dbbfbf092118-instrument-34-audio.wav',0,0.844,121,3671.08,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(772,34,'Kick B3','956b5970-2552-4b2e-867e-53391e487b62-instrument-34-audio.wav',0,0.719,121,76.085,'2018-10-06 21:09:51','2018-10-06 21:09:51','Published'),(773,34,'Hihat Open 4','e127c584-e507-43db-9a09-ede286f3855e-instrument-34-audio.wav',0.048,0.456,121,4894.78,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(774,34,'Hihat Open 3','c248a5f8-6d25-4db6-a8ab-f5aeb4df85b4-instrument-34-audio.wav',0.048,0.453,121,4894.78,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(775,34,'Hihat Open 2','55633d22-d328-469d-98a0-ca771ad21a00-instrument-34-audio.wav',0.05,0.467,121,4894.78,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(776,34,'Hihat Open 1','d4b1b624-3bc8-40c5-835e-b276b6ab4e93-instrument-34-audio.wav',0.042,0.426,121,4894.78,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(777,34,'Hihat Closed 5','ff67294e-2e40-430b-900e-97635b2fb28d-instrument-34-audio.wav',0,0.397,121,3671.08,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(778,34,'Hihat Closed 4','950b053a-d83f-4f26-860d-543694b2daf4-instrument-34-audio.wav',0.005,0.447,121,4004.82,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(779,34,'Hihat Closed 3','97f96d1f-7af4-4d3e-911e-654f12b0b06e-instrument-34-audio.wav',0.01,0.248,121,3388.69,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(780,34,'Hihat Closed 2','8ea17691-f68b-429f-9702-8b3a632fb143-instrument-34-audio.wav',0.005,0.232,121,299.68,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(781,34,'Hihat Closed 1','5a5f246d-3af0-4eaa-984b-df605e690e54-instrument-34-audio.wav',0.014,0.357,121,1631.59,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(782,34,'Crash 2','98212bf8-f414-45d2-b110-7feb549e4649-instrument-34-audio.wav',0,4.024,121,4009.09,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(783,34,'Crash 1','82a7ed83-e780-4499-8847-5f38bde617c7-instrument-34-audio.wav',0,4.032,121,4900,'2018-10-06 21:09:52','2018-10-06 21:09:52','Published'),(784,35,'Tom High 2','66b4998e-7ad9-45ff-abdc-c803267f36e6-instrument-35-audio.wav',0.002,0.411,120,201.37,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(785,35,'Tom High','cbff0b34-7248-4dd7-88d6-5b17eb8a15a2-instrument-35-audio.wav',0,0.2,120,190.909,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(786,35,'Tom 2','2b6e6271-6093-4942-b892-5a92fbe92fd8-instrument-35-audio.wav',0,0.488,120,149.492,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(787,35,'Tom','794713a0-e00d-4918-a729-7d343e09c719-instrument-35-audio.wav',0,0.36,120,104.751,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(788,35,'Snare Rim','e149c7fb-76bc-4ef5-92eb-38498f87e768-instrument-35-audio.wav',0,0.014,120,445.445,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(789,35,'Snare','03d347ba-e65a-481d-954f-eb8f64460e41-instrument-35-audio.wav',0,0.093,120,177.823,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(790,35,'Maracas 2','71282f6f-efdb-4af2-a2f3-c0dc03115853-instrument-35-audio.wav',0.009,0.43,120,11025,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(791,35,'Maracas','411aff24-ac6b-4617-a483-303b050ff502-instrument-35-audio.wav',0,0.026,120,190.086,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(792,35,'Kick Long 2','a47b2510-6e4a-4933-a51a-8a14b2218c40-instrument-35-audio.wav',0,1.963,120,60.494,'2018-10-06 21:10:00','2018-10-06 21:10:00','Published'),(793,35,'Kick Long','04cc3d33-9fe5-4664-a4c9-4a9e7b480bd2-instrument-35-audio.wav',0,0.865,120,57.05,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(794,35,'Kick 2','89f40f31-a538-4afa-871d-861f555f7dbe-instrument-35-audio.wav',0,0.34,120,69.122,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(795,35,'Kick','324b83d8-2993-4a49-83a1-78f3425c3ac4-instrument-35-audio.wav',0,0.702,120,57.495,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(796,35,'Hihat Open','7067e7ee-f192-459a-8733-49e550467c67-instrument-35-audio.wav',0,0.598,120,7350,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(797,35,'Hihat Closed','5a62482a-3d44-493e-95e5-e5b4e5747bd9-instrument-35-audio.wav',0,0.053,120,6300,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(798,35,'Cymbal Crash 2','d2ba2873-c970-45de-afc7-f2d405784376-instrument-35-audio.wav',0,2,120,816.667,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(799,35,'Cymbal Crash','03111025-2607-45f6-b48c-a6abb249c4a9-instrument-35-audio.wav',0,2.229,120,109.701,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(800,35,'Cowbell','fd71cbf9-1677-49d6-b4d5-60ea65d78d09-instrument-35-audio.wav',0,0.34,120,268.902,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(801,35,'Conga High','fe30bbf3-a789-4c71-97b4-40f2746139be-instrument-35-audio.wav',0,0.179,120,397.297,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(802,35,'Conga','5c180a6b-9586-4f1d-8df2-631cb63774a8-instrument-35-audio.wav',0,0.26,120,213,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(803,35,'Claves','5f76cf95-4e48-47bb-b161-2fe38871c72e-instrument-35-audio.wav',0,0.03,120,2594,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(804,35,'Clap 3','7ff0e518-ab41-469f-89d2-a28b8bdba0e9-instrument-35-audio.wav',0,0.734,120,980,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(805,35,'Clap 2','281e202c-cb93-49ba-81cc-06c20e218f1e-instrument-35-audio.wav',0.002,0.356,120,1225,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(806,35,'Clap','094e7d52-866d-4514-8bb7-72c124c4c3ff-instrument-35-audio.wav',0,0.361,120,1102.5,'2018-10-06 21:10:01','2018-10-06 21:10:01','Published'),(807,36,'Tom B3','4a35c285-ed7b-43e7-b37f-32e5441624fb-instrument-36-audio.wav',0,1.858,121,104.255,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(808,36,'Tom B2','83a41117-77b2-406c-824b-88c293e35724-instrument-36-audio.wav',0,1.55,121,136.533,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(809,36,'Tom B1','ab2e3e91-cf16-4737-923d-77889852ea54-instrument-36-audio.wav',0,1.158,121,148.986,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(810,36,'Snare 34','db961345-3fca-4bed-a297-597a765d535c-instrument-36-audio.wav',0,0.462,121,6300,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(811,36,'Snare 34','c935b049-2de0-4caf-897d-6cf21763f41c-instrument-36-audio.wav',0,1.372,121,172.266,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(812,36,'Snare 33','82f11927-1d2d-4bb9-9636-cd79c5344232-instrument-36-audio.wav',0,1.289,121,240.984,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(813,36,'Snare 31','4746ca89-d9f5-4815-a12c-91f4a01e56f4-instrument-36-audio.wav',0,1.834,121,175.697,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(814,36,'Snare 30','8ddc0c7b-7324-4226-8380-f70d53b7be33-instrument-36-audio.wav',0,1.268,121,235.829,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(815,36,'Kick 70','c359546f-7170-4510-9b31-a58bd3b2fceb-instrument-36-audio.wav',0,0.294,121,79.032,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(816,36,'Kick 30','4428cae6-c79a-4f91-9cef-b930bbf68533-instrument-36-audio.wav',0,0.473,121,5512.5,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(817,36,'Kick 0','c353888f-1d25-4b13-ac8e-fec4cdcd10f3-instrument-36-audio.wav',0,0.1,121,153.659,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(818,36,'Hihat Open 2 Half Edge','ad96a177-2b26-46f6-9aaf-f95a5004480e-instrument-36-audio.wav',0.001,0.455,121,7350,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(819,36,'Hihat Open 1 Half Edge','b4195987-56ed-46c6-836d-aa3c05102a80-instrument-36-audio.wav',0.01,0.59,121,518.824,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(820,36,'Hihat Open 1 Half','bc144296-7bc8-4993-addd-8bf88381ce49-instrument-36-audio.wav',0.001,0.457,121,331.579,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(821,36,'Hihat Open 1','975ce349-7f3e-44bb-988b-69c75511962e-instrument-36-audio.wav',0.01,1.39,121,331.579,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(822,36,'Hihat Closed 2','0ca9465e-95f3-4e52-bc7f-6a9176bd7f5a-instrument-36-audio.wav',0,0.149,121,7350,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(823,36,'Hihat Closed 1 Pedal','af2a8ec3-23b8-49b7-8f03-861ca9edf048-instrument-36-audio.wav',0.02,0.13,121,6300,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(824,36,'Hihat Closed 1 Edge','2d93990f-a395-4f47-a646-f4f0e9a8c5aa-instrument-36-audio.wav',0.003,0.204,121,537.805,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(825,36,'Hihat Closed 1','5cab2c30-f2e2-45c9-a16a-35c90a259d92-instrument-36-audio.wav',0.001,0.172,121,373.729,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(826,36,'Crash 7','f4d71032-0066-442a-a102-c13abdf226f1-instrument-36-audio.wav',0,3.175,121,678.462,'2018-10-06 21:10:12','2018-10-06 21:10:12','Published'),(827,38,'Taiko','8aac83e9-16b2-4840-a6e9-a28b9440d817-instrument-38-audio.wav',0,2.006,88,2205,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(828,38,'Snare Rim 6','eedf7a0f-64b2-4c71-8ea8-ba0c75425c8e-instrument-38-audio.wav',0,0.358,120,5512.5,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(829,38,'Snare Rim 4','f2681dcd-8ba3-4baa-a80a-14cad5dbb8f4-instrument-38-audio.wav',0,0.216,120,304.138,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(830,38,'Snare Rim 3','6d19fe2d-9873-4706-bde3-8f10af0db404-instrument-38-audio.wav',0,0.346,120,1002.27,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(831,38,'Snare Rim 2','c20a1f4b-058f-49b9-b982-adb2a6aaefac-instrument-38-audio.wav',0,0.251,120,1050,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(832,38,'Snare Rim 1','34b0ad8c-eb55-4fb7-860e-2b50b5e181b6-instrument-38-audio.wav',0,0.342,120,280.892,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(833,38,'Shamisen B','20d70530-d30d-43ef-9b07-478ebfe18687-instrument-38-audio.wav',0.005,1.353,88,264.072,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(834,38,'Shamisen','5dcc49f0-fcb4-4f63-942d-d7a0b09aa351-instrument-38-audio.wav',0,1,88,262.5,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(835,38,'Shami','8a5b5710-d5a7-48a9-b1b1-02514aedb0f3-instrument-38-audio.wav',0.006,0.999,88,262.5,'2018-10-06 21:10:36','2018-10-06 21:10:36','Published'),(836,38,'Koto','075b432c-3616-4737-a841-033284cf1252-instrument-38-audio.wav',0,1.294,88,132.831,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(837,38,'Kick 93','576f3155-d321-475d-a5b8-494b9fae6f44-instrument-38-audio.wav',0,1.831,120,106.01,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(838,38,'Kick 92','b6eea389-d360-4604-9b15-0435ba573e2a-instrument-38-audio.wav',0,1.463,120,63.728,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(839,38,'Kick 87','3badc739-a69a-4bce-8fad-510aaf0f3074-instrument-38-audio.wav',0,1.158,120,64.663,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(840,38,'Kick 86','be679c0a-93ea-451d-bc10-f454673cbc28-instrument-38-audio.wav',0,1.046,120,67.951,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(841,38,'Kalimba','a825b767-773c-49fe-b1eb-5d62867bc092-instrument-38-audio.wav',0,1.175,88,262.5,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(842,38,'Hihat Open 14','3cc4f146-ba60-4489-b4c1-310069e1c509-instrument-38-audio.wav',0,0.422,121,149.84,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(843,38,'Hihat Open 12','b7f421ab-381a-4983-b732-658374b8ce33-instrument-38-audio.wav',0,0.41,121,155.116,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(844,38,'Hihat Closed 15','27ef6f44-99a4-4d57-95cf-448bebb702a2-instrument-38-audio.wav',0,0.314,121,205.855,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(845,38,'Hihat Closed 14','5ab088d9-7302-48f1-bd57-74f6924e61ee-instrument-38-audio.wav',0,0.293,121,620.465,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(846,38,'F','0e95155d-d5a7-4ec4-a4b8-1830a54589cb-instrument-38-audio.wav',0,0.802,88,773.684,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(847,38,'E','108bc006-8cec-4ba8-84da-75c94a77bc19-instrument-38-audio.wav',0,0.895,88,588,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(848,38,'D','fb0fe08a-e881-404c-a9dd-532bc7cab45c-instrument-38-audio.wav',0,1.837,88,250.568,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(849,38,'Crash 14','4493b949-9bc2-44a2-9d74-c65d5b96587c-instrument-38-audio.wav',0,1.479,121,518.824,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(850,38,'C','def6bba3-4fa7-4248-b1cf-2fe875734f03-instrument-38-audio.wav',0,0.805,88,722.951,'2018-10-06 21:10:37','2018-10-06 21:10:37','Published'),(851,39,'Tom Low E4','e7ff4972-b1d2-4f23-8334-7f38e65251e4-instrument-39-audio.wav',0,2.194,121,81.818,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(852,39,'Tom Low D2','83dbc947-ea66-4fbf-9bb5-a86ac0c35a4d-instrument-39-audio.wav',0,0.997,121,96.248,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(853,39,'Tom Low A2','0c205d69-ae55-4b79-ba04-1b7a37877bab-instrument-39-audio.wav',0,1.457,121,111.646,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(854,39,'Tom High E1','f662ab55-da9e-4a7c-8cc9-9f0bdfb85af6-instrument-39-audio.wav',0,2.049,121,161.538,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(855,39,'Tom High D1','dbbe75be-4db1-4fe9-b2cc-c9f9672cd168-instrument-39-audio.wav',0,0.859,121,179.268,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(856,39,'Tom High A1','f1798f7d-e93d-47c4-975b-0a81a11dd08a-instrument-39-audio.wav',0,1.065,121,164.552,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(857,39,'Snare 7','d79952fc-c6e3-4318-8b73-23d15449df57-instrument-39-audio.wav',0,1.123,121,270.552,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(858,39,'Snare 44','42c87a08-09c0-4186-8b8c-7fa588f335a6-instrument-39-audio.wav',0,0.458,121,11025,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(859,39,'Snare 3','b7402bc0-1ae4-480f-97fb-538de7405d99-instrument-39-audio.wav',0,1.057,121,270.552,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(860,39,'Kick 38','7d3f0e48-77b8-4dbd-8f02-813a779b2c67-instrument-39-audio.wav',0,0.774,121,58.8,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(861,39,'Kick 32','bb337e3f-310c-4732-8b4c-8b80dedc9504-instrument-39-audio.wav',0,0.753,121,56.394,'2018-10-06 21:10:44','2018-10-06 21:10:44','Published'),(862,39,'Kick 23','4b94b1e1-c1d1-455d-8d23-3faaf1c89a99-instrument-39-audio.wav',0,0.992,121,52.814,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(863,39,'Kick 16','88525f80-96f7-4208-833e-e3026dc04a2b-instrument-39-audio.wav',0,0.808,121,59.274,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(864,39,'Hihat Open (Tambourine) 5','a579d1f0-6339-4f76-a001-6cb329489dfa-instrument-39-audio.wav',0,0.262,121,2594.12,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(865,39,'Hihat Open (Tambourine) 4','68419cfa-0b82-484a-8a2b-a84348b7fb88-instrument-39-audio.wav',0,0.148,121,4594.12,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(866,39,'Hihat Open (Tambourine) 2','05e5a569-5754-4976-9869-e3188ce046cf-instrument-39-audio.wav',0,0.321,121,6300,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(867,39,'Hihat Open (Tambourine) 1','0e1affdb-ba5b-4107-a0d4-4ef6565d0255-instrument-39-audio.wav',0,0.297,121,6300,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(868,39,'Hihat Closed (Shaker) 5','a6f581cd-7c96-4726-8b22-4337a66d0022-instrument-39-audio.wav',0.015,0.092,121,6300,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(869,39,'Hihat Closed (Shaker) 4','295d712b-0726-48fb-9238-e29f7a6e3ca0-instrument-39-audio.wav',0.015,0.086,121,7350,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(870,39,'Hihat Closed (Shaker) 3','7965cf0f-4721-4853-b466-208d193b878a-instrument-39-audio.wav',0.015,0.087,121,6300,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(871,39,'Hihat Closed (Shaker) 1','f7715bfc-ac3e-45f7-9e64-b53406d7376d-instrument-39-audio.wav',0.025,0.1,121,2321.05,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(872,39,'Crash 12','3cb0cdaa-f60d-4c34-9202-795543bf0982-instrument-39-audio.wav',0,1.768,121,4900,'2018-10-06 21:10:45','2018-10-06 21:10:45','Published'),(873,40,'Electric Wind Sweep','79e64c9a-2163-4eb0-b4c7-16ab5ad821cd-instrument-40-audio.wav',0,0.937,121,1000,'2018-10-08 06:31:32','2018-10-08 06:31:32','Published'),(874,40,'Kick 1','73379128-dfca-4eeb-b8b4-7c111e5f38c4-instrument-40-audio.wav',0,0.147,121,9600,'2018-10-08 06:32:58','2018-10-08 06:32:58','Published'),(875,40,'Kick 2','649f1db8-ff49-40bb-83f0-29b5bd880c6d-instrument-40-audio.wav',0,0.345,121,6000,'2018-10-08 06:35:42','2018-10-08 06:35:42','Published'),(876,40,'Snare 1','618972fa-818c-49ab-87c8-f8c73386917a-instrument-40-audio.wav',0,0.134,121,2666.67,'2018-10-08 06:36:55','2018-10-08 06:36:55','Published'),(877,40,'Snare 2','28d1308d-ac22-4469-af63-616b39a55f7e-instrument-40-audio.wav',0,0.181,121,156.863,'2018-10-08 06:38:44','2018-10-08 06:38:44','Published'),(878,40,'Snare 3','98899718-561c-41cc-b783-7ffccd081ed9-instrument-40-audio.wav',0.005,0.256,121,241.206,'2018-10-08 06:40:36','2018-10-08 06:40:36','Published'),(879,40,'Fluxing Shaker 1','a55df999-37fa-414a-9fe2-4db2098ae7de-instrument-40-audio.wav',0,0.306,121,1411.77,'2018-10-08 06:42:37','2018-10-08 06:42:37','Published'),(880,40,'Rim Click 1','5ea3562c-3058-4062-862d-29171a1a1cea-instrument-40-audio.wav',0,0.124,121,923.077,'2018-10-08 06:44:00','2018-10-08 06:44:00','Published'),(881,40,'Fluxing Shaker 2','3a4c6376-c4f6-4538-846e-2c6d32b1a3f0-instrument-40-audio.wav',0,0.363,121,1263.16,'2018-10-08 06:48:32','2018-10-08 06:48:32','Published'),(882,40,'Clapping Snare Spread','b45e5925-3c6e-4423-a278-e1fb8c52579e-instrument-40-audio.wav',0,0.105,121,641.176,'2018-10-08 06:50:48','2018-10-08 06:50:48','Published'),(883,40,'Clapping Snare Spread 2','58283ebc-53e3-4f33-b01d-57f8c5541e2f-instrument-40-audio.wav',0,0.206,121,237.624,'2018-10-08 06:52:59','2018-10-08 06:52:59','Published'),(884,40,'Clap 1','03b7667b-4b3f-4649-9671-e6fae61e18f0-instrument-40-audio.wav',0,0.385,121,6857.14,'2018-10-08 06:54:55','2018-10-08 06:54:55','Published'),(885,40,'Crashing Cabinet','459f065f-a3aa-4190-8f0e-ffed0cbae468-instrument-40-audio.wav',0.006,1.358,121,2000,'2018-10-08 06:59:10','2018-10-08 06:59:10','Published'),(886,40,'Snare 4','644ff7da-2674-4644-9dab-f65044b1e662-instrument-40-audio.wav',0,0.122,121,224.299,'2018-10-08 07:00:58','2018-10-08 07:00:58','Published'),(887,40,'Snare 5','96ebdcac-f6d0-4aad-8b45-293c6c182a07-instrument-40-audio.wav',0,0.236,121,5333.33,'2018-10-08 07:01:59','2018-10-08 07:01:59','Published'),(888,40,'Snare 6','64703edd-ae19-4ffb-84f3-d016d1209fc0-instrument-40-audio.wav',0,0.245,121,5333.33,'2018-10-08 07:03:28','2018-10-08 07:03:28','Published'),(890,40,'Snare 7','95f6d747-cc59-4376-a03f-f15de2d6771d-instrument-40-audio.wav',0,0.407,121,480,'2018-10-08 07:06:05','2018-10-08 07:06:05','Published'),(891,40,'Hi-Hat 1','9a91b6f0-1a14-48c7-933b-5a53c7824e3d-instrument-40-audio.wav',0,0.068,121,16000,'2018-10-08 07:07:28','2018-10-08 07:07:28','Published'),(892,40,'Hi-Hat 2','f19c9101-1ad1-43c9-8fc0-8a18fe116e64-instrument-40-audio.wav',0,0.175,121,16000,'2018-10-08 07:08:46','2018-10-08 07:08:46','Published'),(893,40,'Future Shaker','2c0665c5-2a39-4cd1-8c06-2829d81e94f9-instrument-40-audio.wav',0,0.257,121,12000,'2018-10-08 07:10:37','2018-10-08 07:10:37','Published'),(894,40,'Peering Insect','ede6468f-2ecf-44e3-9ce9-6749f54e4b23-instrument-40-audio.wav',0,0.116,121,259.459,'2018-10-08 07:12:50','2018-10-08 07:14:26','Published'),(895,40,'Short Lazer','a53a0ec5-ce7e-42fc-b014-d83f1460cad4-instrument-40-audio.wav',0,0.197,121,3200,'2018-10-08 07:16:19','2018-10-08 07:16:19','Published'),(896,40,'Tiny Vibrating String','5a34d08c-ae2a-4d41-be0c-599e4f641a48-instrument-40-audio.wav',0,0.395,121,716.418,'2018-10-08 07:17:32','2018-10-08 07:17:32','Published'),(897,40,'Reverse Life 1 second','43675ee8-ccbc-476f-ae00-8a9f42fab6bb-instrument-40-audio.wav',0.02,0.283,121,923.077,'2018-10-08 07:19:52','2018-10-08 07:19:52','Published'),(898,40,'Berimbau-ish Hi-Hat','f71746de-a0c8-4c3a-9284-36dd7067a0b1-instrument-40-audio.wav',0,0.209,121,6857.14,'2018-10-08 07:22:23','2018-10-08 07:22:23','Published'),(899,40,'Dead Clave','bfcdb30f-664a-4c2e-ab75-9ba5cf2f703e-instrument-40-audio.wav',0,0.015,121,421.053,'2018-10-08 07:24:02','2018-10-08 07:24:02','Published'),(900,40,'Hi Tom','6339d702-184d-449d-8a2a-9422c1c247f1-instrument-40-audio.wav',0,1.286,121,6000,'2018-10-08 07:26:23','2018-10-08 07:30:37','Published'),(901,40,'Phasing Triangle','e34f439c-5f18-4841-943f-5264b56f90bc-instrument-40-audio.wav',0,1.242,121,369.231,'2018-10-08 07:29:06','2018-10-08 07:29:06','Published'),(902,40,'Mid Tom','ae0e0456-e47e-4d02-9726-5e9736952564-instrument-40-audio.wav',0,1.286,121,6000,'2018-10-08 07:30:11','2018-10-08 07:30:11','Published'),(903,40,'Vibraslap','1bf9238f-dbf4-4e54-b18e-1a5eee2eaeb4-instrument-40-audio.wav',0,1.159,121,2666.67,'2018-10-08 07:31:49','2018-10-08 07:31:49','Published'),(904,40,'Flaming Mid Tom','923087e9-a5d1-4732-8386-27103a6b118b-instrument-40-audio.wav',0,0.264,121,151.899,'2018-10-08 07:33:20','2018-10-08 07:33:20','Published'),(905,40,'Dirty Sweep','c1da2d42-8504-4eda-95e9-95da8df53cc4-instrument-40-audio.wav',0,0.338,121,207.792,'2018-10-08 07:34:56','2018-10-08 07:34:56','Published'),(906,40,'Mid Tom 2','15e73c79-697f-4ac8-bc43-a313f4717714-instrument-40-audio.wav',0,0.789,121,6000,'2018-10-08 07:36:49','2018-10-08 07:36:49','Published'),(907,40,'Mid Tom 3','932664e1-2a18-4816-8509-ed8a4dde5161-instrument-40-audio.wav',0,0.95,121,5333.33,'2018-10-08 07:38:25','2018-10-08 07:38:25','Published'),(908,40,'Kick 3','bda7865e-6158-4fcb-ab76-33c84b15eae3-instrument-40-audio.wav',0,0.36,121,8000,'2018-10-08 07:40:03','2018-10-08 07:40:03','Published'),(909,40,'Kick 4','96f9a747-3ec3-4b58-8a90-8fc7bd45ecba-instrument-40-audio.wav',0,0.308,121,76.8,'2018-10-08 07:41:10','2018-10-08 07:41:10','Published');
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
INSERT INTO `audio_event` VALUES (1,2,1,1,'KICK',0.03,0.5,'X','2017-04-22 21:24:11','2017-04-22 21:28:14'),(2,3,1,1,'KICKLONG',0.025,0.5,'X','2017-04-22 21:24:54','2017-04-24 02:19:23'),(3,4,1,0.1,'HIHATCLOSED',0.025,0.1,'X','2017-04-22 21:26:58','2017-06-10 19:24:57'),(4,5,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-04-22 21:43:14','2017-04-22 21:43:14'),(5,6,1,0.4,'SNARE',0.002,0.091,'X','2017-04-22 21:45:06','2017-04-22 21:45:06'),(6,7,0.7,0.6,'TOM',0.002,0.35,'X','2017-04-22 21:46:12','2017-04-22 21:46:12'),(7,8,0.8,0.8,'CLAVES',0,0.05,'X','2017-04-24 00:03:50','2017-04-24 00:03:50'),(8,9,0.8,0.9,'CONGA',0.004,0.2,'X','2017-04-24 00:04:13','2017-04-24 00:04:13'),(9,11,1,1,'TOMHIGH',0.004,0.2,'X','2017-04-24 02:18:57','2017-04-24 02:18:57'),(10,10,1,1,'CONGAHIGH',0.005,0.2,'x','2017-04-24 02:20:10','2017-04-24 02:20:10'),(11,12,0.8,0.3,'CLAP',0.004,0.3,'x','2017-04-24 02:21:39','2017-06-04 04:30:00'),(12,13,1,0.5,'COWBELL',0.004,0.3,'x','2017-04-24 02:23:14','2017-04-24 02:23:14'),(13,14,1,0,'CYMBALCRASH',0,4,'x','2017-04-24 02:24:36','2017-06-16 03:26:40'),(14,15,0.5,0.1,'HIHATOPEN',0.002,0.59,'x','2017-04-24 02:25:56','2017-06-10 19:25:57'),(15,16,0.6,0.2,'SNARERIM',0.001,0.014,'x','2017-04-24 02:27:24','2017-06-10 19:27:09'),(16,22,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:35','2017-06-15 23:27:35'),(17,23,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:55','2017-06-15 23:27:55'),(18,24,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:28:31','2017-06-15 23:28:31'),(20,26,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:29:00','2017-06-15 23:29:00'),(21,27,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:14','2017-06-15 23:29:14'),(22,28,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:28','2017-06-15 23:29:28'),(23,29,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:40','2017-06-15 23:29:40'),(24,30,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:02','2017-06-15 23:30:02'),(25,31,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:22','2017-06-15 23:30:22'),(26,32,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:40','2017-06-15 23:30:40'),(27,33,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:30:54','2017-06-15 23:30:54'),(28,34,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:06','2017-06-15 23:31:06'),(29,35,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:17','2017-06-15 23:31:17'),(30,36,1,0.6,'TOMHIGH',0,1,'x','2017-06-15 23:31:59','2017-06-16 01:07:01'),(31,37,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:17','2017-06-15 23:32:17'),(32,38,1,0.6,'TOM',0,1,'x','2017-06-15 23:32:30','2017-06-16 01:07:14'),(33,39,1,0.6,'CONGAHIGH',0,1,'x','2017-06-15 23:32:39','2017-06-16 01:07:57'),(34,40,1,0.6,'CONGA',0,1,'x','2017-06-15 23:32:48','2017-06-16 01:08:10'),(35,41,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:59','2017-06-15 23:32:59'),(36,42,1,0.1,'SNARE',0,1,'x','2017-06-15 23:33:08','2017-06-15 23:33:08'),(37,44,1,1,'KICK',0,1,'x','2017-06-16 00:25:00','2017-06-16 00:25:00'),(38,43,1,1,'KICK',0,1,'x','2017-06-16 00:25:19','2017-06-16 00:25:19'),(39,45,1,1,'KICK',0,1,'x','2017-06-16 00:27:24','2017-06-16 00:27:24'),(42,48,1,0.6,'TOMLOW',0,1,'x','2017-06-16 00:34:38','2017-06-16 01:07:44'),(43,49,1,0.6,'TOM',0,1,'x','2017-06-16 00:36:12','2017-06-16 01:08:36'),(44,50,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 00:38:24','2017-06-16 01:08:52'),(45,51,1,1,'KICKLONG',0,1,'x','2017-06-16 01:10:01','2017-06-16 01:10:01'),(48,54,1,0.1,'CLAP',0,1,'x','2017-06-16 02:16:01','2017-06-16 02:16:01'),(49,56,1,1,'KICK',0,1,'x','2017-06-16 03:01:30','2017-06-16 03:01:30'),(50,57,1,1,'KICKLONG',0,1,'x','2017-06-16 03:04:19','2017-06-16 03:04:19'),(51,58,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 03:06:43','2017-06-16 03:06:43'),(52,59,1,0.6,'TOMLOW',0,1,'x','2017-06-16 03:07:39','2017-06-16 03:07:39'),(53,60,1,0.6,'TOM',0,1,'x','2017-06-16 03:10:02','2017-06-16 03:10:02'),(54,61,1,0.1,'CLAP',0,1,'x','2017-06-16 03:13:38','2017-06-16 03:13:38'),(55,62,1,0.1,'CLAP',0,1,'x','2017-06-16 03:14:51','2017-06-16 03:14:51'),(56,63,1,0.1,'MARACAS',0,1,'x','2017-06-16 03:17:20','2017-06-16 03:17:20'),(57,64,1,0.2,'COWBELL',0,1,'x','2017-06-16 03:20:15','2017-06-16 03:20:15'),(58,65,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:22:02','2017-06-16 03:24:34'),(59,66,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:24:20','2017-06-16 03:24:20'),(60,67,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:25:47','2017-06-16 03:25:47'),(61,68,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:29:05','2017-06-16 03:29:05'),(62,69,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:12:32','2017-06-20 23:16:38'),(63,70,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:17:52','2017-06-20 23:17:52'),(64,71,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:33:17','2017-06-20 23:33:17'),(65,72,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:35:34','2017-06-20 23:35:34'),(66,73,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:36:46','2017-06-20 23:36:46'),(67,74,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:37:56','2017-06-20 23:37:56'),(68,75,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:39:16','2017-06-20 23:39:16'),(69,76,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:40:49','2017-06-20 23:40:49'),(70,77,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:45:46','2017-06-20 23:45:46'),(71,78,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:46:41','2017-06-20 23:46:41'),(72,79,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:47:35','2017-06-20 23:47:35'),(73,80,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:48:55','2017-06-20 23:48:55'),(74,81,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:49:58','2017-06-20 23:49:58'),(75,82,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:50:42','2017-06-20 23:50:42'),(76,83,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:51:32','2017-06-20 23:51:32'),(77,84,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:52:08','2017-06-20 23:52:08'),(78,85,1,0.6,'TOM',0,1,'X','2017-06-20 23:54:26','2017-07-29 21:16:34'),(79,86,1,0.6,'KICK',0,1,'X','2017-06-20 23:56:22','2017-07-29 21:16:26'),(80,87,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:00','2017-07-29 21:15:54'),(81,88,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:46','2017-07-29 21:15:47'),(82,89,1,0.6,'TOM',0,1,'X','2017-06-21 00:00:29','2017-07-29 21:15:41'),(83,90,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:16','2017-07-29 21:15:34'),(84,91,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:59','2017-07-29 21:15:27'),(85,92,1,0.6,'TOM',0,1,'X','2017-06-21 00:02:39','2017-07-29 21:16:17'),(86,93,1,0.1,'SNARE',0,1,'X','2017-06-21 00:14:01','2017-06-21 00:14:01'),(87,94,1,0.15,'SNARE',0,1,'X','2017-06-21 00:14:49','2017-06-21 00:14:49'),(88,95,1,0.1,'SNARE',0,1,'X','2017-06-21 00:15:35','2017-06-21 00:15:35'),(89,96,1,0.15,'SNARE',0,1,'X','2017-06-21 00:49:39','2017-06-21 00:50:12'),(90,97,1,0.15,'SNARE',0,1,'X','2017-06-21 00:51:14','2017-06-21 00:51:14'),(91,98,1,0.15,'SNARE',0,1,'X','2017-06-21 00:53:11','2017-06-21 00:53:11'),(92,99,1,0.15,'SNARE',0,1,'X','2017-06-21 00:54:35','2017-06-21 00:54:35'),(93,100,1,0.15,'SNARE',0,1,'X','2017-06-21 00:55:37','2017-06-21 00:55:37'),(94,101,1,0.15,'SNARE',0,1,'X','2017-06-21 00:57:01','2017-06-21 00:57:01'),(95,102,1,0.15,'SNARE',0,1,'X','2017-06-21 00:58:31','2017-06-21 00:58:31'),(96,103,1,0.15,'SNARE',0,1,'X','2017-06-21 01:00:02','2017-06-21 01:00:02'),(97,104,1,0.6,'CONGA',0,1,'X','2017-06-21 01:04:26','2017-06-21 01:04:26'),(98,105,1,0.6,'TOM',0,1,'X','2017-06-21 01:05:37','2017-06-21 01:05:37'),(99,106,1,0.6,'CONGA',0,1,'X','2017-06-21 01:06:29','2017-06-21 01:06:29'),(100,107,1,0.6,'CONGA',0,1,'X','2017-06-21 01:07:21','2017-06-21 01:07:21'),(101,108,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:06','2017-06-21 01:08:13'),(102,109,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:59','2017-06-21 01:08:59'),(103,110,1,0.6,'CONGA',0,1,'X','2017-06-21 01:09:39','2017-06-21 01:09:39'),(104,111,1,0.6,'CONGA',0,1,'X','2017-06-21 01:10:23','2017-06-21 01:10:23'),(105,112,1,0.6,'CONGA',0,1,'X','2017-06-21 01:11:09','2017-06-21 01:11:09'),(106,113,1,0.6,'CONGA',0,1,'X','2017-06-21 01:12:01','2017-06-21 01:12:01'),(107,114,1,0.6,'CONGA',0,1,'X','2017-06-21 01:13:01','2017-06-21 01:13:01'),(108,115,1,0.6,'TOM',0,1,'X','2017-06-21 01:13:42','2017-06-21 01:13:42'),(109,116,1,0.6,'TOM',0,1,'X','2017-06-21 01:14:28','2017-06-21 01:14:28'),(110,117,1,0.6,'TOM',0,1,'X','2017-06-21 01:15:12','2017-06-21 01:15:12'),(111,119,1,0.6,'TOM',0,1,'X','2017-06-21 01:19:59','2017-06-21 01:19:59'),(112,120,1,0.6,'TOM',0,1,'X','2017-06-21 01:21:05','2017-06-21 01:21:05'),(113,121,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:02','2017-06-21 01:22:02'),(114,122,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:46','2017-06-21 01:22:46'),(115,123,1,0.6,'TOM',0,1,'X','2017-06-21 01:23:31','2017-06-21 01:23:31'),(116,124,1,0.6,'TOM',0,1,'X','2017-06-21 01:24:10','2017-06-21 01:24:10'),(118,126,1,0.3,'X',0,1,'0','2017-06-23 23:54:24','2017-06-23 23:54:24'),(119,127,1,0.3,'HEY',0,1,'X','2017-06-23 23:56:04','2017-06-23 23:56:04'),(120,128,1,0.3,'HEY',0,1,'X','2017-06-23 23:57:11','2017-06-23 23:57:11'),(121,129,1,0.3,'HEY',0,1,'X','2017-06-23 23:58:09','2017-06-23 23:58:09'),(124,132,1,0.3,'HEY',0,1,'X','2017-06-24 00:00:37','2017-06-24 00:00:37'),(125,133,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:08','2017-06-24 00:11:08'),(126,134,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:47','2017-06-24 00:11:47'),(127,135,1,0.3,'HEY',0,1,'X','2017-06-24 00:13:39','2017-06-24 00:13:39'),(128,136,1,0.3,'HEY',0,1,'X','2017-06-24 00:15:04','2017-06-24 00:15:04'),(129,137,1,0.3,'HEY',0,1,'X','2017-06-24 00:16:27','2017-06-24 00:16:27'),(130,138,1,0.3,'HEY',0,1,'X','2017-06-24 00:17:46','2017-06-24 00:17:46'),(131,139,1,0.3,'HEY',0,1,'X','2017-06-24 00:20:45','2017-06-24 00:20:45'),(132,140,1,0.3,'HEY',0,1,'X','2017-06-24 00:22:47','2017-06-24 00:22:47'),(133,141,1,0.3,'HEY',0,1,'X','2017-06-24 00:24:25','2017-06-24 00:24:25'),(134,143,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:08','2017-06-24 00:26:08'),(135,144,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:59','2017-06-24 00:26:59'),(136,145,1,0.3,'HEY',0,1,'X','2017-06-24 00:27:47','2017-06-24 00:27:47'),(137,147,1,0.3,'HEY',0,1,'X','2017-06-24 00:30:09','2017-06-24 00:30:09'),(138,148,1,0.3,'HEY',0,1,'X','2017-06-24 00:31:42','2017-06-24 00:31:42'),(139,149,1,0.3,'HEY',0,1,'X','2017-06-24 00:32:45','2017-06-24 00:32:45'),(140,150,1,0.3,'HEY',0,1,'X','2017-06-24 00:33:52','2017-06-24 00:33:52'),(141,151,1,0.3,'HEY',0,1,'X','2017-06-24 00:34:48','2017-06-24 00:34:48'),(142,152,1,0.3,'HEY',0,1,'X','2017-06-24 00:35:36','2017-06-24 00:35:36'),(143,153,1,0.3,'HEY',0,1,'X','2017-06-24 00:36:22','2017-06-24 00:36:22'),(144,154,1,0.3,'HEY',0.025,1,'X','2017-06-24 00:37:19','2017-07-24 20:21:27'),(150,166,2,1,'KICK',0,1,'x','2017-07-27 22:36:01','2017-12-03 03:28:37'),(151,167,2,1,'KICK',0,1,'x','2017-07-27 22:37:04','2017-12-03 03:28:45'),(158,174,2,1,'KICK',0,1,'x','2017-07-27 23:07:54','2017-12-03 03:28:15'),(160,176,2,1,'KICK',0,1,'x','2017-07-27 23:13:22','2017-12-03 03:27:51'),(171,190,1,1,'OOH',0,4,'C5','2017-12-14 08:12:26','2017-12-14 08:12:26'),(172,191,1,1,'OOH',0,2,'C5','2017-12-14 08:16:11','2017-12-14 08:16:11'),(174,193,1,1,'POING',0,3,'C4','2017-12-14 08:21:17','2017-12-14 08:21:17'),(176,195,1,1,'BOOM',0,4,'G1','2017-12-14 08:27:14','2017-12-14 08:27:14'),(177,196,1,1,'WAOW',0,4,'C2','2017-12-14 08:28:38','2017-12-14 08:28:38'),(178,197,1,0.8,'OOH',0,4,'A3','2017-12-14 08:30:46','2017-12-14 08:30:46'),(180,199,1,0.6,'DOING',0,1,'c4','2017-12-14 08:37:55','2017-12-14 08:37:55'),(181,200,1,0.6,'DONG',0,1,'C3','2017-12-14 08:39:20','2017-12-14 08:39:20'),(182,201,1,0.5,'TUNG',0,1,'C4','2017-12-14 08:45:28','2017-12-14 08:45:28'),(187,206,1,0.6,'PLONG',0,1,'c4','2017-12-14 09:27:31','2017-12-14 09:27:31'),(467,493,1,0.1,'CLAP',0,1,'D5','2018-02-09 21:42:37','2018-02-09 21:42:37'),(469,495,1,0,'HIHATOPEN',0,1,'Bb7','2018-03-06 06:12:09','2018-03-06 06:12:09'),(470,497,1,0,'HIHATCLOSED',0,1,'Bb7','2018-03-06 06:17:57','2018-03-06 06:17:57'),(471,497,1,0,'HIHATCLOSED',0,1,'Bb7','2018-03-06 06:17:58','2018-03-06 06:17:58'),(473,500,1,0,'HIHATOPEN',0,1,'D8','2018-03-06 06:24:15','2018-03-06 06:24:15'),(474,501,1,0,'TAMBOURINE',0,1,'F7','2018-03-06 06:27:34','2018-03-06 06:27:34'),(475,502,1,0,'SHAKER',0,1,'D8','2018-03-06 06:44:00','2018-03-06 06:44:00'),(476,503,1,0,'CLAVES',0,1,'F7','2018-03-06 06:46:48','2018-03-06 06:46:48'),(477,504,1,0,'CLAVES',0,1,'F7','2018-03-06 06:49:18','2018-03-06 06:49:18'),(478,505,1,0,'COWBELL',0,1,'F5','2018-03-06 06:52:23','2018-03-06 06:52:23'),(479,506,1,0,'COWBELL',0,1,'A4','2018-03-06 06:54:44','2018-03-06 06:54:44'),(480,507,1,0,'SNARERIM',0,1,'G4','2018-03-06 06:58:15','2018-03-06 06:58:15'),(481,508,1,0,'SNARERIM',0,1,'B3','2018-03-06 07:03:07','2018-03-06 07:03:07'),(482,509,1,0,'CLAP',0,1,'Eb5','2018-03-06 07:06:12','2018-03-06 07:06:12'),(483,510,1,0,'CLAP',0,1,'B8','2018-03-06 07:10:07','2018-03-06 07:10:07'),(484,511,1,0,'SNARE',0,1,'G3','2018-03-06 07:12:44','2018-03-06 07:12:44'),(485,512,1,0,'SNARE',0,1,'D7','2018-03-06 07:15:33','2018-03-06 07:15:33'),(486,513,1,0,'KICK',0,1,'C3','2018-03-06 07:18:57','2018-03-06 07:18:57'),(487,514,1,0,'SNARE',0,1,'F#3','2018-03-20 00:45:38','2018-03-20 00:45:38'),(488,515,1,0,'SNARE',0,1,'Bb7','2018-03-20 00:49:19','2018-03-20 00:49:19'),(489,516,1,0,'SNARE',0,1,'C#3','2018-03-20 00:52:46','2018-03-20 00:52:46'),(490,517,1,0,'SNARERIM',0,1,'D6','2018-03-20 00:55:23','2018-03-20 00:55:23'),(491,518,1,0,'KICK',0,1,'C2','2018-03-20 01:00:07','2018-03-20 01:00:07'),(492,519,1,0,'KICK',0,1,'F2','2018-03-20 01:02:29','2018-03-20 01:02:29'),(493,520,1,0,'KICK',0,1,'C3','2018-03-20 01:06:18','2018-03-20 01:06:18'),(494,521,1,0,'KICK',0,1,'B2','2018-03-20 01:14:40','2018-03-20 01:14:40'),(495,522,1,0,'KICK',0,1,'B','2018-03-20 01:18:24','2018-03-20 01:18:24'),(497,524,1,0,'KICK',0,1,'B8','2018-03-20 01:23:33','2018-03-20 01:23:33'),(498,525,1,0,'KICK',0,1,'G2','2018-03-20 01:25:35','2018-03-20 01:25:35'),(499,526,1,0,'TOMLOWMID',0,1,'Bb3','2018-03-20 01:29:22','2018-03-20 01:29:22'),(500,527,1,0,'TOMLOW',0,1,'E2','2018-03-20 01:32:27','2018-03-20 01:32:27'),(501,528,1,0,'TOMLOW',0,1,'D2','2018-03-20 01:38:54','2018-03-20 01:38:54'),(503,531,1,0,'TOMLOW',0,1,'Bb3','2018-03-20 01:47:17','2018-03-20 01:47:17'),(504,532,1,0,'TOMLOW',0,1,'F#4','2018-03-20 01:49:22','2018-03-20 01:49:22'),(505,533,1,0,'TOMLOW',0,1,'Eb5','2018-03-20 01:51:17','2018-03-20 01:51:17'),(506,534,1,0,'TOMLOWMID',0,1,'E2','2018-03-20 01:53:37','2018-03-20 01:53:37'),(507,536,1,0,'TOMLOW',0,1,'F#1','2018-03-20 01:57:55','2018-03-20 01:57:55'),(508,537,1,0,'TOMLOWMID',0,1,'B2','2018-03-20 01:59:57','2018-03-20 01:59:57'),(509,538,1,0,'TOMLOW',0,1,'Eb2','2018-03-20 02:01:40','2018-03-20 02:01:40'),(510,539,1,0,'TOMLOW',0,1,'C#8','2018-03-20 02:03:27','2018-03-20 02:03:27'),(511,540,1,0,'TOMLOW',0,1,'A2','2018-03-20 02:06:00','2018-03-20 02:06:00'),(512,541,1,0,'AGOGOHIGH',0,1,'A7','2018-03-20 02:08:39','2018-03-20 02:08:39'),(513,542,1,0,'AGOGOLOW',0,1,'G6','2018-03-20 02:10:19','2018-03-20 02:10:19'),(514,543,1,0,'AGOGOHIGH',0,1,'Eb5','2018-03-20 02:12:50','2018-03-20 02:12:50'),(515,544,1,0,'SHAKER',0,1,'F#8','2018-03-20 02:14:59','2018-03-20 02:14:59'),(516,545,1,0,'SHAKER',0,1,'E8','2018-03-20 02:16:29','2018-03-20 02:16:29'),(517,546,1,0,'SHAKER',0,1,'F#9','2018-03-20 02:17:59','2018-03-20 02:17:59'),(518,547,1,0,'SNARERIM',0,1,'B8','2018-03-20 02:20:00','2018-03-20 02:20:00'),(519,548,1,0,'KICK',0,1,'G3','2018-03-26 16:18:06','2018-03-26 16:18:06'),(520,549,1,0,'KICK',0,1,'F#3','2018-03-26 16:20:43','2018-03-26 16:20:43'),(521,550,1,0,'KICK',0,1,'F#2','2018-03-26 16:23:52','2018-03-26 16:23:52'),(522,551,1,0,'KICK',0,1,'F2','2018-03-26 16:25:53','2018-03-26 16:25:53'),(523,552,1,0,'KICK',0,1,'F2','2018-03-26 16:28:01','2018-03-26 16:28:01'),(524,553,1,0,'KICK',0,1,'F2','2018-03-26 16:30:00','2018-03-26 16:30:00'),(525,554,1,0,'KICK',0,1,'E2','2018-03-26 16:32:19','2018-03-26 16:32:19'),(526,555,1,0,'KICK',0,1,'B6','2018-03-26 16:34:07','2018-03-26 16:34:07'),(527,556,1,0,'SNARE',0,1,'C#3','2018-03-26 16:36:31','2018-03-26 16:36:31'),(528,557,1,0,'SNARE',0,1,'B2','2018-03-26 16:37:40','2018-03-26 16:37:40'),(529,558,1,0,'SNARE',0,1,'Eb4','2018-03-26 16:39:27','2018-03-26 16:39:27'),(530,559,1,0,'SNARE',0,1,'C#3','2018-03-26 16:41:08','2018-03-26 16:41:08'),(531,560,1,0,'HIHATCLOSED',0,1,'G4','2018-03-26 16:47:04','2018-03-26 16:47:04'),(532,561,1,0,'HIHATCLOSED',0,1,'D8','2018-03-26 16:49:54','2018-03-26 16:49:54'),(533,562,1,0,'HIHATCLOSED',0,1,'E5','2018-03-26 16:51:23','2018-03-26 16:51:23'),(534,563,1,0,'HIHATCLOSED',0,1,'E8','2018-03-26 16:53:11','2018-03-26 16:53:11'),(535,564,1,0,'HIHATCLOSED',0,1,'E8','2018-03-26 16:54:41','2018-03-26 16:54:41'),(536,565,1,0,'HIHATOPEN',0,1,'D8','2018-03-26 16:57:01','2018-03-26 16:57:01'),(537,566,1,0,'SNARE',0,1,'G7','2018-03-26 16:58:44','2018-03-26 16:58:44'),(538,567,1,0,'SNARE',0,1,'B8','2018-03-26 17:00:08','2018-03-26 17:00:08'),(539,568,1,0,'TOMLOW',0,1,'B2','2018-03-26 17:03:10','2018-03-26 17:03:10'),(540,569,1,0,'SNARE',0,1,'F#4','2018-03-26 17:04:51','2018-03-26 17:04:51'),(541,570,1,0,'CYMBALCRASH',0,1,'B7','2018-03-26 17:06:26','2018-03-26 17:06:26'),(542,571,1,0,'SNARE',0,1,'Bb7','2018-03-26 17:08:20','2018-03-26 17:08:20'),(543,572,1,0,'SNARE',0,1,'E2','2018-03-26 17:10:00','2018-03-26 17:10:00'),(544,573,1,0,'CYMBALCRASH',0,1,'Eb6','2018-03-26 17:11:35','2018-03-26 17:11:35'),(545,574,1,0,'SNARE',0,1,'C#4','2018-03-26 17:12:59','2018-03-26 17:12:59'),(546,575,1,0,'SNARE',0,1,'A3','2018-03-26 17:14:14','2018-03-26 17:14:14'),(547,576,1,0,'SNARE',0,1,'D9','2018-03-26 17:15:43','2018-03-26 17:15:43'),(548,577,1,0,'SNARE',0,1,'A6','2018-03-26 17:17:08','2018-03-26 17:17:08'),(549,578,1,0,'TOMHIGH',0,1,'E3','2018-03-26 17:18:58','2018-03-26 17:18:58'),(550,579,1,0,'TOMHIGHMID',0,1,'C3','2018-03-26 17:20:23','2018-03-26 17:20:23'),(551,580,1,0,'TOMLOW',0,1,'A2','2018-03-26 17:21:39','2018-03-26 17:21:39'),(552,581,1,0,'CABASA',0,1,'B7','2018-03-27 21:18:48','2018-03-27 21:18:48'),(553,582,1,0,'CLAP',0,1,'B8','2018-03-27 21:20:32','2018-03-27 21:20:32'),(554,583,1,0,'CLAVE',0,1,'A6','2018-03-27 21:22:00','2018-03-27 21:22:00'),(555,584,1,0,'GONG',0,1,'B2','2018-03-27 21:23:57','2018-03-27 21:23:57'),(556,585,1,0,'KICK',0,1,'Ab2','2018-03-27 21:26:15','2018-03-27 21:26:15'),(557,586,1,0,'SNARE',0,1,'E4','2018-03-27 21:27:34','2018-03-27 21:27:34'),(558,588,1,0,'CLAVE',0,1,'A3','2018-03-27 21:31:56','2018-03-27 21:31:56'),(559,589,1,0,'CLAVE',0,1,'Eb6','2018-03-27 21:33:19','2018-03-27 21:33:19'),(560,590,1,0,'SHAKER',0,1,'F#8','2018-03-27 21:35:48','2018-03-27 21:35:48'),(561,591,1,0,'HIHATCLOSED',0,1,'E6','2018-03-27 21:37:17','2018-03-27 21:37:17'),(562,592,1,0,'HIHATCLOSED',0,1,'F#8','2018-03-27 21:38:41','2018-03-27 21:38:41'),(563,593,1,0,'SNARE',0,1,'D8','2018-03-27 21:40:07','2018-03-27 21:40:07'),(564,595,1,0,'SNARE',0,1,'F2','2018-03-27 21:44:40','2018-03-27 21:44:40'),(565,597,1,0,'SNARE',0,1,'D8','2018-03-27 21:48:19','2018-03-27 21:48:19'),(566,598,1,0,'TOMHIGH',0,1,'Eb2','2018-03-27 21:50:12','2018-03-27 21:50:12'),(567,599,1,0,'TOMHIGHMID',0,1,'A7','2018-03-27 21:52:19','2018-03-27 21:52:19'),(568,600,1,0,'TOMHIGH',0,1,'A7','2018-03-27 21:54:39','2018-03-27 21:54:39'),(569,601,1,0,'KICK',0,1,'Db4','2018-03-27 21:56:28','2018-03-27 21:56:28'),(570,602,1,0,'CYMBALCRASH',0,1,'D6','2018-03-27 21:58:00','2018-03-27 21:58:00'),(572,604,1,0,'KICK',0,1,'A2','2018-08-28 20:19:43','2018-08-28 20:19:43'),(573,605,1,0,'KICK',0,1,'Bb1','2018-08-28 20:21:48','2018-08-28 20:21:48'),(575,607,1,0,'KICK',0,1,'F#2','2018-08-28 20:25:26','2018-08-28 20:25:26'),(576,608,1,0,'KICK',0,1,'F#2','2018-08-28 20:26:52','2018-08-28 20:26:52'),(577,609,1,0,'KICK',0,1,'A#1','2018-08-28 20:28:32','2018-08-28 20:28:32'),(578,610,1,0,'KICK',0,1,'A2','2018-08-28 20:29:57','2018-08-28 20:29:57'),(579,611,1,0,'KICK',0,1,'B2','2018-08-28 20:32:08','2018-08-28 20:32:08'),(580,614,1,0,'KICK',0,1,'G2','2018-08-28 20:39:12','2018-08-28 20:39:12'),(581,613,1,0,'KICK',0,1,'G2','2018-08-28 20:40:54','2018-08-28 20:40:54'),(583,615,1,0,'KICK',0,1,'D#2','2018-08-28 20:47:01','2018-08-28 20:47:01'),(584,616,1,0,'KICK',0,1,'F3','2018-08-28 20:48:37','2018-08-28 20:48:37'),(585,617,1,0,'KICK',0,1,'D2','2018-08-28 20:49:52','2018-08-28 20:49:52'),(586,618,1,0,'KICK',0,1,'D#2','2018-08-28 20:51:21','2018-08-28 20:51:21'),(587,619,1,0,'KICK',0,1,'D2','2018-08-28 20:53:26','2018-08-28 20:53:26'),(588,620,1,0,'KICK',0,1,'D#2','2018-08-28 20:55:09','2018-08-28 20:55:09'),(589,621,1,0,'KICK',0,1,'F2','2018-08-28 20:59:43','2018-08-28 20:59:43'),(590,622,1,0,'KICK',0,1,'F#2','2018-08-28 21:01:21','2018-08-28 21:01:21'),(591,623,1,0,'KICK',0,1,'G2','2018-08-28 21:02:39','2018-08-28 21:02:39'),(592,624,1,0,'KICK',0,1,'B1','2018-08-28 21:04:43','2018-08-28 21:04:43'),(593,625,1,0,'KICK',0,1,'D2','2018-08-28 21:06:07','2018-08-28 21:06:07'),(594,626,1,0,'KICK',0,1,'C3','2018-08-28 21:07:36','2018-08-28 21:07:36'),(595,627,1,0,'KICK',0,1,'F#3','2018-08-28 21:09:02','2018-08-28 21:09:02'),(596,628,1,0,'KICK',0,1,'A#2','2018-08-28 21:17:23','2018-08-28 21:17:23'),(597,629,1,0,'KICK',0,1,'G2','2018-08-28 21:19:32','2018-08-28 21:19:32'),(598,630,1,0,'KICK',0,1,'D2','2018-08-28 21:21:05','2018-08-28 21:21:05'),(599,631,1,0,'KICK',0,1,'E2','2018-08-28 21:22:50','2018-08-28 21:22:50'),(600,632,1,0,'KICK',0,1,'F#2','2018-08-28 21:24:02','2018-08-28 21:24:02'),(601,633,1,0,'KICK',0,1,'A#1','2018-08-28 21:58:17','2018-08-28 21:58:17'),(602,634,1,0,'KICK',0,1,'B1','2018-08-28 21:59:53','2018-08-28 21:59:53'),(603,635,1,0,'KICK',0,1,'G2','2018-08-28 22:01:25','2018-08-28 22:01:25'),(605,637,1,0,'KICK',0,1,'F#2','2018-08-28 23:33:24','2018-08-28 23:33:24'),(606,638,1,0,'KICK',0,1,'F2','2018-08-28 23:40:02','2018-08-28 23:40:02'),(607,639,1,0,'KICK',0,1,'F2','2018-08-28 23:42:51','2018-08-28 23:42:51'),(608,640,1,0,'KICK',0,1,'B2','2018-08-28 23:45:37','2018-08-28 23:45:37'),(609,641,1,0,'SNARE',0,1,'A#7','2018-08-29 00:05:12','2018-08-29 00:05:12'),(610,642,1,0,'SNARE',0,1,'F3','2018-08-29 00:07:48','2018-08-29 00:07:48'),(611,643,1,0,'SNARE',0,1,'F#8','2018-08-29 00:09:47','2018-08-29 00:09:47'),(612,644,1,0,'SNARE',0,1,'F#8','2018-08-29 00:11:09','2018-08-29 00:11:09'),(613,645,1,0,'SNARE',0,1,'E8','2018-08-29 00:13:10','2018-08-29 00:13:10'),(614,646,1,0,'SNARE',0,1,'D#4','2018-08-29 00:14:57','2018-08-29 00:14:57'),(615,647,1,0,'SNARE',0,1,'D#4','2018-08-29 00:26:05','2018-08-29 00:26:05'),(616,648,1,0,'SNARE',0,1,'F#8','2018-08-29 00:27:23','2018-08-29 00:27:23'),(617,649,1,0,'SNARE',0,1,'C7','2018-08-29 00:29:41','2018-08-29 00:29:41'),(618,650,1,0,'SNARE',0,1,'D7','2018-08-29 00:36:45','2018-08-29 00:36:45'),(621,653,1,0,'SNARE',0,1,'A7','2018-08-29 00:47:15','2018-08-29 00:47:15'),(622,654,1,0,'SNARE',0,1,'A7','2018-08-29 00:48:59','2018-08-29 00:48:59'),(623,655,1,0,'SNARE',0,1,'B7','2018-08-29 00:50:55','2018-08-29 00:50:55'),(624,656,1,0,'SNARE',0,1,'A7','2018-08-29 00:52:34','2018-08-29 00:52:34'),(625,657,1,0,'SNARE',0,1,'D3','2018-08-29 01:06:49','2018-08-29 01:06:49'),(626,658,1,0,'SNARE',0,1,'D8','2018-08-29 01:08:38','2018-08-29 01:08:38'),(627,659,1,0,'SNARE',0,1,'F#8','2018-08-29 02:40:50','2018-08-29 02:40:50'),(628,660,1,0,'SNARE',0,1,'A7','2018-08-29 02:43:11','2018-08-29 02:43:11'),(629,661,1,0,'SNARE',0,1,'E8','2018-08-29 02:44:18','2018-08-29 02:44:18'),(630,662,1,0,'SNARE',0,1,'D#4','2018-08-29 02:45:32','2018-08-29 02:45:32'),(631,663,1,0,'SNARE',0,1,'F#3','2018-08-29 02:46:47','2018-08-29 02:46:47'),(632,664,1,0,'SNARE',0,1,'C7','2018-08-29 02:49:05','2018-08-29 02:49:05'),(633,665,1,0,'SNARE',0,1,'E6','2018-08-29 02:50:22','2018-08-29 02:50:22'),(634,666,1,0,'SNARE',0,1,'A7','2018-08-29 02:51:40','2018-08-29 02:51:40'),(635,667,1,0,'SNARE',0,1,'A7','2018-08-29 02:52:39','2018-08-29 02:52:39'),(636,668,1,0,'SNARE',0,1,'A7','2018-08-29 02:53:55','2018-08-29 02:53:55'),(637,669,1,0,'SNARE',0,1,'A3','2018-08-29 02:55:08','2018-08-29 02:55:08'),(638,671,1,0,'SNARE',0,1,'D#7','2018-08-29 03:04:23','2018-08-29 03:04:23'),(639,672,1,0,'SNARE',0,1,'E8','2018-08-29 03:06:14','2018-08-29 03:06:14'),(640,673,1,0,'SNARE',0,1,'E8','2018-08-29 03:07:54','2018-08-29 03:07:54'),(641,674,1,0,'SNARE',0,1,'E8','2018-08-29 03:09:52','2018-08-29 03:09:52'),(642,675,1,0,'SNARE',0,1,'E8','2018-08-29 03:12:11','2018-08-29 03:12:11'),(643,676,1,0,'SNARE',0,1,'D#4','2018-08-29 03:13:23','2018-08-29 03:13:23'),(644,677,1,0,'SNARE',0,1,'F#8','2018-08-29 03:14:38','2018-08-29 03:14:38'),(645,678,1,0,'SNARE',0,1,'G7','2018-08-29 03:15:55','2018-08-29 03:15:55'),(646,679,1,0,'SNARE',0,1,'F#7','2018-08-29 03:17:08','2018-08-29 03:17:08'),(647,680,1,0,'SNARE',0,1,'A7','2018-08-29 03:18:21','2018-08-29 03:18:21'),(648,681,1,0,'SNARE',0,1,'G7','2018-08-29 03:19:22','2018-08-29 03:19:22'),(649,682,1,0,'SNARE',0,1,'G7','2018-08-29 03:20:18','2018-08-29 03:20:18'),(650,683,1,0,'SNARE',0,1,'A#7','2018-08-29 03:21:27','2018-08-29 03:21:27'),(651,684,1,0,'SNARE',0,1,'B7','2018-08-29 03:23:21','2018-08-29 03:23:21'),(652,685,1,0,'SNARE',0,1,'E8','2018-08-29 03:24:52','2018-08-29 03:24:52'),(653,686,1,0,'SNARE',0,1,'B8','2018-08-29 03:26:37','2018-08-29 03:26:37'),(654,687,1,0,'SNARE',0,1,'F#8','2018-08-29 03:27:54','2018-08-29 03:27:54'),(655,688,1,0,'SNARE',0,1,'E8','2018-08-29 03:29:00','2018-08-29 03:29:00'),(656,689,1,0,'TOMHI',0,1,'E8','2018-08-29 04:30:40','2018-08-29 04:30:40'),(657,690,1,0,'TOMMID',0,1,'F#8','2018-08-29 04:42:58','2018-08-29 04:42:58'),(658,691,1,0,'TOMLO',0,1,'F#8','2018-08-29 04:44:46','2018-08-29 04:45:00'),(659,692,1,0,'TOMMID',0,1,'C#3','2018-08-29 04:46:34','2018-08-29 04:46:34'),(660,693,1,0,'TOMMID',0,1,'D3','2018-08-29 04:47:55','2018-08-29 04:47:55'),(661,694,1,0,'TOMMID',0,1,'A8','2018-08-29 04:49:29','2018-08-29 04:49:29'),(662,695,1,0,'TOMMID',0,1,'F#8','2018-08-29 04:50:45','2018-08-29 04:50:45'),(663,696,1,0,'TOMMID',0,1,'F#8','2018-08-29 04:51:57','2018-08-29 04:51:57'),(664,697,1,0,'TOMMID',0,1,'B8','2018-08-29 04:53:12','2018-08-29 04:53:12'),(665,698,1,0,'TOMMID',0,1,'D3','2018-08-29 04:54:37','2018-08-29 04:54:37'),(666,699,1,0,'KICK',0,1,'A1','2018-09-20 01:37:57','2018-09-20 01:37:57'),(667,700,1,0,'KICK',0,1,'A2','2018-09-20 01:39:20','2018-09-20 01:39:20'),(668,701,1,0,'KICK',0,1,'F2','2018-09-20 01:40:42','2018-09-20 01:40:42'),(669,702,1,0,'KICK',0,1,'Bb2','2018-09-20 01:42:05','2018-09-20 01:42:05'),(670,703,1,0,'KICK',0,1,'G2','2018-09-20 01:43:13','2018-09-20 01:43:13'),(671,704,1,0,'KICK',0,1,'Eb2','2018-09-20 01:44:36','2018-09-20 01:44:36'),(672,705,1,0,'KICK',0,1,'G2','2018-09-20 01:45:47','2018-09-20 01:45:47'),(673,706,1,0,'KICK',0,1,'E2','2018-09-20 01:47:06','2018-09-20 01:47:06'),(674,707,1,0,'KICK',0,1,'D2','2018-09-20 01:48:12','2018-09-20 01:48:12'),(675,708,1,0,'KICK',0,1,'C2','2018-09-20 01:49:28','2018-09-20 01:49:28'),(676,709,1,0,'SNARE',0,1,'Eb3','2018-09-20 01:52:16','2018-09-20 01:52:16'),(677,710,1,0,'SNARE',0,1,'F3','2018-09-20 01:53:24','2018-09-20 01:53:24'),(678,711,1,0,'SNARE',0,1,'B7','2018-09-20 01:54:43','2018-09-20 01:54:43'),(679,712,1,0,'SNARE',0,1,'Db4','2018-09-20 01:55:43','2018-09-20 01:55:43'),(680,713,1,0,'SNARE',0,1,'E4','2018-09-20 01:57:10','2018-09-20 01:57:10'),(681,714,1,0,'SNARE',0,1,'E3','2018-09-20 01:59:18','2018-09-20 01:59:18'),(682,715,1,0,'SNARE',0,1,'E3','2018-09-20 02:00:29','2018-09-20 02:00:29'),(683,716,1,0,'SNARE',0,1,'Ab3','2018-09-20 02:01:34','2018-09-20 02:01:34'),(684,717,1,0,'SNARE',0,1,'B8','2018-09-20 02:04:07','2018-09-20 02:04:07'),(685,718,1,0,'SNARE',0,1,'A3','2018-09-20 02:05:34','2018-09-20 02:05:34'),(686,719,1,0,'SNARE',0,1,'B7','2018-09-20 02:07:09','2018-09-20 02:07:09'),(687,720,1,0,'HATCLOSED',0,1,'F#9','2018-09-20 02:09:59','2018-09-20 02:10:32'),(689,721,1,0,'HIHATCLOSED',0,1,'D9','2018-09-20 02:15:46','2018-09-20 02:15:46'),(690,722,1,0,'HIHATCLOSED',0,1,'F#9','2018-09-20 02:17:04','2018-09-20 02:17:04'),(691,723,1,0,'HIHATCLOSED',0,1,'B8','2018-09-20 02:20:43','2018-09-20 02:20:43'),(692,724,1,0,'HIHATCLOSED',0,1,'A8','2018-09-20 02:22:09','2018-09-20 02:22:09'),(693,725,1,0,'HIHATCLOSED',0,1,'F#9','2018-09-20 02:24:18','2018-09-20 02:24:18'),(694,726,1,0,'HIHATCLOSED',0,1,'G#5','2018-09-20 02:25:52','2018-09-20 02:25:52'),(695,727,1,0,'HIHATCLOSED',0,1,'G4','2018-09-20 02:27:40','2018-09-20 02:27:40'),(696,729,1,0.6,'TOMHIGH',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(697,728,1,0.6,'TOMLOW',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(698,730,1,0.6,'TOMHIGH',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(699,731,1,0.6,'TOM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(700,732,1,0.6,'TOM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(701,733,1,0.3,'STICKSIDE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(702,734,1,0.3,'STICKSIDE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(703,735,1,0.3,'STICKSIDE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(704,736,1,0.1,'SNARERIM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(705,737,1,0.1,'SNARERIM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(706,738,1,0.1,'SNARERIM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(707,739,1,0.1,'SNARERIM',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(708,740,1,0.1,'SNARE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(709,741,1,0.1,'SNARE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(710,742,1,0.1,'SNARE',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(711,743,1,1,'KICKLONG',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(712,744,1,1,'KICK',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(713,745,1,1,'KICK',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(714,746,1,1,'KICK',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(715,747,1,0.1,'HIHATOPEN',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(716,748,1,0.1,'HIHATOPEN',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(717,749,1,0.1,'HIHATOPEN',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(718,750,1,0.1,'HIHATCLOSED',0,1,'X','2018-10-06 21:09:42','2018-10-06 21:09:42'),(719,751,1,0.1,'HIHATCLOSED',0,1,'X','2018-10-06 21:09:42','2018-10-06 21:09:42'),(720,752,1,0.1,'HIHATCLOSED',0,1,'X','2018-10-06 21:09:42','2018-10-06 21:09:42'),(721,753,1,0,'CYMBALCRASH',0,4,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(722,754,1,0,'CYMBALCRASH',0,4,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(723,755,1,0,'CYMBALCRASH',0,4,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(724,756,1,0.2,'COWBELL',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(725,757,1,0.6,'CONGAHIGH',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(726,758,1,0.6,'CONGA',0,1,'x','2018-10-06 21:09:42','2018-10-06 21:09:42'),(727,760,1,0.1,'CLAP',0,1,'x','2018-10-06 21:09:43','2018-10-06 21:09:43'),(728,761,1,1,'TOM',0,1,'D3','2018-10-06 21:09:51','2018-10-06 21:09:51'),(729,768,1,0,'SNARE',0,1,'E3','2018-10-06 21:09:52','2018-10-06 21:09:52'),(730,765,1,1,'TOM',0,1,'Eb3','2018-10-06 21:09:52','2018-10-06 21:09:52'),(731,767,1,0,'SNARE',0,1,'C5','2018-10-06 21:09:52','2018-10-06 21:09:52'),(732,764,1,1,'TOM',0,1,'Eb3','2018-10-06 21:09:52','2018-10-06 21:09:52'),(733,766,1,0,'SNARE',0,1,'Db4','2018-10-06 21:09:52','2018-10-06 21:09:52'),(734,763,1,1,'TOM',0,1,'Eb3','2018-10-06 21:09:52','2018-10-06 21:09:52'),(735,762,1,1,'TOM',0,1,'D3','2018-10-06 21:09:52','2018-10-06 21:09:52'),(736,769,1,0,'SNARE',0,1,'Bb5','2018-10-06 21:09:52','2018-10-06 21:09:52'),(737,770,1,0,'SNARE',0,1,'E4','2018-10-06 21:09:52','2018-10-06 21:09:52'),(738,771,1,1,'KICK',0,1,'Bb4','2018-10-06 21:09:52','2018-10-06 21:09:52'),(739,772,1,1,'KICK',0,1,'Eb2','2018-10-06 21:09:52','2018-10-06 21:09:52'),(740,773,1,0,'HIHATOPEN',0,0.5,'Eb8','2018-10-06 21:09:52','2018-10-06 21:09:52'),(741,774,1,0,'HIHATOPEN',0,0.5,'Eb8','2018-10-06 21:09:52','2018-10-06 21:09:52'),(742,775,1,0,'HIHATOPEN',0,0.5,'Eb8','2018-10-06 21:09:52','2018-10-06 21:09:52'),(743,776,1,0,'HIHATOPEN',0,0.5,'Eb8','2018-10-06 21:09:52','2018-10-06 21:09:52'),(744,777,1,0,'HIHATCLOSED',0,0.5,'Bb7','2018-10-06 21:09:52','2018-10-06 21:09:52'),(745,779,1,0,'HIHATCLOSED',0,0.5,'Ab7','2018-10-06 21:09:52','2018-10-06 21:09:52'),(746,778,1,0,'HIHATCLOSED',0,0.5,'B7','2018-10-06 21:09:52','2018-10-06 21:09:52'),(747,780,1,0,'HIHATCLOSED',0,0.5,'D4','2018-10-06 21:09:52','2018-10-06 21:09:52'),(748,781,1,0,'HIHATCLOSED',0,0.5,'Ab6','2018-10-06 21:09:52','2018-10-06 21:09:52'),(749,782,1,0,'CYMBALCRASH',0,4,'B7','2018-10-06 21:09:52','2018-10-06 21:09:52'),(750,783,1,0,'CYMBALCRASH',0,4,'Eb8','2018-10-06 21:09:52','2018-10-06 21:09:52'),(751,784,1,0.6,'TOMHIGH',0,1,'x','2018-10-06 21:10:00','2018-10-06 21:10:00'),(752,785,1,1,'TOMHIGH',0,0.2,'X','2018-10-06 21:10:00','2018-10-06 21:10:00'),(753,786,1,0.6,'TOM',0,1,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(754,788,0.6,0.2,'SNARERIM',0,0.014,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(755,787,0.7,0.6,'TOM',0,0.35,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(756,789,1,0.4,'SNARE',0,0.091,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(757,790,1,0.1,'MARACAS',0,1,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(758,791,0.8,0.6,'MARACAS',0.01,0.015,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(759,792,1,1,'KICKLONG',0,1,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(760,793,1,1,'KICKLONG',0.02,0.5,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(761,796,0.5,0.1,'HIHATOPEN',0,0.59,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(762,795,1,1,'KICK',0.03,0.5,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(763,794,1,1,'KICK',0,1,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(764,797,1,0.1,'HIHATCLOSED',0.02,0.1,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(765,798,1,0,'CYMBALCRASH',0,4,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(766,799,1,0,'CYMBALCRASH',0,4,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(767,800,1,0.5,'COWBELL',0,0.3,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(768,801,1,1,'CONGAHIGH',0,0.2,'x','2018-10-06 21:10:01','2018-10-06 21:10:01'),(769,802,0.8,0.9,'CONGA',0,0.2,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(770,803,0.8,0.8,'CLAVES',0,0.05,'X','2018-10-06 21:10:01','2018-10-06 21:10:01'),(771,804,1,0.1,'CLAP',0,1,'x','2018-10-06 21:10:02','2018-10-06 21:10:02'),(772,805,1,0.1,'CLAP',0,1,'x','2018-10-06 21:10:02','2018-10-06 21:10:02'),(773,806,0.8,0.3,'CLAP',0,0.3,'x','2018-10-06 21:10:02','2018-10-06 21:10:02'),(774,807,1,1,'TOM',0,1,'Ab2','2018-10-06 21:10:12','2018-10-06 21:10:12'),(775,808,1,1,'TOM',0,1,'Db3','2018-10-06 21:10:12','2018-10-06 21:10:12'),(776,809,1,1,'TOM',0,1,'D3','2018-10-06 21:10:12','2018-10-06 21:10:12'),(777,810,1,0,'SNARE',0,1,'g8','2018-10-06 21:10:12','2018-10-06 21:10:12'),(778,811,1,0,'SNARE',0,1,'f3','2018-10-06 21:10:12','2018-10-06 21:10:12'),(779,812,1,0,'SNARE',0,1,'B','2018-10-06 21:10:12','2018-10-06 21:10:12'),(780,814,1,0,'SNARE',0,1,'Bb3','2018-10-06 21:10:12','2018-10-06 21:10:12'),(781,815,1,1,'KICK',0,1,'Eb2','2018-10-06 21:10:12','2018-10-06 21:10:12'),(782,816,1,1,'KICK',0,1,'f8','2018-10-06 21:10:12','2018-10-06 21:10:12'),(783,817,1,1,'KICK',0,1,'Eb3','2018-10-06 21:10:12','2018-10-06 21:10:12'),(784,818,1,0,'HIHATOPEN',0,0.5,'Bb8','2018-10-06 21:10:12','2018-10-06 21:10:12'),(785,819,1,0,'HIHATOPEN',0,0.5,'c5','2018-10-06 21:10:12','2018-10-06 21:10:12'),(786,820,1,0,'HIHATOPEN',0,0.5,'e4','2018-10-06 21:10:12','2018-10-06 21:10:12'),(787,821,1,0,'HIHATOPEN',0,1,'e4','2018-10-06 21:10:12','2018-10-06 21:10:12'),(788,822,1,0,'HIHATCLOSED',0,0.5,'Bb8','2018-10-06 21:10:12','2018-10-06 21:10:12'),(789,823,1,0,'HIHATCLOSED',0,0.5,'g8','2018-10-06 21:10:12','2018-10-06 21:10:12'),(790,824,1,0,'HIHATCLOSED',0,0.5,'C5','2018-10-06 21:10:12','2018-10-06 21:10:12'),(791,825,1,0,'HIHATCLOSED',0,0.5,'Gb4','2018-10-06 21:10:13','2018-10-06 21:10:13'),(792,826,1,0,'CYMBALCRASH',0,4,'E5','2018-10-06 21:10:13','2018-10-06 21:10:13'),(793,827,1,0.3,'KICK',0,2,'Db3','2018-10-06 21:10:36','2018-10-06 21:10:36'),(794,828,1,0,'SNARE',0,1,'F8','2018-10-06 21:10:36','2018-10-06 21:10:36'),(795,829,1,0,'SNARE',0,1,'Eb4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(796,830,1,0,'SNARE',0,1,'B5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(797,831,1,0,'SNARE',0,1,'C6','2018-10-06 21:10:37','2018-10-06 21:10:37'),(798,832,1,0,'SNARE',0,1,'Db4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(799,833,1,0.6,'PLONG',0,1,'c4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(800,835,1,0.6,'DOING',0,1,'c4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(801,834,1,0.5,'TUNG',0,1,'C4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(802,836,1,0.6,'DONG',0,1,'C3','2018-10-06 21:10:37','2018-10-06 21:10:37'),(803,837,1,1,'KICK',0,1,'Ab2','2018-10-06 21:10:37','2018-10-06 21:10:37'),(804,838,1,1,'KICK',0,1,'C2','2018-10-06 21:10:37','2018-10-06 21:10:37'),(805,839,1,1,'KICK',0,1,'C2','2018-10-06 21:10:37','2018-10-06 21:10:37'),(806,840,1,1,'KICK',0,1,'Db2','2018-10-06 21:10:37','2018-10-06 21:10:37'),(807,841,1,0.8,'POOM',0,2,'C4','2018-10-06 21:10:37','2018-10-06 21:10:37'),(808,843,1,0,'HIHATOPEN',0,0.5,'Eb3','2018-10-06 21:10:37','2018-10-06 21:10:37'),(809,842,1,0,'HIHATOPEN',0,0.5,'D3','2018-10-06 21:10:37','2018-10-06 21:10:37'),(810,844,1,0,'HIHATCLOSED',0,0.5,'Ab3','2018-10-06 21:10:37','2018-10-06 21:10:37'),(811,845,1,0,'HIHATCLOSED',0,0.5,'Eb5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(812,846,1,1,'TOM',0,1,'G5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(813,848,1,1,'TOM',0,2,'B3','2018-10-06 21:10:37','2018-10-06 21:10:37'),(814,847,1,1,'TOM',0,1,'D5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(815,849,1,0,'CYMBALCRASH',0,4,'C5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(816,850,1,1,'TOM',0,1,'Gb5','2018-10-06 21:10:37','2018-10-06 21:10:37'),(817,851,1,1,'TOMLOW',0,1,'e2','2018-10-06 21:10:44','2018-10-06 21:10:44'),(818,852,1,1,'TOMLOW',0,1,'G2','2018-10-06 21:10:45','2018-10-06 21:10:45'),(819,853,1,1,'TOMLOW',0,1,'A2','2018-10-06 21:10:45','2018-10-06 21:10:45'),(820,854,1,1,'TOMHIGH',0,1,'E3','2018-10-06 21:10:45','2018-10-06 21:10:45'),(821,855,1,1,'TOMHIGH',0,1,'F3','2018-10-06 21:10:45','2018-10-06 21:10:45'),(822,856,1,1,'TOMHIGH',0,1,'E3','2018-10-06 21:10:45','2018-10-06 21:10:45'),(823,857,1,0,'SNARE',0,1,'Db4','2018-10-06 21:10:45','2018-10-06 21:10:45'),(824,859,1,0,'SNARE',0,1,'Db44','2018-10-06 21:10:45','2018-10-06 21:10:45'),(825,860,1,1,'KICK',0,1,'Bb1','2018-10-06 21:10:45','2018-10-06 21:10:45'),(826,858,1,0,'SNARE',0,1,'f9','2018-10-06 21:10:45','2018-10-06 21:10:45'),(827,861,1,1,'KICK',0,1,'A1','2018-10-06 21:10:45','2018-10-06 21:10:45'),(828,862,1,1,'KICK',0,1,'Ab1','2018-10-06 21:10:45','2018-10-06 21:10:45'),(829,864,1,0,'HIHATOPEN',0,0.5,'E7','2018-10-06 21:10:45','2018-10-06 21:10:45'),(830,863,1,1,'KICK',0,1,'Bb1','2018-10-06 21:10:45','2018-10-06 21:10:45'),(831,865,1,0,'HIHATOPEN',0,0.5,'e7','2018-10-06 21:10:45','2018-10-06 21:10:45'),(832,866,1,0,'HIHATOPEN',0,0.5,'G8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(833,867,1,0,'HIHATOPEN',0,0.5,'g8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(834,868,1,0,'HIHATCLOSED',0,0.5,'G8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(835,869,1,0,'HIHATCLOSED',0,0.5,'Bb8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(836,870,1,0,'HIHATCLOSED',0,0.5,'G8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(837,871,1,0,'HIHATCLOSED',0,0.5,'G8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(838,872,1,0,'CYMBALCRASH',0,1,'Eb8','2018-10-06 21:10:45','2018-10-06 21:10:45'),(839,874,1,0,'KICK',0,1,'D9','2018-10-08 06:33:43','2018-10-08 06:33:43'),(840,873,1,0,'SWEEP',0,1,'B5','2018-10-08 06:34:33','2018-10-08 06:34:33'),(841,875,1,0,'KICK',0,1,'F#8','2018-10-08 06:35:58','2018-10-08 06:35:58'),(842,876,1,0,'SNARE',0,1,'E7','2018-10-08 06:37:10','2018-10-08 06:37:10'),(843,877,1,0,'SNARE',0,1,'Eb3','2018-10-08 06:39:01','2018-10-08 06:39:01'),(844,878,1,0,'SNARE',0,1,'B3','2018-10-08 06:40:48','2018-10-08 06:40:48'),(845,879,1,0,'SHAKER',0,1,'F6','2018-10-08 06:42:49','2018-10-08 06:42:49'),(846,880,1,0,'RIMCLICK',0,1,'Bb5','2018-10-08 06:44:37','2018-10-08 06:44:37'),(847,881,1,0,'SHAKER',0,1,'D#6','2018-10-08 06:48:50','2018-10-08 06:48:50'),(848,882,1,0,'SNARE',0,1,'A#5','2018-10-08 06:51:06','2018-10-08 06:51:06'),(849,883,1,0,'SNARE',0,1,'A#3','2018-10-08 06:53:18','2018-10-08 06:53:18'),(850,885,1,0,'CRASHCYMBAL',0,1,'B6','2018-10-08 06:59:30','2018-10-08 06:59:30'),(851,886,1,0,'SNARE',0,1,'A3','2018-10-08 07:01:09','2018-10-08 07:01:09'),(852,887,1,0,'SNARE',0,1,'E8','2018-10-08 07:02:15','2018-10-08 07:02:15'),(853,888,1,0,'SNARE',0,1,'E8','2018-10-08 07:03:40','2018-10-08 07:03:40'),(855,890,1,0,'SNARE',0,1,'B4','2018-10-08 07:06:19','2018-10-08 07:06:19'),(856,891,1,0,'HIHATCLOSED',0,1,'B9','2018-10-08 07:07:44','2018-10-08 07:07:44'),(857,892,1,0,'HIHATCLOSED',0,1,'B9','2018-10-08 07:09:06','2018-10-08 07:09:06'),(858,893,1,0,'SHAKER',0,1,'F#9','2018-10-08 07:10:48','2018-10-08 07:10:48'),(859,894,1,0,'HIHATOPEN',0,1,'C4','2018-10-08 07:13:08','2018-10-08 07:13:08'),(860,895,1,0,'HIHATOPEN',0,1,'G7','2018-10-08 07:16:39','2018-10-08 07:16:39'),(861,896,1,0,'HIHATOPEN',0,1,'F5','2018-10-08 07:17:48','2018-10-08 07:17:48'),(862,897,1,0,'HIHATOPEN',0,1,'A#5','2018-10-08 07:20:13','2018-10-08 07:20:13'),(863,898,1,0,'HIHATCLOSED',0,1,'A8','2018-10-08 07:22:42','2018-10-08 07:22:42'),(864,899,1,0,'CLAVE',0,1,'Ab4','2018-10-08 07:24:12','2018-10-08 07:24:12'),(865,900,1,0,'TOMHI',0,1,'F#8','2018-10-08 07:27:36','2018-10-08 07:27:57'),(866,901,1,0,'TRIANGLE',0,1,'F#4','2018-10-08 07:29:20','2018-10-08 07:29:20'),(867,902,1,0,'TOMMID',0,1,'F#8','2018-10-08 07:30:25','2018-10-08 07:30:25'),(868,903,1,0,'CRASHCYMBAL',0,1,'E7','2018-10-08 07:32:10','2018-10-08 07:32:10'),(869,904,1,0,'TOMMID',0,1,'D#3','2018-10-08 07:33:42','2018-10-08 07:33:42'),(870,905,1,0,'SWEEP',0,1,'G#3','2018-10-08 07:35:13','2018-10-08 07:35:13'),(871,906,1,0,'TOMMID',0,1,'F#8','2018-10-08 07:37:11','2018-10-08 07:37:11'),(872,907,1,0,'TOMMID',0,1,'E8','2018-10-08 07:38:39','2018-10-08 07:38:39'),(873,908,1,0,'KICK',0,1,'B8','2018-10-08 07:40:19','2018-10-08 07:40:19'),(874,909,1,0,'KICK',0,1,'D#2','2018-10-08 07:41:24','2018-10-08 07:41:24');
/*!40000 ALTER TABLE `audio_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument`
--

LOCK TABLES `instrument` WRITE;
/*!40000 ALTER TABLE `instrument` DISABLE KEYS */;
INSERT INTO `instrument` VALUES (3,1,1,'percussive','Electronic',0.8,'2017-04-21 16:33:55','2017-06-16 02:19:40'),(4,1,1,'percussive','Acoustic',0.5,'2017-06-15 22:32:29','2017-06-15 22:32:29'),(5,1,1,'percussive','Pots & Pans',0.76,'2017-06-20 23:02:25','2017-07-27 17:12:15'),(7,1,3,'Harmonic','Wind Flute Note',0.2,'2017-12-14 08:10:02','2017-12-14 08:10:02'),(8,1,3,'Harmonic','Water Sitar Harmony',0.35,'2017-12-14 08:19:43','2017-12-14 08:31:27'),(9,1,3,'Harmonic','Earth Bass Harmony',0.4,'2017-12-14 08:25:57','2017-12-14 08:31:37'),(10,1,3,'Harmonic','Water Whale Harmony',0.4,'2017-12-14 08:29:25','2017-12-14 08:29:25'),(12,1,3,'Harmonic','Fire String Hits',0.5,'2017-12-14 08:36:41','2017-12-23 23:30:20'),(25,3,4,'Percussive','Flammy Clap',0.5,'2018-02-09 21:04:25','2018-02-09 21:04:25'),(27,3,3,'Percussive','Water Small',0.6,'2018-03-06 06:04:31','2018-10-06 21:18:30'),(28,3,3,'Percussive','Earth Small',0.6,'2018-03-20 00:34:47','2018-10-06 21:18:23'),(29,3,3,'Percussive','Fire Small',0.6,'2018-03-26 16:13:37','2018-10-06 21:18:15'),(30,3,3,'Percussive','Wind Small',0.6,'2018-03-27 21:16:43','2018-10-06 21:18:47'),(31,3,3,'Percussive','Water Big',0.6,'2018-08-28 20:09:41','2018-08-28 20:09:41'),(32,3,3,'Percussive','Earth Big',0.6,'2018-09-20 01:22:05','2018-09-20 01:22:05'),(33,1,1,'Percussive','Earth A (legacy)',0.618,'2018-10-06 21:09:41','2018-10-06 21:09:41'),(34,1,1,'Percussive','Earth B (legacy)',0.618,'2018-10-06 21:09:51','2018-10-06 21:09:51'),(35,1,1,'Percussive','Fire A  (legacy)',0.618,'2018-10-06 21:10:00','2018-10-06 21:10:00'),(36,1,1,'Percussive','Water B (legacy)',0.618,'2018-10-06 21:10:12','2018-10-06 21:10:12'),(37,1,1,'Percussive','Water Basic X',0.6,'2018-10-06 21:10:25','2018-10-06 21:10:25'),(38,1,1,'Percussive','Wind A (legacy)',0.618,'2018-10-06 21:10:36','2018-10-06 21:10:36'),(39,1,1,'Percussive','Wind B (legacy)',0.618,'2018-10-06 21:10:44','2018-10-06 21:10:44'),(40,3,3,'Percussive','Wind Big',0.6,'2018-10-07 23:46:36','2018-10-07 23:46:36'),(41,3,3,'Percussive','Fire Big',0.6,'2018-10-08 04:43:47','2018-10-08 04:43:47');
/*!40000 ALTER TABLE `instrument` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument_meme`
--

LOCK TABLES `instrument_meme` WRITE;
/*!40000 ALTER TABLE `instrument_meme` DISABLE KEYS */;
INSERT INTO `instrument_meme` VALUES (2,3,'Classic','2017-04-23 23:13:29','2017-04-23 23:13:29'),(3,3,'Deep','2017-04-23 23:13:33','2017-04-23 23:13:33'),(4,3,'Acid','2017-04-23 23:13:36','2017-04-23 23:13:36'),(6,3,'Tech','2017-04-23 23:13:41','2017-04-23 23:13:41'),(7,3,'Electro','2017-04-23 23:13:43','2017-04-23 23:13:43'),(10,3,'Cool','2017-04-23 23:20:57','2017-04-23 23:20:57'),(11,3,'Hard','2017-04-23 23:20:59','2017-04-23 23:20:59'),(13,3,'Progressive','2017-04-23 23:23:43','2017-04-23 23:23:43'),(14,4,'Classic','2017-06-15 22:59:20','2017-06-15 22:59:20'),(16,4,'Tropical','2017-06-15 22:59:32','2017-06-15 22:59:32'),(17,4,'Hot','2017-06-15 22:59:35','2017-06-15 22:59:35'),(19,4,'Easy','2017-06-15 22:59:43','2017-06-15 22:59:43'),(20,4,'Progressive','2017-06-15 22:59:46','2017-06-15 22:59:46'),(21,5,'Classic','2017-06-21 01:25:37','2017-06-21 01:25:37'),(22,5,'Deep','2017-06-21 01:25:41','2017-06-21 01:25:41'),(23,5,'Hard','2017-06-21 01:25:58','2017-06-21 01:25:58'),(27,4,'Deep','2017-06-21 01:40:43','2017-06-21 01:40:43'),(28,4,'Hard','2017-06-21 01:40:56','2017-06-21 01:40:56'),(30,5,'Hot','2017-06-24 01:38:58','2017-06-24 01:38:58'),(31,5,'Cool','2017-06-24 01:39:02','2017-06-24 01:39:02'),(36,7,'Wind','2017-12-14 08:10:06','2017-12-14 08:10:06'),(37,8,'Water','2017-12-14 08:19:48','2017-12-14 08:19:48'),(38,9,'Earth','2017-12-14 08:26:02','2017-12-14 08:26:02'),(39,10,'Water','2017-12-14 08:29:31','2017-12-14 08:29:31'),(85,27,'Water','2018-03-16 00:57:19','2018-03-16 00:57:19'),(86,28,'Earth','2018-08-10 16:48:37','2018-08-10 16:48:37'),(87,28,'Small','2018-08-10 16:49:01','2018-08-10 16:49:01'),(88,29,'Fire','2018-08-10 16:49:44','2018-08-10 16:49:44'),(89,29,'Small','2018-08-10 16:49:48','2018-08-10 16:49:48'),(90,27,'Small','2018-08-10 16:50:12','2018-08-10 16:50:12'),(91,30,'Wind','2018-08-10 16:50:19','2018-08-10 16:50:19'),(92,30,'Small','2018-08-10 16:50:21','2018-08-10 16:50:21'),(93,33,'Earth','2018-10-06 21:09:41','2018-10-06 21:09:41'),(94,34,'Earth','2018-10-06 21:09:51','2018-10-06 21:09:51'),(95,35,'Fire','2018-10-06 21:10:00','2018-10-06 21:10:00'),(96,36,'Water','2018-10-06 21:10:12','2018-10-06 21:10:12'),(97,38,'Wind','2018-10-06 21:10:36','2018-10-06 21:10:36'),(98,39,'Wind','2018-10-06 21:10:44','2018-10-06 21:10:44'),(99,32,'Earth','2018-10-06 21:16:12','2018-10-06 21:16:12'),(100,32,'Big','2018-10-06 21:16:27','2018-10-06 21:16:27'),(101,31,'Water','2018-10-06 21:16:35','2018-10-06 21:16:35'),(102,31,'Big','2018-10-06 21:16:38','2018-10-06 21:16:38');
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
INSERT INTO `sequence` VALUES (6,1,1,'Rhythm','2-Step Shuffle Beat',0.62,'C',133,'2017-04-23 23:21:52','2018-02-03 00:56:45','Published'),(7,1,1,'Macro','Deep, from Hot to Cool',0.6,'C',133,'2017-05-01 18:59:22','2018-02-03 00:56:45','Published'),(8,1,1,'Macro','Deep, from Cool to Hot',0.6,'G minor',133,'2017-05-01 18:59:32','2018-02-03 00:56:45','Published'),(9,1,1,'Main','I\'ll House You',0.5,'C',133,'2017-05-13 00:04:19','2018-02-03 00:56:45','Published'),(11,27,3,'Main','Water Galq',0.5,'E-',121,'2017-12-12 22:05:13','2018-03-28 05:44:46','Published'),(12,1,3,'Macro','Earth to Fire',0.5,'Ebm',121,'2017-12-13 06:38:26','2018-02-03 00:56:45','Published'),(13,1,3,'Macro','Earth to Water',0.5,'Gm',121,'2017-12-13 06:40:22','2018-02-03 00:56:45','Published'),(14,1,3,'Macro','Earth to Wind',0.5,'Cm',121,'2017-12-13 06:42:39','2018-02-03 00:56:45','Published'),(15,1,3,'Macro','Fire to Earth',0.5,'G',121,'2017-12-13 06:43:43','2018-02-03 00:56:45','Published'),(16,1,3,'Macro','Fire to Water',0.5,'E',121,'2017-12-13 06:45:43','2018-02-03 00:56:45','Published'),(17,1,3,'Macro','Fire to Wind',0.5,'G',121,'2017-12-13 06:46:56','2018-02-03 00:56:45','Published'),(18,1,3,'Macro','Wind to Earth',0.5,'Ebm',121,'2017-12-13 06:48:06','2018-02-03 00:56:45','Published'),(19,1,3,'Macro','Wind to Fire',0.5,'Bm',121,'2017-12-13 06:49:09','2018-02-03 00:56:45','Published'),(20,1,3,'Macro','Wind to Water',0.5,'Ebm',121,'2017-12-13 06:49:56','2018-02-03 00:56:45','Published'),(29,1,3,'Rhythm','2-Step Shuffle',0.62,'C',121,'2017-12-22 06:43:19','2018-02-03 00:56:45','Published'),(30,1,3,'Macro','Water to Wind',0.5,'G',121,'2017-12-23 22:11:19','2018-02-03 00:56:45','Published'),(31,1,3,'Macro','Water to Fire',0.5,'C',121,'2017-12-23 22:12:58','2018-02-03 00:56:45','Published'),(32,1,3,'Macro','Water to Earth',0.5,'G',121,'2017-12-23 22:14:36','2018-02-03 00:56:45','Published'),(34,1,3,'Rhythm','Half-time 2-Step Shuffle',0.62,'C',121,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published'),(35,27,3,'Main','Fire Camp',0.6,'C',121,'2018-01-19 20:51:14','2018-03-28 05:38:23','Published'),(47,27,3,'Main','Earth First',0.5,'Bb',121,'2018-02-25 07:46:29','2018-02-25 07:46:29','Published'),(48,27,3,'Main','Fire Babes',0.5,'E-',121,'2018-03-02 06:35:24','2018-03-02 06:35:24','Published'),(49,27,3,'Main','Water Me Up',0.5,'F',121,'2018-03-02 07:00:10','2018-03-02 07:00:10','Published'),(50,27,3,'Main','Wind Terb',0.5,'D-',121,'2018-03-02 07:21:04','2018-03-02 07:21:04','Published'),(52,27,3,'Main','Earth Rudy',0.5,'D',121,'2018-03-02 07:34:58','2018-03-02 07:34:58','Published'),(53,27,3,'Main','Wind Bagz',0.4,'Eb',121,'2018-03-19 01:31:40','2018-03-28 05:16:51','Published'),(54,27,3,'Main','Wind Mole',0.4,'F',121,'2018-03-19 01:31:44','2018-03-19 01:31:44','Published'),(55,27,3,'Main','Bert',0.5,'B',121,'2018-03-22 19:15:11','2018-03-22 19:15:11','Published'),(56,27,3,'Main','Bert',0.5,'B',121,'2018-03-22 19:15:43','2018-03-22 19:15:43','Published'),(57,27,3,'Main','Water Wibs',0.6,'C#-',121,'2018-03-22 19:26:28','2018-03-23 04:23:27','Published'),(59,27,3,'Main','Earthen Satay',0.5,'Db',121,'2018-03-22 19:48:06','2018-03-23 04:37:07','Published'),(60,27,3,'Main','Fire Tom Perez',0.5,'C-',121,'2018-03-22 19:50:57','2018-03-28 04:53:27','Published'),(61,27,3,'Main','Earth Earth',0.5,'C',121,'2018-03-22 19:50:57','2018-06-05 03:32:57','Published'),(62,27,3,'Main','Wind Wind',0.5,'F',121,'2018-03-22 19:50:58','2018-06-05 03:41:09','Published'),(63,27,3,'Main','Temporary',0.5,'C',121,'2018-03-22 19:50:59','2018-03-22 19:50:59','Published'),(64,27,3,'Main','Fire Fire',0.5,'C#-',121,'2018-03-22 19:50:59','2018-06-14 03:04:20','Published'),(65,27,3,'Main','Water Water',0.5,'C#',121,'2018-03-22 19:51:00','2018-06-05 03:54:32','Published'),(66,27,3,'Main','Earth Knyght',0.5,'Bb-',121,'2018-03-22 19:51:00','2018-03-23 04:29:01','Published'),(70,1,3,'Rhythm','Shuffle ½ density',0.62,'C',121,'2018-08-21 18:19:01','2018-10-06 23:19:05','Published');
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sequence_meme`
--

LOCK TABLES `sequence_meme` WRITE;
/*!40000 ALTER TABLE `sequence_meme` DISABLE KEYS */;
INSERT INTO `sequence_meme` VALUES (1,6,'Classic','2017-04-23 23:22:21','2017-04-23 23:22:21'),(2,6,'Deep','2017-04-23 23:22:23','2017-04-23 23:22:23'),(3,6,'Acid','2017-04-23 23:22:24','2017-04-23 23:22:24'),(5,6,'Tech','2017-04-23 23:22:28','2017-04-23 23:22:28'),(6,6,'Electro','2017-04-23 23:22:31','2017-04-23 23:22:31'),(7,6,'Tropical','2017-04-23 23:22:34','2017-04-23 23:22:34'),(8,6,'Hot','2017-04-23 23:22:36','2017-04-23 23:22:36'),(9,6,'Cool','2017-04-23 23:22:39','2017-04-23 23:22:39'),(10,6,'Hard','2017-04-23 23:22:40','2017-04-23 23:22:40'),(11,6,'Easy','2017-04-23 23:22:42','2017-04-23 23:22:42'),(12,6,'Progressive','2017-04-23 23:23:17','2017-04-23 23:23:17'),(15,7,'Deep','2017-05-01 18:59:46','2017-05-01 18:59:46'),(16,8,'Deep','2017-05-01 19:42:36','2017-05-01 19:42:36'),(17,9,'Deep','2017-05-13 00:04:41','2017-05-13 00:04:41'),(18,9,'Classic','2017-05-13 00:04:44','2017-05-13 00:04:44'),(34,9,'Hard','2017-06-16 04:26:57','2017-06-16 04:26:57'),(36,11,'Earth','2017-12-13 00:44:05','2017-12-13 00:44:05'),(68,35,'Earth','2018-01-19 20:56:22','2018-01-19 20:56:22'),(72,47,'Earth','2018-03-18 21:30:31','2018-03-18 21:30:31'),(73,70,'Wind','2018-08-21 18:19:06','2018-08-21 18:19:06'),(75,29,'Wind','2018-08-21 18:19:17','2018-08-21 18:19:17'),(76,29,'Fire','2018-08-21 18:19:21','2018-08-21 18:19:21'),(78,34,'Wind','2018-08-21 18:19:39','2018-08-21 18:19:39'),(79,34,'Earth','2018-08-21 18:19:42','2018-08-21 18:19:42'),(81,70,'Water','2018-10-06 23:24:35','2018-10-06 23:24:35'),(82,70,'Earth','2018-10-06 23:24:41','2018-10-06 23:24:41'),(83,34,'Fire','2018-10-06 23:25:04','2018-10-06 23:25:04'),(84,29,'Water','2018-10-06 23:25:17','2018-10-06 23:25:17');
/*!40000 ALTER TABLE `sequence_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern`
--

LOCK TABLES `pattern` WRITE;
/*!40000 ALTER TABLE `pattern` DISABLE KEYS */;
INSERT INTO `pattern` VALUES (3,'Loop',6,'drop d beet',0,4,NULL,NULL,NULL,'2017-04-23 23:44:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(4,'Macro',7,'from Hot',0,0,0.7,'C',133,'2017-05-01 19:39:59','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(5,'Macro',7,'to Cool',1,0,0.5,'Bb Minor',133,'2017-05-01 19:40:18','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(6,'Macro',8,'from Cool',0,0,0.5,'G minor',133,'2017-05-01 19:43:06','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(7,'Macro',8,'to Hot',1,0,0.7,'C',133,'2017-05-01 19:43:26','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(8,'Main',9,'Drop',0,32,0.4,'C',133,'2017-05-13 00:05:29','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(9,'Main',9,'Breakdown A',1,16,0.6,'G minor',133,'2017-05-13 00:07:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(14,'Main',9,'Breakdown B',2,16,0.8,'G minor',133,'2017-07-27 17:40:32','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(21,'Macro',12,'Passion Volcano',0,0,NULL,'Ebm',NULL,'2017-12-13 06:39:29','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(29,'Macro',16,'Volcanic Island',0,0,NULL,'E',NULL,'2017-12-13 06:46:14','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(30,'Macro',16,'Sex on the Beach',1,0,NULL,'Am',NULL,'2017-12-13 06:46:37','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(31,'Macro',17,'Smoke in the Air',0,0,NULL,'G',NULL,'2017-12-13 06:47:25','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(32,'Macro',17,'Dreams',1,0,NULL,'E',NULL,'2017-12-13 06:47:43','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(33,'Macro',18,'Open Road Tumbleweed',0,0,NULL,'Ebm',NULL,'2017-12-13 06:48:26','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(34,'Macro',18,'Rolling Stone',1,0,NULL,'D',NULL,'2017-12-13 06:48:48','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(35,'Macro',19,'Stoke the Flames',0,0,NULL,'Bm',NULL,'2017-12-13 06:49:21','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(64,'Loop',29,'Loop A',0,4,NULL,NULL,NULL,'2017-12-22 06:43:19','2018-09-07 19:18:34','Published',4,4,0),(65,'Macro',12,'Exploding',2,0,NULL,'B',NULL,'2017-12-23 21:50:57','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(66,'Macro',13,'Arrival',2,0,NULL,'F',NULL,'2017-12-23 21:54:53','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(68,'Macro',15,'Defeat',2,0,NULL,'Am',NULL,'2017-12-23 22:00:15','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(69,'Macro',16,'Glory',2,0,NULL,'F',NULL,'2017-12-23 22:02:07','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(70,'Macro',17,'Waking',2,0,NULL,'Am',NULL,'2017-12-23 22:04:27','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(71,'Macro',18,'Freedom',2,0,NULL,'Bm',NULL,'2017-12-23 22:05:48','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(72,'Macro',19,'Wilderness',2,0,NULL,'A',NULL,'2017-12-23 22:08:59','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(73,'Macro',20,'Afloat',2,0,NULL,'A',NULL,'2017-12-23 22:10:24','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(74,'Macro',30,'Rain',0,0,NULL,'G',NULL,'2017-12-23 22:11:35','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(75,'Macro',30,'Fog',1,0,NULL,'C',NULL,'2017-12-23 22:12:04','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(76,'Macro',30,'Dew',2,0,NULL,'Am',NULL,'2017-12-23 22:12:28','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(77,'Macro',31,'Hydrant',0,0,NULL,'C',NULL,'2017-12-23 22:13:12','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(78,'Macro',31,'Engine',1,0,NULL,'Dm',NULL,'2017-12-23 22:13:32','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(79,'Macro',31,'Steam',2,0,NULL,'C',NULL,'2017-12-23 22:13:50','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(80,'Macro',32,'Irrigation',0,0,NULL,'G',NULL,'2017-12-23 22:14:53','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(81,'Macro',32,'Nourishment',1,0,NULL,'C',NULL,'2017-12-23 22:15:19','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(82,'Macro',32,'Growth',2,0,NULL,'Am',NULL,'2017-12-23 22:15:42','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(86,'Loop',29,'Loop B',0,4,NULL,NULL,NULL,'2018-01-05 07:39:54','2018-10-06 22:49:25','Published',4,4,0),(87,'Outro',29,'Outro A',0,4,NULL,NULL,NULL,'2018-01-05 08:37:43','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(88,'Outro',29,'Outro B',0,4,NULL,NULL,NULL,'2018-01-05 08:38:44','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(91,'Intro',29,'Intro A',0,4,NULL,NULL,NULL,'2018-01-05 09:43:57','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(92,'Intro',29,'Intro B',0,4,NULL,NULL,NULL,'2018-01-05 10:04:13','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(99,'Loop',34,'Loop A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(100,'Loop',34,'Loop B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(101,'Outro',34,'Outro A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(102,'Outro',34,'Outro B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(103,'Intro',34,'Intro A',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(104,'Intro',34,'Intro B',0,4,NULL,NULL,NULL,'2018-01-05 14:54:11','2018-02-03 00:56:45','Published',NULL,NULL,NULL),(180,'Main',47,'Verse',0,16,NULL,NULL,NULL,'2018-02-25 07:48:50','2018-02-25 07:49:02','Published',NULL,NULL,NULL),(181,'Main',47,'Chorus',1,32,NULL,NULL,NULL,'2018-02-25 07:55:39','2018-02-25 07:55:39','Published',NULL,NULL,NULL),(182,'Main',47,'Interlude',2,8,NULL,NULL,NULL,'2018-02-25 08:00:47','2018-02-25 08:00:47','Published',NULL,NULL,NULL),(183,'Main',48,'Verse',0,16,NULL,NULL,NULL,'2018-03-02 06:36:51','2018-03-02 06:36:51','Published',NULL,NULL,NULL),(184,'Main',48,'Prechorus',1,32,NULL,NULL,NULL,'2018-03-02 06:39:44','2018-03-02 06:48:15','Published',NULL,NULL,NULL),(185,'Main',48,'Chorus',2,16,NULL,NULL,NULL,'2018-03-02 06:50:11','2018-03-02 06:50:11','Published',NULL,NULL,NULL),(186,'Main',48,'Bridge',3,32,NULL,NULL,NULL,'2018-03-02 06:53:35','2018-03-02 06:53:35','Published',NULL,NULL,NULL),(187,'Main',49,'A',0,16,NULL,NULL,NULL,'2018-03-02 07:01:49','2018-03-02 07:01:49','Published',NULL,NULL,NULL),(188,'Main',49,'B',1,32,NULL,NULL,NULL,'2018-03-02 07:04:34','2018-03-02 07:04:34','Published',NULL,NULL,NULL),(189,'Main',49,'C',2,8,NULL,NULL,NULL,'2018-03-02 07:13:38','2018-03-02 07:13:38','Published',NULL,NULL,NULL),(190,'Main',50,'Intro',0,64,NULL,NULL,NULL,'2018-03-02 07:22:43','2018-03-02 07:22:43','Published',NULL,NULL,NULL),(191,'Main',50,'A',1,16,NULL,NULL,NULL,'2018-03-02 07:25:38','2018-03-02 07:25:38','Published',NULL,NULL,NULL),(192,'Main',50,'B',2,32,NULL,NULL,NULL,'2018-03-02 07:28:02','2018-03-02 07:29:29','Published',NULL,NULL,NULL),(193,'Main',50,'Interlude',3,64,NULL,NULL,NULL,'2018-03-02 07:32:02','2018-03-02 07:32:02','Published',NULL,NULL,NULL),(194,'Main',52,'A',0,32,NULL,'D',NULL,'2018-03-19 01:11:16','2018-03-19 01:11:16','Published',NULL,NULL,NULL),(195,'Main',52,'B',1,32,NULL,NULL,NULL,'2018-03-19 01:20:17','2018-03-19 01:24:27','Published',NULL,NULL,NULL),(196,'Main',52,'C',2,32,NULL,NULL,NULL,'2018-03-19 01:25:28','2018-03-19 01:25:48','Published',NULL,NULL,NULL),(197,'Main',57,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:24:41','2018-03-23 04:24:41','Published',NULL,NULL,NULL),(198,'Main',57,'B',1,16,NULL,NULL,NULL,'2018-03-23 04:25:53','2018-03-23 04:25:53','Published',NULL,NULL,NULL),(199,'Main',66,'I',0,32,NULL,NULL,NULL,'2018-03-23 04:30:25','2018-03-23 04:30:25','Published',NULL,NULL,NULL),(200,'Main',66,'A',1,64,NULL,NULL,NULL,'2018-03-23 04:30:56','2018-03-23 04:30:56','Published',NULL,NULL,NULL),(201,'Main',59,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:37:54','2018-03-23 04:37:54','Published',NULL,NULL,NULL),(202,'Main',59,'B',1,32,NULL,NULL,NULL,'2018-03-23 04:38:37','2018-03-23 04:38:37','Published',NULL,NULL,NULL),(203,'Main',59,'C',2,16,NULL,NULL,NULL,'2018-03-23 04:41:10','2018-03-23 04:41:10','Published',NULL,NULL,NULL),(204,'Main',54,'A',0,16,NULL,NULL,NULL,'2018-03-23 04:45:59','2018-03-23 04:45:59','Published',NULL,NULL,NULL),(205,'Main',54,'B',1,16,NULL,NULL,NULL,'2018-03-23 04:49:50','2018-03-23 04:51:05','Published',NULL,NULL,NULL),(206,'Main',60,'A',0,32,NULL,NULL,NULL,'2018-03-28 04:55:06','2018-03-28 04:55:06','Published',NULL,NULL,NULL),(207,'Main',60,'B',1,16,NULL,NULL,NULL,'2018-03-28 05:03:48','2018-03-28 05:03:48','Published',NULL,NULL,NULL),(208,'Main',53,'A',0,32,NULL,NULL,NULL,'2018-03-28 05:18:04','2018-03-28 05:18:04','Published',NULL,NULL,NULL),(209,'Main',53,'B',1,32,NULL,NULL,NULL,'2018-03-28 05:21:09','2018-03-28 05:21:09','Published',NULL,NULL,NULL),(210,'Main',35,'A',0,16,NULL,NULL,NULL,'2018-03-28 05:32:19','2018-03-28 05:32:19','Published',NULL,NULL,NULL),(211,'Main',35,'B',1,32,NULL,NULL,NULL,'2018-03-28 05:35:23','2018-03-28 05:35:23','Published',NULL,NULL,NULL),(212,'Main',11,'A',0,12,NULL,NULL,NULL,'2018-03-28 05:45:53','2018-03-28 05:45:53','Published',NULL,NULL,NULL),(213,'Main',11,'B',1,12,NULL,NULL,NULL,'2018-03-28 05:52:35','2018-03-28 05:52:35','Published',NULL,NULL,NULL),(214,'Main',11,'X',2,16,NULL,NULL,NULL,'2018-03-28 05:58:54','2018-03-28 05:58:54','Published',NULL,NULL,NULL),(215,'Main',61,'A',0,32,NULL,NULL,NULL,'2018-06-05 03:34:53','2018-06-05 03:34:53','Published',NULL,NULL,NULL),(216,'Main',62,'A',0,64,NULL,NULL,NULL,'2018-06-05 03:41:34','2018-06-05 03:41:34','Published',NULL,NULL,NULL),(217,'Main',65,'A',0,32,NULL,NULL,NULL,'2018-06-05 03:59:09','2018-06-05 03:59:09','Published',NULL,NULL,NULL),(218,'Main',64,'A',0,32,NULL,NULL,NULL,'2018-06-14 03:07:47','2018-06-14 03:27:53','Published',NULL,NULL,NULL),(252,'Loop',29,'Loop C',0,4,NULL,NULL,NULL,'2018-08-10 23:21:55','2018-08-10 23:21:55','Published',NULL,NULL,NULL),(253,'Loop',34,'Loop C',0,4,NULL,NULL,NULL,'2018-08-10 23:23:26','2018-08-10 23:23:26','Published',NULL,NULL,NULL),(254,'Loop',70,'Loop A',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:14:42','Published',4,4,0),(255,'Loop',70,'Loop B',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:15:29','Published',4,4,0),(256,'Outro',70,'Outro A',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:16:02','Published',4,4,0),(257,'Outro',70,'Outro B',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:16:38','Published',4,4,0),(258,'Intro',70,'Intro A',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:17:27','Published',4,4,0),(259,'Intro',70,'Intro B',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:18:01','Published',4,4,0),(260,'Loop',70,'Loop C',0,4,NULL,NULL,NULL,'2018-08-21 18:19:02','2018-10-06 23:18:32','Published',4,4,0),(261,'Intro',29,'Intro C',0,4,NULL,NULL,NULL,'2018-10-06 23:01:29','2018-10-06 23:01:29','Published',4,4,0);
/*!40000 ALTER TABLE `pattern` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_chord`
--

LOCK TABLES `pattern_chord` WRITE;
/*!40000 ALTER TABLE `pattern_chord` DISABLE KEYS */;
INSERT INTO `pattern_chord` VALUES (7,3,'C',0,'2017-04-23 23:44:43','2017-04-23 23:44:43'),(8,8,'C major 7',0,'2017-05-13 00:05:58','2017-06-16 03:54:30'),(9,8,'Cm7',8,'2017-05-13 00:06:11','2017-06-16 03:55:17'),(10,8,'F7',12,'2017-05-13 00:06:28','2017-06-16 03:58:00'),(11,8,'Bb major 7',16,'2017-05-13 00:06:41','2017-06-16 03:58:13'),(12,9,'D',0,'2017-05-13 00:07:40','2017-06-16 04:00:16'),(13,9,'G',4,'2017-05-13 00:07:47','2017-06-16 04:00:22'),(14,9,'C',8,'2017-05-13 00:07:55','2017-06-16 04:00:31'),(15,9,'F7',12,'2017-05-13 00:08:01','2017-06-16 04:31:20'),(19,8,'Bb m7',24,'2017-06-16 03:59:02','2017-06-16 03:59:02'),(20,8,'Eb7',28,'2017-06-16 03:59:38','2017-06-16 03:59:38'),(21,8,'Ab major 7',30,'2017-06-16 03:59:46','2017-06-16 03:59:46'),(32,14,'E minor 7',0,'2017-07-27 17:41:02','2017-07-27 17:41:02'),(33,14,'Eb minor 7',4,'2017-07-27 17:41:11','2017-07-27 17:41:11'),(38,14,'D minor 7',8,'2017-07-30 23:22:08','2017-07-30 23:22:08'),(39,14,'Db minor 7',12,'2017-07-30 23:22:15','2017-07-30 23:22:15'),(364,64,'C',0,'2017-12-22 06:43:19','2017-12-22 06:43:19'),(368,86,'C',0,'2018-01-05 07:39:55','2018-01-05 07:39:55'),(369,87,'C',0,'2018-01-05 08:37:43','2018-01-05 08:37:43'),(370,88,'C',0,'2018-01-05 08:38:44','2018-01-05 08:38:44'),(372,91,'C',0,'2018-01-05 09:43:58','2018-01-05 09:43:58'),(373,92,'C',0,'2018-01-05 10:04:13','2018-01-05 10:04:13'),(380,99,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(381,100,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(382,101,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(383,102,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(384,103,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(385,104,'C',0,'2018-01-05 14:54:11','2018-01-05 14:54:11'),(1107,180,'Bb',0,'2018-02-25 07:51:08','2018-02-25 07:51:08'),(1108,180,'D-',4,'2018-02-25 07:51:23','2018-02-25 07:51:23'),(1109,180,'Eb',8,'2018-02-25 07:51:38','2018-02-25 07:51:38'),(1110,180,'Bb',16,'2018-02-25 07:52:39','2018-02-25 07:52:39'),(1111,180,'G-',20,'2018-02-25 07:52:54','2018-02-25 07:52:54'),(1112,180,'Eb',24,'2018-02-25 07:53:43','2018-02-25 07:53:43'),(1113,181,'F',0,'2018-02-25 07:55:58','2018-02-25 07:55:58'),(1114,181,'G-',4,'2018-02-25 07:56:24','2018-02-25 07:56:24'),(1115,181,'Eb',8,'2018-02-25 07:56:56','2018-02-25 07:57:27'),(1116,181,'Bb',12,'2018-02-25 07:57:49','2018-02-25 07:57:49'),(1117,181,'C-',16,'2018-02-25 07:58:08','2018-02-25 07:58:08'),(1118,181,'G-',20,'2018-02-25 07:58:27','2018-02-25 07:58:27'),(1119,181,'Eb',24,'2018-02-25 07:58:37','2018-02-25 07:58:37'),(1120,182,'D-',0,'2018-02-25 08:01:09','2018-02-25 08:01:09'),(1121,182,'Fsus4',1.5,'2018-02-25 08:02:08','2018-03-02 06:33:19'),(1122,183,'E-',0,'2018-03-02 06:37:10','2018-03-02 06:37:10'),(1123,183,'G',8,'2018-03-02 06:37:25','2018-03-02 06:37:25'),(1124,183,'A',12,'2018-03-02 06:38:12','2018-03-02 06:38:12'),(1125,184,'A-',0,'2018-03-02 06:41:00','2018-03-02 06:41:00'),(1126,184,'Bsus4',4,'2018-03-02 06:41:29','2018-03-02 06:41:29'),(1127,184,'C',8,'2018-03-02 06:42:27','2018-03-02 06:42:27'),(1128,184,'G',12,'2018-03-02 06:42:36','2018-03-02 06:42:36'),(1129,184,'F',16,'2018-03-02 06:45:50','2018-03-02 06:45:50'),(1130,184,'C/D',24,'2018-03-02 06:46:26','2018-03-02 06:46:26'),(1131,185,'E-',0,'2018-03-02 06:50:21','2018-03-02 06:50:21'),(1132,185,'C',4,'2018-03-02 06:51:06','2018-03-02 06:51:06'),(1133,185,'A-',8,'2018-03-02 06:51:13','2018-03-02 06:51:13'),(1134,185,'G',12,'2018-03-02 06:51:30','2018-03-02 06:51:30'),(1135,185,'D',14,'2018-03-02 06:51:36','2018-03-02 06:51:36'),(1136,186,'F',0,'2018-03-02 06:53:53','2018-03-02 06:53:53'),(1137,186,'A-',8,'2018-03-02 06:54:21','2018-03-02 06:54:21'),(1138,186,'D-',16,'2018-03-02 06:54:29','2018-03-02 06:54:50'),(1139,186,'C/G',24,'2018-03-02 06:56:22','2018-03-02 06:56:22'),(1140,187,'Fmaj7',0,'2018-03-02 07:02:02','2018-03-02 07:02:02'),(1141,187,'Ebmaj7',4,'2018-03-02 07:02:13','2018-03-02 07:02:13'),(1142,188,'Bb-7',0,'2018-03-02 07:06:34','2018-03-02 07:06:34'),(1143,188,'Gbmaj7',4,'2018-03-02 07:06:45','2018-03-02 07:06:45'),(1144,188,'Eb-7',12,'2018-03-02 07:07:07','2018-03-02 07:07:07'),(1145,188,'Bb-7',16,'2018-03-02 07:08:40','2018-03-02 07:08:40'),(1146,188,'Gbmaj7',20,'2018-03-02 07:08:51','2018-03-02 07:08:51'),(1147,188,'Gbmaj7/Ab',28,'2018-03-02 07:09:17','2018-03-02 07:09:17'),(1148,189,'Fmaj7',0,'2018-03-02 07:13:49','2018-03-02 07:13:49'),(1149,189,'Dbmaj7',3.5,'2018-03-02 07:14:42','2018-03-02 07:14:42'),(1150,189,'Bb-7',7.5,'2018-03-02 07:15:41','2018-03-02 07:15:41'),(1151,189,'Gbmaj7',11.5,'2018-03-02 07:16:09','2018-03-02 07:16:09'),(1152,190,'NC',0,'2018-03-02 07:22:54','2018-03-02 07:22:54'),(1153,191,'D-',0,'2018-03-02 07:25:57','2018-03-02 07:25:57'),(1154,191,'D-/G',3,'2018-03-02 07:26:09','2018-03-02 07:26:09'),(1155,191,'D-/F',11,'2018-03-02 07:26:28','2018-03-02 07:26:28'),(1156,192,'D-/C',0,'2018-03-02 07:29:45','2018-03-02 07:29:45'),(1157,192,'Bbmaj7',8,'2018-03-02 07:30:29','2018-03-02 07:30:29'),(1158,192,'D-/F',16,'2018-03-02 07:31:01','2018-03-02 07:31:01'),(1159,192,'D-/G',24,'2018-03-02 07:31:13','2018-03-02 07:31:13'),(1160,193,'NC',0,'2018-03-02 07:32:10','2018-03-02 07:32:10'),(1161,194,'D',0,'2018-03-19 01:11:31','2018-03-19 01:11:31'),(1162,194,'F#-',3.5,'2018-03-19 01:12:20','2018-03-19 01:12:20'),(1163,194,'G',7.5,'2018-03-19 01:12:39','2018-03-19 01:12:39'),(1164,194,'E-7',13.5,'2018-03-19 01:13:00','2018-03-19 01:13:00'),(1165,194,'D',16,'2018-03-19 01:13:13','2018-03-19 01:13:13'),(1166,194,'F#-',19.5,'2018-03-19 01:13:34','2018-03-19 01:13:34'),(1167,194,'G',23.5,'2018-03-19 01:13:50','2018-03-19 01:13:50'),(1168,194,'Gmaj7/A',29.5,'2018-03-19 01:14:59','2018-03-19 01:14:59'),(1169,195,'B-',0,'2018-03-19 01:20:31','2018-03-19 01:20:31'),(1170,195,'E-',4,'2018-03-19 01:20:47','2018-03-19 01:20:47'),(1171,195,'G',12,'2018-03-19 01:20:55','2018-03-19 01:20:55'),(1172,195,'B-',16,'2018-03-19 01:21:13','2018-03-19 01:21:13'),(1173,195,'E-',20,'2018-03-19 01:21:26','2018-03-19 01:21:26'),(1174,195,'G',28,'2018-03-19 01:23:58','2018-03-19 01:23:58'),(1175,196,'F#-',0,'2018-03-19 01:26:01','2018-03-19 01:26:01'),(1176,197,'C#-7',0,'2018-03-23 04:24:57','2018-03-23 04:24:57'),(1177,197,'D#-7',14.5,'2018-03-23 04:25:34','2018-03-23 04:25:34'),(1178,198,'G#-',0,'2018-03-23 04:26:31','2018-03-23 04:26:31'),(1179,198,'Amaj7/E',3.75,'2018-03-23 04:27:09','2018-03-23 04:27:09'),(1180,198,'Bmaj6',11.75,'2018-03-23 04:27:34','2018-03-23 04:27:34'),(1181,199,'Bb-',0,'2018-03-23 04:30:36','2018-03-23 04:30:36'),(1182,200,'Bb-',0,'2018-03-23 04:31:12','2018-03-23 04:31:12'),(1183,200,'Abmaj6/9',8,'2018-03-23 04:32:20','2018-03-23 04:32:20'),(1184,200,'Gbmaj7',16,'2018-03-23 04:32:33','2018-03-23 04:32:33'),(1185,200,'Db',24,'2018-03-23 04:32:55','2018-03-23 04:32:55'),(1186,200,'Eb-',28,'2018-03-23 04:33:05','2018-03-23 04:33:05'),(1187,200,'Bb-',32,'2018-03-23 04:33:17','2018-03-23 04:33:17'),(1188,200,'Abmaj6/9',40,'2018-03-23 04:33:36','2018-03-23 04:33:36'),(1189,200,'Gbmaj7',48,'2018-03-23 04:33:45','2018-03-23 04:33:45'),(1190,200,'Fsus4',56,'2018-03-23 04:34:03','2018-03-23 04:34:03'),(1191,200,'F',60,'2018-03-23 04:34:11','2018-03-23 04:34:11'),(1192,201,'Bb-',0,'2018-03-23 04:38:06','2018-03-23 04:38:06'),(1193,201,'Db',8,'2018-03-23 04:38:15','2018-03-23 04:38:15'),(1194,202,'Gb',0,'2018-03-23 04:39:01','2018-03-23 04:39:01'),(1195,202,'Ab',4,'2018-03-23 04:39:20','2018-03-23 04:39:20'),(1196,202,'Bb-',6,'2018-03-23 04:39:30','2018-03-23 04:39:30'),(1197,202,'Eb-',8,'2018-03-23 04:39:41','2018-03-23 04:39:41'),(1198,202,'Db/F',14,'2018-03-23 04:39:55','2018-03-23 04:39:55'),(1199,202,'Gb',16,'2018-03-23 04:40:06','2018-03-23 04:40:06'),(1200,202,'Ab',20,'2018-03-23 04:40:16','2018-03-23 04:40:16'),(1201,202,'Bb-',22,'2018-03-23 04:40:27','2018-03-23 04:40:27'),(1202,202,'Eb-',24,'2018-03-23 04:40:35','2018-03-23 04:40:35'),(1203,202,'Ab7sus4',28,'2018-03-23 04:40:53','2018-03-23 04:40:53'),(1204,203,'F-7',0,'2018-03-23 04:41:24','2018-03-23 04:41:24'),(1205,203,'Gbmaj6/9',8,'2018-03-23 04:41:48','2018-03-23 04:41:48'),(1206,203,'Ab7sus4',12,'2018-03-23 04:41:58','2018-03-23 04:41:58'),(1207,204,'Fsus4add3',0,'2018-03-23 04:46:22','2018-03-23 04:46:22'),(1208,204,'C7sus4',8,'2018-03-23 04:47:22','2018-03-23 04:47:22'),(1209,205,'Ebmaj6/9',0,'2018-03-23 04:50:43','2018-03-23 04:50:43'),(1210,205,'Fsus4/Gb',8,'2018-03-23 04:50:54','2018-03-23 04:50:54'),(1211,206,'C-',0,'2018-03-28 04:55:15','2018-03-28 04:55:15'),(1212,206,'Db',4,'2018-03-28 04:55:24','2018-03-28 04:55:24'),(1213,206,'Bb-',12,'2018-03-28 04:55:37','2018-03-28 04:55:37'),(1214,206,'C-',16,'2018-03-28 04:55:44','2018-03-28 04:55:44'),(1215,206,'Db',20,'2018-03-28 04:55:54','2018-03-28 04:55:54'),(1216,206,'E-',29.5,'2018-03-28 05:00:17','2018-03-28 05:00:17'),(1217,207,'Ab-',0,'2018-03-28 05:04:21','2018-03-28 05:04:21'),(1218,207,'B',4,'2018-03-28 05:04:28','2018-03-28 05:04:28'),(1219,207,'Eb-',8,'2018-03-28 05:04:39','2018-03-28 05:04:39'),(1220,208,'Ebmaj7',0,'2018-03-28 05:18:24','2018-03-28 05:18:24'),(1221,208,'G-7',8,'2018-03-28 05:18:44','2018-03-28 05:18:44'),(1222,208,'Ebmaj7',16,'2018-03-28 05:19:09','2018-03-28 05:19:09'),(1223,208,'G-7',24,'2018-03-28 05:19:23','2018-03-28 05:19:23'),(1224,208,'F-7',27.5,'2018-03-28 05:19:40','2018-03-28 05:19:40'),(1225,208,'Abmaj7',29.5,'2018-03-28 05:19:50','2018-03-28 05:19:50'),(1226,209,'Abmaj7',0,'2018-03-28 05:21:23','2018-03-28 05:21:23'),(1227,209,'F-7',8,'2018-03-28 05:21:30','2018-03-28 05:21:30'),(1228,209,'C-',16,'2018-03-28 05:23:40','2018-03-28 05:23:40'),(1229,209,'Bb7sus4',24,'2018-03-28 05:24:05','2018-03-28 05:24:05'),(1230,210,'Cmaj7',0,'2018-03-28 05:32:36','2018-03-28 05:32:36'),(1231,210,'Emaj7',3.5,'2018-03-28 05:32:45','2018-03-28 05:32:45'),(1232,210,'Abmaj7',7.5,'2018-03-28 05:33:06','2018-03-28 05:33:06'),(1233,210,'Fmaj7',11.5,'2018-03-28 05:33:28','2018-03-28 05:33:28'),(1234,211,'Dbmaj7',0,'2018-03-28 05:36:02','2018-03-28 05:36:02'),(1235,211,'Amaj7',3.5,'2018-03-28 05:36:16','2018-03-28 05:36:16'),(1236,211,'Fmaj7',8,'2018-03-28 05:36:25','2018-03-28 05:36:25'),(1237,211,'Dbmaj7',16,'2018-03-28 05:36:38','2018-03-28 05:36:38'),(1238,211,'Amaj7',19.5,'2018-03-28 05:36:49','2018-03-28 05:36:49'),(1239,211,'Fmaj7',24,'2018-03-28 05:37:02','2018-03-28 05:37:02'),(1240,212,'E-7',0,'2018-03-28 05:46:04','2018-03-28 05:47:21'),(1241,212,'Fmaj7',4,'2018-03-28 05:46:15','2018-03-28 05:46:15'),(1242,212,'A-7',8,'2018-03-28 05:47:31','2018-03-28 05:47:31'),(1243,213,'Dmaj6',0,'2018-03-28 05:53:22','2018-03-28 05:53:22'),(1244,213,'Cmaj7/E',4,'2018-03-28 05:53:33','2018-03-28 05:53:33'),(1245,213,'Cmaj7add9',8,'2018-03-28 05:53:49','2018-03-28 05:53:49'),(1246,214,'D/G',0,'2018-03-28 05:59:06','2018-03-28 05:59:06'),(1247,214,'C/G',8,'2018-03-28 05:59:14','2018-03-28 05:59:14'),(1248,215,'C',0,'2018-06-05 03:35:03','2018-06-05 03:35:03'),(1249,215,'A-7',4,'2018-06-05 03:35:12','2018-06-05 03:35:12'),(1250,215,'E-7',8,'2018-06-05 03:35:19','2018-06-05 03:35:19'),(1251,215,'Fmaj6',12,'2018-06-05 03:35:26','2018-06-05 03:35:26'),(1252,215,'C',16,'2018-06-05 03:35:34','2018-06-05 03:35:34'),(1253,215,'A-7',20,'2018-06-05 03:35:44','2018-06-05 03:35:44'),(1254,215,'D-7',24,'2018-06-05 03:35:55','2018-06-05 03:35:55'),(1255,215,'Fmaj6',27.5,'2018-06-05 03:36:27','2018-06-05 03:36:27'),(1256,215,'F/G',29.5,'2018-06-05 03:36:38','2018-06-05 03:36:38'),(1257,216,'F5',0,'2018-06-05 03:41:47','2018-06-05 03:41:47'),(1258,216,'F5/D',16,'2018-06-05 03:42:04','2018-06-05 03:42:04'),(1259,216,'F5/Db',32,'2018-06-05 03:42:21','2018-06-05 03:42:21'),(1260,216,'Abmaj6',48,'2018-06-05 03:43:28','2018-06-05 03:43:28'),(1261,216,'C7sus4',60,'2018-06-05 03:46:02','2018-06-05 03:46:02'),(1262,217,'C#maj7',0,'2018-06-05 03:59:30','2018-06-05 03:59:30'),(1263,217,'F#-7add9',1.5,'2018-06-05 03:59:51','2018-06-05 04:00:19'),(1264,217,'G#-7',8,'2018-06-05 04:00:13','2018-06-05 04:00:13'),(1265,217,'Emaj7add9',9.5,'2018-06-05 04:00:52','2018-06-05 04:00:52'),(1266,217,'C#maj7',16,'2018-06-05 04:01:24','2018-06-05 04:01:24'),(1267,217,'Amaj7',17.5,'2018-06-05 04:01:41','2018-06-05 04:01:41'),(1268,217,'G#-7',24,'2018-06-05 04:02:02','2018-06-05 04:02:02'),(1269,217,'Dmaj7add13',25.5,'2018-06-05 04:03:24','2018-06-05 04:03:24'),(1270,218,'C#-',0,'2018-06-14 03:08:03','2018-06-14 03:08:03'),(1271,218,'C#sus4/D',1.5,'2018-06-14 03:08:25','2018-06-14 03:08:25'),(1272,218,'Emaj7add9',3.5,'2018-06-14 03:08:56','2018-06-14 03:08:56'),(1273,218,'F#-6',5.5,'2018-06-14 03:09:22','2018-06-14 03:09:22'),(1274,218,'C#-7/B',8,'2018-06-14 03:10:10','2018-06-14 03:21:10'),(1275,218,'Emaj7/G#',16,'2018-06-14 03:10:54','2018-06-14 03:22:26'),(1276,218,'E/A',17.5,'2018-06-14 03:24:01','2018-06-14 03:24:01'),(1277,218,'E/B',19.5,'2018-06-14 03:24:20','2018-06-14 03:24:20'),(1278,218,'Bmaj6',21.5,'2018-06-14 03:24:41','2018-06-14 03:24:41'),(1279,218,'Badd4/C#',24,'2018-06-14 03:32:37','2018-06-14 03:35:13'),(1538,252,'C',0,'2018-08-10 23:21:55','2018-08-10 23:21:55'),(1539,253,'C',0,'2018-08-10 23:23:26','2018-08-10 23:23:26'),(1540,254,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1541,256,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1542,255,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1543,257,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1544,258,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1545,259,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1546,260,'C',0,'2018-08-21 18:19:02','2018-08-21 18:19:02'),(1547,261,'C',0,'2018-10-06 23:01:30','2018-10-06 23:01:30');
/*!40000 ALTER TABLE `pattern_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_meme`
--

LOCK TABLES `pattern_meme` WRITE;
/*!40000 ALTER TABLE `pattern_meme` DISABLE KEYS */;
INSERT INTO `pattern_meme` VALUES (5,6,'Cool','2017-05-01 19:43:30','2017-05-01 19:43:30'),(7,7,'Hot','2017-05-01 19:44:52','2017-05-01 19:44:52'),(8,4,'Hot','2017-05-01 19:45:58','2017-05-01 19:45:58'),(9,5,'Cool','2017-05-01 19:46:10','2017-05-01 19:46:10'),(10,4,'Tropical','2017-06-16 03:37:25','2017-06-16 03:37:25'),(11,5,'Electro','2017-06-16 03:38:03','2017-06-16 03:38:03'),(12,6,'Hard','2017-06-16 03:38:19','2017-06-16 03:38:19'),(13,7,'Easy','2017-06-16 03:38:40','2017-06-16 03:38:40'),(15,14,'Hard','2017-07-29 23:48:20','2017-07-29 23:48:20'),(22,21,'Earth','2017-12-13 06:39:45','2017-12-13 06:39:45'),(30,29,'Fire','2017-12-13 06:46:18','2017-12-13 06:46:18'),(31,30,'Water','2017-12-13 06:46:44','2017-12-13 06:46:44'),(32,31,'Fire','2017-12-13 06:47:29','2017-12-13 06:47:29'),(33,32,'Wind','2017-12-13 06:47:51','2017-12-13 06:47:51'),(34,33,'Wind','2017-12-13 06:48:33','2017-12-13 06:48:33'),(35,34,'Earth','2017-12-13 06:48:55','2017-12-13 06:48:55'),(36,35,'Wind','2017-12-13 06:49:27','2017-12-13 06:49:27'),(66,65,'Fire','2017-12-23 21:51:03','2017-12-23 21:51:03'),(68,66,'Water','2017-12-23 21:54:58','2017-12-23 21:54:58'),(72,68,'Earth','2017-12-23 22:00:20','2017-12-23 22:00:20'),(74,69,'Water','2017-12-23 22:02:13','2017-12-23 22:02:13'),(75,30,'Fire','2017-12-23 22:02:20','2017-12-23 22:02:20'),(76,32,'Fire','2017-12-23 22:04:01','2017-12-23 22:04:01'),(77,70,'Wind','2017-12-23 22:04:33','2017-12-23 22:04:33'),(78,34,'Wind','2017-12-23 22:05:35','2017-12-23 22:05:35'),(79,71,'Earth','2017-12-23 22:05:54','2017-12-23 22:05:54'),(80,72,'Fire','2017-12-23 22:09:04','2017-12-23 22:09:04'),(83,73,'Water','2017-12-23 22:10:31','2017-12-23 22:10:31'),(84,74,'Water','2017-12-23 22:11:41','2017-12-23 22:11:41'),(85,75,'Water','2017-12-23 22:12:10','2017-12-23 22:12:10'),(86,75,'Wind','2017-12-23 22:12:14','2017-12-23 22:12:14'),(87,76,'Wind','2017-12-23 22:12:33','2017-12-23 22:12:33'),(88,77,'Water','2017-12-23 22:13:17','2017-12-23 22:13:17'),(89,78,'Water','2017-12-23 22:13:36','2017-12-23 22:13:36'),(90,78,'Fire','2017-12-23 22:13:39','2017-12-23 22:13:39'),(91,79,'Fire','2017-12-23 22:13:54','2017-12-23 22:13:54'),(92,80,'Water','2017-12-23 22:14:58','2017-12-23 22:14:58'),(93,81,'Water','2017-12-23 22:15:25','2017-12-23 22:15:25'),(94,81,'Earth','2017-12-23 22:15:29','2017-12-23 22:15:29'),(95,82,'Earth','2017-12-23 22:15:47','2017-12-23 22:15:47');
/*!40000 ALTER TABLE `pattern_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_event`
--

LOCK TABLES `pattern_event` WRITE;
/*!40000 ALTER TABLE `pattern_event` DISABLE KEYS */;
INSERT INTO `pattern_event` VALUES (270,3,4,0.3,1,'KICK',2.5,0.5,'C2','2017-06-02 23:57:53','2018-10-06 22:52:00'),(274,3,4,1,0.2,'SNARE',1,1,'G8','2017-06-02 23:58:37','2018-01-03 21:36:07'),(275,3,5,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-06-03 00:09:06','2018-01-03 21:36:07'),(276,3,5,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-06-03 00:10:09','2018-01-03 21:36:07'),(277,3,5,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-06-03 00:10:14','2018-01-03 21:36:07'),(278,3,5,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-06-03 00:10:19','2018-01-03 21:36:07'),(280,3,5,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2017-06-03 00:11:48','2018-01-05 09:32:21'),(281,3,5,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2017-06-03 00:11:52','2018-01-05 09:32:21'),(282,3,5,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-06-03 00:11:57','2018-01-03 21:36:07'),(283,3,5,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2017-06-03 00:12:02','2018-01-05 09:32:21'),(284,3,5,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2017-06-03 00:12:32','2018-01-05 09:32:21'),(285,3,5,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2017-06-03 00:12:37','2018-01-05 09:32:21'),(286,3,5,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2017-06-03 00:12:41','2018-01-05 09:32:21'),(287,3,5,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2017-06-03 00:12:46','2018-01-05 09:32:21'),(288,3,5,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-06-03 00:13:33','2018-01-03 21:36:07'),(290,3,5,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2017-06-03 00:13:43','2018-01-05 09:32:21'),(291,3,5,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-06-03 00:13:47','2018-01-03 21:36:07'),(294,3,4,0.2,1,'KICK',2.25,0.2,'F#2','2017-06-04 04:26:37','2018-01-05 09:32:21'),(301,3,5,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-06-04 04:49:14','2018-01-03 21:36:07'),(302,3,5,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-06-04 04:51:11','2018-01-03 21:36:07'),(303,3,5,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-06-04 04:51:23','2018-01-03 21:36:07'),(304,3,5,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-06-04 04:51:28','2018-01-03 21:36:07'),(305,3,5,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-06-04 04:51:34','2018-01-03 21:36:07'),(306,3,5,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2017-06-04 04:51:43','2018-01-05 09:32:21'),(314,3,8,0.1,0.6,'TOM',0.5,0.75,'C6','2017-06-11 19:51:53','2018-01-03 21:36:07'),(315,3,8,0.05,0.6,'TOM',1.25,0.7,'G5','2017-06-11 19:52:17','2018-01-05 09:32:21'),(316,3,8,0.2,0.6,'TOM',2,1,'C5','2017-06-11 19:53:15','2018-01-03 21:36:07'),(320,3,8,0.1,0.6,'TOM',3.5,0.5,'G3','2017-06-11 20:16:54','2018-01-03 21:36:07'),(322,3,4,0.1,0.2,'SNARE',1.75,0.2,'G5','2017-06-12 19:14:16','2018-01-05 09:32:21'),(323,3,8,0.05,0.5,'COWBELL',2,1,'F5','2017-06-12 19:20:22','2018-01-03 21:36:07'),(339,3,12,1,1,'KICK',0,1,'C2','2017-12-07 03:43:32','2018-10-06 22:52:00'),(341,3,12,0.8,1,'KICK',2.5,1,'C2','2017-12-07 03:43:52','2018-01-03 21:36:07'),(343,3,4,1,0.1,'SNARE',3,1,'G8','2017-12-07 08:17:58','2018-01-03 21:36:07'),(371,3,17,0.05,0.5,'TOMLOW',2,0.8,'G4','2017-12-21 00:38:41','2018-01-07 00:38:37'),(372,3,17,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2017-12-21 00:39:01','2018-01-07 00:38:37'),(373,3,17,0.1,0.5,'TOMLOW',3.5,1,'G4','2017-12-21 00:39:18','2018-01-07 00:38:37'),(374,3,17,0.05,0.5,'TOMLOW',1,0.5,'G4','2017-12-21 00:39:32','2018-01-07 00:38:37'),(375,3,17,0.1,0.5,'TOMLOW',1,1,'G4','2017-12-21 00:39:41','2018-01-07 00:38:37'),(378,64,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2017-12-22 06:43:19','2018-01-05 09:32:21'),(379,64,18,0.2,1,'KICK',2.25,0.2,'F#2','2017-12-22 06:43:19','2018-01-05 09:32:21'),(380,64,18,0.3,1,'KICK',2.5,0.5,'C2','2017-12-22 06:43:19','2018-10-06 22:52:00'),(382,64,18,1,0.1,'SNARE',3,1,'G8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(383,64,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-12-22 06:43:19','2018-01-03 21:36:07'),(384,64,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(385,64,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(386,64,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-12-22 06:43:19','2018-01-03 21:36:07'),(387,64,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(388,64,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-12-22 06:43:19','2018-01-03 21:36:07'),(389,64,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2017-12-22 06:43:19','2018-01-05 09:32:21'),(390,64,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(391,64,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(392,64,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(393,64,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(394,64,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2017-12-22 06:43:20','2018-01-05 09:32:21'),(395,64,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(396,64,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(397,64,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(398,64,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-12-22 06:43:20','2018-01-03 21:36:07'),(399,64,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(400,64,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2017-12-22 06:43:20','2018-01-05 09:32:21'),(401,64,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(402,64,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-12-22 06:43:20','2018-01-03 21:36:07'),(403,64,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2017-12-22 06:43:20','2018-01-05 09:32:21'),(405,64,20,0.1,0.6,'TOM',0.5,0.75,'C6','2017-12-22 06:43:20','2018-01-03 21:36:07'),(406,64,20,0.05,0.6,'TOM',1.25,0.7,'G5','2017-12-22 06:43:20','2018-01-05 09:32:21'),(407,64,20,0.2,0.6,'TOM',2,1,'C5','2017-12-22 06:43:20','2018-01-03 21:36:07'),(410,64,20,0.1,0.6,'TOM',3.5,0.5,'G3','2017-12-22 06:43:20','2018-01-03 21:36:07'),(419,64,23,0.8,1,'KICK',2.5,1,'C2','2017-12-22 06:43:20','2018-01-03 21:36:07'),(420,64,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(423,64,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(424,64,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2017-12-22 06:43:20','2018-01-07 00:38:37'),(550,86,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(551,86,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(552,86,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 07:39:55','2018-01-05 07:39:55'),(553,86,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(554,86,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(555,86,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 07:39:55','2018-01-07 00:38:37'),(556,86,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(557,86,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(560,86,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(561,86,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 07:39:55','2018-01-05 09:32:21'),(562,86,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(563,86,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(564,86,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(565,86,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 07:39:55','2018-01-05 09:32:21'),(566,86,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(569,86,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 07:39:55','2018-01-05 09:32:21'),(571,86,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 07:39:55','2018-01-05 07:39:55'),(572,86,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(573,86,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(574,86,18,0.3,1,'KICK',2.5,0.5,'C2','2018-01-05 07:39:55','2018-10-06 22:52:00'),(575,86,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(577,86,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 07:39:55','2018-01-05 07:39:55'),(578,86,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(580,86,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 09:32:21'),(581,86,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(583,86,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(584,86,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 07:39:55','2018-01-05 07:39:55'),(585,86,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 07:39:55','2018-01-05 07:39:55'),(586,86,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 07:39:55','2018-01-07 00:38:37'),(587,86,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 07:39:55','2018-01-05 09:32:21'),(588,86,23,0.8,1,'KICK',2,1,'C2','2018-01-05 08:36:52','2018-01-05 08:36:52'),(590,87,23,1,1,'KICK',0,1,'C2','2018-01-05 08:37:43','2018-10-06 22:52:00'),(591,87,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 08:37:43','2018-01-05 08:37:43'),(592,87,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(593,87,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 08:37:43','2018-01-05 08:37:43'),(594,87,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(595,87,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(596,87,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 08:37:43','2018-01-07 00:38:37'),(597,87,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(598,87,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 08:37:43','2018-01-05 08:37:43'),(601,87,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(602,87,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 08:37:43','2018-01-05 09:32:21'),(603,87,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(604,87,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 08:37:43','2018-01-05 08:37:43'),(605,87,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 08:37:43','2018-01-05 09:32:21'),(606,87,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 08:37:43','2018-01-05 09:32:21'),(607,87,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(608,87,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 08:37:44','2018-01-05 08:37:44'),(609,87,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 08:37:44','2018-01-05 08:37:44'),(610,87,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 08:37:44','2018-01-05 09:32:21'),(611,87,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 08:37:44','2018-01-05 09:32:21'),(612,87,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 08:37:44','2018-01-05 08:37:44'),(613,87,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(614,87,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(615,87,18,0.3,1,'KICK',2.5,0.5,'C2','2018-01-05 08:37:44','2018-10-06 22:52:00'),(616,87,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(618,87,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 08:37:44','2018-01-05 08:37:44'),(621,87,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 09:32:21'),(622,87,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(624,87,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(625,87,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 08:37:44','2018-01-05 08:37:44'),(626,87,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 08:37:44','2018-01-05 08:37:44'),(627,87,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 08:37:44','2018-01-07 00:38:37'),(628,87,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 08:37:44','2018-01-05 09:32:21'),(629,88,23,1,1,'KICK',0,1,'C2','2018-01-05 08:38:44','2018-10-06 22:52:00'),(631,88,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 08:38:44','2018-01-05 08:38:44'),(632,88,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 08:38:44','2018-01-05 09:32:21'),(633,88,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 08:38:44','2018-01-05 08:38:44'),(634,88,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 08:38:44','2018-01-05 08:38:44'),(635,88,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 08:38:44','2018-01-05 08:38:44'),(636,88,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 08:38:44','2018-01-07 00:38:37'),(637,88,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 08:38:44','2018-01-05 09:32:21'),(640,88,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(641,88,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(642,88,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 08:38:45','2018-01-05 09:32:21'),(643,88,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(644,88,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(645,88,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(646,88,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 08:38:45','2018-01-05 09:32:21'),(647,88,23,0.8,1,'KICK',1.75,1,'C2','2018-01-05 08:38:45','2018-01-05 10:06:35'),(648,88,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 08:38:45','2018-01-05 08:38:45'),(649,88,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(650,88,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 08:38:45','2018-01-05 09:32:21'),(651,88,18,0.1,1,'SNARE',2.25,0.2,'F#2','2018-01-05 08:38:45','2018-01-05 10:07:09'),(652,88,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(654,88,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(656,88,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(657,88,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 08:38:45','2018-01-07 00:38:37'),(658,88,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 08:38:45','2018-01-05 08:38:45'),(659,88,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(661,88,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 09:32:21'),(662,88,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(664,88,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(665,88,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 08:38:45','2018-01-05 08:38:45'),(666,88,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 08:38:45','2018-01-05 08:38:45'),(667,88,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 08:38:45','2018-01-07 00:38:37'),(668,88,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 08:38:45','2018-01-05 09:32:21'),(669,88,18,0.5,0.1,'SNARE',2.5,1,'G8','2018-01-05 08:39:07','2018-01-05 10:07:25'),(725,91,23,1,1,'KICK',0,1,'C2','2018-01-05 09:43:58','2018-10-06 22:52:00'),(726,91,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(727,91,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(728,91,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 09:43:58','2018-01-05 09:43:58'),(729,91,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(730,91,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(731,91,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(732,91,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(733,91,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(736,91,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(737,91,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(738,91,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(739,91,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(740,91,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(741,91,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(742,91,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(743,91,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(744,91,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 09:43:58','2018-01-05 09:43:58'),(745,91,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(746,91,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(747,91,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 09:43:58','2018-01-05 09:43:58'),(748,91,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(749,91,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(750,91,18,0.3,1,'KICK',2.5,0.5,'C2','2018-01-05 09:43:58','2018-10-06 22:52:00'),(751,91,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(752,91,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(753,91,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(754,91,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(756,91,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(757,91,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(759,91,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(760,91,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 09:43:58','2018-01-05 09:43:58'),(761,91,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 09:43:58','2018-01-05 09:43:58'),(762,91,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 09:43:58','2018-01-07 00:38:37'),(763,91,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 09:43:58','2018-01-05 09:43:58'),(764,91,20,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 09:47:17','2018-01-06 23:29:10'),(765,91,19,0.125,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 09:57:22','2018-01-06 23:29:10'),(766,91,24,0.0625,0,'CYMBALCRASH',3,4,'F5','2018-01-05 09:57:47','2018-01-06 23:29:10'),(768,92,20,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 10:04:13','2018-01-06 23:29:10'),(769,92,20,1,1,'KICK',1,4,'C5','2018-01-05 10:04:13','2018-10-06 22:52:00'),(770,92,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(771,92,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(772,92,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 10:04:13','2018-01-05 10:04:13'),(773,92,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 10:04:13','2018-01-07 00:38:37'),(774,92,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(775,92,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(776,92,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(777,92,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(780,92,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 10:04:13','2018-01-05 10:04:13'),(781,92,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(782,92,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(783,92,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 10:04:13','2018-01-05 10:04:13'),(784,92,19,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 10:04:13','2018-01-06 23:29:10'),(785,92,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(786,92,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-01-05 10:04:13','2018-01-05 10:04:13'),(787,92,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 10:04:13','2018-01-05 10:04:13'),(788,92,20,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 10:04:14','2018-01-05 10:04:14'),(789,92,20,0.2,0.6,'TOM',2,1,'C5','2018-01-05 10:04:14','2018-01-05 10:04:14'),(790,92,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(791,92,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-01-05 10:04:14','2018-01-05 10:04:14'),(792,92,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(793,92,18,0.3,1,'KICK',2.5,0.5,'C2','2018-01-05 10:04:14','2018-10-06 22:52:00'),(794,92,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(795,92,23,0.8,1,'KICK',2.5,1,'C2','2018-01-05 10:04:14','2018-01-05 10:04:14'),(796,92,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(797,92,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 10:04:14','2018-01-07 00:38:37'),(798,92,24,0.03,0,'CYMBALCRASH',3,4,'F5','2018-01-05 10:04:14','2018-01-06 23:29:10'),(800,92,18,1,0.1,'SNARE',3,1,'G8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(801,92,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(802,92,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(803,92,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(805,92,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(806,92,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 10:04:14','2018-01-05 10:04:14'),(807,92,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 10:04:14','2018-01-05 10:04:14'),(808,92,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 10:04:14','2018-01-07 00:38:37'),(809,92,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 10:04:14','2018-01-05 10:04:14'),(1048,99,37,1,1,'KICK',0,4,'C5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1050,99,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1052,99,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1053,100,37,1,1,'KICK',0,4,'C5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1054,102,37,1,1,'KICK',0,4,'C5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1056,99,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1057,100,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1058,101,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1060,99,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1062,100,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1063,102,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1064,101,37,1,1,'KICK',0,4,'c5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1065,99,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1066,103,37,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1067,100,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1068,102,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1069,101,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1070,99,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1071,103,37,1,1,'KICK',0,4,'C5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1072,100,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1073,102,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1074,101,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1075,99,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1076,103,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1077,100,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1078,104,37,1,1,'KICK',0,4,'C5','2018-01-05 14:54:11','2018-10-06 22:52:00'),(1079,102,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1080,101,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1081,103,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1082,99,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1083,100,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1084,102,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1085,101,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1086,103,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1087,104,37,0.25,0,'CYMBALCRASH',0,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1089,100,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1090,102,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1091,101,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1092,103,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1095,104,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1096,102,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1097,101,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1098,103,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1099,99,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1101,102,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1103,103,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1104,99,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1105,104,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1106,100,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1109,103,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1110,99,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1111,100,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1112,104,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1113,101,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1115,103,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1116,99,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1117,100,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1118,101,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1119,102,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1120,99,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1122,100,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1123,104,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-01-05 14:54:11','2018-01-07 00:38:37'),(1126,101,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1127,102,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1128,100,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1129,104,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1130,99,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1131,103,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1132,101,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1133,102,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1134,100,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1136,103,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1137,104,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1139,102,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1140,101,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1141,99,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1142,103,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1144,102,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1145,101,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1146,104,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1147,99,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1148,103,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1152,103,36,0.125,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 14:54:11','2018-01-06 23:29:10'),(1154,101,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1155,104,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1156,100,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1158,103,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1160,101,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1161,100,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1162,102,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1163,104,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1164,99,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1166,100,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1167,101,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1168,102,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1169,99,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1170,103,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1171,100,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1172,104,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1173,102,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1174,101,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1175,103,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:11','2018-01-05 14:54:11'),(1180,104,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1181,103,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1182,99,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1186,103,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1187,104,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1188,99,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1189,100,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1190,101,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1192,99,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1194,100,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1195,101,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1196,103,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1197,104,36,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1198,102,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1203,102,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1204,104,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1207,101,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1208,103,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1209,102,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1210,99,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1211,100,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1212,101,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1215,102,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1216,99,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1217,100,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1218,101,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1219,103,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1220,102,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1222,104,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1224,101,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1225,103,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1227,99,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1228,100,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1230,103,39,0.0625,0,'CYMBALCRASH',3,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1232,104,37,0.05,0.5,'COWBELL',2,1,'F5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1233,99,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1234,100,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1235,101,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1236,102,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1238,99,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1239,104,37,0.2,0.6,'TOM',2,1,'C5','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1240,102,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1241,100,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1242,101,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1244,99,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1246,104,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1247,100,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1248,103,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1249,99,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1250,101,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1251,100,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1252,102,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1253,103,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1254,101,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1256,102,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1257,103,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1258,101,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1259,104,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1260,102,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1262,102,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1263,103,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1265,102,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1266,103,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1267,104,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1268,103,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1269,103,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1271,103,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1272,104,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1273,104,39,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1275,104,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1277,104,39,0.03,0,'CYMBALCRASH',3,4,'F5','2018-01-05 14:54:12','2018-01-06 23:29:10'),(1278,104,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1279,104,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1281,104,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1282,104,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1283,104,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1284,104,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-01-05 14:54:12','2018-01-07 00:38:37'),(1285,104,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-01-05 14:54:12','2018-01-05 14:54:12'),(1286,99,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:09:36','2018-01-07 02:09:36'),(1287,100,37,0.4,0,'SNARE',2,1,'g5','2018-01-07 02:10:12','2018-01-07 02:13:24'),(1288,101,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:10:51','2018-01-07 02:10:51'),(1289,101,37,0.4,0,'SNARE',3.5,1,'g5','2018-01-07 02:11:06','2018-01-07 02:11:06'),(1290,102,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:11:31','2018-01-07 02:11:31'),(1291,103,37,0.8,0,'SNARE',2,1,'g5','2018-01-07 02:12:11','2018-01-07 02:12:11'),(1292,104,37,0.4,0,'SNARE',2,1,'g5','2018-01-07 02:12:55','2018-01-07 02:13:04'),(1293,252,23,1,1,'KICK',0,1,'C2','2018-08-10 23:21:55','2018-10-06 22:52:00'),(1295,252,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1296,252,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1297,252,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1298,252,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1299,252,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1300,252,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1301,252,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1302,252,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1303,252,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1304,252,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1305,252,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1306,252,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1307,252,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1308,252,18,0.1,0.2,'SNARE',1.75,0.2,'G5','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1309,252,23,0.8,1,'KICK',2,1,'C2','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1310,252,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1311,252,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1312,252,23,0.8,1,'KICK',2.5,1,'C2','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1313,252,18,0.3,1,'KICK',2.5,0.5,'C2','2018-08-10 23:21:55','2018-10-06 22:52:00'),(1314,252,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1315,252,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1316,252,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1317,252,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1318,252,18,1,0.1,'SNARE',1.75,1,'G8','2018-08-10 23:21:55','2018-08-10 23:22:35'),(1319,252,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1320,252,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-08-10 23:21:55','2018-08-10 23:21:55'),(1322,252,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-08-10 23:21:56','2018-08-10 23:21:56'),(1323,252,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-10 23:21:56','2018-08-10 23:21:56'),(1324,252,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-10 23:21:56','2018-08-10 23:21:56'),(1325,252,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-10 23:21:56','2018-08-10 23:21:56'),(1326,252,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-08-10 23:21:56','2018-08-10 23:21:56'),(1327,253,36,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1328,253,37,1,1,'KICK',0,4,'C5','2018-08-10 23:23:26','2018-10-06 22:52:00'),(1329,253,36,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1330,253,37,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1331,253,36,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1332,253,36,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1333,253,39,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1334,253,36,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1335,253,36,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1336,253,36,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1337,253,37,0.05,0.6,'TOM',1.25,0.7,'G5','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1338,253,36,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1339,253,36,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1340,253,36,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1341,253,37,0.4,0,'SNARE',1.5,1,'g5','2018-08-10 23:23:26','2018-08-10 23:23:50'),(1342,253,36,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1343,253,36,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1344,253,36,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1345,253,36,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1346,253,36,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-08-10 23:23:26','2018-08-10 23:23:26'),(1347,253,36,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1348,253,36,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1349,253,36,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1351,253,36,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1352,253,36,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1353,253,37,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1354,253,39,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1355,253,36,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-08-10 23:23:27','2018-08-10 23:23:27'),(1359,254,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1366,256,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1367,255,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1368,259,42,0.25,0,'CYMBALCRASH',0,4,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1371,258,42,0.25,0,'CYMBALCRASH',0,4,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1374,254,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1376,259,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1377,256,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1379,255,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1382,257,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1384,258,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1385,254,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1386,255,41,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1388,259,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1389,256,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1392,254,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1394,257,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1395,259,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1396,256,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1397,258,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1400,255,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1401,257,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1404,258,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1405,254,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1406,260,41,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1409,259,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1410,256,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1414,257,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1415,255,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1418,258,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1420,260,42,0.1,0.6,'TOM',0.5,0.75,'C6','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1423,259,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1425,254,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1432,256,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1433,258,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1434,260,41,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1435,257,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1437,259,42,0.05,0.6,'TOM',1.25,0.7,'G5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1441,260,44,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1443,255,41,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1447,258,42,0.05,0.6,'TOM',1.25,0.7,'G5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1451,259,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1452,254,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1454,258,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1455,260,41,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1459,254,42,0.2,0.6,'TOM',2,1,'C5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1460,256,42,0.2,0.6,'TOM',2,1,'C5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1462,258,41,0.125,0,'CYMBALCRASH',1.5,4,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1466,259,41,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1467,256,42,0.05,0.5,'COWBELL',2,1,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1469,255,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1470,257,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1473,260,41,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1474,257,42,0.05,0.5,'COWBELL',2,1,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1476,256,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1479,254,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1490,258,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1492,259,42,0.2,0.6,'TOM',2,1,'C5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1496,257,40,0.5,0.1,'SNARE',2.5,1,'G8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1497,258,42,0.05,0.5,'COWBELL',2,1,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1498,256,43,0.8,1,'KICK',2.5,1,'C2','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1499,259,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1501,255,41,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1504,257,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1506,259,42,0.05,0.5,'COWBELL',2,1,'F5','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1509,256,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1510,260,41,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1514,254,44,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1515,255,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1516,256,40,0.3,1,'KICK',2.5,0.5,'C2','2018-08-21 18:19:02','2018-10-06 22:52:00'),(1521,254,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:02','2018-08-21 18:19:02'),(1524,258,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1526,257,44,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1527,259,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1530,256,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1533,257,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1539,257,40,1,0.1,'SNARE',3,1,'G8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1541,259,41,0.1,0.1,'MARACAS',2.5,0.5,'Bb8','2018-08-21 18:19:03','2018-10-06 23:18:01'),(1545,260,41,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1550,255,41,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1553,257,41,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1555,254,41,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1558,256,41,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1564,255,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1565,256,41,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1566,260,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1567,257,41,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1568,258,44,0.0625,0,'CYMBALCRASH',3,4,'F5','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1569,259,44,0.03,0,'CYMBALCRASH',3,4,'F5','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1570,255,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1573,256,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1577,254,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1578,259,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1580,256,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1581,257,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1583,258,41,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1586,256,41,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1587,257,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1591,257,41,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1592,260,41,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1595,260,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1598,260,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1602,259,41,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1603,258,41,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1604,259,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1605,258,42,0.1,0.6,'TOM',3.5,0.5,'G3','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1606,259,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1607,258,44,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-08-21 18:19:03','2018-08-21 18:19:03'),(1611,64,23,1,0.618,'KICK',0,1,'X','2018-10-06 22:49:13','2018-10-07 18:21:47'),(1612,86,23,1,0.618,'KICK',0,1,'X','2018-10-06 22:49:25','2018-10-07 18:21:47'),(1613,261,20,0.25,0,'CYMBALCRASH',0,4,'F5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1614,261,19,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1615,261,19,0.1,0.1,'HIHATCLOSED',0.25,0.2,'G12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1616,261,20,0.1,0.6,'TOM',0.5,0.75,'C6','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1617,261,24,0.05,0.5,'TOMLOW',0.5,0.5,'G4','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1618,261,19,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1619,261,19,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1620,261,19,0.025,0.06,'HIHATCLOSED',0.75,0.2,'D12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1621,261,19,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1622,261,20,1,1,'KICK',1,4,'C5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1623,261,20,0.05,0.6,'TOM',1.25,0.7,'G5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1624,261,19,0.1,0.1,'HIHATCLOSED',1.25,0.2,'G12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1625,261,19,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1626,261,19,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1627,261,19,0.0625,0,'CYMBALCRASH',1.5,4,'F5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1628,261,19,0.12,0.06,'HIHATCLOSED',1.75,0.2,'D12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1630,261,20,0.2,0.6,'TOM',2,1,'C5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1631,261,19,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1632,261,20,0.05,0.5,'COWBELL',2,1,'F5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1633,261,19,0.1,0.12,'HIHATCLOSED',2.25,0.2,'E8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1634,261,18,0.2,1,'KICK',2.25,0.2,'F#2','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1635,261,19,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1636,261,18,0.3,1,'KICK',2.5,0.5,'C2','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1637,261,19,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1638,261,23,0.8,1,'KICK',2.5,1,'C2','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1639,261,19,0.025,0.06,'HIHATCLOSED',2.75,0.2,'D12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1640,261,24,0.08,0.5,'TOMLOW',2.75,0.8,'G4','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1641,261,24,0.03,0,'CYMBALCRASH',3,4,'F5','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1642,261,19,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1644,261,19,0.1,0.1,'MARACAS',3.25,0.5,'Bb8','2018-10-06 23:01:30','2018-10-06 23:01:30'),(1645,261,19,0.1,0.1,'HIHATCLOSED',3.25,0.2,'G12','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1646,261,19,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1647,261,19,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1648,261,20,0.1,0.6,'TOM',3.5,0.5,'G3','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1649,261,24,0.1,0.5,'TOMLOW',3.5,1,'G4','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1650,261,19,0.12,0.06,'HIHATCLOSED',3.75,0.2,'D12','2018-10-06 23:01:31','2018-10-06 23:01:31'),(1651,261,18,1,0.618,'SNARE',2,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:54'),(1652,261,18,0.66,0.618,'KICK',0,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:47'),(1653,261,18,0.33,0.618,'KICK',0.5,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:47'),(1654,261,18,0.66,0.618,'KICK',0.75,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:47'),(1655,261,18,0.33,0.618,'KICK',1.25,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:47'),(1656,261,18,0.66,0.618,'KICK',1.5,1,'X','2018-10-06 23:02:23','2018-10-07 18:21:47'),(1657,260,40,0.66,0.618,'SNARE',2,1,'X','2018-10-06 23:29:37','2018-10-07 18:21:54'),(1658,255,40,1,0.618,'KICK',0,1,'X','2018-10-06 23:30:37','2018-10-07 18:21:47'),(1659,255,40,0.66,0.618,'KICK',1,1,'X','2018-10-06 23:30:37','2018-10-07 18:21:47'),(1660,255,40,0.1,0.618,'KICK',2,1,'X','2018-10-06 23:30:37','2018-10-07 18:21:47'),(1661,255,40,0.1,0.618,'KICK',3,1,'X','2018-10-06 23:31:02','2018-10-07 18:21:47'),(1662,254,40,0.33,0.618,'KICK',1,1,'X','2018-10-06 23:31:32','2018-10-07 18:21:47'),(1663,254,40,0.66,0.618,'KICK',0,1,'X','2018-10-06 23:31:32','2018-10-07 18:21:47'),(1664,254,40,0.1,0.618,'KICK',2,1,'X','2018-10-06 23:31:32','2018-10-07 18:21:47'),(1665,254,40,0.05,0.618,'KICK',3,1,'X','2018-10-06 23:31:32','2018-10-07 18:21:47'),(1666,254,40,0.1,0.618,'KICK',3.5,1,'X','2018-10-06 23:31:32','2018-10-07 18:21:47'),(1667,260,40,0.33,0.618,'KICK',1,1,'X','2018-10-06 23:32:07','2018-10-07 18:21:47'),(1668,260,40,0.33,0.618,'KICK',0,1,'X','2018-10-06 23:32:07','2018-10-07 18:21:47'),(1669,260,40,0.33,0.618,'KICK',2,1,'X','2018-10-06 23:32:07','2018-10-07 18:21:47'),(1670,260,40,0.33,0.618,'KICK',3,1,'X','2018-10-06 23:32:07','2018-10-07 18:21:47'),(1671,256,40,0.33,0.618,'KICK',1,1,'X','2018-10-06 23:32:29','2018-10-07 18:21:47'),(1672,256,40,1,0.618,'KICK',0,1,'X','2018-10-06 23:32:29','2018-10-07 18:21:47'),(1673,257,40,1,0.618,'KICK',0,1,'X','2018-10-06 23:33:09','2018-10-07 18:21:47'),(1674,257,40,0.66,0.618,'KICK',1,1,'X','2018-10-06 23:33:09','2018-10-07 18:21:47'),(1675,257,40,0.33,0.618,'KICK',2,1,'X','2018-10-06 23:33:09','2018-10-07 18:21:47'),(1676,257,40,0.05,0.618,'KICK',3,1,'X','2018-10-06 23:33:09','2018-10-07 18:21:47'),(1677,257,40,0.05,0.618,'KICK',3.5,1,'X','2018-10-06 23:33:10','2018-10-07 18:21:47'),(1678,258,40,1,0.618,'KICK',0,1,'X','2018-10-06 23:33:44','2018-10-07 18:21:47'),(1679,258,40,0.66,0.618,'KICK',1,1,'X','2018-10-06 23:33:44','2018-10-07 18:21:47'),(1680,258,40,0.33,0.618,'KICK',2,1,'X','2018-10-06 23:33:44','2018-10-07 18:21:47'),(1681,258,40,0.1,0.618,'KICK',3,1,'X','2018-10-06 23:33:44','2018-10-07 18:21:47'),(1682,259,40,0.66,0.618,'KICK',1,1,'X','2018-10-06 23:34:15','2018-10-07 18:21:47'),(1683,259,40,1,0.618,'KICK',0,1,'X','2018-10-06 23:34:15','2018-10-07 18:21:47'),(1684,259,40,0.33,0.618,'KICK',2,1,'X','2018-10-06 23:34:15','2018-10-07 18:21:47'),(1685,259,40,0.1,0.618,'KICK',3,1,'X','2018-10-06 23:34:16','2018-10-07 18:21:47');
/*!40000 ALTER TABLE `pattern_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'ebroot','2017-02-04 17:36:22',142,1),(2,'2','account','SQL','V2__account.sql',-728725086,'ebroot','2017-02-04 17:36:23',117,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'ebroot','2017-02-04 17:36:23',54,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'ebroot','2017-02-04 17:36:23',387,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'ebroot','2017-02-04 17:36:23',226,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'ebroot','2017-02-04 17:36:24',525,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'ebroot','2017-02-04 17:36:24',162,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'ebroot','2017-02-04 17:36:24',64,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'ebroot','2017-02-04 17:36:24',51,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'ebroot','2017-02-04 17:36:24',36,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'ebroot','2017-02-04 17:36:24',13,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'ebroot','2017-02-04 17:36:24',48,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'ebroot','2017-02-04 17:36:25',54,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'ebroot','2017-02-04 17:36:25',54,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'ebroot','2017-02-04 17:36:25',48,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'ebroot','2017-02-04 17:36:25',89,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'ebroot','2017-02-04 17:36:25',130,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'ebroot','2017-02-04 17:36:25',382,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'ebroot','2017-02-04 17:36:26',241,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'ebroot','2017-02-04 17:36:26',143,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'ebroot','2017-02-05 23:06:15',315,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'ebroot','2017-02-07 03:21:14',29,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'ebroot','2017-02-07 03:21:14',125,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'ebroot','2017-02-10 00:03:21',205,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'ebroot','2017-02-10 00:03:21',107,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'ebroot','2017-02-10 00:03:25',3684,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'ebroot','2017-02-13 19:04:58',239,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'ebroot','2017-02-14 04:55:49',175,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'ebroot','2017-02-14 04:55:50',126,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'ebroot','2017-02-14 04:55:50',139,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'ebroot','2017-02-19 22:29:51',207,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'ebroot','2017-02-19 22:29:51',29,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'ebroot','2017-04-21 16:24:11',40,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'ebroot','2017-04-28 14:57:19',58,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'ebroot','2017-04-28 14:57:19',52,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'ebroot','2017-04-28 14:57:19',53,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'ebroot','2017-04-28 14:57:19',78,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'ebroot','2017-05-01 19:09:45',95,1),(39,'40','ALTER choice MODIFY COLUMN phase offset ULONG','SQL','V40__ALTER_choice_MODIFY_COLUMN_phase_offset_ULONG.sql',-240451169,'ebroot','2017-05-18 00:34:09',63,1),(40,'41','CREATE TABLE link meme','SQL','V41__CREATE_TABLE_link_meme.sql',-18883080,'ebroot','2017-05-18 00:34:09',51,1),(41,'42','ALTER phase link INT total','SQL','V42__ALTER_phase_link_INT_total.sql',-1400879099,'ebroot','2017-05-18 00:34:10',122,1),(42,'43','CREATE TABLE link message','SQL','V43__CREATE_TABLE_link_message.sql',1616909549,'ebroot','2017-05-18 00:34:10',46,1),(43,'44','ALTER pick BELONGS TO arrangement DROP morph point','SQL','V44__ALTER_pick_BELONGS_TO_arrangement_DROP_morph_point.sql',449955118,'ebroot','2017-05-26 00:58:12',563,1),(44,'45','ALTER link ADD COLUMN waveform key','SQL','V45__ALTER_link_ADD_COLUMN_waveform_key.sql',-98370,'ebroot','2017-06-01 16:53:07',811,1),(45,'46','ALTER audio ADD COLUMN state','SQL','V46__ALTER_audio_ADD_COLUMN_state.sql',-1300058820,'ebroot','2017-06-04 21:28:24',161,1),(46,'47','ALTER chain ADD COLUMN embed key','SQL','V47__ALTER_chain_ADD_COLUMN_embed_key.sql',317233573,'ebroot','2017-10-15 09:45:02',903,1),(47,'48','CREATE TABLE platform message','SQL','V48__CREATE_TABLE_platform_message.sql',-1332226532,'ebroot','2017-12-02 07:28:17',114,1),(48,'49','CREATE pattern DEPRECATES idea','SQL','V49__CREATE_pattern_DEPRECATES_idea.sql',517513730,'ebroot','2017-12-07 05:37:18',3380,1),(49,'50','REFACTOR voice BELONGS TO pattern','SQL','V50__REFACTOR_voice_BELONGS_TO_pattern.sql',1202195806,'ebroot','2018-01-03 21:36:08',712,1),(50,'51','DROP TABLE pick','SQL','V51__DROP_TABLE_pick.sql',-319463966,'ebroot','2018-01-04 03:41:36',849,1),(51,'52','ALTER phase ADD COLUMN type','SQL','V52__ALTER_phase_ADD_COLUMN_type.sql',-95957482,'ebroot','2018-01-05 07:32:08',602,1),(52,'53','ALTER chord MODIFY COLUMN position INTEGER','SQL','V53__ALTER_chord_MODIFY_COLUMN_position_INTEGER.sql',523400926,'ebroot','2018-01-10 21:53:43',4877,1),(53,'54','RENAME voice event TO phase event','SQL','V54__RENAME_voice_event_TO_phase_event.sql',-370585949,'ebroot','2018-01-17 06:06:10',56,1),(54,'55','ALTER pattern phase ADD COLUMN state','SQL','V55__ALTER_pattern_phase_ADD_COLUMN_state.sql',-1299872216,'ebroot','2018-02-03 00:56:45',460,1),(55,'56','ALTER chord MODIFY COLUMN position FLOAT','SQL','V56__ALTER_chord_MODIFY_COLUMN_position_FLOAT.sql',-894225407,'ebroot','2018-02-06 21:11:43',15080,1),(56,'57','REFACTORING chain segment sequence pattern','SQL','V57__REFACTORING_chain_segment_sequence_pattern.sql',-1235024870,'ebroot','2018-07-01 06:24:58',1815,1),(57,'58','ALTER pattern ADD COLUMNS meter','SQL','V58__ALTER_pattern_ADD_COLUMNS_meter.sql',1342735981,'ebroot','2018-09-07 19:09:16',393,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Charney Kaye','charneykaye@gmail.com','https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'Chris Luken','christopher.luken@gmail.com','https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'David Cole','davecolemusic@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-03-08 02:26:51','2017-03-08 02:26:51'),(4,'Shannon Holloway','shannon.holloway@gmail.com','https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50','2017-03-08 18:14:53','2017-03-08 18:14:53'),(5,'Lev Kaye','lev@kaye.com','https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50','2017-03-09 23:47:12','2017-03-09 23:47:12'),(6,'Justin Knowlden (gus)','gus@gusg.us','https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50','2017-04-14 20:41:41','2017-04-14 20:41:41'),(7,'dave farkas','sakrafd@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-14 20:42:36','2017-04-14 20:42:36'),(8,'Aji Putra','aji.perdana.putra@gmail.com','https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50','2017-04-21 17:33:25','2017-04-21 17:33:25'),(9,'live espn789','scoreplace@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-21 19:13:22','2017-04-21 19:13:22'),(10,'Dmitry Solomadin','dmitry.solomadin@gmail.com','https://lh6.googleusercontent.com/-Ns78xq2VzKk/AAAAAAAAAAI/AAAAAAAAE44/ZOuBZnZqYeU/photo.jpg?sz=50','2017-05-03 21:09:33','2017-05-03 21:09:33'),(11,'Michael Prolagaev','prolagaev@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-05-04 16:13:06','2017-05-04 16:13:06'),(12,'Charney Kaye','nick.c.kaye@gmail.com','https://lh5.googleusercontent.com/-_oXIqxZhTkk/AAAAAAAAAAI/AAAAAAAAUks/dg9oNRfPFco/photo.jpg?sz=50','2017-05-18 17:37:32','2017-05-18 17:37:32'),(13,'Charney Kaye','charney@outrightmental.com','https://lh5.googleusercontent.com/-3yrpEvNKIvE/AAAAAAAAAAI/AAAAAAAAASc/Gls7ZJcVqCk/photo.jpg?sz=50','2017-06-19 20:39:46','2017-06-19 20:39:46'),(14,'Philip Z. Kimball','pzkimball@pzklaw.com','https://lh4.googleusercontent.com/-xnsM2SBKwaE/AAAAAAAAAAI/AAAAAAAAABs/uJouNj6fMgw/photo.jpg?sz=50','2017-06-26 13:56:57','2017-06-26 13:56:57'),(15,'Janae\' Leonard','janaeleo55@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-06-28 09:30:40','2017-06-28 09:30:40'),(16,'yuan liu','minamotoclan@gmail.com','https://lh6.googleusercontent.com/-4orhpHPwHN4/AAAAAAAAAAI/AAAAAAAAFGc/HYueBarZIwA/photo.jpg?sz=50','2017-07-03 03:16:24','2017-07-03 03:16:24'),(17,'Nick Podgurski','nickpodgurski@gmail.com','https://lh5.googleusercontent.com/-Cly5aKHLBMc/AAAAAAAAAAI/AAAAAAAAAYQ/wu8BxP-Zwxk/photo.jpg?sz=50','2017-07-04 03:59:02','2017-07-04 03:59:02'),(18,'Brian Sweeny','brian@vibesinternational.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-05 16:01:53','2017-07-05 16:01:53'),(19,'John Bennett','johnalsobennett@gmail.com','https://lh6.googleusercontent.com/-kFMmioNSrEM/AAAAAAAAAAI/AAAAAAAABfg/SfT2vo__XgI/photo.jpg?sz=50','2017-07-06 15:08:32','2017-07-06 15:08:32'),(20,'Aditi Hebbar','adhebbar@gmail.com','https://lh4.googleusercontent.com/-gUnZUky1WtE/AAAAAAAAAAI/AAAAAAAAEJ8/sFumIpFdaUA/photo.jpg?sz=50','2017-07-07 08:42:46','2017-07-07 08:42:46'),(21,'HANKYOL CHO','hankyolcho@mail.adelphi.edu','https://lh3.googleusercontent.com/-skrgmZw2fas/AAAAAAAAAAI/AAAAAAAAAAA/iwMwVr_CL2U/photo.jpg?sz=50','2017-07-10 14:10:03','2017-07-10 14:10:03'),(22,'Charles Frantz','charlesfrantz@gmail.com','https://lh4.googleusercontent.com/-WtgVMTchHkY/AAAAAAAAAAI/AAAAAAAAAMU/4hX0mxVuIBE/photo.jpg?sz=50','2017-07-13 14:28:39','2017-07-13 14:28:39'),(23,'Alice Gamarnik','ajgamarnik@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-14 16:25:46','2017-07-14 16:25:46'),(24,'liu xin','xinliu2530@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-17 18:46:18','2017-07-17 18:46:18'),(25,'Outright Mental','outrightmental@gmail.com','https://lh5.googleusercontent.com/-2HcQgfYoQRU/AAAAAAAAAAI/AAAAAAAAANE/-ttDusZjeuk/photo.jpg?sz=50','2017-07-30 16:26:49','2017-07-30 16:26:49'),(26,'Joey Lorjuste','joeylorjuste@gmail.com','https://lh4.googleusercontent.com/-WPQgkyb-M5A/AAAAAAAAAAI/AAAAAAAAH-Q/Lf9IG0JJl5c/photo.jpg?sz=50','2017-08-20 19:25:12','2017-08-20 19:25:12'),(27,'Mark Stewart','mark.si.stewart@gmail.com','https://lh3.googleusercontent.com/-PtMRcK_-Bkg/AAAAAAAAAAI/AAAAAAAAASs/YlN0XjZSvdg/photo.jpg?sz=50','2017-08-25 19:30:40','2017-08-25 19:30:40'),(28,'Rosalind Kaye','rckaye@kaye.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-16 00:11:49','2017-10-16 00:11:49'),(29,'Matthew DellaRatta','mdellaratta8@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-17 00:00:36','2017-10-17 00:00:36'),(30,'Justice Whitaker','justice512@gmail.com','https://lh5.googleusercontent.com/-Y9sCwQKldqA/AAAAAAAAAAI/AAAAAAAAADE/3wU9xJLYRG0/photo.jpg?sz=50','2017-12-08 20:45:40','2017-12-08 20:45:40'),(31,'Ed Carney','ed@steirmancpas.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-12-13 15:13:49','2017-12-13 15:13:49'),(32,'Tamil Selvan','prtamil@gmail.com','https://lh6.googleusercontent.com/-iVWQ0iJwSvY/AAAAAAAAAAI/AAAAAAAAAJo/KlOXVs2XwFI/photo.jpg?sz=50','2018-02-04 08:50:11','2018-02-04 08:50:11'),(33,'Riyadh Abdullatif','coldmo@gmail.com','https://lh6.googleusercontent.com/-NQk0LpgjTc0/AAAAAAAAAAI/AAAAAAAAAGk/SCEchWKOh7g/photo.jpg?sz=50','2018-02-26 21:36:44','2018-02-26 21:36:44'),(34,'Ken Kaye','ken@kaye.com','https://lh3.googleusercontent.com/-r0rl7N0eE7g/AAAAAAAAAAI/AAAAAAAAAEc/IC1Dir_2XjE/photo.jpg?sz=50','2018-05-15 12:24:58','2018-05-15 12:24:58'),(35,'Eden Zhong','hydrosulfate@gmail.com','https://lh3.googleusercontent.com/-Ty-LN9tk8TQ/AAAAAAAAAAI/AAAAAAAADi4/J1bPsII4IFY/photo.jpg?sz=50','2018-06-04 20:57:29','2018-06-04 20:57:29'),(36,'Jacky Huang','jackyxhu@usc.edu','https://lh6.googleusercontent.com/-0MMr2iRpOHE/AAAAAAAAAAI/AAAAAAAAAH4/xXcj5T7YPbQ/photo.jpg?sz=50','2018-07-03 19:30:38','2018-07-03 19:30:38');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'user',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'admin',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'user',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,'artist',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(5,'artist',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(8,'user',4,'2017-03-08 18:14:53','2017-03-08 18:14:53'),(9,'artist',4,'2017-03-09 17:48:55','2017-03-09 17:48:55'),(10,'user',5,'2017-03-09 23:47:12','2017-03-09 23:47:12'),(11,'artist',5,'2017-03-10 05:39:23','2017-03-10 05:39:23'),(16,'user',8,'2017-04-21 17:33:25','2017-04-21 17:33:25'),(17,'user',9,'2017-04-21 19:13:22','2017-04-21 19:13:22'),(22,'user',12,'2017-05-18 17:37:32','2017-05-18 17:37:32'),(23,'artist',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(24,'engineer',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(25,'user',13,'2017-06-19 20:39:46','2017-06-19 20:39:46'),(26,'user',14,'2017-06-26 13:56:57','2017-06-26 13:56:57'),(27,'artist',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(28,'engineer',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(29,'user',15,'2017-06-28 09:30:40','2017-06-28 09:30:40'),(30,'user',16,'2017-07-03 03:16:24','2017-07-03 03:16:24'),(31,'user',17,'2017-07-04 03:59:02','2017-07-04 03:59:02'),(32,'user',18,'2017-07-05 16:01:53','2017-07-05 16:01:53'),(33,'user',19,'2017-07-06 15:08:32','2017-07-06 15:08:32'),(34,'user',20,'2017-07-07 08:42:46','2017-07-07 08:42:46'),(35,'banned',10,'2017-07-07 20:53:49','2017-07-07 20:53:49'),(36,'banned',11,'2017-07-07 20:53:55','2017-07-07 20:53:55'),(37,'user',21,'2017-07-10 14:10:03','2017-07-10 14:10:03'),(38,'user',22,'2017-07-13 14:28:39','2017-07-13 14:28:39'),(39,'artist',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(40,'engineer',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(43,'user',23,'2017-07-14 16:25:46','2017-07-14 16:25:46'),(44,'user',24,'2017-07-17 18:46:18','2017-07-17 18:46:18'),(45,'artist',24,'2017-07-17 18:46:58','2017-07-17 18:46:58'),(47,'artist',23,'2017-07-17 18:47:04','2017-07-17 18:47:04'),(49,'user',25,'2017-07-30 16:26:49','2017-07-30 16:26:49'),(50,'artist',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(51,'engineer',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(52,'artist',13,'2017-07-30 16:27:43','2017-07-30 16:27:43'),(53,'user',26,'2017-08-20 19:25:12','2017-08-20 19:25:12'),(54,'user',27,'2017-08-25 19:30:40','2017-08-25 19:30:40'),(55,'artist',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(56,'engineer',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(57,'user',28,'2017-10-16 00:11:49','2017-10-16 00:11:49'),(58,'user',29,'2017-10-17 00:00:36','2017-10-17 00:00:36'),(59,'user',30,'2017-12-08 20:45:40','2017-12-08 20:45:40'),(60,'artist',30,'2017-12-08 20:47:55','2017-12-08 20:47:55'),(61,'engineer',1,'2017-12-12 06:57:46','2017-12-12 06:57:46'),(62,'user',31,'2017-12-13 15:13:49','2017-12-13 15:13:49'),(63,'User',3,'2018-01-05 16:56:10','2018-01-05 16:56:10'),(64,'Artist',3,'2018-01-05 16:56:14','2018-01-05 16:56:14'),(65,'Engineer',3,'2018-01-05 16:56:18','2018-01-05 16:56:18'),(67,'Admin',6,'2018-01-07 21:34:23','2018-01-07 21:34:23'),(68,'Admin',7,'2018-01-07 21:34:52','2018-01-07 21:34:52'),(69,'Engineer',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(70,'Artist',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(71,'User',7,'2018-01-07 21:35:04','2018-01-07 21:35:04'),(72,'Engineer',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(73,'Artist',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(74,'User',6,'2018-01-07 21:37:40','2018-01-07 21:37:40'),(75,'User',32,'2018-02-04 08:50:11','2018-02-04 08:50:11'),(76,'User',33,'2018-02-26 21:36:44','2018-02-26 21:36:44'),(77,'Admin',27,'2018-03-22 19:22:02','2018-03-22 19:22:02'),(78,'User',34,'2018-05-15 12:24:58','2018-05-15 12:24:58'),(79,'User',35,'2018-06-04 20:57:29','2018-06-04 20:57:29'),(80,'User',36,'2018-07-03 19:30:38','2018-07-03 19:30:38');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice`
--

LOCK TABLES `voice` WRITE;
/*!40000 ALTER TABLE `voice` DISABLE KEYS */;
INSERT INTO `voice` VALUES (4,6,'percussive','Kick+Snare','2017-04-23 23:45:07','2018-01-03 21:36:07'),(5,6,'percussive','Locomotion','2017-06-03 00:04:07','2018-01-03 21:36:07'),(8,6,'percussive','Toms+Congas+Misc','2017-06-11 19:50:10','2018-01-03 21:36:07'),(10,6,'percussive','Vocal','2017-06-23 23:43:10','2018-01-03 21:36:07'),(11,6,'percussive','Vocal Echo','2017-06-24 01:29:49','2018-01-03 21:36:07'),(12,6,'Percussive','2x4 Stomp','2017-12-07 03:43:08','2018-01-03 21:36:07'),(17,6,'Percussive','Clave','2017-12-21 00:33:48','2018-01-03 21:36:07'),(18,29,'Percussive','Kick+Snare','2017-12-22 06:43:19','2018-01-03 21:36:07'),(19,29,'Percussive','Locomotion','2017-12-22 06:43:19','2018-01-03 21:36:07'),(20,29,'Percussive','Toms+Congas+Misc','2017-12-22 06:43:20','2018-01-03 21:36:07'),(23,29,'Percussive','Additional Stomp and Pop','2017-12-22 06:43:20','2018-03-16 00:46:18'),(24,29,'Percussive','Clave','2017-12-22 06:43:20','2018-01-03 21:36:07'),(36,34,'Percussive','Locomotion','2018-01-05 14:54:11','2018-01-05 14:54:11'),(37,34,'Percussive','Toms+Congas+Misc','2018-01-05 14:54:11','2018-01-05 14:54:11'),(39,34,'Percussive','Clave','2018-01-05 14:54:11','2018-01-05 14:54:11'),(40,70,'Percussive','Kick+Snare','2018-08-21 18:19:01','2018-08-21 18:19:01'),(41,70,'Percussive','Locomotion','2018-08-21 18:19:01','2018-08-21 18:19:01'),(42,70,'Percussive','Toms+Congas+Misc','2018-08-21 18:19:01','2018-08-21 18:19:01'),(43,70,'Percussive','Additional Stomp and Pop','2018-08-21 18:19:01','2018-08-21 18:19:01'),(44,70,'Percussive','Clave','2018-08-21 18:19:01','2018-08-21 18:19:01');
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

-- Dump completed on 2018-10-08 22:04:25




















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
