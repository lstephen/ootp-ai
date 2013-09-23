package com.ljs.scratch.ootp.html.page;

import com.google.common.base.Throwables;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public class BackgroundLoadingPage implements Page {

    private static final ExecutorService EXECUTOR
        = Executors.newFixedThreadPool(10);

    private final FutureTask<Document> document;

    private BackgroundLoadingPage(Page wrapped) {
        document = new FutureTask<Document>(Pages.asCallable(wrapped));

        EXECUTOR.submit(document);
    }

    @Override
    public Document load() {
        try {
            return document.get();
        } catch (ExecutionException | InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    public static BackgroundLoadingPage wrap(Page page) {
        return new BackgroundLoadingPage(page);
    }

}
