/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import net.sf.flatpack.DataSet;

import com.luckia.biller.core.model.CompanyGroup;
import com.luckia.biller.core.model.LegalEntity;

public class CompanyGroupFeeder extends LegalEntityFeeder<CompanyGroup> {

	@Override
	protected LegalEntity buildEntity(DataSet dataSet) {
		return new CompanyGroup();
	}
}
