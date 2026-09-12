package com.sky.cache;

import java.util.Collection;

/**
 * 轻量级布隆过滤器（位数组 + 双重哈希），零第三方依赖。
 *
 * 用途：在查缓存 / 查库之前做一次"一定不存在"的预校验，把必然不存在的请求挡在缓存和数据库之前，
 * 用于解决缓存穿透（拿不存在的 id 反复打 DB）。
 *
 * 参数推导（n = 预期元素个数，p = 可接受误判率）：
 *   m = -n * ln(p) / (ln2)^2      位数组长度
 *   k = (m / n) * ln2             哈希函数个数
 *
 * 特性：只会有"假阳性"（说可能存在时可能误判），不会有"假阴性"——
 * 只要 mightContain 返回 false，就能 100% 断定这个 key 不存在。
 */
public class BloomFilter {

    private final long[] bits;
    private final int bitCount;
    private final int hashFunctions;

    public BloomFilter(long expectedInsertions, double fpp) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException("expectedInsertions must be > 0");
        }
        if (fpp <= 0 || fpp >= 1) {
            throw new IllegalArgumentException("fpp must be in (0, 1)");
        }
        double ln2 = Math.log(2);
        this.bitCount = (int) Math.min(Integer.MAX_VALUE - 8L,
                Math.max(64L, (long) Math.ceil(-expectedInsertions * Math.log(fpp) / (ln2 * ln2))));
        this.hashFunctions = Math.max(1, (int) Math.round((double) bitCount / expectedInsertions * ln2));
        this.bits = new long[(bitCount + 63) >>> 6];
    }

    public boolean add(long value) {
        long h1 = mix64(value);
        long h2 = mix64(value ^ 0x9E3779B97F4A7C15L);
        boolean changed = false;
        for (int i = 0; i < hashFunctions; i++) {
            int index = index(h1, h2, i);
            long mask = 1L << (index & 63);
            if ((bits[index >>> 6] & mask) == 0) {
                bits[index >>> 6] |= mask;
                changed = true;
            }
        }
        return changed;
    }

    public boolean mightContain(long value) {
        long h1 = mix64(value);
        long h2 = mix64(value ^ 0x9E3779B97F4A7C15L);
        for (int i = 0; i < hashFunctions; i++) {
            int index = index(h1, h2, i);
            if ((bits[index >>> 6] & (1L << (index & 63))) == 0) {
                return false;
            }
        }
        return true;
    }

    public void addAll(Collection<Long> values) {
        if (values == null) {
            return;
        }
        for (Long value : values) {
            if (value != null) {
                add(value);
            }
        }
    }

    public int bitCount() {
        return bitCount;
    }

    public int hashFunctions() {
        return hashFunctions;
    }

    private int index(long h1, long h2, int i) {
        // 注意：Math.floorMod(long, int) 是 Java 9 才有的重载，JDK 8 下必须显式转成 (long, long)
        return (int) Math.floorMod(h1 + (long) i * h2, (long) bitCount);
    }

    /** splitmix64 finalizer：把一个 long 打散，雪崩效应良好 */
    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}