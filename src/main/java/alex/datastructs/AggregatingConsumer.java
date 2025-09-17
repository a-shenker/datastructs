package  alex.datastructs;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single input argument as part of a group of
 * consecutive invocations, each invocation as an instance of a total of {@code batchSize}
 * invocations, and the ordinal number {@code i} of the current parameter in that batch,
 * and returns no result. Unlike most other functional interfaces, {@code AggregatingConsumer}
 * is expected to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(int, int, T)}.
 *
 * @param <T> the type of the input to the operation
 *
 * @author Alexander Shenker
 */
@FunctionalInterface
public interface AggregatingConsumer<T> {
  /**
   * Performs this operation on the given argument.
   *
   * @param batchSize the total number of arguments in this batch.
   * @param i the position of the current argument in the current batch of arguments.
   * @param datum the input argument
   */
  void accept(int batchSize, int i, T datum);

  /**
   * Returns a composed {@code AggregatingConsumer} that performs, in sequence, this
   * operation followed by the {@code after} operation. If performing either
   * operation throws an exception, it is relayed to the caller of the
   * composed operation.  If performing this operation throws an exception,
   * the {@code after} operation will not be performed.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code AggregatingConsumer} that performs in sequence this
   * operation followed by the {@code after} operation
   * @throws NullPointerException if {@code after} is null
   */
  default AggregatingConsumer<T> andThen(Consumer<? super T> after) {
    Objects.requireNonNull(after);
    return (batchSize, i, datum) -> { accept(batchSize, i, datum); after.accept(datum); };
  }
}