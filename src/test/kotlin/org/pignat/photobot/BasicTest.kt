package org.pignat.photobot

import org.junit.jupiter.api.Test
import org.pignat.photobot.Bot
import kotlin.test.assertNotEquals

class BasicTest {
    @Test
    fun testVersion() {
        assertNotEquals(Bot.version(), "");
    }
}