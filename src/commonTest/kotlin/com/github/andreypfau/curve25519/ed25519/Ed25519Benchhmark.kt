package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import kotlinx.benchmark.*
import kotlin.test.Test

@State(Scope.Benchmark)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
open class Ed25519Benchhmark {
    lateinit var seed: ByteArray
    lateinit var privateKey: Ed25519PrivateKey
    lateinit var publicKey: Ed25519PublicKey
    lateinit var message: ByteArray
    lateinit var signature: ByteArray

    @Setup
    fun setup() {
        seed = ByteArray(Ed25519.SEED_SIZE_BYTES)
        privateKey = Ed25519.generateKey(ZeroRandom)
        publicKey = privateKey.publicKey()
        message = "Hello World!".encodeToByteArray()
        signature = privateKey.sign(message)
    }

    @Benchmark
    fun benchmarkGenerateKey(blackhole: Blackhole) {
        blackhole.consume(
            Ed25519.generateKey(ZeroRandom)
        )
    }

    @Benchmark
    fun benchmarkKeyFromSeed(blackhole: Blackhole) {
        blackhole.consume(
            Ed25519.keyFromSeed(seed)
        )
    }

    @Benchmark
    fun benchmarkSigning(blackhole: Blackhole) {
        blackhole.consume(
            privateKey.sign(message)
        )
    }

    @Benchmark
    fun benchmarkVerification(blackhole: Blackhole) {
        blackhole.consume(
            publicKey.verify(message, signature)
        )
    }

    @Test
    fun a() {
        setup()
        publicKey.verify(message, signature)
    }
}
