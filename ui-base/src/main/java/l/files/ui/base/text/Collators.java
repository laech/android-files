package l.files.ui.base.text;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import java.util.Locale;

public final class Collators {

    private Collators() {
    }

    public static Collator of(Locale locale) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(locale);
        collator.setNumericCollation(true);
        collator.freeze();
        return collator;
    }

}
