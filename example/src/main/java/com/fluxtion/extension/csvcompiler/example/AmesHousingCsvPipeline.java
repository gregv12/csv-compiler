package com.fluxtion.extension.csvcompiler.example;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import lombok.SneakyThrows;

import java.nio.file.Path;

/**
 * Utility to Transforms the https://www.kaggle.com/datasets/godofprogramming/ameshousing from 82 columns into an output csv with
 * the following properties:
 * <ul>
 *     <li>a subset of the input columns</li>
 *     <li>replace missing values such as Lot frontage with a default value</li>
 *     <li>calculate the Lot frontage squared</li>
 *     <li>remove Lot frontage field from the output</li>
 *     <li>convert MS Zoning from a text value to a numerical value stored in ms_zone_category field</li>
 *     <li>remove MS Zoning field from the output</li>
 *     <li>use fluent accessors to mirror property names, no get/set</li>
 *     <li>filter rows from the output with missing Lot frontage</li>
 *     <li>filter rows from the output that are not in a defined MS Zoning category set</li>
 * </ul>
 *
 * See {@link HouseSaleRecord} for the column definitions that maps fields
 */
public class AmesHousingCsvPipeline {

    @SneakyThrows
    public static void main(String[] args) {
        RowMarshaller.load(HouseSaleRecord.class).transform(
                Path.of("data/AmesHousing.csv"),
                Path.of("data/PostProcess.csv"),
                s -> s.filter(record -> record.Lot_Frontage() > 0)
                        .map(AmesHousingCsvPipeline::squareLotFrontage)
                        .map(AmesHousingCsvPipeline::ms_zone_to_category)
                        .filter(record -> record.ms_zone_category() > 0));
    }

    public static HouseSaleRecord squareLotFrontage(HouseSaleRecord houseSaleRecord) {
        int lotFrontage = houseSaleRecord.Lot_Frontage();
        houseSaleRecord.Lot_Frontage_Squared(lotFrontage * lotFrontage);
        return houseSaleRecord;
    }

    public static HouseSaleRecord ms_zone_to_category(HouseSaleRecord houseSaleRecord) {
        switch (houseSaleRecord.MS_Zoning()) {
            case "A" -> houseSaleRecord.ms_zone_category(1);
            case "FV" -> houseSaleRecord.ms_zone_category(2);
            case "RL" -> houseSaleRecord.ms_zone_category(3);
            case "RM" -> houseSaleRecord.ms_zone_category(4);
            default -> houseSaleRecord.ms_zone_category(-1);
        }
        return houseSaleRecord;
    }
}
