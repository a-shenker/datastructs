package  alex.datastructs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

public class AggregatingTransferBufferFAST<T> implements AggregatingTransferBuffer<T> {
  private static final Logger _logger = LoggerFactory.getLogger(AggregatingTransferBufferFAST.class);
  private long _maxProducerWait;
  private long _maxConsumerWait;
  private int _maxProducerBacklog;
  private final Clock _clock;
  private final ArrayList<T> _storage;
  private final Semaphore _semaphore = new Semaphore(1, true);

  public AggregatingTransferBufferFAST(final long maxProducerWait, 
                                   final long maxConsumerWait,
                                   final int maxProducerBacklog,
                                   final Clock clock) {
    _maxProducerBacklog = maxProducerBacklog <= 0 ? Integer.MAX_VALUE : maxProducerBacklog;
    _maxProducerWait = Math.max(maxProducerWait, 0);
    _maxConsumerWait = Math.max(maxConsumerWait,  0);
    _clock = clock;
    _storage = maxProducerBacklog > 0 ? new ArrayList<>(maxProducerBacklog) : new ArrayList<>();
  }

  @Override
  public void produce(final T datum) throws InterruptedException, TimeoutException {
    final long timeT = _clock.millis();
    @Nullable T transfer = datum;
    while (transfer != null) {
      acquire(_semaphore, _maxProducerWait, timeT);;
      try {
        checkTimeout(_maxProducerWait, timeT);
        if (_storage.size() < _maxProducerBacklog) {
          _storage.add(transfer);
          transfer = null;
        } else {
          _logger.trace("Producer blocked by backpressure");
        }
      } finally {
        _semaphore.release();
      }
      Thread.onSpinWait();
    }
  }

  @Override
  public void consume(final Consumer<T> singleConsumer,
                      final AggregatingConsumer<T> aggConsumer) throws InterruptedException, TimeoutException {
    final long timeT = _clock.millis();
    boolean trying = true;
    while (trying) {
      acquire(_semaphore, _maxConsumerWait, timeT);
      try {
        checkTimeout(_maxConsumerWait, timeT);
        if (!_storage.isEmpty()) {
          final int numItems = _storage.size();
          if (numItems == 1) {
            singleConsumer.accept(_storage.get(0));
          } else {
            IntStream.range(0, numItems).forEach(i -> aggConsumer.accept(numItems,  i, _storage.get(i)));
          }
          _storage.clear();
          trying = false;
        } else {
          _logger.trace("Consumer blocked on empty event list");
        }
      } finally {
        _semaphore.release();
      }
      Thread.onSpinWait();
    }
  }

  private void checkTimeout(long maxWait, long startT) throws TimeoutException {
    if (maxWait > 0 && (_clock.millis() - startT) >= maxWait) {
      throw new TimeoutException();
    }
  }
  
  private void  acquire(final Semaphore semaphore, long maxWait, long startT)
    throws InterruptedException, TimeoutException {
    if (maxWait == 0) {
      semaphore.acquire();
    } else {
      final long waitNow = Math.max(maxWait - (_clock.millis() - startT), 0);
      if (!semaphore.tryAcquire(waitNow, TimeUnit.MILLISECONDS)) {
        throw new TimeoutException();
      }
    }
  }
}
