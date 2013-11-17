package com.ljs.ootp.ai.io;

import com.ljs.ootp.ai.annotation.ReturnTypesAreNonnullByDefault;
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
