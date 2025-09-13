package util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SizeUtil {
    private SizeUtil() {}
    private static final long KB = 1024L, MB = 1024L * KB, GB = 1024L * MB;
    private static final Pattern RX = Pattern.compile("^\\s*([0-9]+)\\s*(KB|MB|GB)?\\s*$", Pattern.CASE_INSENSITIVE);

    public static Long parseBytesSafe(String text) {
        if (text == null) return null;
        Matcher m = RX.matcher(text);
        if (!m.matches()) return null;
        long val = Long.parseLong(m.group(1));
        String unit = m.group(2);
        if (unit == null) return val;
        return switch (unit.toUpperCase(Locale.ROOT)) {
            case "KB" -> val * KB;
            case "MB" -> val * MB;
            case "GB" -> val * GB;
            default -> null;
        };
    }
}