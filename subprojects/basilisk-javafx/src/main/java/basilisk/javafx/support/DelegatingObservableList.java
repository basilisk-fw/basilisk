/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basilisk.javafx.support;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DelegatingObservableList<E> extends ObservableListBase<E> implements ObservableList<E> {
    private final ObservableList<E> delegate;
    private ListChangeListener<E> sourceListener;

    public DelegatingObservableList(@Nonnull ObservableList<E> delegate) {
        this.delegate = requireNonNull(delegate, "Argument 'delegate' must not be null");
        this.delegate.addListener(new WeakListChangeListener<>(getListener()));
    }

    @Nonnull
    protected ObservableList<E> getDelegate() {
        return delegate;
    }

    private ListChangeListener<E> getListener() {
        if (sourceListener == null) {
            sourceListener = DelegatingObservableList.this::sourceChanged;
        }
        return sourceListener;
    }

    protected abstract void sourceChanged(@Nonnull ListChangeListener.Change<? extends E> c);

    // --== Delegate methods ==--

    public boolean removeAll(E... elements) {
        return getDelegate().removeAll(elements);
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return getDelegate().removeIf(filter);
    }

    public void remove(int from, int to) {
        getDelegate().remove(from, to);
    }

    public E remove(int index) {
        return getDelegate().remove(index);
    }

    public int size() {
        return getDelegate().size();
    }

    public int lastIndexOf(Object o) {
        return getDelegate().lastIndexOf(o);
    }

    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    public SortedList<E> sorted() {
        return getDelegate().sorted();
    }

    public Stream<E> parallelStream() {
        return getDelegate().parallelStream();
    }

    public boolean addAll(E... elements) {
        return getDelegate().addAll(elements);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return getDelegate().subList(fromIndex, toIndex);
    }

    public Spliterator<E> spliterator() {
        return getDelegate().spliterator();
    }

    public void sort(Comparator<? super E> c) {
        getDelegate().sort(c);
    }

    public E set(int index, E element) {
        return getDelegate().set(index, element);
    }

    public void add(int index, E element) {
        getDelegate().add(index, element);
    }

    public void replaceAll(UnaryOperator<E> operator) {
        getDelegate().replaceAll(operator);
    }

    public boolean containsAll(Collection<?> c) {
        return getDelegate().containsAll(c);
    }

    public void clear() {
        getDelegate().clear();
    }

    public Iterator<E> iterator() {
        return getDelegate().iterator();
    }

    public boolean removeAll(Collection<?> c) {
        return getDelegate().removeAll(c);
    }

    public <T> T[] toArray(T[] a) {
        return getDelegate().toArray(a);
    }

    public boolean remove(Object o) {
        return getDelegate().remove(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        return getDelegate().addAll(c);
    }

    public boolean retainAll(E... elements) {
        return getDelegate().retainAll(elements);
    }

    public boolean retainAll(Collection<?> c) {
        return getDelegate().retainAll(c);
    }

    public SortedList<E> sorted(Comparator<E> comparator) {
        return getDelegate().sorted(comparator);
    }

    public FilteredList<E> filtered(Predicate<E> predicate) {
        return getDelegate().filtered(predicate);
    }

    public Stream<E> stream() {
        return getDelegate().stream();
    }

    public boolean contains(Object o) {
        return getDelegate().contains(o);
    }

    public boolean setAll(Collection<? extends E> col) {
        return getDelegate().setAll(col);
    }

    public ListIterator<E> listIterator(int index) {
        return getDelegate().listIterator(index);
    }

    public boolean add(E e) {
        return getDelegate().add(e);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return getDelegate().addAll(index, c);
    }

    public Object[] toArray() {
        return getDelegate().toArray();
    }

    public ListIterator<E> listIterator() {
        return getDelegate().listIterator();
    }

    public void forEach(Consumer<? super E> action) {
        getDelegate().forEach(action);
    }

    public E get(int index) {
        return getDelegate().get(index);
    }

    public boolean setAll(E... elements) {
        return getDelegate().setAll(elements);
    }

    public int indexOf(Object o) {
        return getDelegate().indexOf(o);
    }
}