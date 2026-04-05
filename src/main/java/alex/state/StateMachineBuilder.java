package alex.state;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Builder for {@link StateMachine} that constructs a StateMachine with a
 * built-in validator based on permitted transitions.
 */
public final class StateMachineBuilder<T> {
  private final State initial;
  private final Table<State, Event<T>, State> _transitions = HashBasedTable.create();

  public StateMachineBuilder(final State initial) {
    this.initial = Objects.requireNonNull(initial, "initial");
  }

  /**
   * Permit a transition from {@code from} to {@code to} when the triggering
   * event equals the provided {@code event} instance.
   */
  public StateMachineBuilder<T> with(final State from, final Event<T> event, final State to) {
    _transitions.put(Objects.requireNonNull(from), Objects.requireNonNull(event), Objects.requireNonNull(to));
    return this;
  }

  /**
   * Build the StateMachine. The resulting StateMachine will use a validator
   * that enforces the configured permits; transitions not explicitly permitted
   * will be rejected.
   */
  public StateMachine<T> build() {
    BiPredicate<State, Event<T>> validator = _transitions::contains;
    return new StateMachine<>(initial, validator);
  }
}
