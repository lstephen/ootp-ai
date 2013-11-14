package com.ljs.ootp.extract.html;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class Documents {

    private static final Logger LOGGER =
        Logger.getLogger(Documents.class.getName());

    private Documents() { }

    public static Document load(final String url) {
        LOGGER.log(Level.INFO, "Loading page {0}...", url);

        Retryer<Document> retryer = RetryerBuilder
            .<Document>newBuilder()
            .retryIfException()
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .withWaitStrategy(WaitStrategies.exponentialWait())
            .build();

        try {
            return retryer.call(new Callable<Document>() {
                @Override
                public Document call() throws IOException {
                    try (
                        InputStream in = new URL(url).openStream()) {

                        return Jsoup.parse(in, Charsets.ISO_8859_1.name(), "");
                    }
                }
            });
        } catch (RetryException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

}
