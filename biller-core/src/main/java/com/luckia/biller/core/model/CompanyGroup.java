/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Representa un grupo de empresas
 */
@Entity
@Table(name = "B_COMPANY_GROUP")
@DiscriminatorValue("G")
@SuppressWarnings("serial")
public class CompanyGroup extends LegalEntity {
}
