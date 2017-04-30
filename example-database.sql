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
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'Alpha Team','2017-02-10 00:03:21','2017-02-10 00:03:21'),(2,'Beta Team','2017-02-10 00:03:21','2017-02-10 00:03:21'),(3,'Retrospective','2017-02-10 00:03:21','2017-02-17 17:22:45');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_user`
--

LOCK TABLES `account_user` WRITE;
/*!40000 ALTER TABLE `account_user` DISABLE KEYS */;
INSERT INTO `account_user` VALUES (1,1,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(2,1,2,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(3,3,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(4,2,1,'2017-02-10 00:03:21','2017-02-10 00:03:21'),(5,1,3,'2017-03-08 02:27:34','2017-03-08 02:27:34'),(6,1,4,'2017-03-08 23:30:42','2017-03-08 23:30:42'),(7,1,5,'2017-03-10 05:39:37','2017-03-10 05:39:37'),(8,1,6,'2017-04-17 21:59:20','2017-04-17 21:59:20'),(9,1,7,'2017-04-17 21:59:24','2017-04-17 21:59:24');
/*!40000 ALTER TABLE `account_user` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `arrangement`
--

LOCK TABLES `arrangement` WRITE;
/*!40000 ALTER TABLE `arrangement` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrangement` ENABLE KEYS */;
UNLOCK TABLES;

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
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `audio_fk_instrument_idx` (`instrument_id`),
  CONSTRAINT `audio_fk_instrument` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audio`
--

LOCK TABLES `audio` WRITE;
/*!40000 ALTER TABLE `audio` DISABLE KEYS */;
INSERT INTO `audio` VALUES (1,1,'Kick','https://static.xj.outright.io/instrument/808/kick.wav',0.012,0.54,120,440,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(2,3,'Kick','instrument-3-audio-80454e35-5693-4b42-aa6a-218383a9f584.wav',0,0.702,120,57.495,'2017-04-21 16:41:03','2017-04-24 00:06:17'),(3,3,'Kick Long','instrument-3-audio-ed1957b9-eea0-42f8-8493-b8874e1a6bf9.wav',0,0.865,120,57.05,'2017-04-21 18:52:17','2017-04-24 00:06:22'),(4,3,'Hat Closed','instrument-3-audio-0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14.wav',0,0.053,120,6300,'2017-04-21 19:33:05','2017-04-24 00:06:27'),(5,3,'Maracas','instrument-3-audio-ffe4edd6-5b83-4ac9-8e69-156ddb06762f.wav',0,0.026,120,190.086,'2017-04-21 19:38:16','2017-04-24 00:06:31'),(6,3,'Snare','instrument-3-audio-7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe.wav',0,0.093,120,177.823,'2017-04-21 19:42:59','2017-04-24 00:06:39'),(7,3,'Tom','instrument-3-audio-a6bf0d86-6b45-4cf1-b404-2242095c7876.wav',0,0.36,120,104.751,'2017-04-21 19:43:58','2017-04-24 00:06:43'),(8,3,'Claves','instrument-3-audio-aea2483c-7707-4100-aa86-b680668cd1a0.wav',0,0.03,120,2594,'2017-04-23 23:59:47','2017-04-24 00:06:49'),(9,3,'Conga','instrument-3-audio-f772f19f-b51b-414e-9dc8-8ceb23faa779.wav',0,0.26,120,213,'2017-04-24 00:03:32','2017-04-24 00:06:54'),(10,3,'Conga High','instrument-3-audio-c0975d3a-4f26-44b2-a9d3-800320bfa3e1.wav',0,0.179,120,397.297,'2017-04-24 00:05:34','2017-04-24 00:05:47'),(11,3,'Tom High','instrument-3-audio-aea1351b-bb96-4487-8feb-ae8ad3e499ad.wav',0,0.2,120,190.909,'2017-04-24 02:18:29','2017-04-24 02:18:29'),(12,3,'Hand Clap','instrument-3-audio-ce0662a2-3f7e-425b-8105-fb639d395235.wav',0,0.361,120,1102.5,'2017-04-24 02:21:04','2017-04-24 02:21:04'),(13,3,'Cowbell','instrument-3-audio-aaa877a8-0c89-4781-93f8-69c722285b2a.wav',0,0.34,120,268.902,'2017-04-24 02:22:47','2017-04-24 02:22:47'),(14,3,'Cymbal Crash','instrument-3-audio-37a35a63-23e4-4ef6-a78e-db2577aa9a00.wav',0,2.229,120,109.701,'2017-04-24 02:24:03','2017-04-24 03:08:45'),(15,3,'Hat Open','instrument-3-audio-020ad575-af86-4fe2-a869-957d50d59ac4.wav',0,0.598,120,7350,'2017-04-24 02:25:31','2017-04-24 02:25:31'),(16,3,'Snare Rimshot','instrument-3-audio-58fd7eae-b55e-4567-9c27-ead64b83488a.wav',0,0.014,120,445.445,'2017-04-24 02:26:53','2017-04-24 02:26:53');
/*!40000 ALTER TABLE `audio` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `audio_chord`
--

LOCK TABLES `audio_chord` WRITE;
/*!40000 ALTER TABLE `audio_chord` DISABLE KEYS */;
/*!40000 ALTER TABLE `audio_chord` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audio_event`
--

LOCK TABLES `audio_event` WRITE;
/*!40000 ALTER TABLE `audio_event` DISABLE KEYS */;
INSERT INTO `audio_event` VALUES (1,2,1,1,'KICK',0.03,0.5,'X','2017-04-22 21:24:11','2017-04-22 21:28:14'),(2,3,1,1,'KICKLONG',0.025,0.5,'X','2017-04-22 21:24:54','2017-04-24 02:19:23'),(3,4,1,0.2,'HATCLOSED',0.025,0.05,'X','2017-04-22 21:26:58','2017-04-24 02:19:40'),(4,5,0.8,0.6,'MARACAS',0.011,0.015,'X','2017-04-22 21:43:14','2017-04-22 21:43:14'),(5,6,1,0.4,'SNARE',0.002,0.091,'X','2017-04-22 21:45:06','2017-04-22 21:45:06'),(6,7,0.7,0.6,'TOM',0.002,0.35,'X','2017-04-22 21:46:12','2017-04-22 21:46:12'),(7,8,0.8,0.8,'CLAVES',0,0.05,'X','2017-04-24 00:03:50','2017-04-24 00:03:50'),(8,9,0.8,0.9,'CONGA',0.004,0.2,'X','2017-04-24 00:04:13','2017-04-24 00:04:13'),(9,11,1,1,'TOMHIGH',0.004,0.2,'X','2017-04-24 02:18:57','2017-04-24 02:18:57'),(10,10,1,1,'CONGAHIGH',0.005,0.2,'x','2017-04-24 02:20:10','2017-04-24 02:20:10'),(11,12,0.8,0.3,'HANDCLAP',0.004,0.3,'x','2017-04-24 02:21:39','2017-04-24 02:21:45'),(12,13,1,0.5,'COWBELL',0.004,0.3,'x','2017-04-24 02:23:14','2017-04-24 02:23:14'),(13,14,0.7,0.1,'CYMBALCRASH',0.002,2.2,'x','2017-04-24 02:24:36','2017-04-24 03:09:01'),(14,15,0.5,0.1,'HATCLOSED',0.002,0.59,'x','2017-04-24 02:25:56','2017-04-24 02:25:56'),(15,16,0.6,0.2,'SNARERIMSHOT',0.001,0.014,'x','2017-04-24 02:27:24','2017-04-24 02:27:24');
/*!40000 ALTER TABLE `audio_event` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain`
--

LOCK TABLES `chain` WRITE;
/*!40000 ALTER TABLE `chain` DISABLE KEYS */;
INSERT INTO `chain` VALUES (1,'','First Chain Ever',3,'complete','2017-02-19 22:38:30.000000','2017-02-19 23:05:00.000000','2017-02-10 00:05:30','2017-02-20 07:28:11'),(2,'','Second Chain',3,'complete','2017-02-19 23:08:30.000000','2017-02-19 23:16:00.000000','2017-02-19 23:03:14','2017-02-20 07:26:06'),(3,'','Test Overnight 1-Hour',3,'complete','2017-02-20 08:00:00.000000','2017-02-20 09:00:00.000000','2017-02-20 07:33:05','2017-02-20 09:05:00'),(4,'','Quick Test Chain',3,'complete','2017-02-21 16:23:30.000000','2017-02-21 16:30:00.000000','2017-02-21 16:17:39','2017-02-21 16:35:00'),(5,'','Test Chain',3,'complete','2017-02-22 02:17:00.000000','2017-02-22 02:20:00.000000','2017-02-22 02:11:35','2017-02-22 02:25:00'),(6,'','Go team',3,'complete','2017-03-10 16:15:00.000000','2017-03-10 16:20:00.000000','2017-03-10 16:13:30','2017-03-10 16:25:00'),(7,'preview','House Preview',1,'draft','2017-04-28 15:02:02.000000',NULL,'2017-04-28 15:22:16','2017-04-28 15:22:16');
/*!40000 ALTER TABLE `chain` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_config`
--

LOCK TABLES `chain_config` WRITE;
/*!40000 ALTER TABLE `chain_config` DISABLE KEYS */;
INSERT INTO `chain_config` VALUES (2,7,'OUTPUT_SAMPLE_BITS','16','2017-04-28 15:22:31','2017-04-28 15:22:31'),(3,7,'OUTPUT_FRAME_RATE','48000','2017-04-28 15:22:36','2017-04-28 15:22:36'),(4,7,'OUTPUT_CHANNELS','2','2017-04-28 15:22:40','2017-04-28 15:22:40');
/*!40000 ALTER TABLE `chain_config` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_idea`
--

LOCK TABLES `chain_idea` WRITE;
/*!40000 ALTER TABLE `chain_idea` DISABLE KEYS */;
INSERT INTO `chain_idea` VALUES (1,7,6,'2017-04-28 20:35:12','2017-04-28 20:35:12');
/*!40000 ALTER TABLE `chain_idea` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_instrument`
--

LOCK TABLES `chain_instrument` WRITE;
/*!40000 ALTER TABLE `chain_instrument` DISABLE KEYS */;
INSERT INTO `chain_instrument` VALUES (1,7,3,'2017-04-28 20:35:03','2017-04-28 20:35:03');
/*!40000 ALTER TABLE `chain_instrument` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_library`
--

LOCK TABLES `chain_library` WRITE;
/*!40000 ALTER TABLE `chain_library` DISABLE KEYS */;
INSERT INTO `chain_library` VALUES (2,1,2,'2017-02-13 19:08:46','2017-02-13 19:08:46'),(3,5,2,'2017-03-04 02:12:44','2017-03-04 02:12:44'),(4,7,1,'2017-04-28 15:22:52','2017-04-28 15:22:52');
/*!40000 ALTER TABLE `chain_library` ENABLE KEYS */;
UNLOCK TABLES;

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
  `phase_offset` int(10) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `choice_fk_link_idx` (`link_id`),
  KEY `choice_fk_idea_idx` (`idea_id`),
  CONSTRAINT `choice_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_link` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `choice`
--

LOCK TABLES `choice` WRITE;
/*!40000 ALTER TABLE `choice` DISABLE KEYS */;
/*!40000 ALTER TABLE `choice` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `idea`
--

LOCK TABLES `idea` WRITE;
/*!40000 ALTER TABLE `idea` DISABLE KEYS */;
INSERT INTO `idea` VALUES (2,1,2,'main','Introducing K',0.5,'G minor',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(3,1,2,'main','Mental Addition',0.5,'D minor',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(4,1,2,'main','Children of the Who',0.7,'A major',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(5,1,2,'main','K Project',0.6,'G major',121,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(6,1,1,'rhythm','Basic Beat',0.62,'C',120,'2017-04-23 23:21:52','2017-04-23 23:21:52'),(7,1,1,'macro','Deep, from Hot to Cool',0.6,'C',121,'2017-05-01 18:59:22','2017-05-01 19:44:22'),(8,1,1,'macro','Deep, from Cool to Hot',0.6,'G minor',121,'2017-05-01 18:59:32','2017-05-01 19:44:35');
/*!40000 ALTER TABLE `idea` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `idea_meme`
--

LOCK TABLES `idea_meme` WRITE;
/*!40000 ALTER TABLE `idea_meme` DISABLE KEYS */;
INSERT INTO `idea_meme` VALUES (1,6,'Classic','2017-04-23 23:22:21','2017-04-23 23:22:21'),(2,6,'Deep','2017-04-23 23:22:23','2017-04-23 23:22:23'),(3,6,'Acid','2017-04-23 23:22:24','2017-04-23 23:22:24'),(5,6,'Tech','2017-04-23 23:22:28','2017-04-23 23:22:28'),(6,6,'Electro','2017-04-23 23:22:31','2017-04-23 23:22:31'),(7,6,'Tropical','2017-04-23 23:22:34','2017-04-23 23:22:34'),(8,6,'Hot','2017-04-23 23:22:36','2017-04-23 23:22:36'),(9,6,'Cool','2017-04-23 23:22:39','2017-04-23 23:22:39'),(10,6,'Hard','2017-04-23 23:22:40','2017-04-23 23:22:40'),(11,6,'Easy','2017-04-23 23:22:42','2017-04-23 23:22:42'),(12,6,'Progressive','2017-04-23 23:23:17','2017-04-23 23:23:17'),(15,7,'Deep','2017-05-01 18:59:46','2017-05-01 18:59:46'),(16,8,'Deep','2017-05-01 19:42:36','2017-05-01 19:42:36');
/*!40000 ALTER TABLE `idea_meme` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instrument`
--

LOCK TABLES `instrument` WRITE;
/*!40000 ALTER TABLE `instrument` DISABLE KEYS */;
INSERT INTO `instrument` VALUES (1,1,2,'percussive','Roland TR-808 Drum Machine',0.8,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(2,1,2,'percussive','909 drum machine',0.75,'2017-02-10 00:03:22','2017-02-10 00:03:22'),(3,1,1,'percussive','TR-808',0.8,'2017-04-21 16:33:55','2017-04-21 16:33:55');
/*!40000 ALTER TABLE `instrument` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instrument_meme`
--

LOCK TABLES `instrument_meme` WRITE;
/*!40000 ALTER TABLE `instrument_meme` DISABLE KEYS */;
INSERT INTO `instrument_meme` VALUES (1,1,'Drum','2017-02-10 00:03:23','2017-02-10 00:03:23'),(2,3,'Classic','2017-04-23 23:13:29','2017-04-23 23:13:29'),(3,3,'Deep','2017-04-23 23:13:33','2017-04-23 23:13:33'),(4,3,'Acid','2017-04-23 23:13:36','2017-04-23 23:13:36'),(6,3,'Tech','2017-04-23 23:13:41','2017-04-23 23:13:41'),(7,3,'Electro','2017-04-23 23:13:43','2017-04-23 23:13:43'),(8,3,'Tropical','2017-04-23 23:13:46','2017-04-23 23:13:46'),(9,3,'Hot','2017-04-23 23:20:53','2017-04-23 23:20:53'),(10,3,'Cool','2017-04-23 23:20:57','2017-04-23 23:20:57'),(11,3,'Hard','2017-04-23 23:20:59','2017-04-23 23:20:59'),(12,3,'Easy','2017-04-23 23:21:01','2017-04-23 23:21:01'),(13,3,'Progressive','2017-04-23 23:23:43','2017-04-23 23:23:43');
/*!40000 ALTER TABLE `instrument_meme` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `library`
--

LOCK TABLES `library` WRITE;
/*!40000 ALTER TABLE `library` DISABLE KEYS */;
INSERT INTO `library` VALUES (1,'House',1,'2017-02-10 00:03:23','2017-04-21 16:33:43'),(2,'K Project 2',3,'2017-02-10 00:03:23','2017-02-10 00:03:23');
/*!40000 ALTER TABLE `library` ENABLE KEYS */;
UNLOCK TABLES;

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
  `total` bigint(20) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_chain_offset_index` (`chain_id`,`offset`),
  KEY `link_fk_chain_idx` (`chain_id`),
  CONSTRAINT `link_fk_chain` FOREIGN KEY (`chain_id`) REFERENCES `chain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `link`
--

LOCK TABLES `link` WRITE;
/*!40000 ALTER TABLE `link` DISABLE KEYS */;
INSERT INTO `link` VALUES (1,1,0,'dubbed','2017-02-19 22:38:30.000000','2017-02-19 22:39:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:33:30','2017-02-20 07:27:10'),(2,1,1,'dubbed','2017-02-19 22:39:00.000000','2017-02-19 22:39:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:33:32','2017-02-20 07:27:11'),(3,1,2,'dubbed','2017-02-19 22:39:30.000000','2017-02-19 22:40:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:34:01','2017-02-20 07:27:12'),(4,1,3,'dubbed','2017-02-19 22:40:00.000000','2017-02-19 22:40:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:34:31','2017-02-20 07:27:13'),(5,1,4,'dubbed','2017-02-19 22:40:30.000000','2017-02-19 22:41:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:35:01','2017-02-20 07:27:14'),(6,1,5,'dubbed','2017-02-19 22:41:00.000000','2017-02-19 22:41:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:35:31','2017-02-20 07:27:15'),(7,1,6,'dubbed','2017-02-19 22:41:30.000000','2017-02-19 22:42:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:36:01','2017-02-20 07:27:16'),(8,1,7,'dubbed','2017-02-19 22:42:00.000000','2017-02-19 22:42:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:36:31','2017-02-20 07:27:17'),(9,1,8,'dubbed','2017-02-19 22:42:30.000000','2017-02-19 22:43:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:37:01','2017-02-20 07:27:18'),(10,1,9,'dubbed','2017-02-19 22:43:00.000000','2017-02-19 22:43:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:37:31','2017-02-20 07:27:19'),(11,1,10,'dubbed','2017-02-19 22:43:30.000000','2017-02-19 22:44:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:38:01','2017-02-20 07:27:20'),(12,1,11,'dubbed','2017-02-19 22:44:00.000000','2017-02-19 22:44:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:38:31','2017-02-20 07:27:21'),(13,1,12,'dubbed','2017-02-19 22:44:30.000000','2017-02-19 22:45:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:39:01','2017-02-20 07:27:22'),(14,1,13,'dubbed','2017-02-19 22:45:00.000000','2017-02-19 22:45:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:39:31','2017-02-20 07:27:23'),(15,1,14,'dubbed','2017-02-19 22:45:30.000000','2017-02-19 22:46:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:40:01','2017-02-20 07:27:24'),(16,1,15,'dubbed','2017-02-19 22:46:00.000000','2017-02-19 22:46:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:40:31','2017-02-20 07:27:25'),(17,1,16,'dubbed','2017-02-19 22:46:30.000000','2017-02-19 22:47:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:41:01','2017-02-20 07:27:26'),(18,1,17,'dubbed','2017-02-19 22:47:00.000000','2017-02-19 22:47:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:41:31','2017-02-20 07:27:27'),(19,1,18,'dubbed','2017-02-19 22:47:30.000000','2017-02-19 22:48:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:42:01','2017-02-20 07:27:28'),(20,1,19,'dubbed','2017-02-19 22:48:00.000000','2017-02-19 22:48:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:42:31','2017-02-20 07:27:29'),(21,1,20,'dubbed','2017-02-19 22:48:30.000000','2017-02-19 22:49:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:43:01','2017-02-20 07:27:30'),(22,1,21,'dubbed','2017-02-19 22:49:00.000000','2017-02-19 22:49:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:43:31','2017-02-20 07:27:31'),(23,1,22,'dubbed','2017-02-19 22:49:30.000000','2017-02-19 22:50:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:44:01','2017-02-20 07:27:32'),(24,1,23,'dubbed','2017-02-19 22:50:00.000000','2017-02-19 22:50:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 22:44:31','2017-02-20 07:27:33'),(25,2,0,'dubbed','2017-02-19 23:08:30.000000','2017-02-19 23:09:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 23:03:30','2017-02-20 07:25:51'),(26,2,1,'dubbed','2017-02-19 23:09:00.000000','2017-02-19 23:09:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 23:03:32','2017-02-20 07:25:52'),(27,2,2,'dubbed','2017-02-19 23:09:30.000000','2017-02-19 23:10:00.000000',NULL,NULL,NULL,NULL,'2017-02-19 23:04:01','2017-02-20 07:25:53'),(28,2,3,'dubbed','2017-02-19 23:10:00.000000','2017-02-19 23:10:30.000000',NULL,NULL,NULL,NULL,'2017-02-19 23:04:31','2017-02-20 07:25:54'),(29,2,4,'dubbed','2017-02-19 23:10:30.000000','2017-02-19 23:11:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 05:34:05','2017-02-20 07:25:55'),(30,2,5,'dubbed','2017-02-19 23:11:00.000000','2017-02-19 23:11:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 05:34:49','2017-02-20 07:25:56'),(31,2,6,'dubbed','2017-02-19 23:11:30.000000','2017-02-19 23:12:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 05:35:32','2017-02-20 07:25:57'),(32,2,7,'dubbed','2017-02-19 23:12:00.000000','2017-02-19 23:12:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 06:45:40','2017-02-20 07:25:58'),(33,2,8,'dubbed','2017-02-19 23:12:30.000000','2017-02-19 23:13:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 06:45:42','2017-02-20 07:25:59'),(34,2,9,'dubbed','2017-02-19 23:13:00.000000','2017-02-19 23:13:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 06:45:44','2017-02-20 07:26:00'),(35,2,10,'dubbed','2017-02-19 23:13:30.000000','2017-02-19 23:14:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 06:45:46','2017-02-20 07:26:01'),(36,2,11,'dubbed','2017-02-19 23:14:00.000000','2017-02-19 23:14:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 06:45:48','2017-02-20 07:26:02'),(37,2,12,'dubbed','2017-02-19 23:14:30.000000','2017-02-19 23:15:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:25:50','2017-02-20 07:26:03'),(38,2,13,'dubbed','2017-02-19 23:15:00.000000','2017-02-19 23:15:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:25:52','2017-02-20 07:26:04'),(39,2,14,'dubbed','2017-02-19 23:15:30.000000','2017-02-19 23:16:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:25:54','2017-02-20 07:26:05'),(40,2,15,'dubbed','2017-02-19 23:16:00.000000','2017-02-19 23:16:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:25:56','2017-02-20 07:26:06'),(41,1,24,'dubbed','2017-02-19 22:50:30.000000','2017-02-19 22:51:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:11','2017-02-20 07:27:34'),(42,1,25,'dubbed','2017-02-19 22:51:00.000000','2017-02-19 22:51:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:13','2017-02-20 07:27:35'),(43,1,26,'dubbed','2017-02-19 22:51:30.000000','2017-02-19 22:52:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:15','2017-02-20 07:27:36'),(44,1,27,'dubbed','2017-02-19 22:52:00.000000','2017-02-19 22:52:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:17','2017-02-20 07:27:37'),(45,1,28,'dubbed','2017-02-19 22:52:30.000000','2017-02-19 22:53:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:19','2017-02-20 07:27:38'),(46,1,29,'dubbed','2017-02-19 22:53:00.000000','2017-02-19 22:53:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:21','2017-02-20 07:27:39'),(47,1,30,'dubbed','2017-02-19 22:53:30.000000','2017-02-19 22:54:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:23','2017-02-20 07:27:40'),(48,1,31,'dubbed','2017-02-19 22:54:00.000000','2017-02-19 22:54:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:25','2017-02-20 07:27:41'),(49,1,32,'dubbed','2017-02-19 22:54:30.000000','2017-02-19 22:55:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:27','2017-02-20 07:27:42'),(50,1,33,'dubbed','2017-02-19 22:55:00.000000','2017-02-19 22:55:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:29','2017-02-20 07:27:43'),(51,1,34,'dubbed','2017-02-19 22:55:30.000000','2017-02-19 22:56:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:31','2017-02-20 07:27:44'),(52,1,35,'dubbed','2017-02-19 22:56:00.000000','2017-02-19 22:56:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:33','2017-02-20 07:27:45'),(53,1,36,'dubbed','2017-02-19 22:56:30.000000','2017-02-19 22:57:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:35','2017-02-20 07:27:46'),(54,1,37,'dubbed','2017-02-19 22:57:00.000000','2017-02-19 22:57:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:38','2017-02-20 07:27:47'),(55,1,38,'dubbed','2017-02-19 22:57:30.000000','2017-02-19 22:58:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:39','2017-02-20 07:27:48'),(56,1,39,'dubbed','2017-02-19 22:58:00.000000','2017-02-19 22:58:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:41','2017-02-20 07:27:49'),(57,1,40,'dubbed','2017-02-19 22:58:30.000000','2017-02-19 22:59:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:43','2017-02-20 07:27:50'),(58,1,41,'dubbed','2017-02-19 22:59:00.000000','2017-02-19 22:59:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:45','2017-02-20 07:27:51'),(59,1,42,'dubbed','2017-02-19 22:59:30.000000','2017-02-19 23:00:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:47','2017-02-20 07:27:52'),(60,1,43,'dubbed','2017-02-19 23:00:00.000000','2017-02-19 23:00:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:49','2017-02-20 07:27:53'),(61,1,44,'dubbed','2017-02-19 23:00:30.000000','2017-02-19 23:01:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:51','2017-02-20 07:27:54'),(62,1,45,'dubbed','2017-02-19 23:01:00.000000','2017-02-19 23:01:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:53','2017-02-20 07:27:55'),(63,1,46,'dubbed','2017-02-19 23:01:30.000000','2017-02-19 23:02:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:55','2017-02-20 07:27:57'),(64,1,47,'dubbed','2017-02-19 23:02:00.000000','2017-02-19 23:02:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:57','2017-02-20 07:27:59'),(65,1,48,'dubbed','2017-02-19 23:02:30.000000','2017-02-19 23:03:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:27:59','2017-02-20 07:28:01'),(66,1,49,'dubbed','2017-02-19 23:03:00.000000','2017-02-19 23:03:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:28:01','2017-02-20 07:28:03'),(67,1,50,'dubbed','2017-02-19 23:03:30.000000','2017-02-19 23:04:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:28:03','2017-02-20 07:28:05'),(68,1,51,'dubbed','2017-02-19 23:04:00.000000','2017-02-19 23:04:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:28:05','2017-02-20 07:28:07'),(69,1,52,'dubbed','2017-02-19 23:04:30.000000','2017-02-19 23:05:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:28:07','2017-02-20 07:28:09'),(70,1,53,'dubbed','2017-02-19 23:05:00.000000','2017-02-19 23:05:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:28:09','2017-02-20 07:28:11'),(71,3,0,'dubbed','2017-02-20 08:00:00.000000','2017-02-20 08:00:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:55:00','2017-02-20 07:57:00'),(72,3,1,'dubbed','2017-02-20 08:00:30.000000','2017-02-20 08:01:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:55:02','2017-02-20 07:57:30'),(73,3,2,'dubbed','2017-02-20 08:01:00.000000','2017-02-20 08:01:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:55:31','2017-02-20 07:58:00'),(74,3,3,'dubbed','2017-02-20 08:01:30.000000','2017-02-20 08:02:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:56:02','2017-02-20 07:58:30'),(75,3,4,'dubbed','2017-02-20 08:02:00.000000','2017-02-20 08:02:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:56:31','2017-02-20 07:59:00'),(76,3,5,'dubbed','2017-02-20 08:02:30.000000','2017-02-20 08:03:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:57:02','2017-02-20 07:59:30'),(77,3,6,'dubbed','2017-02-20 08:03:00.000000','2017-02-20 08:03:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:57:31','2017-02-20 08:00:00'),(78,3,7,'dubbed','2017-02-20 08:03:30.000000','2017-02-20 08:04:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:58:01','2017-02-20 08:00:30'),(79,3,8,'dubbed','2017-02-20 08:04:00.000000','2017-02-20 08:04:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:58:31','2017-02-20 08:01:01'),(80,3,9,'dubbed','2017-02-20 08:04:30.000000','2017-02-20 08:05:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:59:01','2017-02-20 08:01:30'),(81,3,10,'dubbed','2017-02-20 08:05:00.000000','2017-02-20 08:05:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 07:59:31','2017-02-20 08:02:00'),(82,3,11,'dubbed','2017-02-20 08:05:30.000000','2017-02-20 08:06:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:00:02','2017-02-20 08:02:30'),(83,3,12,'dubbed','2017-02-20 08:06:00.000000','2017-02-20 08:06:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:00:31','2017-02-20 08:03:00'),(84,3,13,'dubbed','2017-02-20 08:06:30.000000','2017-02-20 08:07:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:01:02','2017-02-20 08:03:30'),(85,3,14,'dubbed','2017-02-20 08:07:00.000000','2017-02-20 08:07:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:01:31','2017-02-20 08:04:00'),(86,3,15,'dubbed','2017-02-20 08:07:30.000000','2017-02-20 08:08:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:02:02','2017-02-20 08:04:30'),(87,3,16,'dubbed','2017-02-20 08:08:00.000000','2017-02-20 08:08:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:02:31','2017-02-20 08:05:00'),(88,3,17,'dubbed','2017-02-20 08:08:30.000000','2017-02-20 08:09:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:03:02','2017-02-20 08:05:30'),(89,3,18,'dubbed','2017-02-20 08:09:00.000000','2017-02-20 08:09:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:03:31','2017-02-20 08:06:00'),(90,3,19,'dubbed','2017-02-20 08:09:30.000000','2017-02-20 08:10:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:04:01','2017-02-20 08:06:30'),(91,3,20,'dubbed','2017-02-20 08:10:00.000000','2017-02-20 08:10:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:04:31','2017-02-20 08:07:00'),(92,3,21,'dubbed','2017-02-20 08:10:30.000000','2017-02-20 08:11:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:05:02','2017-02-20 08:07:30'),(93,3,22,'dubbed','2017-02-20 08:11:00.000000','2017-02-20 08:11:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:05:31','2017-02-20 08:08:00'),(94,3,23,'dubbed','2017-02-20 08:11:30.000000','2017-02-20 08:12:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:06:01','2017-02-20 08:08:30'),(95,3,24,'dubbed','2017-02-20 08:12:00.000000','2017-02-20 08:12:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:06:31','2017-02-20 08:09:00'),(96,3,25,'dubbed','2017-02-20 08:12:30.000000','2017-02-20 08:13:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:07:02','2017-02-20 08:09:30'),(97,3,26,'dubbed','2017-02-20 08:13:00.000000','2017-02-20 08:13:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:07:31','2017-02-20 08:10:00'),(98,3,27,'dubbed','2017-02-20 08:13:30.000000','2017-02-20 08:14:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:08:01','2017-02-20 08:10:30'),(99,3,28,'dubbed','2017-02-20 08:14:00.000000','2017-02-20 08:14:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:08:31','2017-02-20 08:11:00'),(100,3,29,'dubbed','2017-02-20 08:14:30.000000','2017-02-20 08:15:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:09:01','2017-02-20 08:11:30'),(101,3,30,'dubbed','2017-02-20 08:15:00.000000','2017-02-20 08:15:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:09:31','2017-02-20 08:12:00'),(102,3,31,'dubbed','2017-02-20 08:15:30.000000','2017-02-20 08:16:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:10:02','2017-02-20 08:12:30'),(103,3,32,'dubbed','2017-02-20 08:16:00.000000','2017-02-20 08:16:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:10:31','2017-02-20 08:13:00'),(104,3,33,'dubbed','2017-02-20 08:16:30.000000','2017-02-20 08:17:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:11:01','2017-02-20 08:13:30'),(105,3,34,'dubbed','2017-02-20 08:17:00.000000','2017-02-20 08:17:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:11:31','2017-02-20 08:14:00'),(106,3,35,'dubbed','2017-02-20 08:17:30.000000','2017-02-20 08:18:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:12:02','2017-02-20 08:14:30'),(107,3,36,'dubbed','2017-02-20 08:18:00.000000','2017-02-20 08:18:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:12:31','2017-02-20 08:15:01'),(108,3,37,'dubbed','2017-02-20 08:18:30.000000','2017-02-20 08:19:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:13:02','2017-02-20 08:15:30'),(109,3,38,'dubbed','2017-02-20 08:19:00.000000','2017-02-20 08:19:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:13:31','2017-02-20 08:16:00'),(110,3,39,'dubbed','2017-02-20 08:19:30.000000','2017-02-20 08:20:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:14:02','2017-02-20 08:16:30'),(111,3,40,'dubbed','2017-02-20 08:20:00.000000','2017-02-20 08:20:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:14:31','2017-02-20 08:17:00'),(112,3,41,'dubbed','2017-02-20 08:20:30.000000','2017-02-20 08:21:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:15:01','2017-02-20 08:17:30'),(113,3,42,'dubbed','2017-02-20 08:21:00.000000','2017-02-20 08:21:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:15:31','2017-02-20 08:18:00'),(114,3,43,'dubbed','2017-02-20 08:21:30.000000','2017-02-20 08:22:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:16:01','2017-02-20 08:18:30'),(115,3,44,'dubbed','2017-02-20 08:22:00.000000','2017-02-20 08:22:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:16:31','2017-02-20 08:19:00'),(116,3,45,'dubbed','2017-02-20 08:22:30.000000','2017-02-20 08:23:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:17:01','2017-02-20 08:19:30'),(117,3,46,'dubbed','2017-02-20 08:23:00.000000','2017-02-20 08:23:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:17:31','2017-02-20 08:20:00'),(118,3,47,'dubbed','2017-02-20 08:23:30.000000','2017-02-20 08:24:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:18:02','2017-02-20 08:20:30'),(119,3,48,'dubbed','2017-02-20 08:24:00.000000','2017-02-20 08:24:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:18:31','2017-02-20 08:21:00'),(120,3,49,'dubbed','2017-02-20 08:24:30.000000','2017-02-20 08:25:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:19:02','2017-02-20 08:21:30'),(121,3,50,'dubbed','2017-02-20 08:25:00.000000','2017-02-20 08:25:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:19:31','2017-02-20 08:22:00'),(122,3,51,'dubbed','2017-02-20 08:25:30.000000','2017-02-20 08:26:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:20:02','2017-02-20 08:22:30'),(123,3,52,'dubbed','2017-02-20 08:26:00.000000','2017-02-20 08:26:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:20:31','2017-02-20 08:23:00'),(124,3,53,'dubbed','2017-02-20 08:26:30.000000','2017-02-20 08:27:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:21:01','2017-02-20 08:23:30'),(125,3,54,'dubbed','2017-02-20 08:27:00.000000','2017-02-20 08:27:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:21:31','2017-02-20 08:24:00'),(126,3,55,'dubbed','2017-02-20 08:27:30.000000','2017-02-20 08:28:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:22:02','2017-02-20 08:24:30'),(127,3,56,'dubbed','2017-02-20 08:28:00.000000','2017-02-20 08:28:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:22:31','2017-02-20 08:25:00'),(128,3,57,'dubbed','2017-02-20 08:28:30.000000','2017-02-20 08:29:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:23:01','2017-02-20 08:25:30'),(129,3,58,'dubbed','2017-02-20 08:29:00.000000','2017-02-20 08:29:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:23:31','2017-02-20 08:26:00'),(130,3,59,'dubbed','2017-02-20 08:29:30.000000','2017-02-20 08:30:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:24:01','2017-02-20 08:26:30'),(131,3,60,'dubbed','2017-02-20 08:30:00.000000','2017-02-20 08:30:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:24:31','2017-02-20 08:27:00'),(132,3,61,'dubbed','2017-02-20 08:30:30.000000','2017-02-20 08:31:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:25:02','2017-02-20 08:27:30'),(133,3,62,'dubbed','2017-02-20 08:31:00.000000','2017-02-20 08:31:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:25:31','2017-02-20 08:28:00'),(134,3,63,'dubbed','2017-02-20 08:31:30.000000','2017-02-20 08:32:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:26:01','2017-02-20 08:28:30'),(135,3,64,'dubbed','2017-02-20 08:32:00.000000','2017-02-20 08:32:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:26:31','2017-02-20 08:29:00'),(136,3,65,'dubbed','2017-02-20 08:32:30.000000','2017-02-20 08:33:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:27:02','2017-02-20 08:29:30'),(137,3,66,'dubbed','2017-02-20 08:33:00.000000','2017-02-20 08:33:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:27:31','2017-02-20 08:30:00'),(138,3,67,'dubbed','2017-02-20 08:33:30.000000','2017-02-20 08:34:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:28:01','2017-02-20 08:30:30'),(139,3,68,'dubbed','2017-02-20 08:34:00.000000','2017-02-20 08:34:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:28:31','2017-02-20 08:31:00'),(140,3,69,'dubbed','2017-02-20 08:34:30.000000','2017-02-20 08:35:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:29:01','2017-02-20 08:31:30'),(141,3,70,'dubbed','2017-02-20 08:35:00.000000','2017-02-20 08:35:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:29:31','2017-02-20 08:32:00'),(142,3,71,'dubbed','2017-02-20 08:35:30.000000','2017-02-20 08:36:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:30:02','2017-02-20 08:32:30'),(143,3,72,'dubbed','2017-02-20 08:36:00.000000','2017-02-20 08:36:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:30:31','2017-02-20 08:33:00'),(144,3,73,'dubbed','2017-02-20 08:36:30.000000','2017-02-20 08:37:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:31:01','2017-02-20 08:33:30'),(145,3,74,'dubbed','2017-02-20 08:37:00.000000','2017-02-20 08:37:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:31:31','2017-02-20 08:34:00'),(146,3,75,'dubbed','2017-02-20 08:37:30.000000','2017-02-20 08:38:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:32:01','2017-02-20 08:34:30'),(147,3,76,'dubbed','2017-02-20 08:38:00.000000','2017-02-20 08:38:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:32:31','2017-02-20 08:35:00'),(148,3,77,'dubbed','2017-02-20 08:38:30.000000','2017-02-20 08:39:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:33:01','2017-02-20 08:35:30'),(149,3,78,'dubbed','2017-02-20 08:39:00.000000','2017-02-20 08:39:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:33:31','2017-02-20 08:36:00'),(150,3,79,'dubbed','2017-02-20 08:39:30.000000','2017-02-20 08:40:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:34:01','2017-02-20 08:36:30'),(151,3,80,'dubbed','2017-02-20 08:40:00.000000','2017-02-20 08:40:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:34:31','2017-02-20 08:37:00'),(152,3,81,'dubbed','2017-02-20 08:40:30.000000','2017-02-20 08:41:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:35:02','2017-02-20 08:37:30'),(153,3,82,'dubbed','2017-02-20 08:41:00.000000','2017-02-20 08:41:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:35:31','2017-02-20 08:38:00'),(154,3,83,'dubbed','2017-02-20 08:41:30.000000','2017-02-20 08:42:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:36:01','2017-02-20 08:38:30'),(155,3,84,'dubbed','2017-02-20 08:42:00.000000','2017-02-20 08:42:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:36:31','2017-02-20 08:39:00'),(156,3,85,'dubbed','2017-02-20 08:42:30.000000','2017-02-20 08:43:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:37:02','2017-02-20 08:39:30'),(157,3,86,'dubbed','2017-02-20 08:43:00.000000','2017-02-20 08:43:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:37:31','2017-02-20 08:40:00'),(158,3,87,'dubbed','2017-02-20 08:43:30.000000','2017-02-20 08:44:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:38:02','2017-02-20 08:40:30'),(159,3,88,'dubbed','2017-02-20 08:44:00.000000','2017-02-20 08:44:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:38:31','2017-02-20 08:41:00'),(160,3,89,'dubbed','2017-02-20 08:44:30.000000','2017-02-20 08:45:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:39:01','2017-02-20 08:41:30'),(161,3,90,'dubbed','2017-02-20 08:45:00.000000','2017-02-20 08:45:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:39:31','2017-02-20 08:42:00'),(162,3,91,'dubbed','2017-02-20 08:45:30.000000','2017-02-20 08:46:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:40:02','2017-02-20 08:42:30'),(163,3,92,'dubbed','2017-02-20 08:46:00.000000','2017-02-20 08:46:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:40:31','2017-02-20 08:43:00'),(164,3,93,'dubbed','2017-02-20 08:46:30.000000','2017-02-20 08:47:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:41:02','2017-02-20 08:43:30'),(165,3,94,'dubbed','2017-02-20 08:47:00.000000','2017-02-20 08:47:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:41:31','2017-02-20 08:44:00'),(166,3,95,'dubbed','2017-02-20 08:47:30.000000','2017-02-20 08:48:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:42:01','2017-02-20 08:44:30'),(167,3,96,'dubbed','2017-02-20 08:48:00.000000','2017-02-20 08:48:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:42:31','2017-02-20 08:45:00'),(168,3,97,'dubbed','2017-02-20 08:48:30.000000','2017-02-20 08:49:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:43:01','2017-02-20 08:45:30'),(169,3,98,'dubbed','2017-02-20 08:49:00.000000','2017-02-20 08:49:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:43:31','2017-02-20 08:46:00'),(170,3,99,'dubbed','2017-02-20 08:49:30.000000','2017-02-20 08:50:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:44:01','2017-02-20 08:46:30'),(171,3,100,'dubbed','2017-02-20 08:50:00.000000','2017-02-20 08:50:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:44:31','2017-02-20 08:47:00'),(172,3,101,'dubbed','2017-02-20 08:50:30.000000','2017-02-20 08:51:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:45:01','2017-02-20 08:47:30'),(173,3,102,'dubbed','2017-02-20 08:51:00.000000','2017-02-20 08:51:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:45:31','2017-02-20 08:48:00'),(174,3,103,'dubbed','2017-02-20 08:51:30.000000','2017-02-20 08:52:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:46:01','2017-02-20 08:48:30'),(175,3,104,'dubbed','2017-02-20 08:52:00.000000','2017-02-20 08:52:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:46:31','2017-02-20 08:49:00'),(176,3,105,'dubbed','2017-02-20 08:52:30.000000','2017-02-20 08:53:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:47:02','2017-02-20 08:49:30'),(177,3,106,'dubbed','2017-02-20 08:53:00.000000','2017-02-20 08:53:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:47:31','2017-02-20 08:50:00'),(178,3,107,'dubbed','2017-02-20 08:53:30.000000','2017-02-20 08:54:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:48:01','2017-02-20 08:50:30'),(179,3,108,'dubbed','2017-02-20 08:54:00.000000','2017-02-20 08:54:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:48:31','2017-02-20 08:51:00'),(180,3,109,'dubbed','2017-02-20 08:54:30.000000','2017-02-20 08:55:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:49:01','2017-02-20 08:51:30'),(181,3,110,'dubbed','2017-02-20 08:55:00.000000','2017-02-20 08:55:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:49:31','2017-02-20 08:52:00'),(182,3,111,'dubbed','2017-02-20 08:55:30.000000','2017-02-20 08:56:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:50:01','2017-02-20 08:52:30'),(183,3,112,'dubbed','2017-02-20 08:56:00.000000','2017-02-20 08:56:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:50:31','2017-02-20 08:53:00'),(184,3,113,'dubbed','2017-02-20 08:56:30.000000','2017-02-20 08:57:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:51:02','2017-02-20 08:53:30'),(185,3,114,'dubbed','2017-02-20 08:57:00.000000','2017-02-20 08:57:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:51:31','2017-02-20 08:54:00'),(186,3,115,'dubbed','2017-02-20 08:57:30.000000','2017-02-20 08:58:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:52:02','2017-02-20 08:54:30'),(187,3,116,'dubbed','2017-02-20 08:58:00.000000','2017-02-20 08:58:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:52:31','2017-02-20 08:55:00'),(188,3,117,'dubbed','2017-02-20 08:58:30.000000','2017-02-20 08:59:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:53:01','2017-02-20 08:55:30'),(189,3,118,'dubbed','2017-02-20 08:59:00.000000','2017-02-20 08:59:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:53:31','2017-02-20 08:56:00'),(190,3,119,'dubbed','2017-02-20 08:59:30.000000','2017-02-20 09:00:00.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:54:01','2017-02-20 08:56:30'),(191,3,120,'dubbed','2017-02-20 09:00:00.000000','2017-02-20 09:00:30.000000',NULL,NULL,NULL,NULL,'2017-02-20 08:54:31','2017-02-20 08:57:00'),(192,4,0,'dubbed','2017-02-21 16:23:30.000000','2017-02-21 16:24:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:18:30','2017-02-21 16:20:30'),(193,4,1,'dubbed','2017-02-21 16:24:00.000000','2017-02-21 16:24:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:18:32','2017-02-21 16:21:00'),(194,4,2,'dubbed','2017-02-21 16:24:30.000000','2017-02-21 16:25:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:19:01','2017-02-21 16:21:30'),(195,4,3,'dubbed','2017-02-21 16:25:00.000000','2017-02-21 16:25:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:19:31','2017-02-21 16:22:00'),(196,4,4,'dubbed','2017-02-21 16:25:30.000000','2017-02-21 16:26:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:20:01','2017-02-21 16:22:30'),(197,4,5,'dubbed','2017-02-21 16:26:00.000000','2017-02-21 16:26:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:20:31','2017-02-21 16:23:00'),(198,4,6,'dubbed','2017-02-21 16:26:30.000000','2017-02-21 16:27:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:21:01','2017-02-21 16:23:30'),(199,4,7,'dubbed','2017-02-21 16:27:00.000000','2017-02-21 16:27:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:21:31','2017-02-21 16:24:00'),(200,4,8,'dubbed','2017-02-21 16:27:30.000000','2017-02-21 16:28:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:22:01','2017-02-21 16:24:30'),(201,4,9,'dubbed','2017-02-21 16:28:00.000000','2017-02-21 16:28:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:22:31','2017-02-21 16:25:00'),(202,4,10,'dubbed','2017-02-21 16:28:30.000000','2017-02-21 16:29:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:23:01','2017-02-21 16:25:30'),(203,4,11,'dubbed','2017-02-21 16:29:00.000000','2017-02-21 16:29:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:23:31','2017-02-21 16:26:00'),(204,4,12,'dubbed','2017-02-21 16:29:30.000000','2017-02-21 16:30:00.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:24:01','2017-02-21 16:26:30'),(205,4,13,'dubbed','2017-02-21 16:30:00.000000','2017-02-21 16:30:30.000000',NULL,NULL,NULL,NULL,'2017-02-21 16:24:31','2017-02-21 16:27:00'),(206,5,0,'dubbed','2017-02-22 02:17:00.000000','2017-02-22 02:17:30.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:12:01','2017-02-22 02:14:00'),(207,5,1,'dubbed','2017-02-22 02:17:30.000000','2017-02-22 02:18:00.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:12:02','2017-02-22 02:14:30'),(208,5,2,'dubbed','2017-02-22 02:18:00.000000','2017-02-22 02:18:30.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:12:31','2017-02-22 02:15:00'),(209,5,3,'dubbed','2017-02-22 02:18:30.000000','2017-02-22 02:19:00.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:13:02','2017-02-22 02:15:30'),(210,5,4,'dubbed','2017-02-22 02:19:00.000000','2017-02-22 02:19:30.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:13:31','2017-02-22 02:16:00'),(211,5,5,'dubbed','2017-02-22 02:19:30.000000','2017-02-22 02:20:00.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:14:02','2017-02-22 02:16:30'),(212,5,6,'dubbed','2017-02-22 02:20:00.000000','2017-02-22 02:20:30.000000',NULL,NULL,NULL,NULL,'2017-02-22 02:14:31','2017-02-22 02:17:00'),(213,6,0,'dubbed','2017-03-10 16:15:00.000000','2017-03-10 16:15:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:13:52','2017-03-10 16:13:53'),(214,6,1,'dubbed','2017-03-10 16:15:30.000000','2017-03-10 16:16:00.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:13:54','2017-03-10 16:13:55'),(215,6,2,'dubbed','2017-03-10 16:16:00.000000','2017-03-10 16:16:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:13:56','2017-03-10 16:13:57'),(216,6,3,'dubbed','2017-03-10 16:16:30.000000','2017-03-10 16:17:00.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:13:58','2017-03-10 16:13:59'),(217,6,4,'dubbed','2017-03-10 16:17:00.000000','2017-03-10 16:17:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:00','2017-03-10 16:14:01'),(218,6,5,'dubbed','2017-03-10 16:17:30.000000','2017-03-10 16:18:00.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:02','2017-03-10 16:14:30'),(219,6,6,'dubbed','2017-03-10 16:18:00.000000','2017-03-10 16:18:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:04','2017-03-10 16:15:00'),(220,6,7,'dubbed','2017-03-10 16:18:30.000000','2017-03-10 16:19:00.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:06','2017-03-10 16:15:30'),(221,6,8,'dubbed','2017-03-10 16:19:00.000000','2017-03-10 16:19:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:08','2017-03-10 16:16:00'),(222,6,9,'dubbed','2017-03-10 16:19:30.000000','2017-03-10 16:20:00.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:10','2017-03-10 16:16:30'),(223,6,10,'dubbed','2017-03-10 16:20:00.000000','2017-03-10 16:20:30.000000',NULL,NULL,NULL,NULL,'2017-03-10 16:14:31','2017-03-10 16:17:00');
/*!40000 ALTER TABLE `link` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `link_chord`
--

LOCK TABLES `link_chord` WRITE;
/*!40000 ALTER TABLE `link_chord` DISABLE KEYS */;
/*!40000 ALTER TABLE `link_chord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `morph`
--

DROP TABLE IF EXISTS `morph`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `morph` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `arrangement_id` bigint(20) unsigned NOT NULL,
  `position` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `duration` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `morph_fk_arrangement_idx` (`arrangement_id`),
  CONSTRAINT `morph_fk_arrangement` FOREIGN KEY (`arrangement_id`) REFERENCES `arrangement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `morph`
--

LOCK TABLES `morph` WRITE;
/*!40000 ALTER TABLE `morph` DISABLE KEYS */;
/*!40000 ALTER TABLE `morph` ENABLE KEYS */;
UNLOCK TABLES;

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
  `total` bigint(20) unsigned DEFAULT NULL,
  `density` float unsigned DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `tempo` float unsigned DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `phase_fk_idea_idx` (`idea_id`),
  CONSTRAINT `phase_fk_idea` FOREIGN KEY (`idea_id`) REFERENCES `idea` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phase`
--

LOCK TABLES `phase` WRITE;
/*!40000 ALTER TABLE `phase` DISABLE KEYS */;
INSERT INTO `phase` VALUES (2,2,'Intro',0,64,NULL,'G minor',121,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,6,'drop',0,64,NULL,NULL,NULL,'2017-04-23 23:44:19','2017-04-23 23:44:29'),(4,7,'from Hot',0,0,0.7,'C',121,'2017-05-01 19:39:59','2017-05-01 19:45:32'),(5,7,'to Cool',1,0,0.5,'Bb Minor',121,'2017-05-01 19:40:18','2017-05-01 19:46:06'),(6,8,'from Cool',0,0,0.5,'G minor',121,'2017-05-01 19:43:06','2017-05-01 19:45:09'),(7,8,'to Hot',1,0,0.7,'C',121,'2017-05-01 19:43:26','2017-05-01 19:45:02');
/*!40000 ALTER TABLE `phase` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phase_chord`
--

LOCK TABLES `phase_chord` WRITE;
/*!40000 ALTER TABLE `phase_chord` DISABLE KEYS */;
INSERT INTO `phase_chord` VALUES (2,2,'G minor',0,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,2,'D major',12,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(4,2,'G minor',16,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(5,2,'F minor',28,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(6,2,'G minor',32,'2017-02-10 00:03:23','2017-02-10 00:03:23'),(7,3,'C',0,'2017-04-23 23:44:43','2017-04-23 23:44:43');
/*!40000 ALTER TABLE `phase_chord` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phase_meme`
--

LOCK TABLES `phase_meme` WRITE;
/*!40000 ALTER TABLE `phase_meme` DISABLE KEYS */;
INSERT INTO `phase_meme` VALUES (1,2,'Foreboding','2017-02-10 00:03:23','2017-02-10 00:03:23'),(2,2,'Dark','2017-02-10 00:03:23','2017-02-10 00:03:23'),(3,2,'Funky','2017-02-10 00:03:23','2017-02-10 00:03:23'),(4,2,'Hard','2017-02-10 00:03:23','2017-02-10 00:03:23'),(5,6,'Cool','2017-05-01 19:43:30','2017-05-01 19:43:30'),(7,7,'Hot','2017-05-01 19:44:52','2017-05-01 19:44:52'),(8,4,'Hot','2017-05-01 19:45:58','2017-05-01 19:45:58'),(9,5,'Cool','2017-05-01 19:46:10','2017-05-01 19:46:10');
/*!40000 ALTER TABLE `phase_meme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pick`
--

DROP TABLE IF EXISTS `pick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pick` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `arrangement_id` bigint(20) unsigned NOT NULL,
  `morph_id` bigint(20) unsigned NOT NULL,
  `audio_id` bigint(20) unsigned NOT NULL,
  `start` float unsigned NOT NULL,
  `length` float unsigned NOT NULL,
  `amplitude` float unsigned NOT NULL,
  `pitch` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `pick_fk_morph_idx` (`morph_id`),
  KEY `pick_fk_audio_idx` (`audio_id`),
  KEY `pick_fk_arrangement_idx` (`arrangement_id`),
  CONSTRAINT `pick_fk_arrangement` FOREIGN KEY (`arrangement_id`) REFERENCES `arrangement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pick_fk_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pick_fk_morph` FOREIGN KEY (`morph_id`) REFERENCES `morph` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pick`
--

LOCK TABLES `pick` WRITE;
/*!40000 ALTER TABLE `pick` DISABLE KEYS */;
/*!40000 ALTER TABLE `pick` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point`
--

DROP TABLE IF EXISTS `point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `point` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `morph_id` bigint(20) unsigned NOT NULL,
  `voice_event_id` bigint(20) unsigned NOT NULL,
  `position` float unsigned NOT NULL,
  `note` varchar(63) NOT NULL,
  `duration` float unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `point_fk_morph_idx` (`morph_id`),
  KEY `point_fk_voice_event_idx` (`voice_event_id`),
  CONSTRAINT `point_fk_morph` FOREIGN KEY (`morph_id`) REFERENCES `morph` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `point_fk_voice_event` FOREIGN KEY (`voice_event_id`) REFERENCES `voice_event` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point`
--

LOCK TABLES `point` WRITE;
/*!40000 ALTER TABLE `point` DISABLE KEYS */;
/*!40000 ALTER TABLE `point` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','user auth','SQL','V1__user_auth.sql',447090788,'ebroot','2017-02-04 17:36:22',142,1),(2,'2','account','SQL','V2__account.sql',-728725086,'ebroot','2017-02-04 17:36:23',117,1),(3,'3','credit','SQL','V3__credit.sql',-385750700,'ebroot','2017-02-04 17:36:23',54,1),(4,'4','library idea phase meme voice event','SQL','V4__library_idea_phase_meme_voice_event.sql',-1534808241,'ebroot','2017-02-04 17:36:23',387,1),(5,'5','instrument meme audio chord event','SQL','V5__instrument_meme_audio_chord_event.sql',-1907897642,'ebroot','2017-02-04 17:36:23',226,1),(6,'6','chain link chord choice','SQL','V6__chain_link_chord_choice.sql',-2093488888,'ebroot','2017-02-04 17:36:24',525,1),(7,'7','arrangement morph point pick','SQL','V7__arrangement_morph_point_pick.sql',-1775760070,'ebroot','2017-02-04 17:36:24',162,1),(8,'8','user auth column renaming','SQL','V8__user_auth_column_renaming.sql',-1774157694,'ebroot','2017-02-04 17:36:24',64,1),(9,'9','user role','SQL','V9__user_role.sql',-2040912989,'ebroot','2017-02-04 17:36:24',51,1),(10,'10','user access token','SQL','V10__user_access_token.sql',-1589285188,'ebroot','2017-02-04 17:36:24',36,1),(11,'11','user auth column renaming','SQL','V11__user_auth_column_renaming.sql',342405360,'ebroot','2017-02-04 17:36:24',13,1),(12,'12','RENAME account user TO account user role','SQL','V12__RENAME_account_user_TO_account_user_role.sql',569433197,'ebroot','2017-02-04 17:36:24',48,1),(13,'14','ALTER user DROP COLUMN admin','SQL','V14__ALTER_user_DROP_COLUMN_admin.sql',660577316,'ebroot','2017-02-04 17:36:25',54,1),(14,'15','ALTER account ADD COLUMN name','SQL','V15__ALTER_account_ADD_COLUMN_name.sql',2013415455,'ebroot','2017-02-04 17:36:25',54,1),(15,'16','ALTER library ADD COLUMN name','SQL','V16__ALTER_library_ADD_COLUMN_name.sql',652666977,'ebroot','2017-02-04 17:36:25',48,1),(16,'17','RENAME ALTER account user role TO account user','SQL','V17__RENAME_ALTER_account_user_role_TO_account_user.sql',-527669089,'ebroot','2017-02-04 17:36:25',89,1),(17,'18','ALTER chain BELONGS TO account HAS MANY library','SQL','V18__ALTER_chain_BELONGS_TO_account_HAS_MANY_library.sql',407528039,'ebroot','2017-02-04 17:36:25',130,1),(18,'19','DROP credit ALTER idea instrument belong directly to user','SQL','V19__DROP_credit_ALTER_idea_instrument_belong_directly_to_user.sql',-940090323,'ebroot','2017-02-04 17:36:25',382,1),(19,'20','ALTER phase choice BIGINT offset total','SQL','V20__ALTER_phase_choice_BIGINT_offset_total.sql',1174421309,'ebroot','2017-02-04 17:36:26',241,1),(20,'21','ALTER DROP order FORM instrument idea phase meme','SQL','V21__ALTER_DROP_order_FORM_instrument_idea_phase_meme.sql',-825269746,'ebroot','2017-02-04 17:36:26',143,1),(21,'22','ALTER phase optional values','SQL','V22__ALTER_phase_optional_values.sql',2115016285,'ebroot','2017-02-05 23:06:15',315,1),(22,'23','ALTER audio COLUMNS waveformUrl','SQL','V23__ALTER_audio_COLUMNS_waveformUrl.sql',-1407515541,'ebroot','2017-02-07 03:21:14',29,1),(23,'24','ALTER audio FLOAT start length','SQL','V24__ALTER_audio_FLOAT_start_length.sql',-2000888804,'ebroot','2017-02-07 03:21:14',125,1),(24,'25','ALTER chain ADD COLUMNS name state startat stopat','SQL','V25__ALTER_chain_ADD_COLUMNS_name_state_startat_stopat.sql',1356557345,'ebroot','2017-02-10 00:03:21',205,1),(25,'26','ALTER link FLOAT start finish','SQL','V26__ALTER_link_FLOAT_start_finish.sql',-1185447213,'ebroot','2017-02-10 00:03:21',107,1),(26,'27','ALTER all tables ADD COLUMN createdat updatedat','SQL','V27__ALTER_all_tables_ADD_COLUMN_createdat_updatedat.sql',-794640015,'ebroot','2017-02-10 00:03:25',3684,1),(27,'28','ALTER chain link TIMESTAMP microsecond precision','SQL','V28__ALTER_chain_link_TIMESTAMP_microsecond_precision.sql',-1850945451,'ebroot','2017-02-13 19:04:58',239,1),(28,'29','ALTER arrangement DROP COLUMNS name density tempo','SQL','V29__ALTER_arrangement_DROP_COLUMNS_name_density_tempo.sql',-1660342705,'ebroot','2017-02-14 04:55:49',175,1),(29,'30','ALTER pick FLOAT start length','SQL','V30__ALTER_pick_FLOAT_start_length.sql',-1842518453,'ebroot','2017-02-14 04:55:50',126,1),(30,'31','ALTER pick ADD BELONGS TO arrangement','SQL','V31__ALTER_pick_ADD_BELONGS_TO_arrangement.sql',1953331613,'ebroot','2017-02-14 04:55:50',139,1),(31,'32','ALTER link OPTIONAL total density key tempo','SQL','V32__ALTER_link_OPTIONAL_total_density_key_tempo.sql',-98188439,'ebroot','2017-02-19 22:29:51',207,1),(32,'33','ALTER link UNIQUE chain offset','SQL','V33__ALTER_link_UNIQUE_chain_offset.sql',1398816976,'ebroot','2017-02-19 22:29:51',29,1),(33,'34','ALTER audio COLUMNS waveformKey','SQL','V34__ALTER_audio_COLUMNS_waveformKey.sql',66858661,'ebroot','2017-04-21 16:24:11',40,1),(34,'35','CREATE TABLE chain config','SQL','V35__CREATE_TABLE_chain_config.sql',-2134731909,'ebroot','2017-04-28 14:57:19',58,1),(35,'36','CREATE TABLE chain idea','SQL','V36__CREATE_TABLE_chain_idea.sql',2038472760,'ebroot','2017-04-28 14:57:19',52,1),(36,'37','CREATE TABLE chain instrument','SQL','V37__CREATE_TABLE_chain_instrument.sql',1486524130,'ebroot','2017-04-28 14:57:19',53,1),(37,'38','ALTER chain ADD COLUMN type','SQL','V38__ALTER_chain_ADD_COLUMN_type.sql',608321610,'ebroot','2017-04-28 14:57:19',78,1),(38,'39','ALTER phase MODIFY COLUMN total No Longer Required','SQL','V39__ALTER_phase_MODIFY_COLUMN_total_No_Longer_Required.sql',-1504223876,'ebroot','2017-05-01 19:09:45',95,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Charney Kaye','charneykaye@gmail.com','https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'Chris Luken','christopher.luken@gmail.com','https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'David Cole','davecolemusic@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-03-08 02:26:51','2017-03-08 02:26:51'),(4,'Shannon Holloway','shannon.holloway@gmail.com','https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50','2017-03-08 18:14:53','2017-03-08 18:14:53'),(5,'Lev Kaye','lev@kaye.com','https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50','2017-03-09 23:47:12','2017-03-09 23:47:12'),(6,'Justin Knowlden (gus)','gus@gusg.us','https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50','2017-04-14 20:41:41','2017-04-14 20:41:41'),(7,'dave farkas','sakrafd@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-14 20:42:36','2017-04-14 20:42:36'),(8,'Aji Putra','aji.perdana.putra@gmail.com','https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50','2017-04-21 17:33:25','2017-04-21 17:33:25'),(9,'live espn789','scoreplace@gmail.com','https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50','2017-04-21 19:13:22','2017-04-21 19:13:22');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'user',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(2,'admin',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,'user',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,'artist',1,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(5,'artist',2,'2017-02-10 00:03:24','2017-02-10 00:03:24'),(6,'user',3,'2017-03-08 02:26:51','2017-03-08 02:26:51'),(7,'artist',3,'2017-03-08 02:27:15','2017-03-08 02:27:15'),(8,'user',4,'2017-03-08 18:14:53','2017-03-08 18:14:53'),(9,'artist',4,'2017-03-09 17:48:55','2017-03-09 17:48:55'),(10,'user',5,'2017-03-09 23:47:12','2017-03-09 23:47:12'),(11,'artist',5,'2017-03-10 05:39:23','2017-03-10 05:39:23'),(12,'user',6,'2017-04-14 20:41:41','2017-04-14 20:41:41'),(13,'user',7,'2017-04-14 20:42:36','2017-04-14 20:42:36'),(14,'artist',6,'2017-04-17 20:59:16','2017-04-17 20:59:16'),(15,'artist',7,'2017-04-17 20:59:21','2017-04-17 20:59:21'),(16,'user',8,'2017-04-21 17:33:25','2017-04-21 17:33:25'),(17,'user',9,'2017-04-21 19:13:22','2017-04-21 19:13:22');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voice`
--

LOCK TABLES `voice` WRITE;
/*!40000 ALTER TABLE `voice` DISABLE KEYS */;
INSERT INTO `voice` VALUES (2,2,'percussive','Drums','2017-02-10 00:03:24','2017-02-10 00:03:24'),(3,2,'harmonic','Bass','2017-02-10 00:03:24','2017-02-10 00:03:24'),(4,3,'percussive','Drums','2017-04-23 23:45:07','2017-04-30 15:51:16');
/*!40000 ALTER TABLE `voice` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=268 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voice_event`
--

LOCK TABLES `voice_event` WRITE;
/*!40000 ALTER TABLE `voice_event` DISABLE KEYS */;
INSERT INTO `voice_event` VALUES (1,4,1,1,'KICK',0,1,'C2','2017-04-23 23:46:55','2017-04-23 23:46:55'),(2,4,1,1,'KICK',2,1,'C2','2017-04-23 23:47:28','2017-04-23 23:47:28'),(3,4,1,1,'KICK',3,1,'C2','2017-04-23 23:49:55','2017-04-23 23:49:55'),(4,4,1,1,'KICK',4,1,'C2','2017-04-23 23:49:55','2017-04-23 23:49:55'),(5,4,1,1,'KICK',5,1,'C2','2017-04-23 23:49:56','2017-04-23 23:49:56'),(6,4,1,1,'KICK',6,1,'C2','2017-04-23 23:49:56','2017-04-23 23:49:56'),(7,4,1,1,'KICK',7,1,'C2','2017-04-23 23:49:56','2017-04-23 23:49:56'),(8,4,1,1,'KICK',8,1,'C2','2017-04-23 23:49:57','2017-04-23 23:49:57'),(9,4,1,1,'KICK',9,1,'C2','2017-04-23 23:49:57','2017-04-23 23:49:57'),(10,4,1,1,'KICK',10,1,'C2','2017-04-23 23:49:57','2017-04-23 23:49:57'),(11,4,1,1,'KICK',11,1,'C2','2017-04-23 23:49:57','2017-04-23 23:49:57'),(12,4,1,1,'KICK',12,1,'C2','2017-04-23 23:49:58','2017-04-23 23:49:58'),(13,4,1,1,'KICK',13,1,'C2','2017-04-23 23:49:58','2017-04-23 23:49:58'),(14,4,1,1,'KICK',14,1,'C2','2017-04-23 23:49:58','2017-04-23 23:49:58'),(15,4,1,1,'KICK',15,1,'C2','2017-04-23 23:49:58','2017-04-23 23:49:58'),(16,4,1,1,'KICK',16,1,'C2','2017-04-23 23:49:59','2017-04-23 23:49:59'),(17,4,1,1,'KICK',17,1,'C2','2017-04-23 23:49:59','2017-04-23 23:49:59'),(18,4,1,1,'KICK',18,1,'C2','2017-04-23 23:49:59','2017-04-23 23:49:59'),(19,4,1,1,'KICK',19,1,'C2','2017-04-23 23:50:00','2017-04-23 23:50:00'),(20,4,1,1,'KICK',20,1,'C2','2017-04-23 23:50:01','2017-04-23 23:50:01'),(21,4,1,1,'KICK',21,1,'C2','2017-04-23 23:50:01','2017-04-23 23:50:01'),(22,4,1,1,'KICK',22,1,'C2','2017-04-23 23:50:01','2017-04-23 23:50:01'),(23,4,1,1,'KICK',23,1,'C2','2017-04-23 23:50:02','2017-04-23 23:50:02'),(24,4,1,1,'KICK',24,1,'C2','2017-04-23 23:50:02','2017-04-23 23:50:02'),(25,4,1,1,'KICK',25,1,'C2','2017-04-23 23:50:02','2017-04-23 23:50:02'),(26,4,1,1,'KICK',26,1,'C2','2017-04-23 23:50:03','2017-04-23 23:50:03'),(27,4,1,1,'KICK',27,1,'C2','2017-04-23 23:50:03','2017-04-23 23:50:03'),(28,4,1,1,'KICK',28,1,'C2','2017-04-23 23:50:03','2017-04-23 23:50:03'),(29,4,1,1,'KICK',29,1,'C2','2017-04-23 23:50:04','2017-04-23 23:50:04'),(30,4,1,1,'KICK',30,1,'C2','2017-04-23 23:50:04','2017-04-23 23:50:04'),(31,4,1,1,'KICK',31,1,'C2','2017-04-23 23:50:04','2017-04-23 23:50:04'),(32,4,1,1,'KICK',32,1,'C2','2017-04-23 23:50:04','2017-04-23 23:50:04'),(33,4,1,1,'KICK',33,1,'C2','2017-04-23 23:50:05','2017-04-23 23:50:05'),(34,4,1,1,'KICK',34,1,'C2','2017-04-23 23:50:05','2017-04-23 23:50:05'),(35,4,1,1,'KICK',35,1,'C2','2017-04-23 23:50:05','2017-04-23 23:50:05'),(36,4,1,1,'KICK',36,1,'C2','2017-04-23 23:50:06','2017-04-23 23:50:06'),(37,4,1,1,'KICK',37,1,'C2','2017-04-23 23:50:06','2017-04-23 23:50:06'),(38,4,1,1,'KICK',38,1,'C2','2017-04-23 23:50:06','2017-04-23 23:50:06'),(39,4,1,1,'KICK',39,1,'C2','2017-04-23 23:50:07','2017-04-23 23:50:07'),(40,4,1,1,'KICK',40,1,'C2','2017-04-23 23:50:07','2017-04-23 23:50:07'),(41,4,1,1,'KICK',41,1,'C2','2017-04-23 23:50:07','2017-04-23 23:50:07'),(42,4,1,1,'KICK',42,1,'C2','2017-04-23 23:50:07','2017-04-23 23:50:07'),(43,4,1,1,'KICK',43,1,'C2','2017-04-23 23:50:08','2017-04-23 23:50:08'),(44,4,1,1,'KICK',44,1,'C2','2017-04-23 23:50:08','2017-04-23 23:50:08'),(45,4,1,1,'KICK',45,1,'C2','2017-04-23 23:50:08','2017-04-23 23:50:08'),(46,4,1,1,'KICK',46,1,'C2','2017-04-23 23:50:09','2017-04-23 23:50:09'),(47,4,1,1,'KICK',47,1,'C2','2017-04-23 23:50:09','2017-04-23 23:50:09'),(48,4,1,1,'KICK',48,1,'C2','2017-04-23 23:50:09','2017-04-23 23:50:09'),(49,4,1,1,'KICK',49,1,'C2','2017-04-23 23:50:09','2017-04-23 23:50:09'),(50,4,1,1,'KICK',50,1,'C2','2017-04-23 23:50:10','2017-04-23 23:50:10'),(51,4,1,1,'KICK',51,1,'C2','2017-04-23 23:50:10','2017-04-23 23:50:10'),(52,4,1,1,'KICK',52,1,'C2','2017-04-23 23:50:10','2017-04-23 23:50:10'),(53,4,1,1,'KICK',53,1,'C2','2017-04-23 23:50:11','2017-04-23 23:50:11'),(54,4,1,1,'KICK',54,1,'C2','2017-04-23 23:50:11','2017-04-23 23:50:11'),(55,4,1,1,'KICK',55,1,'C2','2017-04-23 23:50:11','2017-04-23 23:50:11'),(56,4,1,1,'KICK',56,1,'C2','2017-04-23 23:50:12','2017-04-23 23:50:12'),(57,4,1,1,'KICK',57,1,'C2','2017-04-23 23:50:12','2017-04-23 23:50:12'),(58,4,1,1,'KICK',58,1,'C2','2017-04-23 23:50:12','2017-04-23 23:50:12'),(59,4,1,1,'KICK',59,1,'C2','2017-04-23 23:50:12','2017-04-23 23:50:12'),(60,4,1,1,'KICK',60,1,'C2','2017-04-23 23:50:13','2017-04-23 23:50:13'),(61,4,1,1,'KICK',61,1,'C2','2017-04-23 23:50:13','2017-04-23 23:50:13'),(62,4,1,1,'KICK',62,1,'C2','2017-04-23 23:50:13','2017-04-23 23:50:13'),(63,4,1,1,'KICK',63,1,'C2','2017-04-23 23:50:14','2017-04-23 23:50:14'),(64,4,1,1,'KICK',1,1,'C2','2017-04-23 23:50:27','2017-04-23 23:50:27'),(67,4,1,0.4,'SNARE',63,1,'G2','2017-04-23 23:52:34','2017-04-23 23:52:34'),(68,4,1,0.4,'SNARE',1,1,'G2','2017-04-23 23:53:32','2017-04-23 23:53:32'),(69,4,1,0.4,'SNARE',3,1,'G2','2017-04-23 23:53:32','2017-04-23 23:53:32'),(70,4,1,0.4,'SNARE',5,1,'G2','2017-04-23 23:53:32','2017-04-23 23:53:32'),(71,4,1,0.4,'SNARE',7,1,'G2','2017-04-23 23:53:33','2017-04-23 23:53:33'),(72,4,1,0.4,'SNARE',9,1,'G2','2017-04-23 23:53:33','2017-04-23 23:53:33'),(73,4,1,0.4,'SNARE',11,1,'G2','2017-04-23 23:53:33','2017-04-23 23:53:33'),(74,4,1,0.4,'SNARE',13,1,'G2','2017-04-23 23:53:34','2017-04-23 23:53:34'),(75,4,1,0.4,'SNARE',15,1,'G2','2017-04-23 23:53:34','2017-04-23 23:53:34'),(76,4,1,0.4,'SNARE',17,1,'G2','2017-04-23 23:53:34','2017-04-23 23:53:34'),(77,4,1,0.4,'SNARE',19,1,'G2','2017-04-23 23:53:35','2017-04-23 23:53:35'),(78,4,1,0.4,'SNARE',21,1,'G2','2017-04-23 23:53:35','2017-04-23 23:53:35'),(79,4,1,0.4,'SNARE',23,1,'G2','2017-04-23 23:53:35','2017-04-23 23:53:35'),(80,4,1,0.4,'SNARE',25,1,'G2','2017-04-23 23:53:36','2017-04-23 23:53:36'),(81,4,1,0.4,'SNARE',27,1,'G2','2017-04-23 23:53:36','2017-04-23 23:53:36'),(82,4,1,0.4,'SNARE',29,1,'G2','2017-04-23 23:53:36','2017-04-23 23:53:36'),(83,4,1,0.4,'SNARE',31,1,'G2','2017-04-23 23:53:37','2017-04-23 23:53:37'),(84,4,1,0.4,'SNARE',33,1,'G2','2017-04-23 23:53:37','2017-04-23 23:53:37'),(85,4,1,0.4,'SNARE',35,1,'G2','2017-04-23 23:53:37','2017-04-23 23:53:37'),(86,4,1,0.4,'SNARE',37,1,'G2','2017-04-23 23:53:37','2017-04-23 23:53:37'),(87,4,1,0.4,'SNARE',39,1,'G2','2017-04-23 23:53:38','2017-04-23 23:53:38'),(88,4,1,0.4,'SNARE',41,1,'G2','2017-04-23 23:53:38','2017-04-23 23:53:38'),(89,4,1,0.4,'SNARE',43,1,'G2','2017-04-23 23:53:38','2017-04-23 23:53:38'),(90,4,1,0.4,'SNARE',45,1,'G2','2017-04-23 23:53:39','2017-04-23 23:53:39'),(91,4,1,0.4,'SNARE',47,1,'G2','2017-04-23 23:53:39','2017-04-23 23:53:39'),(92,4,1,0.4,'SNARE',49,1,'G2','2017-04-23 23:53:39','2017-04-23 23:53:39'),(93,4,1,0.4,'SNARE',51,1,'G2','2017-04-23 23:53:40','2017-04-23 23:53:40'),(94,4,1,0.4,'SNARE',53,1,'G2','2017-04-23 23:53:40','2017-04-23 23:53:40'),(95,4,1,0.4,'SNARE',55,1,'G2','2017-04-23 23:53:40','2017-04-23 23:53:40'),(96,4,1,0.4,'SNARE',57,1,'G2','2017-04-23 23:53:40','2017-04-23 23:53:40'),(97,4,1,0.4,'SNARE',59,1,'G2','2017-04-23 23:53:41','2017-04-23 23:53:41'),(98,4,1,0.4,'SNARE',61,1,'G2','2017-04-23 23:53:41','2017-04-23 23:53:41'),(100,4,0.6,0.2,'HATOPEN',0.5,0.5,'E2','2017-04-23 23:56:19','2017-04-24 02:45:04'),(101,4,0.6,0.2,'HATOPEN',1.5,0.5,'E2','2017-04-23 23:56:19','2017-04-24 02:45:04'),(102,4,0.6,0.2,'HATOPEN',2.5,0.5,'E2','2017-04-23 23:56:19','2017-04-24 02:45:04'),(103,4,0.6,0.2,'HATOPEN',3.5,0.5,'E2','2017-04-23 23:56:19','2017-04-24 02:45:04'),(104,4,0.6,0.2,'HATOPEN',4.5,0.5,'E2','2017-04-23 23:56:20','2017-04-24 02:45:04'),(105,4,0.6,0.2,'HATOPEN',5.5,0.5,'E2','2017-04-23 23:56:20','2017-04-24 02:45:04'),(106,4,0.6,0.2,'HATOPEN',6.5,0.5,'E2','2017-04-23 23:56:20','2017-04-24 02:45:04'),(107,4,0.6,0.2,'HATOPEN',7.5,0.5,'E2','2017-04-23 23:56:20','2017-04-24 02:45:04'),(108,4,0.6,0.2,'HATOPEN',8.5,0.5,'E2','2017-04-23 23:56:21','2017-04-24 02:45:04'),(109,4,0.6,0.2,'HATOPEN',9.5,0.5,'E2','2017-04-23 23:56:21','2017-04-24 02:45:04'),(110,4,0.6,0.2,'HATOPEN',10.5,0.5,'E2','2017-04-23 23:56:21','2017-04-24 02:45:04'),(111,4,0.6,0.2,'HATOPEN',11.5,0.5,'E2','2017-04-23 23:56:22','2017-04-24 02:45:04'),(112,4,0.6,0.2,'HATOPEN',12.5,0.5,'E2','2017-04-23 23:56:22','2017-04-24 02:45:04'),(113,4,0.6,0.2,'HATOPEN',13.5,0.5,'E2','2017-04-23 23:56:22','2017-04-24 02:45:04'),(114,4,0.6,0.2,'HATOPEN',14.5,0.5,'E2','2017-04-23 23:56:23','2017-04-24 02:45:04'),(115,4,0.6,0.2,'HATOPEN',15.5,0.5,'E2','2017-04-23 23:56:23','2017-04-24 02:45:04'),(116,4,0.6,0.2,'HATOPEN',16.5,0.5,'E2','2017-04-23 23:56:23','2017-04-24 02:45:04'),(117,4,0.6,0.2,'HATOPEN',17.5,0.5,'E2','2017-04-23 23:56:23','2017-04-24 02:45:04'),(118,4,0.6,0.2,'HATOPEN',18.5,0.5,'E2','2017-04-23 23:56:24','2017-04-24 02:45:04'),(119,4,0.6,0.2,'HATOPEN',19.5,0.5,'E2','2017-04-23 23:56:24','2017-04-24 02:45:04'),(120,4,0.6,0.2,'HATOPEN',20.5,0.5,'E2','2017-04-23 23:56:24','2017-04-24 02:45:04'),(121,4,0.6,0.2,'HATOPEN',21.5,0.5,'E2','2017-04-23 23:56:25','2017-04-24 02:45:04'),(122,4,0.6,0.2,'HATOPEN',22.5,0.5,'E2','2017-04-23 23:56:25','2017-04-24 02:45:04'),(123,4,0.6,0.2,'HATOPEN',23.5,0.5,'E2','2017-04-23 23:56:25','2017-04-24 02:45:04'),(124,4,0.6,0.2,'HATOPEN',24.5,0.5,'E2','2017-04-23 23:56:25','2017-04-24 02:45:04'),(125,4,0.6,0.2,'HATOPEN',25.5,0.5,'E2','2017-04-23 23:56:26','2017-04-24 02:45:04'),(126,4,0.6,0.2,'HATOPEN',26.5,0.5,'E2','2017-04-23 23:56:26','2017-04-24 02:45:04'),(127,4,0.6,0.2,'HATOPEN',27.5,0.5,'E2','2017-04-23 23:56:26','2017-04-24 02:45:04'),(128,4,0.6,0.2,'HATOPEN',28.5,0.5,'E2','2017-04-23 23:56:26','2017-04-24 02:45:04'),(129,4,0.6,0.2,'HATOPEN',29.5,0.5,'E2','2017-04-23 23:56:27','2017-04-24 02:45:04'),(130,4,0.6,0.2,'HATOPEN',30.5,0.5,'E2','2017-04-23 23:56:27','2017-04-24 02:45:04'),(131,4,0.6,0.2,'HATOPEN',31.5,0.5,'E2','2017-04-23 23:56:27','2017-04-24 02:45:04'),(132,4,0.6,0.2,'HATOPEN',32.5,0.5,'E2','2017-04-23 23:56:28','2017-04-24 02:45:04'),(133,4,0.6,0.2,'HATOPEN',33.5,0.5,'E2','2017-04-23 23:56:28','2017-04-24 02:45:04'),(134,4,0.6,0.2,'HATOPEN',34.5,0.5,'E2','2017-04-23 23:56:28','2017-04-24 02:45:04'),(135,4,0.6,0.2,'HATOPEN',35.5,0.5,'E2','2017-04-23 23:56:28','2017-04-24 02:45:04'),(136,4,0.6,0.2,'HATOPEN',36.5,0.5,'E2','2017-04-23 23:56:29','2017-04-24 02:45:04'),(137,4,0.6,0.2,'HATOPEN',37.5,0.5,'E2','2017-04-23 23:56:29','2017-04-24 02:45:04'),(138,4,0.6,0.2,'HATOPEN',38.5,0.5,'E2','2017-04-23 23:56:29','2017-04-24 02:45:04'),(139,4,0.6,0.2,'HATOPEN',39.5,0.5,'E2','2017-04-23 23:56:30','2017-04-24 02:45:04'),(140,4,0.6,0.2,'HATOPEN',40.5,0.5,'E2','2017-04-23 23:56:30','2017-04-24 02:45:04'),(141,4,0.6,0.2,'HATOPEN',41.5,0.5,'E2','2017-04-23 23:56:30','2017-04-24 02:45:04'),(142,4,0.6,0.2,'HATOPEN',42.5,0.5,'E2','2017-04-23 23:56:30','2017-04-24 02:45:04'),(143,4,0.6,0.2,'HATOPEN',43.5,0.5,'E2','2017-04-23 23:56:31','2017-04-24 02:45:04'),(144,4,0.6,0.2,'HATOPEN',44.5,0.5,'E2','2017-04-23 23:56:31','2017-04-24 02:45:04'),(145,4,0.6,0.2,'HATOPEN',45.5,0.5,'E2','2017-04-23 23:56:31','2017-04-24 02:45:04'),(146,4,0.6,0.2,'HATOPEN',46.5,0.5,'E2','2017-04-23 23:56:31','2017-04-24 02:45:04'),(147,4,0.6,0.2,'HATOPEN',47.5,0.5,'E2','2017-04-23 23:56:32','2017-04-24 02:45:04'),(148,4,0.6,0.2,'HATOPEN',48.5,0.5,'E2','2017-04-23 23:56:32','2017-04-24 02:45:04'),(149,4,0.6,0.2,'HATOPEN',49.5,0.5,'E2','2017-04-23 23:56:32','2017-04-24 02:45:04'),(150,4,0.6,0.2,'HATOPEN',50.5,0.5,'E2','2017-04-23 23:56:33','2017-04-24 02:45:04'),(151,4,0.6,0.2,'HATOPEN',51.5,0.5,'E2','2017-04-23 23:56:33','2017-04-24 02:45:04'),(152,4,0.6,0.2,'HATOPEN',52.5,0.5,'E2','2017-04-23 23:56:33','2017-04-24 02:45:04'),(153,4,0.6,0.2,'HATOPEN',53.5,0.5,'E2','2017-04-23 23:56:34','2017-04-24 02:45:04'),(154,4,0.6,0.2,'HATOPEN',54.5,0.5,'E2','2017-04-23 23:56:34','2017-04-24 02:45:04'),(155,4,0.6,0.2,'HATOPEN',55.5,0.5,'E2','2017-04-23 23:56:34','2017-04-24 02:45:04'),(156,4,0.6,0.2,'HATOPEN',56.5,0.5,'E2','2017-04-23 23:56:34','2017-04-24 02:45:04'),(157,4,0.6,0.2,'HATOPEN',57.5,0.5,'E2','2017-04-23 23:56:35','2017-04-24 02:45:04'),(158,4,0.6,0.2,'HATOPEN',58.5,0.5,'E2','2017-04-23 23:56:35','2017-04-24 02:45:04'),(159,4,0.6,0.2,'HATOPEN',59.5,0.5,'E2','2017-04-23 23:56:35','2017-04-24 02:45:04'),(160,4,0.6,0.2,'HATOPEN',60.5,0.5,'E2','2017-04-23 23:56:35','2017-04-24 02:45:04'),(161,4,0.6,0.2,'HATOPEN',61.5,0.5,'E2','2017-04-23 23:56:36','2017-04-24 02:45:04'),(162,4,0.6,0.2,'HATOPEN',62.5,0.5,'E2','2017-04-23 23:56:36','2017-04-24 02:45:04'),(164,4,0.3,0.1,'HATCLOSED',0.25,0.25,'D2','2017-04-24 02:46:20','2017-04-24 02:46:20'),(166,4,0.3,0.1,'HATCLOSED',2.25,0.25,'D2','2017-04-24 02:46:21','2017-04-24 02:46:21'),(167,4,0.3,0.1,'HATCLOSED',3.25,0.25,'D2','2017-04-24 02:46:22','2017-04-24 02:46:22'),(168,4,1,0.1,'CYMBALCRASH',0,4,'C2','2017-04-24 02:46:22','2017-04-24 03:08:15'),(169,4,0.3,0.1,'HATCLOSED',5.25,0.25,'D2','2017-04-24 02:46:23','2017-04-24 02:46:23'),(170,4,0.3,0.1,'HATCLOSED',6.25,0.25,'D2','2017-04-24 02:46:23','2017-04-24 02:46:23'),(171,4,0.3,0.1,'HATCLOSED',7.25,0.25,'D2','2017-04-24 02:46:24','2017-04-24 02:46:24'),(172,4,0.3,0.1,'HATCLOSED',8.25,0.25,'D2','2017-04-24 02:46:24','2017-04-24 02:46:24'),(173,4,0.3,0.1,'HATCLOSED',9.25,0.25,'D2','2017-04-24 02:46:25','2017-04-24 02:46:25'),(174,4,0.3,0.1,'HATCLOSED',10.25,0.25,'D2','2017-04-24 02:46:25','2017-04-24 02:46:25'),(175,4,0.3,0.1,'HATCLOSED',11.25,0.25,'D2','2017-04-24 02:46:26','2017-04-24 02:46:26'),(176,4,0.3,0.1,'HATCLOSED',12.25,0.25,'D2','2017-04-24 02:46:27','2017-04-24 02:46:27'),(177,4,0.3,0.1,'HATCLOSED',13.25,0.25,'D2','2017-04-24 02:46:27','2017-04-24 02:46:27'),(178,4,0.3,0.1,'HATCLOSED',14.25,0.25,'D2','2017-04-24 02:46:28','2017-04-24 02:46:28'),(179,4,0.3,0.1,'HATCLOSED',15.25,0.25,'D2','2017-04-24 02:46:28','2017-04-24 02:46:28'),(180,4,0.3,0.1,'HATCLOSED',16.25,0.25,'D2','2017-04-24 02:46:29','2017-04-24 02:46:29'),(181,4,0.3,0.1,'HATCLOSED',17.25,0.25,'D2','2017-04-24 02:46:29','2017-04-24 02:46:29'),(182,4,0.3,0.1,'HATCLOSED',18.25,0.25,'D2','2017-04-24 02:46:30','2017-04-24 02:46:30'),(183,4,0.3,0.1,'HATCLOSED',19.25,0.25,'D2','2017-04-24 02:46:30','2017-04-24 02:46:30'),(184,4,0.3,0.1,'HATCLOSED',20.25,0.25,'D2','2017-04-24 02:46:31','2017-04-24 02:46:31'),(185,4,0.3,0.1,'HATCLOSED',21.25,0.25,'D2','2017-04-24 02:46:31','2017-04-24 02:46:31'),(186,4,0.3,0.1,'HATCLOSED',22.25,0.25,'D2','2017-04-24 02:46:32','2017-04-24 02:46:32'),(187,4,0.3,0.1,'HATCLOSED',23.25,0.25,'D2','2017-04-24 02:46:33','2017-04-24 02:46:33'),(188,4,0.3,0.1,'HATCLOSED',24.25,0.25,'D2','2017-04-24 02:46:33','2017-04-24 02:46:33'),(189,4,0.3,0.1,'HATCLOSED',25.25,0.25,'D2','2017-04-24 02:46:34','2017-04-24 02:46:34'),(190,4,0.3,0.1,'HATCLOSED',26.25,0.25,'D2','2017-04-24 02:46:34','2017-04-24 02:46:34'),(191,4,0.3,0.1,'HATCLOSED',27.25,0.25,'D2','2017-04-24 02:46:35','2017-04-24 02:46:35'),(192,4,0.3,0.1,'HATCLOSED',28.25,0.25,'D2','2017-04-24 02:46:35','2017-04-24 02:46:35'),(193,4,0.3,0.1,'HATCLOSED',29.25,0.25,'D2','2017-04-24 02:46:36','2017-04-24 02:46:36'),(194,4,0.3,0.1,'HATCLOSED',30.25,0.25,'D2','2017-04-24 02:46:36','2017-04-24 02:46:36'),(195,4,0.3,0.1,'HATCLOSED',31.25,0.25,'D2','2017-04-24 02:46:37','2017-04-24 02:46:37'),(196,4,0.3,0.1,'HATCLOSED',32.25,0.25,'D2','2017-04-24 02:46:38','2017-04-24 02:46:38'),(197,4,0.3,0.1,'HATCLOSED',33.25,0.25,'D2','2017-04-24 02:46:38','2017-04-24 02:46:38'),(198,4,0.3,0.1,'HATCLOSED',34.25,0.25,'D2','2017-04-24 02:46:39','2017-04-24 02:46:39'),(199,4,0.3,0.1,'HATCLOSED',35.25,0.25,'D2','2017-04-24 02:46:39','2017-04-24 02:46:39'),(200,4,0.3,0.1,'HATCLOSED',36.25,0.25,'D2','2017-04-24 02:46:40','2017-04-24 02:46:40'),(201,4,0.3,0.1,'HATCLOSED',37.25,0.25,'D2','2017-04-24 02:46:40','2017-04-24 02:46:40'),(202,4,0.3,0.1,'HATCLOSED',38.25,0.25,'D2','2017-04-24 02:46:41','2017-04-24 02:46:41'),(203,4,0.3,0.1,'HATCLOSED',39.25,0.25,'D2','2017-04-24 02:46:41','2017-04-24 02:46:41'),(204,4,0.3,0.1,'HATCLOSED',40.25,0.25,'D2','2017-04-24 02:46:42','2017-04-24 02:46:42'),(205,4,0.3,0.1,'HATCLOSED',41.25,0.25,'D2','2017-04-24 02:46:43','2017-04-24 02:46:43'),(206,4,0.3,0.1,'HATCLOSED',42.25,0.25,'D2','2017-04-24 02:46:43','2017-04-24 02:46:43'),(207,4,0.3,0.1,'HATCLOSED',43.25,0.25,'D2','2017-04-24 02:46:44','2017-04-24 02:46:44'),(208,4,0.3,0.1,'HATCLOSED',44.25,0.25,'D2','2017-04-24 02:46:44','2017-04-24 02:46:44'),(209,4,0.3,0.1,'HATCLOSED',45.25,0.25,'D2','2017-04-24 02:46:45','2017-04-24 02:46:45'),(210,4,0.3,0.1,'HATCLOSED',46.25,0.25,'D2','2017-04-24 02:46:45','2017-04-24 02:46:45'),(211,4,0.3,0.1,'HATCLOSED',47.25,0.25,'D2','2017-04-24 02:46:46','2017-04-24 02:46:46'),(212,4,0.3,0.1,'HATCLOSED',48.25,0.25,'D2','2017-04-24 02:46:46','2017-04-24 02:46:46'),(213,4,0.3,0.1,'HATCLOSED',49.25,0.25,'D2','2017-04-24 02:46:47','2017-04-24 02:46:47'),(214,4,0.3,0.1,'HATCLOSED',50.25,0.25,'D2','2017-04-24 02:46:47','2017-04-24 02:46:47'),(215,4,0.3,0.1,'HATCLOSED',51.25,0.25,'D2','2017-04-24 02:46:48','2017-04-24 02:46:48'),(216,4,0.3,0.1,'HATCLOSED',52.25,0.25,'D2','2017-04-24 02:46:49','2017-04-24 02:46:49'),(217,4,0.3,0.1,'HATCLOSED',53.25,0.25,'D2','2017-04-24 02:46:49','2017-04-24 02:46:49'),(218,4,0.3,0.1,'HATCLOSED',54.25,0.25,'D2','2017-04-24 02:46:50','2017-04-24 02:46:50'),(219,4,0.3,0.1,'HATCLOSED',55.25,0.25,'D2','2017-04-24 02:46:50','2017-04-24 02:46:50'),(220,4,0.3,0.1,'HATCLOSED',56.25,0.25,'D2','2017-04-24 02:46:51','2017-04-24 02:46:51'),(221,4,0.3,0.1,'HATCLOSED',57.25,0.25,'D2','2017-04-24 02:46:51','2017-04-24 02:46:51'),(222,4,0.3,0.1,'HATCLOSED',58.25,0.25,'D2','2017-04-24 02:46:52','2017-04-24 02:46:52'),(223,4,0.3,0.1,'HATCLOSED',59.25,0.25,'D2','2017-04-24 02:46:52','2017-04-24 02:46:52'),(224,4,0.3,0.1,'HATCLOSED',60.25,0.25,'D2','2017-04-24 02:46:53','2017-04-24 02:46:53'),(225,4,0.3,0.1,'HATCLOSED',61.25,0.25,'D2','2017-04-24 02:46:53','2017-04-24 02:46:53'),(226,4,0.3,0.1,'HATCLOSED',62.25,0.25,'D2','2017-04-24 02:46:54','2017-04-24 02:46:54'),(228,4,0.15,0.1,'HATCLOSED',1.75,0.25,'B1','2017-04-24 02:48:50','2017-04-24 02:57:38'),(229,4,0.15,0.1,'HATCLOSED',2.75,0.25,'B1','2017-04-24 02:48:51','2017-04-24 02:48:51'),(230,4,0.15,0.1,'HATCLOSED',4.75,0.25,'B1','2017-04-24 02:48:52','2017-04-24 02:48:52'),(231,4,0.15,0.1,'HATCLOSED',6.75,0.25,'B1','2017-04-24 02:48:52','2017-04-24 02:48:52'),(232,4,0.15,0.1,'HATCLOSED',8.75,0.25,'B1','2017-04-24 02:48:53','2017-04-24 02:48:53'),(233,4,0.15,0.1,'HATCLOSED',10.75,0.25,'B1','2017-04-24 02:48:53','2017-04-24 02:48:53'),(234,4,0.15,0.1,'HATCLOSED',12.75,0.25,'B1','2017-04-24 02:48:54','2017-04-24 02:48:54'),(235,4,0.15,0.1,'HATCLOSED',14.75,0.25,'B1','2017-04-24 02:48:54','2017-04-24 02:48:54'),(236,4,0.15,0.1,'HATCLOSED',16.75,0.25,'B1','2017-04-24 02:48:55','2017-04-24 02:48:55'),(237,4,0.15,0.1,'HATCLOSED',18.75,0.25,'B1','2017-04-24 02:48:55','2017-04-24 02:48:55'),(238,4,0.15,0.1,'HATCLOSED',20.75,0.25,'B1','2017-04-24 02:48:56','2017-04-24 02:48:56'),(239,4,0.15,0.1,'HATCLOSED',22.75,0.25,'B1','2017-04-24 02:48:57','2017-04-24 02:48:57'),(240,4,0.15,0.1,'HATCLOSED',24.75,0.25,'B1','2017-04-24 02:48:57','2017-04-24 02:48:57'),(241,4,0.15,0.1,'HATCLOSED',26.75,0.25,'B1','2017-04-24 02:48:58','2017-04-24 02:48:58'),(242,4,0.15,0.1,'HATCLOSED',28.75,0.25,'B1','2017-04-24 02:48:58','2017-04-24 02:48:58'),(243,4,0.15,0.1,'HATCLOSED',30.75,0.25,'B1','2017-04-24 02:48:59','2017-04-24 02:48:59'),(244,4,0.15,0.1,'HATCLOSED',32.75,0.25,'B1','2017-04-24 02:48:59','2017-04-24 02:48:59'),(245,4,0.15,0.1,'HATCLOSED',34.75,0.25,'B1','2017-04-24 02:49:00','2017-04-24 02:49:00'),(246,4,0.15,0.1,'HATCLOSED',36.75,0.25,'B1','2017-04-24 02:49:00','2017-04-24 02:49:00'),(247,4,0.15,0.1,'HATCLOSED',38.75,0.25,'B1','2017-04-24 02:49:01','2017-04-24 02:49:01'),(248,4,0.15,0.1,'HATCLOSED',40.75,0.25,'B1','2017-04-24 02:49:02','2017-04-24 02:49:02'),(249,4,0.15,0.1,'HATCLOSED',42.75,0.25,'B1','2017-04-24 02:49:02','2017-04-24 02:49:02'),(250,4,0.15,0.1,'HATCLOSED',44.75,0.25,'B1','2017-04-24 02:49:03','2017-04-24 02:49:03'),(251,4,0.15,0.1,'HATCLOSED',46.75,0.25,'B1','2017-04-24 02:49:03','2017-04-24 02:49:03'),(252,4,0.15,0.1,'HATCLOSED',48.75,0.25,'B1','2017-04-24 02:49:04','2017-04-24 02:49:04'),(253,4,0.15,0.1,'HATCLOSED',50.75,0.25,'B1','2017-04-24 02:49:05','2017-04-24 02:49:05'),(254,4,0.15,0.1,'HATCLOSED',52.75,0.25,'B1','2017-04-24 02:49:05','2017-04-24 02:49:05'),(255,4,0.15,0.1,'HATCLOSED',54.75,0.25,'B1','2017-04-24 02:49:06','2017-04-24 02:49:06'),(256,4,0.15,0.1,'HATCLOSED',56.75,0.25,'B1','2017-04-24 02:49:06','2017-04-24 02:49:06'),(257,4,0.15,0.1,'HATCLOSED',58.75,0.25,'B1','2017-04-24 02:49:07','2017-04-24 02:49:07'),(258,4,0.15,0.1,'HATCLOSED',60.75,0.25,'B1','2017-04-24 02:49:07','2017-04-24 02:49:07'),(260,4,0.5,0.1,'CLAP',14.5,0.5,'G5','2017-04-29 01:03:41','2017-04-29 01:03:41'),(261,4,0.8,0.1,'CLAP',15,0.5,'G4','2017-04-29 01:04:55','2017-04-29 01:04:55'),(262,4,0.5,0.1,'CLAP',30.5,0.5,'G4','2017-04-30 17:41:57','2017-04-30 17:41:57'),(263,4,1,0.1,'CLAP',31,0.5,'G4','2017-04-30 17:43:12','2017-04-30 17:43:12'),(264,4,1,0.1,'CLAP',47,0.5,'G4','2017-04-30 17:43:29','2017-04-30 17:43:29'),(265,4,1,0.1,'CLAP',63,0.5,'G4','2017-04-30 17:43:40','2017-04-30 17:43:40'),(266,4,0.5,0.1,'CLAP',62.5,0.5,'G4','2017-04-30 17:43:53','2017-04-30 17:45:37'),(267,4,0.5,0.1,'CLAP',46.5,0.5,'G4','2017-04-30 17:44:07','2017-04-30 17:44:07');
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

-- Dump completed on 2017-05-01 19:54:41
