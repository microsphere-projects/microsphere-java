/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.lang.function;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.Predicates.or;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * The utilities class for {@link Stream}
 *
 * @since 1.0.0
 */
public interface Streams {

    static <T> Stream<T> stream(T... values) {
        return Stream.of(values);
    }

    static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    static <T> Stream<T> filterStream(T[] values, Predicate<? super T> predicate) {
        Stream<T> stream = stream(values);
        return stream.filter(predicate);
    }

    static <T, S extends Iterable<T>> Stream<T> filterStream(S values, Predicate<? super T> predicate) {
        return stream(values).filter(predicate);
    }

    static <T> List<T> filterList(T[] values, Predicate<? super T> predicate) {
        return filterList(asList(values), predicate);
    }

    static <T, S extends Iterable<T>> List<T> filterList(S values, Predicate<? super T> predicate) {
        return filterStream(values, predicate).collect(toList());
    }

    static <T> Set<T> filterSet(T[] values, Predicate<? super T> predicate) {
        return filterSet(ofSet(values), predicate);
    }

    static <T, S extends Iterable<T>> Set<T> filterSet(S values, Predicate<? super T> predicate) {
        // new Set with insertion order
        return filterStream(values, predicate).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    static <T, S extends Iterable<T>> S filter(S values, Predicate<? super T> predicate) {
        final boolean isSet = isSet(values);
        return (S) (isSet ? filterSet(values, predicate) : filterList(values, predicate));
    }

    static <T, S extends Iterable<T>> S filterAll(T[] values, Predicate<? super T>... predicates) {
        return (S) filterAll(asList(values), and(predicates));
    }

    static <T, S extends Iterable<T>> S filterAll(S values, Predicate<? super T>... predicates) {
        return filter(values, and(predicates));
    }

    static <T, S extends Iterable<T>> S filterAny(T[] values, Predicate<? super T>... predicates) {
        return (S) filterAny(asList(values), or(predicates));
    }

    static <T, S extends Iterable<T>> S filterAny(S values, Predicate<? super T>... predicates) {
        return filter(values, or(predicates));
    }

    static <T> T filterFirst(Iterable<T> values, Predicate<? super T>... predicates) {
        return StreamSupport.stream(values.spliterator(), false)
                .filter(and(predicates))
                .findFirst()
                .orElse(null);
    }
}


