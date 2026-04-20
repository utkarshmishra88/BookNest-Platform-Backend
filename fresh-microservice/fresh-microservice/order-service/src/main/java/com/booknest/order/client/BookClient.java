package com.booknest.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "book-service")
public interface BookClient {
    
    @GetMapping("/books/{id}")
    Map<String, Object> getBookById(@PathVariable("id") int id);
}