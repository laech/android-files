package l.files.base.text;

import androidx.annotation.NonNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

/**
 * Like a locale sensitive {@link java.text.CollationKey} but performs more
 * natural sorting for numbers.
 * <p/>
 * This provides more meaning to humans, for example if you have a list of book
 * chapters to be sorted:
 * <p/>
 * <pre>
 *   1. Introduction
 *   1.1. xxx
 *   1.2. yyy
 *        ...
 *   1.9. aaa
 *   1.10. bbb
 * </pre>
 * <p/>
 * This class will sort them as listed above.
 */
public final class CollationKey implements Comparable<CollationKey> {

    private final Segment[] segments;

    private CollationKey(Segment[] segments) {
        this.segments = segments;
    }

    public static CollationKey create(Collator collator, String value) {
        char[] chars = value.toCharArray();
        List<Segment> segments = new ArrayList<>();

        for (int start = 0, end = start; end < chars.length; start = end) {
            end++;

            char c = chars[start];
            if (isDigit(c)) {
                while (end < chars.length && isDigit(chars[end])) {
                    end++;
                }

            } else if (isWhitespace(c)) {
                while (end < chars.length && isWhitespace(chars[end])) {
                    end++;
                }

            } else {
                while (end < chars.length &&
                    !isDigit(chars[end]) &&
                    !isWhitespace(chars[end])) {
                    end++;
                }
            }

            if (end > start) {
                byte[] collationKey =
                    collator.getCollationKey(value.substring(start, end))
                        .toByteArray();
                segments.add(new Segment(
                    chars,
                    start,
                    end,
                    isDigit(c),
                    collationKey
                ));
            }

        }
        return new CollationKey(segments.toArray(new Segment[segments.size()]));
    }

    private static boolean isDigit(char c) {
        return c >= '0' & c <= '9';
    }

    @Override
    public int compareTo(@NonNull CollationKey that) {
        int i;
        int len = min(this.segments.length, that.segments.length);
        for (i = 0; i < len; i++) {
            Segment a = this.segments[i];
            Segment b = that.segments[i];
            int compare = (a.isNumber && b.isNumber)
                ? compareNumber(
                a.chars,
                a.start,
                a.end,
                b.chars,
                b.start,
                b.end
            )
                : compare(a.collectionKey, b.collectionKey);
            if (compare != 0) {
                return compare;
            }
        }

        return (this.segments.length - i) - (that.segments.length - i);
    }

    private static int compareNumber(
        char[] a, int startA, int endA,
        char[] b, int startB, int endB
    ) {

        int startOffsetA = skipLeadingZeros(a, startA, endA);
        int startOffsetB = skipLeadingZeros(b, startB, endB);
        int compare = Integer.compare(endA - startOffsetA, endB - startOffsetB);
        if (compare == 0) {
            return compareChars(a, startOffsetA, endA, b, startOffsetB, endB);
        }
        return compare;
    }

    private static int compare(byte[] a, byte[] b) {
        int minSize = min(a.length, b.length);
        for (int i = 0; i < minSize; ++i) {
            if (a[i] != b[i]) {
                return (a[i] & 0xFF) - (b[i] & 0xFF);
            }
        }
        return a.length - b.length;
    }

    private static int compareChars(
        char[] a, int startA, int endA,
        char[] b, int startB, int endB
    ) {

        int len = min(endA - startA, endB - startB);
        int i;
        for (i = 0; i < len; i++) {
            int compare = a[i + startA] - b[i + startB];
            if (compare != 0) {
                return compare;
            }
        }
        return (endA - startA - i) - (endB - startB - i);
    }

    private static int skipLeadingZeros(char[] chars, int start, int end) {
        int i = start;
        while (i < end && chars[i] == '0') {
            i++;
        }
        return i;
    }

    private static final class Segment {

        final char[] chars;
        final int start;
        final int end;
        final boolean isNumber;
        final byte[] collectionKey;

        Segment(
            char[] chars,
            int start,
            int end,
            boolean isNumber,
            byte[] collectionKey
        ) {
            this.chars = chars;
            this.start = start;
            this.end = end;
            this.isNumber = isNumber;
            this.collectionKey = collectionKey;
        }

    }

}
