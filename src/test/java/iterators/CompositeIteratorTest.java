package iterators;

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


public class CompositeIteratorTest {
    private final List<Integer> collectionA = Lists.newArrayList(1, 4, 6, 7);
    private final List<Integer> collectionB = Lists.newArrayList(2, 8, 11);
    private final List<Integer> collectionC = Lists.newArrayList(3, 5, 9);
    private final List<Integer> collectionD = Lists.newArrayList(10);

    @Test
    public void expectEmptyCompositeIteratorWhenNoIteratorsProvided() {
        assertThat(new CompositeIterator<Integer>()).isEmpty();
    }

    @Test
    public void expectEmptyCompositeIteratorWhenAllWrappedIteratorsAreEmpty() {
        assertThat(new CompositeIterator<>(emptyIterator())).isEmpty();
        assertThat(new CompositeIterator<>(emptyIterator(), emptyIterator())).isEmpty();
        assertThat(new CompositeIterator<>(emptyIterator(), emptyIterator(), emptyIterator())).isEmpty();
    }

    @Test
    public void expectNonEmptyCompositeIterator() {
        assertThat(new CompositeIterator<>(iterator(0, 1, 2, 3, 4, 5)))
                .isNotEmpty();
    }

    @Test
    public void shouldIterateInOrderGivenSingleNonEmptyIterator() {
        assertThat(new CompositeIterator<>(iterator(0, 1, 2, 3, 4, 5)))
                .containsSequence(0, 1, 2, 3, 4, 5);
    }

    @Test
    public void iterationShouldNotBeAffectedByEmptyIterators() {
        assertThat(new CompositeIterator<>(emptyIterator(), iterator(0, 1, 2, 3, 4, 5), emptyIterator()))
                .containsSequence(0, 1, 2, 3, 4, 5);
    }

    @Test
    public void expectProperOrderForMultipleIntIterators() {
        assertThat(new CompositeIterator<>(
                iterator(15, 100, 102),
                iterator(6, 14),
                iterator(4, 11),
                iterator(1, 2, 3, 5, 101),
                iterator()
        )).containsSequence(1, 2, 3, 4, 5, 6, 11, 14, 15, 100, 101, 102);
    }

    @Test
    public void expectProperOrderForMultipleStringIterators() {
        assertThat(new CompositeIterator<>(
                iterator("zebra"),
                iterator("dog"),
                iterator("cat"),
                iterator("butterfly")
        )).containsSequence("butterfly", "cat", "dog", "zebra");
    }

    @Test
    public void expectProperOrderForMultipleCharIterators() {
        assertThat(new CompositeIterator<>(
                iterator('a', 'd', 'e', 'f'),
                iterator('b', 'k', 'o'),
                iterator('x', 'y', 'z'),
                iterator('l')
        )).containsSequence('a', 'b', 'd', 'e', 'f', 'k', 'l', 'o', 'x', 'y', 'z');
    }

    @Test
    public void shouldRemoveOneElementFromSingleCollection() {
        // Given a single-element collection
        List<Integer> collection = Lists.newArrayList(1);
        CompositeIterator<Integer> it = new CompositeIterator<>(collection.iterator());

        // After removing this element during iteration
        it.next(); it.remove();

        // Expect an empty collection
        assertThat(it.hasNext()).isFalse();
        assertThat(collection).isEmpty();
    }

    @Test
    public void shouldRemoveManyElementsFromSingleCollection() {
        // Given a collection with 6 elements
        List<Integer> collection = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collection.iterator());

        // After removing 2 of them during iteration
        skip(iterator, 1); iterator.remove();
        skip(iterator, 2); iterator.remove();

        // Expect collection with 4 elements
        assertThat(collection).hasSize(4).containsSequence(1, 3, 4, 6);
    }

    @Test
    public void shouldRemoveTwoElementsInRow() {
        // Given a collection with 4 elements
        List<Integer> collection = Lists.newArrayList(1, 2, 3, 4);
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collection.iterator());

        // After removing 2 elements in a row during iteration
        skip(iterator, 1); iterator.remove();
        iterator.next(); iterator.remove();

        // Expect collection with 2 elements
        assertThat(collection).hasSize(2).containsSequence(1, 4);
    }

    @Test
    public void shouldRemoveOneElementFromMultipleCollections() {
        // Given a composite iterator wrapping four non-empty iterators
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collectionA.iterator(), collectionB.iterator(),
                collectionC.iterator(), collectionD.iterator());

        // After removing 1 element during iteration
        skip(iterator, 4); iterator.remove();
        assertThat(iterator).containsSequence(6, 7, 8, 9, 10, 11);

        // Expect that it was removed from a proper collection
        assertThat(collectionC).containsSequence(3, 9);

        // ... and that all remaining collections stay unaffected.
        assertThat(collectionA).containsSequence(1, 4, 6, 7);
        assertThat(collectionB).containsSequence(2, 8, 11);
        assertThat(collectionD).containsSequence(10);
    }

    @Test
    public void shouldRemoveManyElementsFromMultipleCollections() {
        // Given a composite iterator wrapping four non-empty iterators
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collectionA.iterator(), collectionB.iterator(),
                collectionC.iterator(), collectionD.iterator());

        // After removing several elements during iteration
        skip(iterator, 1); iterator.remove();
        skip(iterator, 1); iterator.remove();
        skip(iterator, 2); iterator.remove();
        skip(iterator, 2); iterator.remove();

        // Expect that proper elements were removed from backing collections
        assertThat(collectionA).containsSequence(1, 6);
        assertThat(collectionB).containsSequence(8, 11);
        assertThat(collectionD).isEmpty();

        //... and that all remaining collections stay unaffected.
        assertThat(collectionC).containsSequence(3, 5, 9);
    }

    @Test(expected = IllegalStateException.class)
    public void failOnAttemptToCallRemoveBeforeNext() {
        new CompositeIterator<>(iterator(1, 2, 3)).remove();
    }

    @Test(expected = IllegalStateException.class)
    public void failOnAttemptToRemoveElementTwice() {
        CompositeIterator<Integer> iterator = new CompositeIterator<>(iterator(1, 2, 3));
        iterator.next();
        iterator.remove();
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenIteratorsAreNotSorted() {
        new CompositeIterator<>(
                iterator(1),
                emptyIterator(),
                iterator(5, 4, 3, 2),   // order is violated here
                iterator(10)
        ).forEachRemaining(ignore());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenAnyOfIteratorsContainDuplicateElements() {
        new CompositeIterator<>(
                iterator(1),
                emptyIterator(),
                iterator(2, 3, 4, 4, 5),   // duplicate elements here
                iterator(10)
        ).forEachRemaining(ignore());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenSetOfIteratorsContainDuplicateElements() {
        new CompositeIterator<>(
                iterator(1),
                emptyIterator(),
                iterator(2, 3, 4, 5), // 4 is duplicate
                iterator(4)           // 4 is duplicate
        ).forEachRemaining(ignore());
    }

    @Test(expected = NoSuchElementException.class)
    public void failWhenIteratorHasNoMoreElements() {
        new CompositeIterator<>(emptyIterator()).next();
    }

    @SuppressWarnings("all")
    @Test(expected = IllegalArgumentException.class)
    public void failInitializationForNullParameterList() {
        Iterator<Integer>[] iterators = null;
        new CompositeIterator(iterators);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failInitializationWhenAtLeastOneIteratorIsNull() {
        new CompositeIterator<>(emptyIterator(), null, emptyIterator());
    }

    private Iterator<Integer> emptyIterator() {
        return iterator();
    }

    private <T> Iterator<T> iterator(T... values) {
        List<T> list = new LinkedList<>();
        Collections.addAll(list, values);
        return list.iterator();
    }

    private Consumer<Integer> ignore() {
        return ignore -> {
        };
    }

    private <T> void skip(Iterator<T> iterator, int count) {
        do {
            iterator.next();
        } while (count-- > 0);
    }
}
