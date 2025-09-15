package alex.datastructs;

import alex.weather.WeatherHelper;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import alex.weather.WeatherModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ATBMain {
  private static final Logger log = LoggerFactory.getLogger(ATBMain.class);
  private final AggregatingTransferBuffer<WeatherModel> _atb;
  private final boolean _simulateWeather;
  
  public static void main(String[] argv) {
    try {
      final ATBMain atbMain = new ATBMain(argv[0], argv.length > 1 && argv[1].equals("simul"));
      Executors.newSingleThreadExecutor(r -> new Thread(r, "Consumer")).execute(atbMain::runConsumer);
      Executors.newSingleThreadExecutor(r -> new Thread(r, "Producer")).execute(atbMain::runProducer);
    } catch (final Exception e) {
      log.error("Caught exception", e);
    }
  }

  public ATBMain(final String type, final boolean simulateWeather) {
    switch (type) {
      case "fast":
       _atb = new AggregatingTransferBufferFAST<>(30000, 60000, 500000, Clock.systemDefaultZone());
       break;
      case "slow":
       _atb = new AggregatingTransferBufferSLOW<>(30000, 60000, 500000,  Clock.systemDefaultZone());
       break;
      default:
        throw new IllegalArgumentException("First argument must be either fast or slow");
    }
    _simulateWeather = simulateWeather;
  }

  public void runProducer() {
    final WeatherHelper wh = new WeatherHelper();
    while (true) {
      try {
        final WeatherModel wm = _simulateWeather ?  new WeatherModel(76.06d, 73, 1015, "broken clouds", "New York") :
            wh.getWeatherDetails(40.705, -73.975);
        log.info("Producing weather item {}", wm);
        wm.setNano(System.nanoTime());
        _atb.produce(wm);
        if (!_simulateWeather) {
          Thread.sleep(1000);
        }
      } catch (final Exception e) {
        log.error("Failed to produce weather", e);
      }
    }
  }

  public void runConsumer() {
    final AtomicLong totalNanos = new AtomicLong();
    final AtomicLong maxLag = new AtomicLong(0);
    final AtomicInteger numEvents = new AtomicInteger();
    while (true) {
      try {
        _atb.consume(wm -> {
              final long lag = System.nanoTime() - wm.getNano();
              if (lag > maxLag.get()) {
                maxLag.set(lag);;
              }
              final double avgLag = totalNanos.addAndGet(lag)/numEvents.incrementAndGet();
              log.info("Consuming single item: {}, lag {}, avg lag {}, max lag {} nanos for {} events", wm, lag, avgLag, maxLag, numEvents.get());
            },
            (count, i, wm) -> {
                final long lag = System.nanoTime() - wm.getNano();
                if (lag > maxLag.get()) {
                  maxLag.set(lag);
                }
                final double avgLag = totalNanos.addAndGet(lag)/numEvents.incrementAndGet();
                log.info("Consuming item {} of {}:  {} lag {}, avg lag {}, max lag {} nanos for {} events", i, count, wm, lag, avgLag, maxLag, numEvents.get());
            });
        Thread.sleep(20);
      } catch (final TimeoutException | InterruptedException e) {
        log.error("Failed to consume  weather", e);
      }
    }
  }
}