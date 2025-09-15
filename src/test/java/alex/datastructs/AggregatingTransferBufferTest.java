package alex.datastructs;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AggregatingTransferBufferTest {
  
  @ParameterizedTest
  @MethodSource("simpleBuffer") 
  public void testSimpleBuffer(final AggregatingTransferBuffer<Object> subject)
  throws TimeoutException, InterruptedException {
    final AtomicReference<Object> singleConsumer  = new AtomicReference<>();
    final List<Object> multiConsumer = new ArrayList<>();
    
    final Object obj1 = new Object();
    final Object obj2 = new Object();
    final Object obj3 = new Object();
    subject.produce(obj1);
    subject.consume(singleConsumer::set, (n, i, o) -> multiConsumer.add(o));
    Assertions.assertTrue(multiConsumer.isEmpty());
    Assertions.assertEquals(obj1, singleConsumer.get());
    singleConsumer.set(null);
    subject.produce(obj2);
    subject.produce(obj3);
    subject.consume(singleConsumer::set, (n, i, o) -> multiConsumer.add(o));
    Assertions.assertEquals(List.of(obj2, obj3), multiConsumer); 
  }
  
  
  private static Stream<AggregatingTransferBuffer<Object>> simpleBuffer() {
    return Stream.of(
        new AggregatingTransferBufferSLOW<Object>(0L, 0L, 0, Clock.systemDefaultZone()),
        new AggregatingTransferBufferFAST<Object>(0L, 0L, 0, Clock.systemDefaultZone()));
  }
  
  
  private static Stream<AggregatingTransferBuffer<Object>> timeoutBuffer() {
    return Stream.of(
        new AggregatingTransferBufferSLOW<Object>(1L, 1L, 1, Clock.systemDefaultZone()),
        new AggregatingTransferBufferFAST<Object>(1L, 1L, 1, Clock.systemDefaultZone()));
  }
}