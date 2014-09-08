package com.xqbase.util;

import java.util.Iterator;

class AVLNode<E extends Comparable<E>> {
	private AVLNode<E> left, right;
	private boolean leftIsPrevious, rightIsNext;
	private int height, relativePosition;

	E value;

	AVLNode(int relativePosition, E obj, AVLNode<E> rightFollower, AVLNode<E> leftFollower) {
		this.relativePosition = relativePosition;
		value = obj;
		rightIsNext = true;
		leftIsPrevious = true;
		right = rightFollower;
		left = leftFollower;
	}

	AVLNode<E> get(int index) {
		int diff = index - relativePosition;
		if (diff == 0) {
			return this;
		}
		AVLNode<E> nextNode = ((diff < 0) ? getLeftSubTree() : getRightSubTree());
		if (nextNode == null) {
			return null;
		}
		return nextNode.get(diff);
	}

	int indexOf(E object) {
		if (getLeftSubTree() != null) {
			int result = left.indexOf(object);
			if (result != Integer.MIN_VALUE) {
				return relativePosition + result;
			}
		}
		if (value == null ? value == object : value.equals(object)) {
			return relativePosition;
		}
		if (getRightSubTree() != null) {
			int result = right.indexOf(object);
			if (result != Integer.MIN_VALUE) {
				return relativePosition + result;
			}
		}
		return Integer.MIN_VALUE;
	}

	AVLNode<E> next() {
		return (getRightSubTree() == null) ? right : right.min();
	}

	AVLNode<E> previous() {
		return (getLeftSubTree() == null) ? left : left.max();
	}

	AVLNode<E> insert(E obj) {
		if (obj.compareTo(value) < 0) {
			return insertOnLeft(obj);
		}
		return insertOnRight(obj);
	}

	private AVLNode<E> insertOnLeft(E obj) {
		if (getLeftSubTree() == null) {
			setLeft(new AVLNode<>(-1, obj, this, left), null);
		} else {
			setLeft(left.insert(obj), null);
		}
		if (relativePosition >= 0) {
			relativePosition++;
		}
		AVLNode<E> ret = balance();
		recalcHeight();
		return ret;
	}

	private AVLNode<E> insertOnRight(E obj) {
		if (getRightSubTree() == null) {
			setRight(new AVLNode<>(+1, obj, right, this), null);
		} else {
			setRight(right.insert(obj), null);
		}
		if (relativePosition < 0) {
			relativePosition--;
		}
		AVLNode<E> ret = balance();
		recalcHeight();
		return ret;
	}

	private AVLNode<E> getLeftSubTree() {
		return (leftIsPrevious ? null : left);
	}

	private AVLNode<E> getRightSubTree() {
		return (rightIsNext ? null : right);
	}

	AVLNode<E> max() {
		return (getRightSubTree() == null) ? this : right.max();
	}

	AVLNode<E> min() {
		return (getLeftSubTree() == null) ? this : left.min();
	}

	AVLNode<E> remove(E obj) {
		int diff = obj.compareTo(value);
		if (diff == 0) {
			return removeSelf();
		}
		if (diff > 0) {
			setRight(right.remove(obj), right.right);
			if (relativePosition < 0) {
				relativePosition++;
			}
		} else {
			setLeft(left.remove(obj), left.left);
			if (relativePosition > 0) {
				relativePosition--;
			}
		}
		recalcHeight();
		return balance();
	}

	private AVLNode<E> removeMax() {
		if (getRightSubTree() == null) {
			return removeSelf();
		}
		setRight(right.removeMax(), right.right);
		if (relativePosition < 0) {
			relativePosition++;
		}
		recalcHeight();
		return balance();
	}

	private AVLNode<E> removeMin() {
		if (getLeftSubTree() == null) {
			return removeSelf();
		}
		setLeft(left.removeMin(), left.left);
		if (relativePosition > 0) {
			relativePosition--;
		}
		recalcHeight();
		return balance();
	}

	private AVLNode<E> removeSelf() {
		if (getRightSubTree() == null && getLeftSubTree() == null) {
			return null;
		}
		if (getRightSubTree() == null) {
			if (relativePosition > 0) {
				left.relativePosition += relativePosition + (relativePosition > 0 ? 0 : 1);
			}
			left.max().setRight(null, right);
			return left;
		}
		if (getLeftSubTree() == null) {
			right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
			right.min().setLeft(null, left);
			return right;
		}

		if (heightRightMinusLeft() > 0) {
			// more on the right, so delete from the right
			AVLNode<E> rightMin = right.min();
			value = rightMin.value;
			if (leftIsPrevious) {
				left = rightMin.left;
			}
			right = right.removeMin();
			if (relativePosition < 0) {
				relativePosition++;
			}
		} else {
			// more on the left or equal, so delete from the left
			AVLNode<E> leftMax = left.max();
			value = leftMax.value;
			if (rightIsNext) {
				right = leftMax.right;
			}
			AVLNode<E> leftPrevious = left.left;
			left = left.removeMax();
			if (left == null) {
				// special case where left that was deleted was a double link
				// only occurs when height difference is equal
				left = leftPrevious;
				leftIsPrevious = true;
			}
			if (relativePosition > 0) {
				relativePosition--;
			}
		}
		recalcHeight();
		return this;
	}

	private AVLNode<E> balance() {
		switch (heightRightMinusLeft()) {
		case 1:
		case 0:
		case -1:
			return this;
		case -2:
			if (left.heightRightMinusLeft() > 0) {
				setLeft(left.rotateLeft(), null);
			}
			return rotateRight();
		case 2:
			if (right.heightRightMinusLeft() < 0) {
				setRight(right.rotateRight(), null);
			}
			return rotateLeft();
		default:
			throw new RuntimeException("tree inconsistent!");
		}
	}

	private void recalcHeight() {
		height = Math.max(
				getLeftSubTree() == null ? -1 : getLeftSubTree().height,
				getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
	}

	private int heightRightMinusLeft() {
		return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
	}

	private AVLNode<E> rotateLeft() {
		AVLNode<E> newTop = right; // can't be faedelung!
		AVLNode<E> movedNode = getRightSubTree().getLeftSubTree();

		int newTopPosition = relativePosition + getOffset(newTop);
		int myNewPosition = -newTop.relativePosition;
		int movedPosition = getOffset(newTop) + getOffset(movedNode);

		setRight(movedNode, newTop);
		newTop.setLeft(this, null);

		setOffset(newTop, newTopPosition);
		setOffset(this, myNewPosition);
		setOffset(movedNode, movedPosition);
		return newTop;
	}

	private AVLNode<E> rotateRight() {
		AVLNode<E> newTop = left; // can't be faedelung
		AVLNode<E> movedNode = getLeftSubTree().getRightSubTree();

		int newTopPosition = relativePosition + getOffset(newTop);
		int myNewPosition = -newTop.relativePosition;
		int movedPosition = getOffset(newTop) + getOffset(movedNode);

		setLeft(movedNode, newTop);
		newTop.setRight(this, null);

		setOffset(newTop, newTopPosition);
		setOffset(this, myNewPosition);
		setOffset(movedNode, movedPosition);
		return newTop;
	}

	private void setLeft(AVLNode<E> node, AVLNode<E> previous) {
		leftIsPrevious = (node == null);
		left = (leftIsPrevious ? previous : node);
		recalcHeight();
	}

	private void setRight(AVLNode<E> node, AVLNode<E> next) {
		rightIsNext = (node == null);
		right = (rightIsNext ? next : node);
		recalcHeight();
	}

	private static int getOffset(AVLNode<?> node) {
		if (node == null) {
			return 0;
		}
		return node.relativePosition;
	}

	private static int setOffset(AVLNode<?> node, int newOffest) {
		if (node == null) {
			return 0;
		}
		int oldOffset = getOffset(node);
		node.relativePosition = newOffest;
		return oldOffset;
	}

	private static int getHeight(AVLNode<?> node) {
		return (node == null ? -1 : node.height);
	}
}

public class TreeList<E extends Comparable<E>> implements Iterable<E> {
	AVLNode<E> root = null;

	public void add(E e) {
		if (root == null) {
			root = new AVLNode<>(0, e, null, null);
		} else {
			root = root.insert(e);
		}
	}

	public void remove(E e) {
		if (root == null) {
			return;
		}
		root = root.remove(e);
	}

	public void clear() {
		root = null;
	}

	public int indexOf(E e) {
		if (root == null) {
			return -1;
		}
		int index = root.indexOf(e);
		return index < 0 ? -1 : index;
	}

	public E get(int index) {
		if (root == null) {
			return null;
		}
		AVLNode<E> node = root.get(index);
		return node == null ? null : node.value;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private AVLNode<E> next = root == null ? null : root.min();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public E next() {
				E value = next.value;
				next = next.next();
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Iterator<E> descendingIterator() {
		return new Iterator<E>() {
			private AVLNode<E> next = root == null ? null : root.max();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public E next() {
				E value = next.value;
				next = next.previous();
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Iterable<E> descendingIterable() {
		return new Iterable<E>() {
			@Override
			public Iterator<E> iterator() {
				return descendingIterator();
			}
		};
	}
}