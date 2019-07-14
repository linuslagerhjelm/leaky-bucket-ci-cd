# LeakyBucket
This is a simple implementation of a [Leaky bucket](https://en.wikipedia.org/wiki/Leaky_bucket)
data structure as it was described in the book [Release it](https://www.goodreads.com/book/show/1069827.Release_It_). 

In short, a leaky bucket can be set to monitor an operation and keep track of how often the operation fails and throw
an exception if the operation fails too many times within a specific time frame.

Basic usage:

```java
var action = LeakyBucket.monitor(s -> System.out.println(s));
try {
    action.invoke("Hello!");
} catch (OperationFailedException e) {
    e.getTargetException().printStackTrace();
} catch (LeakyBucket.BucketOverflowException e) {
    // Do something in response to the failing operation
}
```



