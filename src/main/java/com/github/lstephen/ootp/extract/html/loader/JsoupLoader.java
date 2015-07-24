package com.github.lstephen.ootp.extract.html.loader;

import com.github.lstephen.ootp.extract.html.Documents;
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
