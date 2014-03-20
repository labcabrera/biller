package com.luckia.biller.core.model;

/**
 * Define el tipo de modelo de facturación. Se dividen en dos tipos:
 * <ul>
 * <li><b>Facturas:</b> aplicable a los modelos de facturación en los que hay
 * que devengar el IVA (hay prestación de servicios). Estos modelos son los que
 * se aplicaran entre las relaciones entre establecimientos y operadores.</li>
 * <li><b>Liquidaciones:</b> aplicable a los modelos</li>
 * </ul>
 * 
 */
public enum BillingModelType {

	Bill, Liquidation

}
