package com.xqbase.util;

public class LinkedEntry {
	private LinkedEntry next = this, prev = this;

	public LinkedEntry getNext() {
		return next;
	}

	public LinkedEntry getPrev() {
		return prev;
	}

	public void remove() {
		prev.next = next;
		next.prev = prev;
		next = prev = null;
	}

	public void clear() {
		next = prev = this;
	}

	public void addPrev(LinkedEntry e) {
		e.next = this;
		e.prev = prev;
		prev.next = e;
		prev = e;
	}

	public void addNext(LinkedEntry e) {
		e.prev = this;
		e.next = next;
		next.prev = e;
		next = e;
	}
}