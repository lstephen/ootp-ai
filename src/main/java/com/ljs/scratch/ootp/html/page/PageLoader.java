package com.ljs.scratch.ootp.html.page;

import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public interface PageLoader {

    Document load(String url);

}
