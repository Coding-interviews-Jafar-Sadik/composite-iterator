package iterators;

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


public class CompositeIteratorTest {
    @Test
    public void emptyCompositeIteratorWhenNoIteratorsProvided() {
        assertThat(new CompositeIterator<Integer>()).isEmpty();
    }

    @Test
    public void emptyCompositeIteratorWhenAllIteratorsEmpty() {
        // one empty iterator
        assertThat(new CompositeIterator<>(emptyIterator())).isEmpty();
        // two empty iterators
        assertThat(new CompositeIterator<>(emptyIterator(), emptyIterator())).isEmpty();
        // three empty iterators
        assertThat(new CompositeIterator<>(emptyIterator(), emptyIterator(), emptyIterator())).isEmpty();
    }

    @Test
    public void nonEmptyCompositeIteratorForSingleNonEmptyIterator() {
        assertThat(new CompositeIterator<>(iterator(0, 1, 2, 3, 4, 5)))
                .isNotEmpty();
    }

    @Test
    public void expectCompositeIteratorWithASingleElement() {
        int anyInt = 0;

        // Valid for a single-element iterator
        assertThat(new CompositeIterator<>(iterator(anyInt))).hasSize(1);

        // ... as well as for many iterators, one of which is not empty
        assertThat(new CompositeIterator<>(iterator(anyInt), emptyIterator())).hasSize(1);
        assertThat(new CompositeIterator<>(iterator(anyInt), emptyIterator(), emptyIterator())).hasSize(1);
        assertThat(new CompositeIterator<>(emptyIterator(), emptyIterator(), iterator(anyInt))).hasSize(1);
    }

    @Test
    public void expectProperOrderForASingleNonEmptyIterator() {
        // Valid for a single non-empty iterator
        assertThat(new CompositeIterator<>(iterator(0, 1, 2, 3, 4, 5)))
                .containsSequence(0, 1, 2, 3, 4, 5);

        // Valid for a multiple iterators, one of which is non empty
        assertThat(new CompositeIterator<>(iterator(0, 1, 2, 3, 4, 5), emptyIterator(), emptyIterator()))
                .containsSequence(0, 1, 2, 3, 4, 5);

        // ... and orders of empty iterators shouldn't matter
        assertThat(new CompositeIterator<>(emptyIterator(), iterator(0, 1, 2, 3, 4, 5), emptyIterator()))
                .containsSequence(0, 1, 2, 3, 4, 5);
    }

    @Test
    public void expectProperOrderForMultipleNonEmptyIterators() {
        // First element to pick should be the lowest element in any of the iterators, after that the second lowest
        // elements in any of the iterators and so on until we iterate through all the elements of the iterators.
        assertThat(new CompositeIterator<>(
                iterator(1, 4, 6, 7),
                iterator(2, 8, 11),
                iterator(3, 5, 9),
                iterator(10)
        )).containsSequence(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

        assertThat(new CompositeIterator<>(
                iterator(5),
                iterator(4),
                iterator(3),
                iterator(2),
                iterator(1)
        )).containsSequence(1, 2, 3, 4, 5);

        assertThat(new CompositeIterator<>(
                iterator(15, 100, 102),
                iterator(6, 14),
                iterator(4, 11),
                iterator(1, 2, 3, 5, 101),
                iterator()
        )).containsSequence(1, 2, 3, 4, 5, 6, 11, 14, 15, 100, 101, 102);
    }

    @Test
    public void expectProperOrderForMultipleNonEmptyStringIterators() {
        assertThat(new CompositeIterator<>(
                iterator("zebra"),
                iterator("dog"),
                iterator("cat"),
                iterator("butterfly")
        )).containsSequence("butterfly", "cat", "dog", "zebra");

        assertThat(new CompositeIterator<>(
                iterator('a', 'd', 'e', 'f'),
                iterator('b', 'k', 'o'),
                iterator('x', 'y', 'z'),
                iterator('l')
        )).containsSequence('a', 'b', 'd', 'e', 'f', 'k', 'l', 'o', 'x', 'y', 'z');
    }


    @Test
    public void removeOneElementFromSingleCollection() {
        List<Integer> collection = Lists.newArrayList(1);
        CompositeIterator<Integer> it = new CompositeIterator<>(collection.iterator());
        assertThat(it.next()).isEqualTo(1);
        it.remove(); // removes 1 from the backing collection
        assertThat(it.hasNext()).isFalse();
        assertThat(collection).isEmpty();
    }

    @Test
    public void removeManyElementsFromSingleCollection() {
        //Given collection with 6 elements
        List<Integer> collection = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collection.iterator());

        // After removing 2 of them during iteration
        assertThat(iterator.next()).isEqualTo(1);
        assertThat(iterator.next()).isEqualTo(2);
        iterator.remove(); // removes 2 from the backing collection
        assertThat(iterator.next()).isEqualTo(3);
        assertThat(iterator.next()).isEqualTo(4);
        assertThat(iterator.next()).isEqualTo(5);
        iterator.remove(); // removes 5 from the backing collection
        assertThat(iterator.next()).isEqualTo(6);
        assertThat(iterator.hasNext()).isFalse();

        // We expect collection with 4 elements
        assertThat(collection).hasSize(4).containsSequence(1, 3, 4, 6);
    }

    @Test
    public void removeTwoElementsInARow() {
        // Given collection with 4 elements
        List<Integer> collection = Lists.newArrayList(1, 2, 3, 4);
        CompositeIterator<Integer> iterator = new CompositeIterator<>(collection.iterator());

        // After removing 2 elements in row during iteration
        assertThat(iterator.next()).isEqualTo(1);
        assertThat(iterator.next()).isEqualTo(2);
        iterator.remove(); // remove 2 from the backing collection
        assertThat(iterator.next()).isEqualTo(3);
        iterator.remove(); // remove 3 from the backing collection
        assertThat(iterator.next()).isEqualTo(4);
        assertThat(iterator.hasNext()).isFalse();

        // We expect collection with 2 elements
        assertThat(collection).hasSize(2).containsSequence(1, 4);
    }

    @Test
    public void removeOneElementFromMultipleCollections() {
        // Given four non-empty collections
        List<Integer> collectionA = Lists.newArrayList(1, 4, 6, 7),
                collectionB = Lists.newArrayList(2, 8, 11),
                collectionC = Lists.newArrayList(3, 5, 9),
                collectionD = Lists.newArrayList(10);

        CompositeIterator<Integer> iterator = new CompositeIterator<>(collectionA.iterator(), collectionB.iterator(),
                collectionC.iterator(), collectionD.iterator());

        // After removing 1 element during iteration
        assertThat(iterator.next()).isEqualTo(1);
        assertThat(iterator.next()).isEqualTo(2);
        assertThat(iterator.next()).isEqualTo(3);
        assertThat(iterator.next()).isEqualTo(4);
        assertThat(iterator.next()).isEqualTo(5);
        iterator.remove(); // remove 5 from collection C
        assertThat(iterator).containsSequence(6, 7, 8, 9, 10, 11);

        // We expect that it has been removed from proper backing collection
        assertThat(collectionC).containsSequence(3, 9);

        // ... and that all remaining collections stay unaffected.
        assertThat(collectionA).containsSequence(1, 4, 6, 7);
        assertThat(collectionB).containsSequence(2, 8, 11);
        assertThat(collectionD).containsSequence(10);
    }

    @Test
    public void removeManyElementsFromMultipleCollections() {
        // Given four non-empty collections
        List<Integer> collectionA = Lists.newArrayList(1, 4, 6, 7),
                collectionB = Lists.newArrayList(2, 8, 11),
                collectionC = Lists.newArrayList(3, 5, 9),
                collectionD = Lists.newArrayList(10);

        CompositeIterator<Integer> iterator = new CompositeIterator<>(collectionA.iterator(), collectionB.iterator(),
                collectionC.iterator(), collectionD.iterator());

        // After removing several elements during iteration
        assertThat(iterator.next()).isEqualTo(1);
        assertThat(iterator.next()).isEqualTo(2);
        iterator.remove(); // remove 2 from collection B
        assertThat(iterator.next()).isEqualTo(3);
        assertThat(iterator.next()).isEqualTo(4);
        iterator.remove(); // remove 4 from collection A
        assertThat(iterator.next()).isEqualTo(5);
        assertThat(iterator.next()).isEqualTo(6);
        assertThat(iterator.next()).isEqualTo(7);
        iterator.remove(); // remove 7 from collection A
        assertThat(iterator.next()).isEqualTo(8);
        assertThat(iterator.next()).isEqualTo(9);
        assertThat(iterator.next()).isEqualTo(10);
        iterator.remove(); // remove 10 from collection D
        assertThat(iterator.next()).isEqualTo(11);
        assertThat(iterator).isEmpty();

        // We expect that those elements have been removed from proper backing collections
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
        CompositeIterator iterator = new CompositeIterator<>(iterator(1, 2, 3));
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
}
