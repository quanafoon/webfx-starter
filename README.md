# WebFX

WebFX is a lightweight Java library designed to bring the familiar web development experience to desktop development. It uses JavaFX’s `WebView` to simulate a web-based frontend, while enabling developers to write backend logic in Java, bridging the gap between traditional web apps and desktop apps.

---

## Purpose

WebFX is ideal for developers who:

- Prefer building UIs with HTML/CSS/JS.
- Want backend logic written in Java.
- Need a minimal setup for desktop app packaging (MSI, EXE).
- Are building tools that feel like web apps but run natively.

---

## 🧩 Core Technologies

- **JavaFX WebView** – Renders HTML/CSS/JS interfaces.
- **Gson** – Facilitates JSON serialization and communication.
- **Reflection** – Powers dynamic routing, form binding, and annotations.

---

## 📦 Modules

### 1. **Router**
Simulates routing in web apps. Works with annotated methods and handles:

- Navigation (`route("home")`, `route_with_data("profile", data)`).
- Dynamic data binding using `@Endpoint`.
- JSON form submission handling.
- Seamless view transitions (like SPAs).


### 2. **ResourceManager**
Manages static assets like:

- Stylesheets, scripts, images, templates, and more.
- Ensures packaged assets are accessible via `getResourcePath(...)`.
- Works with external files post-packaging using `/resources` structure.
- It mitigates the need to explicitly write sql, peferring simple java functions.

### 3. **Database**
Simple SQLite-based ORM-like layer:

- Provides methods like `get()`, `getAll()`, `insert()`, `update()`, `delete()`.
- Uses `@Table` and `@Column` annotations for binding Java classes to SQL tables.
- Supports custom `WHERE` clauses and automatic `Data` mapping.


Example:

```java
get("Users", "1")
```
Example where sql is needed:

```java
getAll("Users", "username = ? AND active = ?", "john", "1");
```


### 4. Template Engine

- Variable injection using ${variableName}.
- Loop support: <!-- loop list as item -->...<!-- end -->
- Conditional rendering: <!-- if condition -->...<!-- end -->
- Can be toggled on/off per template.
- Does not require additional runtime libraries.








