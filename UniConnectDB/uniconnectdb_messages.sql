-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: uniconnectdb
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `conver_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `message_text` varchar(255) NOT NULL,
  `date_sent` datetime NOT NULL,
  PRIMARY KEY (`message_id`),
  KEY `conversation_id_idx` (`conver_id`),
  KEY `sender_id_idx` (`sender_id`),
  CONSTRAINT `conver_id` FOREIGN KEY (`conver_id`) REFERENCES `conversations` (`conversation_id`),
  CONSTRAINT `sender_id` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stores messages from all chats';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,1,1,'Hi Me','2025-08-17 20:52:10'),(2,1,6,'Hi Nick','2025-08-17 20:52:10'),(3,1,1,'I\'m bored','2025-08-17 20:52:10'),(4,1,6,'Hi bored, I\'m Me','2025-08-17 20:52:10'),(5,1,1,'Funny...','2025-08-17 20:52:10'),(6,2,3,'Hi guys, I\'m new here','2025-08-17 20:52:10'),(7,2,4,'Hi there','2025-08-17 20:52:10'),(8,2,5,'\'Sup','2025-08-17 20:52:10'),(9,2,7,'//Insert greeting here','2025-08-17 20:52:10'),(10,2,3,'Good to meet everyone','2025-08-17 20:52:10'),(11,3,1,'Who loves chocolate? We love chocolate','2025-08-17 20:52:10'),(12,3,2,'My favourite is Ferror Rocher','2025-08-17 20:52:10'),(13,3,1,'Ooh, that\'s a good one. Mine is PepperMint Crisp','2025-08-17 20:52:10'),(14,3,7,'I like white chocolate','2025-08-17 20:52:10'),(15,3,1,'Mm, I find white chocolate too sweet','2025-08-17 20:52:10');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
