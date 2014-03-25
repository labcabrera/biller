package com.luckia.biller.core.services.bills;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.luckia.biller.core.jpa.Sequencer;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;

/**
 * Servicio encargado de generar los codigos de las facturas. Estos codigos han de ser consecutivos. Cada establecimiento puede definir una
 * plantilla de factura con un formato similar al siguiente:
 * 
 * <pre>
 * A{year}/1035/{sequence, 4}
 * </pre>
 * 
 * Esta expresión define un conjunto de reemplazos posibles que introduciremos como <code>{expressionName}</code>, el valor de la secuencia
 * que se generará junto con su tamaño (por defecto será 6 completando con ceros por la izquierda hasta llegar a esa longitud) y texto fijo.
 * a continuación se muestran unos ejemplos en los que se muestran los codigos asociados a diferentes plantillas:
 * <table>
 * <tr>
 * <th>Expresión</th>
 * <th>Resultado</th>
 * </tr>
 * <tr>
 * <td>''</td>
 * <td>'000001'</td>
 * <tr>
 * <tr>
 * <td>'A{year}/1035/{sequence, 4}'</td>
 * <td>'A2014/1035/0001'</td>
 * <tr>
 * <tr>
 * <td>'A{year}/1035/{sequence, 6}'</td>
 * <td>'A2014/1035/0001'</td>
 * <tr>
 * <tr>
 * <td>'A{year}/1035/'</td>
 * <td>'A2014/1035/000001'</td>
 * <tr>
 * <tr>
 * <td>'A'</td>
 * <td>'A000001'</td>
 * <tr>
 * </table>
 * <p>
 * No se permite declarar texto despues de la expresion {sequence}.
 * </p>
 * 
 */
public class BillCodeGenerator {

	@Inject
	private Sequencer sequencer;

	/**
	 * En el caso de las facturas se genera el numero de factura a partir de la secuencia de cada establecimiento.
	 * 
	 * @param bill
	 */
	public void generateCode(Bill bill) {
		Store store = bill.getSender(Store.class);
		String template = store.getBillSequencePrefix() != null ? store.getBillSequencePrefix() : "";
		Map<Pattern, String> values = configureReplacements();
		String prefix = template;
		for (Pattern pattern : values.keySet()) {
			String replacement = values.get(pattern);
			Matcher matcher = pattern.matcher(prefix);
			if (matcher.find()) {
				prefix = matcher.replaceAll(replacement);
			}
		}
		int padding = 6;
		Pattern pattern = Pattern.compile("\\{sequence\\s?,\\s?(\\d+)\\}");
		Matcher matcher = pattern.matcher(prefix);
		if (matcher.find()) {
			padding = Integer.parseInt(matcher.group(1));
			prefix = matcher.replaceAll("");
		}
		Long sequence = sequencer.nextSequence(prefix);
		String code = prefix.concat(StringUtils.leftPad(String.valueOf(sequence), padding, "0"));
		bill.setCode(code);
	}

	private Map<Pattern, String> configureReplacements() {
		Map<Pattern, String> values = new LinkedHashMap<Pattern, String>();
		values.put(Pattern.compile("\\{year\\}"), String.valueOf(new DateTime().getYear()));
		values.put(Pattern.compile("\\{month\\}"), String.valueOf(new DateTime().getMonthOfYear()));
		return values;
	}
}
