package alex.weather;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherHelper {
  private static final String WEATHER_API_KEY = "";
  private static final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather";
  private final HttpClient _client = HttpClient.newHttpClient();
  private final ObjectMapper _mapper = new ObjectMapper();
  
  public WeatherModel getWeatherDetails(double latitude, double longitude) throws IOException, InterruptedException {
    final HttpRequest req = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2)
        .uri(URI.create(new StringBuilder(URL_BASE)
             .append("?lat=").append(Double.toString(latitude)).append("&lon=").append(Double.toString(longitude))
             .append("&units=imperial").append("&appid=").append(WEATHER_API_KEY).toString())).GET().build();
    final HttpResponse<String> response = _client.send(req, HttpResponse.BodyHandlers.ofString());
    return _mapper.readValue(response.body(), WeatherModel.class);
  }
}
