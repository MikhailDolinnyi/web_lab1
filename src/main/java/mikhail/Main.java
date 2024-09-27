package mikhail;

import com.fastcgi.FCGIInterface;
import org.json.simple.JSONObject;

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

    private static final String HTTP_FORBIDDEN = """
            HTTP/1.1 403 Forbidden
            Content-Type: application/json
            Content-Length: %d

            %s
            """;
    private static final String HTTP_BAD_REQUEST = """
            HTTP/1.1 400 Bad Request
            Content-Type: application/json
            Content-Length: %d

            %s
            """;

    private static final String HTTP_NOT_FOUND = """
            HTTP/1.1 404 Not Found
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
                try {
                    String requestMethod = System.getProperties().getProperty("REQUEST_METHOD");
                    if (!"POST".equals(requestMethod)) {
                        throw new ValidateException("Поддерживаются только POST запросы");
                    }

                }catch (ValidateException e){
                    String response = responseError(HTTP_FORBIDDEN,e.getMessage(),formatter);
                    System.out.println(response);
                }

                try{
                    String URI = System.getProperties().getProperty("REQUEST_URI");
                    if(!URI.matches(".*/fcgi-bin/lab-1.jar(/)?")){
                        throw new ValidateException("Неверный URI");
                    }
                }catch (ValidateException e ){
                    String response = responseError(HTTP_NOT_FOUND,e.getMessage(),formatter);
                    System.out.println(response);
                }


                // Получаем заголовок Content-Length
                String contentLengthHeader = System.getProperties().getProperty("CONTENT_LENGTH");
                JSONObject jsonObject = getJsonParse(contentLengthHeader); // преобразование в JSON Object

                Validator validator = new Validator();
                validator.validate(jsonObject); //  валидация значений

                Dto dto = new Dto(); // Data Transfer Object класс для хранения/передачи значений
                dto.setAll(jsonObject);

                var startTime = Instant.now();
                boolean result = checkDot(dto.getX(),dto.getY(),dto.getR()); // расчет
                var endTime = Instant.now();

                // Расчет времени работы и форматирование
                long timeTakenNanos = ChronoUnit.NANOS.between(startTime, endTime);
                String formattedNow = LocalDateTime.now().format(formatter); // Форматируем текущее время

                // Формируем JSON ответ
                var json = String.format(RESULT_JSON, timeTakenNanos, formattedNow, result);
                var response = String.format(HTTP_RESPONSE, json.getBytes(StandardCharsets.UTF_8).length, json);
                System.out.println(response);
            } catch (ValidateException | IOException e) {
                String response = responseError(HTTP_BAD_REQUEST, e.getMessage(), formatter);
                System.out.println(response);
            }
        }
    }



    private static JSONObject getJsonParse(String contentLengthHeader) throws ValidateException, IOException {
        JSONParse jsonParse =  new JSONParse();
        return jsonParse.parse(readSystemIn(contentLengthHeader));
    }

//  Метод для чтения System.in
    private static String readSystemIn(String contentLengthHeader) throws ValidateException, IOException {
        if (contentLengthHeader == null) {
            throw new ValidateException("Отсутствует заголовок Content-Length");
        }

        int contentLength = Integer.parseInt(contentLengthHeader);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        char[] bodyChars = new char[contentLength];
        reader.read(bodyChars, 0, contentLength);
        return new String(bodyChars);

    }

    private static boolean checkDot(int x, float y, float r) {
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


    private static String responseError(String http,String error, DateTimeFormatter formatter){
        var formattedNow = LocalDateTime.now().format(formatter);
        var json = String.format(ERROR_JSON, formattedNow, error);
        return String.format(http, json.getBytes(StandardCharsets.UTF_8).length, json);

    }
}
