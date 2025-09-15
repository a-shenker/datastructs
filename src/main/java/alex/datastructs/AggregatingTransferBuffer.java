package  alex.datastructs;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface AggregatingTransferBuffer<T> {
  void produce(T datum)  throws InterruptedException, TimeoutException;
  void consume(Consumer<T> singleConsumer, AggregatingConsumer<T> aggConsumer) throws InterruptedException, TimeoutException;
}