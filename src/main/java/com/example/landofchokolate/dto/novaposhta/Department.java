// ============================================================================
// dto/novaposhta/Department.java - ИСПРАВЛЕННАЯ ВЕРСИЯ
// ============================================================================
package com.example.landofchokolate.dto.novaposhta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department {
    @JsonProperty("SiteKey")
    private String siteKey;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("DescriptionRu")
    private String descriptionRu;

    @JsonProperty("ShortAddress")
    private String shortAddress;

    @JsonProperty("ShortAddressRu")
    private String shortAddressRu;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("TypeOfWarehouse")
    private String typeOfWarehouse;

    @JsonProperty("Ref")
    private String ref;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("CityRef")
    private String cityRef;

    @JsonProperty("CityDescription")
    private String cityDescription;

    @JsonProperty("CityDescriptionRu")
    private String cityDescriptionRu;

    @JsonProperty("SettlementRef")
    private String settlementRef;

    @JsonProperty("SettlementDescription")
    private String settlementDescription;

    @JsonProperty("SettlementAreaDescription")
    private String settlementAreaDescription;

    @JsonProperty("SettlementRegionsDescription")
    private String settlementRegionsDescription;

    @JsonProperty("SettlementTypeDescription")
    private String settlementTypeDescription;

    @JsonProperty("Longitude")
    private String longitude;

    @JsonProperty("Latitude")
    private String latitude;

    @JsonProperty("PostFinance")
    private String postFinance;

    @JsonProperty("BicycleParking")
    private String bicycleParking;

    @JsonProperty("PaymentAccess")
    private String paymentAccess;

    @JsonProperty("POSTerminal")
    private String posTerminal;

    @JsonProperty("InternationalShipping")
    private String internationalShipping;

    @JsonProperty("SelfServiceWorkplacesCount")
    private String selfServiceWorkplacesCount;

    @JsonProperty("TotalMaxWeightAllowed")
    private String totalMaxWeightAllowed;

    @JsonProperty("PlaceMaxWeightAllowed")
    private String placeMaxWeightAllowed;

    @JsonProperty("DistrictCode")
    private String districtCode;

    @JsonProperty("WarehouseStatus")
    private String warehouseStatus;

    @JsonProperty("WarehouseStatusDate")
    private String warehouseStatusDate;

    @JsonProperty("CategoryOfWarehouse")
    private String categoryOfWarehouse;

    @JsonProperty("Direct")
    private String direct;

    @JsonProperty("RegionCity")
    private String regionCity;

    @JsonProperty("WarehouseForAgent")
    private String warehouseForAgent;

    @JsonProperty("GeneratorEnabled")
    private String generatorEnabled;

    @JsonProperty("MaxDeclaredCost")
    private String maxDeclaredCost;

    @JsonProperty("WorkInMobileAwesome")
    private String workInMobileAwesome;

    @JsonProperty("DenyToSelect")
    private String denyToSelect;

    @JsonProperty("CanGetMoneyTransfer")
    private String canGetMoneyTransfer;

    @JsonProperty("HasMirror")
    private String hasMirror;

    @JsonProperty("HasFittingRoom")
    private String hasFittingRoom;

    @JsonProperty("OnlyReceivingParcel")
    private String onlyReceivingParcel;

    @JsonProperty("PostMachineType")
    private String postMachineType;

    @JsonProperty("PostalCodeUA")
    private String postalCodeUA;

    @JsonProperty("WarehouseIndex")
    private String warehouseIndex;

    @JsonProperty("BeaconCode")
    private String beaconCode;

    // ✅ Удобные методы для UI
    public String getDisplayName() {
        if (number != null && description != null) {
            return "№" + number + ": " + description;
        }
        return description != null ? description : "Відділення";
    }

    public boolean isActive() {
        return !"1".equals(denyToSelect) && "Working".equals(warehouseStatus);
    }

    public boolean hasPaymentTerminal() {
        return "1".equals(posTerminal);
    }
}