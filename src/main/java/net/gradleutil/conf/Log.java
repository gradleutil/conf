package net.gradleutil.conf;

public class Log {
    LoaderOptions options;

    public Log(LoaderOptions options) {
        this.options = options;
    }

    public void info(String string) {
        if (!options.silent) {
            System.out.println("'conf-info: '" + string);
        }
    }

    void error(String string) {
        if (!options.silent) {
            if (options.config != null) {
                try {
                    System.err.println(LoaderOptions.jsonPrint(options.config, ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.err.println("'conf-info: '" + string);
        }
    }
}
