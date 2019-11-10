package com.github.brymck.gsondeepmerge;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GsonDeepMergeTest {
  private Gson gson = new Gson();
  private GsonDeepMerge gsonDeepMerge = new GsonDeepMerge();

  @Test
  void combinesBothObjectsWhereNoConflictsExist() {
    String json = "{\"name\":\"Foo\"}";
    String updateJson = "{\"country\":\"JP\"}";
    String expectedJson = "{\"name\":\"Foo\",\"country\":\"JP\"}";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject update = gson.fromJson(updateJson, JsonObject.class);
    JsonObject merged = gsonDeepMerge.deepMerge(original, update);
    String actualJson = gson.toJson(merged);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  void prefersTheUpdatedValueWhenConflictsExist() {
    String json = "{\"country\":\"US\"}";
    String updateJson = "{\"country\":\"JP\"}";
    String expectedJson = "{\"country\":\"JP\"}";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject update = gson.fromJson(updateJson, JsonObject.class);
    JsonObject merged = gsonDeepMerge.deepMerge(original, update);
    String actualJson = gson.toJson(merged);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  void ignoresTheUpdatedValueWhenItsNull() {
    String json = "{\"name\":\"Foo\"}";
    String updateJson = "{\"name\":null}";
    String expectedJson = "{\"name\":\"Foo\"}";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject update = gson.fromJson(updateJson, JsonObject.class);
    JsonObject merged = gsonDeepMerge.deepMerge(original, update);
    String actualJson = gson.toJson(merged);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  void extendsArrays() {
    String json = "{\"countries\":[\"US\"]}";
    String updateJson = "{\"countries\":[\"JP\"]}";
    String expectedJson = "{\"countries\":[\"US\",\"JP\"]}";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject update = gson.fromJson(updateJson, JsonObject.class);
    JsonObject merged = gsonDeepMerge.deepMerge(original, update);
    String actualJson = gson.toJson(merged);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  void deepMerges() {
    String json = "{\"person\":{\"name\":\"Foo\"}}";
    String updateJson = "{\"person\":{\"country\":\"JP\"}}";
    String expectedJson = "{\"person\":{\"name\":\"Foo\",\"country\":\"JP\"}}";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject update = gson.fromJson(updateJson, JsonObject.class);
    JsonObject merged = gsonDeepMerge.deepMerge(original, update);
    String actualJson = gson.toJson(merged);
    assertEquals(expectedJson, actualJson);
  }

  @Test
  void deepMergesClass() {
    Person alice = new Person();
    alice.setName("Alice");
    Person bob = new Person();
    bob.setName("Bob");
    Person marriageUpdate = new Person();
    marriageUpdate.setSpouse(bob);
    ;
    Person marriedAlice = gsonDeepMerge.deepMerge(gson, alice, marriageUpdate, Person.class);
    assertAll(
        () -> assertEquals("Alice", marriedAlice.getName()),
        () -> assertNotNull(marriedAlice.getSpouse()),
        () -> assertEquals("Bob", marriedAlice.getSpouse().getName()));
  }

  @ParameterizedTest
  @CsvSource({"0,1", "\"zero\",\"one\"", "false,true", "[],[1]", "{},{\"foo\":\"bar\"}"})
  void succeedsWhenTypesDoNotConflict(String value1, String value2) {
    String json1 = String.format("{ \"value\": %s }", value1);
    String json2 = String.format("{ \"value\": %s }", value2);
    JsonObject original1 = gson.fromJson(json1, JsonObject.class);
    JsonObject original2 = gson.fromJson(json2, JsonObject.class);
    assertAll(
        () -> Assertions.assertDoesNotThrow(() -> gsonDeepMerge.deepMerge(original1, original2)),
        () -> Assertions.assertDoesNotThrow(() -> gsonDeepMerge.deepMerge(original2, original1)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"zero\"", "0", "false", "[]", "{}"})
  void succeedsWhenOneValueIsNull(String value) {
    String json = String.format("{ \"value\": %s }", value);
    String nullJson = "{ \"value\": null }";
    JsonObject original = gson.fromJson(json, JsonObject.class);
    JsonObject nullJsonObject = gson.fromJson(nullJson, JsonObject.class);
    assertAll(
        () ->
            Assertions.assertDoesNotThrow(() -> gsonDeepMerge.deepMerge(original, nullJsonObject)),
        () ->
            Assertions.assertDoesNotThrow(() -> gsonDeepMerge.deepMerge(nullJsonObject, original)));
  }

  @ParameterizedTest
  @CsvSource({
    "\"zero\",0",
    "\"zero\",false",
    "\"zero\",[]",
    "\"zero\",{}",
    "0,false",
    "0,[]",
    "0,{}",
    "[],{}"
  })
  void throwsAnExceptionWhenTypesConflict(String value1, String value2) {
    String json1 = String.format("{ \"value\": %s }", value1);
    String json2 = String.format("{ \"value\": %s }", value2);
    JsonObject jsonObject1 = gson.fromJson(json1, JsonObject.class);
    JsonObject jsonObject2 = gson.fromJson(json2, JsonObject.class);
    assertAll(
        () ->
            assertThrows(
                IllegalStateException.class,
                () -> gsonDeepMerge.deepMerge(jsonObject1, jsonObject2)),
        () ->
            assertThrows(
                IllegalStateException.class,
                () -> gsonDeepMerge.deepMerge(jsonObject2, jsonObject1)));
  }
}
