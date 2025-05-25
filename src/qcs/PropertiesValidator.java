package qcs;

import java.io.*;
import java.util.*;

public class PropertiesValidator {

    public static void checkForDuplicateKeys(String path) throws IOException {
        if (!isDebugMode()) return;

        File file = new File(path);
        Map<String, Integer> keyLines = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                    String key = line.substring(0, line.indexOf("=")).trim();

                    if (keyLines.containsKey(key)) {
                        int firstLine = keyLines.get(key);
                        String warning = String.format("DUPLICATE KEY: '%s' first defined at line %d and redefined at line %d in %s",
                                key, firstLine, lineNumber, path);
                        warnings.add(warning);
                    } else {
                        keyLines.put(key, lineNumber);
                    }
                }
            }
        }

        if (!warnings.isEmpty()) {
            // Create log directory
            File logDir = new File("log");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            // Write to log file
            File logFile = new File(logDir, "duplicate_keys.log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                for (String warning : warnings) {
                    writer.println(warning);
                }
            }
            System.out.println("Duplicate key warnings logged to " + logFile.getAbsolutePath());
        } else {
            System.out.println("No duplicate keys found in " + path);
        }
    }

    private static boolean isDebugMode() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
    }
}
