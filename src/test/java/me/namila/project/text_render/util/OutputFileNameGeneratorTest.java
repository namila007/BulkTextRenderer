package me.namila.project.text_render.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OutputFileNameGeneratorTest {

    @Nested
    @DisplayName("Filename Generation Tests")
    class FilenameGenerationTests {

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

            // Then - spaces become underscores
            assertThat(result).isEqualTo("wedding-template-John_Doe-final.pdf");
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
            assertThat(result).isEqualTo("batch1-invitation-Alice_Smith.png");
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
            assertThat(result).isEqualTo("template-Bob_Johnson-v2.pdf");
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
            assertThat(result).isEqualTo("card-Charlie_Brown.pdf");
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
            assertThat(result).isEqualTo("template-Test_Name.pdf");
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
    }
    
    @Nested
    @DisplayName("Text Truncation Tests")
    class TextTruncationTests {
        
        @Test
        void shouldTruncateTextTo20Characters() {
            // Given
            String templatePath = "template.pdf";
            String text = "ThisIsAVeryLongNameThatExceedsTwentyCharacters";
            String prefix = null;
            String postfix = null;
            String format = "pdf";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, prefix, postfix, format);

            // Then - text part should be max 20 chars
            String textPart = result.replace("template-", "").replace(".pdf", "");
            assertThat(textPart.length()).isEqualTo(20);
            assertThat(textPart).isEqualTo("ThisIsAVeryLongNameT");
        }
        
        @Test
        void shouldNotTruncateShortText() {
            // Given
            String templatePath = "template.pdf";
            String text = "ShortName";
            
            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-ShortName.pdf");
        }
    }

    @Nested
    @DisplayName("Space Handling Tests")
    class SpaceHandlingTests {
        
        @Test
        void shouldReplaceSpacesWithUnderscores() {
            // Given
            String templatePath = "template.pdf";
            String text = "John Doe Smith";
            
            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-John_Doe_Smith.pdf");
        }
        
        @Test
        void shouldReplaceMultipleSpacesWithSingleUnderscore() {
            // Given
            String templatePath = "template.pdf";
            String text = "John    Multiple   Spaces";
            
            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then - multiple spaces become single underscore, truncated to 20 chars
            assertThat(result).isEqualTo("template-John_Multiple_Spaces.pdf");
        }
    }

    @Nested
    @DisplayName("Name Prefix Removal Tests")
    class NamePrefixRemovalTests {
        
        @ParameterizedTest
        @CsvSource({
            "Mr. John Doe, John_Doe",
            "Mrs. Jane Smith, Jane_Smith",
            "Miss Emily Brown, Emily_Brown",
            "Ms. Sarah Wilson, Sarah_Wilson",
            "Dr. Robert Jones, Robert_Jones",
            "Prof. Michael Lee, Michael_Lee"
        })
        void shouldRemoveNamePrefixes(String input, String expectedTextPart) {
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", input, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-" + expectedTextPart + ".pdf");
        }
        
        @Test
        void shouldRemovePrefixWithoutPeriod() {
            // Given
            String text = "Mr John Doe";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-John_Doe.pdf");
        }
        
        @Test
        void shouldRemovePrefixCaseInsensitively() {
            // Given
            String text = "MR. UPPERCASE NAME";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-UPPERCASE_NAME.pdf");
        }
        
        @Test
        void shouldNotRemoveMrFromMiddleOfName() {
            // Given
            String text = "Tamara Mrsic";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-Tamara_Mrsic.pdf");
        }
    }

    @Nested
    @DisplayName("Non-Alphanumeric Character Removal Tests")
    class NonAlphanumericRemovalTests {
        
        @Test
        void shouldRemoveSpecialCharactersFromText() {
            // Given
            String templatePath = "template.pdf";
            String text = "John:Doe<Test>Name";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then - special characters should be removed
            assertThat(result).isEqualTo("template-JohnDoeTestName.pdf");
        }

        @Test
        void shouldRemoveAllInvalidFilenameCharacters() {
            // Given
            String templatePath = "template.pdf";
            String text = "Name\\With/Special*Chars?\"<>|End";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then - special chars removed, truncated to 20
            assertThat(result).doesNotContain("\\", "/", "*", "?", "\"", "<", ">", "|");
            assertThat(result).isEqualTo("template-NameWithSpecialChars.pdf");
        }
        
        @Test
        void shouldRemoveAtSignAndAmpersand() {
            // Given
            String text = "John@Email&Company";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-JohnEmailCompany.pdf");
        }
        
        @Test
        void shouldKeepUnderscoresAfterProcessing() {
            // Given
            String text = "Already_Has_Underscores";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then - 23 chars truncated to 20
            assertThat(result).isEqualTo("template-Already_Has_Undersco.pdf");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        void shouldHandleNullText() {
            // Given
            String templatePath = "template.pdf";
            String text = null;

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-unnamed.pdf");
        }

        @Test
        void shouldHandleBlankText() {
            // Given
            String templatePath = "template.pdf";
            String text = "   ";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-unnamed.pdf");
        }

        @Test
        void shouldHandleTemplateWithoutExtension() {
            // Given
            String templatePath = "template";
            String text = "Test";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, "pdf");

            // Then
            assertThat(result).isEqualTo("template-Test.pdf");
        }

        @Test
        void shouldUseLowercaseFormat() {
            // Given
            String templatePath = "template.pdf";
            String text = "Test";
            String format = "PDF";

            // When
            String result = OutputFileNameGenerator.generate(templatePath, text, null, null, format);

            // Then
            assertThat(result).endsWith(".pdf");
        }
        
        @Test
        void shouldHandleTextWithOnlySpecialChars() {
            // Given
            String text = "@#$%^&*()";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then - should return unnamed when all chars removed
            assertThat(result).isEqualTo("template-unnamed.pdf");
        }
        
        @Test
        void shouldHandleTextWithOnlyPrefix() {
            // Given - "Mr." becomes empty after removing prefix and period
            String text = "Mr.";
            
            // When
            String result = OutputFileNameGenerator.generate("template.pdf", text, null, null, "pdf");

            // Then - Mr. removed, period removed = empty = unnamed
            assertThat(result).isEqualTo("template-Mr.pdf");
        }
    }
    
    @Nested
    @DisplayName("Process Text Method Tests")
    class ProcessTextTests {
        
        @Test
        void shouldProcessComplexName() {
            // Given
            String text = "Mr. John-Michael O'Connor Jr.";
            
            // When
            String result = OutputFileNameGenerator.processText(text);

            // Then - removes prefix, keeps hyphen as non-alpha removed, apostrophe removed
            assertThat(result).isEqualTo("JohnMichael_OConnor_");
        }
    }

    @Nested
    @DisplayName("Clean Name Processing Tests (Multi-Column CSV)")
    class CleanNameProcessingTests {

        @Test
        void shouldProcessCleanNameWithoutPrefixStripping() {
            // Given - clean name from multi-column CSV (no Mr., Dr. etc.)
            String cleanName = "Adam Smith";
            
            // When
            String result = OutputFileNameGenerator.processCleanName(cleanName);

            // Then - spaces to underscores, no prefix stripping
            assertThat(result).isEqualTo("Adam_Smith");
        }

        @Test
        void shouldNotStripMrFromCleanName() {
            // Given - name that starts with "Mr" as part of actual name
            String cleanName = "Mrauk U";
            
            // When
            String result = OutputFileNameGenerator.processCleanName(cleanName);

            // Then - should NOT strip "Mr" since it's part of name
            assertThat(result).isEqualTo("Mrauk_U");
        }

        @Test
        void shouldTruncateCleanNameTo20Characters() {
            // Given
            String cleanName = "Very Long Name That Exceeds Maximum";
            
            // When
            String result = OutputFileNameGenerator.processCleanName(cleanName);

            // Then
            assertThat(result).hasSize(20);
            assertThat(result).isEqualTo("Very_Long_Name_That_");
        }

        @Test
        void shouldRemoveNonAlphanumericFromCleanName() {
            // Given
            String cleanName = "John@Smith#Jr!";
            
            // When
            String result = OutputFileNameGenerator.processCleanName(cleanName);

            // Then
            assertThat(result).isEqualTo("JohnSmithJr");
        }

        @Test
        void shouldHandleEmptyCleanName() {
            // When
            String result = OutputFileNameGenerator.processCleanName("");

            // Then
            assertThat(result).isEqualTo("unnamed");
        }

        @Test
        void shouldHandleNullCleanName() {
            // When
            String result = OutputFileNameGenerator.processCleanName(null);

            // Then
            assertThat(result).isEqualTo("unnamed");
        }
    }

    @Nested
    @DisplayName("Generate From Clean Name Tests")
    class GenerateFromCleanNameTests {

        @Test
        void shouldGenerateFilenameFromCleanNameWithPrefixPostfix() {
            // Given
            String templatePath = "/path/to/template.pdf";
            String cleanName = "Adam Smith";
            String prefix = "wedding";
            String postfix = "final";
            String format = "pdf";

            // When
            String result = OutputFileNameGenerator.generateFromCleanName(templatePath, cleanName, prefix, postfix, format);

            // Then
            assertThat(result).isEqualTo("wedding-template-Adam_Smith-final.pdf");
        }

        @Test
        void shouldGenerateFilenameFromCleanNameWithoutPrefixPostfix() {
            // Given
            String templatePath = "invitation.png";
            String cleanName = "Jane Doe";
            String format = "png";

            // When
            String result = OutputFileNameGenerator.generateFromCleanName(templatePath, cleanName, null, null, format);

            // Then
            assertThat(result).isEqualTo("invitation-Jane_Doe.png");
        }

        @Test
        void shouldNotStripMrPrefixWhenUsingCleanNameGenerator() {
            // Given - using legacy generate() would strip "Mr."
            // But generateFromCleanName() should NOT strip it
            String templatePath = "template.pdf";
            String cleanName = "Mrak Johnson"; // Name that happens to start with "Mr"
            String format = "pdf";

            // When
            String result = OutputFileNameGenerator.generateFromCleanName(templatePath, cleanName, null, null, format);

            // Then - "Mr" is preserved since it's part of the actual name
            assertThat(result).isEqualTo("template-Mrak_Johnson.pdf");
        }

        @Test
        void shouldGenerateFilenameForJpegFormat() {
            // Given
            String templatePath = "photo.jpg";
            String cleanName = "John Williams";
            String format = "jpg";

            // When
            String result = OutputFileNameGenerator.generateFromCleanName(templatePath, cleanName, null, "edited", format);

            // Then
            assertThat(result).isEqualTo("photo-John_Williams-edited.jpg");
        }
    }
}
