package com.emerginggames.snappersbackend;

import java.util.Date;

public class PromoCode {
	private String promoCode;
	private Date validTill;
	private int promoHints;
	
	public int getPromoHints() {
		return promoHints;
	}
	
	public void setPromoHints(int promoHints) {
		this.promoHints = promoHints;
	}
	
	public Date getValidTill() {
		return validTill;
	}
	
	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}
	
	public String getPromoCode() {
		return promoCode;
	}
	
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	
}
