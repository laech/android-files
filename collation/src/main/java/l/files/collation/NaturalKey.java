package l.files.collation;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import java.util.Locale;

/**
 * Like a locale sensitive {@link CollationKey} but performs more natural
 * sorting for numbers.
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
public final class NaturalKey implements Comparable<NaturalKey> {

    private final CollationKey key;

    NaturalKey(CollationKey key) {
        this.key = key;
    }

    public static NaturalKey create(Collator collator, String value) {
        return new NaturalKey(collator.getCollationKey(value));
    }

    public static Collator collator(Locale locale) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(locale);
        collator.setNumericCollation(true);
        return collator;
    }

    @Override
    public int compareTo(NaturalKey that) {
        return key.compareTo(that.key);
    }

}
