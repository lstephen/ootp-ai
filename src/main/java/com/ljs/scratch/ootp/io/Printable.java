package com.ljs.scratch.ootp.io;

import com.ljs.scratch.ootp.annotation.ReturnTypesAreNonnullByDefault;
import java.io.PrintWriter;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
@ReturnTypesAreNonnullByDefault
public interface Printable {

    void print(PrintWriter w);

}
