package com.ljs.scratch.ootp.report;

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
public final class Reports {

    private Reports() { }

    public static ReportDestination print(final Report r) {
        return new ReportDestination() {
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

    public interface ReportDestination {
        void to(OutputStream out);
        void to(Writer w);
    }



}
