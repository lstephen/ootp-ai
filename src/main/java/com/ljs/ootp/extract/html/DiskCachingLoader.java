package com.ljs.ootp.extract.html;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.cache.AbstractCache;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class DiskCachingLoader implements PageLoader {

    private static final String CACHE_DIR = "c:/ootp/cache";

    private static final DiskCache CACHE = DiskCache.cachingTo(CACHE_DIR);

    private final PageLoader wrapped;

    private DiskCachingLoader(PageLoader wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Document load(String url) {
        return CACHE.get(url, PageLoaders.asCallable(wrapped, url));
    }

    private static final class DiskCache
        extends AbstractCache<String, Document> {

        private static final Charset CHARSET = Charsets.UTF_8;

        private final String dir;

        private DiskCache(String dir) {
            super();
            this.dir = dir;
        }

        private File getFileForKey(Object key) {

            String pathName =
                StringUtils.substringBeforeLast(key.toString(), "/");

            try {
                File path = new File(
                    dir,
                    URLEncoder.encode(pathName, CHARSET.name()));

                String fileName =
                    StringUtils.substringAfterLast(key.toString(), "/");

                File forKey = new File(
                    path,
                    URLEncoder.encode(fileName, CHARSET.name()));

                Files.createParentDirs(forKey);

                return forKey;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void put(String key, Document value) {
            try {
                Files.write(value.outerHtml(), getFileForKey(key), CHARSET);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public Document get(String key, Callable<? extends Document> callable) {
            Document doc = getIfPresent(key);

            if (doc == null) {
                try {
                    put(key, callable.call());
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
                return getIfPresent(key);
            } else {
                return doc;
            }
        }

        @Override
        public Document getIfPresent(Object key) {
            File f = getFileForKey(key);

            if (f.exists()) {
                try {
                    return Jsoup.parse(f, CHARSET.name());
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            } else {
                return null;
            }
        }

        public static DiskCache cachingTo(String dir) {
            return new DiskCache(dir);
        }
    }

    public static DiskCachingLoader wrap(PageLoader loader) {
        return new DiskCachingLoader(loader);
    }

}
