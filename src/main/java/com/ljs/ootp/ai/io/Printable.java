package com.ljs.ootp.ai.io;

import java.io.PrintWriter;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
public interface Printable {

    void print(PrintWriter w);

}
