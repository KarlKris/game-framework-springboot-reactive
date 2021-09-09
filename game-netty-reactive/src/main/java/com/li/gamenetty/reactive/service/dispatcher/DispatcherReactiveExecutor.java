package com.li.gamenetty.reactive.service.dispatcher;


import com.li.gamenetty.reactive.protocol.Response;
import com.li.gamenetty.reactive.service.session.Session;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

/**
 * @author li-yuanwen
 * @date 2021/9/2 22:38
 * cpu密集型任务 分发线程池入口
 **/
public interface DispatcherReactiveExecutor {


    /**
     * 执行方法(根据Session hash一个线程执行)
     * @param callable
     */
    Mono<Response<Object>> executeRequest(Session session, Callable<Mono<Response<Object>>> callable);

}
