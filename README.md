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

## Core Technologies

- **JavaFX WebView** – Renders HTML/CSS/JS interfaces.
- **Gson** – Facilitates JSON serialization and communication.
- **Reflection** – Powers dynamic routing, form binding, and annotations.

---

## Modules

### 1. **Router**
Simulates routing in web apps. Works with annotated methods and handles:

- Navigation (`route("home")`, `route_with_data("profile", data)`).
- Dynamic data binding using `@Endpoint`.
- JSON form submission handling.
- Seamless view transitions.


### 2. **ResourceManager**
Manages static assets like:

- Stylesheets, scripts, images, templates, and more.
- Ensures packaged assets are accessible via `getResourcePath(...)`.
- Works with external files post-packaging using `/resources` structure.
- It mitigates the need to explicitly write sql, peferring simple java functions.


### 3. **Database**
Simple SQLite-based ORM-like layer:

- Provides methods like `get()`, `getAll()`, `insert()`, `update()`, `delete()`.
- Uses `@Nullable` and `@Ignore` annotations to provide control over database columns.
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
- Can be toggled on/off per template.
- Does not require additional runtime libraries.


## Annotations
WebFX uses annotations to reduce boilerplate:

- @Endpoint(path = "submit"): Binds Java method to a JS form submission.
- @Ignore variable: Ignores variable when database table is made from class.
- @Nullable variable: Allows database column to be nullable.


## File Structure
Projects should adhere to the following file structure to make get the most out of the library, especially the ResourceManager

```
src/
└── main/
    ├── java/
    │   └── your/package/
    └── resources/
        ├── templates/
        ├── styles/
        └── images/
```
- Certain methods from the ResourceManager like `getTemplate()` and `getStyleSheet()` would look for files within templates and styles folder.
- Of course the `getResource()` can be used with any structure, but the ResourceManager's get functions ensures that these files being retreived are of the correct type.


## Summary
- WebFX helps developers write Java desktop apps like they’re building web apps—just plug in HTML/CSS/JS, write backend logic in Java, and enjoy fast, native performance with a web-like development workflow.

- Built for developers who love both web and Java.



