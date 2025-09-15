package  alex.datastructs;

import java.time.Clock;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class AggregatingTransferBufferSLOW<T> implements AggregatingTransferBuffer<T> {
  private long _maxProducerWait;
  private long _maxConsumerWait;
  private int _maxProducerBacklog;
  private final Clock _clock;
  private final ArrayList<T> _storage;

  public AggregatingTransferBufferSLOW(final long maxProducerWait,
                                       final long maxConsumerWait,
                                       final int maxProducerBacklog,
                                       final Clock clock) {
    _maxProducerBacklog = maxProducerBacklog <= 0 ? Integer.MAX_VALUE : maxProducerBacklog;
    _maxProducerWait = maxProducerWait <= 0 ? Long.MAX_VALUE : maxProducerWait;
    _maxConsumerWait = maxConsumerWait <= 0 ? Long.MAX_VALUE : maxConsumerWait;
    _clock = clock;
    _storage = maxProducerBacklog > 0 ? new ArrayList<>(maxProducerBacklog) : new ArrayList<>();
  }

  @Override
  public void produce(final T datum) throws InterruptedException, TimeoutException {
    final long timeT = _clock.millis();
    synchronized (_storage) {
      while (_storage.size() >= _maxProducerBacklog) {
        if ((_clock.millis() - timeT) >= _maxProducerWait) {
          throw new TimeoutException();
        }
        _storage.wait(_maxProducerWait); 
      }
      _storage.add(datum);
      _storage.notify();
    }
  }

  @Override
  public void consume(final Consumer<T> singleConsumer,
    final AggregatingConsumer<T> aggConsumer) throws InterruptedException, TimeoutException {
    final long timeT = _clock.millis();
    synchronized (_storage) {
      while (_storage.isEmpty()) {
        if ((_clock.millis() - timeT) >= _maxConsumerWait) {
          throw new TimeoutException();
        }
        _storage.wait(_maxConsumerWait);
      }
      final int numItems = _storage.size();
      if (numItems == 1) {
        singleConsumer.accept(_storage.get(0));
      } else {
        IntStream.range(0, _storage.size()).forEach(i -> aggConsumer.accept(_storage.size(),  i, _storage.get(i)));
      }
      _storage.clear();
    }
  }
}
