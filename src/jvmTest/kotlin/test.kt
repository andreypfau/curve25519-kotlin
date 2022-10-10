import com.github.andreypfau.curve25519.scalar.UInt128

fun main() {
    val a = UInt128(0xFFFFFFFFFFFFFFFFu, 0uL)
    repeat(129) {
        println("$it: ${a.shr(it)}")
    }
}
