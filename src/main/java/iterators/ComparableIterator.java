package iterators;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A {@link Comparable} wrapper for a single {@link Iterator}.
 * It caches the recently accessed value as {@code Iterator.next()} can be read only once.
 */
class ComparableIterator<T extends Comparable<T>> implements Comparable<ComparableIterator<T>>, Iterator<ComparableIterator<T>> {

    private final Iterator<T> iterator;
    private final T currentValue;

    public ComparableIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        this.currentValue = iterator.hasNext() ? iterator.next() : null;
    }

    public T currentValue() {
        return currentValue;
    }

    @Override
    public int compareTo(ComparableIterator<T> that) {
        return currentValue.compareTo(that.currentValue);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public ComparableIterator<T> next() {
        return new ComparableIterator<>(iterator);
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super ComparableIterator<T>> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }
}
