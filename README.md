## Iterator of iterators

Implement an `Iterator` that allows traversal through `Comparable` items in multiple wrapped iterators. 
First element to pick should be the lowest element in any of the iterators, after that the second lowest elements in any 
of the iterators and so on until we iterate through all the elements of the iterators. 

Let’s say we have got the following iterators:

```
Iterator 1: [1, 4, 6, 7]
Iterator 2: [2, 8, 11]
Iterator 3: [3, 5, 9]
Iterator 4: [10]
```

The iteration should traverse items as follows: 

```
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
```

For simplicity, let’s assume there are not repeated elements in any iterator, a value can only be present in one 
iterator (so no repeated elements across the iterators either) and the elements in each iterator are already sorted.

i.e.
```
Iterator 1: [1, 4, 4, 6, 7] CANNOT HAPPEN
Iterator 1: [1, 2, 5]
Iterator 2: [2, 3, 6] CANNOT HAPPEN
```
