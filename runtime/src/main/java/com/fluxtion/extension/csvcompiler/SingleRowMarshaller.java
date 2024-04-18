package com.fluxtion.extension.csvcompiler;

import java.util.Iterator;

/**
 * Wraps a {@link RowMarshaller} to parse single messages with a call to {@link #parse(String)}
 *
 * @param <T> The target type of the {@link RowMarshaller}
 */
public class SingleRowMarshaller<T> {

    private final OverwritingStringReader overwritingStringReader = new OverwritingStringReader();
    private final RowMarshaller<T> rowMarshaller;
    private final Iterator<T> iterator;

    public SingleRowMarshaller(RowMarshaller<T> rowMarshaller) {
        this.rowMarshaller = rowMarshaller;
        iterator = rowMarshaller.iterator(overwritingStringReader);
    }

    /**
     * Tries to marshall a message into an instance of the target type of the {@link RowMarshaller}
     * @param message the input message to process
     *
     * @return A marshalled instance or null if the message could not be transformed into a valid instance of the target type
     */
    public T parse(String message) {
        overwritingStringReader.append(message);
        return iterator.hasNext() ? iterator.next() : null;
    }

    public void writeHeaders(StringBuilder builder) {
        rowMarshaller.writeHeaders(builder);
    }

    public void writeInputHeaders(StringBuilder builder) {
        rowMarshaller.writeInputHeaders(builder);
    }
}
