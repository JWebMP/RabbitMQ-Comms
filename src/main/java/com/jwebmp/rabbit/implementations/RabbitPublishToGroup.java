package com.jwebmp.rabbit.implementations;

import com.guicedee.guicedservlets.websockets.services.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@Log
@Singleton
public class RabbitPublishToGroup implements GuicedWebSocketOnAddToGroup<RabbitPublishToGroup>,
        GuicedWebSocketOnRemoveFromGroup<RabbitPublishToGroup>,
        GuicedWebSocketOnPublish<RabbitPublishToGroup>
{

    @Inject
    private RabbitMQClient client;

    private Set<String> declaredExchanges = new HashSet<>();

    @Inject
    void setup()
    {
        onAddToGroup("Everyone");
    }

    @Override
    public CompletableFuture<Boolean> onAddToGroup(String groupName)
    {
        if (declaredExchanges.contains(groupName))
        {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        if (client.isConnected())
        {
            var result = client.exchangeDeclare(groupName, "fanout", !groupName.startsWith("__vertx"), groupName.startsWith("__vertx"));
            result.onComplete((handler) -> {
                if (handler.succeeded())
                {
                    declaredExchanges.add(groupName);
                    log.info("Group [" + groupName + "] exchange created");
                    resultFuture.complete(true);
                }
                else
                {
                    log.warning("Could not create group exchange [" + groupName + "] - " + handler.cause()
                                                                                                  .getMessage());
                    resultFuture.complete(false);
                }
            });
        }
        else
        {
            client.addConnectionEstablishedCallback(connection -> {
                var result = client.exchangeDeclare(groupName, "fanout", !groupName.startsWith("__vertx"), groupName.startsWith("__vertx"));
                result.onComplete((handler) -> {
                    if (handler.succeeded())
                    {
                        declaredExchanges.add(groupName);
                        log.info("Group [" + groupName + "] exchange created after connection established");
                        resultFuture.complete(true);
                    }
                    else
                    {
                        log.warning("Could not create group exchange on established [" + groupName + "] - " + handler.cause()
                                                                                                                     .getMessage());
                        resultFuture.complete(false);
                    }
                });
            });
        }

        return resultFuture;
    }

    @Override
    public boolean publish(String groupName, String message)
    {
        try
        {
            onAddToGroup(groupName).whenComplete((success, error) -> {
                                       if (success)
                                       {
                                           client.basicPublish(groupName, "", Buffer.buffer(message));
                                       }
                                       else
                                       {
                                           log.log(Level.SEVERE, "Could not publish message to queue group [" + groupName + "]");
                                       }
                                   })
                                   .get(5, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Could not publish message to queue group [" + groupName + "]", e);
            return false;
        }

        return true;
    }

    @Override
    public CompletableFuture<Boolean> onRemoveFromGroup(String groupName)
    {
        //var a = onAddToGroup(groupName);
        if (groupName.startsWith("__vertx"))
        {
            declaredExchanges.remove(groupName);
            client.exchangeDelete(groupName);
        }
        return CompletableFuture.completedFuture(true);
    }
}
