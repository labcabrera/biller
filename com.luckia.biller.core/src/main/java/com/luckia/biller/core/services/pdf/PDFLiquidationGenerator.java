package com.luckia.biller.core.services.pdf;

import java.io.OutputStream;

import com.luckia.biller.core.model.Liquidation;

public class PDFLiquidationGenerator extends PDFGenerator<Liquidation> {

	@Override
	public void generate(Liquidation entity, OutputStream out) {
	}

	@Override
	protected float getLineHeight() {
		return 0;
	}
}
