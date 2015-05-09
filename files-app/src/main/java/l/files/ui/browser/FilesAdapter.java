package l.files.ui.browser;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;

import java.text.DateFormat;
import java.util.Date;

import l.files.R;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.ui.StableAdapter;
import l.files.ui.browser.FileListItem.File;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.SANS_SERIF;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Instant.EPOCH;
import static l.files.ui.FilesApp.getBitmapCache;
import static l.files.ui.IconFonts.getBackgroundResourceForFileMediaType;
import static l.files.ui.IconFonts.getDefaultBackgroundResource;
import static l.files.ui.IconFonts.getDefaultFileIcon;
import static l.files.ui.IconFonts.getDirectoryIcon;
import static l.files.ui.IconFonts.getIconForFileMediaType;

final class FilesAdapter extends StableAdapter<FileListItem>
{
    private final Function<Stat, CharSequence> dateFormatter;
    private final ImageDecorator imageDecorator;

    FilesAdapter(
            final Context context,
            final int maxThumbnailWidth,
            final int maxThumbnailHeight)
    {
        this.dateFormatter = newDateFormatter(context);
        this.imageDecorator = new ImageDecorator(
                getBitmapCache(context), maxThumbnailWidth, maxThumbnailHeight);
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(final int position)
    {
        return getItem(position).isFile() ? 1 : 0;
    }

    @Override
    public boolean isEnabled(final int position)
    {
        return getItem(position).isFile();
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public View getView(
            final int position,
            final View view,
            final ViewGroup parent)
    {
        final FileListItem item = getItem(position);
        if (item.isHeader())
        {
            return getHeaderView(item, view, parent);
        }
        else
        {
            return getFileView((File) item, view, parent);
        }
    }

    private View getFileView(
            final File file,
            final View recycled,
            final ViewGroup parent)
    {
        final FileViewHolder holder;
        final View view;
        if (recycled == null)
        {
            view = inflate(R.layout.files_item, parent);
            holder = new FileViewHolder(view);
            view.setTag(holder);
        }
        else
        {
            view = recycled;
            holder = (FileViewHolder) view.getTag();
        }

        holder.setTitle(file);
        holder.setIcon(file);
        holder.setSymlink(file);
        holder.setDate(file);
        holder.setSize(file);
        holder.setPreview(file);

        return view;
    }

    private View getHeaderView(
            final Object item,
            final View recycled,
            final ViewGroup parent)
    {
        final HeaderViewHolder holder;
        final View view;
        if (recycled == null)
        {
            view = inflate(R.layout.files_item_header, parent);
            holder = new HeaderViewHolder(view);
            view.setTag(holder);
        }
        else
        {
            view = recycled;
            holder = (HeaderViewHolder) view.getTag();
        }
        holder.title.setText(item.toString());
        return view;
    }

    private static Function<Stat, CharSequence> newDateFormatter(
            final Context context)
    {
        final DateFormat dateFormat = getDateFormat(context);
        final DateFormat timeFormat = getTimeFormat(context);
        final Date date = new Date();
        final Time t1 = new Time();
        final Time t2 = new Time();
        final int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;

        return new Function<Stat, CharSequence>()
        {
            @Override
            public CharSequence apply(final Stat file)
            {
                final Instant instant = file.modificationTime();
                if (instant.equals(EPOCH))
                {
                    return context.getString(R.string.__);
                }

                final long millis = instant.to(MILLISECONDS);
                date.setTime(millis);
                t1.setToNow();
                t2.set(millis);
                if (t1.year == t2.year)
                {
                    if (t1.yearDay == t2.yearDay)
                    {
                        return timeFormat.format(date);
                    }
                    else
                    {
                        return formatDateTime(context, millis, flags);
                    }
                }
                return dateFormat.format(date);
            }
        };
    }

    static FilesAdapter get(final Context context)
    {
        final Resources res = context.getResources();
        final DisplayMetrics metrics = res.getDisplayMetrics();

        final int width = metrics.widthPixels
                - (int) (applyDimension(COMPLEX_UNIT_DIP, 90, metrics) + 0.5f);

        final int height = (int) (metrics.heightPixels * 0.6f);

        return new FilesAdapter(context, width, height);
    }

    @Override
    protected Object getItemIdObject(final int position)
    {
        final FileListItem item = getItem(position);
        if (item instanceof File)
        {
            return ((File) item).getResource();
        }
        return item;
    }

    private class FileViewHolder
    {
        final TextView icon;
        final TextView title;
        final TextView date;
        final TextView size;
        final TextView symlink;
        final ImageView preview;

        FileViewHolder(final View root)
        {
            icon = (TextView) root.findViewById(R.id.icon);
            title = (TextView) root.findViewById(R.id.title);
            date = (TextView) root.findViewById(R.id.date);
            size = (TextView) root.findViewById(R.id.size);
            symlink = (TextView) root.findViewById(R.id.symlink);
            preview = (ImageView) root.findViewById(R.id.preview);
        }

        void setTitle(final File file)
        {
            title.setText(file.getResource().name());
            title.setEnabled(file.getStat() != null && file.isReadable());
        }

        void setIcon(final File file)
        {
            final Context context = icon.getContext();
            final AssetManager assets = context.getAssets();
            icon.setEnabled(file.getTargetStat() != null && file.isReadable());

            if (file.getTargetStat() != null
                    && setLocalIcon(icon, file.getTargetStat()))
            {
                icon.setTypeface(SANS_SERIF, BOLD);
            }
            else
            {
                icon.setText(R.string.ic_font_char);
                icon.setTypeface(getIcon(file, assets));
            }
            icon.setBackgroundResource(getIconBackgroundResource(file));
        }

        boolean setLocalIcon(final TextView icon, final Stat stat)
        {
            if (stat.isBlockDevice())
            {
                icon.setText("B");
            }
            else if (stat.isCharacterDevice())
            {
                icon.setText("C");
            }
            else if (stat.isSocket())
            {
                icon.setText("S");
            }
            else if (stat.isFifo())
            {
                icon.setText("P");
            }
            else
            {
                return false;
            }
            return true;
        }

        private Typeface getIcon(
                final File file,
                final AssetManager assets)
        {
            final Stat stat = file.getStat();
            if (stat == null)
            {
                return getDefaultFileIcon(assets);
            }
            if (stat.isDirectory() || file.getTargetStat().isDirectory())
            {
                return getDirectoryIcon(assets, file.getResource());
            }
            else
            {
                return getIconForFileMediaType(assets, file.getBasicMediaType());
            }
        }

        private int getIconBackgroundResource(final File file)
        {
            final Stat stat = file.getStat();
            if (stat == null
                    || stat.isDirectory()
                    || file.getTargetStat().isDirectory())
            {
                return getDefaultBackgroundResource();
            }
            else
            {
                return getBackgroundResourceForFileMediaType(
                        file.getBasicMediaType());
            }
        }

        void setDate(final File file)
        {
            final Stat stat = file.getStat();
            if (stat == null)
            {
                date.setText("");
                date.setVisibility(GONE);
            }
            else
            {
                date.setEnabled(file.isReadable());
                date.setText(dateFormatter.apply(stat));
            }
        }

        void setSize(final File file)
        {
            final Stat stat = file.getStat();
            if (stat == null)
            {
                size.setText("");
                size.setVisibility(GONE);
            }
            else
            {
                size.setEnabled(file.isReadable());
                size.setText(stat.isDirectory()
                        ? ""
                        : formatShortFileSize(size.getContext(), stat.size()));
                size.setVisibility(stat.isRegularFile() ? VISIBLE : GONE);
            }
        }

        void setPreview(final File file)
        {
            if (file.getStat() == null)
            {
                preview.setImageDrawable(null);
                preview.setVisibility(GONE);
            }
            else
            {
                imageDecorator.decorate(
                        preview, file.getResource(), file.getStat());
            }
        }

        void setSymlink(final File file)
        {
            final Stat stat = file.getStat();
            if (stat == null || !stat.isSymbolicLink())
            {
                symlink.setVisibility(GONE);
            }
            else
            {
                symlink.setEnabled(file.isReadable());
                symlink.setVisibility(VISIBLE);
            }
        }

    }

    private class HeaderViewHolder
    {
        final TextView title;

        HeaderViewHolder(final View root)
        {
            title = (TextView) root.findViewById(android.R.id.title);
        }
    }

}
