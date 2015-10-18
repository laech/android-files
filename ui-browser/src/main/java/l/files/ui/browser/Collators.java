package l.files.ui.browser;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import java.util.Locale;

final class Collators {

    private Collators() {
    }

    static Collator of(Locale locale) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(locale);
        collator.setNumericCollation(true);
        collator.freeze();
        return collator;
    }

}
