/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * CharSequence backed by an array. Views on the array can be accessed using
 * {@link #subSequence(int, int) } returning a {@link CharSequence} that points
 * to the underlying char array. The view can re-point its view by calling
 * {@link #subSequence(int, int) } this will return the original view but with
 * pointers moved to new positions on the underlying array.
 *
 * @author gregp
 */
public class CharArrayCharSequence implements CharSequence {

    private final char[] array;
    private final StringCache cache = new StringCache();

    public CharArrayCharSequence(char[] array) {
        this.array = array;
    }

    @Override
    public char charAt(int index) {
        return array[index];
    }

    @Override
    public int length() {
        return array.length;
    }

    @Override
    public CharSequenceView subSequence(int start, int end) {
        return new CharSequenceView(start, end);
    }

    public CharSequenceView view() {
        return subSequence(0, 0);
    }

    @Override
    public String toString() {
        return new String(array);
    }

    @Override
    public int hashCode() {
        return hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    static int hashCode(final CharSequence csq) {
        int h = 0;
        for (int i = 0, len = csq.length(); i < len; i++) {
            h = 31 * h + csq.charAt(i);
        }
        return h;
    }

    public class CharSequenceView implements CharSequence {

        private int start;
        private int end;
        private int length;
        private boolean updated = true;
        private String myString = null;

        public CharSequenceView(int start, int end) {
            this.start = start;
            this.end = end;
            calcLength();
            updated = true;
        }

        private final void calcLength(){
            length = end - start;
        }

        @Override
        public char charAt(int index) {
            return CharArrayCharSequence.this.charAt(index + start);
        }

        @Override
        public int length() {
            return length;
        }

        public boolean isEmpty() {
            return this.length() == 0;
        }

        public CharSequenceView subSequenceNoOffset(int start, int end) {
            updated = true;
            this.start = start;
            this.end = end;
            calcLength();
            return this;
        }

        @Override
        public CharSequenceView subSequence(int newStart, int newEnd) {
            updated = true;
            this.start = newStart + start;
            this.end = newEnd + start;
            calcLength();
            return this;
        }

        @Override
        public String toString() {
            if(updated){
                myString = new String(array, start, end - start);
            }
            return myString;
        }
        
        public String intern() {
            return (String) cache.intern(this);
        }

        @Override
        public int hashCode() {
            return CharArrayCharSequence.hashCode(CharSequenceView.this);
        }

        @Override
        public boolean equals(Object obj) {
            return toString().equals(obj.toString());
        }

        public final CharSequenceView trim() {
            while ((start < end) && (array[start] <= ' ')) {
                start++;
            }
            while ((start < end) && (array[end - 1] <= ' ')) {
                end--;
            }
            calcLength();
            return this;
        }

    }

    /**
     * A String cache, receives a {@link CharSequence} and checks if there is a
     * match in the cache, returning a cached char sequence that equals the input.
     *
     * This is similar functionality to {@link String#intern() } but does not require
     * a String to intern, only a CharSequence.
     *
     * @author gregp
     */
    public static class StringCache {

        private final ByteBuffer buffer;
        private final byte[] array;
        private static final int DEFAULT_SIZE = 64;
        private final HashMap<ByteBuffer, String> cache;

        public StringCache() {
            array = new byte[DEFAULT_SIZE];
            buffer = ByteBuffer.wrap(array);
            cache = new HashMap<>(256);
        }

        public CharSequence intern(CharSequence cs) {
            ((Buffer)buffer).clear();
            for (int i = 0; i < cs.length(); i++) {
                buffer.put((byte) cs.charAt(i));
            }
            buffer.flip();
            String ret = cache.get(buffer);
            if (ret == null) {
                ret = new String(array, 0, buffer.limit());
                cache.put(ByteBuffer.wrap(Arrays.copyOf(array, buffer.limit())), ret);
            }
            return ret;
        }

        public int cacheSize() {
            return cache.size();
        }

        public void clearCache() {
            cache.clear();
        }

    }
}
