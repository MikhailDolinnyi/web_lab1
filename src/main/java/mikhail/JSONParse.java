package mikhail;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class JSONParse {


    public JSONObject parse(String jsonString) throws ValidateException {
        try {
            // Парсинг JSON-строки
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(jsonString);

        } catch (ParseException e) {
            throw new ValidateException("Ошибка парсинга из JSON");
        }
    }





}


