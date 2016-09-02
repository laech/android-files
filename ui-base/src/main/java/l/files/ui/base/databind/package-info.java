@NonnullByDefault
package l.files.ui.base.databind;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.widget.ImageView;

import l.files.base.annotation.NonnullByDefault;

@BindingMethods({
        @BindingMethod(
                type = ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageResource"
        )
})
class DataBindings {
}
