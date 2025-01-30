package com.linkedin.davinci.consumer;

public class ChangeEvent<T> {
  private final T previousValue;
  private final T currentValue;

  public ChangeEvent(T previousValue, T currentValue) {
    this.previousValue = previousValue;
    this.currentValue = currentValue;
  }

  public T getPreviousValue() {
    return previousValue;
  }

  public T getCurrentValue() {
    return currentValue;
  }

  public class Counter2 {
    private int count = 0;

    public void increment() {
      count++;
    }

    public int getCount() {
      return count;
    }
  }
}
