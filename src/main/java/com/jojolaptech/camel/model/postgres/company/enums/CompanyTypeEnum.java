package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum CompanyTypeEnum {
    PRIVATE_LIMITED("Private Limited Company", "Privately owned limited liability company"),
    PUBLIC_LIMITED("Public Limited Company", "Listed or publicly traded company"),
    SOLE_PROPRIETORSHIP("Sole Proprietorship", "Single owner business"),
    PARTNERSHIP("Partnership Firm", "Business owned by two or more partners"),
    LLP("Limited Liability Partnership", "LLP business structure"),
    GOVERNMENT("Government Organization", "Ministries, departments, municipalities"),
    NON_PROFIT("Non-Profit Organization", "NGO, charity, foundation"),
    COOPERATIVE("Cooperative", "Savings & credit, agriculture, multipurpose cooperatives"),
    EDUCATIONAL("Educational Institution", "School, college, university, training center"),
    HEALTHCARE("Healthcare Organization", "Hospital, clinic, laboratory"),
    FINANCIAL_INSTITUTION("Financial Institution", "Finance company, microfinance, leasing"),
    MANUFACTURING("Manufacturing Company", "Factory or production company"),
    TRADING("Trading Company", "Import, export, wholesale, retail"),
    SERVICE("Service Company", "IT, consulting, marketing, outsourcing, etc."),
    CONSTRUCTION("Construction Company", "Construction, engineering, contractors"),
    HOSPITALITY("Hospitality", "Hotel, restaurant, resort, cafe"),
    LOGISTICS("Logistics & Transportation", "Courier, freight, transport"),
    AGRICULTURE("Agriculture", "Farming, livestock, agro-business"),
    OTHER("Other", "Custom organization type");

    private final String displayName;
    private final String description;

    CompanyTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
