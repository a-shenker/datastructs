package  alex.datastructs;

@FunctionalInterface
public interface AggregatingConsumer<T> {
  void accept(int batchSize, int i, T datum);
}