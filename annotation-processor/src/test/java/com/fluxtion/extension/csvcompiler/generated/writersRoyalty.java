package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.FieldAccessor;
import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;


@CsvMarshaller(
        acceptPartials = false,
        mappingRow = 1,
        headerLines = 0,
        skipCommentLines = true,
        processEscapeSequences = true,
        skipEmptyLines = false,
        fieldSeparator = '\t',
        ignoreQuotes = false,
        trim = true,
        failOnFirstError = false
)
public final class writersRoyalty implements FieldAccessor {
    @ColumnMapping(
            columnName = "Work Writer List",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = "",
            escapeOutput = false
    )
    private String artistnamesource;

    @ColumnMapping(
            columnName = "Work Primary Title",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = "",
            escapeOutput = true
    )
    private String trackNameSource;

    public String getArtistnamesource() {
        return artistnamesource;
    }

    public void setArtistnamesource(String artistnamesource) {
        this.artistnamesource = artistnamesource;
    }

    public String getTrackNameSource() {
        return trackNameSource;
    }

    public void setTrackNameSource(String trackNameSource) {
        this.trackNameSource = trackNameSource;
    }

    @Override
    public <T> T getField(String fieldName) {
        switch(fieldName) {
            case "artistnamesource":
                return (T)(Object)artistnamesource;
            case "trackNameSource":
                return (T)(Object)trackNameSource;
            default:
                break;
        }
        return null;
    }

    public String toString() {
        String toString = "writersRoyalty: {";
        toString += "artistnamesource: " + artistnamesource + ", ";
        toString += "trackNameSource: " + trackNameSource;
        toString += "}";
        return toString;
    }
}


