import com.github.andreypfau.curve25519.ed25519.Ed25519
import com.github.andreypfau.curve25519.ed25519.Ed25519PrivateKey
import com.github.andreypfau.curve25519.internal.hex
import kotlin.test.assertContentEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main() {
    testGolden()
}

@OptIn(ExperimentalTime::class)
fun testGolden() {
    var time = Duration.ZERO
    val lines = GOLDEN_ED25519.lines()
    val iterations = 1000
    repeat(iterations) {
        lines.forEach { line ->
            time += measureTime {
                testGolden(line)
            }.also {
                println("$it - $line")
            }
        }
    }
    println("avg: ${time / lines.size / iterations}")
}

fun testGolden(line: String) {
    var (
        privateBytes,
        pubKey,
        msg,
        sig
    ) = line.split(":").map { hex(it) }

    sig = sig.copyOf(Ed25519.SIGNATURE_SIZE_BYTES)
    val priv = Ed25519PrivateKey(ByteArray(Ed25519.PRIVATE_KEY_SIZE_BYTES))
    privateBytes.copyInto(priv.data)
    pubKey.copyInto(priv.data, 32)

    val sig2 = priv.sign(msg)
    assertContentEquals(sig, sig2)

    val priv2 = Ed25519.keyFromSeed(priv.data.copyOf(32))
    assertContentEquals(priv.data, priv2.data)

    val pubKey2 = priv2.publicKey()
    assertContentEquals(pubKey, pubKey2.data)

    val seed = priv2.seed()
    assertContentEquals(priv.data.copyOf(32), seed)
}
