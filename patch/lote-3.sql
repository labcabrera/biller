
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


  

