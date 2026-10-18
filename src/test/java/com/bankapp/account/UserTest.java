package com.bankapp.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {

    @Test
    @DisplayName("stores name and email")
    void constructorStoresFields() {
        User user = new User("Ayse Yilmaz", "ayse@example.com");

        assertAll(
                () -> assertEquals("Ayse Yilmaz", user.getName()),
                () -> assertEquals("ayse@example.com", user.getEmail())
        );
    }

    @Test
    @DisplayName("replaces name and email")
    void settersReplaceFields() {
        User user = new User("Old Name", "old@example.com");

        user.setName("New Name");
        user.setEmail("new@example.com");

        assertAll(
                () -> assertEquals("New Name", user.getName()),
                () -> assertEquals("new@example.com", user.getEmail())
        );
    }

    @Test
    @DisplayName("accepts null values")
    void nullValuesAreAccepted() {
        User user = new User(null, null);

        assertNull(user.getName());
        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("equals is not overridden")
    void equalityIsIdentityBased() {
        assertNotEquals(new User("Ali", "ali@example.com"), new User("Ali", "ali@example.com"));
    }
}