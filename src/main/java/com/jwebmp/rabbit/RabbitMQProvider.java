package com.jwebmp.rabbit;

import com.jwebmp.core.base.angular.client.annotations.angular.NgProvider;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependency;
import com.jwebmp.core.base.angular.client.services.EventBusService;
import com.jwebmp.core.base.angular.client.services.interfaces.INgProvider;
import com.jwebmp.core.base.angular.client.services.ContextIdService;

import java.util.List;

//@TsDependency(value = "@cloudamqp/amqp-client", version = "*")
@TsDependency(value = "@stomp/stompjs", version = "*")
@TsDependency(value = "sockjs-client", version = "*")
@TsDevDependency(value = "@types/sockjs-client", version = "*")

@NgConstructorParameter(value = "private rabbitMqProvider : RabbitMQProvider", onParent = true, onSelf = false)

@NgComponentReference(EventBusService.class)

@NgComponentReference(ContextIdService.class)
@NgConstructorParameter("private contextIdService : ContextIdService")

@NgImportReference(value = "Injectable", reference = "@angular/core")
@NgImportReference(value = "Location", reference = "@angular/common")
@NgImportReference(value = "ElementRef", reference = "@angular/core")
@NgImportReference(value = "RouterModule", reference = "@angular/router")
@NgImportReference(value = "ParamMap", reference = "@angular/router")
@NgImportReference(value = "Router", reference = "@angular/router")
@NgImportReference(value = "ActivatedRoute", reference = "@angular/router")
@NgImportReference(value = "Subscription", reference = "rxjs")
@NgImportReference(value = "BehaviorSubject", reference = "rxjs")
//@NgImportReference(value = "!* as AMQPWebSocketClient", reference = "@cloudamqp/amqp-client/dist/amqp-websocket-client.mjs")
@NgImportReference(value = "Client", reference = "@stomp/stompjs")
@NgImportReference(value = "Message", reference = "@stomp/stompjs")
@NgImportReference(value = "!* as SockJS", reference = "sockjs-client")

@NgField("static client : Client;")

@NgField("url : string = 'ws://127.0.0.1:15674/ws';")
@NgField("user : string = 'guest';")
@NgField("pass : string = 'guest';")
@NgField("virtualHost : string = 'UWEAssist';")

@NgField("exchanges: any = {};")

//@NgField("groups : string[] = [];")
//@NgField("subscriptions : any[] = [];")
@NgField("static contextId: string | null = null;")
@NgField("static contextIdSubscription?: Subscription;")
@NgField("static connected : BehaviorSubject<boolean>  = new BehaviorSubject<boolean>(false);")


@NgConstructorBody("""
                if(!RabbitMQProvider.client){
                RabbitMQProvider.client = new Client({
                       brokerURL: this.url,
                       connectHeaders: {
                           login: this.user,
                           passcode: this.pass,
                           host: this.virtualHost
                       },
                       /*            debug: (str) => {
                                       console.log(str);
                                   },*/
                       reconnectDelay: 5000,
                       /*            heartbeatIncoming: 4000,
                                   heartbeatOutgoing: 4000*/
                   });
        
                   RabbitMQProvider.client.onConnect = (frame: any) => {
                                   console.log('Connected: ' + frame);
                                   this.addGroup("Everyone");
                                   RabbitMQProvider.contextIdSubscription = this.contextIdService.getContextIdObservable().subscribe(contextId => {
                                       if (contextId) {
                                           this.addGroup(window.sessionStorage['contextId']);
                                           RabbitMQProvider.contextId = contextId;
                                       }
                                   })
                                   RabbitMQProvider.connected.next(true);
                               };
        
        
                   RabbitMQProvider.client.onStompError = (frame: any) => {
                       console.error('Broker reported error: ' + frame.headers['message']);
                       console.error('Additional details: ' + frame.body);
                   };
                   this.connect();
              }
        
        """)

@NgMethod("""
        \tpublic subscribe(destination: string) {
                  // this.waitForConnection().then(() => {
                       const desti = destination;
                       if (destination && !destination.startsWith("/exchange")) {
                           destination = "/exchange/" + destination;
                       }
                       if (this.exchanges[desti]) {
                           console.log('already subscribed to - ' + desti)
                           return;
                       }
                       const s = RabbitMQProvider.client.subscribe(destination, (message: Message) => {
                           if (message.body && message.body.length > 0) {
                               console.log('Received: ' + message.body);
                               this.socketClientService.processResult(JSON.parse(message.body));
                           }
                       });
                       if (s && s.id) {
                           this.exchanges[desti] = s;
                        //   this.groups.push(desti);
                        //   this.subscriptions.push(s);
                       }
               //    })
               }""")

@NgMethod("""
        public addGroup(group:string)
            {
                this.subscribe(group);
            }""")

@NgMethod("""
        public removeGroup(group: string) {
            if(this.exchanges[group])
            {
                this.exchanges[group].unsubscribe();
                delete this.exchanges[group];
            }
        }""")

@NgMethod("""
            public connect() {
                       RabbitMQProvider.client.activate();
                   }
        """)
@NgMethod("""
        public disconnect() {
            if (RabbitMQProvider.client) {
                RabbitMQProvider.client.deactivate();
            }
        \t}""")
@NgMethod("""
            public sendMessage(destination: string, body: string) {
                RabbitMQProvider.client.publish({ destination, body });
        \t}""")

@NgMethod("""
        public waitForConnection() {
                     return RabbitMQProvider.connected.asObservable().toPromise()
                 }""")

@NgProvider
public class RabbitMQProvider implements INgProvider<RabbitMQProvider>
{
    @Override
    public List<String> decorators()
    {
        List<String> out = INgProvider.super.decorators();
        out.add("@Injectable({\n" +
                "  providedIn: 'root'\n" +
                "})");
        return out;
    }

    @Override
    public List<String> onDestroy()
    {
        var s = INgProvider.super.onDestroy();
        s.add("this.contextIdSubscription?.unsubscribe()");
        s.add("if(RabbitMQProvider.client && RabbitMQProvider.client.connected)\n" +
                "               {\n" +
                "                   RabbitMQProvider.client.deactivate();\n" +
                "               }\n" +
                "               console.log('RabbitMQProvider destroyed')");
        return s;
    }
}
