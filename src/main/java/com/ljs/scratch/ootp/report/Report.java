package com.ljs.scratch.ootp.report;

import com.ljs.scratch.ootp.annotation.ReturnTypesAreNonnullByDefault;
import java.io.PrintWriter;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
@ReturnTypesAreNonnullByDefault
public interface Report {

    void print(PrintWriter w);

}
