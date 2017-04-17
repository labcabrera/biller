package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.common.BillerException;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.BillingModelAttributes;
import com.luckia.biller.core.model.Rappel;
import com.luckia.biller.core.services.AuditService;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;

public class BillingModelFeeder implements Feeder<BillingModel> {

	private static final Logger LOG = LoggerFactory.getLogger(BillingModelFeeder.class);

	@Inject
	private Provider<EntityManager> entityManagerProvider;
	@Inject
	private AuditService auditService;

	@Override
	public void loadEntities(InputStream source) {
		try {
			Reader reader = new InputStreamReader(source, "UTF8");
			Parser parser = DefaultParserFactory.getInstance().newDelimitedParser(reader,
					',', '"');
			DataSet dataSet = parser.parse();
			EntityManager entityManager = entityManagerProvider.get();
			long readed = 0;
			while (dataSet.next()) {
				BillingModel model = new BillingModel();
				model.setName(dataSet.getString("NAME"));
				model.setStoreModel(new BillingModelAttributes());
				model.setCompanyModel(new BillingModelAttributes());

				model.getStoreModel().setStakesPercent(
						new BigDecimal(dataSet.getString("STORE_STAKES")).setScale(2));
				model.getStoreModel().setGgrPercent(BigDecimal.ZERO);
				model.getStoreModel().setNgrPercent(BigDecimal.ZERO);
				model.getStoreModel().setNrPercent(BigDecimal.ZERO);
				model.getStoreModel().setCoOperatingMonthlyFees(BigDecimal.ZERO);
				model.getStoreModel().setCommercialMonthlyFees(BigDecimal.ZERO);
				model.getStoreModel().setSatMonthlyFees(BigDecimal.ZERO);

				model.getCompanyModel().setStakesPercent(
						new BigDecimal(dataSet.getString("COMPANY_STAKES")).setScale(2));
				model.getCompanyModel().setGgrPercent(
						new BigDecimal(dataSet.getString("COMPANY_GGR")).setScale(2));
				model.getCompanyModel().setNgrPercent(
						new BigDecimal(dataSet.getString("COMPANY_NGR")).setScale(2));
				model.getCompanyModel().setNrPercent(
						new BigDecimal(dataSet.getString("COMPANY_NR")).setScale(2));
				model.getCompanyModel().setCoOperatingMonthlyFees(
						new BigDecimal(dataSet.getString("COOPERATING")).setScale(2));
				model.getCompanyModel().setCommercialMonthlyFees(
						new BigDecimal(dataSet.getString("COMMERCIAL")).setScale(2));
				model.getCompanyModel().setSatMonthlyFees(
						new BigDecimal(dataSet.getString("SAT")).setScale(2));

				String rappelStr = dataSet.getString("RAPPEL");
				if (StringUtils.isNotBlank(rappelStr)) {
					model.setRappel(new ArrayList<Rappel>());
					String[] values = rappelStr.split("\\|");
					for (String str : values) {
						String[] i = str.split(";");
						Rappel rappel = new Rappel();
						rappel.setAmount(new BigDecimal(i[0]));
						rappel.setBonusPercent(StringUtils.isNotBlank(i[1])
								? new BigDecimal(i[1]) : null);
						rappel.setBonusAmount(StringUtils.isNotBlank(i[2])
								? new BigDecimal(i[2]) : null);
						rappel.setModel(model);
						model.getRappel().add(rappel);
					}
				}

				auditService.processCreated(model);
				entityManager.persist(model);
				readed++;
			}
			entityManager.flush();
			LOG.info("Cargados {} modelos de facturacion", readed);
		}
		catch (Exception ex) {
			throw new BillerException(ex);
		}
	}
}
