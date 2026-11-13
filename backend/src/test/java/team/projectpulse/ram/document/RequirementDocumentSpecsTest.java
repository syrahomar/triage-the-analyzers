package team.projectpulse.ram.document;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * PMD UseLocaleWithCaseConversions on {@link RequirementDocumentSpecs#hasTitleLike}: the search term was lowered
 * with the JVM's default locale, while the title column is lowered by the database. On a JVM whose default locale
 * is Turkish, {@code "VISION".toLowerCase()} is {@code "vısıon"} (dotless i), so a title search for "VISION" no
 * longer matches a document titled "Vision and Scope".
 */
class RequirementDocumentSpecsTest {

    private Locale originalDefault;

    @BeforeEach
    void saveDefaultLocale() {
        this.originalDefault = Locale.getDefault();
    }

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(this.originalDefault);
    }

    @Test
    @DisplayName("hasTitleLike lowers the search term the same way whatever the JVM's default locale")
    @SuppressWarnings("unchecked")
    void testHasTitleLikeIsLocaleIndependent() {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));

        Root<RequirementDocument> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<Object> titlePath = mock(Path.class);
        Expression<String> loweredTitle = mock(Expression.class);
        given(root.get("title")).willReturn(titlePath);
        given(criteriaBuilder.lower((Expression) titlePath)).willReturn(loweredTitle);

        RequirementDocumentSpecs.hasTitleLike("VISION").toPredicate(root, query, criteriaBuilder);

        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(criteriaBuilder).like(eq(loweredTitle), pattern.capture());
        assertThat(pattern.getValue()).isEqualTo("%vision%");
    }

}
