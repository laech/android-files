package com.example.files.event;

public interface EventHandler<T> {

  void handle(T event);

}
