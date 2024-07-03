package com.bo.testnfc.utility;


import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class IOUtil {
    private IOUtil() {
        throw new AssertionError("create IOUtil instance is forbidden");
    }

    /**
     * Close the IO object
     *
     * @param src Source IO object
     */
    public static void close(Closeable src) {
        if (src != null) {
            try {
                src.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Convert an exception object to a string
     *
     * @param e Exception Object
     * @return Converted string
     */
    public static String exception2String(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /** Sleep for a specified time */
    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
