package com.ljs.ootp.ai.io;

import com.google.common.base.Charsets;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
public final class Printables {

    private Printables() { }

    public static PrintDestination print(final Printable r) {
        return new PrintDestination() {
            @Override
            public void to(OutputStream out) {
                to(new OutputStreamWriter(out, Charsets.UTF_8));
            }

            @Override
            public void to(Writer w) {
                PrintWriter pw = new PrintWriter(w);
                r.print(pw);
                pw.flush();
            }
        };

    }

    public interface PrintDestination {
        void to(OutputStream out);
        void to(Writer w);
    }

}
