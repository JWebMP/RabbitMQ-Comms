package com.jwebmp.rabbit;

import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.compiler.TypeScriptCompiler;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;

@NgApp(value = "RabbitMQWebTest", bootComponent = RabbitMQPage.class)
class RabbitMQWebTest extends NGApplication<RabbitMQWebTest>
{
    public RabbitMQWebTest()
    {
        getOptions().setTitle("RabbitMQ Web Test");
    }

    @Test
    public void testAppSearch() throws IOException
    {
        GuiceContext.instance()
                .inject();
        for (INgApp<?> app : TypeScriptCompiler.getAllApps())
        {
            TypeScriptCompiler compiler = new TypeScriptCompiler(app);

            System.out.println("Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
                    "in folder " + IComponent.getClassDirectory(app.getClass()));
            System.out.println("================");
            //	compiler.renderAppTS(app);
            System.out.println("================");
        }
    }

}