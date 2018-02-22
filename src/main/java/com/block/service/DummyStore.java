package com.block.service;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.model.AccountBalance;

public class DummyStore {
	private final Map<String, AccountBalance> m = new TreeMap<String, AccountBalance>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public AccountBalance get(String key) {
		r.lock();
		try {
			return m.get(key);
		} finally {
			r.unlock();
		}
	}

	public String[] allKeys() {
		r.lock();
		try {
			return (String[]) m.keySet().toArray();
		} finally {
			r.unlock();
		}
	}

	public AccountBalance put(String key, AccountBalance value) {
		w.lock();
		try {
			return m.put(key, value);
		} finally {
			w.unlock();
		}
	}

	public void transfer(String from, String to, int amount, String hash) throws InsufficientFundsException, AccountNotExistException {
		w.lock();
		try {
			if (m.get(from)==null || m.get(to)==null) {
				throw new AccountNotExistException("either " + from + " or " + to);
			}
			AccountBalance ab = m.get(from);
			if (ab.getBalance() >= amount) {
				ab.setBalance(ab.getBalance() - amount);
				ab.setOuts(ab.getOuts() + amount);
				m.put(from, ab);
				AccountBalance toAb = m.get(to);
				toAb.setBalance(toAb.getBalance() + amount);
				toAb.setIns(toAb.getIns() + amount);
				m.put(to, toAb);
			} else {
				throw new InsufficientFundsException("Insufficient [" + from + "," + to + "," + amount + "," + hash
						+ "] actual = " + ab.getBalance());
			}
		} finally {
			w.unlock();
		}
	}

	public void clear() {
		w.lock();
		try {
			m.clear();
		} finally {
			w.unlock();
		}
	}

	public String dump() {
		return JSON.toJson(m);
	}
}
