package me.namila.project.text_render.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutputFileNameGeneratorTest {

    @Test
    void shouldGenerateFilenameWithPrefixAndPostfix() {
        // Given
        String templatePath = "/path/to/template.pdf";
        String text = "John Doe";
        String prefix = "wedding";
        String postfix = "final";
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("wedding-template-John-final.pdf");
    }

    @Test
    void shouldGenerateFilenameWithOnlyPrefix() {
        // Given
        String templatePath = "/path/to/invitation.png";
        String text = "Alice Smith";
        String prefix = "batch1";
        String postfix = null;
        String format = "png";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("batch1-invitation-Alice.png");
    }

    @Test
    void shouldGenerateFilenameWithOnlyPostfix() {
        // Given
        String templatePath = "template.pdf";
        String text = "Bob Johnson";
        String prefix = null;
        String postfix = "v2";
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-Bob-v2.pdf");
    }

    @Test
    void shouldGenerateFilenameWithoutPrefixAndPostfix() {
        // Given
        String templatePath = "/templates/card.pdf";
        String text = "Charlie Brown";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("card-Charlie.pdf");
    }

    @Test
    void shouldHandleEmptyPrefixAndPostfix() {
        // Given
        String templatePath = "template.pdf";
        String text = "Test Name";
        String prefix = "";
        String postfix = "   ";
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then - empty/blank strings should be treated as null
        assertThat(result).isEqualTo("template-Test.pdf");
    }

    @Test
    void shouldSanitizeSpecialCharactersInText() {
        // Given
        String templatePath = "template.pdf";
        String text = "John:Doe<Test>Name";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then - special characters should be replaced with underscore
        assertThat(result).isEqualTo("template-John_Doe_Test_Name.pdf");
    }

    @Test
    void shouldSanitizeAllInvalidFilenameCharacters() {
        // Given
        String templatePath = "template.pdf";
        String text = "Name\\With/Special*Chars?\"<>|End";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).doesNotContain("\\", "/", "*", "?", "\"", "<", ">", "|");
    }

    @Test
    void shouldTruncateLongText() {
        // Given
        String templatePath = "template.pdf";
        String text = "ThisIsAVeryLongNameThatExceedsFiftyCharactersAndShouldBeTruncatedProperly";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then - text part should be max 50 chars
        String textPart = result.replace("template-", "").replace(".pdf", "");
        assertThat(textPart.length()).isLessThanOrEqualTo(50);
    }

    @Test
    void shouldExtractBaseNameFromTemplatePath() {
        // Given
        String templatePath = "/deep/nested/path/to/my-template.pdf";
        String text = "Test";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("my-template-Test.pdf");
    }

    @Test
    void shouldExtractFirstWordFromText() {
        // Given
        String templatePath = "template.pdf";
        String text = "First Second Third";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-First.pdf");
    }

    @Test
    void shouldHandleTextWithCommas() {
        // Given
        String templatePath = "template.pdf";
        String text = "LastName, FirstName";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then - should extract first part before comma
        assertThat(result).isEqualTo("template-LastName.pdf");
    }

    @Test
    void shouldHandleTextWithSemicolons() {
        // Given
        String templatePath = "template.pdf";
        String text = "Name1; Name2; Name3";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-Name1.pdf");
    }

    @Test
    void shouldHandleNullText() {
        // Given
        String templatePath = "template.pdf";
        String text = null;
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-unnamed.pdf");
    }

    @Test
    void shouldHandleBlankText() {
        // Given
        String templatePath = "template.pdf";
        String text = "   ";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-unnamed.pdf");
    }

    @Test
    void shouldHandleTemplateWithoutExtension() {
        // Given
        String templatePath = "template";
        String text = "Test";
        String prefix = null;
        String postfix = null;
        String format = "pdf";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).isEqualTo("template-Test.pdf");
    }

    @Test
    void shouldUseLowercaseFormat() {
        // Given
        String templatePath = "template.pdf";
        String text = "Test";
        String prefix = null;
        String postfix = null;
        String format = "PDF";

        // When
        String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

        // Then
        assertThat(result).endsWith(".pdf");
    }
}
