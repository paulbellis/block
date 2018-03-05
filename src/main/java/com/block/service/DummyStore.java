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
			return m.putIfAbsent(key, value);
		} finally {
			w.unlock();
		}
	}

	public void transfer(String from, String to, int amount, String hash) throws InsufficientFundsException, AccountNotExistException {
		if (m.get(from)==null || m.get(to)==null) {
			throw new AccountNotExistException("either " + from + " or " + to);
		}
		boolean fromUpdated = false;
		while (!fromUpdated) {
			AccountBalance oldFrom = m.get(from);
			if (oldFrom.getBalance()<amount) {
				throw new InsufficientFundsException("account " + from + " balance=" + oldFrom.getBalance() + ", amount=" + amount);
			}
			AccountBalance newFrom  = new AccountBalance(from, oldFrom.getBalance()-amount, oldFrom.getIns(), oldFrom.getOuts()+amount);
			if (m.replace(from, oldFrom, newFrom)) {
				fromUpdated = true;
				boolean toUpdated = false;
				while (!toUpdated) {
					AccountBalance oldTo = m.get(to);
					AccountBalance newTo = new AccountBalance(to, oldTo.getBalance()+amount, oldFrom.getIns()+amount, oldFrom.getOuts());
					if (m.replace(to, oldTo, newTo)) {
						toUpdated = true;
					}
				}
			}
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
