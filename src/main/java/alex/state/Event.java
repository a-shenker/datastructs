package alex.state;

public interface Event<T> {
  T getPayload();
  void onEnter();
  void onExit();
}