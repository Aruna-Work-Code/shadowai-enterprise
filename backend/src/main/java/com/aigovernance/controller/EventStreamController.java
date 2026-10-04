package com.aigovernance.controller;

import com.aigovernance.service.EventStreamService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/events")
public class EventStreamController {
    private final EventStreamService stream;
    public EventStreamController(EventStreamService stream){this.stream=stream;}
    @GetMapping(value="/stream",produces="text/event-stream")
    public SseEmitter stream(){return stream.connect();}
}
