package mikhail;
import com.fastcgi.FCGIInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
  private static final String HTTP_RESPONSE = """
    HTTP/1.1 200 OK
    Content-Type: application/json
    Content-Length: %d

    %s
    """;
  private static final String HTTP_ERROR = """
    HTTP/1.1 400 Bad Request
    Content-Type: application/json
    Content-Length: %d

    %s
    """;
  private static final String RESULT_JSON = """
    {
        "time": "%s нс",
        "now": "%s",
        "result": %b
    }
    """;
  private static final String ERROR_JSON = """
    {
        "now": "%s",
        "reason": "%s"
    }
    """;

  public static void main(String[] args) {
    var fcgi = new FCGIInterface();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Формат времени

    while (fcgi.FCGIaccept() >= 0) {
      try {
        String requestMethod = System.getProperties().getProperty("REQUEST_METHOD");
        if (!"POST".equals(requestMethod)) {
          throw new ValidationException("Поддерживаются только POST запросы");
        }

        // Получаем заголовок Content-Length
        String contentLengthHeader = System.getProperties().getProperty("CONTENT_LENGTH");
        if (contentLengthHeader == null) {
          throw new ValidationException("Отсутствует заголовок Content-Length");
        }

        int contentLength = Integer.parseInt(contentLengthHeader);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        char[] bodyChars = new char[contentLength];
        reader.read(bodyChars, 0, contentLength);

        String requestBody = new String(bodyChars);
        var params = new Parameters(requestBody);

        var startTime = Instant.now();
        var result = calculate(params.getX(), params.getY(), params.getR()); // расчет
        var endTime = Instant.now();

        // Расчет времени работы и форматирование
        long timeTakenNanos = ChronoUnit.NANOS.between(startTime, endTime);
        String formattedNow = LocalDateTime.now().format(formatter); // Форматируем текущее время

        // Формируем JSON ответ
        var json = String.format(RESULT_JSON, timeTakenNanos, formattedNow, result);
        var response = String.format(HTTP_RESPONSE, json.getBytes(StandardCharsets.UTF_8).length, json);
        System.out.println(response);
      } catch (ValidationException | IOException e) {
        var formattedNow = LocalDateTime.now().format(formatter);
        var json = String.format(ERROR_JSON, formattedNow, e.getMessage());
        var response = String.format(HTTP_ERROR, json.getBytes(StandardCharsets.UTF_8).length, json);
        System.out.println(response);
      }
    }
  }

  private static boolean calculate(float x, float y, float r) {
    if (x > 0 && y > 0) {
      return false;
    }

    if (x < 0 && y > 0) {
      if (x < -r || y > r) {
        return false;
      }
    }

    if (x < 0 && y < 0) {
      if ((x * x + y * y) > r * r) {
        return false;
      }
    }

    if (x > 0 && y < 0) {
      return !(x > r) && !(y < -r);
    }
    return true;
  }
}
