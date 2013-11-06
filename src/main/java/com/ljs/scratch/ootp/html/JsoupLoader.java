package com.ljs.scratch.ootp.html;

import com.ljs.scratch.util.Documents;
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
