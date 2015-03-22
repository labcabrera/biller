package com.luckia.biller.core.reporting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.LegalEntity;
import com.luckia.biller.core.model.Liquidation;

public class LiquidationReportDataSource {

	public Map<LegalEntity, List<Liquidation>> getLiquidations(Date from, Date to, List<LegalEntity> legalEntity) {
		LegalEntity operadora = createLegalEntity("Operador Dummy");
		Map<LegalEntity, List<Liquidation>> parent = new LinkedHashMap<LegalEntity, List<Liquidation>>();
		List<Liquidation> child = new ArrayList<Liquidation>();
		child.add(createMockLiquidation(operadora));
		child.add(createMockLiquidation(operadora));
		parent.put(operadora, child);
		return parent;
	}

	public LegalEntity createLegalEntity(String name) {
		LegalEntity legalEntity = new LegalEntity();
		legalEntity.setId(1L);
		legalEntity.setName(name);
		return legalEntity;
	}

	public Liquidation createMockLiquidation(LegalEntity legalEntity) {
		Liquidation liquidation = new Liquidation();
		liquidation.setSender(legalEntity);
		liquidation.setAmount(new BigDecimal("1234.32"));
		liquidation.setBillDate(new DateTime(2015, 1, 1, 0, 0, 0, 0).toDate());
		liquidation.setBills(new ArrayList<Bill>());
		liquidation.getBills().add(createMockBill("Bar Paco"));
		liquidation.getBills().add(createMockBill("Bar Santos"));
		liquidation.getBills().add(createMockBill("Jonny Mentere"));
		return liquidation;
	}

	public Bill createMockBill(String name) {
		Bill bill = new Bill();
		bill.setSender(new LegalEntity().withName(name));
		bill.setLiquidationTotalAmount(new BigDecimal("645.34"));
		bill.setLiquidationBetAmount(new BigDecimal("54.23"));
		return bill;
	}
}
