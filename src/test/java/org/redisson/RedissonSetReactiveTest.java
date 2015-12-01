package org.redisson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.redisson.api.RSetReactive;

public class RedissonSetReactiveTest extends BaseReactiveTest {

    public static class SimpleBean implements Serializable {

        private Long lng;

        public Long getLng() {
            return lng;
        }

        public void setLng(Long lng) {
            this.lng = lng;
        }

    }

    @Test
    public void testRemoveRandom() {
        RSetReactive<Integer> set = redisson.getSet("simple");
        sync(set.add(1));
        sync(set.add(2));
        sync(set.add(3));

        MatcherAssert.assertThat(sync(set.removeRandom()), Matchers.isOneOf(1, 2, 3));
        MatcherAssert.assertThat(sync(set.removeRandom()), Matchers.isOneOf(1, 2, 3));
        MatcherAssert.assertThat(sync(set.removeRandom()), Matchers.isOneOf(1, 2, 3));
        Assert.assertNull(sync(set.removeRandom()));
    }

    @Test
    public void testAddBean() throws InterruptedException, ExecutionException {
        SimpleBean sb = new SimpleBean();
        sb.setLng(1L);
        RSetReactive<SimpleBean> set = redisson.getSet("simple");
        sync(set.add(sb));
        Assert.assertEquals(sb.getLng(), toIterator(set.iterator()).next().getLng());
    }

    @Test
    public void testAddLong() throws InterruptedException, ExecutionException {
        Long sb = 1l;

        RSetReactive<Long> set = redisson.getSet("simple_longs");
        sync(set.add(sb));

        for (Long l : sync(set)) {
            Assert.assertEquals(sb.getClass(), l.getClass());
        }
    }

    @Test
    public void testRemove() throws InterruptedException, ExecutionException {
        RSetReactive<Integer> set = redisson.getSet("simple");
        sync(set.add(1));
        sync(set.add(3));
        sync(set.add(7));

        Assert.assertTrue(sync(set.remove(1)));
        Assert.assertFalse(sync(set.contains(1)));
        Assert.assertThat(sync(set), Matchers.containsInAnyOrder(3, 7));

        Assert.assertFalse(sync(set.remove(1)));
        Assert.assertThat(sync(set), Matchers.containsInAnyOrder(3, 7));

        sync(set.remove(3));
        Assert.assertFalse(sync(set.contains(3)));
        Assert.assertThat(sync(set), Matchers.contains(7));
    }

    @Test
    public void testIteratorSequence() {
        RSetReactive<Long> set = redisson.getSet("set");
        for (int i = 0; i < 1000; i++) {
            sync(set.add(Long.valueOf(i)));
        }

        Set<Long> setCopy = new HashSet<Long>();
        for (int i = 0; i < 1000; i++) {
            setCopy.add(Long.valueOf(i));
        }

        checkIterator(set, setCopy);
    }

    private void checkIterator(RSetReactive<Long> set, Set<Long> setCopy) {
        for (Iterator<Long> iterator = toIterator(set.iterator()); iterator.hasNext();) {
            Long value = iterator.next();
            if (!setCopy.remove(value)) {
                Assert.fail();
            }
        }

        Assert.assertEquals(0, setCopy.size());
    }

    @Test
    public void testLong() {
        RSetReactive<Long> set = redisson.getSet("set");
        sync(set.add(1L));
        sync(set.add(2L));

        Assert.assertThat(sync(set), Matchers.containsInAnyOrder(1L, 2L));
    }

    @Test
    public void testRetainAll() {
        RSetReactive<Integer> set = redisson.getSet("set");
        for (int i = 0; i < 20000; i++) {
            sync(set.add(i));
        }

        Assert.assertTrue(sync(set.retainAll(Arrays.asList(1, 2))));
        Assert.assertThat(sync(set), Matchers.containsInAnyOrder(1, 2));
        Assert.assertEquals(2, sync(set.size()).intValue());
    }

    @Test
    public void testContainsAll() {
        RSetReactive<Integer> set = redisson.getSet("set");
        for (int i = 0; i < 200; i++) {
            sync(set.add(i));
        }

        Assert.assertTrue(sync(set.containsAll(Collections.emptyList())));
        Assert.assertTrue(sync(set.containsAll(Arrays.asList(30, 11))));
        Assert.assertFalse(sync(set.containsAll(Arrays.asList(30, 711, 11))));
    }

    @Test
    public void testContains() {
        RSetReactive<TestObject> set = redisson.getSet("set");

        sync(set.add(new TestObject("1", "2")));
        sync(set.add(new TestObject("1", "2")));
        sync(set.add(new TestObject("2", "3")));
        sync(set.add(new TestObject("3", "4")));
        sync(set.add(new TestObject("5", "6")));

        Assert.assertTrue(sync(set.contains(new TestObject("2", "3"))));
        Assert.assertTrue(sync(set.contains(new TestObject("1", "2"))));
        Assert.assertFalse(sync(set.contains(new TestObject("1", "9"))));
    }

    @Test
    public void testDuplicates() {
        RSetReactive<TestObject> set = redisson.getSet("set");

        sync(set.add(new TestObject("1", "2")));
        sync(set.add(new TestObject("1", "2")));
        sync(set.add(new TestObject("2", "3")));
        sync(set.add(new TestObject("3", "4")));
        sync(set.add(new TestObject("5", "6")));

        Assert.assertEquals(4, sync(set.size()).intValue());
    }

    @Test
    public void testSize() {
        RSetReactive<Integer> set = redisson.getSet("set");
        sync(set.add(1));
        sync(set.add(2));
        sync(set.add(3));
        sync(set.add(3));
        sync(set.add(4));
        sync(set.add(5));
        sync(set.add(5));

        Assert.assertEquals(5, sync(set.size()).intValue());
    }


    @Test
    public void testRetainAllEmpty() {
        RSetReactive<Integer> set = redisson.getSet("set");
        sync(set.add(1));
        sync(set.add(2));
        sync(set.add(3));
        sync(set.add(4));
        sync(set.add(5));

        Assert.assertTrue(sync(set.retainAll(Collections.<Integer>emptyList())));
        Assert.assertEquals(0, sync(set.size()).intValue());
    }

    @Test
    public void testRetainAllNoModify() {
        RSetReactive<Integer> set = redisson.getSet("set");
        sync(set.add(1));
        sync(set.add(2));

        Assert.assertFalse(sync(set.retainAll(Arrays.asList(1, 2)))); // nothing changed
        Assert.assertThat(sync(set), Matchers.containsInAnyOrder(1, 2));
    }
}