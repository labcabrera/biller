package com.luckia.biller.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Representa cada uno de los detalles que componen una factura.
 */
@Entity
@SuppressWarnings("serial")
@DiscriminatorValue("B")
public class BillDetail extends AbstractBillDetail {

}
