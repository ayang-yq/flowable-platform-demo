package com.flowable.platform.test.assertions;

import com.flowable.platform.entity.User;
import org.assertj.core.api.AbstractAssert;

/**
 * Custom AssertJ assertions for User entities.
 */
public class UserAssert extends AbstractAssert<UserAssert, User> {

    private UserAssert(User actual) {
        super(actual, UserAssert.class);
    }

    public static UserAssert assertThat(User actual) {
        return new UserAssert(actual);
    }

    public UserAssert hasUsername(String username) {
        isNotNull();
        if (!actual.getUsername().equals(username)) {
            failWithMessage("Expected username to be <%s> but was <%s>", username, actual.getUsername());
        }
        return this;
    }

    public UserAssert hasEmail(String email) {
        isNotNull();
        if (!actual.getEmail().equals(email)) {
            failWithMessage("Expected email to be <%s> but was <%s>", email, actual.getEmail());
        }
        return this;
    }

    public UserAssert hasRole(String roleName) {
        isNotNull();
        boolean hasRole = actual.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
        if (!hasRole) {
            failWithMessage("Expected user to have role <%s>", roleName);
        }
        return this;
    }

    public UserAssert isActive() {
        isNotNull();
        if (!actual.getIsActive()) {
            failWithMessage("Expected user to be active");
        }
        return this;
    }

    public UserAssert isInactive() {
        isNotNull();
        if (actual.getIsActive()) {
            failWithMessage("Expected user to be inactive");
        }
        return this;
    }

    public UserAssert hasFirstName(String firstName) {
        isNotNull();
        if (!actual.getFirstName().equals(firstName)) {
            failWithMessage("Expected firstName to be <%s> but was <%s>", firstName, actual.getFirstName());
        }
        return this;
    }

    public UserAssert hasLastName(String lastName) {
        isNotNull();
        if (!actual.getLastName().equals(lastName)) {
            failWithMessage("Expected lastName to be <%s> but was <%s>", lastName, actual.getLastName());
        }
        return this;
    }

    public UserAssert hasLocale(String locale) {
        isNotNull();
        if (!actual.getLocale().equals(locale)) {
            failWithMessage("Expected locale to be <%s> but was <%s>", locale, actual.getLocale());
        }
        return this;
    }

    public UserAssert hasTimezone(String timezone) {
        isNotNull();
        if (!actual.getTimezone().equals(timezone)) {
            failWithMessage("Expected timezone to be <%s> but was <%s>", timezone, actual.getTimezone());
        }
        return this;
    }

    public UserAssert belongsToTenant(String tenantCode) {
        isNotNull();
        if (!actual.getTenant().getCode().equals(tenantCode)) {
            failWithMessage("Expected user to belong to tenant <%s> but was <%s>", tenantCode, actual.getTenant().getCode());
        }
        return this;
    }
}
