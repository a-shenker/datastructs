package alex.weather;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherModel {
 
  private double _temp;
  private int _humidity;
  private int _pressure;
  private String _description;
  private long _tstamp;

  public WeatherModel() {
  }

  public WeatherModel(double temp, int humidity, int pressure, String description, String city) {
    _temp = temp;
    _humidity = humidity;
    _pressure = pressure;
    _description = description;
    _city = city;
  }

  @JsonProperty("name")
  private String _city;

  @JsonProperty("main")
  private void unpackMain(final Map<String, Object> mainElement) {
    _temp = (Double)mainElement.get("temp");
    _humidity = (Integer)mainElement.get("humidity");
    _pressure = (Integer)mainElement.get("pressure");
  }
  @JsonProperty("weather")
  private void unpackWeather(final List<Map<String, String>> weatherElement) {
    if (!weatherElement.isEmpty()) {
      _description = weatherElement.get(0).getOrDefault("description", "N/A");
    } else {
      _description = "N/A";
    }
  }
  public void setNano(final long tstamp) {
    _tstamp = tstamp;
  }
  public long getNano() {
    return _tstamp;
  }

  @Override
  public String toString() {
    return "WeatherModel [city=" + _city + ", description: " + _description + ", temp=" + _temp + 
           ", humidity=" + _humidity + ", pressure=" + _pressure + "]";
  }
}