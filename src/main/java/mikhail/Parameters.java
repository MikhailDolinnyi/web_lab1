package mikhail;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Parameters {
  private final int x;
  private final float y;
  private final float r;

  public Parameters(String jsonString) throws ValidationException {
    if (jsonString == null || jsonString.isEmpty()) {
      throw new ValidationException("Нет тела запроса");
    }

    try {
      // Парсинг JSON-строки
      JSONParser parser = new JSONParser();
      JSONObject jsonObject = (JSONObject) parser.parse(jsonString);  // Парсим строку JSON в объект

      // Достаём значения и валидируем
      this.x = validateX((String) jsonObject.get("x").toString());
      this.y = validateY((String) jsonObject.get("y").toString());
      this.r = validateR((String)jsonObject.get("r").toString());

    } catch (ParseException e) {
      throw new ValidationException("Ошибка парсинга из JSON");
    }
  }

  // Валидация X,Y,R
  private int validateX(String x) throws ValidationException {
    if (x == null || x.isEmpty()) {
      throw new ValidationException("X не имеет значения");
    }
    try {
      int xx = Integer.parseInt(x);
      if (xx < -4 || xx > 4) {
        throw new ValidationException("Неверное значение X");
      }
      return xx;
    } catch (NumberFormatException e) {
      throw new ValidationException("X не является номером");
    }
  }

  private float validateY(String y) throws ValidationException {
    if (y == null || y.isEmpty()) {
      throw new ValidationException("X не имеет значения");
    }
    try {
      float yy = Float.parseFloat(y);
      if (yy < -5 || yy > 5) {
        throw new ValidationException("Неверное значение Y");
      }
      return yy;
    } catch (NumberFormatException e) {
      throw new ValidationException("Y не является номером");
    }
  }

  private float validateR(String r) throws ValidationException {
    if (r == null || r.isEmpty()) {
      throw new ValidationException("R не имеет значения");
    }
    try {
      float rr = Float.parseFloat(r);
      if (rr < 1 || rr > 3) {
        throw new ValidationException("Неверное значение R");
      }
      return rr;
    } catch (NumberFormatException e) {
      throw new ValidationException("R не является номером");
    }
  }


// getters, setters
  public int getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getR() {
    return r;
  }
}
