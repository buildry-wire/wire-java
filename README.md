# wire-java

Official Java SDK for the [Wire](https://wire.mn) payment API. Java 11+, one small dependency (Gson) and the JDK's built-in `java.net.http.HttpClient`.

Docs: [docs.wire.mn](https://docs.wire.mn)

## Install

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("mn.wire:wire-java:1.0.0")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'mn.wire:wire-java:1.0.0'
}
```

### Maven
```xml
<dependency>
  <groupId>mn.wire</groupId>
  <artifactId>wire-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quickstart
```java
import mn.wire.*;
import java.util.List;

Wire wire = new Wire("sk_live_...");

// Create a PaymentIntent. Amounts are in minor units (MNT integer).
PaymentIntent pi = wire.paymentIntents().create(
        PaymentIntentCreateParams.create()
                .amount(50000)
                .currency("MNT")
                .allowedOperators(List.of("sandbox")));
System.out.println(pi.id + " " + pi.status);

// Confirm it.
PaymentIntent confirmed = wire.paymentIntents().confirm(pi.id,
        PaymentIntentConfirmParams.create().returnUrl("https://example.com/return"));
System.out.println(confirmed.status);
```

`allowedOperators` takes the operator ids enabled on your account.

## Configuration
```java
Wire wire = new Wire("sk_live_...", WireOptions.builder()
        .baseUrl("https://api.wire.mn")     // default
        .timeout(java.time.Duration.ofSeconds(30))
        .maxRetries(2)                       // retries on 429/5xx/network
        .build());
```

## Auto-pagination
```java
for (Charge charge : wire.charges().list(ListParams.create().limit(50))) {
    System.out.println(charge.id);
}
```
Pages are fetched lazily, following `has_more` via the cursor.

## Webhook verification
Verify the raw request body before parsing. The header is `WirePayment-Signature: t=...,v1=...`.
```java
import mn.wire.Webhooks;

Webhooks webhooks = new Webhooks();
String signature = request.getHeader(Webhooks.SIGNATURE_HEADER);
try {
    Event event = webhooks.verify(rawBody, signature, endpointSecret);
    System.out.println(event.type);
} catch (SignatureVerificationException e) {
    // reject: 400
}
```

## Errors
```java
import mn.wire.WireException;
import mn.wire.WireConnectionException;

try {
    wire.paymentIntents().create(PaymentIntentCreateParams.create().amount(-1));
} catch (WireConnectionException e) {
    // network/timeout failure after retries
} catch (WireException e) {
    System.out.println(e.getCode() + " " + e.getRequestId() + " " + e.getStatusCode());
}
```
The API key is never logged nor included in error messages.

## License
MIT
