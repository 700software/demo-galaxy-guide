package s700.demo.galaxy_guide;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Util {

    /**
     * Returns path to jar file in which this class resides. Useful for the usage warning.
     *
     * @see Main#main
     */
    static String getJar(Class clazz) {
        return new File(clazz.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }

    /** @return matcher if there was a match. Null otherwise. */
    public static Matcher matches(String input, Pattern regex) {
        Matcher matcher = regex.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
