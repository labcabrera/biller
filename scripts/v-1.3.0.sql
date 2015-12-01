

CREATE TABLE S_USER_ACTIVITY (
  `ID` VARCHAR(36) NOT NULL,
  `USER_ID` BIGINT(20) NOT NULL,
  `ACTIVITY_DATE` TIMESTAMP NOT NULL,
  `TYPE` VARCHAR(128) NOT NULL,
  `DATA` TEXT NULL,
  PRIMARY KEY (`ID`));

ALTER TABLE S_USER_ACTIVITY ADD INDEX `FK_USER_ACTIVITY_USER_IDX` (`USER_ID` ASC);

ALTER TABLE S_USER_ACTIVITY 
ADD CONSTRAINT `FK_USER_ACTIVITY_USER`
  FOREIGN KEY (`USER_ID`)
  REFERENCES S_USER (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

CREATE TABLE S_USER_SESSION (
  `SESSION` VARCHAR(36) NOT NULL,
  `USER_ID` BIGINT(20) NOT NULL,
  `CREATED` TIMESTAMP NOT NULL,
  `LAST_ACCESS` TIMESTAMP,	
  `EXPIRATION` TIMESTAMP,	
  PRIMARY KEY (`SESSION`));

ALTER TABLE S_USER_SESSION ADD INDEX `FK_USER_SESSION_USER_IDX` (`USER_ID` ASC);

ALTER TABLE S_USER_SESSION
ADD CONSTRAINT `FK_USER_SESSION_USER`
  FOREIGN KEY (`USER_ID`)
  REFERENCES S_USER (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE B_BILLING_MODEL 
ADD COLUMN `VAT_LIQUIDATION_TYPE` VARCHAR(32) NULL AFTER `STORE_PRICE_PER_LOCATION`,
ADD COLUMN `RECEIVER_ID` BIGINT(20) NULL AFTER `VAT_LIQUIDATION_TYPE`;

ALTER TABLE B_BILLING_MODEL ADD INDEX `B_BILLING_MODEL_RECEIVER_IDX` (`RECEIVER_ID` ASC);

ALTER TABLE B_BILLING_MODEL 
ADD CONSTRAINT `B_BILLING_MODEL_RECEIVER`
  FOREIGN KEY (`RECEIVER_ID`)
  REFERENCES B_LEGAL_ENTITY (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE B_BILL_DETAIL ADD INDEX `INDEX_BILL_TYPE` (`DTYPE` ASC, `BILL_ID` ASC);

ALTER TABLE B_LIQUIDATION_DETAIL ADD INDEX `INDEX_LIQUIDATION_DETAIL` (`LIQUIDATION_ID` ASC);

ALTER TABLE B_LIQUIDATION_DETAIL ADD COLUMN `LIQUIDATION_INCLUDED` BIT NOT NULL DEFAULT TRUE AFTER `LIQUIDATION_ID`;

ALTER TABLE B_BILL_DETAIL ADD COLUMN `LIQUIDATION_INCLUDED` BIT NOT NULL DEFAULT TRUE AFTER `BILL_ID`;

ALTER TABLE B_BILL
ADD COLUMN `LIQUIDATION_MANUAL_AMOUNT` DECIMAL(18,2) NULL AFTER `LIQUIDATION_TOTAL_AMOUNT`,
ADD COLUMN `LIQUIDATION_OUTER_AMOUNT` DECIMAL(18,2) NULL AFTER `LIQUIDATION_MANUAL_AMOUNT`;

UPDATE B_BILL SET LIQUIDATION_MANUAL_AMOUNT = ADJUSTMENT_AMOUNT;

ALTER TABLE B_BILL DROP COLUMN `ADJUSTMENT_AMOUNT`;

ALTER TABLE B_BILL_DETAIL 
ADD COLUMN `NET_VALUE` DECIMAL(18,2) NULL AFTER `LIQUIDATION_INCLUDED`,
ADD COLUMN `VAT_VALUE` DECIMAL(18,2) NULL AFTER `NET_VALUE`,
ADD COLUMN `VAT_PERCENT` DECIMAL(18,2) NULL AFTER `VAT_VALUE`;

UPDATE B_BILLING_MODEL SET VAT_LIQUIDATION_TYPE = 'EXCLUDED';

ALTER TABLE B_BILL
ADD COLUMN `LIQUIDATION_TOTAL_VAT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_PRICE_PER_LOCATION`,
ADD COLUMN `LIQUIDATION_TOTAL_NET_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_TOTAL_VAT`;

ALTER TABLE B_BILL_DETAIL 
CHANGE COLUMN `BASE_VALUE` `SOURCE_VALUE` DECIMAL(18,2) NULL DEFAULT NULL;

ALTER TABLE B_LIQUIDATION_DETAIL 
ADD COLUMN `SOURCE_VALUE` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_INCLUDED`,
ADD COLUMN `NET_VALUE` DECIMAL(18,2) NULL DEFAULT NULL AFTER `SOURCE_VALUE`,
ADD COLUMN `VAT_VALUE` DECIMAL(18,2) NULL DEFAULT NULL AFTER `NET_VALUE`;

ALTER TABLE B_LIQUIDATION 
ADD COLUMN `NET_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `REPORT_FILE_ID`,
ADD COLUMN `VAT_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `NET_AMOUNT`,
ADD COLUMN `TOTAL_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `VAT_AMOUNT`;

UPDATE B_BILL_DETAIL SET LIQUIDATION_INCLUDED = true WHERE LIQUIDATION_INCLUDED = null;

ALTER TABLE B_LIQUIDATION
ADD COLUMN `STORE_MANUAL_OUTER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `TOTAL_AMOUNT`;

ALTER TABLE B_LIQUIDATION 
ADD COLUMN `TOTAL_OUTER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `STORE_MANUAL_OUTER_AMOUNT`;

ALTER TABLE B_LIQUIDATION 
ADD COLUMN `LIQUIDATION_EFFECTIVE_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `TOTAL_OUTER_AMOUNT`;

UPDATE B_BILL_DETAIL SET NET_VALUE = VALUE WHERE NET_VALUE IS NULL;
UPDATE B_BILL_DETAIL SET VAT_VALUE = 0 WHERE VAT_VALUE IS NULL;

UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'STAKES' WHERE CONCEPT_TYPE = 'Stakes';
UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'SAT_MONTHLY_FEES' WHERE CONCEPT_TYPE = 'SatMonthlyFees';
UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'COMMERCIAL_MONTHLY_FEES' WHERE CONCEPT_TYPE = 'CommercialMonthlyFees';
UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'PRICE_PER_LOCATION' WHERE CONCEPT_TYPE = 'PricePerLocation';
UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'MANUAL_WITH_LIQUIDATION' WHERE CONCEPT_TYPE = 'ManualWithLiquidation';
UPDATE B_BILL_DETAIL SET CONCEPT_TYPE = 'MANUAL_WITHOUT_LIQUIDATION' WHERE CONCEPT_TYPE = 'ManualWithoutLiquidation';

UPDATE B_BILL_RAW_DATA SET CONCEPT = 'NR' WHERE CONCEPT = '0';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'NGR' WHERE CONCEPT = '1';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'GGR' WHERE CONCEPT = '2';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'STAKES' WHERE CONCEPT = '3';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'BONUS' WHERE CONCEPT = '4'; -- 0 rows
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'COMMERCIAL_MONTHLY_FEES' WHERE CONCEPT = '5'; -- 0 rows
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'COOPERATING_MONTHLY_FEES' WHERE CONCEPT = '6'; -- 0 rows
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'SAT_MONTLY_FEES' WHERE CONCEPT = '7'; -- 0 rows
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'PRICE_PER_LOCATION' WHERE CONCEPT = '8';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'STORE_CASH' WHERE CONCEPT = '9';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'CREDIT' WHERE CONCEPT = '10';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'MANUAL' WHERE CONCEPT = '11';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'ADJUSTMENT' WHERE CONCEPT = '12';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'MANUAL_WITH_LIQUIDATION' WHERE CONCEPT = '13';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'MANUAL_WITHOUT_LIQUIDATION' WHERE CONCEPT = '14';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'OTHER' WHERE CONCEPT = '15';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'TOTAL_BET_AMOUNT' WHERE CONCEPT = '16';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'CANCELLED' WHERE CONCEPT = '17';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'TOTAL_WIN_AMOUNT' WHERE CONCEPT = '18';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'TOTAL_ATTRIBUTABLE' WHERE CONCEPT = '19';
UPDATE B_BILL_RAW_DATA SET CONCEPT = 'MARGIN' WHERE CONCEPT = '20';

ALTER TABLE S_USER 
CHANGE COLUMN `created` `CREATED` DATETIME NULL DEFAULT NULL ,
CHANGE COLUMN `NAME` `COMPLETE_NAME` VARCHAR(32) NOT NULL ,
ADD COLUMN `ALIAS` VARCHAR(128) NULL AFTER `PASSWORD_DIGEST`;

UPDATE S_USER SET ALIAS = COMPLETE_NAME;
UPDATE S_USER SET ALIAS = 'lcabrera' WHERE ALIAS = 'Luis Cabrera';

ALTER TABLE S_USER CHANGE COLUMN `ID` `ID` BIGINT(20) NOT NULL AUTO_INCREMENT;

COMMIT;

DELETE FROM S_USER WHERE COMPLETE_NAME <> 'admin';

INSERT INTO S_USER (CREATED, EMAIL, COMPLETE_NAME, PASSWORD_DIGEST, ALIAS) VALUES ('2015-12-01 13:57:53', 'lab.cabera@gmail.com', 'Luis Cabrera', '3UyIwFUp9Kgwe8z+HaWeetKMdhY=', 'lcabrera');
INSERT INTO S_USER (CREATED, EMAIL, COMPLETE_NAME, PASSWORD_DIGEST, ALIAS) VALUES ('2015-12-01 13:57:53', 'migue.celemin@luckia.es', 'Miguel Celemín', '3UyIwFUp9Kgwe8z+HaWeetKMdhY=', 'mcelemin');
INSERT INTO S_USER (CREATED, EMAIL, COMPLETE_NAME, PASSWORD_DIGEST, ALIAS) VALUES ('2015-12-01 13:57:53', 'barbara.alvarez@lukia.es', 'Bárbara Alvarez', '3UyIwFUp9Kgwe8z+HaWeetKMdhY=', 'balvarez');



CREATE TABLE S_USER_ROLE (
  `ID` INT NOT NULL,
  `NAME` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`ID`));

CREATE TABLE S_USER_ROLE_RELATION (
  `USER_ID` INT NOT NULL,
  `ROLE_ID` INT NOT NULL,
  PRIMARY KEY (`USER_ID`, `ROLE_ID`));
  
/*
-- INSERT INTO S_USER_ROLE(ID, NAME) VALUES (1, 'Administrador');
-- INSERT INTO S_USER_ROLE(ID, NAME) VALUES (2, 'Operador');
-- INSERT INTO S_USER_ROLE(ID, NAME) VALUES (3, 'Lectura');
*/

INSERT INTO S_USER_ROLE_RELATION(USER_ID, ROLE_ID) VALUES (6,1);

UPDATE B_BILL_DETAIL SET DTYPE = 'L' WHERE DTYPE = 'BillLiquidationDetail';
UPDATE B_BILL_DETAIL SET DTYPE = 'B' WHERE DTYPE = 'BillDetail';

ALTER TABLE B_LIQUIDATION 
CHANGE COLUMN `ADJUSTMENT_AMOUNT` `LIQUIDATION_MANUAL_INNER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL ,
CHANGE COLUMN `TOTAL_OUTER_AMOUNT` `LIQUIDATION_MANUAL_OUTER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL ,
CHANGE COLUMN `CASH_STORE_ADJUSTMENT_AMOUNT` `CASH_STORE_EFFECTIVE_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL ,
ADD COLUMN `MODEL_VERSION` VARCHAR(8) NULL AFTER `LIQUIDATION_EFFECTIVE_AMOUNT`;

ALTER TABLE B_LIQUIDATION 
CHANGE COLUMN `TOTAL_AMOUNT` `TOTAL_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `ID`,
CHANGE COLUMN `NET_AMOUNT` `NET_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `TOTAL_AMOUNT`,
CHANGE COLUMN `VAT_AMOUNT` `VAT_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `NET_AMOUNT`,
CHANGE COLUMN `STORE_MANUAL_OUTER_AMOUNT` `STORE_MANUAL_OUTER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `VAT_AMOUNT`,
CHANGE COLUMN `CASH_STORE_AMOUNT` `CASH_STORE_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `STORE_MANUAL_OUTER_AMOUNT`,
CHANGE COLUMN `CASH_STORE_EFFECTIVE_AMOUNT` `CASH_STORE_EFFECTIVE_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `CASH_STORE_AMOUNT`,
CHANGE COLUMN `LIQUIDATION_MANUAL_OUTER_AMOUNT` `LIQUIDATION_MANUAL_OUTER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_MANUAL_INNER_AMOUNT`,
CHANGE COLUMN `LIQUIDATION_EFFECTIVE_AMOUNT` `LIQUIDATION_EFFECTIVE_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_MANUAL_OUTER_AMOUNT`,
CHANGE COLUMN `RECEIVER_AMOUNT` `RECEIVER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `LIQUIDATION_EFFECTIVE_AMOUNT`,
CHANGE COLUMN `MODEL_VERSION` `MODEL_VERSION` VARCHAR(8) NULL DEFAULT NULL AFTER `RECEIVER_AMOUNT`,
CHANGE COLUMN `SAT_AMOUNT` `SAT_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `BET_AMOUNT`,
CHANGE COLUMN `REPORT_FILE_ID` `REPORT_FILE_ID` BIGINT(20) NULL DEFAULT NULL AFTER `STORE_AMOUNT`,
CHANGE COLUMN `SENDER_AMOUNT` `SENDER_AMOUNT` DECIMAL(18,2) NULL DEFAULT NULL AFTER `PRICE_PER_LOCATION_AMOUNT`;

UPDATE B_LIQUIDATION SET MODEL_VERSION = '1.2.0' WHERE MODEL_VERSION IS NULL;

ALTER TABLE B_LIQUIDATION DROP COLUMN REPORT_FILE_ID;	

CREATE TABLE S_SCHEDULED_TASK (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(128) NOT NULL,
  `CLASSNAME` TEXT NOT NULL,
  `CRON_EXP` VARCHAR(64) NULL,
  `ENABLED` BIT NOT NULL,
  PRIMARY KEY (`ID`));

CREATE TABLE S_SCHEDULED_TASK_PARAM (
  `TASK_ID` INT NOT NULL,
  `PARAM_KEY` VARCHAR(64) NOT NULL,
  `PARAM_VALUE` VARCHAR(516) NOT NULL,
  PRIMARY KEY (`TASK_ID`, `PARAM_KEY`));

ALTER TABLE S_SCHEDULED_TASK_PARAM 
ADD CONSTRAINT `FK_SCHEDULED_TASK`
  FOREIGN KEY (`TASK_ID`)
  REFERENCES S_SCHEDULED_TASK (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
INSERT INTO S_SCHEDULED_TASK(NAME, CLASSNAME, CRON_EXP, ENABLED) VALUES ('Generación automática de facturas (obsoleto)', 'com.luckia.biller.core.scheduler.BillingJob', '0 0 12 5 1/1 ? *', false);
INSERT INTO S_SCHEDULED_TASK(NAME, CLASSNAME, CRON_EXP, ENABLED) VALUES ('Envío de correos (obsoleto)', 'com.luckia.biller.core.scheduler.MailJob', '0 0 0/1 1/1 * ? *', false);
INSERT INTO S_SCHEDULED_TASK(NAME, CLASSNAME, CRON_EXP, ENABLED) VALUES ('System check', 'com.luckia.biller.core.scheduler.SystemCheckJob', '0 0 0/1 1/1 * ? *', false);
INSERT INTO S_SCHEDULED_TASK(NAME, CLASSNAME, CRON_EXP, ENABLED) VALUES ('Liquidación de rapel (obsoleto)', 'com.luckia.biller.core.scheduler.RappelLiquidationJob', '0 0 12 15 1 ? *', false);

DROP TABLE S_APP_SETTINGS_VALUES;
DROP TABLE S_APP_SETTINGS;

