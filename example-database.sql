-- MySQL dump 10.13  Distrib 5.5.54, for Linux (x86_64)
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=458818 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_fk_account` (`account_id`),
  CONSTRAINT `chain_fk_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chain_idea`
--

DROP TABLE IF EXISTS `chain_idea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chain_idea` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chain_id` bigint(20) unsigned NOT NULL,
  `idea_id` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `chain_idea_fk_chain_idx` (`chain_id`),
  KEY `chain_idea_fk_idea_idx` (`idea_id`),
  CONSTRAINT `chain_idea_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `chain_idea_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;
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
  `idea_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `transpose` int(11) NOT NULL,
  `phase_offset` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `choice_fk_link_idx` (`link_id`),
  KEY `choice_fk_idea_idx` (`idea_id`),
  CONSTRAINT `choice_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=345747 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `idea`
--

DROP TABLE IF EXISTS `idea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `idea` (
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
  KEY `idea_fk_library_idx` (`library_id`),
  KEY `idea_fk_user` (`user_id`),
  CONSTRAINT `idea_fk_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `idea_fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `idea_meme`
--

DROP TABLE IF EXISTS `idea_meme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `idea_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `idea_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `meme_fk_idea_idx` (`idea_id`),
  CONSTRAINT `meme_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=839821 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=791425 DEFAULT CHARSET=latin1;
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
-- Table structure for table `phase`
--

DROP TABLE IF EXISTS `phase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phase` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `idea_id` bigint(20) unsigned NOT NULL,
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
  KEY `phase_fk_idea_idx` (`idea_id`),
  CONSTRAINT `phase_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=37062175 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=334 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-07-07 21:02:33

-- MySQL dump 10.13  Distrib 5.5.54, for Linux (x86_64)
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
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Charney Kaye','charneykaye@gmail.com','https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'Chris Luken','christopher.luken@gmail.com','https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'David Cole','davecolemusic@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-03-08 02:26:51','2017-03-08 02:26:51'),(4,'Shannon Holloway','shannon.holloway@gmail.com','https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50','2017-03-08 18:14:53','2017-03-08 18:14:53'),(5,'Lev Kaye','lev@kaye.com','https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50','2017-03-09 23:47:12','2017-03-09 23:47:12'),(6,'Justin Knowlden (gus)','gus@gusg.us','https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50','2017-04-14 20:41:41','2017-04-14 20:41:41'),(7,'dave farkas','sakrafd@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-14 20:42:36','2017-04-14 20:42:36'),(8,'Aji Putra','aji.perdana.putra@gmail.com','https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50','2017-04-21 17:33:25','2017-04-21 17:33:25'),(9,'live espn789','scoreplace@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-21 19:13:22','2017-04-21 19:13:22'),(10,'Dmitry Solomadin','dmitry.solomadin@gmail.com','https://lh6.googleusercontent.com/-Ns78xq2VzKk/AAAAAAAAAAI/AAAAAAAAE44/ZOuBZnZqYeU/photo.jpg?sz=50','2017-05-03 21:09:33','2017-05-03 21:09:33'),(11,'Michael Prolagaev','prolagaev@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-05-04 16:13:06','2017-05-04 16:13:06'),(12,'Charney Kaye','nick.c.kaye@gmail.com','https://lh5.googleusercontent.com/-_oXIqxZhTkk/AAAAAAAAAAI/AAAAAAAAUks/dg9oNRfPFco/photo.jpg?sz=50','2017-05-18 17:37:32','2017-05-18 17:37:32'),(13,'Charney Kaye','charney@outrightmental.com','https://lh5.googleusercontent.com/-3yrpEvNKIvE/AAAAAAAAAAI/AAAAAAAAASc/Gls7ZJcVqCk/photo.jpg?sz=50','2017-06-19 20:39:46','2017-06-19 20:39:46'),(14,'Philip Z. Kimball','pzkimball@pzklaw.com','https://lh4.googleusercontent.com/-xnsM2SBKwaE/AAAAAAAAAAI/AAAAAAAAABs/uJouNj6fMgw/photo.jpg?sz=50','2017-06-26 13:56:57','2017-06-26 13:56:57'),(15,'Janae\' Leonard','janaeleo55@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-06-28 09:30:40','2017-06-28 09:30:40'),(16,'yuan liu','minamotoclan@gmail.com','https://lh6.googleusercontent.com/-4orhpHPwHN4/AAAAAAAAAAI/AAAAAAAAFGc/HYueBarZIwA/photo.jpg?sz=50','2017-07-03 03:16:24','2017-07-03 03:16:24'),(17,'Nick Podgurski','nickpodgurski@gmail.com','https://lh5.googleusercontent.com/-Cly5aKHLBMc/AAAAAAAAAAI/AAAAAAAAAYQ/wu8BxP-Zwxk/photo.jpg?sz=50','2017-07-04 03:59:02','2017-07-04 03:59:02'),(18,'Brian Sweeny','brian@vibesinternational.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-07-05 16:01:53','2017-07-05 16:01:53'),(19,'John Bennett','johnalsobennett@gmail.com','https://lh6.googleusercontent.com/-kFMmioNSrEM/AAAAAAAAAAI/AAAAAAAABfg/SfT2vo__XgI/photo.jpg?sz=50','2017-07-06 15:08:32','2017-07-06 15:08:32'),(20,'Aditi Hebbar','adhebbar@gmail.com','https://lh4.googleusercontent.com/-gUnZUky1WtE/AAAAAAAAAAI/AAAAAAAAEJ8/sFumIpFdaUA/photo.jpg?sz=50','2017-07-07 08:42:46','2017-07-07 08:42:46');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'user',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'admin',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'user',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,'artist',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(5,'artist',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(6,'user',3,'2017-03-08 02:26:51','2017-03-08 02:26:51'),(7,'artist',3,'2017-03-08 02:27:15','2017-03-08 02:27:15'),(8,'user',4,'2017-03-08 18:14:53','2017-03-08 18:14:53'),(9,'artist',4,'2017-03-09 17:48:55','2017-03-09 17:48:55'),(10,'user',5,'2017-03-09 23:47:12','2017-03-09 23:47:12'),(11,'artist',5,'2017-03-10 05:39:23','2017-03-10 05:39:23'),(12,'user',6,'2017-04-14 20:41:41','2017-04-14 20:41:41'),(13,'user',7,'2017-04-14 20:42:36','2017-04-14 20:42:36'),(14,'artist',6,'2017-04-17 20:59:16','2017-04-17 20:59:16'),(15,'artist',7,'2017-04-17 20:59:21','2017-04-17 20:59:21'),(16,'user',8,'2017-04-21 17:33:25','2017-04-21 17:33:25'),(17,'user',9,'2017-04-21 19:13:22','2017-04-21 19:13:22'),(22,'user',12,'2017-05-18 17:37:32','2017-05-18 17:37:32'),(23,'artist',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(24,'engineer',12,'2017-05-18 17:38:45','2017-05-18 17:38:45'),(25,'user',13,'2017-06-19 20:39:46','2017-06-19 20:39:46'),(26,'user',14,'2017-06-26 13:56:57','2017-06-26 13:56:57'),(27,'artist',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(28,'engineer',14,'2017-06-26 14:46:10','2017-06-26 14:46:10'),(29,'user',15,'2017-06-28 09:30:40','2017-06-28 09:30:40'),(30,'user',16,'2017-07-03 03:16:24','2017-07-03 03:16:24'),(31,'user',17,'2017-07-04 03:59:02','2017-07-04 03:59:02'),(32,'user',18,'2017-07-05 16:01:53','2017-07-05 16:01:53'),(33,'user',19,'2017-07-06 15:08:32','2017-07-06 15:08:32'),(34,'user',20,'2017-07-07 08:42:46','2017-07-07 08:42:46'),(35,'banned',10,'2017-07-07 20:53:49','2017-07-07 20:53:49'),(36,'banned',11,'2017-07-07 20:53:55','2017-07-07 20:53:55');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'Alpha','2017-02-10 00:03:21','2017-05-03 22:15:24'),(2,'Beta','2017-02-10 00:03:21','2017-05-03 22:15:39'),(3,'Retro','2017-02-10 00:03:21','2017-05-03 22:17:04');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_user`
--

LOCK TABLES `account_user` WRITE;
/*!40000 ALTER TABLE `account_user` DISABLE KEYS */;
INSERT INTO `account_user` VALUES (1,1,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(2,1,2,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(3,3,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(4,2,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(5,1,3,'2017-03-08 02:27:34','2017-03-08 02:27:34'),(6,1,4,'2017-03-08 23:30:42','2017-03-08 23:30:42'),(7,1,5,'2017-03-10 05:39:37','2017-03-10 05:39:37'),(8,1,6,'2017-04-17 21:59:20','2017-04-17 21:59:20'),(9,1,7,'2017-04-17 21:59:24','2017-04-17 21:59:24'),(12,1,12,'2017-05-18 17:39:30','2017-05-18 17:39:30'),(13,1,14,'2017-06-26 14:48:43','2017-06-26 14:48:43');
/*!40000 ALTER TABLE `account_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `library`
--

LOCK TABLES `library` WRITE;
/*!40000 ALTER TABLE `library` DISABLE KEYS */;
INSERT INTO `library` VALUES (1,'Pots and Pans #2',1,'2017-02-10 00:03:23','2017-06-21 01:33:46'),(2,'K Project 2',3,'2017-02-10 00:03:23','2017-02-10 00:03:23');
/*!40000 ALTER TABLE `library` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `idea`
--

LOCK TABLES `idea` WRITE;
/*!40000 ALTER TABLE `idea` DISABLE KEYS */;
INSERT INTO `idea` VALUES (2,1,2,'main','Introducing K',0.5,'G minor',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(3,1,2,'main','Mental Addition',0.5,'D minor',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(4,1,2,'main','Children of the Who',0.7,'A major',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(5,1,2,'main','K Project',0.6,'G major',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(6,1,1,'rhythm','Basic Beat',0.62,'C',120,'2017-04-23 23:21:52','2017-04-23 23:21:52'),(7,1,1,'macro','Deep, from Hot to Cool',0.6,'C',118,'2017-05-01 18:59:22','2017-06-16 04:36:20'),(8,1,1,'macro','Deep, from Cool to Hot',0.6,'G minor',118,'2017-05-01 18:59:32','2017-06-16 04:36:07'),(9,1,1,'main','I\'ll House You',0.5,'C',118,'2017-05-13 00:04:19','2017-06-16 04:38:45'),(10,1,1,'support','Tom Conga Jam 1',0.3,'C',120,'2017-06-05 05:19:04','2017-06-05 05:19:04');
/*!40000 ALTER TABLE `idea` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `idea_meme`
--

LOCK TABLES `idea_meme` WRITE;
/*!40000 ALTER TABLE `idea_meme` DISABLE KEYS */;
INSERT INTO `idea_meme` VALUES (1,6,'Classic','2017-04-23 23:22:21','2017-04-23 23:22:21'),(2,6,'Deep','2017-04-23 23:22:23','2017-04-23 23:22:23'),(3,6,'Acid','2017-04-23 23:22:24','2017-04-23 23:22:24'),(5,6,'Tech','2017-04-23 23:22:28','2017-04-23 23:22:28'),(6,6,'Electro','2017-04-23 23:22:31','2017-04-23 23:22:31'),(7,6,'Tropical','2017-04-23 23:22:34','2017-04-23 23:22:34'),(8,6,'Hot','2017-04-23 23:22:36','2017-04-23 23:22:36'),(9,6,'Cool','2017-04-23 23:22:39','2017-04-23 23:22:39'),(10,6,'Hard','2017-04-23 23:22:40','2017-04-23 23:22:40'),(11,6,'Easy','2017-04-23 23:22:42','2017-04-23 23:22:42'),(12,6,'Progressive','2017-04-23 23:23:17','2017-04-23 23:23:17'),(15,7,'Deep','2017-05-01 18:59:46','2017-05-01 18:59:46'),(16,8,'Deep','2017-05-01 19:42:36','2017-05-01 19:42:36'),(17,9,'Deep','2017-05-13 00:04:41','2017-05-13 00:04:41'),(18,9,'Classic','2017-05-13 00:04:44','2017-05-13 00:04:44'),(21,10,'Tropical','2017-06-05 05:19:14','2017-06-05 05:19:14'),(22,10,'Classic','2017-06-05 05:19:18','2017-06-05 05:19:18'),(34,9,'Hard','2017-06-16 04:26:57','2017-06-16 04:26:57');
/*!40000 ALTER TABLE `idea_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase`
--

LOCK TABLES `phase` WRITE;
/*!40000 ALTER TABLE `phase` DISABLE KEYS */;
INSERT INTO `phase` VALUES (2,2,'Intro',0,64,NULL,'G minor',121,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,6,'drop',0,4,NULL,NULL,NULL,'2017-04-23 23:44:19','2017-06-03 00:15:10'),(4,7,'from Hot',0,0,0.7,'C',118,'2017-05-01 19:39:59','2017-06-16 04:35:12'),(5,7,'to Cool',1,0,0.5,'Bb Minor',118,'2017-05-01 19:40:18','2017-06-16 04:35:22'),(6,8,'from Cool',0,0,0.5,'G minor',118,'2017-05-01 19:43:06','2017-06-16 04:35:45'),(7,8,'to Hot',1,0,0.7,'C',118,'2017-05-01 19:43:26','2017-06-16 04:35:52'),(8,9,'Drop',0,32,0.4,'C',121,'2017-05-13 00:05:29','2017-06-16 03:51:44'),(9,9,'Breakdown',1,32,0.8,'G minor',121,'2017-05-13 00:07:19','2017-06-16 04:30:12'),(10,10,'Drop',0,8,NULL,NULL,NULL,'2017-06-05 05:20:06','2017-06-05 05:20:06');
/*!40000 ALTER TABLE `phase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase_chord`
--

LOCK TABLES `phase_chord` WRITE;
/*!40000 ALTER TABLE `phase_chord` DISABLE KEYS */;
INSERT INTO `phase_chord` VALUES (2,2,'G minor',0,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,2,'D major',12,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(4,2,'G minor',16,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(5,2,'F minor',28,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(6,2,'G minor',32,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(7,3,'C',0,'2017-04-23 23:44:43','2017-04-23 23:44:43'),(8,8,'C major 7',0,'2017-05-13 00:05:58','2017-06-16 03:54:30'),(9,8,'Cm7',8,'2017-05-13 00:06:11','2017-06-16 03:55:17'),(10,8,'F7',12,'2017-05-13 00:06:28','2017-06-16 03:58:00'),(11,8,'Bb major 7',16,'2017-05-13 00:06:41','2017-06-16 03:58:13'),(12,9,'D',0,'2017-05-13 00:07:40','2017-06-16 04:00:16'),(13,9,'G',4,'2017-05-13 00:07:47','2017-06-16 04:00:22'),(14,9,'C',8,'2017-05-13 00:07:55','2017-06-16 04:00:31'),(15,9,'F7',12,'2017-05-13 00:08:01','2017-06-16 04:31:20'),(19,8,'Bb m7',24,'2017-06-16 03:59:02','2017-06-16 03:59:02'),(20,8,'Eb7',28,'2017-06-16 03:59:38','2017-06-16 03:59:38'),(21,8,'Ab major 7',30,'2017-06-16 03:59:46','2017-06-16 03:59:46'),(28,9,'E minor 7',16,'2017-06-16 04:30:36','2017-06-16 04:38:15'),(29,9,'Eb minor 7',20,'2017-06-16 04:30:43','2017-06-16 04:38:10'),(30,9,'D minor 7',24,'2017-06-16 04:30:49','2017-06-16 04:38:26'),(31,9,'Db minor 7',28,'2017-06-16 04:31:03','2017-06-16 04:38:33');
/*!40000 ALTER TABLE `phase_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `phase_meme`
--

LOCK TABLES `phase_meme` WRITE;
/*!40000 ALTER TABLE `phase_meme` DISABLE KEYS */;
INSERT INTO `phase_meme` VALUES (1,2,'Foreboding','2017-02-10 00:03:23','2017-02-10 00:03:23'),(2,2,'Dark','2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,2,'Funky','2017-02-10 00:03:23','2017-02-10 00:03:23'),(4,2,'Hard','2017-02-10 00:03:23','2017-02-10 00:03:23'),(5,6,'Cool','2017-05-01 19:43:30','2017-05-01 19:43:30'),(7,7,'Hot','2017-05-01 19:44:52','2017-05-01 19:44:52'),(8,4,'Hot','2017-05-01 19:45:58','2017-05-01 19:45:58'),(9,5,'Cool','2017-05-01 19:46:10','2017-05-01 19:46:10'),(10,4,'Tropical','2017-06-16 03:37:25','2017-06-16 03:37:25'),(11,5,'Electro','2017-06-16 03:38:03','2017-06-16 03:38:03'),(12,6,'Hard','2017-06-16 03:38:19','2017-06-16 03:38:19'),(13,7,'Easy','2017-06-16 03:38:40','2017-06-16 03:38:40');
/*!40000 ALTER TABLE `phase_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice`
--

LOCK TABLES `voice` WRITE;
/*!40000 ALTER TABLE `voice` DISABLE KEYS */;
INSERT INTO `voice` VALUES (2,2,'percussive','Drums','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,2,'harmonic','Bass','2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,3,'percussive','Kick+Snare','2017-04-23 23:45:07','2017-06-03 00:03:54'),(5,3,'percussive','Locomotion','2017-06-03 00:04:07','2017-06-03 00:04:07'),(6,10,'percussive','Toms Congas','2017-06-05 05:20:24','2017-06-05 05:20:24'),(8,3,'percussive','Toms+Congas+Misc','2017-06-11 19:50:10','2017-06-16 02:17:56'),(9,3,'percussive','Cymbal','2017-06-16 02:18:08','2017-06-16 02:18:08'),(10,3,'percussive','Vocal','2017-06-23 23:43:10','2017-06-23 23:43:10'),(11,3,'percussive','Vocal Echo','2017-06-24 01:29:49','2017-06-24 01:29:49');
/*!40000 ALTER TABLE `voice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `voice_event`
--

LOCK TABLES `voice_event` WRITE;
/*!40000 ALTER TABLE `voice_event` DISABLE KEYS */;
INSERT INTO `voice_event` VALUES (268,4,1,1,'KICKLONG',0,1,'C2','2017-06-02 23:57:19','2017-06-20 18:30:42'),(270,4,1,1,'KICKLONG',2.5,0.5,'C2','2017-06-02 23:57:53','2017-06-20 18:30:47'),(274,4,1,0.2,'SNARE',1,1,'G8','2017-06-02 23:58:37','2017-06-10 19:32:41'),(275,5,0.2,0.1,'HIHATCLOSED',0,0.3,'E12','2017-06-03 00:09:06','2017-06-16 00:09:28'),(276,5,0.16,0.1,'HIHATCLOSED',1,0.25,'E12','2017-06-03 00:10:09','2017-06-16 00:10:12'),(277,5,0.2,0.1,'HIHATCLOSED',2,0.25,'E12','2017-06-03 00:10:14','2017-06-16 00:11:24'),(278,5,0.2,0.1,'HIHATCLOSED',3,0.25,'E12','2017-06-03 00:10:19','2017-06-16 00:12:20'),(280,5,0.1,0.1,'HIHATCLOSED',0.3,0.2,'G12','2017-06-03 00:11:48','2017-06-16 00:09:35'),(281,5,0.1,0.1,'HIHATCLOSED',1.3,0.2,'G12','2017-06-03 00:11:52','2017-06-16 00:10:18'),(282,5,0.05,0.1,'HIHATOPEN',2.5,0.25,'G12','2017-06-03 00:11:57','2017-06-16 00:11:41'),(283,5,0.1,0.1,'HIHATCLOSED',3.3,0.2,'G12','2017-06-03 00:12:02','2017-06-16 00:12:26'),(284,5,0.12,0.06,'HIHATCLOSED',3.8,0.2,'D12','2017-06-03 00:12:32','2017-06-16 00:13:09'),(285,5,0.12,0.06,'HIHATCLOSED',2.8,0.2,'D12','2017-06-03 00:12:37','2017-06-16 00:12:07'),(286,5,0.12,0.06,'HIHATCLOSED',1.8,0.2,'D12','2017-06-03 00:12:41','2017-06-16 00:11:18'),(287,5,0.12,0.06,'HIHATCLOSED',0.8,0.2,'D12','2017-06-03 00:12:46','2017-06-16 00:09:57'),(288,5,0.05,0.12,'HIHATOPEN',0.5,0.25,'E8','2017-06-03 00:13:33','2017-06-16 00:09:49'),(290,5,0.1,0.12,'HIHATCLOSED',2.3,0.2,'E8','2017-06-03 00:13:43','2017-06-16 00:11:29'),(291,5,0.05,0.12,'HIHATOPEN',3.5,0.5,'E8','2017-06-03 00:13:47','2017-06-16 00:12:53'),(292,4,0.3,0.1,'CLAP',3,1,'Bb','2017-06-04 04:23:00','2017-06-15 22:50:44'),(294,4,0.5,1,'KICK',2.3,0.2,'F#2','2017-06-04 04:26:37','2017-06-15 22:21:32'),(295,4,0.1,0.1,'CLAP',1,1,'G5','2017-06-04 04:39:36','2017-06-04 04:39:36'),(301,5,0.05,0.12,'HIHATOPEN',1.5,0.25,'E8','2017-06-04 04:49:14','2017-06-16 00:11:02'),(302,5,0.2,0.1,'MARACAS',0.5,0.5,'Bb8','2017-06-04 04:51:11','2017-06-16 00:10:47'),(303,5,0.2,0.1,'MARACAS',1.5,0.5,'Bb8','2017-06-04 04:51:23','2017-06-16 00:10:53'),(304,5,0.2,0.1,'MARACAS',2.5,0.5,'Bb8','2017-06-04 04:51:28','2017-06-16 00:11:52'),(305,5,0.2,0.1,'MARACAS',3.5,0.5,'Bb8','2017-06-04 04:51:34','2017-06-16 00:13:03'),(306,5,0.1,0.1,'MARACAS',3.3,0.5,'Bb8','2017-06-04 04:51:43','2017-06-16 00:12:34'),(307,6,0.2,1,'CONGAHIGH',0,0.8,'C4','2017-06-05 05:21:33','2017-06-05 05:21:33'),(308,6,0.2,1,'CONGA',0.8,0.7,'C8','2017-06-05 05:21:57','2017-06-05 05:21:57'),(309,6,0.2,1,'TOMHIGH',1.5,0.5,'C8','2017-06-05 05:22:28','2017-06-05 05:22:28'),(310,6,0.25,1,'TOMHIGH',2,0.5,'C8','2017-06-05 05:22:51','2017-06-05 05:22:51'),(311,6,0.18,1,'TOM',2.5,1,'C8','2017-06-05 05:23:28','2017-06-05 05:23:28'),(314,8,0.2,1,'TOM',0.5,0.75,'C6','2017-06-11 19:51:53','2017-06-11 20:15:13'),(315,8,0.2,1,'TOM',1.3,0.7,'G5','2017-06-11 19:52:17','2017-06-11 20:17:37'),(316,8,0.2,1,'TOM',2,1,'C5','2017-06-11 19:53:15','2017-06-11 20:15:27'),(318,8,0.2,1,'CONGA',0,1,'F5','2017-06-11 20:15:46','2017-06-11 20:15:46'),(320,8,0.2,1,'TOM',3.5,0.5,'G3','2017-06-11 20:16:54','2017-06-15 22:57:51'),(322,4,0.62,0.2,'SNARE',1.8,0.2,'G5','2017-06-12 19:14:16','2017-06-20 18:31:04'),(323,8,0.05,0.5,'COWBELL',2,1,'F5','2017-06-12 19:20:22','2017-06-15 22:51:48'),(325,4,0.2,0.1,'SNARERIM',2.5,0.5,'G5','2017-06-12 19:31:37','2017-06-12 19:31:55'),(327,4,0.1,0.1,'SNARERIM',3.8,0.2,'G6','2017-06-12 19:32:50','2017-06-12 19:32:50'),(328,4,0.14,1,'KICK',3.8,0.2,'C2','2017-06-12 19:33:17','2017-06-12 19:33:17'),(329,8,0.3,1,'CONGAHIGH',3.3,0.6,'F5','2017-06-15 22:52:40','2017-06-15 22:58:06'),(330,8,0.5,1,'CONGA',3.8,0.2,'F5','2017-06-15 22:52:52','2017-06-15 22:52:52'),(331,9,0.06,0,'CYMBALCRASH',0,4,'C5','2017-06-16 02:18:26','2017-06-20 18:35:03'),(332,10,0.15,0.3,'HEY',0.5,2.5,'X','2017-06-23 23:44:29','2017-06-24 01:46:20'),(333,11,0.025,0.3,'HEY',2.53,1.47,'X','2017-06-24 01:30:13','2017-06-24 01:36:30');
/*!40000 ALTER TABLE `voice_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument`
--

LOCK TABLES `instrument` WRITE;
/*!40000 ALTER TABLE `instrument` DISABLE KEYS */;
INSERT INTO `instrument` VALUES (1,1,2,'percussive','Roland TR-808 Drum Machine',0.8,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(2,1,2,'percussive','909 drum machine',0.75,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(3,1,1,'percussive','Electronic',0.8,'2017-04-21 16:33:55','2017-06-16 02:19:40'),(4,1,1,'percussive','Acoustic',0.5,'2017-06-15 22:32:29','2017-06-15 22:32:29'),(5,1,1,'percussive','Wood Spoon v. Pots & Pans',0.76,'2017-06-20 23:02:25','2017-06-20 23:03:41');
/*!40000 ALTER TABLE `instrument` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `instrument_meme`
--

LOCK TABLES `instrument_meme` WRITE;
/*!40000 ALTER TABLE `instrument_meme` DISABLE KEYS */;
INSERT INTO `instrument_meme` VALUES (1,1,'Drum','2017-02-10 00:03:23','2017-02-10 00:03:23'),(2,3,'Classic','2017-04-23 23:13:29','2017-04-23 23:13:29'),(3,3,'Deep','2017-04-23 23:13:33','2017-04-23 23:13:33'),(4,3,'Acid','2017-04-23 23:13:36','2017-04-23 23:13:36'),(6,3,'Tech','2017-04-23 23:13:41','2017-04-23 23:13:41'),(7,3,'Electro','2017-04-23 23:13:43','2017-04-23 23:13:43'),(10,3,'Cool','2017-04-23 23:20:57','2017-04-23 23:20:57'),(11,3,'Hard','2017-04-23 23:20:59','2017-04-23 23:20:59'),(13,3,'Progressive','2017-04-23 23:23:43','2017-04-23 23:23:43'),(14,4,'Classic','2017-06-15 22:59:20','2017-06-15 22:59:20'),(16,4,'Tropical','2017-06-15 22:59:32','2017-06-15 22:59:32'),(17,4,'Hot','2017-06-15 22:59:35','2017-06-15 22:59:35'),(19,4,'Easy','2017-06-15 22:59:43','2017-06-15 22:59:43'),(20,4,'Progressive','2017-06-15 22:59:46','2017-06-15 22:59:46'),(21,5,'Classic','2017-06-21 01:25:37','2017-06-21 01:25:37'),(22,5,'Deep','2017-06-21 01:25:41','2017-06-21 01:25:41'),(23,5,'Hard','2017-06-21 01:25:58','2017-06-21 01:25:58'),(27,4,'Deep','2017-06-21 01:40:43','2017-06-21 01:40:43'),(28,4,'Hard','2017-06-21 01:40:56','2017-06-21 01:40:56'),(30,5,'Hot','2017-06-24 01:38:58','2017-06-24 01:38:58'),(31,5,'Cool','2017-06-24 01:39:02','2017-06-24 01:39:02');
/*!40000 ALTER TABLE `instrument_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio`
--

LOCK TABLES `audio` WRITE;
/*!40000 ALTER TABLE `audio` DISABLE KEYS */;
INSERT INTO `audio` VALUES (2,3,'Kick','instrument-3-audio-80454e35-5693-4b42-aa6a-218383a9f584.wav',0,0.702,120,57.495,'2017-04-21 16:41:03','2017-06-15 22:23:13','Published'),(3,3,'Kick Long','instrument-3-audio-ed1957b9-eea0-42f8-8493-b8874e1a6bf9.wav',0,0.865,120,57.05,'2017-04-21 18:52:17','2017-06-15 22:23:01','Published'),(4,3,'Hihat Closed','instrument-3-audio-0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14.wav',0,0.053,120,6300,'2017-04-21 19:33:05','2017-06-15 22:22:51','Published'),(5,3,'Maracas','instrument-3-audio-ffe4edd6-5b83-4ac9-8e69-156ddb06762f.wav',0,0.026,120,190.086,'2017-04-21 19:38:16','2017-06-15 22:22:41','Published'),(6,3,'Snare','instrument-3-audio-7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe.wav',0,0.093,120,177.823,'2017-04-21 19:42:59','2017-06-15 22:22:31','Published'),(7,3,'Tom','instrument-3-audio-a6bf0d86-6b45-4cf1-b404-2242095c7876.wav',0,0.36,120,104.751,'2017-04-21 19:43:58','2017-06-04 21:28:24','Published'),(8,3,'Claves','instrument-3-audio-aea2483c-7707-4100-aa86-b680668cd1a0.wav',0,0.03,120,2594,'2017-04-23 23:59:47','2017-06-04 21:28:24','Published'),(9,3,'Conga','instrument-3-audio-f772f19f-b51b-414e-9dc8-8ceb23faa779.wav',0,0.26,120,213,'2017-04-24 00:03:32','2017-06-04 21:28:24','Published'),(10,3,'Conga High','instrument-3-audio-c0975d3a-4f26-44b2-a9d3-800320bfa3e1.wav',0,0.179,120,397.297,'2017-04-24 00:05:34','2017-06-04 21:28:24','Published'),(11,3,'Tom High','instrument-3-audio-aea1351b-bb96-4487-8feb-ae8ad3e499ad.wav',0,0.2,120,190.909,'2017-04-24 02:18:29','2017-06-04 21:28:24','Published'),(12,3,'Clap','instrument-3-audio-ce0662a2-3f7e-425b-8105-fb639d395235.wav',0,0.361,120,1102.5,'2017-04-24 02:21:04','2017-06-10 19:26:40','Published'),(13,3,'Cowbell','instrument-3-audio-aaa877a8-0c89-4781-93f8-69c722285b2a.wav',0,0.34,120,268.902,'2017-04-24 02:22:47','2017-06-04 21:28:24','Published'),(14,3,'Cymbal Crash','instrument-3-audio-37a35a63-23e4-4ef6-a78e-db2577aa9a00.wav',0,2.229,120,109.701,'2017-04-24 02:24:03','2017-06-04 21:28:24','Published'),(15,3,'Hihat Open','instrument-3-audio-020ad575-af86-4fe2-a869-957d50d59ac4.wav',0,0.598,120,7350,'2017-04-24 02:25:31','2017-06-10 19:25:45','Published'),(16,3,'Snare Rim','instrument-3-audio-58fd7eae-b55e-4567-9c27-ead64b83488a.wav',0,0.014,120,445.445,'2017-04-24 02:26:53','2017-06-10 19:27:16','Published'),(22,4,'Hihat Closed 9','instrument-4-audio-0f28ef83-2213-4bbb-ae68-3eecc201ead3.wav',0,0.849,120,648.529,'2017-06-15 22:43:24','2017-06-15 22:43:24','Published'),(23,4,'Hihat Closed 7','instrument-4-audio-e15dc427-b556-4a72-bec8-6b59c6d8bbc8.wav',0.003,0.962,120,8820,'2017-06-15 22:44:55','2017-06-15 22:44:55','Published'),(24,4,'Hihat Closed 8','instrument-4-audio-cb1ffbff-c31d-4e06-9d84-649c1f257a24.wav',0,0.905,120,648.529,'2017-06-15 22:45:52','2017-06-15 22:45:52','Published'),(26,4,'Snare Rim','instrument-4-audio-7b2d94b3-c218-498b-906e-11c313054cd1.wav',0,1.147,120,239.674,'2017-06-15 22:56:58','2017-06-15 22:56:58','Published'),(27,4,'Hihat Open 5','instrument-4-audio-bf2c9ad8-ceb4-4c7e-98ae-a9c561680a1f.wav',0.003,1.115,120,648.529,'2017-06-15 23:04:16','2017-06-15 23:04:16','Published'),(28,4,'Hihat Open 7','instrument-4-audio-4c3c5673-e8f1-4452-ad8c-5466cce0492d.wav',0,2,120,648.529,'2017-06-15 23:06:14','2017-06-15 23:06:14','Published'),(29,4,'Hihat Open 6','instrument-4-audio-9a57a402-98e9-4ceb-86c2-ea60607b56d1.wav',0,0.809,120,648.529,'2017-06-15 23:07:41','2017-06-15 23:07:41','Published'),(30,4,'Stick Side 7','instrument-4-audio-ea042c27-551b-44c7-998b-1df185d319cf.wav',0.003,0.159,120,436.634,'2017-06-15 23:11:51','2017-06-15 23:23:04','Published'),(31,4,'Stick Side 6','instrument-4-audio-0d65a838-e76f-407d-a06b-6485d67ba44c.wav',0,0.335,120,2321.05,'2017-06-15 23:13:00','2017-06-15 23:13:00','Published'),(32,4,'Stick Side 5','instrument-4-audio-99f7dbea-c1fb-419e-ad44-c90804516aa3.wav',0,0.248,120,1837.5,'2017-06-15 23:14:30','2017-06-15 23:14:30','Published'),(33,4,'Snare Rim 7','instrument-4-audio-12e36076-5944-4101-a41b-b39136cf78a4.wav',0,0.461,120,254.913,'2017-06-15 23:15:43','2017-06-15 23:15:43','Published'),(34,4,'Snare Rim 6','instrument-4-audio-5a840f38-7623-442b-b9a9-a0ff1927c7a0.wav',0,0.527,120,245,'2017-06-15 23:16:36','2017-06-15 23:16:36','Published'),(35,4,'Snare Rim 5','instrument-4-audio-d404857a-6bf8-43c4-ad76-5259945d16fe.wav',0,0.463,120,181.481,'2017-06-15 23:17:44','2017-06-15 23:17:44','Published'),(36,4,'Tom High','instrument-4-audio-4888db8b-1c81-4178-8af5-332ae7067ca8.wav',0.002,0.42,120,187.66,'2017-06-15 23:20:38','2017-06-15 23:23:15','Published'),(37,4,'Snare 3','instrument-4-audio-d373a2f8-8c8f-4afa-b7e3-c21623d15f42.wav',0.008,0.404,120,2450,'2017-06-15 23:21:50','2017-06-15 23:21:50','Published'),(38,4,'Tom','instrument-4-audio-d5bcc3a5-d98f-434f-8fcb-987f1913a684.wav',0.009,0.445,120,225,'2017-06-15 23:22:45','2017-06-15 23:22:45','Published'),(39,4,'Conga High','instrument-4-audio-511f5a68-1eca-4ca3-9713-956a219d734c.wav',0.002,0.425,120,187.66,'2017-06-15 23:24:15','2017-06-15 23:24:15','Published'),(40,4,'Conga','instrument-4-audio-2059cab7-8052-46cf-8fd1-2930cfe5ce59.wav',0.001,0.547,120,183.231,'2017-06-15 23:25:03','2017-06-15 23:25:03','Published'),(41,4,'Snare 5','instrument-4-audio-cce1763b-fca3-49c5-9024-c665c1fea7f3.wav',0.008,0.407,120,180.738,'2017-06-15 23:25:58','2017-06-15 23:25:58','Published'),(42,4,'Snare 4','instrument-4-audio-511168e1-3291-4ec8-a6ac-652249206287.wav',0.008,0.439,120,204.167,'2017-06-15 23:27:04','2017-06-15 23:27:04','Published'),(43,4,'Kick 7','instrument-4-audio-2fd75fb8-b968-46ba-8c43-ac6ad2db9a80.wav',0.008,0.537,120,43.534,'2017-06-16 00:20:54','2017-06-16 00:27:34','Published'),(44,4,'Kick 3','instrument-4-audio-3a79549f-cf7b-4338-8756-f75b3fc5deaa.wav',0.005,0.742,120,52.128,'2017-06-16 00:24:47','2017-06-16 00:24:47','Published'),(45,4,'Kick 3','instrument-4-audio-c076a674-1626-4b22-bc07-a639ca90b363.wav',0.01,0.677,120,56.178,'2017-06-16 00:27:07','2017-06-16 00:27:07','Published'),(48,4,'Tom Low 5','instrument-4-audio-246190da-65fd-41a9-a943-2c8e3b763fa5.wav',0,0.73,120,84.483,'2017-06-16 00:33:57','2017-06-16 00:33:57','Published'),(49,4,'Tom 5','instrument-4-audio-bf45a337-c86a-4c44-9663-06093d3ca9ba.wav',0,0.59,120,90.928,'2017-06-16 00:35:25','2017-06-16 00:35:25','Published'),(50,4,'Tom High 5','instrument-4-audio-83294480-eef2-4171-8d69-8f16092557df.wav',0.003,0.444,120,126,'2017-06-16 00:36:37','2017-06-16 00:36:37','Published'),(51,4,'Kick Long 2','instrument-4-audio-b12bf5ff-ebec-47e3-9259-6cd0c9f57724.wav',0.01,1.476,120,59.036,'2017-06-16 00:39:02','2017-06-16 00:39:02','Published'),(54,4,'Clap 1','instrument-4-audio-27b08205-9921-4d48-bc54-ba4110fe238f.wav',0,0.572,120,185.294,'2017-06-16 02:15:47','2017-06-16 02:15:47','Published'),(55,4,'Clap 2','instrument-4-audio-81f55d83-39fe-4832-99bf-4e4f3af69496.wav',0,0.684,120,188.462,'2017-06-16 02:17:11','2017-06-16 02:17:11','Published'),(56,3,'Kick 2','instrument-3-audio-a731fc44-5ae0-4e9f-a728-edfe1895da4b.wav',0,0.34,120,69.122,'2017-06-16 03:01:06','2017-06-16 03:01:06','Published'),(57,3,'Kick Long 2','instrument-3-audio-84b1974c-02b0-406f-b78e-21414282986e.wav',0,1.963,120,60.494,'2017-06-16 03:04:09','2017-06-16 03:04:09','Published'),(58,3,'Tom High 2','instrument-3-audio-618bc8e5-f51f-4635-895c-5bd6522f8d8c.wav',0.002,0.411,120,201.37,'2017-06-16 03:06:30','2017-06-16 03:06:30','Published'),(59,3,'Tom Low 2','instrument-3-audio-014c8939-c9e7-4911-9620-9c4075a3b4a2.wav',0,0.701,120,111.646,'2017-06-16 03:07:20','2017-06-16 03:07:20','Published'),(60,3,'Tom 2','instrument-3-audio-3fcb76bf-6168-4aef-a160-facd1bb18071.wav',0,0.488,120,149.492,'2017-06-16 03:09:50','2017-06-16 03:09:50','Published'),(61,3,'Clap 2','instrument-3-audio-9a3e9e07-b1dd-44a5-9399-3b6c11bd72b1.wav',0.002,0.356,120,1225,'2017-06-16 03:13:28','2017-06-16 03:13:28','Published'),(62,3,'Clap 3','instrument-3-audio-f24484dd-b879-42c5-9c2a-71857555c319.wav',0,0.734,120,980,'2017-06-16 03:14:41','2017-06-16 03:14:41','Published'),(63,3,'Maracas 2','instrument-3-audio-f20dcce7-a936-446c-8692-c8caf37d8896.wav',0.009,0.43,120,11025,'2017-06-16 03:17:11','2017-06-16 03:17:11','Published'),(64,4,'Cowbell','instrument-4-audio-392a388d-8e32-46f9-ad57-b3bd29929262.wav',0.002,0.298,120,525,'2017-06-16 03:20:04','2017-06-16 03:20:04','Published'),(65,4,'Cymbal Crash 1','instrument-4-audio-378df92f-aec2-4a5c-9243-d08384971761.wav',0.018,1.878,120,1297.06,'2017-06-16 03:21:46','2017-06-16 03:21:46','Published'),(66,4,'Cymbal Crash 2','instrument-4-audio-b921f58d-1ce0-4c1e-82d0-08479c25bfff.wav',0.01,3.241,120,469.149,'2017-06-16 03:24:03','2017-06-16 03:24:03','Published'),(67,4,'Cymbal Crash 3','instrument-4-audio-484d5dc0-4627-477d-8de7-f4c30cc4f538.wav',0.01,3.044,120,181.481,'2017-06-16 03:25:34','2017-06-16 03:25:34','Published'),(68,3,'Cymbal Crash 2','instrument-3-audio-bb3e2a48-8f59-4ad0-a05f-30aca579524f.wav',0,2,120,816.667,'2017-06-16 03:28:35','2017-06-16 03:29:18','Published'),(69,5,'A_3','instrument-5-audio-86d61872-a9bf-4b68-b4df-397be09bfe5c.wav',0.007,1.051,120,3428.57,'2017-06-20 23:11:42','2017-06-20 23:11:42','Published'),(70,5,'A_4','instrument-5-audio-92f61e58-7225-48bb-91f3-b71fcf7aef5a.wav',0,0.623,120,888.889,'2017-06-20 23:17:39','2017-06-20 23:17:39','Published'),(71,5,'A_5','instrument-5-audio-8a536dae-3727-488f-8895-a0b047620a38.wav',0.001,0.537,120,888.889,'2017-06-20 23:19:00','2017-06-20 23:19:00','Published'),(72,5,'A_6','instrument-5-audio-e173c291-60d6-4f9a-a422-d2d8c99bd9b3.wav',0.003,0.425,120,3428.57,'2017-06-20 23:35:06','2017-06-20 23:35:06','Published'),(73,5,'A_7','instrument-5-audio-de082694-4a02-48a4-92d1-83c2d2b7dd92.wav',0.001,0.6,120,1263.16,'2017-06-20 23:36:34','2017-06-20 23:36:34','Published'),(74,5,'A_8','instrument-5-audio-7cbe09b2-5fe6-4d7a-b5fa-2f85624e91f5.wav',0,0.73,120,1200,'2017-06-20 23:37:43','2017-06-20 23:37:43','Published'),(75,5,'A_9','instrument-5-audio-96df8da4-5be9-4a0f-a97b-5f8c0d28f161.wav',0,0.432,120,1454.55,'2017-06-20 23:38:52','2017-06-20 23:38:52','Published'),(76,5,'A_10','instrument-5-audio-e4a06acb-c375-4e9b-a5ce-153b815fe6cb.wav',0.002,0.307,120,3000,'2017-06-20 23:40:36','2017-06-20 23:40:36','Published'),(77,5,'F_1','instrument-5-audio-4eb40925-8e37-4801-ba2e-cce991c97093.wav',0,0.969,120,428.155,'2017-06-20 23:45:30','2017-06-20 23:45:30','Published'),(78,5,'F_2','instrument-5-audio-13db8e43-4266-444a-9edd-c5a5cb2442b4.wav',0,1.506,120,182.988,'2017-06-20 23:46:32','2017-06-20 23:46:32','Published'),(79,5,'F_3','instrument-5-audio-3f0dbe3a-d11a-4e9f-a642-befe5747dd01.wav',0,2.567,120,183.75,'2017-06-20 23:47:22','2017-06-20 23:47:22','Published'),(80,5,'F_4','instrument-5-audio-57ff6b97-fedb-4e3f-b963-840ba8fd101b.wav',0.035,2.617,120,416.038,'2017-06-20 23:48:41','2017-06-20 23:48:41','Published'),(81,5,'F_5','instrument-5-audio-70c7404e-1f17-4a32-8f4a-ff28e7d5797c.wav',0,2.734,120,420,'2017-06-20 23:49:48','2017-06-20 23:49:48','Published'),(82,5,'F_6','instrument-5-audio-ed5b3f4c-a6e3-424b-b8ba-34c317640903.wav',0,1.348,120,432.353,'2017-06-20 23:50:30','2017-06-20 23:50:30','Published'),(83,5,'F_7','instrument-5-audio-8d7c72dc-92bb-4ffa-82ff-13750c8ddbfc.wav',0,2.264,120,183.75,'2017-06-20 23:51:23','2017-06-20 23:51:23','Published'),(84,5,'F_8','instrument-5-audio-7eae03f7-d1aa-42e2-a928-ff6f7b00b25d.wav',0,2.595,120,182.988,'2017-06-20 23:51:59','2017-06-20 23:51:59','Published'),(85,5,'H_1','instrument-5-audio-2f4bf7a2-744e-47cc-b5c2-da0a846cab91.wav',0,1.008,120,1422.58,'2017-06-20 23:54:11','2017-06-20 23:54:11','Published'),(86,5,'H_2','instrument-5-audio-2c2d8ba8-911b-4480-a774-c37102c12e90.wav',0.009,2.036,120,1378.12,'2017-06-20 23:56:11','2017-06-20 23:56:11','Published'),(87,5,'H_8','instrument-5-audio-0e5c97c1-ad2a-4cb5-a1f5-10224c7cec3c.wav',0,2.698,120,1633.33,'2017-06-20 23:58:39','2017-06-20 23:58:39','Published'),(88,5,'H_7','instrument-5-audio-2a525acb-dc9a-47f4-b105-89dc3332d78b.wav',0,1.738,120,1764,'2017-06-20 23:59:37','2017-06-20 23:59:37','Published'),(89,5,'H_6','instrument-5-audio-91f5c7de-609d-48fd-a527-c7b132ee2af5.wav',0,2.984,120,1336.36,'2017-06-21 00:00:19','2017-06-21 00:00:19','Published'),(90,5,'H_5','instrument-5-audio-c18a2f87-df5f-421a-aa59-89fda817210c.wav',0,3.133,120,189.27,'2017-06-21 00:01:06','2017-06-21 00:01:06','Published'),(91,5,'H_4','instrument-5-audio-ee21d28c-6102-4ad7-96a5-49cf5ccaf266.wav',0,2.815,120,186.076,'2017-06-21 00:01:52','2017-06-21 00:01:52','Published'),(92,5,'H_3','instrument-5-audio-8494ac91-a1ef-4045-9f1f-3a1b4a53ee3d.wav',0,2.346,120,1378.12,'2017-06-21 00:02:27','2017-06-21 00:02:27','Published'),(93,5,'Q_1','instrument-5-audio-21369f18-b2b6-4d8b-bd28-de36f294b67e.wav',0,1.206,120,5512.5,'2017-06-21 00:13:47','2017-06-21 00:13:47','Published'),(94,5,'Q_11','instrument-5-audio-88ba75c5-9727-43a3-9ef0-856abe729f78.wav',0,1.524,120,6300,'2017-06-21 00:14:33','2017-06-21 00:14:33','Published'),(95,5,'Q_10','instrument-5-audio-b14d6a26-1e35-4f7c-bbfb-6fd262c2d35f.wav',0,1.631,120,1378.12,'2017-06-21 00:15:27','2017-06-21 00:15:27','Published'),(96,5,'Q_9','instrument-5-audio-0818bf78-3838-43a5-8665-7f8f2814bfc4.wav',0.003,0.583,120,249.153,'2017-06-21 00:49:26','2017-06-21 00:49:26','Published'),(97,5,'Q_8','instrument-5-audio-725e8281-c845-4a87-9a37-9117b1e6a830.wav',0.002,0.799,120,355.645,'2017-06-21 00:51:02','2017-06-21 00:51:02','Published'),(98,5,'Q_7','instrument-5-audio-7fd96254-d9cf-4ad6-9899-dee564543853.wav',0.001,0.653,120,5512.5,'2017-06-21 00:52:59','2017-06-21 00:52:59','Published'),(99,5,'Q_6','instrument-5-audio-83fbed4b-648c-4886-9079-f220fb0dc9fb.wav',0.001,0.659,120,134.451,'2017-06-21 00:54:25','2017-06-21 00:54:25','Published'),(100,5,'Q_5','instrument-5-audio-62536d52-8600-4941-ac04-a72106079610.wav',0.002,0.405,120,1025.58,'2017-06-21 00:55:25','2017-06-21 00:55:25','Published'),(101,5,'Q_4','instrument-5-audio-8e17510c-a877-42a6-addc-95ef7d559757.wav',0.001,1.257,120,5512.5,'2017-06-21 00:56:51','2017-06-21 00:56:51','Published'),(102,5,'Q_3','instrument-5-audio-a448d6b9-4669-4f17-883a-8dd8c5ce0b8e.wav',0,0.915,120,5512.5,'2017-06-21 00:58:15','2017-06-21 00:58:15','Published'),(103,5,'Q_2','instrument-5-audio-23d5847f-56e6-4b79-99ad-6dfd13b9c5b3.wav',0.001,1.008,120,6300,'2017-06-21 00:59:33','2017-06-21 00:59:33','Published'),(104,5,'M_8','instrument-5-audio-1c5f4752-e790-47a0-b0d9-4eedd54b24a5.wav',0,0.407,120,531.325,'2017-06-21 01:04:11','2017-06-21 01:04:11','Published'),(105,5,'L_1','instrument-5-audio-568d1c74-a43e-44fc-ab53-0d1d701f6f0f.wav',0,0.851,120,364.463,'2017-06-21 01:05:28','2017-06-21 01:05:28','Published'),(106,5,'M_9','instrument-5-audio-2d2d76f7-9d76-41c6-9e55-0b94703d487c.wav',0,0.407,120,531.325,'2017-06-21 01:06:20','2017-06-21 01:06:20','Published'),(107,5,'M_7','instrument-5-audio-02dde877-01b4-432d-8d22-f1458917154b.wav',0.001,0.502,120,420,'2017-06-21 01:07:04','2017-06-21 01:07:04','Published'),(108,5,'M_6','instrument-5-audio-3bdc44e7-e464-4a0f-a080-ab3d529ac9dc.wav',0.001,0.512,120,612.5,'2017-06-21 01:07:52','2017-06-21 01:07:52','Published'),(109,5,'M_5','instrument-5-audio-0e47652d-265b-4c83-8c4f-c14a34fc9689.wav',0,0.466,120,612.5,'2017-06-21 01:08:48','2017-06-21 01:08:48','Published'),(110,5,'M_4','instrument-5-audio-f6e912f5-d582-4044-b73b-6e004bb32a15.wav',0,0.6,120,612.5,'2017-06-21 01:09:30','2017-06-21 01:09:30','Published'),(111,5,'M_3','instrument-5-audio-710b3011-cb1e-4065-a514-1e6e4fd19bec.wav',0,0.427,120,612.5,'2017-06-21 01:10:06','2017-06-21 01:10:06','Published'),(112,5,'M_3','instrument-5-audio-c8d1affb-9b7c-4661-bf31-cd80dc2a9ce1.wav',0,0.602,120,588,'2017-06-21 01:10:58','2017-06-21 01:10:58','Published'),(113,5,'M_1','instrument-5-audio-983fc7a1-a1ef-466f-be44-cc1e227ae449.wav',0,0.318,120,565.385,'2017-06-21 01:11:49','2017-06-21 01:11:49','Published'),(114,5,'M_1','instrument-5-audio-faf2e9c6-6b12-445e-9b2c-93966451ff5e.wav',0,0.318,120,565.385,'2017-06-21 01:12:52','2017-06-21 01:12:52','Published'),(115,5,'L_10','instrument-5-audio-38c92218-882d-4714-a493-14261e07c4fa.wav',0,0.741,120,302.055,'2017-06-21 01:13:30','2017-06-21 01:13:30','Published'),(116,5,'L_9','instrument-5-audio-50f516a9-faaa-4091-848d-651d96ecc7be.wav',0,0.751,120,176.4,'2017-06-21 01:14:17','2017-06-21 01:14:17','Published'),(117,5,'L_8','instrument-5-audio-f1bac880-fede-4c5d-9249-956f5e179d62.wav',0,0.835,120,290.132,'2017-06-21 01:15:03','2017-06-21 01:15:03','Published'),(119,5,'L_7','instrument-5-audio-b51678cb-50a0-4994-980a-62bf126ca445.wav',0.001,0.674,120,531.325,'2017-06-21 01:19:44','2017-06-21 01:19:44','Published'),(120,5,'L_6','instrument-5-audio-01e988c0-3821-4ba2-8223-70643f3c27cf.wav',0,0.736,120,408.333,'2017-06-21 01:20:53','2017-06-21 01:20:53','Published'),(121,5,'L_5','instrument-5-audio-f6f79c74-f1e0-459b-9728-46f59bd14ee7.wav',0.001,0.608,120,428.155,'2017-06-21 01:21:54','2017-06-21 01:21:54','Published'),(122,5,'L_4','instrument-5-audio-2b9af025-2616-4d03-890f-b74df3413abe.wav',0,0.592,120,11025,'2017-06-21 01:22:35','2017-06-21 01:22:35','Published'),(123,5,'L_3','instrument-5-audio-dd32a686-ef3a-43c4-a3e1-13353d067026.wav',0,0.624,120,110.526,'2017-06-21 01:23:21','2017-06-21 01:23:21','Published'),(124,5,'L_2','instrument-5-audio-6ffdef87-909f-4b67-a2f7-fadbb3a76e33.wav',0,0.528,120,257.895,'2017-06-21 01:23:58','2017-06-21 01:23:58','Published'),(125,3,'Vocal How','instrument-3-audio-a6e95ccc-17a4-48a2-a4d5-ffea0767830e.wav',0.074,0.454,120,284.516,'2017-06-23 23:52:36','2017-06-23 23:52:36','Published'),(126,3,'Vocal Hie','instrument-3-audio-0248ed87-19e8-449c-9211-4722d6ab8342.wav',0.08,0.477,120,364.463,'2017-06-23 23:53:49','2017-06-23 23:53:49','Published'),(127,3,'Vocal Ahh','instrument-3-audio-d35678fa-f163-433d-8741-250a530b5532.wav',0.012,1.037,120,948.696,'2017-06-23 23:55:53','2017-06-23 23:55:53','Published'),(128,3,'Vocal Hoo','instrument-3-audio-54d3503d-af44-4480-a0d0-8044fb403c5a.wav',0.079,0.45,120,205.116,'2017-06-23 23:57:01','2017-06-23 23:57:01','Published'),(129,3,'Vocal Haa','instrument-3-audio-79b9c4f4-037a-4f6f-bc51-7a7a2dff5528.wav',0.053,0.36,120,864.706,'2017-06-23 23:57:45','2017-06-23 23:57:45','Published'),(132,3,'Vocal Eow','instrument-3-audio-0e2d5fb2-9d40-4741-9da8-bc9943722d66.wav',0.045,0.486,120,383.478,'2017-06-24 00:00:25','2017-06-24 00:00:25','Published'),(133,3,'Vocal Grunt Ooh 2','instrument-3-audio-8896e8d4-0c31-4dd8-93ff-6982a30febdb.wav',0.015,0.247,120,404.587,'2017-06-24 00:10:49','2017-06-24 00:10:49','Published'),(134,3,'Vocal Grunt Ooh','instrument-3-audio-ef489ad1-fb9d-4e77-9b5c-a7b3570c8c09.wav',0.011,0.213,120,1696.15,'2017-06-24 00:11:31','2017-06-24 00:11:31','Published'),(135,4,'Vocal JB Get','instrument-4-audio-e5e8a85b-1c3c-46b5-8394-3b44b5c7e6e1.wav',0.027,0.311,120,386.842,'2017-06-24 00:13:30','2017-06-24 00:13:30','Published'),(136,4,'Vocal JB Baz','instrument-4-audio-76a3e02c-979c-4d64-9bab-3b1a91d3635d.wav',0.018,0.405,120,918.75,'2017-06-24 00:14:55','2017-06-24 00:14:55','Published'),(137,4,'Vocal JB Get 2','instrument-4-audio-22efe6d1-3dea-45a5-906c-1e4bd4465606.wav',0.027,0.29,120,386.842,'2017-06-24 00:16:15','2017-06-24 00:16:15','Published'),(138,4,'Vocal JB Baz2','instrument-4-audio-94bd651e-ce98-4b09-95b8-6e36819e2721.wav',0.032,0.29,120,367.5,'2017-06-24 00:17:37','2017-06-24 00:17:37','Published'),(139,4,'Vocal JB Uhh','instrument-4-audio-3bc65d7a-00a0-42cc-9d15-292f9fbe98ee.wav',0,0.408,120,474.194,'2017-06-24 00:20:34','2017-06-24 00:20:34','Published'),(140,4,'Vocal Woo','instrument-4-audio-c7b78912-493a-4e19-a023-10a6b334e2b3.wav',0.01,0.522,120,464.211,'2017-06-24 00:22:32','2017-06-24 00:22:32','Published'),(141,4,'Vocal JB Me','instrument-4-audio-3fbbf18b-eb45-4375-8bd2-efd5e490c4cb.wav',14,0.336,120,367.5,'2017-06-24 00:23:45','2017-06-24 00:23:45','Published'),(143,4,'Vocal JB Hit','instrument-4-audio-686906da-cc85-4abb-a902-121e98def35d.wav',0.05,0.313,120,512.791,'2017-06-24 00:25:58','2017-06-24 00:25:58','Published'),(144,4,'Vocal Hey','instrument-4-audio-5d808588-5930-4075-a034-4f96b0e2b06f.wav',0.046,0.453,120,760.345,'2017-06-24 00:26:50','2017-06-24 00:26:50','Published'),(145,4,'Vocal Ehh','instrument-4-audio-7806beda-4655-4323-adb0-d9a41d2fc939.wav',0.018,0.297,120,648.529,'2017-06-24 00:27:36','2017-06-24 00:27:36','Published'),(146,4,'Vocal Eh','instrument-4-audio-a6049156-69e0-4128-a4b1-6a17ee4ca0bd.wav',0.018,0.449,120,668.182,'2017-06-24 00:28:28','2017-06-24 00:28:28','Published'),(147,5,'Vocal Watch Me','instrument-5-audio-649a2969-6b98-4201-89fc-968d6414f578.wav',0.05,0.807,120,1225,'2017-06-24 00:29:58','2017-06-24 00:29:58','Published'),(148,5,'Vocal Play It','instrument-5-audio-53fc9c8c-2412-4133-b088-9bac349e6794.wav',0.064,0.358,120,116.053,'2017-06-24 00:31:33','2017-06-24 00:31:33','Published'),(149,5,'Vocal Hoh','instrument-5-audio-5709e633-bd69-407b-b6ba-420395b221de.wav',0.028,0.476,120,689.062,'2017-06-24 00:32:33','2017-06-24 00:32:33','Published'),(150,5,'Vocal Woah','instrument-5-audio-7ac9d00c-0b24-49ad-8cbb-c586ac0f080f.wav',0.02,0.488,120,604.11,'2017-06-24 00:33:44','2017-06-24 00:33:44','Published'),(151,5,'Vocal What 3','instrument-5-audio-489c5976-cbda-4449-a8cf-67d653b77dbf.wav',0.04,0.407,120,370.588,'2017-06-24 00:34:40','2017-06-24 00:34:40','Published'),(152,5,'Vocal What 2','instrument-5-audio-70d22a2a-a888-460f-9dfa-01bae076adfe.wav',0.027,0.276,120,416.038,'2017-06-24 00:35:28','2017-06-24 00:35:28','Published'),(153,5,'Vocal What 1','instrument-5-audio-cccc3d64-9cb9-468d-be42-e1ec29ba65b1.wav',0.058,0.401,120,390.265,'2017-06-24 00:36:13','2017-06-24 00:36:13','Published'),(154,5,'Vocal Oobah','instrument-5-audio-a7779c99-55b0-4067-819d-a8203a157cd6.wav',0.025,0.904,120,397.297,'2017-06-24 00:37:11','2017-06-24 00:37:11','Published');
/*!40000 ALTER TABLE `audio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_chord`
--

LOCK TABLES `audio_chord` WRITE;
/*!40000 ALTER TABLE `audio_chord` DISABLE KEYS */;
/*!40000 ALTER TABLE `audio_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audio_event`
--

LOCK TABLES `audio_event` WRITE;
/*!40000 ALTER TABLE `audio_event` DISABLE KEYS */;
INSERT INTO `audio_event` VALUES (1,2,1,1,'KICK',0.03,0.5,'X','2017-04-22 21:24:11','2017-04-22 21:28:14'),(2,3,1,1,'KICKLONG',0.025,0.5,'X','2017-04-22 21:24:54','2017-04-24 02:19:23'),(3,4,1,0.1,'HIHATCLOSED',0.025,0.1,'X','2017-04-22 21:26:58','2017-06-10 19:24:57'),(4,5,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-04-22 21:43:14','2017-04-22 21:43:14'),(5,6,1,0.4,'SNARE',0.002,0.091,'X','2017-04-22 21:45:06','2017-04-22 21:45:06'),(6,7,0.7,0.6,'TOM',0.002,0.35,'X','2017-04-22 21:46:12','2017-04-22 21:46:12'),(7,8,0.8,0.8,'CLAVES',0,0.05,'X','2017-04-24 00:03:50','2017-04-24 00:03:50'),(8,9,0.8,0.9,'CONGA',0.004,0.2,'X','2017-04-24 00:04:13','2017-04-24 00:04:13'),(9,11,1,1,'TOMHIGH',0.004,0.2,'X','2017-04-24 02:18:57','2017-04-24 02:18:57'),(10,10,1,1,'CONGAHIGH',0.005,0.2,'x','2017-04-24 02:20:10','2017-04-24 02:20:10'),(11,12,0.8,0.3,'CLAP',0.004,0.3,'x','2017-04-24 02:21:39','2017-06-04 04:30:00'),(12,13,1,0.5,'COWBELL',0.004,0.3,'x','2017-04-24 02:23:14','2017-04-24 02:23:14'),(13,14,1,0,'CYMBALCRASH',0,4,'x','2017-04-24 02:24:36','2017-06-16 03:26:40'),(14,15,0.5,0.1,'HIHATOPEN',0.002,0.59,'x','2017-04-24 02:25:56','2017-06-10 19:25:57'),(15,16,0.6,0.2,'SNARERIM',0.001,0.014,'x','2017-04-24 02:27:24','2017-06-10 19:27:09'),(16,22,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:35','2017-06-15 23:27:35'),(17,23,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:27:55','2017-06-15 23:27:55'),(18,24,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-15 23:28:31','2017-06-15 23:28:31'),(20,26,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:29:00','2017-06-15 23:29:00'),(21,27,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:14','2017-06-15 23:29:14'),(22,28,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:28','2017-06-15 23:29:28'),(23,29,1,0.1,'HIHATOPEN',0,1,'x','2017-06-15 23:29:40','2017-06-15 23:29:40'),(24,30,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:02','2017-06-15 23:30:02'),(25,31,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:22','2017-06-15 23:30:22'),(26,32,1,0.3,'STICKSIDE',0,1,'x','2017-06-15 23:30:40','2017-06-15 23:30:40'),(27,33,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:30:54','2017-06-15 23:30:54'),(28,34,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:06','2017-06-15 23:31:06'),(29,35,1,0.1,'SNARERIM',0,1,'x','2017-06-15 23:31:17','2017-06-15 23:31:17'),(30,36,1,0.6,'TOMHIGH',0,1,'x','2017-06-15 23:31:59','2017-06-16 01:07:01'),(31,37,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:17','2017-06-15 23:32:17'),(32,38,1,0.6,'TOM',0,1,'x','2017-06-15 23:32:30','2017-06-16 01:07:14'),(33,39,1,0.6,'CONGAHIGH',0,1,'x','2017-06-15 23:32:39','2017-06-16 01:07:57'),(34,40,1,0.6,'CONGA',0,1,'x','2017-06-15 23:32:48','2017-06-16 01:08:10'),(35,41,1,0.1,'SNARE',0,1,'x','2017-06-15 23:32:59','2017-06-15 23:32:59'),(36,42,1,0.1,'SNARE',0,1,'x','2017-06-15 23:33:08','2017-06-15 23:33:08'),(37,44,1,1,'KICK',0,1,'x','2017-06-16 00:25:00','2017-06-16 00:25:00'),(38,43,1,1,'KICK',0,1,'x','2017-06-16 00:25:19','2017-06-16 00:25:19'),(39,45,1,1,'KICK',0,1,'x','2017-06-16 00:27:24','2017-06-16 00:27:24'),(42,48,1,0.6,'TOMLOW',0,1,'x','2017-06-16 00:34:38','2017-06-16 01:07:44'),(43,49,1,0.6,'TOM',0,1,'x','2017-06-16 00:36:12','2017-06-16 01:08:36'),(44,50,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 00:38:24','2017-06-16 01:08:52'),(45,51,1,1,'KICKLONG',0,1,'x','2017-06-16 01:10:01','2017-06-16 01:10:01'),(48,54,1,0.1,'CLAP',0,1,'x','2017-06-16 02:16:01','2017-06-16 02:16:01'),(49,56,1,1,'KICK',0,1,'x','2017-06-16 03:01:30','2017-06-16 03:01:30'),(50,57,1,1,'KICKLONG',0,1,'x','2017-06-16 03:04:19','2017-06-16 03:04:19'),(51,58,1,0.6,'TOMHIGH',0,1,'x','2017-06-16 03:06:43','2017-06-16 03:06:43'),(52,59,1,0.6,'TOMLOW',0,1,'x','2017-06-16 03:07:39','2017-06-16 03:07:39'),(53,60,1,0.6,'TOM',0,1,'x','2017-06-16 03:10:02','2017-06-16 03:10:02'),(54,61,1,0.1,'CLAP',0,1,'x','2017-06-16 03:13:38','2017-06-16 03:13:38'),(55,62,1,0.1,'CLAP',0,1,'x','2017-06-16 03:14:51','2017-06-16 03:14:51'),(56,63,1,0.1,'MARACAS',0,1,'x','2017-06-16 03:17:20','2017-06-16 03:17:20'),(57,64,1,0.2,'COWBELL',0,1,'x','2017-06-16 03:20:15','2017-06-16 03:20:15'),(58,65,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:22:02','2017-06-16 03:24:34'),(59,66,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:24:20','2017-06-16 03:24:20'),(60,67,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:25:47','2017-06-16 03:25:47'),(61,68,1,0,'CYMBALCRASH',0,4,'x','2017-06-16 03:29:05','2017-06-16 03:29:05'),(62,69,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:12:32','2017-06-20 23:16:38'),(63,70,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:17:52','2017-06-20 23:17:52'),(64,71,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:33:17','2017-06-20 23:33:17'),(65,72,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:35:34','2017-06-20 23:35:34'),(66,73,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:36:46','2017-06-20 23:36:46'),(67,74,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:37:56','2017-06-20 23:37:56'),(68,75,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:39:16','2017-06-20 23:39:16'),(69,76,1,0.1,'HIHATCLOSED',0,1,'X','2017-06-20 23:40:49','2017-06-20 23:40:49'),(70,77,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:45:46','2017-06-20 23:45:46'),(71,78,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:46:41','2017-06-20 23:46:41'),(72,79,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:47:35','2017-06-20 23:47:35'),(73,80,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:48:55','2017-06-20 23:48:55'),(74,81,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:49:58','2017-06-20 23:49:58'),(75,82,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:50:42','2017-06-20 23:50:42'),(76,83,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:51:32','2017-06-20 23:51:32'),(77,84,1,0.15,'HIHATOPEN',0,1,'X','2017-06-20 23:52:08','2017-06-20 23:52:08'),(78,85,1,1,'KICK',0,1,'X','2017-06-20 23:54:26','2017-06-20 23:54:26'),(79,86,1,1,'KICK',0,1,'X','2017-06-20 23:56:22','2017-06-20 23:56:22'),(80,87,1,1,'KICK',0,1,'X','2017-06-20 23:59:00','2017-06-20 23:59:00'),(81,88,1,1,'KICK',0,1,'X','2017-06-20 23:59:46','2017-06-20 23:59:46'),(82,89,1,1,'KICK',0,1,'X','2017-06-21 00:00:29','2017-06-21 00:00:29'),(83,90,1,1,'KICK',0,1,'X','2017-06-21 00:01:16','2017-06-21 00:01:16'),(84,91,1,1,'KICK',0,1,'X','2017-06-21 00:01:59','2017-06-21 00:01:59'),(85,92,1,1,'KICK',0,1,'X','2017-06-21 00:02:39','2017-06-21 00:02:39'),(86,93,1,0.1,'SNARE',0,1,'X','2017-06-21 00:14:01','2017-06-21 00:14:01'),(87,94,1,0.15,'SNARE',0,1,'X','2017-06-21 00:14:49','2017-06-21 00:14:49'),(88,95,1,0.1,'SNARE',0,1,'X','2017-06-21 00:15:35','2017-06-21 00:15:35'),(89,96,1,0.15,'SNARE',0,1,'X','2017-06-21 00:49:39','2017-06-21 00:50:12'),(90,97,1,0.15,'SNARE',0,1,'X','2017-06-21 00:51:14','2017-06-21 00:51:14'),(91,98,1,0.15,'SNARE',0,1,'X','2017-06-21 00:53:11','2017-06-21 00:53:11'),(92,99,1,0.15,'SNARE',0,1,'X','2017-06-21 00:54:35','2017-06-21 00:54:35'),(93,100,1,0.15,'SNARE',0,1,'X','2017-06-21 00:55:37','2017-06-21 00:55:37'),(94,101,1,0.15,'SNARE',0,1,'X','2017-06-21 00:57:01','2017-06-21 00:57:01'),(95,102,1,0.15,'SNARE',0,1,'X','2017-06-21 00:58:31','2017-06-21 00:58:31'),(96,103,1,0.15,'SNARE',0,1,'X','2017-06-21 01:00:02','2017-06-21 01:00:02'),(97,104,1,0.6,'CONGA',0,1,'X','2017-06-21 01:04:26','2017-06-21 01:04:26'),(98,105,1,0.6,'TOM',0,1,'X','2017-06-21 01:05:37','2017-06-21 01:05:37'),(99,106,1,0.6,'CONGA',0,1,'X','2017-06-21 01:06:29','2017-06-21 01:06:29'),(100,107,1,0.6,'CONGA',0,1,'X','2017-06-21 01:07:21','2017-06-21 01:07:21'),(101,108,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:06','2017-06-21 01:08:13'),(102,109,1,0.6,'CONGA',0,1,'X','2017-06-21 01:08:59','2017-06-21 01:08:59'),(103,110,1,0.6,'CONGA',0,1,'X','2017-06-21 01:09:39','2017-06-21 01:09:39'),(104,111,1,0.6,'CONGA',0,1,'X','2017-06-21 01:10:23','2017-06-21 01:10:23'),(105,112,1,0.6,'CONGA',0,1,'X','2017-06-21 01:11:09','2017-06-21 01:11:09'),(106,113,1,0.6,'CONGA',0,1,'X','2017-06-21 01:12:01','2017-06-21 01:12:01'),(107,114,1,0.6,'CONGA',0,1,'X','2017-06-21 01:13:01','2017-06-21 01:13:01'),(108,115,1,0.6,'TOM',0,1,'X','2017-06-21 01:13:42','2017-06-21 01:13:42'),(109,116,1,0.6,'TOM',0,1,'X','2017-06-21 01:14:28','2017-06-21 01:14:28'),(110,117,1,0.6,'TOM',0,1,'X','2017-06-21 01:15:12','2017-06-21 01:15:12'),(111,119,1,0.6,'TOM',0,1,'X','2017-06-21 01:19:59','2017-06-21 01:19:59'),(112,120,1,0.6,'TOM',0,1,'X','2017-06-21 01:21:05','2017-06-21 01:21:05'),(113,121,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:02','2017-06-21 01:22:02'),(114,122,1,0.6,'TOM',0,1,'X','2017-06-21 01:22:46','2017-06-21 01:22:46'),(115,123,1,0.6,'TOM',0,1,'X','2017-06-21 01:23:31','2017-06-21 01:23:31'),(116,124,1,0.6,'TOM',0,1,'X','2017-06-21 01:24:10','2017-06-21 01:24:10'),(117,125,1,0.3,'HEY',0,1,'X','2017-06-23 23:53:03','2017-06-23 23:53:03'),(118,126,1,0.3,'X',0,1,'0','2017-06-23 23:54:24','2017-06-23 23:54:24'),(119,127,1,0.3,'HEY',0,1,'X','2017-06-23 23:56:04','2017-06-23 23:56:04'),(120,128,1,0.3,'HEY',0,1,'X','2017-06-23 23:57:11','2017-06-23 23:57:11'),(121,129,1,0.3,'HEY',0,1,'X','2017-06-23 23:58:09','2017-06-23 23:58:09'),(124,132,1,0.3,'HEY',0,1,'X','2017-06-24 00:00:37','2017-06-24 00:00:37'),(125,133,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:08','2017-06-24 00:11:08'),(126,134,1,0.3,'HEY',0,1,'X','2017-06-24 00:11:47','2017-06-24 00:11:47'),(127,135,1,0.3,'HEY',0,1,'X','2017-06-24 00:13:39','2017-06-24 00:13:39'),(128,136,1,0.3,'HEY',0,1,'X','2017-06-24 00:15:04','2017-06-24 00:15:04'),(129,137,1,0.3,'HEY',0,1,'X','2017-06-24 00:16:27','2017-06-24 00:16:27'),(130,138,1,0.3,'HEY',0,1,'X','2017-06-24 00:17:46','2017-06-24 00:17:46'),(131,139,1,0.3,'HEY',0,1,'X','2017-06-24 00:20:45','2017-06-24 00:20:45'),(132,140,1,0.3,'HEY',0,1,'X','2017-06-24 00:22:47','2017-06-24 00:22:47'),(133,141,1,0.3,'HEY',0,1,'X','2017-06-24 00:24:25','2017-06-24 00:24:25'),(134,143,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:08','2017-06-24 00:26:08'),(135,144,1,0.3,'HEY',0,1,'X','2017-06-24 00:26:59','2017-06-24 00:26:59'),(136,145,1,0.3,'HEY',0,1,'X','2017-06-24 00:27:47','2017-06-24 00:27:47'),(137,147,1,0.3,'HEY',0,1,'X','2017-06-24 00:30:09','2017-06-24 00:30:09'),(138,148,1,0.3,'HEY',0,1,'X','2017-06-24 00:31:42','2017-06-24 00:31:42'),(139,149,1,0.3,'HEY',0,1,'X','2017-06-24 00:32:45','2017-06-24 00:32:45'),(140,150,1,0.3,'HEY',0,1,'X','2017-06-24 00:33:52','2017-06-24 00:33:52'),(141,151,1,0.3,'HEY',0,1,'X','2017-06-24 00:34:48','2017-06-24 00:34:48'),(142,152,1,0.3,'HEY',0,1,'X','2017-06-24 00:35:36','2017-06-24 00:35:36'),(143,153,1,0.3,'HEY',0,1,'X','2017-06-24 00:36:22','2017-06-24 00:36:22'),(144,154,1,0.3,'HEY',0,1,'X','2017-06-24 00:37:19','2017-06-24 00:37:19');
/*!40000 ALTER TABLE `audio_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'ebroot','2017-02-04 17:36:22',142,1),(2,'2','account','SQL','V2__account.sql',-728725086,'ebroot','2017-02-04 17:36:23',117,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'ebroot','2017-02-04 17:36:23',54,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'ebroot','2017-02-04 17:36:23',387,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'ebroot','2017-02-04 17:36:23',226,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'ebroot','2017-02-04 17:36:24',525,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'ebroot','2017-02-04 17:36:24',162,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'ebroot','2017-02-04 17:36:24',64,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'ebroot','2017-02-04 17:36:24',51,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'ebroot','2017-02-04 17:36:24',36,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'ebroot','2017-02-04 17:36:24',13,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'ebroot','2017-02-04 17:36:24',48,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'ebroot','2017-02-04 17:36:25',54,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'ebroot','2017-02-04 17:36:25',54,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'ebroot','2017-02-04 17:36:25',48,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'ebroot','2017-02-04 17:36:25',89,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'ebroot','2017-02-04 17:36:25',130,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'ebroot','2017-02-04 17:36:25',382,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'ebroot','2017-02-04 17:36:26',241,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'ebroot','2017-02-04 17:36:26',143,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'ebroot','2017-02-05 23:06:15',315,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'ebroot','2017-02-07 03:21:14',29,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'ebroot','2017-02-07 03:21:14',125,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'ebroot','2017-02-10 00:03:21',205,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'ebroot','2017-02-10 00:03:21',107,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'ebroot','2017-02-10 00:03:25',3684,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'ebroot','2017-02-13 19:04:58',239,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'ebroot','2017-02-14 04:55:49',175,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'ebroot','2017-02-14 04:55:50',126,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'ebroot','2017-02-14 04:55:50',139,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'ebroot','2017-02-19 22:29:51',207,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'ebroot','2017-02-19 22:29:51',29,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'ebroot','2017-04-21 16:24:11',40,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'ebroot','2017-04-28 14:57:19',58,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'ebroot','2017-04-28 14:57:19',52,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'ebroot','2017-04-28 14:57:19',53,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'ebroot','2017-04-28 14:57:19',78,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'ebroot','2017-05-01 19:09:45',95,1),(39,'40','ALTER choice MODIFY COLUMN phase offset ULONG','SQL','V40__ALTER_choice_MODIFY_COLUMN_phase_offset_ULONG.sql',-240451169,'ebroot','2017-05-18 00:34:09',63,1),(40,'41','CREATE TABLE link meme','SQL','V41__CREATE_TABLE_link_meme.sql',-18883080,'ebroot','2017-05-18 00:34:09',51,1),(41,'42','ALTER phase link INT total','SQL','V42__ALTER_phase_link_INT_total.sql',-1400879099,'ebroot','2017-05-18 00:34:10',122,1),(42,'43','CREATE TABLE link message','SQL','V43__CREATE_TABLE_link_message.sql',1616909549,'ebroot','2017-05-18 00:34:10',46,1),(43,'44','ALTER pick BELONGS TO arrangement DROP morph point','SQL','V44__ALTER_pick_BELONGS_TO_arrangement_DROP_morph_point.sql',449955118,'ebroot','2017-05-26 00:58:12',563,1),(44,'45','ALTER link ADD COLUMN waveform key','SQL','V45__ALTER_link_ADD_COLUMN_waveform_key.sql',-98370,'ebroot','2017-06-01 16:53:07',811,1),(45,'46','ALTER audio ADD COLUMN state','SQL','V46__ALTER_audio_ADD_COLUMN_state.sql',-1300058820,'ebroot','2017-06-04 21:28:24',161,1);
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

-- Dump completed on 2017-07-07 21:07:02

