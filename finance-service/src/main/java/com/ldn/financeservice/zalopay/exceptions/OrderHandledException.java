package com.ldn.financeservice.zalopay.exceptions;

public class OrderHandledException extends RuntimeException {
  private static final String message = "Đơn hàng đã hoặc đang được xử lý!";
  public OrderHandledException() {super(message);}
    public OrderHandledException(String message) {
        super(message);
    }
}
