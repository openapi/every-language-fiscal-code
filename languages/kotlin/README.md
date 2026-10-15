# Kotlin

Implementation of the Italian Fiscal Code in Kotlin 2.1, running on the JVM
using only the Kotlin and JDK standard libraries. Includes a minimal JSON parser.

- Source: [`FiscalCode.kt`](FiscalCode.kt)
- Toolchain: compiled with `kotlinc` on `eclipse-temurin:21-jdk-alpine`, run on
  `eclipse-temurin:21-jre-alpine` (see [`Dockerfile`](Dockerfile))

```bash
./elc test kotlin
```
