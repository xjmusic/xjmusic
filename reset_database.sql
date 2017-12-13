-- MySQL dump 10.13  Distrib 5.7.20, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: xj
-- ------------------------------------------------------
-- Server version	5.6.36

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=458818 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=163 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8;
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
  `value` mediumtext NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_config_fk_chain_idx` (`chain_id`),
  CONSTRAINT `chain_config_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_pattern`
--

DROP TABLE IF EXISTS `chain_pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_pattern` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_idea_fk_chain_idx` (`chain_id`),
  KEY `chain_pattern_fk_pattern` (`pattern_id`),
  CONSTRAINT `chain_idea_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_pattern_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `choice`
--

DROP TABLE IF EXISTS `choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `choice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `link_id` bigint(20) unsigned NOT NULL,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `transpose` int(11) NOT NULL,
  `phase_offset` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `choice_fk_link_idx` (`link_id`),
  KEY `choice_fk_pattern` (`pattern_id`),
  CONSTRAINT `choice_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=345747 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `link`
--

DROP TABLE IF EXISTS `link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `link` (
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
  KEY `link_fk_chain_idx` (`chain_id`),
  CONSTRAINT `link_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=839821 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `link_chord`
--

DROP TABLE IF EXISTS `link_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `link_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `link_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_link_idx` (`link_id`),
  CONSTRAINT `chord_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=791425 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `link_meme`
--

DROP TABLE IF EXISTS `link_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `link_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `link_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_link_idx` (`link_id`),
  CONSTRAINT `meme_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=518500 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `link_message`
--

DROP TABLE IF EXISTS `link_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `link_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `link_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `message_fk_link_idx` (`link_id`),
  CONSTRAINT `message_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=115344 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pattern`
--

DROP TABLE IF EXISTS `pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pattern` (
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pattern_fk_user` (`user_id`),
  KEY `pattern_fk_library` (`library_id`),
  CONSTRAINT `pattern_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pattern_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `phase`
--

DROP TABLE IF EXISTS `phase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phase` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `total` int(10) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `phase_fk_pattern` (`pattern_id`),
  CONSTRAINT `phase_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `phase_chord`
--

DROP TABLE IF EXISTS `phase_chord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phase_chord` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phase_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `position` float NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chord_fk_phase_idx` (`phase_id`),
  CONSTRAINT `chord_fk_phase` FOREIGN KEY (`phase_id`) REFERENCES `phase` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `phase_meme`
--

DROP TABLE IF EXISTS `phase_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phase_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phase_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_phase_idx` (`phase_id`),
  CONSTRAINT `meme_fk_phase` FOREIGN KEY (`phase_id`) REFERENCES `phase` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pick`
--

DROP TABLE IF EXISTS `pick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pick` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `arrangement_id` bigint(20) unsigned NOT NULL,
  `audio_id` bigint(20) unsigned NOT NULL,
  `start` float unsigned NOT NULL,
  `length` float unsigned NOT NULL,
  `amplitude` float unsigned NOT NULL,
  `pitch` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pick_fk_audio_idx` (`audio_id`),
  KEY `pick_fk_arrangement_idx` (`arrangement_id`),
  CONSTRAINT `pick_fk_arrangement` FOREIGN KEY (`arrangement_id`) REFERENCES `arrangement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pick_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=37062175 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voice`
--

DROP TABLE IF EXISTS `voice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phase_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(1023) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `voice_fk_phase_idx` (`phase_id`),
  CONSTRAINT `voice_fk_phase` FOREIGN KEY (`phase_id`) REFERENCES `phase` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voice_event`
--

DROP TABLE IF EXISTS `voice_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voice_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
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
  KEY `event_fk_voice_idx` (`voice_id`),
  CONSTRAINT `event_fk_voice` FOREIGN KEY (`voice_id`) REFERENCES `voice` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=334 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;






-- MySQL dump 10.13  Distrib 5.5.57, for Linux (x86_64)
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
INSERT INTO `account` VALUES (1,'Alpha','2017-02-10 00:03:21','2017-05-03 22:15:24'),(2,'Ambience','2017-02-10 00:03:21','2017-12-12 21:56:12');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_user`
--

LOCK TABLES `account_user` WRITE;
/*!40000 ALTER TABLE `account_user` DISABLE KEYS */;
INSERT INTO `account_user` VALUES (1,1,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(2,1,2,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(4,2,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(5,1,3,'2017-03-08 02:27:34','2017-03-08 02:27:34'),(7,1,5,'2017-03-10 05:39:37','2017-03-10 05:39:37'),(8,1,6,'2017-04-17 21:59:20','2017-04-17 21:59:20'),(9,1,7,'2017-04-17 21:59:24','2017-04-17 21:59:24'),(12,1,12,'2017-05-18 17:39:30','2017-05-18 17:39:30'),(13,1,14,'2017-06-26 14:48:43','2017-06-26 14:48:43'),(14,1,22,'2017-07-13 15:18:39','2017-07-13 15:18:39'),(15,1,23,'2017-07-17 18:47:27','2017-07-17 18:47:27'),(16,1,24,'2017-07-17 18:47:33','2017-07-17 18:47:33'),(17,1,13,'2017-07-30 16:28:43','2017-07-30 16:28:43'),(18,1,25,'2017-07-30 16:30:06','2017-07-30 16:30:06'),(19,1,27,'2017-08-25 19:47:29','2017-08-25 19:47:29'),(20,2,3,'2017-08-25 19:47:40','2017-08-25 19:47:40'),(21,2,27,'2017-08-25 19:47:45','2017-08-25 19:47:45'),(23,1,28,'2017-12-07 19:01:44','2017-12-07 19:01:44'),(24,1,30,'2017-12-08 20:46:14','2017-12-08 20:46:14');
/*!40000 ALTER TABLE `account_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio`
--

LOCK TABLES `audio` WRITE;
/*!40000 ALTER TABLE `audio` DISABLE KEYS */;
INSERT INTO `audio` VALUES (2,3,'Kick','80454e35-5693-4b42-aa6a-218383a9f584-instrument-3-audio.wav',0,0.702,120,57.495,'2017-04-21 16:41:03','2017-07-26 22:08:30','Published'),(3,3,'Kick Long','ed1957b9-eea0-42f8-8493-b8874e1a6bf9-instrument-3-audio.wav',0,0.865,120,57.05,'2017-04-21 18:52:17','2017-07-26 22:08:30','Published'),(4,3,'Hihat Closed','0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14-instrument-3-audio.wav',0,0.053,120,6300,'2017-04-21 19:33:05','2017-07-26 22:08:30','Published'),(5,3,'Maracas','ffe4edd6-5b83-4ac9-8e69-156ddb06762f-instrument-3-audio.wav',0,0.026,120,190.086,'2017-04-21 19:38:16','2017-07-26 22:08:30','Published'),(6,3,'Snare','7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe-instrument-3-audio.wav',0,0.093,120,177.823,'2017-04-21 19:42:59','2017-07-26 22:08:30','Published'),(7,3,'Tom','a6bf0d86-6b45-4cf1-b404-2242095c7876-instrument-3-audio.wav',0,0.36,120,104.751,'2017-04-21 19:43:58','2017-07-26 22:08:30','Published'),(8,3,'Claves','aea2483c-7707-4100-aa86-b680668cd1a0-instrument-3-audio.wav',0,0.03,120,2594,'2017-04-23 23:59:47','2017-07-26 22:08:30','Published'),(9,3,'Conga','f772f19f-b51b-414e-9dc8-8ceb23faa779-instrument-3-audio.wav',0,0.26,120,213,'2017-04-24 00:03:32','2017-07-26 22:08:30','Published'),(10,3,'Conga High','c0975d3a-4f26-44b2-a9d3-800320bfa3e1-instrument-3-audio.wav',0,0.179,120,397.297,'2017-04-24 00:05:34','2017-07-26 22:08:30','Published'),(11,3,'Tom High','aea1351b-bb96-4487-8feb-ae8ad3e499ad-instrument-3-audio.wav',0,0.2,120,190.909,'2017-04-24 02:18:29','2017-07-26 22:08:30','Published'),(12,3,'Clap','ce0662a2-3f7e-425b-8105-fb639d395235-instrument-3-audio.wav',0,0.361,120,1102.5,'2017-04-24 02:21:04','2017-07-26 22:08:30','Published'),(13,3,'Cowbell','aaa877a8-0c89-4781-93f8-69c722285b2a-instrument-3-audio.wav',0,0.34,120,268.902,'2017-04-24 02:22:47','2017-07-26 22:08:30','Published'),(14,3,'Cymbal Crash','37a35a63-23e4-4ef6-a78e-db2577aa9a00-instrument-3-audio.wav',0,2.229,120,109.701,'2017-04-24 02:24:03','2017-07-26 22:08:30','Published'),(15,3,'Hihat Open','020ad575-af86-4fe2-a869-957d50d59ac4-instrument-3-audio.wav',0,0.598,120,7350,'2017-04-24 02:25:31','2017-07-26 22:08:30','Published'),(16,3,'Snare Rim','58fd7eae-b55e-4567-9c27-ead64b83488a-instrument-3-audio.wav',0,0.014,120,445.445,'2017-04-24 02:26:53','2017-07-26 22:08:30','Published'),(22,4,'Hihat Closed 9','0f28ef83-2213-4bbb-ae68-3eecc201ead3-instrument-4-audio.wav',0,0.849,120,648.529,'2017-06-15 22:43:24','2017-07-26 22:08:30','Published'),(23,4,'Hihat Closed 7','e15dc427-b556-4a72-bec8-6b59c6d8bbc8-instrument-4-audio.wav',0.003,0.962,120,8820,'2017-06-15 22:44:55','2017-07-26 22:08:30','Published'),(24,4,'Hihat Closed 8','cb1ffbff-c31d-4e06-9d84-649c1f257a24-instrument-4-audio.wav',0,0.905,120,648.529,'2017-06-15 22:45:52','2017-07-26 22:08:30','Published'),(26,4,'Snare Rim','7b2d94b3-c218-498b-906e-11c313054cd1-instrument-4-audio.wav',0,1.147,120,239.674,'2017-06-15 22:56:58','2017-07-26 22:08:30','Published'),(27,4,'Hihat Open 5','bf2c9ad8-ceb4-4c7e-98ae-a9c561680a1f-instrument-4-audio.wav',0.003,1.115,120,648.529,'2017-06-15 23:04:16','2017-07-26 22:08:30','Published'),(28,4,'Hihat Open 7','4c3c5673-e8f1-4452-ad8c-5466cce0492d-instrument-4-audio.wav',0,2,120,648.529,'2017-06-15 23:06:14','2017-07-26 22:08:30','Published'),(29,4,'Hihat Open 6','9a57a402-98e9-4ceb-86c2-ea60607b56d1-instrument-4-audio.wav',0,0.809,120,648.529,'2017-06-15 23:07:41','2017-07-26 22:08:30','Published'),(30,4,'Stick Side 7','ea042c27-551b-44c7-998b-1df185d319cf-instrument-4-audio.wav',0.003,0.159,120,436.634,'2017-06-15 23:11:51','2017-07-26 22:08:30','Published'),(31,4,'Stick Side 6','0d65a838-e76f-407d-a06b-6485d67ba44c-instrument-4-audio.wav',0,0.335,120,2321.05,'2017-06-15 23:13:00','2017-07-26 22:08:30','Published'),(32,4,'Stick Side 5','99f7dbea-c1fb-419e-ad44-c90804516aa3-instrument-4-audio.wav',0,0.248,120,1837.5,'2017-06-15 23:14:30','2017-07-26 22:08:30','Published'),(33,4,'Snare Rim 7','12e36076-5944-4101-a41b-b39136cf78a4-instrument-4-audio.wav',0,0.461,120,254.913,'2017-06-15 23:15:43','2017-07-26 22:08:30','Published'),(34,4,'Snare Rim 6','5a840f38-7623-442b-b9a9-a0ff1927c7a0-instrument-4-audio.wav',0,0.527,120,245,'2017-06-15 23:16:36','2017-07-26 22:08:30','Published'),(35,4,'Snare Rim 5','d404857a-6bf8-43c4-ad76-5259945d16fe-instrument-4-audio.wav',0,0.463,120,181.481,'2017-06-15 23:17:44','2017-07-26 22:08:30','Published'),(36,4,'Tom High','4888db8b-1c81-4178-8af5-332ae7067ca8-instrument-4-audio.wav',0.002,0.42,120,187.66,'2017-06-15 23:20:38','2017-07-26 22:08:30','Published'),(37,4,'Snare 3','d373a2f8-8c8f-4afa-b7e3-c21623d15f42-instrument-4-audio.wav',0.008,0.404,120,2450,'2017-06-15 23:21:50','2017-07-26 22:08:30','Published'),(38,4,'Tom','d5bcc3a5-d98f-434f-8fcb-987f1913a684-instrument-4-audio.wav',0.009,0.445,120,225,'2017-06-15 23:22:45','2017-07-26 22:08:30','Published'),(39,4,'Conga High','511f5a68-1eca-4ca3-9713-956a219d734c-instrument-4-audio.wav',0.002,0.425,120,187.66,'2017-06-15 23:24:15','2017-07-26 22:08:30','Published'),(40,4,'Conga','2059cab7-8052-46cf-8fd1-2930cfe5ce59-instrument-4-audio.wav',0.001,0.547,120,183.231,'2017-06-15 23:25:03','2017-07-26 22:08:30','Published'),(41,4,'Snare 5','cce1763b-fca3-49c5-9024-c665c1fea7f3-instrument-4-audio.wav',0.008,0.407,120,180.738,'2017-06-15 23:25:58','2017-07-26 22:08:30','Published'),(42,4,'Snare 4','511168e1-3291-4ec8-a6ac-652249206287-instrument-4-audio.wav',0.008,0.439,120,204.167,'2017-06-15 23:27:04','2017-07-26 22:08:30','Published'),(43,4,'Kick 7','2fd75fb8-b968-46ba-8c43-ac6ad2db9a80-instrument-4-audio.wav',0.008,0.537,120,43.534,'2017-06-16 00:20:54','2017-07-26 22:08:30','Published'),(44,4,'Kick 3','3a79549f-cf7b-4338-8756-f75b3fc5deaa-instrument-4-audio.wav',0.005,0.742,120,52.128,'2017-06-16 00:24:47','2017-07-26 22:08:30','Published'),(45,4,'Kick 3','c076a674-1626-4b22-bc07-a639ca90b363-instrument-4-audio.wav',0.01,0.677,120,56.178,'2017-06-16 00:27:07','2017-07-26 22:08:30','Published'),(48,4,'Tom Low 5','246190da-65fd-41a9-a943-2c8e3b763fa5-instrument-4-audio.wav',0,0.73,120,84.483,'2017-06-16 00:33:57','2017-07-26 22:08:30','Published'),(49,4,'Tom 5','bf45a337-c86a-4c44-9663-06093d3ca9ba-instrument-4-audio.wav',0,0.59,120,90.928,'2017-06-16 00:35:25','2017-07-26 22:08:30','Published'),(50,4,'Tom High 5','83294480-eef2-4171-8d69-8f16092557df-instrument-4-audio.wav',0.003,0.444,120,126,'2017-06-16 00:36:37','2017-07-26 22:08:30','Published'),(51,4,'Kick Long 2','b12bf5ff-ebec-47e3-9259-6cd0c9f57724-instrument-4-audio.wav',0.01,1.476,120,59.036,'2017-06-16 00:39:02','2017-07-26 22:08:30','Published'),(54,4,'Clap 1','27b08205-9921-4d48-bc54-ba4110fe238f-instrument-4-audio.wav',0,0.572,120,185.294,'2017-06-16 02:15:47','2017-07-26 22:08:30','Published'),(55,4,'Clap 2','81f55d83-39fe-4832-99bf-4e4f3af69496-instrument-4-audio.wav',0,0.684,120,188.462,'2017-06-16 02:17:11','2017-07-26 22:08:30','Published'),(56,3,'Kick 2','a731fc44-5ae0-4e9f-a728-edfe1895da4b-instrument-3-audio.wav',0,0.34,120,69.122,'2017-06-16 03:01:06','2017-07-26 22:08:30','Published'),(57,3,'Kick Long 2','84b1974c-02b0-406f-b78e-21414282986e-instrument-3-audio.wav',0,1.963,120,60.494,'2017-06-16 03:04:09','2017-07-26 22:08:30','Published'),(58,3,'Tom High 2','618bc8e5-f51f-4635-895c-5bd6522f8d8c-instrument-3-audio.wav',0.002,0.411,120,201.37,'2017-06-16 03:06:30','2017-07-26 22:08:30','Published'),(59,3,'Tom Low 2','014c8939-c9e7-4911-9620-9c4075a3b4a2-instrument-3-audio.wav',0,0.701,120,111.646,'2017-06-16 03:07:20','2017-07-26 22:08:30','Published'),(60,3,'Tom 2','3fcb76bf-6168-4aef-a160-facd1bb18071-instrument-3-audio.wav',0,0.488,120,149.492,'2017-06-16 03:09:50','2017-07-26 22:08:30','Published'),(61,3,'Clap 2','9a3e9e07-b1dd-44a5-9399-3b6c11bd72b1-instrument-3-audio.wav',0.002,0.356,120,1225,'2017-06-16 03:13:28','2017-07-26 22:08:30','Published'),(62,3,'Clap 3','f24484dd-b879-42c5-9c2a-71857555c319-instrument-3-audio.wav',0,0.734,120,980,'2017-06-16 03:14:41','2017-07-26 22:08:30','Published'),(63,3,'Maracas 2','f20dcce7-a936-446c-8692-c8caf37d8896-instrument-3-audio.wav',0.009,0.43,120,11025,'2017-06-16 03:17:11','2017-07-26 22:08:30','Published'),(64,4,'Cowbell','392a388d-8e32-46f9-ad57-b3bd29929262-instrument-4-audio.wav',0.002,0.298,120,525,'2017-06-16 03:20:04','2017-07-26 22:08:30','Published'),(65,4,'Cymbal Crash 1','378df92f-aec2-4a5c-9243-d08384971761-instrument-4-audio.wav',0.018,1.878,120,1297.06,'2017-06-16 03:21:46','2017-07-26 22:08:30','Published'),(66,4,'Cymbal Crash 2','b921f58d-1ce0-4c1e-82d0-08479c25bfff-instrument-4-audio.wav',0.01,3.241,120,469.149,'2017-06-16 03:24:03','2017-07-26 22:08:30','Published'),(67,4,'Cymbal Crash 3','484d5dc0-4627-477d-8de7-f4c30cc4f538-instrument-4-audio.wav',0.01,3.044,120,181.481,'2017-06-16 03:25:34','2017-07-26 22:08:30','Published'),(68,3,'Cymbal Crash 2','bb3e2a48-8f59-4ad0-a05f-30aca579524f-instrument-3-audio.wav',0,2,120,816.667,'2017-06-16 03:28:35','2017-07-26 22:08:30','Published'),(69,5,'Hihat Closed A_3','86d61872-a9bf-4b68-b4df-397be09bfe5c-instrument-5-audio.wav',0.007,1.051,120,3428.57,'2017-06-20 23:11:42','2017-07-27 17:35:01','Published'),(70,5,'Hihat Closed A_4','92f61e58-7225-48bb-91f3-b71fcf7aef5a-instrument-5-audio.wav',0,0.623,120,888.889,'2017-06-20 23:17:39','2017-07-27 17:35:07','Published'),(71,5,'Hihat Closed A_5','8a536dae-3727-488f-8895-a0b047620a38-instrument-5-audio.wav',0.001,0.537,120,888.889,'2017-06-20 23:19:00','2017-07-27 17:34:31','Published'),(72,5,'Hihat Closed A_6','e173c291-60d6-4f9a-a422-d2d8c99bd9b3-instrument-5-audio.wav',0.003,0.425,120,3428.57,'2017-06-20 23:35:06','2017-07-27 17:34:36','Published'),(73,5,'Hihat Closed A_7','de082694-4a02-48a4-92d1-83c2d2b7dd92-instrument-5-audio.wav',0.001,0.6,120,1263.16,'2017-06-20 23:36:34','2017-07-27 17:34:40','Published'),(74,5,'Hihat Closed A_8','7cbe09b2-5fe6-4d7a-b5fa-2f85624e91f5-instrument-5-audio.wav',0,0.73,120,1200,'2017-06-20 23:37:43','2017-07-27 17:34:43','Published'),(75,5,'Hihat Closed A_9','96df8da4-5be9-4a0f-a97b-5f8c0d28f161-instrument-5-audio.wav',0,0.432,120,1454.55,'2017-06-20 23:38:52','2017-07-27 17:34:48','Published'),(76,5,'Hihat Closed A_10','e4a06acb-c375-4e9b-a5ce-153b815fe6cb-instrument-5-audio.wav',0.002,0.307,120,3000,'2017-06-20 23:40:36','2017-07-27 17:34:58','Published'),(77,5,'Hihat Open F_1','4eb40925-8e37-4801-ba2e-cce991c97093-instrument-5-audio.wav',0,0.969,120,428.155,'2017-06-20 23:45:30','2017-07-27 17:34:01','Published'),(78,5,'Hihat Open F_2','13db8e43-4266-444a-9edd-c5a5cb2442b4-instrument-5-audio.wav',0,1.506,120,182.988,'2017-06-20 23:46:32','2017-07-27 17:34:05','Published'),(79,5,'Hihat Open F_3','3f0dbe3a-d11a-4e9f-a642-befe5747dd01-instrument-5-audio.wav',0,2.567,120,183.75,'2017-06-20 23:47:22','2017-07-27 17:34:08','Published'),(80,5,'Hihat Open F_4','57ff6b97-fedb-4e3f-b963-840ba8fd101b-instrument-5-audio.wav',0.035,2.617,120,416.038,'2017-06-20 23:48:41','2017-07-27 17:33:29','Published'),(81,5,'Hihat Open F_5','70c7404e-1f17-4a32-8f4a-ff28e7d5797c-instrument-5-audio.wav',0,2.734,120,420,'2017-06-20 23:49:48','2017-07-27 17:33:34','Published'),(82,5,'Hihat Open F_6','ed5b3f4c-a6e3-424b-b8ba-34c317640903-instrument-5-audio.wav',0,1.348,120,432.353,'2017-06-20 23:50:30','2017-07-27 17:33:38','Published'),(83,5,'Hihat Open F_7','8d7c72dc-92bb-4ffa-82ff-13750c8ddbfc-instrument-5-audio.wav',0,2.264,120,183.75,'2017-06-20 23:51:23','2017-07-27 17:33:42','Published'),(84,5,'Hihat Open F_8','7eae03f7-d1aa-42e2-a928-ff6f7b00b25d-instrument-5-audio.wav',0,2.595,120,182.988,'2017-06-20 23:51:59','2017-07-27 17:33:48','Published'),(85,5,'Tom H_1','2f4bf7a2-744e-47cc-b5c2-da0a846cab91-instrument-5-audio.wav',0,1.008,120,1422.58,'2017-06-20 23:54:11','2017-07-27 17:27:27','Published'),(86,5,'Tom H_2','2c2d8ba8-911b-4480-a774-c37102c12e90-instrument-5-audio.wav',0.009,2.036,120,1378.12,'2017-06-20 23:56:11','2017-07-27 17:27:52','Published'),(87,5,'Tom H_8','0e5c97c1-ad2a-4cb5-a1f5-10224c7cec3c-instrument-5-audio.wav',0,2.698,120,1633.33,'2017-06-20 23:58:39','2017-07-27 17:27:11','Published'),(88,5,'Tom H_7','2a525acb-dc9a-47f4-b105-89dc3332d78b-instrument-5-audio.wav',0,1.738,120,1764,'2017-06-20 23:59:37','2017-07-27 17:26:57','Published'),(89,5,'Tom H_6','91f5c7de-609d-48fd-a527-c7b132ee2af5-instrument-5-audio.wav',0,2.984,120,1336.36,'2017-06-21 00:00:19','2017-07-27 17:26:42','Published'),(90,5,'Tom H_5','c18a2f87-df5f-421a-aa59-89fda817210c-instrument-5-audio.wav',0,3.133,120,189.27,'2017-06-21 00:01:06','2017-07-27 17:26:10','Published'),(91,5,'Tom H_4','ee21d28c-6102-4ad7-96a5-49cf5ccaf266-instrument-5-audio.wav',0,2.815,120,186.076,'2017-06-21 00:01:52','2017-07-27 17:26:06','Published'),(92,5,'Tom H_3','8494ac91-a1ef-4045-9f1f-3a1b4a53ee3d-instrument-5-audio.wav',0,2.346,120,1378.12,'2017-06-21 00:02:27','2017-07-27 17:28:04','Published'),(93,5,'Snare Q_1','21369f18-b2b6-4d8b-bd28-de36f294b67e-instrument-5-audio.wav',0,1.206,120,5512.5,'2017-06-21 00:13:47','2017-07-27 17:19:35','Published'),(94,5,'Snare Q_11','88ba75c5-9727-43a3-9ef0-856abe729f78-instrument-5-audio.wav',0,1.524,120,6300,'2017-06-21 00:14:33','2017-07-27 17:19:52','Published'),(95,5,'Snare Q_10','b14d6a26-1e35-4f7c-bbfb-6fd262c2d35f-instrument-5-audio.wav',0,1.631,120,1378.12,'2017-06-21 00:15:27','2017-07-27 17:19:44','Published'),(96,5,'Snare Q_9','0818bf78-3838-43a5-8665-7f8f2814bfc4-instrument-5-audio.wav',0.003,0.583,120,249.153,'2017-06-21 00:49:26','2017-07-27 17:18:07','Published'),(97,5,'Snare Q_8','725e8281-c845-4a87-9a37-9117b1e6a830-instrument-5-audio.wav',0.002,0.799,120,355.645,'2017-06-21 00:51:02','2017-07-27 17:19:07','Published'),(98,5,'Snare Q_7','7fd96254-d9cf-4ad6-9899-dee564543853-instrument-5-audio.wav',0.001,0.653,120,5512.5,'2017-06-21 00:52:59','2017-07-27 17:18:59','Published'),(99,5,'Snare Q_6','83fbed4b-648c-4886-9079-f220fb0dc9fb-instrument-5-audio.wav',0.001,0.659,120,134.451,'2017-06-21 00:54:25','2017-07-27 17:18:54','Published'),(100,5,'Snare Q_5','62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav',0.002,0.405,120,1025.58,'2017-06-21 00:55:25','2017-07-27 17:18:48','Published'),(101,5,'Snare Q_4','8e17510c-a877-42a6-addc-95ef7d559757-instrument-5-audio.wav',0.001,1.257,120,5512.5,'2017-06-21 00:56:51','2017-07-27 17:18:40','Published'),(102,5,'Snare Q_3','a448d6b9-4669-4f17-883a-8dd8c5ce0b8e-instrument-5-audio.wav',0,0.915,120,5512.5,'2017-06-21 00:58:15','2017-07-27 17:20:02','Published'),(103,5,'Snare Q_2','23d5847f-56e6-4b79-99ad-6dfd13b9c5b3-instrument-5-audio.wav',0.001,1.008,120,6300,'2017-06-21 00:59:33','2017-07-27 17:19:57','Published'),(104,5,'Conga M_8','1c5f4752-e790-47a0-b0d9-4eedd54b24a5-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:04:11','2017-07-27 17:20:52','Published'),(105,5,'Tom L_1','568d1c74-a43e-44fc-ab53-0d1d701f6f0f-instrument-5-audio.wav',0,0.851,120,364.463,'2017-06-21 01:05:28','2017-07-27 17:24:19','Published'),(106,5,'Conga M_9','2d2d76f7-9d76-41c6-9e55-0b94703d487c-instrument-5-audio.wav',0,0.407,120,531.325,'2017-06-21 01:06:20','2017-07-27 17:20:58','Published'),(107,5,'Conga M_7','02dde877-01b4-432d-8d22-f1458917154b-instrument-5-audio.wav',0.001,0.502,120,420,'2017-06-21 01:07:04','2017-07-27 17:20:46','Published'),(108,5,'Conga M_6','3bdc44e7-e464-4a0f-a080-ab3d529ac9dc-instrument-5-audio.wav',0.001,0.512,120,612.5,'2017-06-21 01:07:52','2017-07-27 17:20:40','Published'),(109,5,'Conga M_5','0e47652d-265b-4c83-8c4f-c14a34fc9689-instrument-5-audio.wav',0,0.466,120,612.5,'2017-06-21 01:08:48','2017-07-27 17:20:34','Published'),(110,5,'Conga M_4','f6e912f5-d582-4044-b73b-6e004bb32a15-instrument-5-audio.wav',0,0.6,120,612.5,'2017-06-21 01:09:30','2017-07-27 17:22:48','Published'),(111,5,'Conga M_3','710b3011-cb1e-4065-a514-1e6e4fd19bec-instrument-5-audio.wav',0,0.427,120,612.5,'2017-06-21 01:10:06','2017-07-27 17:22:42','Published'),(112,5,'Conga M_3','c8d1affb-9b7c-4661-bf31-cd80dc2a9ce1-instrument-5-audio.wav',0,0.602,120,588,'2017-06-21 01:10:58','2017-07-27 17:22:38','Published'),(113,5,'Conga M_1','983fc7a1-a1ef-466f-be44-cc1e227ae449-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:11:49','2017-07-27 17:22:34','Published'),(114,5,'Conga M_1','faf2e9c6-6b12-445e-9b2c-93966451ff5e-instrument-5-audio.wav',0,0.318,120,565.385,'2017-06-21 01:12:52','2017-07-27 17:21:29','Published'),(115,5,'Tom L_10','38c92218-882d-4714-a493-14261e07c4fa-instrument-5-audio.wav',0,0.741,120,302.055,'2017-06-21 01:13:30','2017-07-27 17:24:25','Published'),(116,5,'Tom L_9','50f516a9-faaa-4091-848d-651d96ecc7be-instrument-5-audio.wav',0,0.751,120,176.4,'2017-06-21 01:14:17','2017-07-27 17:24:05','Published'),(117,5,'Tom L_8','f1bac880-fede-4c5d-9249-956f5e179d62-instrument-5-audio.wav',0,0.835,120,290.132,'2017-06-21 01:15:03','2017-07-27 17:24:01','Published'),(119,5,'Tom L_7','b51678cb-50a0-4994-980a-62bf126ca445-instrument-5-audio.wav',0.001,0.674,120,531.325,'2017-06-21 01:19:44','2017-07-27 17:23:58','Published'),(120,5,'Tom L_6','01e988c0-3821-4ba2-8223-70643f3c27cf-instrument-5-audio.wav',0,0.736,120,408.333,'2017-06-21 01:20:53','2017-07-27 17:23:54','Published'),(121,5,'Tom L_5','f6f79c74-f1e0-459b-9728-46f59bd14ee7-instrument-5-audio.wav',0.001,0.608,120,428.155,'2017-06-21 01:21:54','2017-07-27 17:23:50','Published'),(122,5,'Tom L_4','2b9af025-2616-4d03-890f-b74df3413abe-instrument-5-audio.wav',0,0.592,120,11025,'2017-06-21 01:22:35','2017-07-27 17:24:36','Published'),(123,5,'Tom L_3','dd32a686-ef3a-43c4-a3e1-13353d067026-instrument-5-audio.wav',0,0.624,120,110.526,'2017-06-21 01:23:21','2017-07-27 17:24:32','Published'),(124,5,'Tom L_2','6ffdef87-909f-4b67-a2f7-fadbb3a76e33-instrument-5-audio.wav',0,0.528,120,257.895,'2017-06-21 01:23:58','2017-07-27 17:24:29','Published'),(126,3,'Vocal Hie','0248ed87-19e8-449c-9211-4722d6ab8342-instrument-3-audio.wav',0.08,0.477,120,364.463,'2017-06-23 23:53:49','2017-07-26 22:08:31','Published'),(127,3,'Vocal Ahh','d35678fa-f163-433d-8741-250a530b5532-instrument-3-audio.wav',0.012,1.037,120,948.696,'2017-06-23 23:55:53','2017-07-26 22:08:31','Published'),(128,3,'Vocal Hoo','54d3503d-af44-4480-a0d0-8044fb403c5a-instrument-3-audio.wav',0.079,0.45,120,205.116,'2017-06-23 23:57:01','2017-07-26 22:08:31','Published'),(129,3,'Vocal Haa','79b9c4f4-037a-4f6f-bc51-7a7a2dff5528-instrument-3-audio.wav',0.053,0.36,120,864.706,'2017-06-23 23:57:45','2017-07-26 22:08:31','Published'),(132,3,'Vocal Eow','0e2d5fb2-9d40-4741-9da8-bc9943722d66-instrument-3-audio.wav',0.045,0.486,120,383.478,'2017-06-24 00:00:25','2017-07-26 22:08:31','Published'),(133,3,'Vocal Grunt Ooh 2','8896e8d4-0c31-4dd8-93ff-6982a30febdb-instrument-3-audio.wav',0.015,0.247,120,404.587,'2017-06-24 00:10:49','2017-07-26 22:08:31','Published'),(134,3,'Vocal Grunt Ooh','ef489ad1-fb9d-4e77-9b5c-a7b3570c8c09-instrument-3-audio.wav',0.011,0.213,120,1696.15,'2017-06-24 00:11:31','2017-07-26 22:08:31','Published'),(135,4,'Vocal JB Get','e5e8a85b-1c3c-46b5-8394-3b44b5c7e6e1-instrument-4-audio.wav',0.027,0.311,120,386.842,'2017-06-24 00:13:30','2017-07-26 22:08:31','Published'),(136,4,'Vocal JB Baz','76a3e02c-979c-4d64-9bab-3b1a91d3635d-instrument-4-audio.wav',0.018,0.405,120,918.75,'2017-06-24 00:14:55','2017-07-26 22:08:31','Published'),(137,4,'Vocal JB Get 2','22efe6d1-3dea-45a5-906c-1e4bd4465606-instrument-4-audio.wav',0.027,0.29,120,386.842,'2017-06-24 00:16:15','2017-07-26 22:08:31','Published'),(138,4,'Vocal JB Baz2','94bd651e-ce98-4b09-95b8-6e36819e2721-instrument-4-audio.wav',0.032,0.29,120,367.5,'2017-06-24 00:17:37','2017-07-26 22:08:31','Published'),(139,4,'Vocal JB Uhh','3bc65d7a-00a0-42cc-9d15-292f9fbe98ee-instrument-4-audio.wav',0,0.408,120,474.194,'2017-06-24 00:20:34','2017-07-26 22:08:31','Published'),(140,4,'Vocal Woo','c7b78912-493a-4e19-a023-10a6b334e2b3-instrument-4-audio.wav',0.01,0.522,120,464.211,'2017-06-24 00:22:32','2017-07-26 22:08:31','Published'),(141,4,'Vocal JB Me','3fbbf18b-eb45-4375-8bd2-efd5e490c4cb-instrument-4-audio.wav',14,0.336,120,367.5,'2017-06-24 00:23:45','2017-07-26 22:08:31','Published'),(143,4,'Vocal JB Hit','686906da-cc85-4abb-a902-121e98def35d-instrument-4-audio.wav',0.05,0.313,120,512.791,'2017-06-24 00:25:58','2017-07-26 22:08:31','Published'),(144,4,'Vocal Hey','5d808588-5930-4075-a034-4f96b0e2b06f-instrument-4-audio.wav',0.046,0.453,120,760.345,'2017-06-24 00:26:50','2017-07-26 22:08:31','Published'),(145,4,'Vocal Ehh','7806beda-4655-4323-adb0-d9a41d2fc939-instrument-4-audio.wav',0.018,0.297,120,648.529,'2017-06-24 00:27:36','2017-07-26 22:08:31','Published'),(146,4,'Vocal Eh','a6049156-69e0-4128-a4b1-6a17ee4ca0bd-instrument-4-audio.wav',0.018,0.449,120,668.182,'2017-06-24 00:28:28','2017-07-26 22:08:31','Published'),(147,5,'Vocal Watch Me','649a2969-6b98-4201-89fc-968d6414f578-instrument-5-audio.wav',0.05,0.807,120,1225,'2017-06-24 00:29:58','2017-07-26 22:08:31','Published'),(148,5,'Vocal Play It','53fc9c8c-2412-4133-b088-9bac349e6794-instrument-5-audio.wav',0.064,0.358,120,116.053,'2017-06-24 00:31:33','2017-07-26 22:08:31','Published'),(149,5,'Vocal Hoh','5709e633-bd69-407b-b6ba-420395b221de-instrument-5-audio.wav',0.028,0.476,120,689.062,'2017-06-24 00:32:33','2017-07-26 22:08:31','Published'),(150,5,'Vocal Woah','7ac9d00c-0b24-49ad-8cbb-c586ac0f080f-instrument-5-audio.wav',0.02,0.488,120,604.11,'2017-06-24 00:33:44','2017-07-26 22:08:31','Published'),(151,5,'Vocal What 3','489c5976-cbda-4449-a8cf-67d653b77dbf-instrument-5-audio.wav',0.04,0.407,120,370.588,'2017-06-24 00:34:40','2017-07-26 22:08:31','Published'),(152,5,'Vocal What 2','70d22a2a-a888-460f-9dfa-01bae076adfe-instrument-5-audio.wav',0.027,0.276,120,416.038,'2017-06-24 00:35:28','2017-07-26 22:08:31','Published'),(153,5,'Vocal What 1','cccc3d64-9cb9-468d-be42-e1ec29ba65b1-instrument-5-audio.wav',0.058,0.401,120,390.265,'2017-06-24 00:36:13','2017-07-26 22:08:31','Published'),(154,5,'Vocal Oobah','a7779c99-55b0-4067-819d-a8203a157cd6-instrument-5-audio.wav',0,0.904,120,397.297,'2017-06-24 00:37:11','2017-07-26 22:08:31','Published'),(166,5,'Kick 11_339','dfe7c338-dd80-42ee-94da-19bc53489ca7-instrument-5-audio.wav',0,0.569,120,69.014,'2017-07-27 22:35:51','2017-07-27 22:35:51','Published'),(167,5,'Kick 12_339','ccfc6b74-c939-481f-b59d-caced86b2528-instrument-5-audio.wav',0,0.457,120,3675,'2017-07-27 22:36:53','2017-07-27 22:36:53','Published'),(174,5,'Kick 24_339','a0d1938b-9f3d-47b3-a98f-fb0a429e6df7-instrument-5-audio.wav',0.013,0.547,120,88.024,'2017-07-27 23:07:42','2017-07-27 23:07:42','Published'),(176,5,'Kick 28_339','cd42b8a7-c820-43a2-beb1-f5fec4634050-instrument-5-audio.wav',0.024,0.653,120,980,'2017-07-27 23:13:10','2017-07-27 23:13:10','Published'),(180,3,'Vocal How','f70ead8e-f770-4782-83ce-854a1cb3c640-instrument-3-audio.wav',0.074,0.454,120,284.516,'2017-12-11 04:58:58','2017-12-11 04:58:58','Published'),(182,6,'A','debfb7e7-8b1d-4346-8fdd-e5524bc6ca22-instrument-6-audio.wav',0,0.213,88,2450,'2017-12-14 07:10:51','2017-12-14 07:10:51','Published'),(183,6,'B','6b6da43c-5b5d-4ff9-9fad-beaf7829423b-instrument-6-audio.wav',0,1.143,88,2450,'2017-12-14 07:14:08','2017-12-14 07:14:08','Published'),(184,6,'C','9ca392b5-3061-4f3f-b1fe-2cc85217b80f-instrument-6-audio.wav',0,0.805,88,722.951,'2017-12-14 07:15:15','2017-12-14 07:15:15','Published'),(185,6,'D','a081c1d1-fe83-4f87-9073-637b72c1245f-instrument-6-audio.wav',0,1.837,88,250.568,'2017-12-14 07:16:36','2017-12-14 07:16:36','Published'),(186,6,'E','74eec84a-d3b5-49b4-ad44-647c7698a327-instrument-6-audio.wav',0,0.895,88,588,'2017-12-14 07:18:40','2017-12-14 07:18:40','Published'),(187,6,'F','b17c79e7-975e-482f-b051-4ad736762b66-instrument-6-audio.wav',0,0.802,88,773.684,'2017-12-14 07:19:49','2017-12-14 07:19:49','Published'),(188,6,'G','cf4a6659-6886-4efb-a18a-9cb31cc2dc87-instrument-6-audio.wav',0,0.474,88,700,'2017-12-14 07:21:00','2017-12-14 07:21:00','Published'),(189,6,'H','2c932e17-6cac-4e3b-ae02-7177781b1063-instrument-6-audio.wav',0,0.479,88,1422.58,'2017-12-14 07:22:07','2017-12-14 07:22:07','Published'),(190,7,'Shakuhachi','e02b5c6c-21a8-47b9-94fc-aaa5d1b2975f-instrument-7-audio.wav',0,2.681,88,525,'2017-12-14 08:11:24','2017-12-14 08:11:24','Published'),(191,7,'Pan Flute','de11db96-dfee-4fc3-8a02-3285d3bd2d80-instrument-7-audio.wav',0,1.624,88,518.824,'2017-12-14 08:15:40','2017-12-14 08:15:40','Published'),(192,6,'Taiko','12a5c53f-dd1d-4ef0-abcd-936871fb2625-instrument-6-audio.wav',0,2.006,88,2205,'2017-12-14 08:18:44','2017-12-14 08:18:44','Published'),(193,8,'Sitar','9dc36d01-fd2e-49f7-a75b-545897962c9d-instrument-8-audio.wav',0,2.424,88,262.5,'2017-12-14 08:20:32','2017-12-14 08:20:32','Published'),(194,6,'Kalimba','52f40ec2-3d7c-492b-a8ee-825be0654234-instrument-6-audio.wav',0,1.175,88,262.5,'2017-12-14 08:22:59','2017-12-14 08:22:59','Published'),(195,9,'Bass Pad','69dfbe99-bca5-4171-bbae-b69c4599531e-instrument-9-audio.wav',0,4.073,88,49.606,'2017-12-14 08:26:47','2017-12-14 08:26:47','Published'),(196,9,'Omen Pad','0732ee48-5a9b-4a1d-bafd-e8c2ef23231d-instrument-9-audio.wav',0,4.45,88,65.333,'2017-12-14 08:28:08','2017-12-14 08:28:08','Published'),(197,10,'Whale Pad','c477ff4c-3212-4cfe-8712-6add5f697a98-instrument-10-audio.wav',0,3.249,88,226.154,'2017-12-14 08:30:10','2017-12-14 08:30:10','Published'),(198,11,'Bright Phase','c655a431-ac05-42b2-8d15-ac2bd5a6f632-instrument-11-audio.wav',0,3.776,88,262.5,'2017-12-14 08:33:49','2017-12-14 08:33:49','Published'),(199,12,'Shami','a166a69f-8944-4577-9a68-8b323dff7a68-instrument-12-audio.wav',0.006,0.999,88,262.5,'2017-12-14 08:37:20','2017-12-14 08:37:39','Published'),(200,12,'Koto','eb8bca2c-994f-4e62-9bf6-1242acc79d21-instrument-12-audio.wav',0,1.294,88,132.831,'2017-12-14 08:38:53','2017-12-14 08:38:53','Published'),(201,12,'Shamisen','0e57fd93-11b6-49d8-b617-2d1b8e657180-instrument-12-audio.wav',0,1,88,262.5,'2017-12-14 08:45:08','2017-12-14 08:45:08','Published'),(202,13,'Steel Drum','28129629-7ba8-4aa7-b7bb-96fddbce26b7-instrument-13-audio.wav',0,1.07,88,518.824,'2017-12-14 09:21:09','2017-12-14 09:21:09','Published'),(203,13,'Steelie Drum','55c5396d-db94-49e0-a1e0-ad5281e00ff5-instrument-13-audio.wav',0,1.104,88,525,'2017-12-14 09:22:36','2017-12-14 09:22:36','Published'),(204,13,'Steal Drum','6ed56bfe-1000-4aa9-8e9e-9f3507aab40c-instrument-13-audio.wav',0,1.666,88,262.5,'2017-12-14 09:24:09','2017-12-14 09:24:09','Published'),(205,13,'Shteil Drum','c47a6ad7-a84c-4654-b4cc-03c8c7e25efa-instrument-13-audio.wav',0,0.863,88,130.473,'2017-12-14 09:25:23','2017-12-14 09:25:23','Published'),(206,12,'Shamisen','e5eff131-8813-48bc-9bda-d378b3eeee9a-instrument-12-audio.wav',0.005,1.353,88,264.072,'2017-12-14 09:27:14','2017-12-14 09:27:14','Published');
/*!40000 ALTER TABLE `audio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_chord`
--

LOCK TABLES `audio_chord` WRITE;
/*!40000 ALTER TABLE `audio_chord` DISABLE KEYS */;
INSERT INTO `audio_chord` VALUES (2,191,'C',0,'2017-12-14 08:16:24','2017-12-14 08:16:24'),(3,190,'C',0,'2017-12-14 08:16:39','2017-12-14 08:16:39'),(4,193,'C',0,'2017-12-14 08:21:27','2017-12-14 08:21:27'),(5,195,'G',0,'2017-12-14 08:26:57','2017-12-14 08:26:57'),(6,196,'C',0,'2017-12-14 08:28:18','2017-12-14 08:28:18'),(7,197,'A',0,'2017-12-14 08:30:23','2017-12-14 08:30:23'),(8,198,'C',0,'2017-12-14 08:34:01','2017-12-14 08:34:01');
/*!40000 ALTER TABLE `audio_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_event`
--

LOCK TABLES `audio_event` WRITE;
/*!40000 ALTER TABLE `audio_event` DISABLE KEYS */;
INSERT INTO `audio_event` VALUES (1,2,1,1,'KICK',0.03,0.5,'X','2017-04-22 21:24:11','2017-04-22 21:28:14'),(2,3,1,1,'KICKLONG',0.025,0.5,'X','2017-04-22 21:24:54','2017-04-24 02:19:23'),(3,4,1,0.1,'HIHATCLOSED',0.025,0.1,'X','2017-04-22 21:26:58','2017-06-10 19:24:57'),(4,5,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-04-22 21:43:14','2017-04-22 21:43:14'),(5,6,1,0.4,'SNARE',0.002,0.091,'X','2017-04-22 21:45:06','2017-04-22 21:45:06'),(6,7,0.7,0.6,'TOM',0.002,0.35,'X','2017-04-22 21:46:12','2017-04-22 21:46:12'),(7,8,0.8,0.8,'CLAVES',0,0.05,'X','2017-04-24 00:03:50','2017-04-24 00:03:50'),(8,9,0.8,0.9,'CONGA',0.004,0.2,'X','2017-04-24 00:04:13','2017-04-24 00:04:13'),(9,11,1,1,'TOMHIGH',0.004,0.2,'X','2017-04-24 02:18:57','2017-04-24 02:18:57'),(10,10,1,1,'CONGAHIGH',0.005,0.2,'x','2017-04-24 02:20:10','2017-04-24 02:20:10'),(11,12,0.8,0.3,'CLAP',0.004,0.3,'x','2017-04-24 02:21:39','2017-06-04 04:30:00'),(12,13,1,0.5,'COWBELL',0.004,0.3,'x','2017-04-24 02:23:14','2017-04-24 02:23:14'),(13,14,1,0,'CYMBALCRASH',0,4,'x','2017-04-24 02:24:36','2017-06-16 03:26:40'),(14,15,0.5,0.1,'HIHATOPEN',0.002,0.59,'x','2017-04-24 02:25:56','2017-06-10 19:25:57'),(15,16,0.6,0.2,'SNARERIM',0.001,0.014,'x','2017-04-24 02:27:24','2017-06-10 19:27:09'),(16,22,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:35','2017-06-15 23:27:35'),(17,23,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:55','2017-06-15 23:27:55'),(18,24,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:28:31','2017-06-15 23:28:31'),(20,26,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:29:00','2017-06-15 23:29:00'),(21,27,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:14','2017-06-15 23:29:14'),(22,28,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:28','2017-06-15 23:29:28'),(23,29,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:40','2017-06-15 23:29:40'),(24,30,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:02','2017-06-15 23:30:02'),(25,31,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:22','2017-06-15 23:30:22'),(26,32,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:40','2017-06-15 23:30:40'),(27,33,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:30:54','2017-06-15 23:30:54'),(28,34,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:06','2017-06-15 23:31:06'),(29,35,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:17','2017-06-15 23:31:17'),(30,36,1,0.6,'TOMHIGH',0,1,'x','2017-06-15 23:31:59','2017-06-16 01:07:01'),(31,37,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:17','2017-06-15 23:32:17'),(32,38,1,0.6,'TOM',0,1,'x','2017-06-15 23:32:30','2017-06-16 01:07:14'),(33,39,1,0.6,'CONGAHIGH',0,1,'x','2017-06-15 23:32:39','2017-06-16 01:07:57'),(34,40,1,0.6,'CONGA',0,1,'x','2017-06-15 23:32:48','2017-06-16 01:08:10'),(35,41,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:59','2017-06-15 23:32:59'),(36,42,1,0.1,'SNARE',0,1,'x','2017-06-15 23:33:08','2017-06-15 23:33:08'),(37,44,1,1,'KICK',0,1,'x','2017-06-16 00:25:00','2017-06-16 00:25:00'),(38,43,1,1,'KICK',0,1,'x','2017-06-16 00:25:19','2017-06-16 00:25:19'),(39,45,1,1,'KICK',0,1,'x','2017-06-16 00:27:24','2017-06-16 00:27:24'),(42,48,1,0.6,'TOMLOW',0,1,'x','2017-06-16 00:34:38','2017-06-16 01:07:44'),(43,49,1,0.6,'TOM',0,1,'x','2017-06-16 00:36:12','2017-06-16 01:08:36'),(44,50,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 00:38:24','2017-06-16 01:08:52'),(45,51,1,1,'KICKLONG',0,1,'x','2017-06-16 01:10:01','2017-06-16 01:10:01'),(48,54,1,0.1,'CLAP',0,1,'x','2017-06-16 02:16:01','2017-06-16 02:16:01'),(49,56,1,1,'KICK',0,1,'x','2017-06-16 03:01:30','2017-06-16 03:01:30'),(50,57,1,1,'KICKLONG',0,1,'x','2017-06-16 03:04:19','2017-06-16 03:04:19'),(51,58,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 03:06:43','2017-06-16 03:06:43'),(52,59,1,0.6,'TOMLOW',0,1,'x','2017-06-16 03:07:39','2017-06-16 03:07:39'),(53,60,1,0.6,'TOM',0,1,'x','2017-06-16 03:10:02','2017-06-16 03:10:02'),(54,61,1,0.1,'CLAP',0,1,'x','2017-06-16 03:13:38','2017-06-16 03:13:38'),(55,62,1,0.1,'CLAP',0,1,'x','2017-06-16 03:14:51','2017-06-16 03:14:51'),(56,63,1,0.1,'MARACAS',0,1,'x','2017-06-16 03:17:20','2017-06-16 03:17:20'),(57,64,1,0.2,'COWBELL',0,1,'x','2017-06-16 03:20:15','2017-06-16 03:20:15'),(58,65,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:22:02','2017-06-16 03:24:34'),(59,66,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:24:20','2017-06-16 03:24:20'),(60,67,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:25:47','2017-06-16 03:25:47'),(61,68,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:29:05','2017-06-16 03:29:05'),(62,69,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:12:32','2017-06-20 23:16:38'),(63,70,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:17:52','2017-06-20 23:17:52'),(64,71,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:33:17','2017-06-20 23:33:17'),(65,72,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:35:34','2017-06-20 23:35:34'),(66,73,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:36:46','2017-06-20 23:36:46'),(67,74,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:37:56','2017-06-20 23:37:56'),(68,75,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:39:16','2017-06-20 23:39:16'),(69,76,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:40:49','2017-06-20 23:40:49'),(70,77,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:45:46','2017-06-20 23:45:46'),(71,78,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:46:41','2017-06-20 23:46:41'),(72,79,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:47:35','2017-06-20 23:47:35'),(73,80,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:48:55','2017-06-20 23:48:55'),(74,81,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:49:58','2017-06-20 23:49:58'),(75,82,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:50:42','2017-06-20 23:50:42'),(76,83,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:51:32','2017-06-20 23:51:32'),(77,84,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:52:08','2017-06-20 23:52:08'),(78,85,1,0.6,'TOM',0,1,'X','2017-06-20 23:54:26','2017-07-29 21:16:34'),(79,86,1,0.6,'KICK',0,1,'X','2017-06-20 23:56:22','2017-07-29 21:16:26'),(80,87,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:00','2017-07-29 21:15:54'),(81,88,1,0.6,'TOM',0,1,'X','2017-06-20 23:59:46','2017-07-29 21:15:47'),(82,89,1,0.6,'TOM',0,1,'X','2017-06-21 00:00:29','2017-07-29 21:15:41'),(83,90,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:16','2017-07-29 21:15:34'),(84,91,1,0.6,'TOM',0,1,'X','2017-06-21 00:01:59','2017-07-29 21:15:27'),(85,92,1,0.6,'TOM',0,1,'X','2017-06-21 00:02:39','2017-07-29 21:16:17'),(86,93,1,0.1,'SNARE',0,1,'X','2017-06-21 00:14:01','2017-06-21 00:14:01'),(87,94,1,0.15,'SNARE',0,1,'X','2017-06-21 00:14:49','2017-06-21 00:14:49'),(88,95,1,0.1,'SNARE',0,1,'X','2017-06-21 00:15:35','2017-06-21 00:15:35'),(89,96,1,0.15,'SNARE',0,1,'X','2017-06-21 00:49:39','2017-06-21 00:50:12'),(90,97,1,0.15,'SNARE',0,1,'X','2017-06-21 00:51:14','2017-06-21 00:51:14'),(91,98,1,0.15,'SNARE',0,1,'X','2017-06-21 00:53:11','2017-06-21 00:53:11'),(92,99,1,0.15,'SNARE',0,1,'X','2017-06-21 00:54:35','2017-06-21 00:54:35'),(93,100,1,0.15,'SNARE',0,1,'X','2017-06-21 00:55:37','2017-06-21 00:55:37'),(94,101,1,0.15,'SNARE',0,1,'X','2017-06-21 00:57:01','2017-06-21 00:57:01'),(95,102,1,0.15,'SNARE',0,1,'X','2017-06-21 00:58:31','2017-06-21 00:58:31'),(96,103,1,0.15,'SNARE',0,1,'X','2017-06-21 01:00:02','2017-06-21 01:00:02'),(97,104,1,0.6,'CONGA',0,1,'X','2017-06-21 01:04:26','2017-06-21 01:04:26'),(98,105,1,0.6,'TOM',0,1,'X','2017-06-21 01:05:37','2017-06-21 01:05:37'),(99,106,1,0.6,'CONGA',0,1,'X','2017-06-21 01:06:29','2017-06-21 01:06:29'),(100,107,1,0.6,'CONGA',0,1,'X','2017-06-21 01:07:21','2017-06-21 01:07:21'),(101,108,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:06','2017-06-21 01:08:13'),(102,109,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:59','2017-06-21 01:08:59'),(103,110,1,0.6,'CONGA',0,1,'X','2017-06-21 01:09:39','2017-06-21 01:09:39'),(104,111,1,0.6,'CONGA',0,1,'X','2017-06-21 01:10:23','2017-06-21 01:10:23'),(105,112,1,0.6,'CONGA',0,1,'X','2017-06-21 01:11:09','2017-06-21 01:11:09'),(106,113,1,0.6,'CONGA',0,1,'X','2017-06-21 01:12:01','2017-06-21 01:12:01'),(107,114,1,0.6,'CONGA',0,1,'X','2017-06-21 01:13:01','2017-06-21 01:13:01'),(108,115,1,0.6,'TOM',0,1,'X','2017-06-21 01:13:42','2017-06-21 01:13:42'),(109,116,1,0.6,'TOM',0,1,'X','2017-06-21 01:14:28','2017-06-21 01:14:28'),(110,117,1,0.6,'TOM',0,1,'X','2017-06-21 01:15:12','2017-06-21 01:15:12'),(111,119,1,0.6,'TOM',0,1,'X','2017-06-21 01:19:59','2017-06-21 01:19:59'),(112,120,1,0.6,'TOM',0,1,'X','2017-06-21 01:21:05','2017-06-21 01:21:05'),(113,121,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:02','2017-06-21 01:22:02'),(114,122,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:46','2017-06-21 01:22:46'),(115,123,1,0.6,'TOM',0,1,'X','2017-06-21 01:23:31','2017-06-21 01:23:31'),(116,124,1,0.6,'TOM',0,1,'X','2017-06-21 01:24:10','2017-06-21 01:24:10'),(118,126,1,0.3,'X',0,1,'0','2017-06-23 23:54:24','2017-06-23 23:54:24'),(119,127,1,0.3,'HEY',0,1,'X','2017-06-23 23:56:04','2017-06-23 23:56:04'),(120,128,1,0.3,'HEY',0,1,'X','2017-06-23 23:57:11','2017-06-23 23:57:11'),(121,129,1,0.3,'HEY',0,1,'X','2017-06-23 23:58:09','2017-06-23 23:58:09'),(124,132,1,0.3,'HEY',0,1,'X','2017-06-24 00:00:37','2017-06-24 00:00:37'),(125,133,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:08','2017-06-24 00:11:08'),(126,134,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:47','2017-06-24 00:11:47'),(127,135,1,0.3,'HEY',0,1,'X','2017-06-24 00:13:39','2017-06-24 00:13:39'),(128,136,1,0.3,'HEY',0,1,'X','2017-06-24 00:15:04','2017-06-24 00:15:04'),(129,137,1,0.3,'HEY',0,1,'X','2017-06-24 00:16:27','2017-06-24 00:16:27'),(130,138,1,0.3,'HEY',0,1,'X','2017-06-24 00:17:46','2017-06-24 00:17:46'),(131,139,1,0.3,'HEY',0,1,'X','2017-06-24 00:20:45','2017-06-24 00:20:45'),(132,140,1,0.3,'HEY',0,1,'X','2017-06-24 00:22:47','2017-06-24 00:22:47'),(133,141,1,0.3,'HEY',0,1,'X','2017-06-24 00:24:25','2017-06-24 00:24:25'),(134,143,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:08','2017-06-24 00:26:08'),(135,144,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:59','2017-06-24 00:26:59'),(136,145,1,0.3,'HEY',0,1,'X','2017-06-24 00:27:47','2017-06-24 00:27:47'),(137,147,1,0.3,'HEY',0,1,'X','2017-06-24 00:30:09','2017-06-24 00:30:09'),(138,148,1,0.3,'HEY',0,1,'X','2017-06-24 00:31:42','2017-06-24 00:31:42'),(139,149,1,0.3,'HEY',0,1,'X','2017-06-24 00:32:45','2017-06-24 00:32:45'),(140,150,1,0.3,'HEY',0,1,'X','2017-06-24 00:33:52','2017-06-24 00:33:52'),(141,151,1,0.3,'HEY',0,1,'X','2017-06-24 00:34:48','2017-06-24 00:34:48'),(142,152,1,0.3,'HEY',0,1,'X','2017-06-24 00:35:36','2017-06-24 00:35:36'),(143,153,1,0.3,'HEY',0,1,'X','2017-06-24 00:36:22','2017-06-24 00:36:22'),(144,154,1,0.3,'HEY',0.025,1,'X','2017-06-24 00:37:19','2017-07-24 20:21:27'),(150,166,2,1,'KICK',0,1,'x','2017-07-27 22:36:01','2017-12-03 03:28:37'),(151,167,2,1,'KICK',0,1,'x','2017-07-27 22:37:04','2017-12-03 03:28:45'),(158,174,2,1,'KICK',0,1,'x','2017-07-27 23:07:54','2017-12-03 03:28:15'),(160,176,2,1,'KICK',0,1,'x','2017-07-27 23:13:22','2017-12-03 03:27:51'),(163,182,1,0.5,'TICK',0,1,'Eb7','2017-12-14 07:11:39','2017-12-14 07:11:39'),(164,183,1,0.5,'TICK',0,1,'Eb7','2017-12-14 07:14:22','2017-12-14 07:14:22'),(165,184,1,0.5,'TICK',0,1,'Gb5','2017-12-14 07:15:30','2017-12-14 07:15:30'),(166,185,1,0.85,'THOOM',0,2,'B3','2017-12-14 07:17:19','2017-12-14 07:17:48'),(167,186,1,0.5,'TOCK',0,1,'D5','2017-12-14 07:18:59','2017-12-14 07:18:59'),(168,187,1,0.5,'TOCK',0,1,'G5','2017-12-14 07:20:01','2017-12-14 07:20:01'),(169,188,1,0.5,'POP',0,1,'f5','2017-12-14 07:21:17','2017-12-14 07:21:17'),(170,189,1,0.5,'PIP',0,1,'F6','2017-12-14 07:22:20','2017-12-14 07:22:20'),(171,190,1,1,'OOH',0,4,'C5','2017-12-14 08:12:26','2017-12-14 08:12:26'),(172,191,1,1,'OOH',0,2,'C5','2017-12-14 08:16:11','2017-12-14 08:16:11'),(173,192,1,0.3,'THOOM',0,2,'Db7','2017-12-14 08:19:08','2017-12-14 08:19:08'),(174,193,1,1,'POING',0,3,'C4','2017-12-14 08:21:17','2017-12-14 08:21:17'),(175,194,1,0.8,'POOM',0,2,'C4','2017-12-14 08:23:33','2017-12-14 08:23:33'),(176,195,1,1,'BOOM',0,4,'G1','2017-12-14 08:27:14','2017-12-14 08:27:14'),(177,196,1,1,'WAOW',0,4,'C2','2017-12-14 08:28:38','2017-12-14 08:28:38'),(178,197,1,0.8,'OOH',0,4,'A3','2017-12-14 08:30:46','2017-12-14 08:30:46'),(179,198,1,0.5,'PEOW',0,4,'C4','2017-12-14 08:34:21','2017-12-14 08:34:21'),(180,199,1,0.6,'DOING',0,1,'c4','2017-12-14 08:37:55','2017-12-14 08:37:55'),(181,200,1,0.6,'DONG',0,1,'C3','2017-12-14 08:39:20','2017-12-14 08:39:20'),(182,201,1,0.5,'TUNG',0,1,'C4','2017-12-14 08:45:28','2017-12-14 08:45:28'),(183,202,1,0.8,'BONG',0,1,'c5','2017-12-14 09:21:25','2017-12-14 09:23:22'),(184,203,1,0.8,'PING',0,1,'C5','2017-12-14 09:22:53','2017-12-14 09:22:53'),(185,204,1,0.8,'TING',0,1,'C4','2017-12-14 09:24:25','2017-12-14 09:24:25'),(186,205,1,0.7,'SHTONG',0,1,'C3','2017-12-14 09:25:44','2017-12-14 09:25:44'),(187,206,1,0.6,'PLONG',0,1,'c4','2017-12-14 09:27:31','2017-12-14 09:27:31');
/*!40000 ALTER TABLE `audio_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument`
--

LOCK TABLES `instrument` WRITE;
/*!40000 ALTER TABLE `instrument` DISABLE KEYS */;
INSERT INTO `instrument` VALUES (3,1,1,'percussive','Electronic',0.8,'2017-04-21 16:33:55','2017-06-16 02:19:40'),(4,1,1,'percussive','Acoustic',0.5,'2017-06-15 22:32:29','2017-06-15 22:32:29'),(5,1,1,'percussive','Pots & Pans',0.76,'2017-06-20 23:02:25','2017-07-27 17:12:15'),(6,1,3,'Percussive','Earth Wood Hits',0.5,'2017-12-13 06:54:32','2017-12-14 09:19:43'),(7,1,3,'Harmonic','Wind Flute Note',0.2,'2017-12-14 08:10:02','2017-12-14 08:10:02'),(8,1,3,'Harmonic','Water Sitar Harmony',0.35,'2017-12-14 08:19:43','2017-12-14 08:31:27'),(9,1,3,'Harmonic','Earth Bass Harmony',0.4,'2017-12-14 08:25:57','2017-12-14 08:31:37'),(10,1,3,'Harmonic','Water Whale Harmony',0.4,'2017-12-14 08:29:25','2017-12-14 08:29:25'),(11,1,3,'Harmonic','Fire Bright Harmony',0.4,'2017-12-14 08:32:13','2017-12-14 08:32:13'),(12,1,3,'Percussive','Fire String Hits',0.5,'2017-12-14 08:36:41','2017-12-14 09:19:53'),(13,1,3,'Percussive','Water Bell Hits',0.5,'2017-12-14 09:19:33','2017-12-14 09:19:33'),(14,1,3,'Percussive','Wind Blow Hits',0.2,'2017-12-14 09:20:08','2017-12-14 09:20:13');
/*!40000 ALTER TABLE `instrument` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument_meme`
--

LOCK TABLES `instrument_meme` WRITE;
/*!40000 ALTER TABLE `instrument_meme` DISABLE KEYS */;
INSERT INTO `instrument_meme` VALUES (2,3,'Classic','2017-04-23 23:13:29','2017-04-23 23:13:29'),(3,3,'Deep','2017-04-23 23:13:33','2017-04-23 23:13:33'),(4,3,'Acid','2017-04-23 23:13:36','2017-04-23 23:13:36'),(6,3,'Tech','2017-04-23 23:13:41','2017-04-23 23:13:41'),(7,3,'Electro','2017-04-23 23:13:43','2017-04-23 23:13:43'),(10,3,'Cool','2017-04-23 23:20:57','2017-04-23 23:20:57'),(11,3,'Hard','2017-04-23 23:20:59','2017-04-23 23:20:59'),(13,3,'Progressive','2017-04-23 23:23:43','2017-04-23 23:23:43'),(14,4,'Classic','2017-06-15 22:59:20','2017-06-15 22:59:20'),(16,4,'Tropical','2017-06-15 22:59:32','2017-06-15 22:59:32'),(17,4,'Hot','2017-06-15 22:59:35','2017-06-15 22:59:35'),(19,4,'Easy','2017-06-15 22:59:43','2017-06-15 22:59:43'),(20,4,'Progressive','2017-06-15 22:59:46','2017-06-15 22:59:46'),(21,5,'Classic','2017-06-21 01:25:37','2017-06-21 01:25:37'),(22,5,'Deep','2017-06-21 01:25:41','2017-06-21 01:25:41'),(23,5,'Hard','2017-06-21 01:25:58','2017-06-21 01:25:58'),(27,4,'Deep','2017-06-21 01:40:43','2017-06-21 01:40:43'),(28,4,'Hard','2017-06-21 01:40:56','2017-06-21 01:40:56'),(30,5,'Hot','2017-06-24 01:38:58','2017-06-24 01:38:58'),(31,5,'Cool','2017-06-24 01:39:02','2017-06-24 01:39:02'),(32,6,'Earth','2017-12-13 06:57:01','2017-12-13 06:57:01'),(36,7,'Wind','2017-12-14 08:10:06','2017-12-14 08:10:06'),(37,8,'Water','2017-12-14 08:19:48','2017-12-14 08:19:48'),(38,9,'Earth','2017-12-14 08:26:02','2017-12-14 08:26:02'),(39,10,'Water','2017-12-14 08:29:31','2017-12-14 08:29:31'),(40,11,'Fire','2017-12-14 08:32:17','2017-12-14 08:32:17'),(41,14,'Wind','2017-12-14 09:20:18','2017-12-14 09:20:18'),(42,13,'Water','2017-12-14 09:20:31','2017-12-14 09:20:31');
/*!40000 ALTER TABLE `instrument_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `library`
--

LOCK TABLES `library` WRITE;
/*!40000 ALTER TABLE `library` DISABLE KEYS */;
INSERT INTO `library` VALUES (1,'Pots and Pans #2',1,'2017-02-10 00:03:23','2017-06-21 01:33:46'),(3,'Cool Ambience',2,'2017-12-12 17:58:00','2017-12-12 17:58:00');
/*!40000 ALTER TABLE `library` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern`
--

LOCK TABLES `pattern` WRITE;
/*!40000 ALTER TABLE `pattern` DISABLE KEYS */;
INSERT INTO `pattern` VALUES (6,1,1,'Rhythm','2-Step Shuffle Beat',0.62,'C',133,'2017-04-23 23:21:52','2017-12-10 11:01:39'),(7,1,1,'Macro','Deep, from Hot to Cool',0.6,'C',133,'2017-05-01 18:59:22','2017-12-10 11:01:45'),(8,1,1,'Macro','Deep, from Cool to Hot',0.6,'G minor',133,'2017-05-01 18:59:32','2017-12-10 11:01:50'),(9,1,1,'Main','I\'ll House You',0.5,'C',133,'2017-05-13 00:04:19','2017-12-10 11:01:56'),(11,1,3,'Main','Earth Positivity',0.5,'Ab',80,'2017-12-12 22:05:13','2017-12-15 03:07:36'),(12,1,3,'Macro','Earth to Fire',0.5,'Bb',88,'2017-12-13 06:38:26','2017-12-13 06:38:26'),(13,1,3,'Macro','Earth to Water',0.5,'D#',88,'2017-12-13 06:40:22','2017-12-13 06:40:22'),(14,1,3,'Macro','Earth to Wind',0.5,'Dm',88,'2017-12-13 06:42:39','2017-12-13 06:42:39'),(15,1,3,'Macro','Fire to Earth',0.5,'Cm',88,'2017-12-13 06:43:43','2017-12-13 06:43:43'),(16,1,3,'Macro','Fire to Water',0.5,'G',88,'2017-12-13 06:45:43','2017-12-13 06:45:43'),(17,1,3,'Macro','Fire to Wind',0.5,'Dm',88,'2017-12-13 06:46:56','2017-12-13 06:46:56'),(18,1,3,'Macro','Wind to Earth',0.5,'Ab minor',88,'2017-12-13 06:48:06','2017-12-13 07:17:36'),(19,1,3,'Macro','Wind to Fire',0.5,'D',88,'2017-12-13 06:49:09','2017-12-13 06:49:09'),(20,1,3,'Macro','Wind to Water',0.5,'C',88,'2017-12-13 06:49:56','2017-12-13 06:49:56'),(21,1,3,'Rhythm','Bakalaka',0.5,'C',88,'2017-12-13 06:57:29','2017-12-15 07:11:41'),(22,1,3,'Main','Wind Spirits',0.5,'D',88,'2017-12-15 03:10:38','2017-12-15 03:10:49'),(23,1,3,'Main','Fire Nice',0.5,'F',88,'2017-12-15 06:55:57','2017-12-15 06:55:57'),(24,1,3,'Main','Water Night',0.5,'C',88,'2017-12-15 07:05:48','2017-12-15 07:07:58');
/*!40000 ALTER TABLE `pattern` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pattern_meme`
--

LOCK TABLES `pattern_meme` WRITE;
/*!40000 ALTER TABLE `pattern_meme` DISABLE KEYS */;
INSERT INTO `pattern_meme` VALUES (1,6,'Classic','2017-04-23 23:22:21','2017-04-23 23:22:21'),(2,6,'Deep','2017-04-23 23:22:23','2017-04-23 23:22:23'),(3,6,'Acid','2017-04-23 23:22:24','2017-04-23 23:22:24'),(5,6,'Tech','2017-04-23 23:22:28','2017-04-23 23:22:28'),(6,6,'Electro','2017-04-23 23:22:31','2017-04-23 23:22:31'),(7,6,'Tropical','2017-04-23 23:22:34','2017-04-23 23:22:34'),(8,6,'Hot','2017-04-23 23:22:36','2017-04-23 23:22:36'),(9,6,'Cool','2017-04-23 23:22:39','2017-04-23 23:22:39'),(10,6,'Hard','2017-04-23 23:22:40','2017-04-23 23:22:40'),(11,6,'Easy','2017-04-23 23:22:42','2017-04-23 23:22:42'),(12,6,'Progressive','2017-04-23 23:23:17','2017-04-23 23:23:17'),(15,7,'Deep','2017-05-01 18:59:46','2017-05-01 18:59:46'),(16,8,'Deep','2017-05-01 19:42:36','2017-05-01 19:42:36'),(17,9,'Deep','2017-05-13 00:04:41','2017-05-13 00:04:41'),(18,9,'Classic','2017-05-13 00:04:44','2017-05-13 00:04:44'),(34,9,'Hard','2017-06-16 04:26:57','2017-06-16 04:26:57'),(36,11,'Earth','2017-12-13 00:44:05','2017-12-13 00:44:05'),(50,22,'Wind','2017-12-15 03:10:53','2017-12-15 03:10:53'),(51,23,'Fire','2017-12-15 06:56:04','2017-12-15 06:56:04'),(52,24,'Water','2017-12-15 07:05:54','2017-12-15 07:05:54');
/*!40000 ALTER TABLE `pattern_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase`
--

LOCK TABLES `phase` WRITE;
/*!40000 ALTER TABLE `phase` DISABLE KEYS */;
INSERT INTO `phase` VALUES (3,6,'drop d beet',0,4,NULL,NULL,NULL,'2017-04-23 23:44:19','2017-12-07 03:44:24'),(4,7,'from Hot',0,0,0.7,'C',133,'2017-05-01 19:39:59','2017-12-10 11:02:28'),(5,7,'to Cool',1,0,0.5,'Bb Minor',123,'2017-05-01 19:40:18','2017-12-12 06:59:11'),(6,8,'from Cool',0,0,0.5,'G minor',123,'2017-05-01 19:43:06','2017-12-12 06:59:24'),(7,8,'to Hot',1,0,0.7,'C',133,'2017-05-01 19:43:26','2017-12-10 11:02:49'),(8,9,'Drop',0,32,0.4,'C',133,'2017-05-13 00:05:29','2017-12-12 07:04:40'),(9,9,'Breakdown A',1,16,0.6,'G minor',133,'2017-05-13 00:07:19','2017-12-12 07:04:46'),(14,9,'Breakdown B',2,16,0.8,'G minor',133,'2017-07-27 17:40:32','2017-12-10 11:02:16'),(15,11,'Verse',0,24,NULL,'Ab',NULL,'2017-12-12 22:05:49','2017-12-13 07:21:00'),(16,11,'Chorus',1,24,NULL,'Ab',NULL,'2017-12-12 22:08:13','2017-12-13 07:21:07'),(17,11,'Verse',2,24,NULL,'Ab',NULL,'2017-12-12 22:08:50','2017-12-13 07:21:13'),(18,11,'Bridge',3,28,NULL,'Ab',NULL,'2017-12-12 22:10:25','2017-12-13 07:21:19'),(19,11,'Chorus',4,32,NULL,'Ab',NULL,'2017-12-12 22:12:08','2017-12-13 07:21:25'),(20,11,'Outro',5,8,NULL,'Ab',NULL,'2017-12-12 22:12:44','2017-12-13 07:21:31'),(21,12,'Passion Volcano',0,0,NULL,'Gm',NULL,'2017-12-13 06:39:29','2017-12-13 06:39:29'),(22,12,'Falling in Love',1,0,NULL,'Bb',NULL,'2017-12-13 06:40:03','2017-12-13 06:40:03'),(23,13,'Nostalgia River',0,0,NULL,'Dm',NULL,'2017-12-13 06:42:02','2017-12-13 06:42:02'),(24,13,'Passage of Time',1,0,NULL,'D#',NULL,'2017-12-13 06:42:21','2017-12-13 06:42:21'),(25,14,'Tornado Avalanche',0,0,NULL,'Am',NULL,'2017-12-13 06:43:03','2017-12-13 06:43:03'),(26,14,'The Seasons',1,0,NULL,'Dm',NULL,'2017-12-13 06:43:24','2017-12-13 06:43:24'),(27,15,'Lightning Strike',0,0,NULL,'B',NULL,'2017-12-13 06:43:58','2017-12-13 06:43:58'),(28,15,'Car Racing',1,0,NULL,'Cm',NULL,'2017-12-13 06:44:24','2017-12-13 06:44:24'),(29,16,'Volcanic Island',0,0,NULL,'C',NULL,'2017-12-13 06:46:14','2017-12-13 06:46:14'),(30,16,'Sex on the Beach',1,0,NULL,'G',NULL,'2017-12-13 06:46:37','2017-12-13 06:46:37'),(31,17,'Smoke in the Air',0,0,NULL,'F',NULL,'2017-12-13 06:47:25','2017-12-13 06:47:25'),(32,17,'Dreams',1,0,NULL,'Dm',NULL,'2017-12-13 06:47:43','2017-12-13 06:47:43'),(33,18,'Open Road Tumbleweed',0,0,NULL,'Am',NULL,'2017-12-13 06:48:26','2017-12-13 06:48:26'),(34,18,'Rolling Stone',1,0,NULL,'Ab minor',NULL,'2017-12-13 06:48:48','2017-12-13 07:17:48'),(35,19,'Stoke the Flames',0,0,NULL,'Am',NULL,'2017-12-13 06:49:21','2017-12-13 06:49:21'),(36,19,'Inspiration Adventure',1,0,NULL,'D',NULL,'2017-12-13 06:49:41','2017-12-13 06:49:41'),(37,20,'Make Waves',0,0,NULL,'Bb minor',NULL,'2017-12-13 06:50:11','2017-12-13 07:18:17'),(38,20,'Bon Voyage',1,0,NULL,'C',NULL,'2017-12-13 06:50:29','2017-12-13 06:50:29'),(39,21,'Beat',0,8,NULL,NULL,NULL,'2017-12-13 06:58:02','2017-12-15 04:31:21'),(40,22,'Intro',0,16,NULL,NULL,NULL,'2017-12-15 03:11:05','2017-12-15 03:11:05'),(41,22,'Verse',1,40,NULL,NULL,NULL,'2017-12-15 03:12:20','2017-12-15 03:12:20'),(42,22,'Bridge',2,24,NULL,NULL,NULL,'2017-12-15 03:17:13','2017-12-15 03:17:13'),(43,22,'Outro',3,16,NULL,NULL,NULL,'2017-12-15 03:19:57','2017-12-15 03:19:57'),(44,23,'Intro',0,16,NULL,NULL,NULL,'2017-12-15 06:56:17','2017-12-15 06:56:17'),(45,23,'Verse',1,48,NULL,NULL,NULL,'2017-12-15 06:58:24','2017-12-15 06:58:24'),(46,23,'Bridge',2,32,NULL,NULL,NULL,'2017-12-15 07:02:47','2017-12-15 07:02:47'),(47,24,'Verse',0,32,NULL,NULL,NULL,'2017-12-15 07:06:03','2017-12-15 07:06:03'),(48,24,'Chorus',1,56,NULL,'null',NULL,'2017-12-15 07:08:52','2017-12-15 07:11:03');
/*!40000 ALTER TABLE `phase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase_chord`
--

LOCK TABLES `phase_chord` WRITE;
/*!40000 ALTER TABLE `phase_chord` DISABLE KEYS */;
INSERT INTO `phase_chord` VALUES (7,3,'C',0,'2017-04-23 23:44:43','2017-04-23 23:44:43'),(8,8,'C major 7',0,'2017-05-13 00:05:58','2017-06-16 03:54:30'),(9,8,'Cm7',8,'2017-05-13 00:06:11','2017-06-16 03:55:17'),(10,8,'F7',12,'2017-05-13 00:06:28','2017-06-16 03:58:00'),(11,8,'Bb major 7',16,'2017-05-13 00:06:41','2017-06-16 03:58:13'),(12,9,'D',0,'2017-05-13 00:07:40','2017-06-16 04:00:16'),(13,9,'G',4,'2017-05-13 00:07:47','2017-06-16 04:00:22'),(14,9,'C',8,'2017-05-13 00:07:55','2017-06-16 04:00:31'),(15,9,'F7',12,'2017-05-13 00:08:01','2017-06-16 04:31:20'),(19,8,'Bb m7',24,'2017-06-16 03:59:02','2017-06-16 03:59:02'),(20,8,'Eb7',28,'2017-06-16 03:59:38','2017-06-16 03:59:38'),(21,8,'Ab major 7',30,'2017-06-16 03:59:46','2017-06-16 03:59:46'),(32,14,'E minor 7',0,'2017-07-27 17:41:02','2017-07-27 17:41:02'),(33,14,'Eb minor 7',4,'2017-07-27 17:41:11','2017-07-27 17:41:11'),(38,14,'D minor 7',8,'2017-07-30 23:22:08','2017-07-30 23:22:08'),(39,14,'Db minor 7',12,'2017-07-30 23:22:15','2017-07-30 23:22:15'),(40,15,'Ebm',0,'2017-12-12 22:05:59','2017-12-12 22:05:59'),(41,15,'Db',2,'2017-12-12 22:06:10','2017-12-12 22:06:10'),(42,15,'B',4,'2017-12-12 22:06:22','2017-12-12 22:06:22'),(43,15,'Bb',8,'2017-12-12 22:06:30','2017-12-12 22:06:30'),(44,15,'Ebm',16,'2017-12-12 22:06:41','2017-12-12 22:06:41'),(45,15,'Db',18,'2017-12-12 22:06:50','2017-12-12 22:06:50'),(46,15,'B',20,'2017-12-12 22:06:57','2017-12-12 22:06:57'),(47,15,'Bb',22,'2017-12-12 22:07:02','2017-12-12 22:07:02'),(48,16,'Gb',0,'2017-12-12 22:08:21','2017-12-12 22:08:21'),(49,16,'Ab',2,'2017-12-12 22:08:28','2017-12-13 00:47:26'),(50,16,'Bb',4,'2017-12-12 22:08:34','2017-12-13 00:47:30'),(51,17,'Bb',0,'2017-12-12 22:09:54','2017-12-12 22:09:54'),(52,17,'Eb',8,'2017-12-12 22:10:00','2017-12-12 22:10:00'),(53,17,'Bb',16,'2017-12-12 22:10:07','2017-12-12 22:10:07'),(54,18,'F',0,'2017-12-12 22:10:34','2017-12-12 22:10:34'),(55,18,'Gm7',4,'2017-12-12 22:10:41','2017-12-12 22:10:41'),(56,18,'C',6,'2017-12-12 22:10:46','2017-12-12 22:10:46'),(57,18,'F',8,'2017-12-12 22:10:53','2017-12-12 22:10:53'),(58,18,'Gm7',12,'2017-12-12 22:11:02','2017-12-12 22:11:02'),(59,18,'C',14,'2017-12-12 22:11:11','2017-12-12 22:11:11'),(60,18,'F',16,'2017-12-12 22:11:17','2017-12-12 22:11:17'),(61,18,'G',20,'2017-12-12 22:11:23','2017-12-12 22:11:23'),(62,18,'A',21,'2017-12-12 22:11:27','2017-12-12 22:11:27'),(63,18,'C',22,'2017-12-12 22:11:32','2017-12-12 22:11:32'),(64,18,'D',23,'2017-12-12 22:11:36','2017-12-12 22:11:36'),(65,18,'F9',24,'2017-12-12 22:11:44','2017-12-12 22:11:44'),(66,19,'Gb',0,'2017-12-12 22:12:16','2017-12-13 00:46:18'),(67,19,'Ab',2,'2017-12-12 22:12:21','2017-12-13 00:45:54'),(68,19,'Bb',4,'2017-12-12 22:12:27','2017-12-13 00:46:08'),(69,20,'Gb',0,'2017-12-12 22:12:50','2017-12-12 22:12:50'),(70,20,'Ab',2,'2017-12-12 22:12:56','2017-12-12 22:12:56'),(71,20,'Bb',4,'2017-12-12 22:13:01','2017-12-12 22:13:01'),(72,20,'Ab',6,'2017-12-12 22:13:06','2017-12-12 22:13:06'),(74,19,'Gb',8,'2017-12-13 00:46:23','2017-12-13 00:46:23'),(75,19,'Ab',10,'2017-12-13 00:46:27','2017-12-13 00:46:27'),(76,19,'Bb',12,'2017-12-13 00:46:34','2017-12-13 00:46:34'),(77,19,'Gb',16,'2017-12-13 00:46:40','2017-12-13 00:46:40'),(78,19,'Ab',18,'2017-12-13 00:46:47','2017-12-13 00:46:47'),(79,19,'Bb',20,'2017-12-13 00:46:55','2017-12-13 00:46:55'),(80,19,'Gb',24,'2017-12-13 00:47:04','2017-12-13 00:47:04'),(81,19,'Ab',26,'2017-12-13 00:47:09','2017-12-13 00:47:09'),(82,19,'Bb',28,'2017-12-13 00:47:14','2017-12-13 00:47:14'),(83,16,'G',8,'2017-12-13 00:47:44','2017-12-13 00:47:44'),(84,16,'G',16,'2017-12-13 00:48:08','2017-12-13 00:48:08'),(85,16,'G',24,'2017-12-13 00:48:11','2017-12-13 00:48:11'),(86,16,'Ab',26,'2017-12-13 00:48:18','2017-12-13 00:48:18'),(87,16,'Ab',18,'2017-12-13 00:48:24','2017-12-13 00:48:24'),(88,16,'Ab',10,'2017-12-13 00:48:29','2017-12-13 00:48:29'),(89,16,'Bb',12,'2017-12-13 00:48:36','2017-12-13 00:48:36'),(90,16,'Bb',20,'2017-12-13 00:48:40','2017-12-13 00:48:40'),(91,16,'Bb',28,'2017-12-13 00:48:44','2017-12-13 00:48:44'),(92,40,'A',0,'2017-12-15 03:11:13','2017-12-15 03:11:13'),(93,40,'E',2,'2017-12-15 03:11:20','2017-12-15 03:11:20'),(94,40,'A',4,'2017-12-15 03:11:27','2017-12-15 03:11:27'),(95,40,'E',6,'2017-12-15 03:11:33','2017-12-15 03:11:33'),(96,40,'A',8,'2017-12-15 03:11:39','2017-12-15 03:11:39'),(97,40,'E',10,'2017-12-15 03:11:44','2017-12-15 03:11:44'),(98,40,'F#m7',12,'2017-12-15 03:11:54','2017-12-15 03:12:01'),(99,41,'D',0,'2017-12-15 03:12:34','2017-12-15 03:12:34'),(100,41,'Bm6',4,'2017-12-15 03:14:16','2017-12-15 03:14:16'),(101,41,'F#m',8,'2017-12-15 03:14:44','2017-12-15 03:14:44'),(102,41,'F#m7',10,'2017-12-15 03:14:52','2017-12-15 03:14:52'),(103,41,'F#m6',14,'2017-12-15 03:14:59','2017-12-15 03:14:59'),(104,41,'E',16,'2017-12-15 03:15:08','2017-12-15 03:15:08'),(105,41,'E',24,'2017-12-15 03:15:14','2017-12-15 03:15:14'),(106,41,'Cdim',22,'2017-12-15 03:15:21','2017-12-15 03:15:21'),(107,41,'A#m7-5',28,'2017-12-15 03:15:36','2017-12-15 03:16:04'),(108,41,'A',32,'2017-12-15 03:16:14','2017-12-15 03:16:14'),(109,41,'E',34,'2017-12-15 03:16:25','2017-12-15 03:16:25'),(110,41,'F#m7',36,'2017-12-15 03:16:36','2017-12-15 03:16:36'),(111,41,'E',38,'2017-12-15 03:16:45','2017-12-15 03:16:45'),(112,42,'G',0,'2017-12-15 03:17:29','2017-12-15 03:17:29'),(113,42,'Em6',2,'2017-12-15 03:17:55','2017-12-15 03:17:55'),(114,42,'Bm',4,'2017-12-15 03:18:05','2017-12-15 03:18:05'),(115,42,'Bm7',5,'2017-12-15 03:18:12','2017-12-15 03:18:12'),(116,42,'E7',6,'2017-12-15 03:18:18','2017-12-15 03:18:18'),(117,42,'A',8,'2017-12-15 03:18:26','2017-12-15 03:18:47'),(118,42,'Fdim',10,'2017-12-15 03:18:55','2017-12-15 03:18:55'),(119,42,'A',12,'2017-12-15 03:19:03','2017-12-15 03:19:03'),(120,42,'D#m7-5',14,'2017-12-15 03:19:12','2017-12-15 03:19:12'),(121,42,'D',16,'2017-12-15 03:19:20','2017-12-15 03:19:20'),(122,42,'A',18,'2017-12-15 03:19:29','2017-12-15 03:19:29'),(123,42,'Bm',20,'2017-12-15 03:19:36','2017-12-15 03:19:36'),(124,43,'A',0,'2017-12-15 03:20:12','2017-12-15 03:20:12'),(125,43,'A',8,'2017-12-15 03:20:16','2017-12-15 03:20:16'),(126,43,'E',10,'2017-12-15 03:20:35','2017-12-15 03:20:35'),(127,43,'E',2,'2017-12-15 03:20:39','2017-12-15 03:20:39'),(128,43,'E',6,'2017-12-15 03:20:42','2017-12-15 03:20:42'),(129,43,'E',14,'2017-12-15 03:20:45','2017-12-15 03:20:45'),(130,43,'F#m7',12,'2017-12-15 03:20:53','2017-12-15 03:20:53'),(131,43,'F#m7',4,'2017-12-15 03:20:57','2017-12-15 03:20:57'),(132,44,'A',0,'2017-12-15 06:56:26','2017-12-15 06:56:26'),(133,44,'A',8,'2017-12-15 06:56:56','2017-12-15 06:56:56'),(134,44,'F#m',2,'2017-12-15 06:57:05','2017-12-15 06:57:05'),(135,44,'F#m',10,'2017-12-15 06:57:10','2017-12-15 06:57:10'),(136,44,'Bm',4,'2017-12-15 06:57:18','2017-12-15 06:57:18'),(137,44,'C',12,'2017-12-15 06:57:34','2017-12-15 06:57:34'),(138,44,'D',6,'2017-12-15 06:57:54','2017-12-15 06:57:54'),(139,45,'F',0,'2017-12-15 07:00:25','2017-12-15 07:00:25'),(140,45,'Bb',8,'2017-12-15 07:00:34','2017-12-15 07:00:34'),(141,45,'Gm7',12,'2017-12-15 07:00:43','2017-12-15 07:00:43'),(142,45,'C7',16,'2017-12-15 07:00:54','2017-12-15 07:00:54'),(143,45,'F',20,'2017-12-15 07:01:07','2017-12-15 07:01:07'),(144,45,'Bb',24,'2017-12-15 07:01:15','2017-12-15 07:01:15'),(145,45,'Gm7',28,'2017-12-15 07:01:26','2017-12-15 07:01:26'),(146,45,'C7',30,'2017-12-15 07:01:36','2017-12-15 07:01:36'),(147,45,'Dm7',32,'2017-12-15 07:01:43','2017-12-15 07:01:43'),(148,45,'Dm7',40,'2017-12-15 07:01:47','2017-12-15 07:01:47'),(149,45,'Cm7',36,'2017-12-15 07:01:54','2017-12-15 07:01:54'),(150,45,'Am7',44,'2017-12-15 07:02:01','2017-12-15 07:02:01'),(151,45,'Gm7',46,'2017-12-15 07:02:07','2017-12-15 07:02:07'),(152,45,'C7',47,'2017-12-15 07:02:16','2017-12-15 07:02:16'),(153,46,'D7',0,'2017-12-15 07:03:23','2017-12-15 07:03:23'),(154,46,'D7',8,'2017-12-15 07:03:27','2017-12-15 07:03:27'),(155,46,'G7',2,'2017-12-15 07:03:35','2017-12-15 07:03:35'),(156,46,'G7',10,'2017-12-15 07:03:38','2017-12-15 07:03:38'),(157,46,'F#m7',4,'2017-12-15 07:03:52','2017-12-15 07:03:52'),(158,46,'F#m7',12,'2017-12-15 07:03:56','2017-12-15 07:03:56'),(159,46,'Bm7',6,'2017-12-15 07:04:03','2017-12-15 07:04:03'),(160,46,'Bm7',14,'2017-12-15 07:04:11','2017-12-15 07:04:11'),(161,46,'F#m7',16,'2017-12-15 07:04:31','2017-12-15 07:04:31'),(162,46,'F#m7',24,'2017-12-15 07:04:34','2017-12-15 07:04:34'),(163,46,'Bm7',20,'2017-12-15 07:04:43','2017-12-15 07:04:43'),(164,46,'C7',28,'2017-12-15 07:04:56','2017-12-15 07:04:56'),(165,47,'C',0,'2017-12-15 07:06:16','2017-12-15 07:06:16'),(166,47,'C',8,'2017-12-15 07:06:19','2017-12-15 07:06:19'),(167,47,'G',4,'2017-12-15 07:06:26','2017-12-15 07:06:26'),(168,47,'G',12,'2017-12-15 07:06:34','2017-12-15 07:06:34'),(169,47,'C',6,'2017-12-15 07:06:41','2017-12-15 07:06:41'),(170,47,'C',14,'2017-12-15 07:06:46','2017-12-15 07:06:46'),(171,47,'Am',16,'2017-12-15 07:06:55','2017-12-15 07:06:55'),(172,47,'F',20,'2017-12-15 07:07:04','2017-12-15 07:07:04'),(173,47,'Dm',23,'2017-12-15 07:07:13','2017-12-15 07:07:13'),(174,47,'C',25,'2017-12-15 07:07:19','2017-12-15 07:07:19'),(175,47,'Dm',28,'2017-12-15 07:07:29','2017-12-15 07:07:29'),(176,47,'C',31,'2017-12-15 07:07:38','2017-12-15 07:07:38'),(177,48,'G',0,'2017-12-15 07:09:12','2017-12-15 07:09:12'),(178,48,'G',16,'2017-12-15 07:09:17','2017-12-15 07:09:17'),(179,48,'C',8,'2017-12-15 07:09:25','2017-12-15 07:09:25'),(180,48,'C',24,'2017-12-15 07:09:30','2017-12-15 07:09:30'),(181,48,'F',32,'2017-12-15 07:09:41','2017-12-15 07:09:41'),(182,48,'Am',40,'2017-12-15 07:09:49','2017-12-15 07:09:49'),(183,48,'C',44,'2017-12-15 07:09:55','2017-12-15 07:09:55'),(184,48,'Dm',48,'2017-12-15 07:10:02','2017-12-15 07:10:02'),(186,48,'G',52,'2017-12-15 07:10:20','2017-12-15 07:10:20'),(187,48,'C',55,'2017-12-15 07:10:34','2017-12-15 07:10:34');
/*!40000 ALTER TABLE `phase_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase_meme`
--

LOCK TABLES `phase_meme` WRITE;
/*!40000 ALTER TABLE `phase_meme` DISABLE KEYS */;
INSERT INTO `phase_meme` VALUES (5,6,'Cool','2017-05-01 19:43:30','2017-05-01 19:43:30'),(7,7,'Hot','2017-05-01 19:44:52','2017-05-01 19:44:52'),(8,4,'Hot','2017-05-01 19:45:58','2017-05-01 19:45:58'),(9,5,'Cool','2017-05-01 19:46:10','2017-05-01 19:46:10'),(10,4,'Tropical','2017-06-16 03:37:25','2017-06-16 03:37:25'),(11,5,'Electro','2017-06-16 03:38:03','2017-06-16 03:38:03'),(12,6,'Hard','2017-06-16 03:38:19','2017-06-16 03:38:19'),(13,7,'Easy','2017-06-16 03:38:40','2017-06-16 03:38:40'),(15,14,'Hard','2017-07-29 23:48:20','2017-07-29 23:48:20'),(22,21,'Earth','2017-12-13 06:39:45','2017-12-13 06:39:45'),(23,22,'Fire','2017-12-13 06:40:10','2017-12-13 06:40:10'),(24,23,'Earth','2017-12-13 06:42:09','2017-12-13 06:42:09'),(25,24,'Water','2017-12-13 06:42:26','2017-12-13 06:42:26'),(26,25,'Earth','2017-12-13 06:43:08','2017-12-13 06:43:08'),(27,26,'Wind','2017-12-13 06:43:31','2017-12-13 06:43:31'),(28,27,'Fire','2017-12-13 06:44:02','2017-12-13 06:44:02'),(29,28,'Earth','2017-12-13 06:44:33','2017-12-13 06:44:33'),(30,29,'Fire','2017-12-13 06:46:18','2017-12-13 06:46:18'),(31,30,'Water','2017-12-13 06:46:44','2017-12-13 06:46:44'),(32,31,'Fire','2017-12-13 06:47:29','2017-12-13 06:47:29'),(33,32,'Wind','2017-12-13 06:47:51','2017-12-13 06:47:51'),(34,33,'Wind','2017-12-13 06:48:33','2017-12-13 06:48:33'),(35,34,'Earth','2017-12-13 06:48:55','2017-12-13 06:48:55'),(36,35,'Wind','2017-12-13 06:49:27','2017-12-13 06:49:27'),(37,36,'Fire','2017-12-13 06:49:47','2017-12-13 06:49:47'),(38,37,'Wind','2017-12-13 06:50:16','2017-12-13 06:50:16'),(39,38,'Water','2017-12-13 06:50:33','2017-12-13 06:50:33');
/*!40000 ALTER TABLE `phase_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'ebroot','2017-02-04 17:36:22',142,1),(2,'2','account','SQL','V2__account.sql',-728725086,'ebroot','2017-02-04 17:36:23',117,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'ebroot','2017-02-04 17:36:23',54,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'ebroot','2017-02-04 17:36:23',387,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'ebroot','2017-02-04 17:36:23',226,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'ebroot','2017-02-04 17:36:24',525,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'ebroot','2017-02-04 17:36:24',162,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'ebroot','2017-02-04 17:36:24',64,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'ebroot','2017-02-04 17:36:24',51,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'ebroot','2017-02-04 17:36:24',36,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'ebroot','2017-02-04 17:36:24',13,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'ebroot','2017-02-04 17:36:24',48,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'ebroot','2017-02-04 17:36:25',54,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'ebroot','2017-02-04 17:36:25',54,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'ebroot','2017-02-04 17:36:25',48,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'ebroot','2017-02-04 17:36:25',89,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'ebroot','2017-02-04 17:36:25',130,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'ebroot','2017-02-04 17:36:25',382,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'ebroot','2017-02-04 17:36:26',241,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'ebroot','2017-02-04 17:36:26',143,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'ebroot','2017-02-05 23:06:15',315,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'ebroot','2017-02-07 03:21:14',29,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'ebroot','2017-02-07 03:21:14',125,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'ebroot','2017-02-10 00:03:21',205,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'ebroot','2017-02-10 00:03:21',107,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'ebroot','2017-02-10 00:03:25',3684,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'ebroot','2017-02-13 19:04:58',239,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'ebroot','2017-02-14 04:55:49',175,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'ebroot','2017-02-14 04:55:50',126,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'ebroot','2017-02-14 04:55:50',139,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'ebroot','2017-02-19 22:29:51',207,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'ebroot','2017-02-19 22:29:51',29,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'ebroot','2017-04-21 16:24:11',40,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'ebroot','2017-04-28 14:57:19',58,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'ebroot','2017-04-28 14:57:19',52,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'ebroot','2017-04-28 14:57:19',53,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'ebroot','2017-04-28 14:57:19',78,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'ebroot','2017-05-01 19:09:45',95,1),(39,'40','ALTER choice MODIFY COLUMN phase offset ULONG','SQL','V40__ALTER_choice_MODIFY_COLUMN_phase_offset_ULONG.sql',-240451169,'ebroot','2017-05-18 00:34:09',63,1),(40,'41','CREATE TABLE link meme','SQL','V41__CREATE_TABLE_link_meme.sql',-18883080,'ebroot','2017-05-18 00:34:09',51,1),(41,'42','ALTER phase link INT total','SQL','V42__ALTER_phase_link_INT_total.sql',-1400879099,'ebroot','2017-05-18 00:34:10',122,1),(42,'43','CREATE TABLE link message','SQL','V43__CREATE_TABLE_link_message.sql',1616909549,'ebroot','2017-05-18 00:34:10',46,1),(43,'44','ALTER pick BELONGS TO arrangement DROP morph point','SQL','V44__ALTER_pick_BELONGS_TO_arrangement_DROP_morph_point.sql',449955118,'ebroot','2017-05-26 00:58:12',563,1),(44,'45','ALTER link ADD COLUMN waveform key','SQL','V45__ALTER_link_ADD_COLUMN_waveform_key.sql',-98370,'ebroot','2017-06-01 16:53:07',811,1),(45,'46','ALTER audio ADD COLUMN state','SQL','V46__ALTER_audio_ADD_COLUMN_state.sql',-1300058820,'ebroot','2017-06-04 21:28:24',161,1),(46,'47','ALTER chain ADD COLUMN embed key','SQL','V47__ALTER_chain_ADD_COLUMN_embed_key.sql',317233573,'ebroot','2017-10-15 09:45:02',903,1),(47,'48','CREATE TABLE platform message','SQL','V48__CREATE_TABLE_platform_message.sql',-1332226532,'ebroot','2017-12-02 07:28:17',114,1),(48,'49','CREATE pattern DEPRECATES idea','SQL','V49__CREATE_pattern_DEPRECATES_idea.sql',517513730,'ebroot','2017-12-07 05:37:18',3380,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Charney Kaye','charneykaye@gmail.com','https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'Chris Luken','christopher.luken@gmail.com','https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'David Cole','davecolemusic@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-03-08 02:26:51','2017-03-08 02:26:51'),(4,'Shannon Holloway','shannon.holloway@gmail.com','https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50','2017-03-08 18:14:53','2017-03-08 18:14:53'),(5,'Lev Kaye','lev@kaye.com','https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50','2017-03-09 23:47:12','2017-03-09 23:47:12'),(6,'Justin Knowlden (gus)','gus@gusg.us','https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50','2017-04-14 20:41:41','2017-04-14 20:41:41'),(7,'dave farkas','sakrafd@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-14 20:42:36','2017-04-14 20:42:36'),(8,'Aji Putra','aji.perdana.putra@gmail.com','https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50','2017-04-21 17:33:25','2017-04-21 17:33:25'),(9,'live espn789','scoreplace@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-21 19:13:22','2017-04-21 19:13:22'),(10,'Dmitry Solomadin','dmitry.solomadin@gmail.com','https://lh6.googleusercontent.com/-Ns78xq2VzKk/AAAAAAAAAAI/AAAAAAAAE44/ZOuBZnZqYeU/photo.jpg?sz=50','2017-05-03 21:09:33','2017-05-03 21:09:33'),(11,'Michael Prolagaev','prolagaev@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-05-04 16:13:06','2017-05-04 16:13:06'),(12,'Charney Kaye','nick.c.kaye@gmail.com','https://lh5.googleusercontent.com/-_oXIqxZhTkk/AAAAAAAAAAI/AAAAAAAAUks/dg9oNRfPFco/photo.jpg?sz=50','2017-05-18 17:37:32','2017-05-18 17:37:32'),(13,'Charney Kaye','charney@outrightmental.com','https://lh5.googleusercontent.com/-3yrpEvNKIvE/AAAAAAAAAAI/AAAAAAAAASc/Gls7ZJcVqCk/photo.jpg?sz=50','2017-06-19 20:39:46','2017-06-19 20:39:46'),(14,'Philip Z. Kimball','pzkimball@pzklaw.com','https://lh4.googleusercontent.com/-xnsM2SBKwaE/AAAAAAAAAAI/AAAAAAAAABs/uJouNj6fMgw/photo.jpg?sz=50','2017-06-26 13:56:57','2017-06-26 13:56:57'),(15,'Janae\' Leonard','janaeleo55@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-06-28 09:30:40','2017-06-28 09:30:40'),(16,'yuan liu','minamotoclan@gmail.com','https://lh6.googleusercontent.com/-4orhpHPwHN4/AAAAAAAAAAI/AAAAAAAAFGc/HYueBarZIwA/photo.jpg?sz=50','2017-07-03 03:16:24','2017-07-03 03:16:24'),(17,'Nick Podgurski','nickpodgurski@gmail.com','https://lh5.googleusercontent.com/-Cly5aKHLBMc/AAAAAAAAAAI/AAAAAAAAAYQ/wu8BxP-Zwxk/photo.jpg?sz=50','2017-07-04 03:59:02','2017-07-04 03:59:02'),(18,'Brian Sweeny','brian@vibesinternational.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-05 16:01:53','2017-07-05 16:01:53'),(19,'John Bennett','johnalsobennett@gmail.com','https://lh6.googleusercontent.com/-kFMmioNSrEM/AAAAAAAAAAI/AAAAAAAABfg/SfT2vo__XgI/photo.jpg?sz=50','2017-07-06 15:08:32','2017-07-06 15:08:32'),(20,'Aditi Hebbar','adhebbar@gmail.com','https://lh4.googleusercontent.com/-gUnZUky1WtE/AAAAAAAAAAI/AAAAAAAAEJ8/sFumIpFdaUA/photo.jpg?sz=50','2017-07-07 08:42:46','2017-07-07 08:42:46'),(21,'HANKYOL CHO','hankyolcho@mail.adelphi.edu','https://lh3.googleusercontent.com/-skrgmZw2fas/AAAAAAAAAAI/AAAAAAAAAAA/iwMwVr_CL2U/photo.jpg?sz=50','2017-07-10 14:10:03','2017-07-10 14:10:03'),(22,'Charles Frantz','charlesfrantz@gmail.com','https://lh4.googleusercontent.com/-WtgVMTchHkY/AAAAAAAAAAI/AAAAAAAAAMU/4hX0mxVuIBE/photo.jpg?sz=50','2017-07-13 14:28:39','2017-07-13 14:28:39'),(23,'Alice Gamarnik','ajgamarnik@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-14 16:25:46','2017-07-14 16:25:46'),(24,'liu xin','xinliu2530@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-17 18:46:18','2017-07-17 18:46:18'),(25,'Outright Mental','outrightmental@gmail.com','https://lh5.googleusercontent.com/-2HcQgfYoQRU/AAAAAAAAAAI/AAAAAAAAANE/-ttDusZjeuk/photo.jpg?sz=50','2017-07-30 16:26:49','2017-07-30 16:26:49'),(26,'Joey Lorjuste','joeylorjuste@gmail.com','https://lh4.googleusercontent.com/-WPQgkyb-M5A/AAAAAAAAAAI/AAAAAAAAH-Q/Lf9IG0JJl5c/photo.jpg?sz=50','2017-08-20 19:25:12','2017-08-20 19:25:12'),(27,'Mark Stewart','mark.si.stewart@gmail.com','https://lh3.googleusercontent.com/-PtMRcK_-Bkg/AAAAAAAAAAI/AAAAAAAAASs/YlN0XjZSvdg/photo.jpg?sz=50','2017-08-25 19:30:40','2017-08-25 19:30:40'),(28,'Rosalind Kaye','rckaye@kaye.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-16 00:11:49','2017-10-16 00:11:49'),(29,'Matthew DellaRatta','mdellaratta8@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-10-17 00:00:36','2017-10-17 00:00:36'),(30,'Justice Whitaker','justice512@gmail.com','https://lh5.googleusercontent.com/-Y9sCwQKldqA/AAAAAAAAAAI/AAAAAAAAADE/3wU9xJLYRG0/photo.jpg?sz=50','2017-12-08 20:45:40','2017-12-08 20:45:40'),(31,'Ed Carney','ed@steirmancpas.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-12-13 15:13:49','2017-12-13 15:13:49');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'user',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'admin',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'user',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,'artist',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(5,'artist',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(6,'user',3,'2017-03-08 02:26:51','2017-03-08 02:26:51'),(7,'artist',3,'2017-03-08 02:27:15','2017-03-08 02:27:15'),(8,'user',4,'2017-03-08 18:14:53','2017-03-08 18:14:53'),(9,'artist',4,'2017-03-09 17:48:55','2017-03-09 17:48:55'),(10,'user',5,'2017-03-09 23:47:12','2017-03-09 23:47:12'),(11,'artist',5,'2017-03-10 05:39:23','2017-03-10 05:39:23'),(12,'user',6,'2017-04-14 20:41:41','2017-04-14 20:41:41'),(13,'user',7,'2017-04-14 20:42:36','2017-04-14 20:42:36'),(14,'artist',6,'2017-04-17 20:59:16','2017-04-17 20:59:16'),(15,'artist',7,'2017-04-17 20:59:21','2017-04-17 20:59:21'),(16,'user',8,'2017-04-21 17:33:25','2017-04-21 17:33:25'),(17,'user',9,'2017-04-21 19:13:22','2017-04-21 19:13:22'),(22,'user',12,'2017-05-18 17:37:32','2017-05-18 17:37:32'),(23,'artist',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(24,'engineer',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(25,'user',13,'2017-06-19 20:39:46','2017-06-19 20:39:46'),(26,'user',14,'2017-06-26 13:56:57','2017-06-26 13:56:57'),(27,'artist',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(28,'engineer',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(29,'user',15,'2017-06-28 09:30:40','2017-06-28 09:30:40'),(30,'user',16,'2017-07-03 03:16:24','2017-07-03 03:16:24'),(31,'user',17,'2017-07-04 03:59:02','2017-07-04 03:59:02'),(32,'user',18,'2017-07-05 16:01:53','2017-07-05 16:01:53'),(33,'user',19,'2017-07-06 15:08:32','2017-07-06 15:08:32'),(34,'user',20,'2017-07-07 08:42:46','2017-07-07 08:42:46'),(35,'banned',10,'2017-07-07 20:53:49','2017-07-07 20:53:49'),(36,'banned',11,'2017-07-07 20:53:55','2017-07-07 20:53:55'),(37,'user',21,'2017-07-10 14:10:03','2017-07-10 14:10:03'),(38,'user',22,'2017-07-13 14:28:39','2017-07-13 14:28:39'),(39,'artist',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(40,'engineer',22,'2017-07-13 15:19:25','2017-07-13 15:19:25'),(41,'engineer',7,'2017-07-13 15:19:32','2017-07-13 15:19:32'),(42,'engineer',6,'2017-07-13 15:20:27','2017-07-13 15:20:27'),(43,'user',23,'2017-07-14 16:25:46','2017-07-14 16:25:46'),(44,'user',24,'2017-07-17 18:46:18','2017-07-17 18:46:18'),(45,'artist',24,'2017-07-17 18:46:58','2017-07-17 18:46:58'),(47,'artist',23,'2017-07-17 18:47:04','2017-07-17 18:47:04'),(49,'user',25,'2017-07-30 16:26:49','2017-07-30 16:26:49'),(50,'artist',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(51,'engineer',25,'2017-07-30 16:27:35','2017-07-30 16:27:35'),(52,'artist',13,'2017-07-30 16:27:43','2017-07-30 16:27:43'),(53,'user',26,'2017-08-20 19:25:12','2017-08-20 19:25:12'),(54,'user',27,'2017-08-25 19:30:40','2017-08-25 19:30:40'),(55,'artist',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(56,'engineer',27,'2017-08-25 19:45:56','2017-08-25 19:45:56'),(57,'user',28,'2017-10-16 00:11:49','2017-10-16 00:11:49'),(58,'user',29,'2017-10-17 00:00:36','2017-10-17 00:00:36'),(59,'user',30,'2017-12-08 20:45:40','2017-12-08 20:45:40'),(60,'artist',30,'2017-12-08 20:47:55','2017-12-08 20:47:55'),(61,'engineer',1,'2017-12-12 06:57:46','2017-12-12 06:57:46'),(62,'user',31,'2017-12-13 15:13:49','2017-12-13 15:13:49');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice`
--

LOCK TABLES `voice` WRITE;
/*!40000 ALTER TABLE `voice` DISABLE KEYS */;
INSERT INTO `voice` VALUES (4,3,'percussive','Kick+Snare','2017-04-23 23:45:07','2017-06-03 00:03:54'),(5,3,'percussive','Locomotion','2017-06-03 00:04:07','2017-06-03 00:04:07'),(8,3,'percussive','Toms+Congas+Misc','2017-06-11 19:50:10','2017-06-16 02:17:56'),(10,3,'percussive','Vocal','2017-06-23 23:43:10','2017-06-23 23:43:10'),(11,3,'percussive','Vocal Echo','2017-06-24 01:29:49','2017-06-24 01:29:49'),(12,3,'Percussive','2x4 Stomp','2017-12-07 03:43:08','2017-12-09 16:19:10'),(13,39,'Percussive','Locomotion','2017-12-13 06:58:10','2017-12-13 06:58:10'),(14,39,'Percussive','4x4','2017-12-15 04:29:58','2017-12-15 04:29:58'),(15,39,'Percussive','Alternate 2x2','2017-12-15 04:31:34','2017-12-15 04:31:34');
/*!40000 ALTER TABLE `voice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice_event`
--

LOCK TABLES `voice_event` WRITE;
/*!40000 ALTER TABLE `voice_event` DISABLE KEYS */;
INSERT INTO `voice_event` VALUES (270,4,0.3,1,'KICKLONG',2.5,0.5,'C2','2017-06-02 23:57:53','2017-12-07 08:12:58'),(274,4,1,0.2,'SNARE',1,1,'G8','2017-06-02 23:58:37','2017-06-10 19:32:41'),(275,5,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-06-03 00:09:06','2017-06-16 00:09:28'),(276,5,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-06-03 00:10:09','2017-06-16 00:10:12'),(277,5,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-06-03 00:10:14','2017-06-16 00:11:24'),(278,5,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-06-03 00:10:19','2017-06-16 00:12:20'),(280,5,0.1,0.1,'HIHATCLOSED',0.3,0.2,'G12','2017-06-03 00:11:48','2017-06-16 00:09:35'),(281,5,0.1,0.1,'HIHATCLOSED',1.3,0.2,'G12','2017-06-03 00:11:52','2017-06-16 00:10:18'),(282,5,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-06-03 00:11:57','2017-06-16 00:11:41'),(283,5,0.1,0.1,'HIHATCLOSED',3.3,0.2,'G12','2017-06-03 00:12:02','2017-06-16 00:12:26'),(284,5,0.12,0.06,'HIHATCLOSED',3.8,0.2,'D12','2017-06-03 00:12:32','2017-06-16 00:13:09'),(285,5,0.025,0.06,'HIHATCLOSED',2.8,0.2,'D12','2017-06-03 00:12:37','2017-12-07 18:57:08'),(286,5,0.12,0.06,'HIHATCLOSED',1.8,0.2,'D12','2017-06-03 00:12:41','2017-06-16 00:11:18'),(287,5,0.025,0.06,'HIHATCLOSED',0.8,0.2,'D12','2017-06-03 00:12:46','2017-12-07 18:56:55'),(288,5,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-06-03 00:13:33','2017-06-16 00:09:49'),(290,5,0.1,0.12,'HIHATCLOSED',2.3,0.2,'E8','2017-06-03 00:13:43','2017-06-16 00:11:29'),(291,5,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-06-03 00:13:47','2017-06-16 00:12:53'),(292,4,0.3,0.1,'CLAP',3,1,'Bb','2017-06-04 04:23:00','2017-06-15 22:50:44'),(294,4,0.2,1,'KICK',2.3,0.2,'F#2','2017-06-04 04:26:37','2017-12-07 08:13:06'),(295,4,0.1,0.1,'CLAP',1,1,'G5','2017-06-04 04:39:36','2017-06-04 04:39:36'),(301,5,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-06-04 04:49:14','2017-06-16 00:11:02'),(302,5,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-06-04 04:51:11','2017-06-16 00:10:47'),(303,5,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-06-04 04:51:23','2017-06-16 00:10:53'),(304,5,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-06-04 04:51:28','2017-06-16 00:11:52'),(305,5,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-06-04 04:51:34','2017-06-16 00:13:03'),(306,5,0.1,0.1,'MARACAS',3.3,0.5,'Bb8','2017-06-04 04:51:43','2017-06-16 00:12:34'),(314,8,0.1,0.6,'TOM',0.5,0.75,'C6','2017-06-11 19:51:53','2017-12-10 10:03:32'),(315,8,0.05,0.6,'TOM',1.3,0.7,'G5','2017-06-11 19:52:17','2017-12-10 10:03:13'),(316,8,0.2,0.6,'TOM',2,1,'C5','2017-06-11 19:53:15','2017-07-29 21:19:23'),(318,8,0.1,0.6,'CONGA',0,1,'F5','2017-06-11 20:15:46','2017-12-10 10:03:39'),(320,8,0.1,0.6,'TOM',3.5,0.5,'G3','2017-06-11 20:16:54','2017-12-07 08:22:05'),(322,4,0.1,0.2,'SNARE',1.8,0.2,'G5','2017-06-12 19:14:16','2017-12-10 10:05:01'),(323,8,0.05,0.5,'COWBELL',2,1,'F5','2017-06-12 19:20:22','2017-06-15 22:51:48'),(329,8,0.3,0.6,'CONGAHIGH',3.3,0.6,'F5','2017-06-15 22:52:40','2017-07-29 21:19:27'),(332,10,0.1,0.3,'HEY',0.5,1,'X','2017-06-23 23:44:29','2017-07-31 03:48:10'),(333,11,0.01,0.3,'HEY',1,1,'X','2017-06-24 01:30:13','2017-12-10 11:04:42'),(334,10,0.05,0.3,'HEY',1.5,1,'X','2017-07-31 03:48:04','2017-07-31 03:48:04'),(335,10,0.025,0.3,'HEY',2.5,1,'x','2017-07-31 03:48:39','2017-07-31 03:48:39'),(336,10,0.05,0.3,'HEY',3.5,0.5,'x','2017-07-31 03:48:51','2017-07-31 03:48:51'),(337,11,0.0125,0.3,'HEY',2,1,'x','2017-07-31 03:49:36','2017-07-31 03:49:36'),(338,11,0.025,0.3,'HEY',3.25,1,'x','2017-07-31 03:49:56','2017-12-10 11:05:37'),(339,12,1,1,'KICKLONG',0,1,'C2','2017-12-07 03:43:32','2017-12-07 08:10:49'),(341,12,0.8,1,'KICK',2.5,1,'C2','2017-12-07 03:43:52','2017-12-09 16:19:31'),(343,4,1,0.1,'SNARE',3,1,'G8','2017-12-07 08:17:58','2017-12-07 08:17:58'),(344,13,0.5,0.1,'BA',0,2,'C5','2017-12-13 06:58:47','2017-12-14 07:55:29'),(345,13,0.5,0.1,'BA',1,2,'C5','2017-12-13 06:59:02','2017-12-14 07:55:29'),(346,13,0.5,0.1,'BA',2,2,'C5','2017-12-13 06:59:07','2017-12-14 07:55:29'),(347,13,0.5,0.1,'BA',3,2,'C5','2017-12-13 06:59:11','2017-12-14 07:55:29'),(348,13,0.1,0.1,'KA',3.25,2,'C5','2017-12-13 06:59:27','2017-12-14 07:55:39'),(349,13,0.1,0.1,'KA',3.75,2,'C5','2017-12-13 06:59:32','2017-12-14 07:55:39'),(350,13,0.1,0.1,'KA',2.75,2,'C5','2017-12-13 06:59:40','2017-12-14 07:55:39'),(351,13,0.1,0.1,'KA',2.25,2,'C5','2017-12-13 06:59:45','2017-12-14 07:55:39'),(352,13,0.1,0.1,'KA',1.25,2,'C5','2017-12-13 06:59:52','2017-12-14 07:55:39'),(353,13,0.1,0.1,'KA',1.75,2,'C5','2017-12-13 06:59:56','2017-12-14 07:55:39'),(354,13,0.3,0.2,'LA',1.5,2,'C5','2017-12-13 07:00:24','2017-12-14 07:55:51'),(355,13,0.3,0.2,'LA',2.5,2,'C5','2017-12-13 07:00:28','2017-12-14 07:55:51'),(356,13,0.3,0.2,'LA',3.5,2,'C5','2017-12-13 07:00:33','2017-12-14 07:55:51'),(357,13,0.3,0.2,'LA',0.5,2,'C5','2017-12-13 07:00:59','2017-12-14 07:55:51'),(358,13,0.1,0.1,'KA',0.75,2,'C5','2017-12-13 07:01:08','2017-12-14 07:55:39'),(359,13,0.1,0.1,'KA',0.25,2,'C5','2017-12-13 07:01:14','2017-12-14 07:55:39'),(360,14,1,1,'POOM',0,1,'c5','2017-12-15 04:30:12','2017-12-15 04:30:12'),(361,14,1,1,'POOM',1,1,'c5','2017-12-15 04:30:28','2017-12-15 04:30:28'),(362,14,1,1,'POOM',2,1,'G5','2017-12-15 04:30:39','2017-12-15 04:30:39'),(363,14,1,1,'POOM',3,1,'C5','2017-12-15 04:30:47','2017-12-15 04:30:47'),(364,15,1,1,'BOOOOM',4,4,'C3','2017-12-15 04:31:51','2017-12-15 04:33:48'),(365,15,0.3,1,'POP',6,2,'c2','2017-12-15 04:32:14','2017-12-15 04:32:14');
/*!40000 ALTER TABLE `voice_event` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-12-15  7:33:19
