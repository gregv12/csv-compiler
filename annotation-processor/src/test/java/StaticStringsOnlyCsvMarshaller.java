import com.fluxtion.extension.csvcompiler.BaseMarshaller;
import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.beans.StringsOnly;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.fluxtion.extension.csvcompiler.converters.Conversion.getIdentifier;

@AutoService(RowMarshaller.class)
public class StaticStringsOnlyCsvMarshaller extends BaseMarshaller<StringsOnly> {

    private static final int HEADER_ROWS = 0;
    private boolean escaping = false;
    private boolean prevIsQuote = false;
    private final CharSequenceView setField1 = sequence.view();
    private final CharSequenceView setField3 = sequence.view();
    private final CharSequenceView setField2 = sequence.view();
    private final int fieldIndex_field1 = 0;
    private final int fieldIndex_field3 = 2;
    private final int fieldIndex_field2 = 1;

    public StaticStringsOnlyCsvMarshaller() {
        super(false);
    }

    public Class<StringsOnly> targetClass() {
        return StringsOnly.class;
    }

    public void init() {
        target = new StringsOnly();
        fieldMap.put(fieldIndex_field1, "setField1");
        fieldMap.put(fieldIndex_field3, "setField3");
        fieldMap.put(fieldIndex_field2, "setField2");
    }

    public boolean charEvent(char character) {
        passedValidation = true;
        if (character == '\u0000') {
            return false;
        }
        if (!processChar(character)) {
            return false;
        }
        if (escaping) {
            chars[writeIndex++] = character;
            return false;
        }
        //NEW LOGIC REQUIRED HERE
        if (character == '\n') {
            return processRow();
        }
        if (character == ',') {
            updateFieldIndex();
        }
        chars[writeIndex++] = character;
        return false;
    }

    private boolean processChar(char character) {
        boolean isQuote = character == '"';
        if (!escaping & isQuote) { // first quote
            prevIsQuote = false;
            escaping = true;
            return false;
        } else if (escaping & !prevIsQuote & isQuote) { // possible termination
            prevIsQuote = true;
            return false;
        } else if (escaping & prevIsQuote & !isQuote) { // actual termination
            prevIsQuote = false;
            escaping = false;
        } else if (escaping & prevIsQuote & isQuote) { // an escaped quote
            prevIsQuote = false;
        }
        return true;
    }

    protected boolean processRow() {
        boolean targetChanged = false;
        rowNumber++;
        if (chars[0] == '#') {
            writeIndex = 0;
            fieldIndex = 0;
            return targetChanged;
        }
        if (writeIndex < 1) {
            writeIndex = 0;
            fieldIndex = 0;
            return targetChanged;
        }
        if (HEADER_ROWS < rowNumber) {
            targetChanged = updateTarget();
        }
        writeIndex = 0;
        fieldIndex = 0;
        return targetChanged;
    }

    private boolean updateTarget() {
        int length = 0;
        target = new StringsOnly();
        int maxFieldIndex = fieldIndex;
        try {
            updateFieldIndex();
            if (maxFieldIndex >= fieldIndex_field1) {
                setField1.subSequenceNoOffset(
                        delimiterIndex[fieldIndex_field1],
                        delimiterIndex[fieldIndex_field1 + 1] - 1);
                target.setField1(setField1.toString());
            }
            if (maxFieldIndex >= fieldIndex_field3) {
                setField3.subSequenceNoOffset(
                        delimiterIndex[fieldIndex_field3],
                        delimiterIndex[fieldIndex_field3 + 1] - 1);
                target.setField3(setField3.toString());
            }
            if (maxFieldIndex >= fieldIndex_field2) {
                setField2.subSequenceNoOffset(
                        delimiterIndex[fieldIndex_field2],
                        delimiterIndex[fieldIndex_field2 + 1] - 1);
                target.setField2(setField2.toString());
            }
        } catch (Exception e) {
            logException(
                    "problem pushing '"
                            + sequence.subSequence(
                                            delimiterIndex[fieldIndex],
                                            delimiterIndex[fieldIndex + 1] - 1)
                                    .toString()
                            + "'"
                            + " from row:'"
                            + rowNumber
                            + "'",
                    false,
                    e);
            passedValidation = false;
            return false;
        } finally {
            fieldIndex = 0;
        }
        return true;
    }

    private void mapHeader() {
        String header = new String(chars).trim().substring(0, writeIndex);
        header = headerTransformer.apply(header);
        header = header.replace("\"", "");
        List<String> headers = new ArrayList();
        for (String colName : header.split(Pattern.quote(","))) {
            headers.add(getIdentifier(colName));
        }
    }
}
