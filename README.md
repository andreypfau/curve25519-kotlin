# curve25519-kotlin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.andreypfau/curve25519-kotlin.svg)](https://search.maven.org/artifact/io.github.andreypfau/curve25519-kotlin/0.0.4/pom)
[![Kotlin](https://img.shields.io/badge/kotlin-1.7.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

**A pure Kotlin/Multiplatform implementation of group operations on Curve25519.**

### Gradle Kotlin DSL:

```kotlin
dependencies {
    implementation("io.github.andreypfau:curve25519-kotlin:0.0.4")
}
```

### Apache Maven:

```xml

<dependency>
    <groupId>io.github.andreypfau</groupId>
    <artifactId>curve25519-kotlin-jvm</artifactId>
    <version>0.0.4</version>
</dependency>
```

## Examples:

### Generate key-pair from random

```kotlin
val privateKey: Ed25519PrivateKey = Ed25519.generateKey(Random)
val publicKey: Ed25519PublicKey = privateKey.publicKey()
```

### Generate key-pair from seed bytes

```kotlin
val seedBytes: ByteArray = ByteArray(32)
val privateKey: Ed25519PrivateKey = Ed25519.keyFromSeed(seedBytes)
val publicKey: Ed25519PublicKey = privateKey.publicKey() 
```

### Signing messages & verify signatures

```kotlin
val message: ByteArray = "test message".encodeToByteArray()
val signature: ByteArray = privateKey.sign(message)

check(publicKey.verify(message, signature)) // Valid message returns true

val invalidMessage = "invalid message".encodeToByteArray()
check(!publicKey.verify(invalidMessage, signature)) // Invalid message returns false
```

### Shared key calculation

```kotlin
val alicePrivate = Ed25519.generateKey(Random)
val alicePublic = alicePrivate.publicKey()

val bobPrivate = Ed25519.generateKey(Random)
val bobPublic = bobPrivate.publicKey()

val aliceShared = alicePrivate.sharedKey(bobPublic)
val bobShared = bobPrivate.sharedKey(alicePublic)

check(aliceShared.contentEquals(bobShared))
```
