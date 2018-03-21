
package monitor;

import java.util.ArrayList;
import java.util.List;

public class BusMonitor {

    public static List<StatusReporter> reporters = new ArrayList<StatusReporter>();

    static {

    }

    public static void addReport(StatusReporter reporter) {
        reporters.add(reporter);
    }

    public static void appendReport(StringBuilder sb) {
        for (StatusReporter reporter : reporters) {
            reporter.appendReport(sb);
        }
    }
}
