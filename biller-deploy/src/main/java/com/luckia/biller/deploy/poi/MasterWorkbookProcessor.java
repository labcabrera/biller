package com.luckia.biller.deploy.poi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.Address;
import com.luckia.biller.core.model.AppSettings;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.CostCenter;
import com.luckia.biller.core.model.IdCard;
import com.luckia.biller.core.model.IdCardType;
import com.luckia.biller.core.model.Owner;
import com.luckia.biller.core.model.Province;
import com.luckia.biller.core.model.Region;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.model.TerminalRelation;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.SettingsService;

public class MasterWorkbookProcessor extends BaseWoorbookProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(MasterWorkbookProcessor.class);
	private static final String COST_CENTER_NAME = "Central de pagos (ccc EH)";
	private static final String DATABASE_FILE = "./src/main/resources/bootstrap/hoja-maestra-facturacion.xlsx";

	@Inject
	protected Provider<EntityManager> entityManagerProvider;
	@Inject
	protected WorkbookEntityResolver entityResolver;
	@Inject
	protected BillingModelResolver billingModelResolver;
	@Inject
	protected PersonNameResolver personNameResolver;
	@Inject
	protected SettingsService settingsService;
	@Inject
	protected AuditService auditService;

	protected Sheet sheetCompanies;
	protected Sheet sheetStores;
	protected Map<Long, Company> companies;
	protected List<Store> stores;

	public static void main(String... args) throws IOException {
		InputStream in = new FileInputStream(DATABASE_FILE);
		Injector injector = Guice.createInjector(new BillerModule());
		MasterWorkbookProcessor processor = injector.getInstance(MasterWorkbookProcessor.class);
		processor.process(in, true);
	}

	public void process(InputStream in) {
		process(in, true);
	}

	public void process(InputStream in, boolean persist) {
		Validate.notNull(in, "Missing InputStream");
		LOG.info("Cargando hoja maestra de facturacion");
		try {
			Workbook wb = WorkbookFactory.create(in);
			sheetCompanies = wb.getSheet("Empresas operadoras");
			sheetStores = wb.getSheet("Locales");
			LOG.debug("Obteniendo empresas operadoras");
			loadCompanies();
			LOG.debug("Obteniendo establecimientos");
			loadStores();
			LOG.debug("Numero de modelos registrados: {}", billingModelResolver.getNewModels().size());
			if (persist) {
				EntityManager entityManager = entityManagerProvider.get();
				entityManager.getTransaction().begin();
				deleteEntities();
				entityManager.flush();
				for (BillingModel model : billingModelResolver.getNewModels()) {
					auditService.processCreated(model);
					entityManager.persist(model);
				}
				entityManager.flush();
				for (Company company : companies.values()) {
					auditService.processCreated(company);
					entityManager.persist(company);
				}
				entityManager.flush();
				for (Store store : stores) {
					auditService.processCreated(store);
					if (store.getOwner() != null) {
						auditService.processCreated(store.getOwner());
						if (store.getOwner().getId() == null) {
							entityManager.persist(store.getOwner());
						}
					}
					entityManager.persist(store);
				}
				entityManager.flush();
				entityManager.getTransaction().commit();
				entityManager.getTransaction().begin();
				updateCostCenters();
				updateSettings();
				entityManager.getTransaction().commit();
				LOG.debug("Importacion finalizada");
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private void loadCompanies() {
		companies = new HashMap<Long, Company>();
		int rowNumber = 1;
		Row row;
		while ((row = sheetCompanies.getRow(rowNumber++)) != null && rowNumber < 1000) {
			try {
				if (row.getCell(1) != null) {
					String name = readCellAsString(row.getCell(1));
					Long id = readCellAsBigDecimal(row.getCell(0)).longValue();
					String cif = readCellAsString(row.getCell(2));
					String road = readCellAsString(row.getCell(3));
					Region region = entityResolver.resolveRegion(readCellAsString(row.getCell(4)));
					Province province = entityResolver.resolveProvince(readCellAsString(row.getCell(5)));
					LOG.debug(String.format("%-2s %-30s %-12s %-30s", id, name, cif, road));
					Company company = new Company();
					company.setName(name);
					company.setIdCard(new IdCard(IdCardType.CIF, cif));
					company.setAddress(new Address());
					company.getAddress().setRoad(road);
					company.getAddress().setProvince(province);
					company.getAddress().setRegion(region);
					companies.put(id, company);
				}
			} catch (Exception ex) {
				LOG.error("Error al leer la fila " + rowNumber, ex);
			}
		}
		LOG.info("Obtenidas {} companias", companies.size());
	}

	private void loadStores() {
		stores = new ArrayList<Store>();
		int rowNumber = 1;
		Row row;
		while ((row = sheetStores.getRow(rowNumber++)) != null) {
			try {
				String name = readCellAsString(row.getCell(5));
				String idCompanyString = readCellAsString(row.getCell(0));
				idCompanyString = idCompanyString.replaceAll(".0", "");
				if (!StringUtils.isEmpty(name) && !"TBC".equals(idCompanyString)) {
					Long idCompany = idCompanyString.matches("\\d+") ? Long.parseLong(idCompanyString) : null;
					String ownerCompleteName = readCellAsString(row.getCell(6));
					String ownerIdCardNumber = readCellAsString(row.getCell(7));
					String type = readCellAsString(row.getCell(2));
					String road = readCellAsString(row.getCell(8));
					String regionName = readCellAsString(row.getCell(9));
					String provinceName = readCellAsString(row.getCell(10));
					Region region = entityResolver.resolveRegion(regionName);
					Province province = entityResolver.resolveProvince(provinceName);

					Mutable<String> ownerName = new MutableObject<String>();
					Mutable<String> ownerFirstSurname = new MutableObject<String>();
					Mutable<String> ownerSecondSurname = new MutableObject<String>();
					personNameResolver.resolve(ownerCompleteName, ownerName, ownerFirstSurname, ownerSecondSurname);
					LOG.debug(String.format("%-30s %-40s %-30s %-30s %-30s", name, ownerName, road, province, region));

					StringBuffer comments = new StringBuffer();

					Owner owner = null;
					if (!"tbd".equals(ownerCompleteName) && !"?".equals(ownerCompleteName)) {
						owner = new Owner();
						owner.setName(ownerName.getValue());
						owner.setFirstSurname(ownerFirstSurname.getValue());
						owner.setSecondSurname(ownerSecondSurname.getValue());
						owner.setIdCard(new IdCard(IdCardType.NIF, ownerIdCardNumber));
					} else {
						comments.append("No se ha definido el titular. En el campo aparece '" + ownerCompleteName + "'\n");
					}

					Store store = new Store();
					store.setName(name);
					store.setOwner(owner);
					store.setType(entityResolver.resolveStoreType(type));
					store.setAddress(new Address());
					store.getAddress().setRoad(road);
					store.getAddress().setProvince(province);
					store.getAddress().setRegion(region);
					if (idCompany != null) {
						store.setParent(companies.get(idCompany));
					}
					if (region == null) {
						comments.append("No se encuentra la region: " + regionName + "\n");
					}
					if (province == null) {
						comments.append("No se encuentra la provincia: " + regionName + "\n");
					}
					store.setComments(comments.toString());
					store.setTerminalRelations(new ArrayList<TerminalRelation>());
					appendStoreTerminals(store, readCellAsString(row.getCell(3)).replaceAll(",", ""), true, store.getTerminalRelations());
					appendStoreTerminals(store, readCellAsString(row.getCell(4)), false, store.getTerminalRelations());
					store.setBillingModel(billingModelResolver.resolveBillingModel(row));
					stores.add(store);
				}
			} catch (Exception ex) {
				LOG.error("Error al procesar el establecimiento", ex);
			}
		}
		LOG.info("Obtenidos {} establecimientos", stores.size());
	}

	private void appendStoreTerminals(Store store, String input, Boolean isMaster, List<TerminalRelation> list) {
		Pattern pattern = Pattern.compile("(\\d\\d+)");
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			String value = input.substring(matcher.start(), matcher.end());
			boolean find = false;
			for (TerminalRelation i : list) {
				if (value.equals(i.getCode())) {
					find = true;
					break;
				}
			}
			if (!find) {
				TerminalRelation relation = new TerminalRelation();
				relation.setCode(value);
				relation.setIsMaster(isMaster);
				relation.setStore(store);
				auditService.processCreated(relation);
				list.add(relation);
			}
		}
	}

	private void deleteEntities() {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.createQuery("update Bill e set e.sender = :value, e.receiver = :value").setParameter("value", null).executeUpdate();
		entityManager.createQuery("update LegalEntity e set e.idCard = :value").setParameter("value", null).executeUpdate();
		entityManager.createQuery("update Store e set e.owner = :value").setParameter("value", null).executeUpdate();
		entityManager.createQuery("delete from TerminalRelation").executeUpdate();
		entityManager.createQuery("delete from Owner").executeUpdate();
		entityManager.createQuery("delete from Store").executeUpdate();
		entityManager.createQuery("delete from Company").executeUpdate();
		entityManager.createQuery("delete from CompanyGroup").executeUpdate();
		entityManager.createQuery("delete from Address").executeUpdate();
		entityManager.createQuery("delete from IdCard").executeUpdate();
	}

	private void updateSettings() {
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Company> query = entityManager.createQuery("select c from Company c where c.name = :value", Company.class);
		List<Company> companies = query.setParameter("value", COST_CENTER_NAME).getResultList();
		if (!companies.isEmpty()) {
			AppSettings settings = settingsService.getBillingSettings();
			settings.setValue("global.costCenter.id", String.valueOf(companies.iterator().next().getId()));
			entityManager.merge(settings);
		}
	}

	private void updateCostCenters() {
		LOG.info("Actualizando centros de coste");
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<CostCenter> query = entityManager.createQuery("select c from CostCenter c where c.name = :name", CostCenter.class);
		CostCenter valencia = query.setParameter("name", "Centro de Coste Valencia").getSingleResult();
		CostCenter galicia = query.setParameter("name", "Centro de Coste Galicia").getSingleResult();
		Map<String, CostCenter> map = new HashMap<String, CostCenter>();
		map.put("Alicante/Alacant", valencia);
		map.put("Barcelona", valencia);
		map.put("Castellón/Castelló", valencia);
		map.put("Coruña, A", galicia);
		map.put("Lugo", galicia);
		map.put("Ourense", galicia);
		map.put("Pontevedra", galicia);
		map.put("Valencia/Valéncia", valencia);
		entityManager.flush();
		List<Store> stores = entityManager.createQuery("select s from Store s", Store.class).getResultList();
		for (Store store : stores) {
			if (store.getAddress() != null && store.getAddress().getProvince() != null) {
				store.setCostCenter(map.get(store.getAddress().getProvince().getName()));
				entityManager.merge(store);
			}
		}
	}
}
