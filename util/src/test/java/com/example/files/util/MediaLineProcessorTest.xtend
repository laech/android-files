package com.example.files.util

import com.example.files.util.media.MediaLineProcessor
import com.google.common.collect.ImmutableMap
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class MediaLineProcessorTest {

  var MediaLineProcessor processor

  @Test def ignoresComments() {
    assertTrue(processor.processLine("# This is a comment"))
    assertTrue(processor.result.isEmpty)
  }

  @Test def processesMultipleExtensions() {
    assertTrue(processor.processLine("text/html\t html \thtm"))
    val expected = ImmutableMap::of("htm", "text/html", "html", "text/html")
    assertEquals(expected, processor.result)
  }

  @Test def processesOneExtension() {
    assertTrue(processor.processLine("application/json\t json"))
    val expected = ImmutableMap::of("json", "application/json")
    assertEquals(expected, processor.result)
  }

  @Before def void setup() {
    processor = new MediaLineProcessor
  }
}