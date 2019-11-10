package com.github.brymck.gsondeepmerge;

import com.google.gson.*;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class GsonDeepMerge {
  public <T> T deepMerge(
      @NotNull Gson gson, @NotNull T original, @NotNull T update, @NotNull Class<T> classOfT)
      throws IllegalStateException {
    JsonObject originalJsonObject = gson.toJsonTree(original).getAsJsonObject();
    JsonObject updateJsonObject = gson.toJsonTree(update).getAsJsonObject();
    JsonObject mergedJsonObject = deepMerge(originalJsonObject, updateJsonObject);
    return gson.fromJson(mergedJsonObject, classOfT);
  }

  /**
   * Recursively merge a [JsonObject] with a provided [updateObject]. This method mutates the
   * [JsonObject] it's called on.
   */
  public JsonObject deepMerge(@NotNull JsonObject originalObject, @NotNull JsonObject updateObject)
      throws IllegalStateException {
    // We make a deep copy of the original object, which is what deepMergeInPlace operates on
    return deepMergeInPlace(originalObject.deepCopy(), updateObject);
  }

  /**
   * Recursively merge a [JsonObject] with a provided [updateObject]. This method mutates the
   * [JsonObject] it's called on.
   */
  private JsonObject deepMergeInPlace(
      @NotNull JsonObject originalObject, @NotNull JsonObject updateObject)
      throws IllegalStateException {
    for (Map.Entry<String, JsonElement> entry : updateObject.entrySet()) {
      String updateKey = entry.getKey();
      JsonElement updateValue = entry.getValue();
      if (originalObject.has(updateKey)) {
        // Handle conflicts with different logic for arrays, objects and primitives
        JsonElement originalValue = originalObject.get(updateKey);
        if (typesConflict(originalValue, updateValue)) {
          String message = String.format("Type of %s and %s conflict", originalValue, updateValue);
          throw new IllegalStateException(message);
        } else if (originalValue.isJsonArray() && updateValue.isJsonArray()) {
          // Extend arrays
          JsonArray originalArray = originalValue.getAsJsonArray();
          JsonArray updateArray = updateValue.getAsJsonArray();
          for (JsonElement it : updateArray) {
            originalArray.add(it);
          }
        } else if (originalValue.isJsonObject() && updateValue.isJsonObject()) {
          // Update objects, preferring the update value
          deepMergeInPlace(originalValue.getAsJsonObject(), updateValue.getAsJsonObject());
        } else if (!updateValue.isJsonNull()) {
          // Use the update value unless it's null
          originalObject.add(updateKey, updateValue);
        }
        // Otherwise, keep the existing value
      } else {
        // No conflicts. Add updated value to object
        originalObject.add(updateKey, updateValue);
      }
    }
    return originalObject;
  }

  private boolean typesConflict(@NotNull JsonElement element1, @NotNull JsonElement element2) {
    if (element1.isJsonNull()) {
      return false;
    } else if (element2.isJsonNull()) {
      return false;
    } else if (element1.isJsonObject()) {
      return !element2.isJsonObject();
    } else if (element1.isJsonArray()) {
      return !element2.isJsonArray();
    } else {
      // We can assume the first element is a primitive
      if (element2.isJsonPrimitive()) {
        JsonPrimitive primitive1 = element1.getAsJsonPrimitive();
        JsonPrimitive primitive2 = element2.getAsJsonPrimitive();
        if (primitive1.isBoolean()) {
          return !primitive2.isBoolean();
        } else if (primitive1.isNumber()) {
          return !primitive2.isNumber();
        } else {
          // We can assume the first element is a string
          return !primitive2.isString();
        }
      } else {
        return true;
      }
    }
  }
}
