-- MySQL dump 10.13  Distrib 8.0.23, for macos10.15 (x86_64)
--
-- Host: 192.168.164.132    Database: xcplus_learning
-- ------------------------------------------------------
-- Server version	8.0.35

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `xc_choose_course`
--

DROP TABLE IF EXISTS `xc_choose_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xc_choose_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `order_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `course_price` float(10,2) NOT NULL COMMENT '课程价格',
  `valid_days` int NOT NULL COMMENT '课程有效期(天)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课状态',
  `validtime_start` datetime NOT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '结束服务时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xc_choose_course`
--

LOCK TABLES `xc_choose_course` WRITE;
/*!40000 ALTER TABLE `xc_choose_course` DISABLE KEYS */;
INSERT INTO `xc_choose_course` VALUES (18,24,'人工智能+python','52',1232141425,'700001','2024-03-30 18:28:04',0.00,365,'701001','2024-03-30 18:28:04','2025-03-30 18:28:04',NULL),(19,1,'JAVA8/9/10新特性讲解','52',1232141425,'700002','2024-03-30 18:29:40',2.00,1,'701001','2024-03-30 18:29:40','2024-03-31 18:29:40',NULL);
/*!40000 ALTER TABLE `xc_choose_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xc_course_tables`
--

DROP TABLE IF EXISTS `xc_course_tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xc_course_tables` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `choose_course_id` bigint NOT NULL COMMENT '选课记录id',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `course_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `course_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `validtime_start` datetime DEFAULT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '到期时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `course_tables_unique` (`user_id`,`course_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xc_course_tables`
--

LOCK TABLES `xc_course_tables` WRITE;
/*!40000 ALTER TABLE `xc_course_tables` DISABLE KEYS */;
INSERT INTO `xc_course_tables` VALUES (12,18,'52',24,1232141425,'人工智能+python','700001','2024-03-30 18:28:04','2024-03-30 18:28:04','2025-03-30 18:28:04',NULL,NULL),(13,19,'52',1,1232141425,'JAVA8/9/10新特性讲解','700002','2024-04-04 00:07:30','2024-03-30 18:29:40','2024-04-30 18:29:40',NULL,NULL);
/*!40000 ALTER TABLE `xc_course_tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xc_learn_record`
--

DROP TABLE IF EXISTS `xc_learn_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xc_learn_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程名称',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `learn_date` datetime DEFAULT NULL COMMENT '最近学习时间',
  `learn_length` bigint DEFAULT NULL COMMENT '学习时长',
  `teachplan_id` bigint DEFAULT NULL COMMENT '章节id',
  `teachplan_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '章节名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `learn_record_unique` (`course_id`,`user_id`,`teachplan_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xc_learn_record`
--

LOCK TABLES `xc_learn_record` WRITE;
/*!40000 ALTER TABLE `xc_learn_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `xc_learn_record` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-04-25 16:44:09
