package org.sg.ehcache.bootstrapper;

import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;

public class BootstrapCacheLoaderClass implements BootstrapCacheLoader {

	private int threadCount = 1;
	private int sleepTime = 200;
	
	/**
	 * Constructor to create a Cache Loader.
	 * @param threadCount
	 * @param sleepTime
	 */
	public BootstrapCacheLoaderClass(int threadCount, int sleepTime) {
		if (threadCount == 0) {
			this.threadCount = 3;
		}
		this.threadCount = threadCount;
		this.sleepTime = sleepTime;
	}
	
	
	public Object clone() throws CloneNotSupportedException { 
		// TODO Auto-generated method stub 
		return super.clone(); 
	} 

	
	@Override
	public boolean isAsynchronous() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Ehcache ehcache;
	private int start; 
	private int end; 
	private List keys = new LinkedList();

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoader#load(net.sf.ehcache.Ehcache)
	 * Main method to load the data into the local heap/offheap
	 */
	@Override
	public void load(Ehcache cache) throws CacheException {
		// TODO Auto-generated method stub
		
		System.out.println("*** Starting Bootstrap Loading for " + cache.getName());
		
		this.ehcache = cache;
		keys = cache.getKeysWithExpiryCheck();
		
		if (keys.size() > 0) {
			System.out.println("*** Loading a total of: " + keys.size() + " objects, using " + threadCount + " threads - Sleep time set to " + sleepTime + " msecs...");
		
			int objectsPerThread = keys.size() / threadCount;
			
			this.start = 0;
			this.end = start + objectsPerThread;
			
				
			Thread[] threads = new Thread[threadCount];
			for (int i = 0; i < threads.length; i++) {

				//System.out.println("Starting Thread " + i);
				threads[i] = new Thread() {

					public void run() {
						System.out.println("Thread: " + Thread.currentThread().getId() + " is processing items " + Integer.valueOf(start) + " up to " + Integer.valueOf(end));
						List<String> subList = keys.subList(Integer.valueOf(start), Integer.valueOf(end));
						loadData(subList);
					}
				};
				threads[i].start();
				
				// Timing issue - need to wait for the thread to initiate first. Give it a second to start! 
				// WIll need to check why later on...
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				start = start + objectsPerThread;
				end = end + objectsPerThread;
			}
			
			for (int i = 0; i < threads.length; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			
		} else {
			System.out.println("*** Cache is empty - Will exit! ...");
		}

		System.out.println("*** Finished Bootstrap Loading for " + cache.getName());
		
		//Only for testing purposes :)
		//System.exit(0);
		
	}
	
	
	
	
	/**
	 * Method to load the data into the local heap/offheap. 
	 * @param items
	 */
	private void loadData(List items) {
		
		for (int i = 0; i < items.size(); i++) {
			ehcache.get(items.get(i));
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		// Lets not take the getAll, as it can lead to OOME
		//cache.getAll(items);
		
	}

}