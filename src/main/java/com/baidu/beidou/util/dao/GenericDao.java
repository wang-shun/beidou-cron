package com.baidu.beidou.util.dao;



import java.io.Serializable;
import java.util.List;

public interface GenericDao<T, ID extends Serializable> {
	public T findById(ID id);
	
	T findById(ID id, boolean lock);

	List<T> findAll();

	List<T> findByExample(T exampleInstance);

	T makePersistent(T entity);

	void makeTransient(T entity);
	
	void flush();

}
