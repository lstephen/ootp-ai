package com.ljs.ootp.extract.html.loader;

import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public interface PageLoader {

    Document load(String url);

}
