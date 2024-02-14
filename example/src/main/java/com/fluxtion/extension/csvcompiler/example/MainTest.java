package com.fluxtion.extension.csvcompiler.example;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import lombok.SneakyThrows;

import java.nio.file.Path;

public class MainTest {

    @SneakyThrows
    public static void main(String[] args) {
        RowMarshaller.load(HousingData.class).transform(
                Path.of("data/AmesHousing.csv"),
                Path.of("data/PostProcess.csv"),
                s -> s.filter(MainTest::validFrontageValue)
                        .map(MainTest::squareLotFrontage)
                        .map(MainTest::ms_zone_to_category)
                        .filter(MainTest::validZoneCategory));
    }

    public static HousingData squareLotFrontage(HousingData housingData) {
        int lotFrontage = housingData.Lot_Frontage();
        housingData.Lot_Frontage_Squared(lotFrontage * lotFrontage);
        return housingData;
    }

    public static HousingData ms_zone_to_category(HousingData housingData) {
        switch (housingData.MS_Zoning()) {
            case "A" -> housingData.ms_zone_category(1);
            case "FV" -> housingData.ms_zone_category(2);
            case "RL" -> housingData.ms_zone_category(3);
            case "RM" -> housingData.ms_zone_category(4);
            default -> housingData.ms_zone_category(-1);
        }
        return housingData;
    }

    public static boolean validFrontageValue(HousingData h) {
        return h.Lot_Frontage() > 0;
    }

    public static boolean validZoneCategory(HousingData h) {
        return h.ms_zone_category() > 0;
    }
}
