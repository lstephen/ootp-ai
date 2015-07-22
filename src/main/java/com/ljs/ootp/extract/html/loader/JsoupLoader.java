package com.ljs.ootp.extract.html.loader;

import com.ljs.ootp.extract.html.Documents;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public class JsoupLoader implements PageLoader {

    @Override
    public Document load(String url) {
        return Documents.load(url);
    }

}
