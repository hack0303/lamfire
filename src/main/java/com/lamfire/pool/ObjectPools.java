package com.lamfire.pool;

/**
 * Created with IntelliJ IDEA.
 * User: lamfire
 * Date: 15-2-27
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */
public class ObjectPools {

    public static <E> ObjectPool<E> makeFixedObjectPool(PoolableObjectFactory<E> objectFactory,int maxInstance,int idleInstance){
        FixedObjectPool<E> pool = new FixedObjectPool<E>(objectFactory);
        pool.setMaxInstances(maxInstance);
        pool.setMaxIdle(idleInstance);
        return pool;
    }

    public static <E> ObjectPool<E> makeSingleObjectPool(PoolableObjectFactory<E> objectFactory){
        FixedObjectPool<E> pool = new FixedObjectPool<E>(objectFactory);
        pool.setMaxInstances(1);
        pool.setMaxIdle(1);
        return pool;
    }

    public static <E> ObjectPool<E> makeCachedObjectPool(PoolableObjectFactory<E> objectFactory,int idleInstance){
        CachedObjectPool<E> pool = new CachedObjectPool<E>(objectFactory);
        pool.setMaxIdle(idleInstance);
        return pool;
    }
}
