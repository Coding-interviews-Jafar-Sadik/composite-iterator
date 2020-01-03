package iterators;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link Iterator} that allows ordered traversal through {@link Comparable} items of multiple wrapped iterators.
 * The input iterators are not pooled until necessary.
 */
public class CompositeIterator<T extends Comparable<T>> implements Iterator<T> {
    // A priority queue maintains iterators in an ascending order.
    private final PriorityQueue<ComparableIterator<T>> priorityQueue;

    // Iterator that holds the next value in order
    private ComparableIterator<T> iterator = emptyIterator();

    @SafeVarargs
    public CompositeIterator(Iterator<T>... iterators) {
        requireValidParameterList(iterators);
        priorityQueue = new PriorityQueue<>(wrap(iterators));
    }

    /**
     * Returns true if the iterator has more elements or false otherwise.
     */
    @Override
    public boolean hasNext() {
        return !priorityQueue.isEmpty() || iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @throws IllegalArgumentException provided iterators are either not sorted or contain duplicates
     * @throws NoSuchElementException   if the iterator has no more elements
     */
    @Override
    public T next() {
        T previousValue = iterator.currentValue();
        enqueueNextNonEmptyIterator();
        iterator = dequeueIteratorInOrder();
        failWhenResultIndicatesEmptyIterator(iterator);
        ensureIteratorsSortedWithoutDuplicates(previousValue, iterator.currentValue());
        return iterator.currentValue();
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator.
     *
     * @throws IllegalStateException remove() method called twice on the same element or next() method hasn't been called
     */
    @Override
    public void remove() {
        iterator.remove();
    }

    private void enqueueNextNonEmptyIterator() {
        if (iterator.hasNext()) {
            priorityQueue.add(iterator.next());
        }
    }

    private ComparableIterator<T> dequeueIteratorInOrder() {
        return priorityQueue.poll();
    }

    private void failWhenResultIndicatesEmptyIterator(ComparableIterator e) {
        if (e == null) {
            throw new NoSuchElementException("iterator is empty");
        }
    }

    private void ensureIteratorsSortedWithoutDuplicates(T previousValue, T currentValue) {
        if (previousValue != null) {
            checkArgument(previousValue.compareTo(currentValue) < 0, "at least one iterator not sorted");
            checkArgument(previousValue.compareTo(currentValue) != 0, "duplicate items in one or more iterators");
        }
    }

    private List<ComparableIterator<T>> wrap(Iterator<T>[] iterators) {
        return Stream.of(iterators)
                .filter(Iterator::hasNext)
                .map(ComparableIterator::new)
                .collect(Collectors.toList());
    }

    private void requireValidParameterList(Iterator<T>[] iterators) {
        checkArgument(iterators != null, "null argument list");
        checkArgument(allNonNullIterators(iterators), "null iterator");
    }

    private boolean allNonNullIterators(Iterator<T>[] iterators) {
        return Stream.of(iterators).noneMatch(Objects::isNull);
    }

    private ComparableIterator<T> emptyIterator() {
        return new ComparableIterator<T>(Collections.emptyIterator());
    }
}
