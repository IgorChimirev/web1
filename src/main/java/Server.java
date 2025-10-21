import com.fastcgi.FCGIInterface;
import validation.Validate;
import check.Checker;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {
    public static void main(String[] args) {
        System.err.println("Starting server...");
        FCGIInterface fcgiInterface = new FCGIInterface();
        Validate v = new Validate();
        Checker checker = new Checker();

        while (fcgiInterface.FCGIaccept() >= 0) {
            System.err.println("Received request");

            String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
            System.err.println("Method: " + method);

            if (!"GET".equals(method)) {
                System.err.println("Invalid method: " + method);
                System.out.println(err("method"));
                continue;
            }

            long startTime = System.nanoTime();
            String req = FCGIInterface.request.params.getProperty("QUERY_STRING");
            System.err.println("Query string: " + req);

            if (req == null || req.isEmpty()) {
                System.err.println("Empty query string");
                System.out.println(err("fill"));
                continue;
            }

            Map<String, String> m = getValues(req);
            System.err.println("Parsed parameters: " + m);

            Float x = parseFloat(m.get("x"));
            Float y = parseFloat(m.get("y"));
            Float r = parseFloat(m.get("r"));
            String timeZone = m.getOrDefault("timeZone", "UTC");

            System.err.println("Parsed values - x: " + x + ", y: " + y + ", r: " + r + ", timeZone: " + timeZone);

            if (x == null || y == null || r == null) {
                System.err.println("Null values detected");
                System.out.println(err("Invalid data"));
                continue;
            }

            if (!v.check(x, y, r)) {
                System.err.println("Validation failed: " + v.getErr());
                System.out.println(err(v.getErr()));
                continue;
            }

            boolean isShot = checker.hit(x, y, r);
            System.err.println("Check result: " + isShot);

            System.out.println(resp(isShot, x.toString(), y.toString(), r.toString(), startTime, timeZone));
            System.err.println("Response sent");
        }
    }

    private static Map<String, String> getValues(String inpString) {
        String[] args = inpString.split("&");
        Map<String, String> map = new LinkedHashMap<>();
        for (String s : args) {
            String[] arg = s.split("=");
            if (arg.length == 2) {
                map.put(arg[0], arg[1]);
            }
        }
        return map;
    }

    private static String resp(boolean isShoot, String x, String y, String r, long startTime, String timeZone) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);
        } catch (Exception e) {
            System.err.println("Invalid timezone: " + timeZone + ", using UTC");
            zoneId = ZoneId.of("UTC");
        }

        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss z");
        String workTime = currentTime.format(formatter);
        String processingTime = String.format("%.5f", (System.nanoTime() - startTime) / 1_000_000_000.0);

        String content = String.format(
                "{\"result\":\"%s\",\"x\":\"%s\",\"y\":\"%s\",\"r\":\"%s\",\"time\":\"%s\",\"workTime\":\"%s\",\"error\":\"all ok\"}",
                isShoot, x, y, r, processingTime, workTime
        );

        return String.format(
                "Content-Type: application/json; charset=utf-8\nContent-Length: %d\n\n%s",
                content.getBytes(StandardCharsets.UTF_8).length,
                content
        );
    }

    private static String err(String msg) {
        String content = String.format("{\"error\":\"%s\"}", msg);
        return String.format(
                "Content-Type: application/json; charset=utf-8\nContent-Length: %d\n\n%s",
                content.getBytes(StandardCharsets.UTF_8).length,
                content
        );
    }

    private static Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse integer: " + s);
            return null;
        }
    }

    private static Float parseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse float: " + s);
            return null;
        }
    }
}