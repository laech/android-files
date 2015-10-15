package l.files.collation;

import com.ibm.icu.text.Collator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Locale.CHINESE;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static l.files.collation.NaturalKeyTest.Ord.EQUAL;
import static l.files.collation.NaturalKeyTest.Ord.GREATER_THAN;
import static l.files.collation.NaturalKeyTest.Ord.LESS_THAN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class NaturalKeyTest {

    enum Ord {

        LESS_THAN {
            @Override
            void verify(NaturalKey a, NaturalKey b) {
                assertTrue(a.compareTo(b) < 0);
                assertTrue(b.compareTo(a) > 0);
                assertFalse(a.compareTo(b) > 0);
                assertFalse(b.compareTo(a) < 0);
                assertFalse(a.compareTo(b) == 0);
                assertFalse(b.compareTo(a) == 0);
            }
        },

        GREATER_THAN {
            @Override
            void verify(NaturalKey a, NaturalKey b) {
                assertTrue(a.compareTo(b) > 0);
                assertTrue(b.compareTo(a) < 0);
                assertFalse(a.compareTo(b) < 0);
                assertFalse(b.compareTo(a) > 0);
                assertFalse(b.compareTo(a) == 0);
                assertFalse(b.compareTo(a) == 0);
            }
        },

        EQUAL {
            @Override
            void verify(NaturalKey a, NaturalKey b) {
                assertTrue(a.compareTo(b) == 0);
                assertTrue(b.compareTo(a) == 0);
                assertFalse(a.compareTo(b) > 0);
                assertFalse(a.compareTo(b) < 0);
                assertFalse(b.compareTo(a) > 0);
                assertFalse(b.compareTo(a) < 0);
            }
        };

        abstract void verify(NaturalKey a, NaturalKey b);
    }

    private final Ord ord;
    private final NaturalKey a;
    private final NaturalKey b;

    public NaturalKeyTest(Ord ord, Locale locale, String a, String b) {
        Collator collator = NaturalKey.collator(locale);
        this.ord = requireNonNull(ord);
        this.a = NaturalKey.create(collator, a);
        this.b = NaturalKey.create(collator, b);
    }

    @Parameters(name = "{0} {1} \"{2}\" \"{3}\"")
    public static Collection<Object[]> params() {
        return asList(new Object[][]{
                {LESS_THAN, ENGLISH, ".9", "0"},
                {LESS_THAN, ENGLISH, "", "0"},
                {LESS_THAN, ENGLISH, "0", "1"},
                {LESS_THAN, ENGLISH, "00", "1"},
                {LESS_THAN, ENGLISH, "0", "01"},
                {LESS_THAN, ENGLISH, "1.", "1.2"},
                {LESS_THAN, ENGLISH, "1 Hi", "1. Hi"},
                {LESS_THAN, ENGLISH, "1. Hi", "1.2 Hi"},
                {LESS_THAN, ENGLISH, "1. Hi", "1.2. Hi"},
                {LESS_THAN, ENGLISH, "1.9", "1.a"},
                {LESS_THAN, ENGLISH, "9", "a"},
                {LESS_THAN, ENGLISH, "2", "10"},
                {LESS_THAN, ENGLISH, "2.1", "2.10"},
                {LESS_THAN, ENGLISH, "2", "2.10"},
                {LESS_THAN, ENGLISH, "a1", "a2"},
                {LESS_THAN, ENGLISH, "a3", "a20"},
                {LESS_THAN, ENGLISH, "a3.5", "a20"},
                {LESS_THAN, ENGLISH, "a3.5.7", "a20"},
                {LESS_THAN, ENGLISH, "a3", "a20.1"},
                {LESS_THAN, ENGLISH, "a3.1", "a20.1"},
                {LESS_THAN, ENGLISH, "abc123b1", "abc123b10"},
                {LESS_THAN, ENGLISH, "1-I Love Rock", "1-一公尺"},
                {LESS_THAN, CHINESE, "啊", "只"},

                {GREATER_THAN, ENGLISH, "0", ""},
                {GREATER_THAN, ENGLISH, "1", "0"},
                {GREATER_THAN, ENGLISH, "1", "00"},
                {GREATER_THAN, ENGLISH, "01", "0"},
                {GREATER_THAN, ENGLISH, "1.2", "1."},
                {GREATER_THAN, ENGLISH, "1.2 Hi", "1. Hi"},
                {GREATER_THAN, ENGLISH, "1.2. Hi", "1. Hi"},
                {GREATER_THAN, ENGLISH, "10", "2"},
                {GREATER_THAN, ENGLISH, "1.a", "1.9"},
                {GREATER_THAN, ENGLISH, "a", "9"},
                {GREATER_THAN, ENGLISH, "2.10", "2.1"},
                {GREATER_THAN, ENGLISH, "2.10", "2"},
                {GREATER_THAN, ENGLISH, "a2", "a1"},
                {GREATER_THAN, ENGLISH, "a20", "a3"},
                {GREATER_THAN, ENGLISH, "a20", "a3.1"},
                {GREATER_THAN, ENGLISH, "a20", "a3.1.5"},
                {GREATER_THAN, ENGLISH, "a20.1", "a3"},
                {GREATER_THAN, ENGLISH, "a20.1", "a3.1"},
                {GREATER_THAN, CHINESE, "好", "不"},
                {GREATER_THAN, CHINESE, "只", "啊"},

                {EQUAL, ENGLISH, "", ""},
                {EQUAL, ENGLISH, "0", "0"},
                {EQUAL, ENGLISH, "0", "00"},
                {EQUAL, ENGLISH, "00", "0"},
                {EQUAL, ENGLISH, "1", "1"},
                {EQUAL, ENGLISH, "2", "002"},
                {EQUAL, ENGLISH, "02", "2"},
                {EQUAL, ENGLISH, "0.0", "0.00"},
                {EQUAL, ENGLISH, "0.1", "0.1"},
                {EQUAL, ENGLISH, "1" + Integer.MAX_VALUE + "0", "1" + Integer.MAX_VALUE + "0"},
                {EQUAL, ENGLISH, "1" + Long.MAX_VALUE + "0", "1" + Long.MAX_VALUE + "0"},
                {EQUAL, ENGLISH, "1" + Float.MAX_VALUE + "0", "1" + Float.MAX_VALUE + "0"},
                {EQUAL, ENGLISH, "1" + Double.MAX_VALUE + "0", "1" + Double.MAX_VALUE + "0"},
                {EQUAL, CHINESE, "你", "你"},
        });
    }

    @Test
    public void compare() throws Exception {
        ord.verify(a, b);
    }

}
