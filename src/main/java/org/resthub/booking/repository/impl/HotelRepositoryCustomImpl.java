package org.resthub.booking.repository.impl;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.resthub.booking.model.Hotel;
import org.resthub.booking.repository.HotelRepositoryCustom;
import org.resthub.common.util.PostInitialize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * @author Guillaume Zurbach
 */
@Named("hotelRepositoryImpl")
public class HotelRepositoryCustomImpl implements HotelRepositoryCustom {

	private static final int BATCH_SIZE = 10;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Page<Hotel> find(final String query, final Pageable pageable) {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		// create native Lucene query
		String[] fields = new String[] { "name", "address", "city", "state", "country" };
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, fields, new StandardAnalyzer(
				Version.LUCENE_35));

		Query q;
		try {
			q = parser.parse(query);
		} catch (ParseException ex) {
			return null;
		}

		FullTextQuery persistenceQuery = fullTextEntityManager.createFullTextQuery(q, Hotel.class);

		if (pageable == null) {
			return new PageImpl<Hotel>(persistenceQuery.getResultList());
		} else {
			persistenceQuery.setFirstResult(pageable.getOffset());
			persistenceQuery.setMaxResults(pageable.getPageSize());
			return new PageImpl<Hotel>(persistenceQuery.getResultList(), pageable, persistenceQuery.getResultSize());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebuildIndex() {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		Session session = (Session) fullTextEntityManager.getDelegate();
		FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);

		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(Hotel.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		while (results.next()) {
			index++;
			fullTextSession.index(results.get(0)); // index each element
			if (index % BATCH_SIZE == 0) {
				fullTextSession.flushToIndexes(); // apply changes to indexes
				fullTextSession.clear(); // free memory since the queue is
											// processed
			}
		}
	}
}
