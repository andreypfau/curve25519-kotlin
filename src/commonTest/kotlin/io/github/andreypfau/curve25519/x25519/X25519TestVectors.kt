package io.github.andreypfau.curve25519.x25519

import io.github.andreypfau.curve25519.internal.hex

data class X25519TestVector(
    val scalar: ByteArray,
    val point: ByteArray,
    val expected: ByteArray
)

val X25519_TEST_VECTORS = arrayOf(
    X25519TestVector(
        scalar = hex("668fb9f76ad971c81ac900071a1560bce2ca00cac7e67af99348913761434014"),
        point = hex("db5f32b7f841e7a1a00968effded12735fc47a3eb13b579aacadeae80939a7dd"),
        expected = hex("090d85e599ea8e2beeb61304d37be10ec5c905f9927d32f42a9a0afb3e0b4074"),
    ),
    X25519TestVector(
        scalar = hex("636695e34f75b9a279c8706fad1289f2c0b1e22e16f8b8861729c10a582958af"),
        point = hex("090d0701f8fde28f70043b83f2346225419b18a7f27e9e3d2bfd04e10f3d213e"),
        expected = hex("bf26ec7ec413061733d44070ea67cab02a85dc1be8cfe1ff73d541cc08325506"),
    ),
    X25519TestVector(
        scalar = hex("734181cd1a9406522a56fe25e43ecbf0295db5ddd0609b3c2b4e79c06f8bd46d"),
        point = hex("f8a8421c7d21a92db3ede979e1fa6acb062b56b1885c71c51153ccb880ac7315"),
        expected = hex("1176d01681f2cf929da2c7a3df66b5d7729fd422226fd6374216bf7e02fd0f62"),
    ),
    X25519TestVector(
        scalar = hex("1f70391f6ba858129413bd801b12acbf662362825ca2509c8187590a2b0e6172"),
        point = hex("d3ead07a0008f44502d5808bffc8979f25a859d5adf4312ea487489c30e01b3b"),
        expected = hex("f8482f2e9e58bb067e86b28724b3c0a3bbb5073e4c6acd93df545effdbba505f"),
    ),
    X25519TestVector(
        scalar = hex("3a7ae6cf8b889d2b7a60a470ad6ad999206bf57d9030ddf7f8680c8b1a645daa"),
        point = hex("4d254c8083d87f1a9b3ea731efcff8a6f2312d6fed680ef829185161c8fc5060"),
        expected = hex("47b356d5818de8efac774b714c42c44be68523dd57dbd73962d5a52631876237"),
    ),
    X25519TestVector(
        scalar = hex("203161c3159a876a2beaec29d2427fb0c7c30d382cd013d27cc3d393db0daf6f"),
        point = hex("6ab95d1abe68c09b005c3db9042cc91ac849f7e94a2a4a9b893678970b7b95bf"),
        expected = hex("11edaedc95ff78f563a1c8f15591c071dea092b4d7ecaac8e0387b5a160c4e5d"),
    ),
    X25519TestVector(
        scalar = hex("13d65491fe75f203a008b4415abc60d532e695dbd2f1e803accb34b2b72c3d70"),
        point = hex("2e784e04ca0073336256a839255ed2f7d4796a64cdc37f1eb0e5c4c8d1d1e0f5"),
        expected = hex("563e8c9adaa7d73101b0f2ead3cae1ea5d8fcd5cd36080bb8e6ec03d61450917"),
    ),
    X25519TestVector(
        scalar = hex("686f7da93bf268e588069831f047163f33589989d0826e9808fb678ed57e6749"),
        point = hex("8b549b2df642d3b25fe8380f8cc4375f99b7bb4d275f779f3b7c81b8a2bbc129"),
        expected = hex("01476965426b6171749a8add9235025ce5f557fe4009f7393044ebbb8ae95279"),
    ),
    X25519TestVector(
        scalar = hex("82d61ccedc806a6060a3349a5e87cbc7ac115e4f87776250ae256098a7c44959"),
        point = hex("8b6b9d08f61fc91fe8b32953c42340f007b571dcb0a56d10724ecef9950cfb25"),
        expected = hex("9c49941f9c4f1871fa4091fed716d34999c95234edf2fdfba6d14a5afe9e0558"),
    ),
    X25519TestVector(
        scalar = hex("7dc76404831397d5884fdf6f97e1744c9eb118a31a7b23f8d79f48ce9cad154b"),
        point = hex("1acd292784f47919d455f887448358610bb9459670eb99dee46005f689ca5fb6"),
        expected = hex("00f43c022e94ea3819b036ae2b36b2a76136af628a751fe5d01e030d44258859"),
    ),
    X25519TestVector(
        scalar = hex("fbc4511d23a682ae4efd08c8179c1c067f9c8be79bbc4eff5ce296c6bc1ff445"),
        point = hex("55caff2181f2136b0ed0e1e2994448e16cc970646a983d140dc4eab3d94c284e"),
        expected = hex("ae39d816532345794d2691e0801caa525fc3634d402ce9580b3338b46f8bb972"),
    ),
    X25519TestVector(
        scalar = hex("4e060ce10cebf095098716c86619eb9f7df66524698ba7988c3b9095d9f50134"),
        point = hex("57733f2d869690d0d2edaec9523daa2da95445f44f5783c1faec6c3a982818f3"),
        expected = hex("a61e74552cce75f5e972e424f2ccb09c83bc1b67014748f02c371a209ef2fb2c"),
    ),
    X25519TestVector(
        scalar = hex("5c492cba2cc892488a9ceb9186c2aac22f015bf3ef8d3ecc9c4176976261aab1"),
        point = hex("6797c2e7dc92ccbe7c056bec350ab6d3bd2a2c6bc5a807bbcae1f6c2af803644"),
        expected = hex("fcf307dfbc19020b28a6618c6c622f317e45967dacf4ae4a0a699a10769fde14"),
    ),
    X25519TestVector(
        scalar = hex("ea33349296055a4e8b192e3c23c5f4c844282a3bfc19ecc9dc646a42c38dc248"),
        point = hex("2c75d85142ecad3e69447004540c1c23548fc8f486251b8a19463f3df6f8ac61"),
        expected = hex("5dcab68973f95bd3ae4b34fab949fb7fb15af1d8cae28cd699f9c1aa3337342f"),
    ),
    X25519TestVector(
        scalar = hex("4f2979b1ec8619e45c0a0b2b520934541ab94407b64d190a76f32314efe184e7"),
        point = hex("f7cae18d8d36a7f56117b8b70e2552277ffc99df8756b5e138bf6368bc87f74c"),
        expected = hex("e4e634ebb4fb664fe8b2cfa1615f00e6466fff732ce1f8a0c8d2727431d16f14"),
    ),
    X25519TestVector(
        scalar = hex("f5d8a927901d4fa4249086b7ffec24f5297d80118e4ac9d3fc9a8237951e3b7f"),
        point = hex("3c235edc02f9115641dbf516d5de8a735d6e53e22aa2ac143656045ff2e95249"),
        expected = hex("ab9515ab14af9d270e1dae0c5680cbc8880bd8a8e7eb67b4da42a661961efc0b"),
    ),
)
