package com.jwebmp.rabbit.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions;

import java.util.Set;

public class RabbitMQWebModuleInclusion implements IGuiceScanModuleInclusions<RabbitMQWebModuleInclusion>
{
    @Override
    public Set<String> includeModules()
    {
        return Set.of("com.jwebmp.rabbit");
    }
}
