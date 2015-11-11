package de.maci.photography.eyebeam.library.storage.persistent;

import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 07.11.15
 */
public class StorablesTest {

    private static final class TestStorable implements Storable<String> {

        private final String value;

        private TestStorable(String value) {
            this.value = value;
        }

        @Nonnull
        @Override
        public String unbox() {
            return value;
        }
    }

    @Test
    public void testUnboxNullSafe_ReturnsEmpty_IfNullIsPassed() throws Exception {
        assertFalse(Storables.unboxNullSafe(null).isPresent());
    }

    @Test
    public void testUnboxNullSafe_ReturnsTheValue_IfStorableContainsABoxedValue() throws Exception {
        assertThat(Storables.unboxNullSafe(new TestStorable("value")), equalTo(Optional.of("value")));
    }
}