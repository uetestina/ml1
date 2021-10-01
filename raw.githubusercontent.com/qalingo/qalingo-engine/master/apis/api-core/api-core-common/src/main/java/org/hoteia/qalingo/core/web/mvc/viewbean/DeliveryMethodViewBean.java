/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.web.mvc.viewbean;

public class DeliveryMethodViewBean extends AbstractViewBean {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 267113307585572454L;

	private int version;
	private String name;
	private String description;
	private String code;

	// private Set<ShippingCountry> shippingCountries = new
	// HashSet<ShippingCountry>();

    protected String catalogPrice;
    protected String salePrice;
    protected String currencySign;
    protected String currencyAbbreviated;
    protected String priceWithCurrencySign;
    
    private String detailsUrl;
    private String editUrl;
    
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

    public String getCatalogPrice() {
        return catalogPrice;
    }

    public void setCatalogPrice(String catalogPrice) {
        this.catalogPrice = catalogPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getCurrencySign() {
        return currencySign;
    }

    public void setCurrencySign(String currencySign) {
        this.currencySign = currencySign;
    }

    public String getCurrencyAbbreviated() {
        return currencyAbbreviated;
    }

    public void setCurrencyAbbreviated(String currencyAbbreviated) {
        this.currencyAbbreviated = currencyAbbreviated;
    }

    public String getPriceWithCurrencySign() {
        return priceWithCurrencySign;
    }

    public void setPriceWithCurrencySign(String priceWithCurrencySign) {
        this.priceWithCurrencySign = priceWithCurrencySign;
    }
    
    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getEditUrl() {
        return editUrl;
    }

    public void setEditUrl(String editUrl) {
        this.editUrl = editUrl;
    }
    
}