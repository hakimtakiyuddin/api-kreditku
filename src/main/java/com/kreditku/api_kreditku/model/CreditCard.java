package com.kreditku.api_kreditku.model;

public class CreditCard {
    private String name;
    private String bank;
    private String benefits;
    private String bestFor;
    private String annualFee;
    private String minIncome;

    public CreditCard() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getBestFor() {
        return bestFor;
    }

    public void setBestFor(String bestFor) {
        this.bestFor = bestFor;
    }

    public String getAnnualFee() {
        return annualFee;
    }

    public void setAnnualFee(String annualFee) {
        this.annualFee = annualFee;
    }

    public String getMinIncome() {
        return minIncome;
    }

    public void setMinIncome(String minIncome) {
        this.minIncome = minIncome;
    }
}
