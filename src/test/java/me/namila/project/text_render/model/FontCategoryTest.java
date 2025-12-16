package me.namila.project.text_render.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FontCategoryTest {

    @Test
    void shouldHaveBuiltInCategory() {
        // Then
        assertThat(FontCategory.BUILT_IN).isNotNull();
        assertThat(FontCategory.BUILT_IN.name()).isEqualTo("BUILT_IN");
    }

    @Test
    void shouldHaveSystemCategory() {
        // Then
        assertThat(FontCategory.SYSTEM).isNotNull();
        assertThat(FontCategory.SYSTEM.name()).isEqualTo("SYSTEM");
    }

    @Test
    void shouldHaveExactlyTwoCategories() {
        // Then
        assertThat(FontCategory.values()).hasSize(2);
    }
}
