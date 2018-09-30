package com.routon.ad.pkg;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
	private static final int CORE_POOL_SIZE = 4;// 16;
	private static final int MAX_POOL_SIZE = 10;// 32;
	private static final int KEEP_ALIVE_TIME = 5;
	private static final int QUEUE_SIZE = 10;
	
	private BlockingQueue<Runnable> mWorkQueue;

	private ThreadPoolExecutor mThreadPool;

	public TaskExecutor() {
		this(CORE_POOL_SIZE);
	}
	
	public TaskExecutor(int corePoolSize) {
		this(corePoolSize, MAX_POOL_SIZE);
	}
	
	public TaskExecutor(int corePoolSize, int maxPoolSize) {
		this(corePoolSize, maxPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, QUEUE_SIZE);
	}
	
	public TaskExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit, int queueSize) {
		mWorkQueue = new ArrayBlockingQueue<Runnable>(queueSize);		
		mThreadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, mWorkQueue);		
	}

	public void execute(Runnable task) {
		mThreadPool.execute(task);
	}
	
	public void remove(Runnable task) {
		mThreadPool.remove(task);
	}
}
