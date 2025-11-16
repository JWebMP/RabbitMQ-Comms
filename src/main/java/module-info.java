import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.rabbit.implementations.RabbitMQWebModuleInclusion;
import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions;
import com.jwebmp.rabbit.implementations.RabbitPublishToGroup;
import com.jwebmp.rabbit.*;

module com.jwebmp.rabbit {

    exports com.jwebmp.rabbit;

    requires transitive com.jwebmp.vertx;
    requires transitive com.jwebmp.core.angular;
    requires com.jwebmp.core;
    
    requires com.guicedee.rabbit;
    requires com.guicedee.guicedinjection;

    requires transitive io.vertx.rabbitmq;
    requires static lombok;

    provides IGuiceScanModuleInclusions with RabbitMQWebModuleInclusion;
    provides GuicedWebSocketOnAddToGroup with RabbitPublishToGroup;
    provides GuicedWebSocketOnRemoveFromGroup with RabbitPublishToGroup;
    provides GuicedWebSocketOnPublish with RabbitPublishToGroup;

    provides com.jwebmp.core.base.angular.modules.services.angular.WebSocketGroupAdd with RabbitMQDirective;

    opens com.jwebmp.rabbit to com.google.guice, com.fasterxml.jackson.databind;
    opens com.jwebmp.rabbit.implementations to com.google.guice, com.fasterxml.jackson.databind;
}