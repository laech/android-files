package l.files.ui.browser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class FilesLoader extends AsyncTaskLoader<List<FileListItem>> {

    private static final Logger logger = Logger.get(FilesLoader.class);
    private static final Handler handler = new Handler(getMainLooper());

    private final WatchService service;
    private final ConcurrentMap<Resource, FileListItem.File> data;
    private final EventListener listener;
    private final Runnable deliverResult;

    private final Resource resource;
    private final FileSort sort;
    private final boolean showHidden;

    /**
     * @param resource   the resource to load files from
     * @param sort       the comparator for sorting results
     * @param showHidden whether to show hidden files
     */
    public FilesLoader(Context context, Resource resource,
                       FileSort sort, boolean showHidden) {
        super(context);

        this.resource = requireNonNull(resource, "resource");
        this.sort = requireNonNull(sort, "sort");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.listener = new EventListener();
        this.deliverResult = new DeliverResultRunnable();
        this.service = resource.getResource().getWatcher();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data.isEmpty()) {
            forceLoad();
        } else {
            deliverResult(buildResult());
        }
    }

    @Override
    public List<FileListItem> loadInBackground() {
        data.clear();
        try {
            service.register(resource.getPath(), listener);
        } catch (IOException e) {
            logger.debug(e);
            return emptyList();
        }
        try (Resource.Stream resources = resource.getResource().openDirectory()) {
            for (Resource resource : resources) {
                checkCancelled();
                addData(resource);
            }
        } catch (IOException e) {
            logger.debug(e);
            return emptyList();
        }
        return buildResult();
    }

    private List<FileListItem> buildResult() {
        List<FileListItem.File> files = new ArrayList<>(data.size());
        if (showHidden) {
            files.addAll(data.values());
        } else {
            for (FileListItem.File item : data.values()) {
                if (!item.getPath().isHidden()) {
                    files.add(item);
                }
            }
        }
        Collections.sort(files, sort.newComparator(Locale.getDefault()));

        List<FileListItem> result = new ArrayList<>(files.size() + 6);

        Categorizer categorizer = sort.newCategorizer();
        Resources res = getContext().getResources();
        String preCategory = null;
        for (int i = 0; i < files.size(); i++) {
            FileListItem.File stat = files.get(i);
            String category = categorizer.get(res, stat);
            if (i == 0) {
                if (category != null) {
                    result.add(FileListItem.Header.create(category));
                }
            } else {
                if (!Objects.equals(preCategory, category)) {
                    result.add(FileListItem.Header.create(category));
                }
            }
            result.add(stat);
            preCategory = category;
        }

        return result;
    }

    @Override
    protected void onReset() {
        super.onReset();
        service.unregister(resource.getPath(), listener);
        data.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!isReset()) {
            service.unregister(resource.getPath(), listener);
            logger.error("WatchService has not been unregistered");
        }
    }

    private void checkCancelled() {
        if (isLoadInBackgroundCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /**
     * Adds the new status of the given path to the data map. Returns true if
     * the data map is changed.
     */
    private boolean addData(Resource resource) {
        try {

            ResourceStatus stat = resource.readStatus(false);
            ResourceStatus targetStat = readTargetStatus(stat);
            FileListItem.File newStat = FileListItem.File.create(resource.getPath(), stat, targetStat);
            FileListItem.File oldStat = data.put(resource, newStat);
            return !Objects.equals(newStat, oldStat);

        } catch (FileNotFoundException e) {
            return data.remove(resource) != null;

        } catch (IOException e) {
            data.put(resource, FileListItem.File.create(resource.getPath(), null, null));
            return true;
        }
    }

    private ResourceStatus readTargetStatus(ResourceStatus status) {
        if (status.isSymbolicLink()) {
            try {
                return status.getResource().readStatus(true);
            } catch (IOException e) {
                logger.debug(e);
            }
        }
        return status;
    }

    final class EventListener implements WatchEvent.Listener {
        @Override
        public void onEvent(WatchEvent event) {
            switch (event.getKind()) {
                case CREATE:
                case MODIFY:
                    if (!resource.equals(event.getResource()) && addData(event.getResource())) {
                        redeliverResult();
                    }
                    break;
                case DELETE:
                    if (data.remove(event.getResource()) != null) {
                        redeliverResult();
                    }
                    break;
                default:
                    throw new AssertionError(event);
            }
        }
    }

    private void redeliverResult() {
        handler.removeCallbacks(deliverResult);
        handler.postDelayed(deliverResult, 100);
    }

    final class DeliverResultRunnable implements Runnable {
        @Override
        public void run() {
            deliverResult(buildResult());
        }
    }
}
