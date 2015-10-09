package l.files.preview;

import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Stat;

import static l.files.preview.Preview.PALETTE_MAX_COLOR_COUNT;

final class PaletteCache extends PersistenceCache<Palette> {

    PaletteCache(File cacheDir) {
        super(cacheDir);
    }

    @Override
    String cacheFileName() {
        return "palettes";
    }

    @Override
    Palette read(DataInput in) throws IOException {
        int size = in.readInt();
        if (size <= 0 || size > PALETTE_MAX_COLOR_COUNT) {
            throw new InvalidObjectException("size=" + size);
        }
        List<Swatch> swatches = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int color = in.readInt();
            int population = in.readInt();
            swatches.add(new Swatch(color, population));
        }
        return Palette.from(swatches);
    }

    @Override
    void write(DataOutput out, Palette palette) throws IOException {
        List<Swatch> swatches = palette.getSwatches();
        out.writeInt(swatches.size());
        for (Swatch swatch : swatches) {
            out.writeInt(swatch.getRgb());
            out.writeInt(swatch.getPopulation());
        }
    }

    @Override
    Snapshot<Palette> put(File res, Stat stat, Rect constraint, Palette value) {
        if (value.getSwatches().isEmpty()) {
            return null;
        }
        return super.put(res, stat, constraint, value);
    }

}
