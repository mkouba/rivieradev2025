---
title: A Qute story - rethinking templating in Quarkus
---

## A Qute story

### Rethinking templating in Quarkus

#### Martin Kouba

Riviera DEV 2025

---

### Who is Martin?

- [~] Introvert and fan of open source
- [~] Software engineer at Ret Hat/IBM
- [~] Quarkus Qute contributor/maintainer

---

### What's the plan for today?

- [~] Qute history, goals & design
- [~] Hello example - what happens under the hood?
- [~] Fragments & HTMX example
- [~] Qute.next?

---

### Slides and examples

![GitHub repo link](deck-assets/qr.png)

<span class="demo" style="font-size:1.1em;">👀 EXAMPLE</span> ➵ https://github.com/mkouba/rivieradev2025

---

### Part 1 - Qute history, goals & design

---

### What is Qute?

- **QU**arkus **TE**mplating
- Quarkus core extension
- Introduced in Quarkus [1.1.0.CR1](https://github.com/quarkusio/quarkus/pull/5793), ~ 6 years ago

---

### Templates you say...

- [~] ✅ The good thing: everybody understands templates
- [~] ❌ The bad thing: everybody UNDERSTANDS templates

---

### What's the adoption of Qute?

- [~] We don't know the exact numbers,
- [~] but we fixed a number of bugs and implemented a bunch of feature requests!
- [~] 37 open issues, 328 closed issues

---

### Original high-level goals

- [~] Simple syntax with minimal logic
- [~] Extensibility
- [~] Build-time validations (Optional)
- [~] Friendly to [Quarkus Reactive Architecture](https://quarkus.io/guides/quarkus-reactive-architecture)
- [~] Friendly to native images
- [~] Decent performance
- [~] First-class Quarkus citizen

<!--
You don't want to fall into the trap of benchmark-driven development.
 -->
---

### Have we achieved all those goals?

[~] Yes ✅ and no ❌.

---

### What went wrong? #1

- Simple syntax with minimal logic
- [~] <span class="red">*Too many users requested more powerful syntax with more complex logic*</span>

---

### What went wrong? #2

- Extensibility
- [~] <span class="red">*Extensibility is essential but it often goes against performance optimizations*</span>

---

### What went wrong? #3

- Build-time validations (Optional)
- [~] <span class="red">*Build-time validations = killer feature, but they're hard to implement and not always 100%*</span>

---

### What went wrong? #4

- Friendly to [Quarkus Reactive Architecture](https://quarkus.io/guides/quarkus-reactive-architecture)
- [~] <span class="red">*Reactive means more complex code and sometimes goes against decent performance*</span>

---

### What went wrong? #5

- Friendly to native images
- [~] <span class="red">*TODO*</span>

---

### Part 2 - What happens under the hood?

---


### What happens under the hood?

1. **BUILD** - analyse, validate, generate
2. **RUNTIME** - initialize, parse, watch

---


### Template hello.html <span class="demo">👀 EXAMPLE</span>

```html[1: 1-16|7|9-11|14]
<html>
<head>
   <title>Hello - Riviera DEV 2025</title>
</head>
<body>
   <h1>Hello - Riviera DEV 2025</h1>
   <p>Hello {name ?: "Jean"}!</p>
   <ul>
   {#for header in headers.sorted.reversed}
      <li>{header}</li>
   {/for}
   </ul>
   <hr>
   Uptime: {cdi:system.upTime.seconds} s
</body>
</html>
```

---

### JAX-RS resource <span class="demo">👀 EXAMPLE</span>

```java[1: 1-15|4-5|9-13]
@Path("/hello")
public class HelloResource {

    record hello(String name, List<String> headers) 
             implements TemplateInstance {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@RestQuery String name, 
            HttpHeaders headers) {
        return new hello(name, headers.getRequestHeaders().entrySet()
                .stream().map(e -> e.getKey() + ": " + e.getValue())
                .toList());
    }
}
```

---

### Implied template parameters

- A type-safe template definition implies template parameter declarations:
  - `name` to `java.lang.String`
  - `headers` to `java.util.List<String>`

---

### Effective template

```html[1: 1-17|1-2]
{@String name}
{@java.util.List<String> headers}
<html>
<head>
   <title>Hello - Riviera DEV 2025</title>
</head>
<body>
   <p>Hello {name ?: "Jean"}!</p>
   <ul>
   {#for header in headers.sorted.reversed}
      <li>{header.toLowerCase}</li>
   {/for}
   </ul>
    <hr>
   Uptime: {cdi:upTime.seconds} s
</body>
</html>
```

---

### What happens during the build?

- [~] Parse, analyze and validate all known templates
- [~] Generate optimized bytecode

---

### Build - analysis

- find all Qute constructs in the template
  - `{name ?: "Jean"}`
  - `{#for header in headers.sorted.reversed}`
  - `{header.toLowerCase}`
  - `{/for}` (validation not needed)
  - `{cdi:system.upTime.seconds}`

---

### Build validation - `headers.sorted` <span class="demo">👀 EXAMPLE</span>

- [~] Is there `sorted` on `List` ? 
- [~] Wait, there is no `sorted` on `List`!
- [~] ✅ You're right, `headers.sorted` is handled by the template extension method:
   ```java
    @TemplateExtension
    static List<String> sorted(List<String> list) {
        List<String> sorted = new ArrayList<>(list);
        sorted.sort(null);
        return sorted;
    }
   ```

---

### Build validation - `headers.sorted.reversed` <span class="demo">👀 EXAMPLE</span>

- [~] Is there `reversed` on `List`? 
- [~] Wait, there is no `reversed` on `List`!
- [~] ✅ You're right again, `sorted.reversed` is handled by the template extension method:
   ```java
    @TemplateExtension
    static <T> Iterator<T> reversed(List<T> list) {
        ListIterator<T> it = list.listIterator(list.size());
        return new Iterator<T>() {
            public boolean hasNext() {
                return it.hasPrevious();
            }
            public T next() {
                return it.previous();
            }
        };
    }
   ```
---

### Build validation - `header.toLowerCase`

- [~] Is there `toLowerCase` on `String`? 
- [~] ✅ It's there!
- [~] NOTE: `String` was derived from the previous validations of `headers.sorted.reversed`
---

### Build validation - `cdi:system`

- [~] Is there a CDI bean with the name `system`? 
- [~] ✅ It's there!
---

### Build validation - `cdi:system.upTime`

- [~] Is there `upTime` on `org.acme.System`? 
- [~] ✅ `upTime()` is there so we're fine!
---

### Build validation - `cdi:system.upTime.seconds`

- [~] Is there `seconds` on `java.time.Duration`? 
- [~] ✅ `getSeconds()` is there so we're fine!
---

### Build validations results

⛔ If any of the validations fails then the build fails as well.

---

### Build - generating optimized bytecode

- [~] `{header.toLowerCase}` → generate an accessor for `java.lang.String#toLowerCase()`
- [~] `{cdi:system.upTime.seconds}` → generate an accessor for `org.acme.System#upTime()`
- [~] etc.

---

### How does it look like?

Let's see...

---

### What happens at runtime

- [~] Engine is initialized and configured
- [~] All known templates are parsed and cached
- [~] In the dev mode, all templates are watched for changes

---

### Part - Template fragments & HTMX example

---

### Template systemInfo.html <span class="demo">👀 EXAMPLE</span>

```html[1: 1-16|4|9-10|11-13]
<html>
<head>
   <title>System info - Riviera DEV 2025</title>
   {#bundle /}
</head>
<body>
   <h1>System info - Riviera DEV 2025</h1>
   <div class="uptime" 
      hx-get="/systemInfo?frag=uptime" 
      hx-trigger="every 2s">
      {#fragment id=uptime}
      Up-time: {cdi:system.upTime.seconds} s
      {/fragment}
    </div>
   </p>
</body>
</html>
```

---

### Also powered by Qute...

- [~] Quarkus mailer extension
- [~] Renarde
- [~] Roq

---

### Part - Qute.NEXT

- [~] Improve the parser
- [~] Extend the syntax

---

### That's all folks

![GitHub repo link](deck-assets/qr.png)

https://github.com/mkouba/rivieradev2025

Thank you!

---
