package com.ljs.scratch.ootp.io;

import com.google.common.base.Charsets;
import com.ljs.scratch.ootp.annotation.ReturnTypesAreNonnullByDefault;
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
@ReturnTypesAreNonnullByDefault
public final class Printables {

    private Printables() { }

    public static PrintDestination print(final Printable r) {
        return new PrintDestination() {
            @Override
            public void to(OutputStream out) {
                to(new OutputStreamWriter(out, Charsets.ISO_8859_1));
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
