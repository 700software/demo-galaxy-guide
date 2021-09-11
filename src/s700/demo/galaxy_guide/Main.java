package s700.demo.galaxy_guide;

import java.io.*;

public class Main {

    public static void main(String[] args) {

        if (args.length != 1)
            usage();
        String argFile = args[0];

        try (InputStream in = mainInputStream(argFile)) {
            DemoGalaxyGuide guid = new DemoGalaxyGuide();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            for (String line; (line = br.readLine()) != null; ) {
                System.out.println(guid.query(line, true));
            }
        } catch (IOException e) {
            System.err.println("Whoops! " + e);
            System.exit(1);
        }
    }

    private static InputStream mainInputStream(String argFile) {
        InputStream in = null;
        if (argFile.equals("-")) {
            in = System.in;
        } else {
            try {
                in = new FileInputStream(argFile);
            } catch (FileNotFoundException e) {
                System.out.println("Whoops! " + e.getMessage());
                System.exit(1);
            }
        }
        return in;
    }

    private static void usage() {
        String jar = Util.getJar(Main.class);
        System.out.println("Usage: java -jar " + jar + " input-file.txt");
        System.exit(9);
    }
}
