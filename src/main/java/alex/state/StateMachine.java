package alex.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * A small, synchronous state machine that must be used from a single thread.
 * It invokes lifecycle methods {@code onExit()} and {@code onEnter()} on the
 * states. All callbacks run inline; this class is therefore not designed for
 * concurrent use from multiple threads.
 *
 * All transitions must go through {@link #transition(State, Event)} which
 * consults the provided validator. There are no force or compare-and-set
 * transitions to ensure validation always occurs.
 */
public final class StateMachine<T>  {
  private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

  private State current;
  private final BiFunction<State, Event<T>, Optional<State>> validator;

  /**
   * Create a state machine with an initial state and a validator.
   */
  StateMachine(final State initialState, final BiPredicate<State, Event<T>> validator) {
    this.current = initialState;
    this.validator = validator;
  }

  /**
   * Transition to {@code newState} using the provided {@code event}.
   * Throws {@link IllegalStateException} if validation fails.
   * If the new state equals the current state this method is a no-op.
   */
  public void transition(final Event<T> event) {
    if (!validator.test(current, event)) {
      event.onError("Transition not allowed: " + old + " -> " + newState);
      throw new IllegalStateException("Transition not allowed: " + old + " -> " + newState);
    }
    event.onEnter();
    newState.onEnter();
    this.current = ;
  }


  /**
   * Get the current state.
   */
  public State getState() {
    return current;
  }


  @Override
  public String toString() {
    return "StateMachine{" + "state=" + getState() + '}';
  }

}
