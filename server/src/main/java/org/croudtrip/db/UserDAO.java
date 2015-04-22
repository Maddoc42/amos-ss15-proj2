package org.croudtrip.db;


import com.google.common.base.Optional;

import org.croudtrip.auth.User;
import org.hibernate.SessionFactory;

import java.util.List;

import javax.inject.Inject;

import io.dropwizard.hibernate.AbstractDAO;

public class UserDAO extends AbstractDAO<User> {

	@Inject
	UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}


	public Optional<User> findById(long id) {
		return Optional.fromNullable(get(id));
	}


	public List<User> findAll() {
		return list(namedQuery(User.QUERY_NAME_FIND_ALL));
	}


	public Optional<User> findByEmail(String email) {
		return Optional.fromNullable(
				uniqueResult(
						namedQuery(User.QUERY_NAME_FIND_BY_EMAIL).setParameter(User.QUERY_PARAM_MAIL, email)));
	}


	public void save(User user) {
		currentSession().save(user);
	}


	public void delete(User user) {
		currentSession().delete(user);
	}

}