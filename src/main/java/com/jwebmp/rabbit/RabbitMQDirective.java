package com.jwebmp.rabbit;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnDestroy;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnInit;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;
import com.jwebmp.core.base.angular.modules.services.angular.WebSocketGroupAdd;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.rabbit.implementations.RabbitPublishToGroup;

@NgDirective(value = "[data-rabbit-groups]", standalone = true)
@NgField("@Input('data-rabbit-groups') group! : string;")
@NgComponentReference(RabbitMQProvider.class)
@NgOnInit(
        "this.rabbitMqProvider.addGroup(this.group);"
)
@NgOnDestroy(
        "this.rabbitMqProvider.removeGroup(this.group);")
public class RabbitMQDirective implements INgDirective<RabbitMQDirective>, WebSocketGroupAdd<RabbitMQDirective>
{

    @Override
    public boolean addGroup(IComponentHierarchyBase<?, ?> component, String groupName)
    {
        component.asAttributeBase().addAttribute("data-rabbit-groups", groupName);
        component.addConfiguration(AnnotationUtils.getNgComponentReference(RabbitMQDirective.class));
        IGuiceContext.get(RabbitPublishToGroup.class).onAddToGroup(groupName);
        return true;
    }
}
