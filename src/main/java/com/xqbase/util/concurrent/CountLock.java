package com.xqbase.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountLock extends Count implements Lock {
	private static final long serialVersionUID = 1L;

	private ReentrantLock lock;

	public CountLock() {
		this(false);
	}

	public CountLock(boolean fair) {
		lock = new ReentrantLock(fair);
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		lock.lockInterruptibly();
	}

	@Override
	public boolean tryLock() {
		return lock.tryLock();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		return lock.tryLock(time, unit);
	}

	@Override
	public void unlock() {
		lock.unlock();
	}

	@Override
	public Condition newCondition() {
		return lock.newCondition();
	}
}