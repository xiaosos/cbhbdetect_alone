SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for detectresult
-- ----------------------------
DROP TABLE IF EXISTS `detectresult`;
CREATE TABLE `detectresult` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cardnum` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `detecttype` varchar(11) DEFAULT NULL,
  `amt` int(11) DEFAULT NULL,
  `detecttime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ind_cardnum` (`cardnum`)
) ENGINE=InnoDB AUTO_INCREMENT=212 DEFAULT CHARSET=utf8;
