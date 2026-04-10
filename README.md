# JWebMP RabbitMQ-Comms

[![Maven Central](https://img.shields.io/maven-central/v/com.jwebmp/jwebmp-rabbitmq)](https://central.sonatype.com/artifact/com.jwebmp/jwebmp-rabbitmq)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

![Java 25+](https://img.shields.io/badge/Java-25%2B-green)
![Modular](https://img.shields.io/badge/Modular-JPMS-green)
![Angular](https://img.shields.io/badge/Angular-20-DD0031?logo=angular)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript)

<!-- Tech icons row -->
![Vert.x](https://img.shields.io/badge/Vert.x-5-782A90?logo=eclipse-vert.x)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600?logo=rabbitmq)
![STOMP](https://img.shields.io/badge/STOMP-Protocol-59666C)
![WebSocket](https://img.shields.io/badge/WebSocket-RFC_6455-0A7)

Real-time bidirectional communication bridge connecting Angular browser clients to RabbitMQ message broker via WebSocket/STOMP. Enables reactive server-to-browser push notifications, live updates, and pub/sub messaging patterns for JWebMP applications.

Built on [RabbitMQ](https://www.rabbitmq.com/) · [Vert.x RabbitMQ Client](https://vertx.io/docs/vertx-rabbitmq-client/java/) · [STOMP.js](https://stomp-js.github.io/stomp-websocket/) · [SockJS](https://sockjs.org/) · JPMS module `com.jwebmp.rabbit` · Java 25+

## 📦 Installation

```xml
<dependency>
  <groupId>com.jwebmp</groupId>
  <artifactId>jwebmp-rabbitmq</artifactId>
  <version>2.0.0-RC1</version>
</dependency>
```

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
implementation("com.jwebmp:jwebmp-rabbitmq:2.0.0-RC1")
```
</details>

### RabbitMQ Web STOMP Plugin

Enable the RabbitMQ Web STOMP plugin on your RabbitMQ server:

```bash
rabbitmq-plugins enable rabbitmq_web_stomp
```

Default WebSocket endpoint: `ws://localhost:15674/ws`

## ✨ Features

- **Real-Time Browser Communication** — WebSocket-based bidirectional messaging between Angular clients and RabbitMQ broker
- **STOMP Protocol** — Industry-standard STOMP over WebSocket with automatic reconnection and heartbeat support
- **Group-Based Pub/Sub** — Dynamic subscription management with RabbitMQ fanout exchanges for broadcast messaging
- **Angular Directive Integration** — Declarative HTML attribute `[data-rabbit-groups]` for automatic group subscription
- **Automatic Exchange Management** — Server-side exchange declaration and lifecycle management via Vert.x RabbitMQ client
- **Session-Aware Messaging** — Automatic subscription to session-specific groups using `ContextIdService`
- **Connection Resilience** — Automatic reconnection with configurable delays and connection state observables
- **Server-Side WebSocket Hooks** — Integrates with GuicedEE WebSocket lifecycle events (`onAddToGroup`, `onRemoveFromGroup`, `onPublish`)
- **TypeScript Client Generation** — Fully typed Angular provider and directive generated from Java annotations
- **SockJS Fallback** — Graceful degradation for browsers/proxies without native WebSocket support
- **JPMS Modular** — Fully modular with explicit dependencies via Java Platform Module System

## 🚀 Quick Start

### Server-Side Configuration

#### 1. Add Dependency

Include `jwebmp-rabbitmq` in your JWebMP application:

```xml
<dependency>
  <groupId>com.jwebmp</groupId>
  <artifactId>jwebmp-rabbitmq</artifactId>
</dependency>
```

#### 2. Configure RabbitMQ Connection

The module uses GuicedEE's RabbitMQ module. Configure connection in your `module-info.java` or via environment variables:

```java
module com.myapp {
    requires com.jwebmp.rabbit;
    requires com.guicedee.rabbit;

    opens com.myapp to com.google.guice;
}
```

Environment variables for RabbitMQ connection:

| Variable | Purpose | Default |
|---|---|---|
| `RABBITMQ_HOST` | RabbitMQ hostname | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_WEB_STOMP_PORT` | WebSocket STOMP port | `15674` |
| `RABBITMQ_USERNAME` | Username | `guest` |
| `RABBITMQ_PASSWORD` | Password | `guest` |
| `RABBITMQ_VIRTUAL_HOST` | Virtual host | `/` |

#### 3. Publish Messages from Server

```java
@Inject
private RabbitPublishToGroup publisher;

public void sendUpdate() {
    String message = "{\"type\":\"update\",\"data\":\"Hello from server!\"}";
    publisher.publish("Everyone", message);
}

public void sendToUser(String userId, String message) {
    publisher.publish("user-" + userId, message);
}
```

### Client-Side Integration (Angular)

#### 1. Component with RabbitMQ Directive

Use the `[data-rabbit-groups]` directive to automatically subscribe to RabbitMQ exchanges:

```java
@NgComponent
@NgDataVariable(variableName = "messages", value = "[]")
public class DashboardComponent implements INgComponent<DashboardComponent> {

    @Override
    public String render() {
        return """
            <div [data-rabbit-groups]="'user-dashboard'">
                <h1>Live Dashboard</h1>
                <div *ngFor="let message of messages">
                    {{ message }}
                </div>
            </div>
            """;
    }
}
```

When the component mounts, it automatically:
1. Subscribes to the `user-dashboard` exchange
2. Receives all messages published to that exchange
3. Unsubscribes when the component is destroyed

#### 2. Programmatic Subscription

Inject `RabbitMQProvider` for manual control:

```typescript
// TypeScript (generated from Java annotations)
import { RabbitMQProvider } from './rabbit-mq-provider';

@Component({
  selector: 'app-notifications',
  template: `<div>{{ notification }}</div>`
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notification: string = '';

  constructor(private rabbitMqProvider: RabbitMQProvider) {}

  ngOnInit() {
    // Subscribe to a group
    this.rabbitMqProvider.addGroup('notifications');

    // Send a message
    this.rabbitMqProvider.sendMessage('/exchange/admin',
      JSON.stringify({ action: 'ping' }));
  }

  ngOnDestroy() {
    // Unsubscribe from group
    this.rabbitMqProvider.removeGroup('notifications');
  }
}
```

#### 3. Connection State Monitoring

```typescript
import { RabbitMQProvider } from './rabbit-mq-provider';

export class AppComponent implements OnInit {
  isConnected: boolean = false;

  constructor(private rabbitMqProvider: RabbitMQProvider) {}

  ngOnInit() {
    // Wait for connection before performing actions
    this.rabbitMqProvider.waitForConnection().then(() => {
      this.isConnected = true;
      console.log('RabbitMQ connected!');
    });

    // Or subscribe to connection state
    RabbitMQProvider.connected.subscribe(connected => {
      this.isConnected = connected;
    });
  }
}
```

## 📐 Architecture

### Message Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          RabbitMQ Broker                                 │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  Exchange: "Everyone" (fanout)                                  │    │
│  │  Exchange: "user-dashboard" (fanout)                            │    │
│  │  Exchange: "notifications" (fanout)                             │    │
│  │  Exchange: "__vertx.session-abc123" (fanout, auto-delete)      │    │
│  └────────────────────────────────────────────────────────────────┘    │
│         ▲                                            │                   │
│         │ AMQP 0.9.1                                 │ STOMP/WebSocket  │
│         │ (port 5672)                                │ (port 15674)     │
└─────────┼────────────────────────────────────────────┼──────────────────┘
          │                                            │
          │                                            ▼
┌─────────┴──────────────┐              ┌──────────────────────────────┐
│   Server-Side (Java)   │              │   Browser (Angular/TypeScript) │
│                        │              │                               │
│  RabbitPublishToGroup  │              │     RabbitMQProvider          │
│         │              │              │            │                  │
│         ├─ onAddToGroup │              │            ├─ addGroup()      │
│         ├─ publish()    │              │            ├─ removeGroup()   │
│         └─ onRemoveFrom │              │            ├─ sendMessage()   │
│            Group        │              │            └─ subscribe()     │
│                        │              │                               │
│  RabbitMQDirective     │              │     [data-rabbit-groups]      │
│    (Angular annotation)│              │       (HTML directive)        │
└────────────────────────┘              └───────────────────────────────┘
```

### Component Overview

| Component | Location | Purpose |
|---|---|---|
| **RabbitMQProvider** | Angular Service | STOMP.js client wrapper, connection management, subscription handling |
| **RabbitMQDirective** | Angular Directive | HTML attribute `[data-rabbit-groups]` for declarative subscriptions |
| **RabbitPublishToGroup** | Server (Java) | Server-side message publisher via Vert.x RabbitMQ client |
| **GuicedEE WebSocket Hooks** | Server (Java) | Integration with GuicedEE WebSocket lifecycle events |

### Exchange Lifecycle

1. **Client Subscription** — Angular component subscribes to a group (e.g., `user-dashboard`)
2. **Exchange Declaration** — Server-side `RabbitPublishToGroup.onAddToGroup()` declares a fanout exchange if not exists
3. **Message Publishing** — Server publishes to exchange: `client.basicPublish("user-dashboard", "", buffer)`
4. **Message Distribution** — RabbitMQ fanout exchange broadcasts to all subscribed browser clients
5. **Client Processing** — Angular `RabbitMQProvider.subscribe()` receives message and processes via `EventBusService`
6. **Cleanup** — Component destruction triggers `removeGroup()`, unsubscribing from exchange

### Session-Aware Groups

The module automatically subscribes clients to session-specific groups:

- **Everyone** — Global broadcast group (all connected clients)
- **Session Group** — Unique group per browser session (e.g., `__vertx.session-abc123`)
- **Custom Groups** — Application-defined groups (e.g., `user-{userId}`, `room-{roomId}`)

Session groups are automatically created and deleted based on `ContextIdService` observable.

## 🔧 Configuration

### Server-Side (Java)

#### Exchange Declaration Behavior

Exchanges are declared with the following properties:

```java
client.exchangeDeclare(
    groupName,                              // Exchange name
    "fanout",                               // Exchange type
    !groupName.startsWith("__vertx"),      // Durable (persistent)
    groupName.startsWith("__vertx")        // Auto-delete (temporary)
);
```

- **Persistent exchanges** — Groups without `__vertx` prefix are durable and survive broker restarts
- **Temporary exchanges** — Groups with `__vertx` prefix are auto-deleted when last consumer disconnects

#### Publishing Messages

```java
@Inject
private RabbitPublishToGroup publisher;

// Publish to a group
publisher.publish("groupName", jsonMessage);

// Publish with automatic exchange creation
publisher.onAddToGroup("newGroup").thenAccept(success -> {
    if (success) {
        publisher.publish("newGroup", "Hello!");
    }
});
```

### Client-Side (Angular)

#### Connection Configuration

The `RabbitMQProvider` exposes configurable fields:

```typescript
@NgField("url : string = 'ws://127.0.0.1:15674/ws';")
@NgField("user : string = 'guest';")
@NgField("pass : string = 'guest';")
@NgField("virtualHost : string = 'UWEAssist';")
```

Override in your application:

```typescript
import { RabbitMQProvider } from './rabbit-mq-provider';

export class AppModule {
  constructor(private rabbitMqProvider: RabbitMQProvider) {
    this.rabbitMqProvider.url = 'ws://production.example.com:15674/ws';
    this.rabbitMqProvider.user = 'app-user';
    this.rabbitMqProvider.pass = 'secure-password';
    this.rabbitMqProvider.virtualHost = 'production';
  }
}
```

#### Reconnection Settings

STOMP client reconnection is configured in the constructor:

```typescript
RabbitMQProvider.client = new Client({
  brokerURL: this.url,
  reconnectDelay: 5000,        // 5 seconds between reconnection attempts
  // heartbeatIncoming: 4000,  // Optional: incoming heartbeat interval
  // heartbeatOutgoing: 4000   // Optional: outgoing heartbeat interval
});
```

## 🔌 API Reference

### Java API

#### RabbitPublishToGroup

```java
@Singleton
public class RabbitPublishToGroup {

    // Declare exchange for group (idempotent)
    CompletableFuture<Boolean> onAddToGroup(String groupName);

    // Publish message to group
    boolean publish(String groupName, String message);

    // Remove group exchange (only for __vertx.* groups)
    CompletableFuture<Boolean> onRemoveFromGroup(String groupName);
}
```

#### RabbitMQDirective

```java
@NgDirective(value = "[data-rabbit-groups]", standalone = true)
public class RabbitMQDirective {

    // Add component to RabbitMQ group
    boolean addGroup(IComponentHierarchyBase<?, ?> component, String groupName);
}
```

Usage in Java component:

```java
@NgComponent
public class MyComponent implements INgComponent<MyComponent> {
    @Override
    public String render() {
        return "<div [data-rabbit-groups]=\"'my-group'\">Content</div>";
    }
}
```

### TypeScript API

#### RabbitMQProvider Service

```typescript
@Injectable({ providedIn: 'root' })
export class RabbitMQProvider {

    // Connection state observable
    static connected: BehaviorSubject<boolean>;

    // Subscribe to RabbitMQ exchange
    subscribe(destination: string): void;

    // Add subscription to group
    addGroup(group: string): void;

    // Remove subscription from group
    removeGroup(group: string): void;

    // Connect to broker
    connect(): void;

    // Disconnect from broker
    disconnect(): void;

    // Publish message to destination
    sendMessage(destination: string, body: string): void;

    // Wait for connection to be established
    waitForConnection(): Promise<boolean>;
}
```

#### Directive Usage

```html
<!-- Automatic subscription on mount, unsubscribe on destroy -->
<div [data-rabbit-groups]="'notifications'">
  Live notifications appear here
</div>

<!-- Dynamic group binding -->
<div [data-rabbit-groups]="currentUserGroup">
  User-specific content
</div>

<!-- Multiple components can subscribe to the same group -->
<app-dashboard [data-rabbit-groups]="'dashboard-updates'"></app-dashboard>
<app-chart [data-rabbit-groups]="'dashboard-updates'"></app-chart>
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn clean test

# Run specific test
mvn test -Dtest=RabbitMQWebTest
```

### Test Application

The module includes a test application demonstrating integration:

```java
@NgApp(value = "RabbitMQWebTest", bootComponent = RabbitMQPage.class)
class RabbitMQWebTest extends NGApplication<RabbitMQWebTest> {
    public RabbitMQWebTest() {
        getOptions().setTitle("RabbitMQ Web Test");
    }
}
```

### Integration Testing

For integration tests, use Testcontainers with RabbitMQ:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RabbitMQIntegrationTest {

    private static final RabbitMQContainer rabbitMQ =
        new RabbitMQContainer("rabbitmq:3-management")
            .withPluginsEnabled("rabbitmq_web_stomp");

    @BeforeAll
    static void setup() {
        rabbitMQ.start();

        // Configure connection
        System.setProperty("rabbitmq.host", rabbitMQ.getHost());
        System.setProperty("rabbitmq.port",
            String.valueOf(rabbitMQ.getAmqpPort()));
        System.setProperty("rabbitmq.webstomp.port",
            String.valueOf(rabbitMQ.getHttpPort()));
    }

    @Test
    void testMessagePublishing() {
        // Test implementation
    }
}
```

## 🗺️ Module Graph

```
com.jwebmp.rabbit
 ├── com.jwebmp.vertx              (Vert.x integration, reactive runtime)
 ├── com.jwebmp.core.angular       (Angular annotation framework)
 ├── com.jwebmp.core               (JWebMP core)
 ├── com.guicedee.rabbit           (GuicedEE RabbitMQ integration)
 ├── com.guicedee.guicedinjection  (Guice DI)
 ├── io.vertx.rabbitmq             (Vert.x RabbitMQ client)
 └── TypeScript Dependencies:
     ├── @stomp/stompjs            (STOMP over WebSocket client)
     ├── sockjs-client             (SockJS fallback transport)
     └── @types/sockjs-client      (TypeScript type definitions)
```

## 🧰 Troubleshooting & Best Practices

### Connection Issues

**Problem**: Client cannot connect to RabbitMQ WebSocket endpoint

**Solutions**:
- Verify `rabbitmq_web_stomp` plugin is enabled: `rabbitmq-plugins enable rabbitmq_web_stomp`
- Check firewall allows port 15674 (default Web STOMP port)
- Verify broker URL matches your RabbitMQ server: `ws://your-host:15674/ws`
- Check RabbitMQ logs for authentication errors

### Exchange Not Created

**Problem**: Messages not received by clients

**Solutions**:
- Check server logs for exchange declaration errors
- Verify RabbitMQ AMQP connection is established on server side
- Use RabbitMQ Management UI to inspect exchanges: `http://localhost:15672`
- Ensure client subscribes after connection is established (use `waitForConnection()`)

### Message Loss

**Problem**: Some messages are lost during client reconnection

**Solutions**:
- Use durable exchanges for critical messages (avoid `__vertx` prefix)
- Implement message queues instead of fanout exchanges for guaranteed delivery
- Consider persistent queues and durable subscriptions for mission-critical data
- Use RabbitMQ acknowledgments for reliable delivery

### Memory Leaks

**Problem**: Subscriptions not cleaned up

**Solutions**:
- Always call `removeGroup()` in `ngOnDestroy()` lifecycle hook
- Use `[data-rabbit-groups]` directive for automatic cleanup
- Unsubscribe from observables in component teardown
- Monitor `declaredExchanges` set on server side

### Best Practices

- **Use semantic group names** — `user-{id}`, `room-{id}`, `notifications`, etc.
- **Prefer directives** — Use `[data-rabbit-groups]` for automatic lifecycle management
- **Wait for connection** — Always await `waitForConnection()` before publishing
- **Handle errors** — Subscribe to `onStompError` for error handling
- **Clean up resources** — Properly unsubscribe and disconnect on component/app destruction
- **Use environment variables** — Externalize RabbitMQ connection configuration
- **Monitor connections** — Use RabbitMQ Management UI to monitor WebSocket connections
- **Secure credentials** — Never hardcode credentials, use environment variables or secret management

## 🧭 Documentation

### Related Documentation

- **JWebMP Core**: [`JWebMP/README.md`](../README.md)
- **GuicedEE RabbitMQ**: [`GuicedEE/rabbitmq/README.md`](../../GuicedEE/rabbitmq/README.md)
- **Vert.x Integration**: [`JWebMP/vertx/README.md`](../vertx/README.md)
- **Angular Integration**: [`JWebMP/plugins/angular/README.md`](../plugins/angular/README.md)

### External Resources

- [RabbitMQ Web STOMP Plugin](https://www.rabbitmq.com/web-stomp.html)
- [STOMP.js Documentation](https://stomp-js.github.io/stomp-websocket/)
- [Vert.x RabbitMQ Client](https://vertx.io/docs/vertx-rabbitmq-client/java/)
- [SockJS Protocol](https://sockjs.org/)

## 🤝 Contributing

Issues and pull requests are welcome.

### Guidelines

- Include tests for new features
- Update documentation for behavior changes
- Follow existing code patterns and naming conventions
- Test with RabbitMQ 3.x and latest Web STOMP plugin

## 📄 License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

**JWebMP RabbitMQ-Comms** — Real-time browser-to-RabbitMQ connectivity for reactive web applications.

Built with ❤️ using Java 25+, Vert.x 5, Angular 21, RabbitMQ, and STOMP.
