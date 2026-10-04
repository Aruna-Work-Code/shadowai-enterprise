package com.aigovernance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Lightweight SSE hub for the portfolio application's live activity feed. */
@Service
public class EventStreamService {
    private final List<SseEmitter> clients=new CopyOnWriteArrayList<>();
    public SseEmitter connect(){
        SseEmitter emitter=new SseEmitter(0L);
        clients.add(emitter);
        emitter.onCompletion(()->clients.remove(emitter));
        emitter.onTimeout(()->clients.remove(emitter));
        return emitter;
    }
    public void publish(String type,Object payload){
        for(SseEmitter e:clients){
            try{e.send(SseEmitter.event().name(type).data(payload));}
            catch(IOException ex){e.complete();clients.remove(e);}
        }
    }
}
